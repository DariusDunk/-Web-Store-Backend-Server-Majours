package com.example.ecomerseapplication.Entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "products", schema = "online_shop")
@Data
@EqualsAndHashCode(exclude = {"productImages", "categoryAttributeSet"})
public class Product {

    @Id
    @Column(name = "product_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "product_name", columnDefinition = "character varying(100)")
    private String productName;

    @JoinColumn(name = "product_category_id")
    @ManyToOne
    private ProductCategory productCategory;

    @Column(name = "original_price_stotinki")
    private int originalPriceStotinki;
    @Column(name = "sale_price_stotinki")
    private int salePriceStotinki;

    @Column(name = "product_code", columnDefinition = "character varying(10)")
    private String productCode;

    @Column(name = "quantity_in_stock")
    private int quantityInStock;

    @JoinColumn(name = "manufacturer_id")
    @ManyToOne
    private Manufacturer manufacturer;

    @Column(name = "product_description")
    private String productDescription;

    private short rating;

    @Column(name = "delivery_cost", columnDefinition = "smallint DEFAULT 0")
    private short deliveryCost;

    @Column(columnDefinition = "character varying(30)")
    private String model;

    @JoinTable(name = "product_attributes", schema = "online_shop",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "attribute_id"))
    @ManyToMany(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    private Set<CategoryAttribute> categoryAttributeSet;

    @Column(name = "main_image_url")
    private String mainImageUrl;

    @OneToMany(mappedBy = "product")
    private List<ProductImage> productImages;

    @OneToMany(cascade = {CascadeType.ALL}, mappedBy = "product")
    private List<Review> reviews;

    public boolean isInStock() {
        return quantityInStock > 0;
    }

}
