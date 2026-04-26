package com.example.ecomerseapplication.Entities;

import com.example.ecomerseapplication.CompositeIdClasses.SaleProductId;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "sale_products", schema = "online_shop")
@Data
@NoArgsConstructor
public class SaleProduct {

    @EmbeddedId
    private SaleProductId saleProductId;

    @ManyToOne
    @MapsId("productId")
    private Product product;

    @ManyToOne
    @MapsId("saleId")
    private Sale sale;

    @Column(name = "override_discount_percentage")
    private Short overrideDiscountPercentage;
}
