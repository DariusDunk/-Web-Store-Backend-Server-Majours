package com.example.ecomerseapplication.Services;

import com.example.ecomerseapplication.CompositeIdClasses.FavoriteOfCustomerId;
import com.example.ecomerseapplication.CustomErrorHelpers.ErrorType;
import com.example.ecomerseapplication.DTOs.responses.CompactProductResponse;
import com.example.ecomerseapplication.DTOs.responses.ErrorResponse;
import com.example.ecomerseapplication.DTOs.responses.PageResponse;
import com.example.ecomerseapplication.Entities.Customer;
import com.example.ecomerseapplication.Entities.FavoriteOfCustomer;
import com.example.ecomerseapplication.Entities.Product;
import com.example.ecomerseapplication.Others.GlobalConstants;
import com.example.ecomerseapplication.Others.PageContentLimit;
import com.example.ecomerseapplication.Repositories.FavoriteOfCustomerRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class FavoriteOfCustomerService {

    private final FavoriteOfCustomerRepository repository;

    @Autowired
    public FavoriteOfCustomerService(FavoriteOfCustomerRepository repository) {
        this.repository = repository;
    }

    public Boolean isInFavorites(Customer customer, Product product) {
        return repository.existsByFavoriteOfCustomerId_CustomerAndFavoriteOfCustomerId_Product(customer, product);
    }

    @Transactional
    public ResponseEntity<?> addToFavorite(Customer customer, Product product) {

        if (repository.countAllByFavoriteOfCustomerId_Customer_keycloakId(customer.getKeycloakId()) >= GlobalConstants.favoritesSizeLimit)
            return ResponseEntity.status(HttpStatus.MULTI_STATUS).body(new ErrorResponse(ErrorType.SIZE_LIMIT_REACHED,
                    "Достигнат лимит на любими",
                    HttpStatus.CONFLICT.value(), "Достигнахте максималният лимит на списъка с любими!"));

        if (isInFavorites(customer, product)) {
            return ResponseEntity.status(HttpStatus.MULTI_STATUS).body(new ErrorResponse(ErrorType.DUPLICATION_OF_DATA,
                    "Продуктът вече е в любими",
                    HttpStatus.CONFLICT.value(), "Избраният продукт вече е в списъка ви с любими"));
        }

        FavoriteOfCustomerId favId = new FavoriteOfCustomerId(product, customer);
        FavoriteOfCustomer favoriteEntry = new FavoriteOfCustomer(favId, Instant.now());
        try {
            repository.save(favoriteEntry);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        }
        catch (Exception e)
        {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    @Transactional
    public ResponseEntity<?> removeFromFavoritesWRefetch(Customer customer, Product product, int currentPage) {
        try {
          repository.deleteFavoriteOfCustomerByFavoriteOfCustomerId_CustomerAndFavoriteOfCustomerId_Product(customer, product);

//            System.out.println("Page Response: " + response);

            return calculateNewPageWContent(customer, currentPage);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private ResponseEntity<?> calculateNewPageWContent(Customer customer, int currentPage){

        int favoritesCount = getFavoritesCount(customer);

        if (favoritesCount == 0) {
            return ResponseEntity.ok(new PageResponse<>(new ArrayList<>(),
                    0,
                    0,
                    0L,
                    0,
                    true));
        }

        int totalPages = (int) Math.ceil((double) favoritesCount / PageContentLimit.limit);

        int safePage = Math.min(totalPages-1, currentPage);

        PageRequest pageRequest = PageRequest.of(safePage, PageContentLimit.limit);

        return ResponseEntity.ok(getFromFavourites(customer, pageRequest));

    }

    @Transactional
    public ResponseEntity<?> removeFavoritesBatch(Customer customer, List<String> productCodes, int currentPage) {

        if (productCodes.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        try {
            repository.deleteBatch(customer, productCodes);
          return calculateNewPageWContent(customer, currentPage);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    public int getFavoritesCount(Customer customer) {
        return repository.countAllByFavoriteOfCustomerId_Customer_keycloakId(customer.getKeycloakId());
    }

    public PageResponse<CompactProductResponse> getFromFavourites(Customer customer, PageRequest pageRequest) {
        return PageResponse.from(repository.getFromFavouritesPage(customer.getKeycloakId(), pageRequest));
    }


}
