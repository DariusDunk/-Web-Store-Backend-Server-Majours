package com.example.ecomerseapplication.Services;

import com.example.ecomerseapplication.DTOs.requests.ProductForSaleUpdateRequest;
import com.example.ecomerseapplication.DTOs.requests.SaleUpdateRequest;
import com.example.ecomerseapplication.DTOs.responses.DetailedSalePageResponse;
import com.example.ecomerseapplication.DTOs.responses.DetailedSaleResponse;
import com.example.ecomerseapplication.DTOs.responses.PageResponse;
import com.example.ecomerseapplication.DTOs.responses.ProductOfSaleResponse;
import com.example.ecomerseapplication.DTOs.serverDtos.projectionInterfaces.VeryCompactSaleProjection;
import com.example.ecomerseapplication.DTOs.serverDtos.projectionInterfaces.DetailedSalePageProjection;
import com.example.ecomerseapplication.Entities.Product;
import com.example.ecomerseapplication.Entities.Sale;
import com.example.ecomerseapplication.Entities.SaleProduct;
import com.example.ecomerseapplication.ExceptionHandling.CustomExceptions.ProductAlreadyInSaleException;
import com.example.ecomerseapplication.Mappers.SaleMapper;
import com.example.ecomerseapplication.Others.PageContentLimit;
import com.example.ecomerseapplication.Repositories.SaleRepository;
import jakarta.persistence.EntityManager;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SaleService {

    private final SaleRepository saleRepository;
    private final ProductService productService;
    private final SaleProductService saleProductService;
    private final EntityManager entityManager;

    @Autowired
    public SaleService(SaleRepository saleRepository, ProductService productService, SaleProductService saleProductService, EntityManager entityManager) {
        this.saleRepository = saleRepository;
        this.productService = productService;
        this.saleProductService = saleProductService;
        this.entityManager = entityManager;
    }

    public List<Sale> getExpiredSales() {
        return saleRepository.getExpiredSales();
    }

    public void markAsInActive(List<Sale> expiredSales) {
        saleRepository.markAsInactive(expiredSales);
    }

    public List<VeryCompactSaleProjection> getTopCurrentSalesCompact() {
        return saleRepository.findActiveAndNotExpired(PageRequest.of(0, 2));
    }

    public Sale getById(long id) {
        return saleRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Sale not found"));
    }


    public PageResponse<DetailedSalePageResponse> getAllSales(int page) {
        Page<DetailedSalePageProjection> saleProjectionsPage = saleRepository.getAllSalesProjection(PageRequest.of(page, PageContentLimit.limit));

        List<DetailedSalePageProjection> saleProjections = saleProjectionsPage.getContent();

        List<DetailedSalePageResponse> saleResponses = SaleMapper.saleDetailProjListToResponseList(saleProjections);

        Page<DetailedSalePageResponse> salePage = new PageImpl<>(saleResponses, saleProjectionsPage.getPageable(), saleProjectionsPage.getTotalElements());

        return PageResponse.from(salePage);
    }


    public DetailedSaleResponse getDetailedSaleById(long id) {
        DetailedSalePageProjection saleProjection = saleRepository.getSaleProjection(id);

        List<ProductOfSaleResponse> productsOfSale = productService.getAllProductsOfSale(id);

        return new DetailedSaleResponse(saleProjection.getName(),
                saleProjection.getDefaultDiscount(),
                saleProjection.getStartDate(),
                saleProjection.getEndDate(),
                saleProjection.getIsActive(),
                productsOfSale);
    }

    @Transactional
    public void updateSale(@Valid SaleUpdateRequest request) {
        Sale sale = getById(request.id());

        List<ProductForSaleUpdateRequest> productDtos = request.products();
        List<SaleProduct> oldSaleProducts = saleProductService.getAllBySaleId(sale.getId());
//        Map<String, SaleProduct> oldSaleProductsMap = oldSaleProducts
//                .stream()
//                .collect(Collectors.toMap(sp -> sp.getProduct().getProductCode(), sp -> sp));

        Map<String, ProductForSaleUpdateRequest> productDtossMap = productDtos
                .stream()
                .collect(Collectors.toMap(ProductForSaleUpdateRequest::productCode,
                        p -> p,
                        (oldValue, newValue) -> newValue,
                        HashMap::new));

        List<String> productCodes = productDtos.stream().map(ProductForSaleUpdateRequest::productCode).toList();
        List<Product> products = productService.findByCodesWithSale(productCodes);

        sale.updateSale(request.name(),
                request.defaultDiscount(),
                request.startDate(),
                request.endDate(),
                request.isActive());

        List<SaleProduct> saleProductsForInsert = new ArrayList<>();

        for (Product product : products) {
            SaleProduct oldSaleProduct = product.getMainSaleProduct().orElse(null);
            if (oldSaleProduct != null) {
                System.out.println("Old sale product: " + oldSaleProduct.getSale().getId() + " New sale: " + sale.getId() );

                System.out.println("Old sale product isMain: " + oldSaleProduct.getIsMain() + " \nIs new sale old sale: " + (Objects.equals(oldSaleProduct.getSale().getId(), sale.getId())));

                if ((!oldSaleProduct.getSale().isExpired() && oldSaleProduct.getIsMain())
                && (!Objects.equals(oldSaleProduct.getSale().getId(), sale.getId())) ) {
                    throw new ProductAlreadyInSaleException("Product already in sale", product.getProductName(), sale.getName());
                } else {

                    if(!Objects.equals(oldSaleProduct.getSale().getId(), sale.getId())) {
                        oldSaleProduct.setIsMain(false);

                        SaleProduct saleProduct = new SaleProduct();

                        saleProduct.setSale(sale);
                        saleProduct.setProduct(product);
                        saleProduct.setIsMain(true);
                        saleProduct.setOverrideDiscountPercentage(productDtossMap.get(product.getProductCode()).explicitDiscount());

                        saleProductsForInsert.add(saleProduct);
                    }
                    else {
                        oldSaleProduct.setOverrideDiscountPercentage(productDtossMap.get(product.getProductCode()).explicitDiscount());
                    }
                }

            } else {
                SaleProduct saleProduct = new SaleProduct();

                saleProduct.setSale(sale);
                saleProduct.setProduct(product);
                saleProduct.setIsMain(true);
                saleProduct.setOverrideDiscountPercentage(productDtossMap.get(product.getProductCode()).explicitDiscount());

                saleProductsForInsert.add(saleProduct);

            }

        }
        List<SaleProduct> saleProductsToRemove = oldSaleProducts
                .stream()
                .filter(sp -> !productDtossMap.containsKey(sp.getProduct().getProductCode())).toList();

        saleProductService.deleteAll(saleProductsToRemove);

        entityManager.flush();

        saleProductService.saveAll(saleProductsForInsert);

    }
}

