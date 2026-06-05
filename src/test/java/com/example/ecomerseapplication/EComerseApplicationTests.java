package com.example.ecomerseapplication;
import com.example.ecomerseapplication.DTOs.requests.ProductAttributeUpdateRequest;
import com.example.ecomerseapplication.DTOs.responses.*;
import com.example.ecomerseapplication.DTOs.serverDtos.AttributeOptionDTO;
import com.example.ecomerseapplication.Entities.*;
import com.example.ecomerseapplication.Others.PageContentLimit;
import com.example.ecomerseapplication.Repositories.*;
import com.example.ecomerseapplication.Services.*;
import com.example.ecomerseapplication.Services.Admin.AdminAttributeService;
import com.example.ecomerseapplication.Services.Admin.AdminProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.*;

@SpringBootTest
@ActiveProfiles("test")
class EComerseApplicationTests {

    @Autowired
    private CategoryAttributeRepository categoryAttributeRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private AttributeNameRepository attributeNameRepository;
    @Autowired
    private ReviewRepository reviewRepository;
    @Autowired
    private CartProductRepository cartProductRepository;
    @Autowired
    private PurchaseCartRepository purchaseCartRepository;
    @Autowired
    private ReviewService reviewService;
    @Autowired
    private ProductService productService;
    @Autowired
    private FavoriteOfCustomerService favoriteOfCustomerService;
    @Autowired
    private SessionService sessionService;
    @Autowired
    private AdminAttributeService adminAttributeService;
    @Autowired
    private AdminProductService adminProductService;

    @Test
    void contextLoads() {
    }

    @Autowired
    CategoryService categoryService;

    @Autowired
    CategoryAttributeService categoryAttributeService;

    @Autowired
    CartProductService cartProductService;

    @Autowired
    CustomerService customerService;


    @Test
    void getAttributeByNameAndOption(){

        Map<String, List<String> > attMap = new HashMap<>();
        attMap.put("Широчина на фрезоване", List.of( "1000-1200", "1200-1400"));

//        Set<CategoryAttribute> sets = categoryAttributeRepository.findByNamesAndOptions(names, options);
        Set<CategoryAttribute> sets = categoryAttributeService.getByNamesAndOptions(attMap);

        System.out.println(sets.size());
        System.out.println(sets);
    }

    @Test
    void getAttributesOfCategory(){
        List<AttributeOptionDTO> attributes = categoryRepository.getAttributesOfCategory(5);

        for ( AttributeOptionDTO attributeOptionDTO : attributes) {
            System.out.println(attributeOptionDTO);
        }
    }


    @Test
    void getReviewOverviewByProductCode() {
        System.out.println(reviewRepository.getRatingOverviewByProductCode("20621303"));
    }

    @Test
    void checkTimeZones() {
        System.out.println("Timezone: "+TimeZone.getDefault());
    }

    @Test
    void getFavouritesTest() {
        Customer customer = customerService.getById("a5668417-ddc8-4029-9fcb-4f61512d044f");
        PageResponse<CompactProductResponse> fetchResponse = favoriteOfCustomerService.getFromCustomerPaged(customer, PageRequest.of(0, PageContentLimit.limit));

        System.out.println("Fetch result: " +fetchResponse.content());
    }




    @Test
    void testCartSummary() {
        Customer customer = customerService.getById("a5668417-ddc8-4029-9fcb-4f61512d044f");

       CartSummaryResponse cartSummaryResponse = cartProductService.getSummary(customer);

       System.out.println("Cart summary response: "+cartSummaryResponse);
    }

    @Test
    void testAttributesOfProductQuery() {
        int productId = 3;

        AttributesOfProductAndCategory attributesOfProductAndCategory = adminAttributeService.getAttributesOfProductAndCategory(productId);

        System.out.println("Product attributes:\n");
        System.out.println("Product name: " + attributesOfProductAndCategory.productName());
        for (AttributeOfProductResponse attribute : attributesOfProductAndCategory.productAttributes()) {
            System.out.println(" \n----------------------------------\n" +
                    "attribute name: " + attribute.attributeName()
                    + "\n attribute name id: " + attribute.attributeNameId()
            + "\nmeasurement unit: " + attribute.measurementUnit()
            + "\nvalue: " + attribute.attributeValue()
            + "\nis in category: "+ attribute.isInCategory()
            + " \n" +
                    "----------------------------------\n");
        }

        System.out.println("Category attributes:");
        for (CompactAttributeResponse attribute : attributesOfProductAndCategory.categoryAttributes()) {
            System.out.println(" \n----------------------------------\n" +
                    "attribute name: " + attribute.attributeName()
                    + "\n attribute name id: " + attribute.attributeNameId()
                    + " \n" +
                    "----------------------------------\n");
        }
    }




    //    @Test
//    void testActiveSessionFetch() {
//        Session session = sessionService.getById("L7kDsKjS7jf6k9PBBTnt3vnCx7paPW21p53kL-axDoY");
//
//        if (session != null) {
//            System.out.println("Session is active");
//        }
//    }

//    @Test
//    void testProductAttributeUpdateWithDuplicates() {
//        int productId = 3;
//
//        AttributesOfProductAndCategory attributesOfProductAndCategory = adminAttributeService.getAttributesOfProductAndCategory(productId);
//        List<AttributeOfProductResponse> productAttributes = attributesOfProductAndCategory.productAttributes();
//        AttributeOfProductResponse last = productAttributes.getLast();
//        AttributeOfProductResponse lastUpdated = new AttributeOfProductResponse(last.attributeNameId(),
//                last.attributeName(),
//                "sadasdsa",
//                last.measurementUnit(),
//                last.isInCategory());
//        AttributeOfProductResponse first = productAttributes.getFirst();
//        AttributeOfProductResponse firstUpdated = new AttributeOfProductResponse(
//                first.attributeNameId(),
//                first.attributeName(),
//                "asdsadsdaswqedqw",
//                first.measurementUnit(),
//                first.isInCategory()
//        );
//
//        productAttributes.add(firstUpdated);
//        productAttributes.add(lastUpdated);
//        List<ProductAttributeUpdateRequest> updateRequestList = new ArrayList<>();
//
//        for (AttributeOfProductResponse attribute : productAttributes) {
//            ProductAttributeUpdateRequest attributeUpdateRequest = new ProductAttributeUpdateRequest(attribute.attributeNameId(), attribute.attributeValue());
//            updateRequestList.add(attributeUpdateRequest);
//        }
//
//        adminProductService.updateProductAttributes(productId, updateRequestList);
//
//    }

}
