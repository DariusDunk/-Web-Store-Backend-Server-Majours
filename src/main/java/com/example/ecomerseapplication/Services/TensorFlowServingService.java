package com.example.ecomerseapplication.Services;

import com.example.ecomerseapplication.DTOs.serverDtos.PredictionResponseDTO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.grpc.ManagedChannel;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.tensorflow.framework.DataType;
import org.tensorflow.framework.TensorProto;
import org.tensorflow.framework.TensorShapeProto;
import tensorflow.serving.Model;
import tensorflow.serving.Predict;
import tensorflow.serving.PredictionServiceGrpc;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TensorFlowServingService {

    private Map<String, String> categoryClasses;
    private Map<String, String> manufacturerClasses;
    private final PredictionServiceGrpc.PredictionServiceBlockingStub Stub;

    public TensorFlowServingService(ManagedChannel channel) {
        this.Stub = PredictionServiceGrpc.newBlockingStub(channel);
    }

    @PostConstruct
    public void init() {

        ObjectMapper mapper = new ObjectMapper();

        try (var catStream = getClass().getClassLoader()
                .getResourceAsStream("ClassMapping/category_mapping.json");
             var manStream = getClass().getClassLoader()
                     .getResourceAsStream("ClassMapping/manufacturer_mapping.json")) {

            if (catStream == null || manStream == null) {
                throw new RuntimeException("Mapping files not found in classpath");
            }

            categoryClasses = mapper.readValue(
                    catStream,
                    new TypeReference<>() {
                    }
            );

            manufacturerClasses = mapper.readValue(
                    manStream,
                    new TypeReference<>() {
                    }
            );

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public PredictionResponseDTO gRPCRequest(BufferedImage image) {

        float[][][][] inputData = preprocessImage(image);

        TensorProto.Builder tensorBuilder = TensorProto.newBuilder();
        TensorShapeProto.Builder shapeBuilder = TensorShapeProto.newBuilder();
        shapeBuilder.addDim(TensorShapeProto.Dim.newBuilder().setSize(1));
        shapeBuilder.addDim(TensorShapeProto.Dim.newBuilder().setSize(224));
        shapeBuilder.addDim(TensorShapeProto.Dim.newBuilder().setSize(224));
        shapeBuilder.addDim(TensorShapeProto.Dim.newBuilder().setSize(3));

        tensorBuilder.setDtype(DataType.DT_FLOAT);
        tensorBuilder.setTensorShape(shapeBuilder.build());

        for (int b = 0; b < 1; b++) {
            for (int y = 0; y < 224; y++) {
                for (int x = 0; x < 224; x++) {
                    for (int c = 0; c < 3; c++) {
                        tensorBuilder.addFloatVal(inputData[b][y][x][c]);
                    }
                }
            }
        }

        TensorProto inputTensor = tensorBuilder.build();

        Predict.PredictRequest request = Predict.PredictRequest.newBuilder()
                .setModelSpec(Model.ModelSpec.newBuilder()
                        .setName("product_image_search_model")
                        .setSignatureName("serving_default"))
                .putInputs("keras_tensor_154", inputTensor)
                .build();

        Predict.PredictResponse response = Stub.predict(request);


        TensorProto categoryTensor =
                response.getOutputsOrThrow("output_0");

        TensorProto manufacturerTensor =
                response.getOutputsOrThrow("output_1");

        List<Float> categoryPredictions =
                categoryTensor.getFloatValList();

        List<Float> manufacturerPredictions =
                manufacturerTensor.getFloatValList();
//
//        System.out.println("Category predictions: " + categoryPredictions);
//        System.out.println("Manufacturer predictions: " + manufacturerPredictions);

        Map<String, Float> categoryPredictionsMap = new HashMap<>();

        mapPredictions(categoryPredictions, categoryClasses, categoryPredictionsMap);

        Map<String, Float> manufacturerPredictionsMap = new HashMap<>();

        mapPredictions(manufacturerPredictions, manufacturerClasses, manufacturerPredictionsMap);

        return new PredictionResponseDTO(categoryPredictionsMap, manufacturerPredictionsMap);
    }

    private void mapPredictions(List<Float> categoryPredictions, Map<String, String> classMap, Map<String, Float> predictionMap) {
        for (int i = 0; i < categoryPredictions.size(); i++) {

            String className = classMap.get(String.valueOf(i));

            predictionMap.put(
                    className,
                    categoryPredictions.get(i)
            );
        }
    }

    private float[][][][] preprocessImage(BufferedImage original){

        Image tmp = original.getScaledInstance(224, 224, Image.SCALE_SMOOTH);
        BufferedImage resized = new BufferedImage(224, 224, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = resized.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();

        float[][][][] arr = new float[1][224][224][3];
        for (int y = 0; y< 224; y++){
            for (int x = 0; x< 224; x++){
                int rgb = resized.getRGB(x, y);
                int r = (rgb >> 16) & 0xff;
                int g = (rgb >> 8) & 0xff;
                int b = rgb & 0xff;

                arr[0][y][x][0] = r / 255.0f;
                arr[0][y][x][1] = g / 255.0f;
                arr[0][y][x][2] = b / 255.0f;
            }
        }
        return arr;
    }
}
