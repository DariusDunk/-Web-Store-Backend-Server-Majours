package com.example.ecomerseapplication.Services;

import com.example.ecomerseapplication.DTOs.responses.CompactManufacturerResponse;
import com.example.ecomerseapplication.DTOs.serverDtos.projectionInterfaces.ManufacturerProjection;
import com.example.ecomerseapplication.Entities.Manufacturer;
import com.example.ecomerseapplication.Entities.ProductCategory;
import com.example.ecomerseapplication.Mappers.ManufacturerMapper;
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

//    public List<Manufacturer> getAll() {
//        return repository.findAll();
//    }

    public List<CompactManufacturerResponse> getAllCompact() {
        List<ManufacturerProjection> projections = repository.getAllProjections();

        return ManufacturerMapper.projectionListToCompactResponseList(projections);
    }

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

    public Manufacturer getById(int id) {
        return repository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Manufacturer not found with id: " + id));
    }

    public CompactManufacturerResponse getByIdCompact(Integer id) {
        Manufacturer manufacturer = getById(id);
        return new CompactManufacturerResponse(manufacturer.getManufacturerName(), manufacturer.getId());
    }
}
