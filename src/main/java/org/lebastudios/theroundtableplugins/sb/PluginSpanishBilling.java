package org.lebastudios.theroundtableplugins.sb;

import javafx.scene.control.TreeItem;
import lombok.Getter;
import org.lebastudios.theroundtable.MainStageController;
import org.lebastudios.theroundtable.accounts.AccountManager;
import org.lebastudios.theroundtable.config.SettingsItem;
import org.lebastudios.theroundtable.entities.AppInstallation;
import org.lebastudios.theroundtable.fxml2java.CompileFxml;
import org.lebastudios.theroundtableplugins.cr.PluginCashRegisterEvents;
import org.lebastudios.theroundtable.plugins.IPlugin;
import org.lebastudios.theroundtableplugins.sb.config.BillingConfigPaneController;
import org.lebastudios.theroundtableplugins.sb.entities.Bill;
import org.lebastudios.theroundtable.components.IconView;
import org.lebastudios.theroundtable.components.LabeledIconButton;
import org.lebastudios.theroundtableplugins.sb.entities.BillingConfig;

import java.util.List;

@CompileFxml(
        directories = {
                "org/lebastudios/theroundtableplugins/sb",
                "org/lebastudios/theroundtableplugins/sb/config",
        }
)
public class PluginSpanishBilling implements IPlugin
{
    private static final int DB_VERSION = 1;

    @Getter private static PluginSpanishBilling instance;

    @Override
    public void initialize()
    {
        if (!AppInstallation.thisInstalation().isMaster()) return;
        
        instance = this;

        PluginCashRegisterEvents.onRequestReceiptBillNumber.addListener((data) -> 
                BillingManager.getInstance().onRequestReceiptBillNumber(data.receiptId(), data.billNumberOutput()));
        PluginCashRegisterEvents.onRequestNewReceiptBillNumber.addListener((data) ->
                BillingManager.getInstance().onRequestNewReceiptBillNumber(data.receiptId(), data.billNumberOutput()));
        PluginCashRegisterEvents.onRequestNewRectificationBillNumber.addListener((data) ->
                BillingManager.getInstance().onRequestNewRectificationBillNumber(data.receiptId(), data.billNumberOutput()));
        PluginCashRegisterEvents.onReceiptBilled.addListener((data) ->
                BillingManager.getInstance().onReceiptBilled(data.receipt(), data.billNumber(), false));
        PluginCashRegisterEvents.onModifiedReceiptBilled.addListener((data) ->
                BillingManager.getInstance().onReceiptBilled(data.receipt(), data.billNumber(), true));
    }

    @Override
    public List<LabeledIconButton> getHomeButtons()
    {
        if (!AppInstallation.thisInstalation().isMaster()) return List.of();
        
        final var billingSettings = new LabeledIconButton("Facturación", "sb:billing.png", _ ->
        {
            MainStageController.getInstance().setCentralNode(new BillManagementPaneController());
        });
        
        billingSettings.setDisable(!AccountManager.getInstance().isAccountAdmin());
        
        return List.of(billingSettings);
    }

    @Override
    public TreeItem<SettingsItem> getSettingsRootTreeItem()
    {
        if (!AppInstallation.thisInstalation().isMaster()) return null;
        if (!AccountManager.getInstance().isAccountAdmin()) return null;
        
        TreeItem<SettingsItem> root = new TreeItem<>();
        root.setGraphic(new IconView("sb:billing.png"));
        
        SettingsItem item = new SettingsItem(new BillingConfigPaneController());    
        root.setValue(item);
        
        return root;
    }

    @Override
    public List<Class<?>> getPluginEntities()
    {
        return List.of(Bill.class, BillingConfig.class);
    }

    @Override
    public int getDatabaseVersion()
    {
        return DB_VERSION;
    }
}
