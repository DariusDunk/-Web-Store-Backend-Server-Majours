package com.example.ecomerseapplication.Services;

import com.example.ecomerseapplication.DTOs.responses.*;
import com.example.ecomerseapplication.Entities.*;
import com.example.ecomerseapplication.Mappers.ProductDTOMapper;
import com.example.ecomerseapplication.Repositories.ProductRepository;
import com.example.ecomerseapplication.Specifications.ProductSpecifications;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    private final CustomerCartService customerCartService;

    private final ReviewService reviewService;
    private final ProductCategoryService productCategoryService;
    private final FavoriteOfCustomerService favoriteOfCustomerService;
    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    public ProductService(ProductRepository productRepository, CustomerCartService customerCartService, ReviewService reviewService, ProductCategoryService productCategoryService, FavoriteOfCustomerService favoriteOfCustomerService) {
        this.productRepository = productRepository;
        this.customerCartService = customerCartService;
        this.reviewService = reviewService;
        this.productCategoryService = productCategoryService;
        this.favoriteOfCustomerService = favoriteOfCustomerService;
    }

    public Page<CompactProductResponse> findAllByRatingResponsePage(PageRequest pageRequest) {
        return productRepository.findAllAsResponseSortByRating(pageRequest);
    }

    private String buildOrderBy(String sort) {
        return switch (sort) {
            case "price_asc" -> " ORDER BY p.sale_price_stotinki ASC ";
            case "price_desc" -> " ORDER BY p.sale_price_stotinki DESC ";
            case "newest" -> " ORDER BY p.added_at DESC ";
            case "review_count" -> " ORDER BY p.review_count DESC ";
            default -> " ORDER BY p.product_name DESC ";
        };
    }

    public Page<CompactProductResponse> getProductsLikeNameSort(PageRequest pageRequest, String search, String sortOrder) {

        String[] words = search.toLowerCase().split("\\s+");

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT p.* ")
                .append("FROM online_shop.products p ")
                .append("LEFT JOIN online_shop.manufacturers m ON p.manufacturer_id = m.manufacturer_id ")
                .append("LEFT JOIN online_shop.product_categories c ON p.product_category_id = c.product_category_id ")
                .append("WHERE ");

        //-----------------word array WHERE clause buildup--------------------
        for (int i = 0; i < words.length; i++) {
            if (i > 0) sql.append(" AND ");
            sql.append("(")
                    .append("LOWER(p.product_name) LIKE :word")
                    .append(i + 1)
                    .append(" OR LOWER(m.manufacturer_name) LIKE :word")
                    .append(i + 1)
                    .append(" OR LOWER(c.category_name) LIKE :word")
                    .append(i + 1)
                    .append(") ");
        }
        sql.append(buildOrderBy(sortOrder))
                .append("LIMIT :limit OFFSET :offset");

        Query query = entityManager.createNativeQuery(sql.toString(), Product.class);

        for (int i = 0; i < words.length; i++) {
            query.setParameter("word" + (i + 1), "%" + words[i].toLowerCase() + "%");
        }
        query.setParameter("limit", pageRequest.getPageSize());
        query.setParameter("offset", pageRequest.getOffset());


        // Word-level WHERE conditions
        return executePagedSearchQuery(pageRequest, search, words, query
        );
    }

    private @NonNull Page<CompactProductResponse> executePagedSearchQuery(PageRequest pageRequest, String search, String[] words, Query query
//                                                                          StringBuilder sql
    ) {
//        for (int i = 0; i < words.length; i++) {
//            if (i > 0) sql.append(" OR ");
//            sql.append("LOWER(p.product_name) LIKE :word")
//                    .append(i + 1)
//                    .append(" AND LOWER(m.manufacturer_name) LIKE :word")
//                    .append(i + 1)
//                    .append(" AND LOWER(c.category_name) LIKE :word")
//                    .append(i + 1);
//        }

//        sql.append(" ORDER BY relevance_score DESC ")
//        sql.append("LIMIT :limit OFFSET :offset");
//
//        Query query = entityManager.createNativeQuery(sql.toString(), Product.class);
//
//        for (int i = 0; i < words.length; i++) {
//            query.setParameter("word" + (i + 1), "%" + words[i].toLowerCase() + "%");
//        }
//        query.setParameter("limit", pageRequest.getPageSize());
//        query.setParameter("offset", pageRequest.getOffset());
//        query.setParameter("search", "%" + search.toLowerCase() + "%");

        @SuppressWarnings("unchecked")
        List<Product> products = query.getResultList();

        // --- Count query for total ---
        StringBuilder countSql = new StringBuilder();
        countSql.append("SELECT COUNT(*) FROM online_shop.products p ")
                .append("LEFT JOIN online_shop.manufacturers m ON p.manufacturer_id = m.manufacturer_id ")
                .append("LEFT JOIN online_shop.product_categories c ON p.product_category_id = c.product_category_id ")
                .append("WHERE ");


        for (int i = 0; i < words.length; i++) {
            if (i > 0) countSql.append(" AND ");
            countSql.append("(")
                    .append("LOWER(p.product_name) LIKE :word")
                    .append(i + 1)
                    .append(" AND LOWER(m.manufacturer_name) LIKE :word")
                    .append(i + 1)
                    .append(" AND LOWER(c.category_name) LIKE :word")
                    .append(i + 1)
                    .append(") ");
        }

        Query countQuery = entityManager.createNativeQuery(countSql.toString());
        for (int i = 0; i < words.length; i++) {
            countQuery.setParameter("word" + (i + 1), "%" + words[i].toLowerCase() + "%");
        }

        long total = ((Number) countQuery.getSingleResult()).longValue();

        return ProductDTOMapper.productPageToDtoPage(new PageImpl<>(products, pageRequest, total));
    }

