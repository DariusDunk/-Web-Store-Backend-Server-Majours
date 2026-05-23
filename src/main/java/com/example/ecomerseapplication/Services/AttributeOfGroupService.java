package com.example.ecomerseapplication.Services;

import com.example.ecomerseapplication.DTOs.serverDtos.projectionInterfaces.AttributeOfGroupProjection;
import com.example.ecomerseapplication.Repositories.AttributesOfGroupRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AttributeOfGroupService {

    private final AttributesOfGroupRepository attributeOfGroupRepository;

    public AttributeOfGroupService(AttributesOfGroupRepository attributeOfGroupRepository) {
        this.attributeOfGroupRepository = attributeOfGroupRepository;
    }

    public List<AttributeOfGroupProjection> getByGroupId(List<Long> groupIds) {
        return attributeOfGroupRepository.findByGroupIdsProjection(groupIds);
    }

    public List<AttributeOfGroupProjection> getAllDetailedProjection() {
        return  attributeOfGroupRepository.findAllProjection();
    }
}
