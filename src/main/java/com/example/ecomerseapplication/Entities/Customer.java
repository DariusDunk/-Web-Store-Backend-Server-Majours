package com.example.ecomerseapplication.Entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "customers", schema = "online_shop")
@Data
@EqualsAndHashCode(exclude = {"reviews","savedPurchaseDetails", "purchases"})
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customer_id")
    private long id;

    @Column(name = "k_id")
    private String keycloakId;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(columnDefinition = "character varying(50)")
    private String email;

    private char[] password;
    @Column(name = "phone_number", columnDefinition = "character varying(10)")
    private String phoneNumber;

    @CreationTimestamp
    @Column(name = "registration_date")
    private LocalDate registrationDate;

    @Column(name = "customer_pfp", columnDefinition = "character varying(255) default 'default_pfp.jpg'")
    private String customerPfp;

    @OneToMany(mappedBy = "customer")
    private List<Purchase> purchases;

    @OneToOne(mappedBy = "customer")
    private SavedPurchaseDetails savedPurchaseDetails;

    @JoinTable(name = "favourites", schema = "online_shop",
    joinColumns = @JoinColumn(name="customer_id", referencedColumnName = "k_id"),//TODO update when the id migration is finished
    inverseJoinColumns = @JoinColumn(name = "product_id"))
    @ManyToMany()
    List<Product> favourites;

    @OneToMany(mappedBy = "customer")
    private List<Review> reviews;

    @OneToMany(mappedBy = "customer")
    private List<Session> sessions;

}
