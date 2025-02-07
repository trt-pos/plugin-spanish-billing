package org.lebastudios.theroundtable.pluginspanishbilling.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.lebastudios.theroundtable.plugincashregister.entities.Receipt;

import java.time.LocalDateTime;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "sb_spain_bill")
public class Bill
{
    @Id
    @Column(name = "bill_number")
    private String billNumber;
    
    @Column(name = "bill_date")
    private LocalDateTime billDate;
    
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
                case DEFAULT -> "bill-ok.png";
                case NEED_SYNC_WITH_SERVER -> "warning.png";
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
