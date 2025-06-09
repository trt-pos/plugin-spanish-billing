package org.lebastudios.theroundtableplugins.sb.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.lebastudios.theroundtable.database.PluginTable;
import org.lebastudios.theroundtableplugins.cr.entities.Receipt;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@PluginTable(name = "spain_bill")
public class Bill
{
    @Id
    @Column(name = "id", nullable = false)
    private String id;
    
    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    private BillType type;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receipt_id", referencedColumnName = "id")
    private Receipt receipt;
    
    public enum Status
    {
        DEFAULT, NEED_SYNC_WITH_SERVER;

        public String getIconName() 
        {
            return switch (this) 
            {
                case DEFAULT -> "sb:bill-ok.png";
                case NEED_SYNC_WITH_SERVER -> "sp:warning.png";
            };
        }
        
        public String getIconTooltip()
        {
            return switch (this)
            {
                case DEFAULT -> "Todo se encuentra en perfectas condiciones.";
                case NEED_SYNC_WITH_SERVER -> "Este recibo necesita ser enviado a hacienda.";
            };
        }
    }
}
