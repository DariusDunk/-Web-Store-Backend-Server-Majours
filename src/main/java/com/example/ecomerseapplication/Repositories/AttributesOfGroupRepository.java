package com.example.ecomerseapplication.Repositories;

import com.example.ecomerseapplication.CompositeIdClasses.AttributesOfGroupId;
import com.example.ecomerseapplication.DTOs.serverDtos.projectionInterfaces.AttributeOfGroupProjection;
import com.example.ecomerseapplication.Entities.AttributeOfGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttributesOfGroupRepository extends JpaRepository<AttributeOfGroup, AttributesOfGroupId> {


    @Query(
"""
select aog.measurementUnit as measurementUnit,
an.attributeName as name,
ag.id as groupId
from AttributeOfGroup aog
join aog.attributesOfGroupId.attributeName an
join aog.attributesOfGroupId.attributeGroup ag
where ag.id in :groupIds
"""
    )
    List<AttributeOfGroupProjection> findByGroupIds(@Param("groupIds") List<Long> groupIds);
}
