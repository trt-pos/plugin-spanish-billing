package org.lebastudios.theroundtable.pluginspanishbilling;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import org.lebastudios.theroundtable.config.GlobalPreferencesConfigData;
import org.lebastudios.theroundtable.controllers.PaneController;
import org.lebastudios.theroundtable.pluginspanishbilling.entities.SimplifiedBill;
import org.lebastudios.theroundtable.ui.IconView;
import org.lebastudios.theroundtable.ui.MultipleItemsListView;

import java.time.format.DateTimeFormatter;

public class BillLabelController extends PaneController<BillLabelController> 
        implements MultipleItemsListView.IReciclablePane<SimplifiedBill>
{ 
    @FXML public IconView receiptStatusIcon;
    @FXML public IconView billStatusIcon;
    @FXML public Label textLabel;
    
    private final Tooltip billStatusTooltip = new Tooltip();

    @Override
    protected void initialize()
    {
        Tooltip.install(billStatusIcon, billStatusTooltip);
    }

    @Override
    public PaneController<?> updateItem(SimplifiedBill item, 
            MultipleItemsListView<SimplifiedBill> control)
    {
        receiptStatusIcon.setIconName(item.receiptStatus().getIconName());
        billStatusIcon.setIconName(item.billStatus().getIconName());

        billStatusTooltip.setText(item.billStatus().getIconTooltip());

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(new GlobalPreferencesConfigData().load().dateTimeFormatter);
        textLabel.setText("Factura: " + item.billNumber() + "   " + item.billDate().format(dtf));
        
        return this;
    }
}
