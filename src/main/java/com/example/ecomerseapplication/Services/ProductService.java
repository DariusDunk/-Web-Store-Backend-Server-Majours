package com.example.ecomerseapplication.Services;

import com.example.ecomerseapplication.DTOs.requests.ProductCodeQuantityPairRequest;
import com.example.ecomerseapplication.DTOs.responses.*;
import com.example.ecomerseapplication.DTOs.serverDtos.CompactProductDto;
import com.example.ecomerseapplication.DTOs.serverDtos.projectionInterfaces.*;
import com.example.ecomerseapplication.Entities.*;
import com.example.ecomerseapplication.ExceptionHandling.CustomExceptions.NoCategoryAndManufacturerPresentException;
import com.example.ecomerseapplication.ExceptionHandling.CustomExceptions.StockForNamedProductExceeded;
import com.example.ecomerseapplication.Mappers.ProductDTOMapper;
import com.example.ecomerseapplication.MetaModels.Product_;
import com.example.ecomerseapplication.Others.PageContentLimit;
import com.example.ecomerseapplication.Repositories.ProductRepository;
import com.example.ecomerseapplication.Specifications.PriceExpressions;
import com.example.ecomerseapplication.Specifications.ProductSpecifications;
import com.example.ecomerseapplication.Utils.SortHelper;
import com.example.ecomerseapplication.enums.ProductSortType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.criteria.*;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CartProductService cartProductService;
    private final ReviewService reviewService;
    private final FavoriteOfCustomerService favoriteOfCustomerService;
    private final SessionService sessionService;
    private final CategoryAttributeService categoryAttributeService;
    private final ProductImageService productImageService;
    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    public ProductService(ProductRepository productRepository, CartProductService cartProductService, ReviewService reviewService, FavoriteOfCustomerService favoriteOfCustomerService, SessionService sessionService, CategoryAttributeService categoryAttributeService, ProductImageService productImageService) {
        this.productRepository = productRepository;
        this.cartProductService = cartProductService;
        this.reviewService = reviewService;
        this.favoriteOfCustomerService = favoriteOfCustomerService;
        this.sessionService = sessionService;
        this.categoryAttributeService = categoryAttributeService;
        this.productImageService = productImageService;
    }

    public Page<CompactProductResponse> findAllByRatingResponsePage(PageRequest pageRequest) {

        Page<CompactProductDto> products = productRepository.findAllAsResponseSortByRating(pageRequest);

        return ProductDTOMapper.compactProductPageToCompactResponsePage(products);
    }

    private String buildNativeOrderBy(String sort) {
        return switch (sort) {
            case "price_asc" -> """
            ORDER BY COALESCE(
                p.original_price_stotinki * (1 - sp.override_discount_percentage / 100.0),
                p.original_price_stotinki * (1 - s.discount_percent / 100.0),
                p.original_price_stotinki
            ) ASC, p.product_id ASC
        """;
            case "price_desc" -> """
            ORDER BY COALESCE(
                p.original_price_stotinki * (1 - sp.override_discount_percentage / 100.0),
                p.original_price_stotinki * (1 - s.discount_percent / 100.0),
                p.original_price_stotinki
            ) DESC, p.product_id ASC
        """;
            case "newest" -> " ORDER BY p.added_at DESC, p.product_id ASC ";
            case "review_count" -> " ORDER BY p.review_count DESC, p.product_id ASC ";
            default -> " ORDER BY p.product_name DESC, p.product_id ASC ";
        };
    }

    public Page<CompactProductResponse> getProductsLikeNameSort(PageRequest pageRequest, String search, String sortOrder) {

        String[] words = search.toLowerCase().split("\\s+");

        String sql = "SELECT p.* " +
                "FROM online_shop.products p " +
                "LEFT JOIN online_shop.manufacturers m ON p.manufacturer_id = m.manufacturer_id " +
                "LEFT JOIN online_shop.product_categories c ON p.product_category_id = c.product_category_id " +
                nativeSaleJoin() +
                "WHERE " +

                //-----------------word array WHERE clause buildup--------------------
                buildWhereClause(words) +
                buildNativeOrderBy(sortOrder) +
                "LIMIT :limit OFFSET :offset";

        Query query = entityManager.createNativeQuery(sql, Product.class);

        for (int i = 0; i < words.length; i++) {
            query.setParameter("word" + (i + 1), "%" + words[i].toLowerCase() + "%");
        }
        query.setParameter("limit", pageRequest.getPageSize());
        query.setParameter("offset", pageRequest.getOffset());

        return executePagedSearchQuery(pageRequest, words, query
        );
    }

    private String nativeSaleJoin() {
        return "LEFT JOIN online_shop.sale_products sp ON p.product_id = sp.product_id AND sp.is_main = true " +
                "LEFT JOIN online_shop.sales s ON sp.sale_id = s.sale_id AND s.is_active = true " +
                "AND s.start_date <= NOW() " +
                "AND s.end_date > NOW() ";
    }

    private @NonNull Page<CompactProductResponse> executePagedSearchQuery(PageRequest pageRequest, String[] words, Query query
    ) {


        @SuppressWarnings("unchecked")
        List<Product> products = query.getResultList();

        // --- Count query for total ---


        String countSql = "SELECT COUNT(*) FROM online_shop.products p " +
                "LEFT JOIN online_shop.manufacturers m ON p.manufacturer_id = m.manufacturer_id " +
                "LEFT JOIN online_shop.product_categories c ON p.product_category_id = c.product_category_id " +
                "WHERE " +
                buildWhereClause(words);

        Query countQuery = entityManager.createNativeQuery(countSql);
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
        sql.append(buildWhereClause(words));
        sql.append(" ORDER BY relevance_score DESC, p.rating DESC, p.product_id ASC ")
                .append("LIMIT :limit OFFSET :offset");

        Query query = entityManager.createNativeQuery(sql.toString(), Product.class);

        for (int i = 0; i < words.length; i++) {
            query.setParameter("word" + (i + 1), "%" + words[i].toLowerCase() + "%");
        }
        query.setParameter("limit", pageRequest.getPageSize());
        query.setParameter("offset", pageRequest.getOffset());
        query.setParameter("search", "%" + search.toLowerCase() + "%");

        return executePagedSearchQuery(pageRequest, words, query
        );
    }

    private String buildWhereClause(String[] words) {
        StringBuilder where = new StringBuilder();

        for (int i = 0; i < words.length; i++) {
            if (i > 0) where.append(" AND ");
            where.append("(")
                    .append("LOWER(p.product_name) LIKE :word").append(i + 1)
                    .append(" OR LOWER(m.manufacturer_name) LIKE :word").append(i + 1)
                    .append(" OR LOWER(c.category_name) LIKE :word").append(i + 1)
                    .append(")");
        }

        return where.toString();
    }

    public List<String> getNameSuggestions(String name) {
        return productRepository.getNameSuggestions(name);
    }

    public DetailedProductResponse getByCodeForAuth(String productCode, Customer customer) {
        Product product = findByCodeWithRelations(productCode);

        List<AttributeOfProjection> attributeNames = categoryAttributeService.getAttributesOfProduct(product.getId());
        List<String> productImageNames = productImageService.getImageNamesByProductId(product.getId());

        DetailedProductResponse detailedProductResponse = ProductDTOMapper.entityToDetailedResponse(product,
                attributeNames,
                productImageNames);

        if (cartProductService.cartItemExistsByCustomer(customer, product))
            detailedProductResponse.inCart = true;

        detailedProductResponse.inFavourites = favoriteOfCustomerService.isInFavorites(customer, product);

        if (reviewService.exists(product, customer))
            detailedProductResponse.reviewed = true;

        return detailedProductResponse;
    }


    @Transactional(readOnly = true)
    public DetailedProductResponse getByCodeAndWithSession(String productCode) {
        Session session = sessionService.getRequestSession();
        try
        {
            if (session.getIsGuest()) {

                return getByCodeForGuest(productCode);
            } else {
                Customer customer = session.getCustomer();
                return getByCodeForAuth(productCode, customer);
            }
        }
        catch (Exception e)
        {
            System.out.println("-------------------------Exception in product detail endpoint-------------------------\n" + e.getMessage());
            throw e;
        }
    }


    public DetailedProductResponse getByCodeForGuest(String productCode) {
        Product product = findByCodeWithRelations(productCode);

        List<AttributeOfProjection> attributeNames = categoryAttributeService.getAttributesOfProduct(product.getId());
        List<String> productImageNames = productImageService.getImageNamesByProductId(product.getId());

        return ProductDTOMapper.entityToDetailedResponse(product, attributeNames, productImageNames);
    }

        public Page<CompactProductResponse> getByCategory(ProductCategory productCategory, int page, String sortOrder, int pageSize) {

            boolean isPriceSort = sortOrder != null
                    && !sortOrder.isBlank()
                    && (sortOrder.equals(ProductSortType.PRICE_ASC.getValue())
                    || Objects.equals(sortOrder, ProductSortType.PRICE_DESC.getValue()));

            if (isPriceSort) {
                Sort.Direction dir = sortOrder.equals(ProductSortType.PRICE_ASC.getValue())
                        ? Sort.Direction.ASC
                        : Sort.Direction.DESC;
                return getByCategorySortedByPrice(productCategory, page, dir);
            }

            Sort sort = setUpSort(sortOrder);

            PageRequest pageRequest = PageRequest.of(page, pageSize, sort);
            return ProductDTOMapper.productPageToDtoPage(
                    productRepository.getByProductCategory(productCategory, pageRequest)
            );
        }

        private Page<CompactProductResponse> getByCategorySortedByPrice(ProductCategory productCategory, int page, Sort.Direction direction) {

            int pageSize = PageContentLimit.limit;

            CriteriaBuilder cb = entityManager.getCriteriaBuilder();

            // ---  SELECT p.id + ORDER BY finalPrice ---
            CriteriaQuery<Integer> idQuery = cb.createQuery(Integer.class);
            Root<Product> idRoot = idQuery.from(Product.class);

            Expression<Number> finalPrice = PriceExpressions.finalPrice(idRoot, cb);

            idQuery.select(idRoot.get(Product_.ID))
                    .where(cb.equal(idRoot.get(Product_.PRODUCT_CATEGORY), productCategory))
                    .orderBy(
                            direction.isAscending() ? cb.asc(finalPrice) : cb.desc(finalPrice),
                            cb.asc(idRoot.get(Product_.ID))
                    );

            List<Integer> orderedIds = entityManager.createQuery(idQuery)
                    .setFirstResult(page * pageSize)
                    .setMaxResults(pageSize)
                    .getResultList();

            if (orderedIds.isEmpty()) {
                return Page.empty(PageRequest.of(page, pageSize));
            }

            // --- fetch by IDs ---
            List<Product> products = productRepository.findAllById(orderedIds);

            Map<Integer, Product> productById = products.stream()
                    .collect(Collectors.toMap(Product::getId, p -> p));
            List<Product> orderedProducts = orderedIds.stream()
                    .map(productById::get)
                    .filter(Objects::nonNull)
                    .toList();

            // --- Pagination Count query ---
            CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
            Root<Product> countRoot = countQuery.from(Product.class);
            countQuery.select(cb.count(countRoot))
                    .where(cb.equal(countRoot.get(Product_.PRODUCT_CATEGORY), productCategory));
            long total = entityManager.createQuery(countQuery).getSingleResult();

            return ProductDTOMapper.productPageToDtoPage(
                    new PageImpl<>(orderedProducts, PageRequest.of(page, pageSize), total)
            );
        }

        public Page<CompactProductResponse> getByCategoryFiltersManufacturerAndPriceRange(Set<CategoryAttribute> categoryAttributes,
                                                                                          ProductCategory productCategory,
                                                                                          int priceLowest,
                                                                                          int priceHighest,
                                                                                          List<Manufacturer> manufacturers,
                                                                                          Integer rating,
                                                                                          String sortOrder,
                                                                                          int page) {

            boolean isPriceSort = sortOrder != null
                    && !sortOrder.isBlank()
                    && (sortOrder.equals(ProductSortType.PRICE_ASC.getValue())
                    || Objects.equals(sortOrder, ProductSortType.PRICE_DESC.getValue()));

            Integer normalizedRating = rating != null ? rating * 10 : null;

            if (isPriceSort) {
                Sort.Direction dir = sortOrder.equals(ProductSortType.PRICE_ASC.getValue())
                        ? Sort.Direction.ASC
                        : Sort.Direction.DESC;
                return getByFiltersAndPriceSorted(
                        categoryAttributes, productCategory,
                        priceLowest, priceHighest,
                        manufacturers, normalizedRating,
                        dir, page);
            }

            Specification<Product> productSpec =
                    ProductSpecifications.equalsCategory(productCategory)
                            .and(ProductSpecifications.ratingEqualOrHigher(normalizedRating));

            if (priceLowest != 0 && priceHighest != 0) {

                productSpec = productSpec.and((root, query, cb) -> {
                    if (query != null) query.distinct(true);
                    Expression<Number> fp = PriceExpressions.finalPrice(root, cb);
                    return cb.and(cb.ge(fp, priceLowest), cb.le(fp, priceHighest));
                });
            }

            if (!manufacturers.isEmpty()) {
                productSpec = productSpec.and(ProductSpecifications.manufacturerIn(manufacturers));
            }

            if (!categoryAttributes.isEmpty()) {
                productSpec = productSpec.and(ProductSpecifications.containsAttributes(categoryAttributes));
            }

            Sort sort = setUpSort(sortOrder);

            PageRequest pageRequest = PageRequest.of(page, PageContentLimit.limit, sort);
            return ProductDTOMapper.productPageToDtoPage(productRepository.findAll(productSpec, pageRequest));
        }

        private Page<CompactProductResponse> getByFiltersAndPriceSorted(Set<CategoryAttribute> categoryAttributes,
                                                                         ProductCategory productCategory,
                                                                         int priceLowest,
                                                                         int priceHighest,
                                                                         List<Manufacturer> manufacturers,
                                                                         Integer rating,
                                                                         Sort.Direction direction,
                                                                         int page) {
            int pageSize = PageContentLimit.limit;
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();

            // ---  SELECT p.id + ORDER BY finalPrice ---
            CriteriaQuery<Integer> idQuery = cb.createQuery(Integer.class);
            Root<Product> root = idQuery.from(Product.class);
            Expression<Number> finalPrice = PriceExpressions.finalPrice(root, cb);
            List<Predicate> predicates = new ArrayList<>();

            // Category
            predicates.add(cb.equal(root.get(Product_.PRODUCT_CATEGORY), productCategory));

            // Rating
            if (rating != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get(Product_.RATING), rating));
            }

            // Price range
            if (priceLowest != 0 && priceHighest != 0) {
                predicates.add(cb.ge(finalPrice, priceLowest));
                predicates.add(cb.le(finalPrice, priceHighest));
            }

            // Manufacturers
            if (!manufacturers.isEmpty()) {
                predicates.add(root.get(Product_.MANUFACTURER).in(manufacturers));
            }

            // Attributes
            if (!categoryAttributes.isEmpty()) {
                Map<Integer, Set<Integer>> groups = categoryAttributes.stream()
                        .collect(Collectors.groupingBy(
                                a -> a.getAttributeName().getId(),
                                Collectors.mapping(CategoryAttribute::getId, Collectors.toSet())
                        ));
                for (var entry : groups.entrySet()) {
                    Join<Product, CategoryAttribute> attrJoin =
                            root.join(Product_.CATEGORY_ATTRIBUTE_SET, JoinType.INNER);
                    predicates.add(attrJoin.get("id").in(entry.getValue()));
                }
            }

            idQuery.select(root.get(Product_.ID))
                    .where(predicates.toArray(new Predicate[0]))
                    .orderBy(
                            direction.isAscending() ? cb.asc(finalPrice) : cb.desc(finalPrice),
                            cb.asc(root.get(Product_.ID))
                    );

            List<Integer> orderedIds = entityManager.createQuery(idQuery)
                    .setFirstResult(page * pageSize)
                    .setMaxResults(pageSize)
                    .getResultList();

            if (orderedIds.isEmpty()) {
                return Page.empty(PageRequest.of(page, pageSize));
            }

            // --- fetch by IDs ---
            List<Product> products = productRepository.findAllById(orderedIds);

            Map<Integer, Product> productById = products.stream()
                    .collect(Collectors.toMap(Product::getId, p -> p));
            List<Product> orderedProducts = orderedIds.stream()
                    .map(productById::get)
                    .filter(Objects::nonNull)
                    .toList();

            // --- Pagination Count query ---
            CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
            Root<Product> countRoot = countQuery.from(Product.class);
            Expression<Number> countFinalPrice = PriceExpressions.finalPrice(countRoot, cb);

            List<Predicate> countPredicates = new ArrayList<>();
            countPredicates.add(cb.equal(countRoot.get(Product_.PRODUCT_CATEGORY), productCategory));
            if (rating != null) {
                countPredicates.add(cb.greaterThanOrEqualTo(countRoot.get(Product_.RATING), rating));
            }
            if (priceLowest != 0 && priceHighest != 0) {
                countPredicates.add(cb.ge(countFinalPrice, priceLowest));
                countPredicates.add(cb.le(countFinalPrice, priceHighest));
            }
            if (!manufacturers.isEmpty()) {
                countPredicates.add(countRoot.get(Product_.MANUFACTURER).in(manufacturers));
            }
            if (!categoryAttributes.isEmpty()) {
                Map<Integer, Set<Integer>> groups = categoryAttributes.stream()
                        .collect(Collectors.groupingBy(
                                a -> a.getAttributeName().getId(),
                                Collectors.mapping(CategoryAttribute::getId, Collectors.toSet())
                        ));
                for (var entry : groups.entrySet()) {
                    Join<Product, CategoryAttribute> attrJoin =
                            countRoot.join(Product_.CATEGORY_ATTRIBUTE_SET, JoinType.INNER);
                    countPredicates.add(attrJoin.get("id").in(entry.getValue()));
                }
            }

            countQuery.select(cb.countDistinct(countRoot))
                    .where(countPredicates.toArray(new Predicate[0]));
            long total = entityManager.createQuery(countQuery).getSingleResult();

            return ProductDTOMapper.productPageToDtoPage(
                    new PageImpl<>(orderedProducts, PageRequest.of(page, pageSize), total)
            );
        }

    public Product findByPCode(String code) {
        return productRepository.getByProductCode(code).orElseThrow(() -> new EntityNotFoundException("Product not found with code: " + code));
    }

    public Product findByCodeWithRelations(String code) {
        return productRepository.findProductByProductCode(code).orElseThrow(() -> new EntityNotFoundException("Product not found with code: " + code));
    }

    public List<Product> findByCodesWithSale(List<String> codes) {
        return productRepository.findAllByProductCodeIn(codes);
    }

    public List<CompactProductQuantityPairResponse> findByCodesAndQuantityInspect(List<ProductCodeQuantityPairRequest> requestList) {

        List<String> codes = requestList.stream().map(ProductCodeQuantityPairRequest::productCode).toList();
        List<Product> products = findByCodesWithSale(codes);
        List<CompactProductQuantityPairResponse> productResponses = new ArrayList<>();
        Map<String, Short> codeQuantityMap = Map
                .copyOf(requestList
                        .stream()
                        .collect(HashMap::new,
                                (m, v) -> m.put(v.productCode(), v.quantity()), HashMap::putAll)
                );
        for (Product product : products) {
            if (product.getQuantityInStock() < codeQuantityMap.get(product.getProductCode())) {
                throw new StockForNamedProductExceeded("Requested purchase quantity exceeded for product " + product.getProductName(),
                        product.getProductName(),
                        product.getQuantityInStock());
            }

            CompactProductQuantityPairResponse productResponse = new CompactProductQuantityPairResponse();
            productResponse.compactProductResponse = ProductDTOMapper.entityToCompactResponse(product);
            productResponse.quantity = codeQuantityMap.get(product.getProductCode());

            productResponses.add(productResponse);
        }
        return productResponses;
    }

    private Page<CompactProductResponse> getByManufacturerSortedByPrice(Manufacturer manufacturer, int page, Sort.Direction direction) {

        int pageSize = PageContentLimit.limit;

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        // ---  SELECT p.id + ORDER BY finalPrice ---
        CriteriaQuery<Integer> idQuery = cb.createQuery(Integer.class);
        Root<Product> idRoot = idQuery.from(Product.class);
        Expression<Number> finalPrice = PriceExpressions.finalPrice(idRoot, cb);

        idQuery.select(idRoot.get(Product_.ID))
                .where(cb.equal(idRoot.get(Product_.MANUFACTURER), manufacturer))
                .orderBy(
                        direction.isAscending() ? cb.asc(finalPrice) : cb.desc(finalPrice),
                        cb.asc(idRoot.get(Product_.ID))
                );

        List<Integer> orderedIds = entityManager.createQuery(idQuery)
                .setFirstResult(page * pageSize)
                .setMaxResults(pageSize)
                .getResultList();

        if (orderedIds.isEmpty()) {
            return Page.empty(PageRequest.of(page, pageSize));
        }

        // --- fetch by IDs ---
        List<Product> products = productRepository.findAllById(orderedIds);

        // re-order
        Map<Integer, Product> productById = products.stream()
                .collect(Collectors.toMap(Product::getId, p -> p));
        List<Product> orderedProducts = orderedIds.stream()
                .map(productById::get)
                .filter(Objects::nonNull)
                .toList();

        // --- Pagination Count query ---
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Product> countRoot = countQuery.from(Product.class);
        countQuery.select(cb.count(countRoot))
                .where(cb.equal(countRoot.get(Product_.MANUFACTURER), manufacturer));
        long total = entityManager.createQuery(countQuery).getSingleResult();

        return ProductDTOMapper.productPageToDtoPage(
                new PageImpl<>(orderedProducts, PageRequest.of(page, pageSize), total)
        );
    }

    public Page<CompactProductResponse> getByManufacturer(Manufacturer manufacturer, int page, String sortOrder) {

        boolean isPriceSort = sortOrder != null
                && !sortOrder.isBlank()
                && (sortOrder.equals(ProductSortType.PRICE_ASC.getValue())
                || Objects.equals(sortOrder, ProductSortType.PRICE_DESC.getValue()));

        if (isPriceSort) {
            Sort.Direction dir = sortOrder.equals(ProductSortType.PRICE_ASC.getValue())
                    ? Sort.Direction.ASC
                    : Sort.Direction.DESC;
            return getByManufacturerSortedByPrice(manufacturer, page, dir);
        }

        Sort sort = setUpSort(sortOrder);

        PageRequest pageRequest = PageRequest.of(page, PageContentLimit.limit, sort);
        Specification<Product> manufacturerEqualsSpec = ProductSpecifications.manufacturerEquals(manufacturer);

        Page<Product> productPage = productRepository.findAll(manufacturerEqualsSpec, pageRequest);
        return ProductDTOMapper.productPageToDtoPage(productPage);
    }

    private static Sort setUpSort(String sortOrder) {
        Sort sort = (sortOrder != null && !sortOrder.isBlank())
                ? SortHelper.buildProdSort(ProductSortType.valueOf(sortOrder.toUpperCase()).getValue())
                : SortHelper.buildProdSort(ProductSortType.POPULARITY.getValue());
        return sort.and(SortHelper.buildProdSort(ProductSortType.PRODUCT_ID.getValue()));
    }

    public void save(Product product) {
        productRepository.save(product);
    }


    public Set<Integer> getRatingsOfCategory(ProductCategory category) {

        return productRepository.getRatingsByCategory(category)
                .orElse(Collections.emptySet())
                .stream()
                .map(rating -> rating / 10)
                .filter(rating -> rating > 0)
                .collect(Collectors.toSet());
    }

    public FiltersPriceRange getTotalPriceRangeOfCategory(ProductCategory category) {

        return productRepository.getCategoryPriceRange(category);
    }

    public List<Product> getByCodes(List<String> codes) {
        List<Product> products = productRepository.getAllByProductCodeIn(codes);

        if (products.isEmpty()) {
            throw new ResourceNotFoundException("No products found with codes: " + codes);
        }

        return products;
    }

    public List<Product> getByCodesForPurchaseWithLocking(List<String> codes) {
        List<Product> products = productRepository.getByCodesForPurchaseWithLocking(codes);

        if (products.isEmpty()) {
            throw new ResourceNotFoundException("No products found with codes: " + codes);
        }

        return products;
    }

    public List<CompactProductResponse> getTopProductsOfSale(long saleId) {
        List<CompactSaleProductProjection> productProjections = productRepository
                .getTopProductsOfSale(saleId, PageRequest.of(0, 8));

       return ProductDTOMapper.compactSaleProjectionListToResponseList(productProjections);
    }

    public List<ProductOfSaleResponse> getAllProductsOfSale(long saleId) {
       return productRepository.getAllProductsOfSaleMini(saleId);
    }

    public List<CompactProductProjection> getTopProductsOfCategory(ProductCategory category) {
        return productRepository.getTopProductsOfCategory(category.getId(), PageRequest.of(0, 8));
    }

    public ImageSearchPagedResponse getByCategoriesAndManufacturers(List<ProductCategory> categories, List<Manufacturer> manufacturers, int page) {

        if (manufacturers.isEmpty() && categories.isEmpty()) {
            throw new NoCategoryAndManufacturerPresentException("No categories or manufacturers found from inference");
        }

        int pageSize = PageContentLimit.limit;

        Specification<Product> sp = ProductSpecifications.joinMainSale();

        if (!manufacturers.isEmpty()) {
            sp = sp.and(ProductSpecifications.manufacturerIn(manufacturers));
        }

        if (!categories.isEmpty()) {
            sp = sp.and(ProductSpecifications.categoryIn(categories));
        }

        Sort sort = (SortHelper.buildProdSort(ProductSortType.POPULARITY.getValue()))
                .and(SortHelper.buildProdSort(ProductSortType.PRODUCT_ID.getValue()));

        Page<Product> products = productRepository.findAll(sp,  PageRequest.of(page, pageSize, sort));

        Page<CompactProductResponse> response = ProductDTOMapper.productPageToDtoPage(products);

        return new ImageSearchPagedResponse(PageResponse.from(response),
                categories.stream().map(ProductCategory::getCategoryName).toList(),
                manufacturers.stream().map(Manufacturer::getManufacturerName).toList());

    }

    public List<Product> getByIdSetWithLock(Set<Integer> productIds) {
        return productRepository.getAllByIdIn(productIds);
    }

    public Product getById(int id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    public Product getByIdWithAttributesAndLock(int id) {
        return productRepository.getByIdWithAttributesAndLock(id).orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    public Product getByIdWithImagesAndLocking(int id) {
        return productRepository.getByIdWithImagesAndLock(id).orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    public Product getByIdWithImages(Integer productId) {
        return productRepository.getByIdWithImages(productId).orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
    }

}
