package org.lebastudios.theroundtable.pluginspanishbilling;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.lebastudios.theroundtable.MainStageController;
import org.lebastudios.theroundtable.apparience.UIEffects;
import org.lebastudios.theroundtable.controllers.PaneController;
import org.lebastudios.theroundtable.database.Database;
import org.lebastudios.theroundtable.dialogs.ConfirmationTextDialogController;
import org.lebastudios.theroundtable.pluginspanishbilling.config.BillingConfigData;
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

    @FXML public Label receiptBillNumberPrefix;
    @FXML public TextField nextReceiptBillNumber;
    @FXML public Label lastReceiptNumberLabel;

    @FXML public Label rectificationBillNumberPrefix;
    @FXML public TextField nextRectificationBillNumber;
    @FXML public Label lastRectificationNumberLabel;

    @FXML public Button billingStatusButton;

    private final PaginableListView.ItemsGenerator<SimplifiedBill> simplifiedBillItemsGenerator =
            new PaginableListView.ItemsGenerator<>()
            {
                static final String COMMON_HQL = "from Bill b " +
                        "where b.billNumber like %:filter% " +
                        "order by b.billDate desc";

                @Override
                public List<SimplifiedBill> generateItems(int from, int to)
                {
                    return Database.getInstance().connectQuery(session ->
                    {
                        return session.createQuery(COMMON_HQL,
                                        Bill.class)
                                .setParameter("filter", billsSearchBox.getText())
                                .setFirstResult(from)
                                .setMaxResults(to)
                                .stream()
                                .map(b -> new SimplifiedBill(
                                        b.getBillNumber(),
                                        b.getBillDate(),
                                        b.getReceipt().getStatus(),
                                        Bill.Status.DEFAULT
                                ))
                                .toList();
                    });
                }

                @Override
                public long count()
                {
                    return Database.getInstance().connectQuery(session ->
                    {
                        return session.createQuery("select count(*) " + COMMON_HQL, Long.class)
                                .setParameter("filter", billsSearchBox.getText())
                                .uniqueResult();
                    });
                }
            };

    @Override
    protected void initialize()
    {
        BillingConfigData billingConfigData = new BillingConfigData().load();

        receiptBillNumberPrefix.setText(billingConfigData.getReceiptBillNumberPrefix());
        nextReceiptBillNumber.setText(billingConfigData.nextReceiptBillNumber);
        lastReceiptNumberLabel.setText(billingConfigData.lastReceiptBillNumberWithPrefix);

        nextReceiptBillNumber.textProperty().addListener((_, oldValue, newValue) ->
        {
            if (newValue != null && !newValue.matches("\\d*"))
            {
                UIEffects.shakeNode(nextReceiptBillNumber);
                nextReceiptBillNumber.setText(oldValue);
            }
        });

        rectificationBillNumberPrefix.setText(billingConfigData.getRectificationBillNumberPrefix());
        nextRectificationBillNumber.setText(billingConfigData.nextRectificationBillNumber);
        lastRectificationNumberLabel.setText(billingConfigData.lastRectificationBillNumberWithPrefix);

        nextRectificationBillNumber.textProperty().addListener((_, oldValue, newValue) ->
        {
            if (newValue != null && !newValue.matches("\\d*"))
            {
                UIEffects.shakeNode(nextRectificationBillNumber);
                nextRectificationBillNumber.setText(oldValue);
            }
        });

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

    @FXML
    public void saveNextBillNumbers(ActionEvent actionEvent)
    {
        var billingData = new BillingConfigData().load();

        new ConfirmationTextDialogController("Al modificar este valor tenga en cuenta las reglas de numeración de " +
                "facturas de su país. ¿Desea realizar el cambio de todos modos?", response ->
        {
            if (response)
            {
                billingData.nextReceiptBillNumber = nextReceiptBillNumber.getText();
                billingData.nextRectificationBillNumber = nextRectificationBillNumber.getText();
                billingData.save();
            }
        }).instantiate(true);

        nextReceiptBillNumber.setText(billingData.nextReceiptBillNumber);
        nextRectificationBillNumber.setText(billingData.nextRectificationBillNumber);
    }
}
