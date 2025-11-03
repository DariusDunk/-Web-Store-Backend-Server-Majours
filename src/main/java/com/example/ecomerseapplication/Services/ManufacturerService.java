package com.example.ecomerseapplication.Services;

import com.example.ecomerseapplication.Entities.Manufacturer;
import com.example.ecomerseapplication.Entities.ProductCategory;
import com.example.ecomerseapplication.Repositories.ManufacturerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Set;

@Service
public class ManufacturerService {


    private final ManufacturerRepository manufacturerRepository;

    @Autowired
    public ManufacturerService(ManufacturerRepository manufacturerRepository) {
        this.manufacturerRepository = manufacturerRepository;
    }

    public List<Manufacturer> getAll() {
        return manufacturerRepository.findAll();
    }

    public Set<Object[]> getByCategory(ProductCategory productCategory) {
        return manufacturerRepository.getByCategory(productCategory);
    }

    public Manufacturer findByName(String manufacturerName) {
        return manufacturerRepository.findByManufacturerName(manufacturerName).orElse(null);
    }
}
