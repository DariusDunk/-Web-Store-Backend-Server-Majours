package com.example.ecomerseapplication.Services;

import com.example.ecomerseapplication.DTOs.responses.CompactProductPagedListResponse;
import com.example.ecomerseapplication.DTOs.responses.DetailedProductResponse;
import com.example.ecomerseapplication.Entities.*;
import com.example.ecomerseapplication.Mappers.ProductDTOMapper;
import com.example.ecomerseapplication.DTOs.responses.CompactProductResponse;
import com.example.ecomerseapplication.Repositories.ProductRepository;
import com.example.ecomerseapplication.Specifications.ProductSpecifications;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    ProductRepository productRepository;

    @Autowired
    CustomerCartService customerCartService;

    @Autowired
    ReviewService reviewService;


    public Page<CompactProductResponse> findAllProductsPage(PageRequest pageRequest) {
        return ProductDTOMapper.productPageToDtoPage(productRepository.findAllSortByRating(pageRequest));
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

        DetailedProductResponse detailedProductResponse = ProductDTOMapper.entityToDetailedResponse(product);

        if (customerCartService.cartExists(customer, product))
            detailedProductResponse.inCart = true;

        if (product.getFavouredBy().contains(customer))
            detailedProductResponse.inFavourites = true;

        if (reviewService.exists(product,customer))
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

    public CompactProductPagedListResponse getByCategoryFiltersManufacturerAndPriceRange(Set<CategoryAttribute> categoryAttributes,
                                                                                         ProductCategory productCategory,
                                                                                         int priceLowest,
                                                                                         int priceHighest,
                                                                                         Manufacturer manufacturer,
                                                                                         Pageable pageable) {

        Specification<Product> productSpec = Specification.where(
                ProductSpecifications.equalsCategory(productCategory)
                        .and(ProductSpecifications.priceBetween(priceLowest, priceHighest)));

        if (manufacturer != null)
            productSpec = productSpec.and(ProductSpecifications.equalsManufacturer(manufacturer));

        List<Product> products = productRepository.findAll(productSpec);

        List<Product> filteredProductsList = new ArrayList<>();

        if (products.isEmpty())
            new PageImpl<>(
                    new ArrayList<>(),
                    pageable,
                    0);

        if (categoryAttributes != null) {
            if (!categoryAttributes.isEmpty())
                for (Product product : products)
                    if (product.getCategoryAttributeSet().containsAll(categoryAttributes))
                        filteredProductsList.add(product);
        } else
            filteredProductsList.addAll(products);
        return ProductDTOMapper.pagedListToDtoResponse(filteredProductsList, pageable);
    }

    public Product findByPCode(String code) {
        return productRepository.getByProductCode(code).orElse(null);
    }

    public void save(Product product) {
        productRepository.save(product);
    }
}
