package com.example.ecomerseapplication.Services;

import com.example.ecomerseapplication.DTOs.responses.*;
import com.example.ecomerseapplication.Entities.*;
import com.example.ecomerseapplication.Mappers.ProductDTOMapper;
import com.example.ecomerseapplication.Repositories.CustomerRepository;
import com.example.ecomerseapplication.Repositories.ProductRepository;
import com.example.ecomerseapplication.Specifications.ProductSpecifications;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    private final CustomerCartService customerCartService;

    private final ReviewService reviewService;
    private final ProductCategoryService productCategoryService;
    private final CustomerRepository customerRepository;
    private final FavoriteOfCustomerService favoriteOfCustomerService;

    @Autowired
    public ProductService(ProductRepository productRepository, CustomerCartService customerCartService, ReviewService reviewService, ProductCategoryService productCategoryService, CustomerRepository customerRepository, FavoriteOfCustomerService favoriteOfCustomerService) {
        this.productRepository = productRepository;
        this.customerCartService = customerCartService;
        this.reviewService = reviewService;
        this.productCategoryService = productCategoryService;
        this.customerRepository = customerRepository;
        this.favoriteOfCustomerService = favoriteOfCustomerService;
    }


    public Page<CompactProductResponse> findAllProductsPage(PageRequest pageRequest) {
        return ProductDTOMapper.productPageToDtoPage(productRepository.findAllSortByRating(pageRequest));
    }

    public Page<CompactProductResponse> findAllByRatingResponsePage(PageRequest pageRequest) {
        return productRepository.findAllAsResponseSortByRating(pageRequest);
    }

    public Page<CompactProductResponse> getProductsLikeName(PageRequest pageRequest, String name) {

        Page<Product> productPage = productRepository.getByNameLike(name, pageRequest);

        return ProductDTOMapper.productPageToDtoPage(productPage);
    }

    public List<String> getNameSuggestions(String name) {
        return productRepository.getNameSuggestions(name);
    }

    public ResponseEntity<DetailedProductResponse> getByNameAndCode(String productCode, Customer customer) {
        Product product = productRepository.getByProductCode(productCode).orElse(null);
        if (product == null)
            return ResponseEntity.notFound().build();

        List<AttributeName> attributeNames = new ArrayList<>();

        for (CategoryAttribute categoryAttribute : product.getCategoryAttributeSet()) {
            attributeNames.add(categoryAttribute.getAttributeName());
        }

        List<String[]> attributeNameMUnitPairs = productCategoryService
                .getSpecificAttributesOfCategory(product.getProductCategory().getId(), attributeNames);

        DetailedProductResponse detailedProductResponse = ProductDTOMapper.entityToDetailedResponse(product, attributeNameMUnitPairs, customer.getId());

        if (customerCartService.cartExists(customer, product))
            detailedProductResponse.inCart = true;

//        if (product.getFavouredBy().contains(customer))

        detailedProductResponse.inFavourites = favoriteOfCustomerService.isInFavorites(customer, product);

        if (reviewService.exists(product, customer))
            detailedProductResponse.reviewed = true;


        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(detailedProductResponse);
    }

    public Page<CompactProductResponse> getByManufacturer(Manufacturer manufacturer, Pageable pageable) {

        Page<Product> productPage = productRepository.getByManufacturerOrderByRatingDescIdAsc(manufacturer, pageable);

        return ProductDTOMapper.productPageToDtoPage(productPage);
    }


    public List<CompactProductResponse> getFeaturedProducts() {
        return productRepository.getProductsByRating()
                .stream()
                .map(ProductDTOMapper::entityToCompactResponse)
                .collect(Collectors.toList());
    }

    public Page<CompactProductResponse> getByCategory(ProductCategory productCategory, Pageable pageable) {
        return ProductDTOMapper
                .productPageToDtoPage(productRepository
                        .getByProductCategoryOrderByRatingDesc(productCategory, pageable));
    }

    public Page<CompactProductResponse> getByCategoryFiltersManufacturerAndPriceRange(Set<CategoryAttribute> categoryAttributes,
                                                                                      ProductCategory productCategory,
                                                                                      int priceLowest,
                                                                                      int priceHighest,
                                                                                      List<Manufacturer> manufacturers,
                                                                                      Integer rating,
                                                                                      Pageable pageable) {

        Specification<Product> productSpec =
                ProductSpecifications.equalsCategory(productCategory)
                        .and(ProductSpecifications.priceBetween(priceLowest, priceHighest))
                        .and(ProductSpecifications.ratingEqualOrHigher(rating));

        if (!manufacturers.isEmpty()) {
//            System.out.println("Manufacturers: ");
//            for (Manufacturer manufacturer : manufacturers) {
//                System.out.println(manufacturer.getManufacturerName());
//            }
            productSpec = productSpec.and(ProductSpecifications.manufacturerIn(manufacturers));
        }

//        System.out.println(categoryAttributes.toString());

        if (!categoryAttributes.isEmpty()) {
            productSpec = productSpec.and(ProductSpecifications.containsAttributes(categoryAttributes));
        }

//        productSpec = productSpec

        Page<Product> products = productRepository.findAll(productSpec, pageable);

        return ProductDTOMapper.productPageToDtoPage(products);
    }

    public Product findByPCode(String code) {
        return productRepository.getByProductCode(code).orElse(null);
    }

    public void save(Product product) {
        productRepository.save(product);
    }

    public void saveAll(List<Product> updatedQuantProducts) {
        productRepository.saveAll(updatedQuantProducts);
    }

    public Set<Integer> getRatingsOfCategory(ProductCategory category) {
        Set<Integer> dbrResponse = productRepository.getRatingsByCategory(category).orElse(new HashSet<>());

        Set<Integer> roundResponse = new HashSet<>();

        if (!dbrResponse.isEmpty()) {
            for (Integer i : dbrResponse) {

                roundResponse.add(Integer.parseInt(String.valueOf(i.toString().charAt(0))));
            }
        }

        return roundResponse;
    }

    public Object[] getTotalPriceRangeOfCategory(ProductCategory category) {
        Object result = productRepository.getTotalPriceRange(category);

        return (Object[]) result;
    }

    public List<Product> getByCodes(List<String> codes) {
        return productRepository.getAllByProductCodeIn(codes);
    }
}
