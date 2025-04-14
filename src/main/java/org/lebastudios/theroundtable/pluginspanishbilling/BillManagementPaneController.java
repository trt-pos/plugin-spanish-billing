package org.lebastudios.theroundtable.pluginspanishbilling;

import javafx.event.ActionEvent;
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
import org.lebastudios.theroundtable.controllers.PaneController;
import org.lebastudios.theroundtable.dialogs.ConfirmationTextDialogController;
import org.lebastudios.theroundtable.plugincashregister.entities.Receipt;
import org.lebastudios.theroundtable.pluginspanishbilling.config.BillingConfigData;
import org.lebastudios.theroundtable.pluginspanishbilling.entities.Bill;
import org.lebastudios.theroundtable.ui.IconView;
import org.lebastudios.theroundtable.ui.MultipleItemsListView;
import org.lebastudios.theroundtable.ui.SearchBox;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BillManagementPaneController extends PaneController<BillManagementPaneController>
{
    @FXML public SearchBox billsSearchBox;
    @FXML public MultipleItemsListView<SimplifiedBill> billsListView;

    @FXML public Label receiptBillNumberPrefix;
    @FXML public TextField nextReceiptBillNumber;
    @FXML public Label lastReceiptNumberLabel;

    @FXML public Label rectificationBillNumberPrefix;
    @FXML public TextField nextRectificationBillNumber;
    @FXML public Label lastRectificationNumberLabel;

    @FXML public Button billingStatusButton;

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

        billsListView.setCellReciclerGenerator(_ -> new MultipleItemsListView.ICellRecicler<>()
        {
            private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            
            @Getter private final HBox graphic;
            @Getter private String text = "";
            
            private final IconView receiptStatusIcon = new IconView();
            private final IconView billStatusIcon = new IconView();
            private final Tooltip billStatusTooltip = new Tooltip();

            {
                Tooltip.install(billStatusIcon, billStatusTooltip);

                receiptStatusIcon.setIconSize(30);
                billStatusIcon.setIconSize(30);
                
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

    public record SimplifiedBill(String billNumber, LocalDateTime billDate, Receipt.Status receiptStatus,
                                 Bill.Status billStatus) {}
}
