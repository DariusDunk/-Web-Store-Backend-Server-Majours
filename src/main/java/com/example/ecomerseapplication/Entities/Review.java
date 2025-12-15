package com.example.ecomerseapplication.Entities;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "reviews", schema = "online_shop")
@Data
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private long id;

    @JoinColumn(name = "product_id")
    @ManyToOne
    private Product product;

    @JoinColumn(name = "customer_id_keyk", referencedColumnName = "k_id", insertable = false, updatable = false)
    @ManyToOne //TODO remove unnecessary details when the id migration in customer is finished
    private Customer customer;

    @Column(name = "review_text", columnDefinition = "character varying(500)")
    private String reviewText;

    private short rating;

    @Column(name = "post_timestamp")
    private LocalDateTime postTimestamp;

    private Boolean verifiedCustomer;
}
