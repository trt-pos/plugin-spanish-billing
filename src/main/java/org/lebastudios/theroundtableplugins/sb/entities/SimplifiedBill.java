package org.lebastudios.theroundtableplugins.sb.entities;

import org.lebastudios.theroundtableplugins.cr.entities.Receipt;

import java.time.LocalDateTime;

public record SimplifiedBill(String billNumber, LocalDateTime billDate, Receipt.Status receiptStatus,
                             Bill.Status billStatus) {}
