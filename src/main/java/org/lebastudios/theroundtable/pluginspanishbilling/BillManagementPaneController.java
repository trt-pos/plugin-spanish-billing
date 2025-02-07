package org.lebastudios.theroundtable.pluginspanishbilling;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import lombok.Getter;
import lombok.NonNull;
import org.lebastudios.theroundtable.MainStageController;
import org.lebastudios.theroundtable.apparience.UIEffects;
import org.lebastudios.theroundtable.config.data.JSONFile;
import org.lebastudios.theroundtable.controllers.PaneController;
import org.lebastudios.theroundtable.dialogs.ConfirmationTextDialogController;
import org.lebastudios.theroundtable.plugincashregister.entities.Receipt;
import org.lebastudios.theroundtable.pluginspanishbilling.data.BillingData;
import org.lebastudios.theroundtable.pluginspanishbilling.entities.Bill;
import org.lebastudios.theroundtable.ui.IconView;
import org.lebastudios.theroundtable.ui.MultipleItemsListView;
import org.lebastudios.theroundtable.ui.SearchBox;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BillManagementPaneController extends PaneController<BillManagementPaneController>
{
    @FXML private SearchBox billsSearchBox;
    @FXML private MultipleItemsListView<SimplifiedBill> billsListView;

    @FXML private Label receiptBillNumberPrefix;
    @FXML private TextField nextReceiptBillNumber;
    @FXML private Label lastReceiptNumberLabel;

    @FXML private Label rectificationBillNumberPrefix;
    @FXML private TextField nextRectificationBillNumber;
    @FXML private Label lastRectificationNumberLabel;

    @FXML private Button billingStatusButton;

    @Override
    protected void initialize()
    {
        BillingData billingData = new JSONFile<>(BillingData.class).get();

        receiptBillNumberPrefix.setText(billingData.getReceiptBillNumberPrefix());
        nextReceiptBillNumber.setText(billingData.nextReceiptBillNumber);
        lastReceiptNumberLabel.setText(billingData.lastReceiptBillNumberWithPrefix);

        nextReceiptBillNumber.textProperty().addListener((_, oldValue, newValue) ->
        {
            if (newValue != null && !newValue.matches("\\d*"))
            {
                UIEffects.shakeNode(nextReceiptBillNumber);
                nextReceiptBillNumber.setText(oldValue);
            }
        });

        rectificationBillNumberPrefix.setText(billingData.getRectificationBillNumberPrefix());
        nextRectificationBillNumber.setText(billingData.nextRectificationBillNumber);
        lastRectificationNumberLabel.setText(billingData.lastRectificationBillNumberWithPrefix);

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

        billsListView.setCellReciclerGenerator(_ -> new MultipleItemsListView.ICellRecicler<>()
        {
            private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            
            @Getter private final HBox graphic;
            @Getter private String text = "";
            
            private final IconView receiptStatusIcon = new IconView();
            private final IconView billStatusIcon = new IconView();
            private final Tooltip billStatusTooltip = new Tooltip();

            {
                System.out.println("Creating cell recicler");
                Tooltip.install(billStatusIcon, billStatusTooltip);

                graphic = new HBox(receiptStatusIcon, billStatusIcon);
                graphic.setSpacing(5);
            }

            @Override
            public void update(@NonNull SimplifiedBill item)
            {
                receiptStatusIcon.setIconName(item.receiptStatus().getIconName());
                billStatusIcon.setIconName(item.billStatus().getIconName());

                billStatusTooltip.setText(item.billStatus().getIconTooltip());

                text = "Factura: " + item.billNumber() + "   " + item.billDate().format(DATE_FORMATTER);
            }
        });

        populateBillsListView("");
        billsSearchBox.setOnSearch(this::populateBillsListView);
    }

    private void populateBillsListView(String filter)
    {
        String hqlFilter = filter.isEmpty() ? "" : " where b.billNumber like '%" + filter + "%'";

        billsListView.setItemsGenerator(
                new MultipleItemsListView.HQLItemsGenerator<>("from Bill b" + hqlFilter + " order by b.billDate desc",
                        Bill.class, b ->
                        new SimplifiedBill(
                                b.getBillNumber(),
                                b.getBillDate(),
                                b.getReceipt().getStatus(),
                                Bill.Status.DEFAULT
                        )
                )
        );
        billsListView.refresh();
    }

    @FXML
    private void saveNextBillNumbers()
    {
        var billingData = new JSONFile<>(BillingData.class);

        new ConfirmationTextDialogController("Al modificar este valor tenga en cuenta las reglas de numeración de " +
                "facturas de su país. ¿Desea realizar el cambio de todos modos?", response ->
        {
            if (response)
            {
                billingData.get().nextReceiptBillNumber = nextReceiptBillNumber.getText();
                billingData.get().nextRectificationBillNumber = nextRectificationBillNumber.getText();
                billingData.save();
            }
        }).instantiate(true);

        nextReceiptBillNumber.setText(billingData.get().nextReceiptBillNumber);
        nextRectificationBillNumber.setText(billingData.get().nextRectificationBillNumber);
    }

    @Override
    public Class<?> getBundleClass()
    {
        return PluginSpanishBilling.class;
    }

    public record SimplifiedBill(String billNumber, LocalDateTime billDate, Receipt.Status receiptStatus,
                                 Bill.Status billStatus) {}
}
