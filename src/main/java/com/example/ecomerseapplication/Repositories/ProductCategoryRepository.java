package com.example.ecomerseapplication.Repositories;

import com.example.ecomerseapplication.DTOs.AttributeOptionDTO;
import com.example.ecomerseapplication.Entities.AttributeName;
import com.example.ecomerseapplication.Entities.ProductCategory;
import com.nimbusds.jose.util.Pair;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.yaml.snakeyaml.util.Tuple;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Integer> {

    Optional<ProductCategory> findByCategoryName(String name);

    @Query(value = "select c.categoryName " +
            "from ProductCategory c " +
            "where c.categoryName!= 'електрически машини' AND c.categoryName!= 'Бензинови машини'")
    List<String> getAllNames();

    @Query(value =
    """
    select distinct new com.example.ecomerseapplication.DTOs.AttributeOptionDTO(an.attributeName, ca.attributeOption, aog.measurementUnit)
        from ProductCategory pc
    join Product p on p.productCategory = pc
        join p.categoryAttributeSet ca
            join AttributeName an on an = ca.attributeName
                join pc.attributeGroups ag
                    join AttributesOfGroup aog on aog.attributesOfGroupId.attributeGroup.id = ag.id
                        where pc.id=:categoryId
                            and aog.attributesOfGroupId.attributeName = an
    """)
    List<AttributeOptionDTO>getAttributesOfCategory(@Param("categoryId") int productCategoryId);

    @Query(value =
            """
            select distinct (an.attributeName,aog.measurementUnit)
                from ProductCategory pc
            join Product p on p.productCategory = pc
                join p.categoryAttributeSet ca
                    join AttributeName an on an = ca.attributeName
                        join pc.attributeGroups ag
                            join AttributesOfGroup aog on aog.attributesOfGroupId.attributeGroup.id = ag.id
                                where pc.id=:categoryId
                                    and aog.attributesOfGroupId.attributeName = an
                                                and aog.attributesOfGroupId.attributeName in :attributeNames
            """)
    List<String> getMeasurementUnitsOfCategoryAttributes(@Param("categoryId")int categoryId, @Param("attributeNames")List<AttributeName> attributeNameIds);
}
