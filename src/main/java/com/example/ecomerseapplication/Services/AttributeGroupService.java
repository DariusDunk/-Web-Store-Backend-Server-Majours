package com.example.ecomerseapplication.Services;

import com.example.ecomerseapplication.DTOs.serverDtos.projectionInterfaces.AttributeGroupsWithCategoryProjection;
import com.example.ecomerseapplication.Repositories.AttributeGroupRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AttributeGroupService {
    private final AttributeGroupRepository attributeGroupRepository;

    public AttributeGroupService(AttributeGroupRepository attributeGroupRepository) {
        this.attributeGroupRepository = attributeGroupRepository;
    }

    public List<AttributeGroupsWithCategoryProjection> getAllWithACategory(int categoryId) {
        return attributeGroupRepository.getAllWithCategory(categoryId);
    }
}
