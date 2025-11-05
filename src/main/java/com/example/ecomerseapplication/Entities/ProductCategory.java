package com.example.ecomerseapplication.Entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

@Entity
@Table(name = "product_categories", schema = "online_shop")
@Data
@EqualsAndHashCode(exclude = {"categoryAttributes", "products"})
@ToString(exclude = {"products","categoryAttributes"} )
public class ProductCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_category_id")
    private int id;

    @Column(name = "category_name", columnDefinition = "character varying(30)")
    private String categoryName;

    @Column(name = "category_image")
    private String categoryImage;


    @JoinColumn(name = "parent_category_id")
    @ManyToOne
    private ProductCategory parentCategory;

    @JsonIgnore
    @OneToMany(mappedBy = "productCategory")
    private List<Product> products;

    @JsonIgnore
    @OneToMany(cascade = {CascadeType.ALL}, mappedBy = "productCategory")
    private List<CategoryAttribute> categoryAttributes;
}