//    public void testRelevance(String search) {
//        List<Product> products = productRepository.findAll();
//
//        String[] words = search.toLowerCase().split("\\s+");
//
//        for (Product p : products) {
//
//            String productName = p.getProductName() == null ? "" : p.getProductName().toLowerCase();
//            String manufacturer = p.getManufacturer() == null ? "" : p.getManufacturer().getManufacturerName().toLowerCase();
//            String category = p.getProductCategory() == null ? "" : p.getProductCategory().getCategoryName().toLowerCase();
//
//            int relevance = 0;
//
//            System.out.println("--------------------------------------------------");
//            System.out.println("Product: " + p.getProductName());
//            System.out.println("Manufacturer: " + manufacturer);
//            System.out.println("Category: " + category);
//            System.out.println("Search input: " + search);
//
//            // exact product name
//            if (productName.equals(search.toLowerCase())) {
//                relevance += 100;
//                System.out.println("+100 exact product name match");
//            }
//
//            // full search contained
//            if (productName.contains(search.toLowerCase())) {
//                relevance += 50;
//                System.out.println("+50 product name contains full search");
//            }
//
//            if (manufacturer.contains(search.toLowerCase())) {
//                relevance += 20;
//                System.out.println("+20 manufacturer contains full search");
//            }
//
//            if (category.contains(search.toLowerCase())) {
//                relevance += 10;
//                System.out.println("+10 category contains full search");
//            }
//
//            // word level scoring
//            for (String word : words) {
//
//                if (productName.contains(word)) {
//                    relevance += 10;
//                    System.out.println("+10 product name contains word: " + word);
//                }
//
//                if (manufacturer.contains(word)) {
//                    relevance += 10;
//                    System.out.println("+10 manufacturer contains word: " + word);
//                }
//
//                if (category.contains(word)) {
//                    relevance += 10;
//                    System.out.println("+10 category contains word: " + word);
//                }
//            }
//
//            System.out.println("FINAL RELEVANCE SCORE: " + relevance);
//        }
//    }

    public Page<CompactProductResponse> getProductsByRelevance(PageRequest pageRequest, String search) {

        String[] words = search.toLowerCase().split("\\s+");

        StringBuilder sql = new StringBuilder();
        // ----------relevance score buildup---------------------
        sql.append("SELECT p.*, (")
                .append("CASE WHEN LOWER(p.product_name) = :search THEN 100 ELSE 0 END + ")
                .append("CASE WHEN LOWER(p.product_name) LIKE :search THEN 50 ELSE 0 END + ")
                .append("CASE WHEN LOWER(m.manufacturer_name) LIKE :search THEN 20 ELSE 0 END + ")
                .append("CASE WHEN LOWER(c.category_name) LIKE :search THEN 10 ELSE 0 END ");

        for (int i = 0; i < words.length; i++) {
            sql.append(" + CASE WHEN LOWER(p.product_name) LIKE :word")
                    .append(i + 1)
                    .append(" THEN 10 ELSE 0 END ")
                    .append(" + CASE WHEN LOWER(m.manufacturer_name) LIKE :word")
                    .append(i + 1)
                    .append(" THEN 10 ELSE 0 END ")
                    .append(" + CASE WHEN LOWER(c.category_name) LIKE :word")
                    .append(i + 1)
                    .append(" THEN 10 ELSE 0 END ");
        }

        sql.append(") AS relevance_score ")
                .append("FROM online_shop.products p ")
                .append("LEFT JOIN online_shop.manufacturers m ON p.manufacturer_id = m.manufacturer_id ")
                .append("LEFT JOIN online_shop.product_categories c ON p.product_category_id = c.product_category_id ")
                .append("WHERE ");

        //-----------------word array WHERE clause buildup--------------------
        for (int i = 0; i < words.length; i++) {
            if (i > 0) sql.append(" AND ");
            sql.append("(")
                    .append("LOWER(p.product_name) LIKE :word")
                    .append(i + 1)
                    .append(" OR LOWER(m.manufacturer_name) LIKE :word")
                    .append(i + 1)
                    .append(" OR LOWER(c.category_name) LIKE :word")
                    .append(i + 1)
                    .append(") ");
        }
        sql.append(" ORDER BY relevance_score DESC ")
                .append("LIMIT :limit OFFSET :offset");

        Query query = entityManager.createNativeQuery(sql.toString(), Product.class);

        for (int i = 0; i < words.length; i++) {
            query.setParameter("word" + (i + 1), "%" + words[i].toLowerCase() + "%");
        }
        query.setParameter("limit", pageRequest.getPageSize());
        query.setParameter("offset", pageRequest.getOffset());
        query.setParameter("search", "%" + search.toLowerCase() + "%");

        return executePagedSearchQuery(pageRequest, search, words, query
        );
    }

    public List<String> getNameSuggestions(String name) {
        return productRepository.getNameSuggestions(name);
    }

    public ResponseEntity<DetailedProductResponse> getByNameAndCode(String productCode, Customer customer) {
        Product product = productRepository.getByProductCode(productCode).orElseThrow(() -> new ResourceNotFoundException("Product not found with code: " + productCode));

        List<AttributeName> attributeNames = new ArrayList<>();

        for (CategoryAttribute categoryAttribute : product.getCategoryAttributeSet()) {
            attributeNames.add(categoryAttribute.getAttributeName());
        }

        List<String[]> attributeNameMUnitPairs = productCategoryService
                .getSpecificAttributesOfCategory(product.getProductCategory().getId(), attributeNames);

        DetailedProductResponse detailedProductResponse = ProductDTOMapper.entityToDetailedResponse(product, attributeNameMUnitPairs, customer.getId());

        if (customerCartService.cartExists(customer, product))
            detailedProductResponse.inCart = true;

        detailedProductResponse.inFavourites = favoriteOfCustomerService.isInFavorites(customer, product);

        if (reviewService.exists(product, customer))
            detailedProductResponse.reviewed = true;

        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(detailedProductResponse);
    }

    public Page<CompactProductResponse> getByManufacturer(Manufacturer manufacturer, Pageable pageable) {

        Page<Product> productPage = productRepository.getByManufacturer(manufacturer, pageable);

        return ProductDTOMapper.productPageToDtoPage(productPage);
    }


