package org.lebastudios.theroundtable.pluginspanishbilling.config;

import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.lebastudios.theroundtable.config.ConfigPaneController;

public class BillingConfigPaneController extends ConfigPaneController<BillingConfigData>
{
    @FXML public Label sellsBillNumberExample;
    @FXML public Label rectificationsBillNumberExample;
    @FXML public TextField sellsSeriesTextField;
    @FXML public TextField rectificationSeriesTextField;
    @FXML public ChoiceBox<String> delimiterChoiceBox;

    public BillingConfigPaneController()
    {
        super(new BillingConfigData(), "Facturación", "billing.png");
    }

    @Override
    public void updateUI(BillingConfigData configData)
    {
        delimiterChoiceBox.getItems().clear();
        delimiterChoiceBox.getItems().addAll("-", "/", "\\", " ", ":");

        sellsSeriesTextField.setText(configData.serieVentas);
        rectificationSeriesTextField.setText(configData.serieRectificaciones);
        delimiterChoiceBox.setValue(String.valueOf(configData.delimitador));

        updateExamples();

        sellsSeriesTextField.textProperty().addListener((_, _, _) -> updateExamples());
        rectificationSeriesTextField.textProperty().addListener((_, _, _) -> updateExamples());
        delimiterChoiceBox.valueProperty().addListener((_, _, _) -> updateExamples());
    }

    @Override
    public void updateConfigData(BillingConfigData configData)
    {
        configData.serieVentas = sellsSeriesTextField.getText();
        configData.serieRectificaciones = rectificationSeriesTextField.getText();
        configData.delimitador = delimiterChoiceBox.getValue().charAt(0);
    }

    @Override
    public boolean validate()
    {
        return true;
    }

    private void updateExamples()
    {
        sellsBillNumberExample.setText("\t" + sellsSeriesTextField.getText() + delimiterChoiceBox.getValue() + "1");
        rectificationsBillNumberExample.setText("\t" + rectificationSeriesTextField.getText() + delimiterChoiceBox.getValue() + "1");
    }
}
