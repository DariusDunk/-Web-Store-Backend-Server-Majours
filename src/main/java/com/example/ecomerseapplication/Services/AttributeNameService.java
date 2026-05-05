package com.example.ecomerseapplication.Services;

import com.example.ecomerseapplication.Repositories.AttributeNameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AttributeNameService {


    private final AttributeNameRepository attributeNameRepository;

    @Autowired
    public AttributeNameService(AttributeNameRepository attributeNameRepository) {
        this.attributeNameRepository = attributeNameRepository;
    }

// --Commented out by Inspection START (2.5.2026  . 17:29):
//    public Set<AttributeName> getNameSetByCategory(ProductCategory productCategory) {
//        return attributeNameRepository.getByCategory(productCategory);
//    }
// --Commented out by Inspection STOP (2.5.2026  . 17:29)
}
