package org.lebastudios.theroundtable.pluginspanishbilling.data;

import org.lebastudios.theroundtable.config.data.FileRepresentator;
import org.lebastudios.theroundtable.pluginspanishbilling.PluginSpanishBilling;

import java.io.File;

public class BillingData implements FileRepresentator
{
    public String serieVentas = "A";
    public String serieRectificaciones = "R";
    public char delimitador = '-';

    public String nextReceiptBillNumber = "1";
    public String lastReceiptBillNumberWithPrefix = "No emitido";

    public String nextRectificationBillNumber = "1";
    public String lastRectificationBillNumberWithPrefix = "No emitido";
    
    public String getReceiptBillNumberPrefix()
    {
        return (serieVentas + delimitador).stripLeading();
    }
    
    public String getRectificationBillNumberPrefix()
    {
        return (serieRectificaciones + delimitador).stripLeading();
    }
    
    @Override
    public File getFile()
    {
        return new File(PluginSpanishBilling.getInstance().getPluginFolder(), "billing-config.json");
    }
}
