package org.lebastudios.theroundtable.pluginspanishbilling.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.Session;
import org.lebastudios.theroundtable.database.Database;

import java.util.HashMap;

@Table(name = "sb_billing_config")
@Entity
@Setter
@Getter
@NoArgsConstructor
public class BillingConfig
{
    @Id
    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    private BillType Type;

    @Column(name = "series", nullable = false)
    private String series;
    
    @Column(name = "delimiter", nullable = false)
    private char delimiter;
    
    @Column(name = "next_number", nullable = false)
    private String nextNumber;
    
    @Column(name = "last_bill_number", nullable = false)
    private String lastBillId;

    public static BillingConfig queryConfig(BillType billType)
    {
        return Database.getInstance().connectQuery(session ->
        {
            return session.createQuery(
                            "from BillingConfig bc where bc.Type = :billType",
                            BillingConfig.class)
                    .setParameter("billType", billType)
                    .uniqueResult();
        });
    }

    public String getNextBillId()
    {
        return this.buildString(this.nextNumber);
    }

    public String buildString(String number)
    {
        return (series + delimiter + number).stripLeading();
    }

    public static HashMap<BillType, BillingConfig> queryAllConfigs()
    {
        return Database.getInstance().connectQuery(session ->
        {
            return queryAllConfigs(session);
        });
    }
    
    public static HashMap<BillType, BillingConfig> queryAllConfigs(Session session)
    {
        HashMap<BillType, BillingConfig> tmp = new HashMap<>();

        session.createQuery("from BillingConfig", BillingConfig.class)
                .list()
                .forEach(config -> tmp.put(config.getType(), config));

        return tmp;
    }

}
