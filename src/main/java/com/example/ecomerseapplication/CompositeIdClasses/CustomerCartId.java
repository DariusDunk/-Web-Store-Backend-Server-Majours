package com.example.ecomerseapplication.CompositeIdClasses;

import com.example.ecomerseapplication.Entities.Customer;
import com.example.ecomerseapplication.Entities.Product;
import jakarta.persistence.Embeddable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Embeddable
@Getter
@NoArgsConstructor
@EqualsAndHashCode
public class CustomerCartId implements Serializable {

    @JoinColumn(name = "product_id")
    @ManyToOne
    private Product product;

    @JoinColumn(name = "customer_id", referencedColumnName = "k_id")
    @ManyToOne
    private Customer customer;

    public CustomerCartId(Product product, Customer customer) {
        this.product = product;
        this.customer = customer;
    }
}
