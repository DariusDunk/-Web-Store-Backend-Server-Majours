package com.example.ecomerseapplication.Entities;

import com.example.ecomerseapplication.CompositeIdClasses.CustomerCartId;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "cart_products",schema = "online_shop")
@Getter
@Setter
@NoArgsConstructor
public class CartProduct {

    @EmbeddedId
    private CustomerCartId customerCartId;

    private short quantity;

    @Column(name = "date_added", updatable = false)
    @CreationTimestamp
    private Instant dateAdded;

    public CartProduct(CustomerCartId customerCartId, short quantity) {
        this.customerCartId = customerCartId;
        this.quantity = quantity;
    }

}
