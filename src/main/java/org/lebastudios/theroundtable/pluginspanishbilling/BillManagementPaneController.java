package org.lebastudios.theroundtable.pluginspanishbilling;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import org.lebastudios.theroundtable.MainStageController;
import org.lebastudios.theroundtable.controllers.PaneController;
import org.lebastudios.theroundtable.database.Database;
import org.lebastudios.theroundtable.pluginspanishbilling.entities.Bill;
import org.lebastudios.theroundtable.pluginspanishbilling.entities.SimplifiedBill;
import org.lebastudios.theroundtable.components.IconView;
import org.lebastudios.theroundtable.components.PaginableListView;
import org.lebastudios.theroundtable.components.SearchBox;

import java.util.List;

public class BillManagementPaneController extends PaneController<BillManagementPaneController>
{
    @FXML public SearchBox billsSearchBox;
    @FXML public PaginableListView<SimplifiedBill> billsListView;

    @FXML public Button billingStatusButton;

    private final PaginableListView.ItemsGenerator<SimplifiedBill> simplifiedBillItemsGenerator =
            new PaginableListView.ItemsGenerator<>()
            {
                static final String COMMON_HQL = "from Bill b " +
                        "where b.id like :filter";

                @Override
                public List<SimplifiedBill> generateItems(int from, int to)
                {
                    return Database.getInstance().connectQuery(session ->
                    {
                        var a = session.createQuery( "from Bill b  order by b.receipt.transaction.date desc",
                                        Bill.class) 
                                .stream()
                                .map(b -> new SimplifiedBill(
                                        b.getId(),
                                        b.getReceipt().getTransaction().getDate(), 
                                        b.getReceipt().getStatus(),
                                        Bill.Status.DEFAULT
                                ))
                                .toList();

                        System.out.println("Generating items from " + from + " to " + to + ": " + a.size());
                        
                        return a;
                    });
                }

                @Override
                public long count()
                {
                    return Database.getInstance().connectQuery(session ->
                    {
                        return session.createQuery("select count(*) " + COMMON_HQL, Long.class)
                                .setParameter("filter", "%" + billsSearchBox.getText() + "%")
                                .uniqueResult();
                    });
                }
            };

    @Override
    protected void initialize()
    {
        if (BillingManager.getInstance().haveAllReceiptsAlreadyBeenBilled())
        {
            billingStatusButton.setText("Todo se encuentra correctamente sincronizado");
            billingStatusButton.setOnMouseClicked(_ ->
            {
            });
        }
        else
        {
            billingStatusButton.setVisible(true);
            billingStatusButton.setGraphic(new IconView("sync.png"));
            billingStatusButton.setText("Quedan recibos sin numerar");
            billingStatusButton.setOnMouseClicked(_ ->
            {
                BillingManager.getInstance().billAllReceipts();
                MainStageController.getInstance().setCentralNode(new BillManagementPaneController());
            });
        }

        billsListView.setReciclablePaneFactory(BillLabelController::new);
        billsListView.setItemsGenerator(simplifiedBillItemsGenerator);
        billsListView.refresh();

        billsSearchBox.setOnSearch(_ -> billsListView.refresh());
    }
}
