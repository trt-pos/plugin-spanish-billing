package org.lebastudios.theroundtable.pluginspanishbilling;

import org.controlsfx.control.action.Action;
import org.hibernate.Session;
import org.lebastudios.theroundtable.MainStageController;
import org.lebastudios.theroundtable.database.Database;
import org.lebastudios.theroundtable.plugincashregister.entities.Receipt;
import org.lebastudios.theroundtable.pluginspanishbilling.entities.Bill;
import org.lebastudios.theroundtable.pluginspanishbilling.entities.BillType;
import org.lebastudios.theroundtable.pluginspanishbilling.entities.BillingConfig;
import org.lebastudios.theroundtable.pluginspanishbilling.ordering.Number;

import java.util.HashMap;
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
                            billNumber.append(existingBill.getId())
                    );
        });
    }

    public void onRequestNewReceiptBillNumber(Integer receiptId, StringBuffer billNumber)
    {
        if (haveAllReceiptsAlreadyBeenBilled(receiptId))
        {
            BillingConfig config = BillingConfig.queryConfig(BillType.SELL);
            billNumber.append(config.getNextBillId());
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
            BillingConfig config = BillingConfig.queryConfig(BillType.RECT);
            billNumber.append(config.getNextBillId());
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

    public void onReceiptBilled(Receipt receipt, String billId, boolean rectification)
    {
        Database.getInstance().connectTransaction(session ->
        {
            Bill bill = new Bill();
            bill.setReceipt(receipt);
            bill.setId(billId);

            HashMap<BillType, BillingConfig> configs = BillingConfig.queryAllConfigs(session);

            BillingConfig config = rectification
                    ? configs.get(BillType.RECT)
                    : configs.get(BillType.SELL);
            
            bill.setType(config.getType());

            session.persist(bill);
            
            String billIdNumber = billId.substring(billId.lastIndexOf(config.getDelimiter()));
            config.setLastBillId(billId);
            config.setNextNumber(Number.next(billIdNumber));
        });
    }

    public boolean haveAllReceiptsAlreadyBeenBilled(Integer... ignore)
    {
        return Database.getInstance().connectQuery(session -> session.createQuery(
                        "select count(*) from Receipt r " +
                                "where r.id not in (select b.receipt.id from Bill b) " +
                                "and r.id not in :ignore",
                        Long.class)
                .setParameter("ignore", List.of(ignore))
                .uniqueResult() == 0);
    }

    public void billAllReceipts()
    {
        Database.getInstance().connectTransaction(session ->
        {
            HashMap<BillType, BillingConfig> configs = BillingConfig.queryAllConfigs(session);

            // Billing the regular receipts
            List<Receipt> notBilledReceipts = session.createQuery("from Receipt r " +
                                    "where r.id not in (select b.receipt.id from Bill b) " +
                                    "and r.id not in (select m.newReceipt.id from ReceiptModification m) " +
                                    "order by r.transaction.date asc",
                            Receipt.class)
                    .getResultList();


            BillingConfig sellConfig = configs.get(BillType.SELL);
            billReceipts(session, notBilledReceipts, sellConfig);

            // Billing the rect receipts
            List<Receipt> notBilledModReceipts = session.createQuery("from Receipt r " +
                            "where r.id not in (select b.receipt.id from Bill b) " +
                            "order by r.transaction.date asc", Receipt.class)
                    .getResultList();

            BillingConfig rectConfig = configs.get(BillType.RECT);
            billReceipts(session, notBilledModReceipts, rectConfig);

            session.merge(sellConfig);
            session.merge(rectConfig);
        });
    }

    private static void billReceipts(Session session, List<Receipt> receipts, BillingConfig config)
    {
        receipts.forEach(r ->
        {
            String billId = config.getNextBillId();

            Bill bill = new Bill();
            bill.setReceipt(r);
            bill.setId(billId);
            bill.setType(config.getType());

            session.persist(bill);

            config.setLastBillId(billId);
            config.setNextNumber(Number.next(config.getNextNumber()));
        });
    }


}