//    public List<CompactProductResponse> getFeaturedProducts() {
//        return productRepository.getProductsByRating()
//                .stream()
//                .map(ProductDTOMapper::entityToCompactResponse)
//                .collect(Collectors.toList());
//    }

    public Page<CompactProductResponse> getByCategory(ProductCategory productCategory, Pageable pageable) {
        return ProductDTOMapper
                .productPageToDtoPage(productRepository
                        .getByProductCategory(productCategory, pageable));
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
                        .and(ProductSpecifications.ratingEqualOrHigher(rating));

        if (priceLowest != 0 && priceHighest != 0) {
            productSpec = productSpec.and(ProductSpecifications.priceBetween(priceLowest, priceHighest));
        }

        if (!manufacturers.isEmpty()) {
            productSpec = productSpec.and(ProductSpecifications.manufacturerIn(manufacturers));
        }

        if (!categoryAttributes.isEmpty()) {
            productSpec = productSpec.and(ProductSpecifications.containsAttributes(categoryAttributes));
        }

        Page<Product> products = productRepository.findAll(productSpec, pageable);

        return ProductDTOMapper.productPageToDtoPage(products);
    }

    public Product findByPCode(String code) {
        return productRepository.getByProductCode(code).orElseThrow(() -> new ResourceNotFoundException("Product not found with code: " + code));
    }

    public void save(Product product) {
        productRepository.save(product);
    }

//    public void saveAll(List<Product> updatedQuantProducts) {
//        productRepository.saveAll(updatedQuantProducts);
//    }

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
        Object[] result = (Object[]) productRepository.getTotalPriceRange(category);
        if (result.length != 2) {
            throw new ResourceNotFoundException("Incorrect or missing price range for category " + category.getCategoryName());
        }
        return result;
    }

    public List<Product> getByCodes(List<String> codes) {
        List<Product> products = productRepository.getAllByProductCodeIn(codes);

        if (products.isEmpty()) {
            throw new ResourceNotFoundException("No products found with codes: " + codes);
        }

        return products;
    }
}
