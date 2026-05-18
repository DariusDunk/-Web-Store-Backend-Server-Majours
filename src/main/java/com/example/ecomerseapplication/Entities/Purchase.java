package com.example.ecomerseapplication.Entities;

import com.example.ecomerseapplication.enums.DeliveryStatus;
import com.example.ecomerseapplication.enums.PaymentMethod;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.List;

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
    private Instant date;

    @Column(name = "total_cost")
    private int totalCost;

    @Column(name = "contact_name", columnDefinition = "character varying(50)")
    private String contactName;

    @Column(name = "contact_number", columnDefinition = "character varying(10)")
    private String contactNumber;

    private String address;

    private String purchaseCode;

    @JoinColumn(name = "session_id")
    @ManyToOne
    private Session session;

    @Column(name = "shipping_fee")
    private int shippingFee;

    @Column(name = "product_total")
    private int productTotal;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_status")
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private DeliveryStatus deliveryStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method")
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private PaymentMethod paymentMethod;

    @Column(name = "email")
    private String email;

    @OneToMany(mappedBy = "purchaseCartId.purchase",
            cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.DETACH},
            orphanRemoval = true)
    List<PurchaseCart> purchaseProducts;

    public Purchase(Customer customer,
                    int totalCost,
                    String contactName,
                    String contactNumber,
                    String address,
                    String purchaseCode,
                    int shippingFee,
                    int productTotal,
                    PaymentMethod paymentMethod) {

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

    public Purchase(Session session,
                    int totalCost,
                    String contactName,
                    String contactNumber,
                    String address,
                    String purchaseCode,
                    int shippingFee,
                    int productTotal,
                    PaymentMethod paymentMethod,
                    String email) {

        this.session = session;
        this.totalCost = totalCost;
        this.contactName = contactName;
        this.contactNumber = contactNumber;
        this.address = address;
        this.purchaseCode = purchaseCode;
        this.shippingFee = shippingFee;
        this.productTotal = productTotal;
        this.deliveryStatus = DeliveryStatus.PROCESSING;
        this.paymentMethod = paymentMethod;
        this.email = email;
    }
}
