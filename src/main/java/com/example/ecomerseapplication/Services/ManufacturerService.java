package com.example.ecomerseapplication.Services;

import com.example.ecomerseapplication.Entities.Manufacturer;
import com.example.ecomerseapplication.Entities.ProductCategory;
import com.example.ecomerseapplication.Repositories.ManufacturerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Set;

@Service
public class ManufacturerService {


    private final ManufacturerRepository repository;

    @Autowired
    public ManufacturerService(ManufacturerRepository repository) {
        this.repository = repository;
    }

    public List<Manufacturer> getAll() {
        return repository.findAll();
    }

//    public Set<Object[]> getByCategory(ProductCategory productCategory) {
//        return repository.getByCategory(productCategory);
//    }

    public Manufacturer findByName(String manufacturerName) {
        return repository.findByManufacturerName(manufacturerName).orElseThrow(()->new ResourceNotFoundException("Manufacturer not found with name: " + manufacturerName));
    }

    public List<Manufacturer> getByNames(List<String> manufacturerNames) {
        return repository.findAllByManufacturerNameIn(manufacturerNames);
    }

    public Set<String> getNamesByCategory(ProductCategory category) {
        Set<String> names = repository.getNamesByCategory(category);
        if (names.isEmpty()) {
            throw new ResourceNotFoundException("No manufacturers found for category "+ category.getCategoryName());
        }
        return names;
    }
}
