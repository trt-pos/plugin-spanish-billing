package org.lebastudios.theroundtable.pluginspanishbilling.config;

import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.lebastudios.theroundtable.config.SettingsPaneController;
import org.lebastudios.theroundtable.config.data.JSONFile;
import org.lebastudios.theroundtable.pluginspanishbilling.PluginSpanishBilling;
import org.lebastudios.theroundtable.pluginspanishbilling.data.BillingData;

public class BillingConfigPaneController extends SettingsPaneController
{
    @FXML private Label sellsBillNumberExample;
    @FXML private Label rectificationsBillNumberExample;
    @FXML private TextField sellsSeriesTextField;
    @FXML private TextField rectificationSeriesTextField;
    @FXML private ChoiceBox<String> delimiterChoiceBox;

    @Override
    protected void initialize()
    {
        super.initialize();

        delimiterChoiceBox.getItems().addAll("-", "/", "\\", " ", ":");
        
        var billingData = new JSONFile<>(BillingData.class).get();

        sellsSeriesTextField.setText(billingData.serieVentas);
        rectificationSeriesTextField.setText(billingData.serieRectificaciones);
        delimiterChoiceBox.setValue(String.valueOf(billingData.delimitador));

        updateExamples();
        
        sellsSeriesTextField.textProperty().addListener((_, _, _) -> updateExamples());
        rectificationSeriesTextField.textProperty().addListener((_, _, _) -> updateExamples());
        delimiterChoiceBox.valueProperty().addListener((_, _, _) -> updateExamples());
    }

    @Override
    public void apply()
    {
        var billingData = new JSONFile<>(BillingData.class);
        
        billingData.get().serieVentas = sellsSeriesTextField.getText();
        billingData.get().serieRectificaciones = rectificationSeriesTextField.getText();
        billingData.get().delimitador = delimiterChoiceBox.getValue().charAt(0);
        
        billingData.save();
    }

    private void updateExamples()
    {
        sellsBillNumberExample.setText("\t" + sellsSeriesTextField.getText() + delimiterChoiceBox.getValue() + "1");
        rectificationsBillNumberExample.setText("\t" + rectificationSeriesTextField.getText() + delimiterChoiceBox.getValue() + "1");
    }

    @Override
    public Class<?> getBundleClass()
    {
        return PluginSpanishBilling.class;
    }
}
