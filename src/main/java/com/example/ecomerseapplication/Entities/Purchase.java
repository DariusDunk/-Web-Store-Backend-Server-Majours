package com.example.ecomerseapplication.Entities;

import com.example.ecomerseapplication.enums.DeliveryStatus;
import com.example.ecomerseapplication.enums.PaymentMethod;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "purchases", schema = "online_shop")
@Data
@NoArgsConstructor
public class Purchase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "purchase_id")
    private long id;

    @JoinColumn(name = "customer_id")
    @ManyToOne
    private Customer customer;

    @Column(name = "purchase_date", updatable = false)
    @CreationTimestamp
    private Instant date;//todo kato stigne6 do rabota s pokupkite vij dali nqkyde kydeto se izpolzva tazi promenliva nqma da ima nujda ot promeni

    @Column(name = "total_cost")
    private int totalCost;

    @Column(name = "contact_name", columnDefinition = "character varying(50)")
    private String contactName;

    @Column(name = "contact_number", columnDefinition = "character varying(10)")
    private String contactNumber;

    private String address;

    private String purchaseCode;

    @Column(name = "session_id")
    private String sessionId;

    @Column(name = "shipping_fee")
    private int shippingFee;

    @Column(name = "product_total")
    private int productTotal;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_status")
    private DeliveryStatus deliveryStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method")
    private PaymentMethod paymentMethod;

    public Purchase(Customer customer, int totalCost, String contactName, String contactNumber, String address, String purchaseCode, int shippingFee, int productTotal, PaymentMethod paymentMethod) {

        this.customer = customer;
        this.totalCost = totalCost;
        this.contactName = contactName;
        this.contactNumber = contactNumber;
        this.address = address;
        this.purchaseCode = purchaseCode;
        this.shippingFee = shippingFee;
        this.productTotal = productTotal;
        this.deliveryStatus = DeliveryStatus.PROCESSING;
        this.paymentMethod = paymentMethod;
    }

    public Purchase(int totalCost, String contactName, String contactNumber, String address, String purchaseCode, String sessionId, int shippingFee, int productTotal, PaymentMethod paymentMethod) {
        this.totalCost = totalCost;
        this.contactName = contactName;
        this.contactNumber = contactNumber;
        this.address = address;
        this.purchaseCode = purchaseCode;
        this.sessionId = sessionId;
        this.shippingFee = shippingFee;
        this.productTotal = productTotal;
        this.deliveryStatus = DeliveryStatus.PROCESSING;
        this.paymentMethod = paymentMethod;
    }
}
