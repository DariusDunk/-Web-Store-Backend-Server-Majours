package com.example.ecomerseapplication;
import com.example.ecomerseapplication.DTOs.responses.CompactProductResponse;
import com.example.ecomerseapplication.DTOs.responses.PageResponse;
import com.example.ecomerseapplication.DTOs.serverDtos.AttributeOptionDTO;
import com.example.ecomerseapplication.Entities.*;
import com.example.ecomerseapplication.Others.PageContentLimit;
import com.example.ecomerseapplication.Others.PurchaseCodeGenerator;
import com.example.ecomerseapplication.Repositories.*;
import com.example.ecomerseapplication.Services.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
//import org.mindrot.jbcrypt.BCrypt;

import java.time.LocalDateTime;
import java.util.*;

@SpringBootTest
class EComerseApplicationTests {

    @Autowired
    private CategoryAttributeRepository categoryAttributeRepository;
    @Autowired
    private ProductCategoryRepository productCategoryRepository;
    @Autowired
    private AttributeNameRepository attributeNameRepository;
    @Autowired
    private ReviewRepository reviewRepository;
    @Autowired
    private CustomerCartRepository customerCartRepository;
    @Autowired
    private PurchaseCartRepository purchaseCartRepository;
    @Autowired
    private ReviewService reviewService;
    @Autowired
    private ProductService productService;
    @Autowired
    private FavoriteOfCustomerService favoriteOfCustomerService;

    @Test
    void contextLoads() {
    }

    @Autowired
    ProductCategoryService categoryService;

    @Autowired
    CategoryAttributeService categoryAttributeService;

    @Autowired
    CustomerCartService customerCartService;

    @Autowired
    CustomerService customerService;

    @Test
    void attributeTest() {
        Optional<ProductCategory> productCategory = categoryService.findById(4);

        List<CategoryAttribute> categoryAttributeList = categoryAttributeService.getByCategory(productCategory.orElse(null));

        System.out.println(categoryAttributeList.getFirst().getAttributeOption());
    }

//    @Test
//    void encryptTest() {
//
//        char[] pass = {'l','u','d','o','t','Z','a','v','e','t'};
//
//        String pwHash = BCrypt.hashpw(Arrays.toString(pass),BCrypt.gensalt(10));
//
//        String candidate = Arrays.toString(pass);
//
//        System.out.println(BCrypt.checkpw(candidate, pwHash));
//    }

    @Test
    void codeHashTest() {
        System.out.println(PurchaseCodeGenerator.generateCode(LocalDateTime.now()));
    }

    @Test
    void cartsByCustomer() {


        List<CustomerCart> customerCarts = customerCartService.cartsByCustomer(customerService.findById(6));

        if (customerCarts.isEmpty())
            System.out.println("Error");
        else
            System.out.println("Finished");
    }

//    @Test
//    void passwordCheck() {
//        Customer customer = customerService.getByEmail("konstantin_aleksandrov@abv.bg");
//
//        String password = "fitnesmaniak200klek";
//
//        String hashedPw = String.valueOf(customer.getPassword());
//        System.out.println(hashedPw);
//
//        System.out.println(BCrypt.checkpw(password,hashedPw));
//    }

    @Test
    void getAttributeByNameAndOption(){

//        HashSet<String> names = new HashSet<>();
//        HashSet<String> options = new HashSet<>();
//        names.add("Широчина на фрезоване");
//
//        options.add("1000-1200");

        Map<String, List<String> > attMap = new HashMap<>();
        attMap.put("Широчина на фрезоване", List.of( "1000-1200", "1200-1400"));

//        Set<CategoryAttribute> sets = categoryAttributeRepository.findByNamesAndOptions(names, options);
        Set<CategoryAttribute> sets = categoryAttributeService.getByNamesAndOptions(attMap);

        System.out.println(sets.size());
        System.out.println(sets);
    }

    @Test
    void getAttributesOfCategory(){
        List<AttributeOptionDTO> attributes = productCategoryRepository.getAttributesOfCategory(5);

        for ( AttributeOptionDTO attributeOptionDTO : attributes) {
            System.out.println(attributeOptionDTO);
        }
    }

