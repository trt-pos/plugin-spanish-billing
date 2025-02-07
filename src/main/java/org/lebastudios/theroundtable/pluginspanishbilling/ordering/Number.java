package org.lebastudios.theroundtable.pluginspanishbilling.ordering;

public class Number
{
    public static String next(String current)
    {
        if (!validateNumber(current))
        {
            throw new IllegalArgumentException("The number is not valid");
        }
        
        StringBuilder nextNumber = new StringBuilder();

        boolean llevada;
        int i = current.length() - 1;
        
        do
        {
            int digit = Character.getNumericValue(current.charAt(i));
            if (digit == 9)
            {
                nextNumber.insert(0, 0);
                llevada = true;
            }
            else
            {
                nextNumber.insert(0, digit + 1);
                llevada = false;
            }
            i--;
        } while (llevada && i >= 0);
        
        if (llevada)
        {
            nextNumber.insert(0, 1);
            i--;
        }
        
        if (i >= 0)
        {
            nextNumber.insert(0, current.substring(0, i + 1));
        }
        
        return nextNumber.toString();
    }
    
    private static boolean validateNumber(String number)
    {
        return number.matches("[0-9]+");
    }
}
