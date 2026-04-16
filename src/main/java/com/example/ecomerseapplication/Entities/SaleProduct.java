package com.example.ecomerseapplication.Entities;

import com.example.ecomerseapplication.CompositeIdClasses.SaleProductId;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "sale_products", schema = "online_shop")
@Data
@NoArgsConstructor
public class SaleProduct {

    @EmbeddedId
    private SaleProductId saleProductId;

    @Column(name = "override_discount_percentage")
    private Short overrideDiscountPercentage;
}
