package org.lebastudios.theroundtable.pluginspanishbilling;

import javafx.scene.control.TreeItem;
import lombok.Getter;
import org.lebastudios.theroundtable.MainStageController;
import org.lebastudios.theroundtable.accounts.AccountManager;
import org.lebastudios.theroundtable.config.SettingsItem;
import org.lebastudios.theroundtable.fxml2java.CompileFxml;
import org.lebastudios.theroundtable.plugincashregister.PluginCashRegisterEvents;
import org.lebastudios.theroundtable.plugins.IPlugin;
import org.lebastudios.theroundtable.pluginspanishbilling.config.BillingConfigPaneController;
import org.lebastudios.theroundtable.pluginspanishbilling.entities.Bill;
import org.lebastudios.theroundtable.components.IconView;
import org.lebastudios.theroundtable.components.LabeledIconButton;

import java.util.List;

@CompileFxml(
        directories = {
                "org/lebastudios/theroundtable/pluginspanishbilling",
                "org/lebastudios/theroundtable/pluginspanishbilling/config",
        }
)
public class PluginSpanishBilling implements IPlugin
{
    private static final int DB_VERSION = 1;

    @Getter private static PluginSpanishBilling instance;

    @Override
    public void initialize()
    {
        instance = this;

        PluginCashRegisterEvents.onRequestReceiptBillNumber.addListener((receiptId, billNumber) -> 
                BillingManager.getInstance().onRequestReceiptBillNumber(receiptId, billNumber));
        PluginCashRegisterEvents.onRequestNewReceiptBillNumber.addListener((receiptId, billNumber) ->
                BillingManager.getInstance().onRequestNewReceiptBillNumber(receiptId, billNumber));
        PluginCashRegisterEvents.onRequestNewRectificationBillNumber.addListener((receiptId, billNumber) ->
                BillingManager.getInstance().onRequestNewRectificationBillNumber(receiptId, billNumber));
        PluginCashRegisterEvents.onReceiptBilled.addListener((receipt, billNumber) ->
                BillingManager.getInstance().onReceiptBilled(receipt, billNumber, false));
        PluginCashRegisterEvents.onModifiedReceiptBilled.addListener((receipt, billNumber) ->
                BillingManager.getInstance().onReceiptBilled(receipt, billNumber, true));
    }

    @Override
    public List<LabeledIconButton> getHomeButtons()
    {
        final var billingSettings = new LabeledIconButton("Facturación", new IconView("billing.png"), _ ->
        {
            MainStageController.getInstance().setCentralNode(new BillManagementPaneController());
        });
        
        billingSettings.setDisable(!AccountManager.getInstance().isAccountAdmin());
        
        return List.of(billingSettings);
    }

    @Override
    public TreeItem<SettingsItem> getSettingsRootTreeItem()
    {
        if (!AccountManager.getInstance().isAccountAdmin()) return null;
        
        TreeItem<SettingsItem> root = new TreeItem<>();
        root.setGraphic(new IconView("billing.png"));
        
        SettingsItem item = new SettingsItem(new BillingConfigPaneController());    
        root.setValue(item);
        
        return root;
    }

    @Override
    public List<Class<?>> getPluginEntities()
    {
        return List.of(Bill.class);
    }

    @Override
    public int getDatabaseVersion()
    {
        return DB_VERSION;
    }
}
