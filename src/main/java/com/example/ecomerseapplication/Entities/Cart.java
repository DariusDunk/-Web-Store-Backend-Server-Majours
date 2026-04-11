package com.example.ecomerseapplication.Entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "carts", schema = "online_shop")
@Data
@NoArgsConstructor
public class Cart {

    @Column(name = "owner_id")
    @Id
    private Long id;

    @OneToOne()
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @OneToOne()
    @JoinColumn(name = "session_id")
    private Session session;

    public Cart(Customer customer) {
        this.customer = customer;
    }
}
