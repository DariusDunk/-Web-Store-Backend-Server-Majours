package com.example.ecomerseapplication.Repositories;

import com.example.ecomerseapplication.Entities.AttributeName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface AttributeNameRepository extends JpaRepository<AttributeName, Integer> {

    @Query(
"""
select an
from AttributeName an
join fetch an.categoryAttributeList
where an.id in ?1
"""
    )
    List<AttributeName> getAllByIdInWithOptions(Collection<Integer> ids);

    Collection<Integer> id(int id);
}
