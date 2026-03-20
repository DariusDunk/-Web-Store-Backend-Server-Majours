package com.example.ecomerseapplication.Services;

import com.example.ecomerseapplication.CompositeIdClasses.FavoriteOfCustomerId;
import com.example.ecomerseapplication.CustomErrorHelpers.ErrorType;
import com.example.ecomerseapplication.DTOs.responses.CompactProductResponse;
import com.example.ecomerseapplication.DTOs.responses.ErrorResponse;
import com.example.ecomerseapplication.DTOs.responses.PageResponse;
import com.example.ecomerseapplication.Entities.Customer;
import com.example.ecomerseapplication.Entities.FavoriteOfCustomer;
import com.example.ecomerseapplication.Entities.Product;
import com.example.ecomerseapplication.ExceptionHandling.CustomExceptions.FavouriteInsertFailedException;
import com.example.ecomerseapplication.ExceptionHandling.CustomExceptions.FavouriteSizeLimitReachedException;
import com.example.ecomerseapplication.ExceptionHandling.CustomExceptions.ProductAlreadyInFavouritesException;
import com.example.ecomerseapplication.Others.GlobalConstants;
import com.example.ecomerseapplication.Others.PageContentLimit;
import com.example.ecomerseapplication.Repositories.FavoriteOfCustomerRepository;
import org.springframework.transaction.annotation.Transactional;
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
    public void addToFavorite(Customer customer, Product product) {

        if (repository.countAllByFavoriteOfCustomerId_Customer_keycloakId(customer.getKeycloakId()) >= GlobalConstants.favoritesSizeLimit)
            throw new FavouriteSizeLimitReachedException("Favourites limit reached");

        if (isInFavorites(customer, product)) {
            throw new ProductAlreadyInFavouritesException("Product already in favorites");
        }

        FavoriteOfCustomerId favId = new FavoriteOfCustomerId(product, customer);
        FavoriteOfCustomer favoriteEntry = new FavoriteOfCustomer(favId, Instant.now());
        try {
            repository.save(favoriteEntry);
        }
        catch (Exception e)
        {
            throw new FavouriteInsertFailedException("Failed to save favorite: " + e.getMessage(), e);
        }
    }
    @Transactional
    public ResponseEntity<?> removeFromFavoritesWRefetch(Customer customer, Product product, int currentPage) {
        try {
          repository.deleteFavoriteOfCustomerByFavoriteOfCustomerId_CustomerAndFavoriteOfCustomerId_Product(customer, product);

            return calculateNewPageWContent(customer, currentPage);

        } catch (Exception e) {
            System.out.println("Error removing favorite: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Transactional
    public ResponseEntity<?> removeFromFavorites(Customer customer, Product product) {
        try {
            repository.deleteFavoriteOfCustomerByFavoriteOfCustomerId_CustomerAndFavoriteOfCustomerId_Product(customer, product);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            System.out.println("Error removing favorite: " + e.getMessage());
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