    @Test
    void getAttributeMeasurementByNameAndCategory() {

        List<AttributeName> names = attributeNameRepository.getAllByIdIn(List.of(
                1, 2, 3, 4, 5));

       List<String > units = productCategoryRepository.getMeasurementUnitsOfCategoryAttributes(5,names );


        for ( String unit : units) {
            System.out.println(unit);
        }
    }

//    @Test
//    void isReviewerVerifiedCheck() {
//        System.out.println("Result: " + purchaseCartRepository.isProductPurchased("20621301",6L));
//    }

    @Test
    void getReviewOverviewByProductCode() {
        System.out.println(reviewRepository.getRatingOverviewByProductCode("20621303"));
    }

    @Test
    void doesReviewExistTest() {

        System.out.println("Result for product= 20621307 and userId=6 "+ reviewService.exists(productService.findByPCode("20621307"), customerService.findById(6L)));
    }

    @Test
    void checkTimeZones() {
        System.out.println("Timezone: "+TimeZone.getDefault());
    }

    @Test
    void addProductToFavouritesTest() {//TODO test
//        customerService.addProductToFavourites("6", productService.findByPCode("20621307"));
        ResponseEntity<?> response = favoriteOfCustomerService.addToFavorite(customerService.getByKID("a5668417-ddc8-4029-9fcb-4f61512d044f"), productService.findByPCode("20621307"));
        System.out.println(response);
    }

    @Test
    void getFavouritesTest() {
        Customer customer = customerService.getByKID("a5668417-ddc8-4029-9fcb-4f61512d044f");
        PageResponse<CompactProductResponse> fetchResponse = favoriteOfCustomerService.getFromFavourites(customer, PageRequest.of(0, PageContentLimit.limit));

        System.out.println("Fetch result: " +fetchResponse.content());
    }

    @Test
    void removeProductFromFavorites() {
        ResponseEntity<?> response = favoriteOfCustomerService.removeFromFavorites(customerService.getByKID("a5668417-ddc8-4029-9fcb-4f61512d044f"), productService.findByPCode("20621307"));
        System.out.println(response);
    }

    @Test
    void removeFavoritesBatch() {
        ResponseEntity<?> response = favoriteOfCustomerService.removeFavoritesBatch(customerService.getByKID("a5668417-ddc8-4029-9fcb-4f61512d044f"),List.of("20621307"));
    }

    @Test
    void testFavorites() {

        Customer customer = customerService.getByKID("a5668417-ddc8-4029-9fcb-4f61512d044f");

        ResponseEntity<?> response = favoriteOfCustomerService.addToFavorite(customer, productService.findByPCode("20621307"));
        System.out.println("Insert 1: " + response);
        ResponseEntity<?> response2 = favoriteOfCustomerService.addToFavorite(customer, productService.findByPCode("20621308"));
        System.out.println("Insert 2: " +response2);
        ResponseEntity<?> response3 = favoriteOfCustomerService.addToFavorite(customer, productService.findByPCode("20621309"));
        System.out.println("Insert 3: " +response3);

        PageResponse<CompactProductResponse> fetchResponse = favoriteOfCustomerService.getFromFavourites(customer, PageRequest.of(0, PageContentLimit.limit));

        System.out.println("Fetch after inserts: " +fetchResponse.content());

        ResponseEntity<?> deleteResponse = favoriteOfCustomerService.removeFromFavorites(customer, productService.findByPCode("20621307"));
        System.out.println("Single delete response: "+deleteResponse);

        PageResponse<CompactProductResponse> fetchResponse2 = favoriteOfCustomerService.getFromFavourites(customer, PageRequest.of(0, PageContentLimit.limit));

        System.out.println("Fetch after single delete: " +fetchResponse2.content());

        ResponseEntity<?> favoritesBatchDeleteResponse = favoriteOfCustomerService.removeFavoritesBatch(customer,List.of("20621308", "20621309"));
        System.out.println("Batch delete response: "+favoritesBatchDeleteResponse);

        PageResponse<CompactProductResponse> fetchResponse3 = favoriteOfCustomerService.getFromFavourites(customer, PageRequest.of(0, PageContentLimit.limit));

        System.out.println("Fetch after batch delete: " +fetchResponse3.content());

    }
}
