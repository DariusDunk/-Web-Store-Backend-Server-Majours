package com.example.ecomerseapplication.Services;

import com.example.ecomerseapplication.DTOs.responses.ImageSearchPagedResponse;
import com.example.ecomerseapplication.DTOs.serverDtos.PredictionResponseDTO;
import com.example.ecomerseapplication.Entities.Manufacturer;
import com.example.ecomerseapplication.Entities.ProductCategory;
import com.example.ecomerseapplication.ExceptionHandling.CustomExceptions.NoCategoryAndManufacturerPresentException;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;

@Service
public class ImageSearchService {

    private final TensorFlowServingService tensorflowServingService;
    private final ProductService productService;
    private final CategoryService categoryService;
    private final ManufacturerService manufacturerService;

    public ImageSearchService(TensorFlowServingService tensorflowServingService, ProductService productService, CategoryService categoryService, ManufacturerService manufacturerService) {
        this.tensorflowServingService = tensorflowServingService;
        this.productService = productService;
        this.categoryService = categoryService;
        this.manufacturerService = manufacturerService;
    }

    public ImageSearchPagedResponse findByImage(BufferedImage image) {

        float categoryThreshold = 0.25f;
        float manufacturerThreshold = 0.35f;

        PredictionResponseDTO predictionResponseDTO = tensorflowServingService.gRPCRequest(image);

        List<Map.Entry<String, Float>> topCategoryPredictions = evaluatePredictions(predictionResponseDTO
                .categoryPredictions(), categoryThreshold);
        List<Map.Entry<String, Float>> topManufacturerPredictions = evaluatePredictions(predictionResponseDTO
                .manufacturerPredictions(),manufacturerThreshold);

        List<String> topCategoryNames = topCategoryPredictions.stream().map(Map.Entry::getKey).toList();
        List<String> topManufacturerNames = topManufacturerPredictions.stream().map(Map.Entry::getKey).toList();

        if (topCategoryNames.isEmpty() && topManufacturerNames.isEmpty()) {
            throw new NoCategoryAndManufacturerPresentException("No categories or manufacturers found from inference");
        }

        List<ProductCategory> categories = categoryService.getAllByNames(topCategoryNames);
        List<Manufacturer> manufacturers = manufacturerService.getByNames(topManufacturerNames);

       return productService.getByCategoriesAndManufacturers(categories, manufacturers,0);
    }

    private List<Map.Entry<String, Float>> evaluatePredictions(
            Map<String, Float> predictions, float threshold
    ) {

        List<Map.Entry<String, Float>> orderedPredictions =
                predictions.entrySet()
                        .stream()
                        .sorted((a, b) ->
                                Float.compare(b.getValue(), a.getValue()))
                        .toList();

        if (orderedPredictions.isEmpty()) {
            return List.of();
        }

        float comparisonThreshold = 0.10f;
        float topScore = orderedPredictions.getFirst().getValue();

        return orderedPredictions.stream()
                .filter(entry ->
                       ( topScore - entry.getValue() <= comparisonThreshold)
                && (entry.getValue() >= threshold))
                .limit(3)
                .toList();
    }

}
