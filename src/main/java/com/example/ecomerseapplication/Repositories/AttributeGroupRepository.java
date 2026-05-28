package com.example.ecomerseapplication.Repositories;

import com.example.ecomerseapplication.DTOs.serverDtos.projectionInterfaces.AttributeGroupsWithCategoryProjection;
import com.example.ecomerseapplication.DTOs.serverDtos.projectionInterfaces.AttributeOfProjection;
import com.example.ecomerseapplication.Entities.AttributeGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface AttributeGroupRepository extends JpaRepository<AttributeGroup, Long>{


    @Query("""
select ag.id as id,
ag.groupName as name,
exists (
    select 1
    from ag.categories c
    where c.id = :categoryId
) as isInCategory
from AttributeGroup ag
""")
    List<AttributeGroupsWithCategoryProjection> getAllWithCategory(@Param("categoryId") int categoryId);

    List<AttributeGroup> getAttributeGroupByGroupNameIn(Collection<String> groupNames);

        @Query(
"""
select an.measurementUnit as measurementUnit,
an.attributeName as name,
ag.id as groupId
from AttributeGroup ag
join ag.attributeNames an
"""
    )
    List<AttributeOfProjection> findAllProjection();

        @Query(
"""
select an.measurementUnit as measurementUnit,
an.attributeName as name,
ag.id as groupId
from AttributeGroup ag
join ag.attributeNames an
where ag.id in :groupIds
"""
    )
    List<AttributeOfProjection> findByGroupIdsProjection(@Param("groupIds") List<Long> groupIds);

        @Query(
"""
select an.attributeName as name,
an.id as nameId,
an.measurementUnit as measurementUnit
from AttributeGroup ag
join ag.attributeNames an
join ag.categories pc
where pc.id = ?1
"""
        )
    List<AttributeOfProjection> getAttributesByCategoryId(int categoryId);
}
