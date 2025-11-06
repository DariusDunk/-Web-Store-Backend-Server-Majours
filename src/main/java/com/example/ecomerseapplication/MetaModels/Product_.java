package com.example.ecomerseapplication.MetaModels;

import com.example.ecomerseapplication.Entities.*;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;

import java.util.Set;

@StaticMetamodel(Product.class)
public class Product_ {

    public static volatile SingularAttribute<Product, Integer> id;
    public static final String ID = "id";

    public static volatile SingularAttribute<Product, String > productName;
    public static final String PRODUCT_NAME = "productName";

    public static volatile SingularAttribute<Product, ProductCategory> productCategory;
    public static final String PRODUCT_CATEGORY = "productCategory";

    public static volatile SingularAttribute<Product, Integer> originalPriceStotinki;
    public static final String ORIGINAL_PRICE_STOTINKI = "originalPriceStotinki";

    public static volatile SingularAttribute<Product, Integer> salePriceStotinki;
    public static final String SALE_PRICE_STOTINKI = "salePriceStotinki";

    public static volatile SingularAttribute<Product, String > productCode;
    public static final String PRODUCT_CODE = "productCode";

    public static volatile SingularAttribute<Product, Manufacturer> manufacturer;
    public static final String MANUFACTURER = "manufacturer";

    public static volatile SingularAttribute<Product, String > productDescription;
    public static final String PRODUCT_DESCRIPTION = "productDescription";

    public static volatile SingularAttribute<Product, Short> rating;
    public static final String RATING = "rating";

    public static volatile SingularAttribute<Product, Short> deliveryCost;
    public static final String DELIVERY_COST = "deliveryCost";

    public static volatile SingularAttribute<Product, String> model;
    public static final String MODEL = "model";

    public static volatile SingularAttribute<Product, Set<CategoryAttribute>> categoryAttributeSet;
    public static final String CATEGORY_ATTRIBUTE_SET = "categoryAttributeSet";

    public static volatile SingularAttribute<Product, ProductImage> productImages;
    public static final String PRODUCT_IMAGES = "productImages";

    public static volatile SingularAttribute<Product, Review> reviews;
    public static final String REVIEWS = "reviews";
}
