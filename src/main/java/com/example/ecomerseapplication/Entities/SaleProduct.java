package com.example.ecomerseapplication.Entities;

import com.example.ecomerseapplication.CompositeIdClasses.SaleProductId;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "sale_products", schema = "online_shop")
@Data
@NoArgsConstructor
@EqualsAndHashCode()
public class SaleProduct {

    @EmbeddedId
    private SaleProductId saleProductId;

    @ManyToOne
    @MapsId("productId")
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne
    @MapsId("saleId")
    @JoinColumn(name = "sale_id")
    private Sale sale;

    @Column(name = "override_discount_percentage")
    private Short overrideDiscountPercentage;
}
