package com.example.ecomerseapplication;
import com.example.ecomerseapplication.DTOs.AttributeOptionDTO;
import com.example.ecomerseapplication.Entities.*;
import com.example.ecomerseapplication.Others.PurchaseCodeGenerator;
import com.example.ecomerseapplication.Repositories.*;
import com.example.ecomerseapplication.Services.CategoryAttributeService;
import com.example.ecomerseapplication.Services.CustomerCartService;
import com.example.ecomerseapplication.Services.CustomerService;
import com.example.ecomerseapplication.Services.ProductCategoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.mindrot.jbcrypt.BCrypt;

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

        System.out.println(categoryAttributeList.get(0).getAttributeOption());
    }

    @Test
    void encryptTest() {

        char[] pass = {'l','u','d','o','t','Z','a','v','e','t'};

        String pwHash = BCrypt.hashpw(Arrays.toString(pass),BCrypt.gensalt(10));

        String candidate = Arrays.toString(pass);

        System.out.println(BCrypt.checkpw(candidate, pwHash));
    }

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

    @Test
    void passwordCheck() {
        Customer customer = customerService.getByEmail("konstantin_aleksandrov@abv.bg");

        String password = "fitnesmaniak200klek";

        String hashedPw = String.valueOf(customer.getPassword());
        System.out.println(hashedPw);

        System.out.println(BCrypt.checkpw(password,hashedPw));
    }

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

    @Test
    void isReviewerVerifiedCheck() {
        System.out.println("Result: " + purchaseCartRepository.isProductPurchased("20621301",6L));
    }
}
