package org.lebastudios.theroundtable.pluginspanishbilling;

import org.controlsfx.control.action.Action;
import org.lebastudios.theroundtable.MainStageController;
import org.lebastudios.theroundtable.config.data.JSONFile;
import org.lebastudios.theroundtable.database.Database;
import org.lebastudios.theroundtable.plugincashregister.entities.Receipt;
import org.lebastudios.theroundtable.pluginspanishbilling.data.BillingData;
import org.lebastudios.theroundtable.pluginspanishbilling.entities.Bill;
import org.lebastudios.theroundtable.pluginspanishbilling.ordering.Number;

import java.util.List;

public class BillingManager
{
    private static BillingManager instance;

    public static BillingManager getInstance()
    {
        if (instance == null) instance = new BillingManager();

        return instance;
    }

    private BillingManager() {}

    public void onRequestReceiptBillNumber(Integer receiptId, StringBuffer billNumber)
    {
        Database.getInstance().connectQuery(session ->
        {
            session.createQuery(
                            "from Bill where receipt.id = :receiptId",
                            Bill.class)
                    .setParameter("receiptId", receiptId)
                    .uniqueResultOptional()
                    .ifPresent(existingBill ->
                            billNumber.append(existingBill.getBillNumber())
                    );
        });
    }

    public void onRequestNewReceiptBillNumber(Integer receiptId, StringBuffer billNumber)
    {
        if (haveAllReceiptsAlreadyBeenBilled(receiptId))
        {
            final var billingData = new JSONFile<>(BillingData.class).get();
            billNumber.append(billingData.getReceiptBillNumberPrefix())
                    .append(billingData.nextReceiptBillNumber);
        }
        else
        {
            MainStageController.getInstance().showNotification(
                    "No se ha podido numerar un recibo porque " +
                            "no se han numerado todos los recibos anteriores.",
                    new Action(
                            "Numerar ahora",
                            _ ->
                                    MainStageController.getInstance().setCentralNode(
                                            new BillManagementPaneController()
                                    )
                    )
            );
        }
    }

    public void onRequestNewRectificationBillNumber(Integer receiptId, StringBuffer billNumber)
    {
        if (haveAllReceiptsAlreadyBeenBilled(receiptId))
        {
            final var billingData = new JSONFile<>(BillingData.class).get();
            billNumber.append(billingData.getRectificationBillNumberPrefix())
                    .append(billingData.nextRectificationBillNumber);
        }
        else
        {
            MainStageController.getInstance().showNotification(
                    "No se ha podido numerar un recibo porque " +
                            "no se han numerado todos los recibos anteriores.",
                    new Action(
                            "Numerar ahora",
                            _ ->
                                    MainStageController.getInstance().setCentralNode(
                                            new BillManagementPaneController()
                                    )
                    )
            );
        }
    }

    public void onReceiptBilled(Receipt receipt, String billNumberWithPrefix, boolean rectification)
    {
        var billingData = new JSONFile<>(BillingData.class);

        int prefixLength = (rectification
                ? billingData.get().getRectificationBillNumberPrefix()
                : billingData.get().getReceiptBillNumberPrefix())
                .length();

        String billNumber = billNumberWithPrefix.substring(prefixLength);

        Database.getInstance().connectTransaction(session ->
        {
            Bill bill = new Bill();
            bill.setReceipt(receipt);
            bill.setBillNumber(billNumberWithPrefix);
            bill.setBillDate(receipt.getTransaction().getDate());

            session.persist(bill);

            if (rectification)
            {
                billingData.get().lastRectificationBillNumberWithPrefix = billNumberWithPrefix;
                billingData.get().nextRectificationBillNumber = Number.next(billNumber);
            }
            else
            {
                billingData.get().lastReceiptBillNumberWithPrefix = billNumberWithPrefix;
                billingData.get().nextReceiptBillNumber = Number.next(billNumber);
            }

            billingData.save();
        });
    }

    public String calculateNextBillNumber(String billNumberWithoutSerie)
    {
        if (!billNumberWithoutSerie.matches("\\d+"))
        {
            throw new IllegalArgumentException("Bill number must be a number.");
        }

        return Number.next(billNumberWithoutSerie);
    }

    public boolean haveAllReceiptsAlreadyBeenBilled(Integer... ignore)
    {
        return Database.getInstance().connectQuery(session ->
        {
            return session.createQuery("select count(*) from Receipt r " +
                            "where r.id not in (select b.receipt.id from Bill b) and r.id not in :ignore", Long.class)
                    .setParameter("ignore", List.of(ignore))
                    .uniqueResult() == 0;

        });
    }

    public void billAllReceipts()
    {
        Database.getInstance().connectTransaction(session ->
        {
            var billingData = new JSONFile<>(BillingData.class);

            // Billing the regular receipts
            List<Receipt> notBilledReceipts = session.createQuery("from Receipt r " +
                            "where r.id not in (select b.receipt.id from Bill b) " +
                            "and r.id not in (select m.newReceipt.id from ReceiptModification m) " +
                            "order by r.transaction.date asc", Receipt.class)
                    .getResultList();

            String receiptBillPrefix = billingData.get().getReceiptBillNumberPrefix();

            notBilledReceipts.forEach(r ->
            {
                String nextBillNumber = billingData.get().nextReceiptBillNumber;

                String thisBillNumber = receiptBillPrefix + nextBillNumber;

                Bill bill = new Bill();

                bill.setReceipt(r);
                bill.setBillNumber(thisBillNumber);
                bill.setBillDate(r.getTransaction().getDate());

                session.persist(bill);

                billingData.get().lastReceiptBillNumberWithPrefix = thisBillNumber;
                billingData.get().nextReceiptBillNumber = calculateNextBillNumber(nextBillNumber);
            });

            // Billing the modification receipts
            List<Receipt> notBilledModReceipts = session.createQuery("from Receipt r " +
                            "where r.id not in (select b.receipt.id from Bill b) " +
                            "order by r.transaction.date asc", Receipt.class)
                    .getResultList();

            String modBillPrefix = billingData.get().getRectificationBillNumberPrefix();
            
            notBilledModReceipts.forEach(r ->
            {
                String nextBillNumber = billingData.get().nextRectificationBillNumber;

                String thisBillNumber = modBillPrefix + nextBillNumber;

                Bill bill = new Bill();

                bill.setReceipt(r);
                bill.setBillNumber(thisBillNumber);
                bill.setBillDate(r.getTransaction().getDate());

                session.persist(bill);

                billingData.get().lastRectificationBillNumberWithPrefix = thisBillNumber;
                billingData.get().nextRectificationBillNumber = calculateNextBillNumber(nextBillNumber);
            });

            billingData.save();
        });
    }
}
