package org.lebastudios.theroundtable.pluginspanishbilling.entities;

import org.lebastudios.theroundtable.plugincashregister.entities.Receipt;

import java.time.LocalDateTime;

public record SimplifiedBill(String billNumber, LocalDateTime billDate, Receipt.Status receiptStatus,
                             Bill.Status billStatus) {}
