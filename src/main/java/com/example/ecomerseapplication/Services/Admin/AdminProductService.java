package com.example.ecomerseapplication.Services.Admin;

import com.example.ecomerseapplication.DTOs.requests.ProductFormRequest;
import com.example.ecomerseapplication.DTOs.responses.AdminProductResponse;
import com.example.ecomerseapplication.DTOs.responses.PageResponse;
import com.example.ecomerseapplication.DTOs.responses.SaleProductSuggestionResponse;
import com.example.ecomerseapplication.DTOs.serverDtos.projectionInterfaces.CompactProductProjection;
import com.example.ecomerseapplication.DTOs.serverDtos.projectionInterfaces.DetailedProductProjection;
import com.example.ecomerseapplication.Entities.Manufacturer;
import com.example.ecomerseapplication.Entities.Product;
import com.example.ecomerseapplication.Entities.ProductCategory;
import com.example.ecomerseapplication.Mappers.ProductDTOMapper;
import com.example.ecomerseapplication.Others.PageContentLimit;
import com.example.ecomerseapplication.Repositories.ProductRepository;
import com.example.ecomerseapplication.Services.CategoryService;
import com.example.ecomerseapplication.Services.ManufacturerService;
import com.example.ecomerseapplication.Services.ProductService;
import com.example.ecomerseapplication.Utils.SortHelper;
import com.example.ecomerseapplication.enums.ProductSortType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class AdminProductService {
    private final ProductService productService;
    private final ProductRepository productRepository;
    private final ManufacturerService manufacturerService;
    private final CategoryService categoryService;

    public AdminProductService(ProductService productService, ProductRepository productRepository, ManufacturerService manufacturerService, CategoryService categoryService) {
        this.productService = productService;
        this.productRepository = productRepository;
        this.manufacturerService = manufacturerService;
        this.categoryService = categoryService;
    }


    @Transactional
    public void updateProduct(ProductFormRequest request, int id) {
        Product product = productService.getById(id);
        ProductCategory category = categoryService.getById(request.categoryId());
        Manufacturer manufacturer = manufacturerService.getById(request.manufacturerId());

        product.updateProduct(request.productName(),
                request.originalPriceStotinki(),
                request.description(),
                category,
                request.productCode(),
                request.stockQuantity(),
                manufacturer,
                request.model());

    }

    @Transactional
    public void createProduct(ProductFormRequest request) {
        ProductCategory category = categoryService.getById(request.categoryId());
        Manufacturer manufacturer = manufacturerService.getById(request.manufacturerId());
        Product product = new Product();
        product.updateProduct(request.productName(),
                request.originalPriceStotinki(),
                request.description(),
                category,
                request.productCode(),
                request.stockQuantity(),
                manufacturer,
                request.model());

        product.setProductCategory(category);
        product.setManufacturer(manufacturer);

        productRepository.save(product);
    }

    public AdminProductResponse getByIdForAdminResponse(int id) {

        DetailedProductProjection projection = productRepository
                .getByIdDetProjection(id).orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        return ProductDTOMapper.detailedProjectionToAdminResponse(projection);
    }

    public PageResponse<AdminProductResponse> getAllProductsPaged(String page) {

        Sort sort = SortHelper.buildProdSort(ProductSortType.PRODUCT_CODE.getValue());

        Page<DetailedProductProjection> productProjections = productRepository.getAllDetailedProductsPaged(
                PageRequest.of(Integer.parseInt(page), PageContentLimit.limit, sort)
        );

        return ProductDTOMapper.adminProductProjPageToResponsePage(productProjections);
    }

    public List<SaleProductSuggestionResponse> getSuggestionsForSale(String keyword) {
        List<CompactProductProjection> projections = productRepository.getNameSuggestionsForSaleForm(keyword);

        List<SaleProductSuggestionResponse> responseList = new ArrayList<>();

        for (CompactProductProjection projection : projections) {
            responseList.add(new SaleProductSuggestionResponse(projection.getName(), projection.getProductCode()));
        }

        return responseList;
    }

}
