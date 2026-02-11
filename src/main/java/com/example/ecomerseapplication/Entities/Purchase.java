package com.example.ecomerseapplication.Entities;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "purchases", schema = "online_shop")
@Data
public class Purchase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "purchase_id")
    private long id;

    @JoinColumn(name = "customer_id_keyk", referencedColumnName = "k_id")
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

}
