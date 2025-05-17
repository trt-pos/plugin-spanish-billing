package org.lebastudios.theroundtable.pluginspanishbilling.config;

import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.lebastudios.theroundtable.config.ConfigPaneController;
import org.lebastudios.theroundtable.config.NoConfigFile;
import org.lebastudios.theroundtable.database.Database;
import org.lebastudios.theroundtable.pluginspanishbilling.entities.BillType;
import org.lebastudios.theroundtable.pluginspanishbilling.entities.BillingConfig;

import java.util.HashMap;

public class BillingConfigPaneController extends ConfigPaneController<NoConfigFile>
{
    @FXML public Label sellsBillNumberExample;
    @FXML public Label rectificationsBillNumberExample;
    @FXML public TextField sellsSeriesTextField;
    @FXML public TextField rectificationSeriesTextField;
    @FXML public ChoiceBox<String> delimiterChoiceBox;
    
    public BillingConfigPaneController()
    {
        super(new NoConfigFile(), "Facturación", "billing.png");
    }

    @Override
    public void updateUI(NoConfigFile configData)
    {
        HashMap<BillType, BillingConfig> billingConfigs = BillingConfig.queryAllConfigs();

        delimiterChoiceBox.getItems().clear();
        delimiterChoiceBox.getItems().addAll("-", "/", "\\", " ", ":");

        BillingConfig sellsConfig = billingConfigs.get(BillType.SELL);
        BillingConfig rectificationConfig = billingConfigs.get(BillType.RECT);
        
        sellsSeriesTextField.setText(sellsConfig.getSeries());
        rectificationSeriesTextField.setText(rectificationConfig.getSeries());
        delimiterChoiceBox.setValue(String.valueOf(sellsConfig.getDelimiter()));

        updateExamples();

        sellsSeriesTextField.textProperty().addListener((_, _, _) -> updateExamples());
        rectificationSeriesTextField.textProperty().addListener((_, _, _) -> updateExamples());
        delimiterChoiceBox.valueProperty().addListener((_, _, _) -> updateExamples());
    }

    @Override
    public void updateConfigData(NoConfigFile configData)
    {
        Database.getInstance().connectTransaction(session ->
        {
            HashMap<BillType, BillingConfig> billingConfigs = BillingConfig.queryAllConfigs(session);
            
            billingConfigs.get(BillType.SELL).setSeries(sellsSeriesTextField.getText());
            billingConfigs.get(BillType.SELL).setDelimiter(delimiterChoiceBox.getValue().charAt(0));
            billingConfigs.get(BillType.RECT).setSeries(rectificationSeriesTextField.getText());
            billingConfigs.get(BillType.RECT).setDelimiter(delimiterChoiceBox.getValue().charAt(0));

            session.merge(billingConfigs.get(BillType.SELL));
            session.merge(billingConfigs.get(BillType.RECT));
            
        });
    }

    @Override
    public ValidationResult validate()
    {
        return ValidationResult.valid();
    }

    private void updateExamples()
    {
        sellsBillNumberExample.setText("\t" + sellsSeriesTextField.getText() + delimiterChoiceBox.getValue() + "1");
        rectificationsBillNumberExample.setText(
                "\t" + rectificationSeriesTextField.getText() + delimiterChoiceBox.getValue() + "1");
    }
}
