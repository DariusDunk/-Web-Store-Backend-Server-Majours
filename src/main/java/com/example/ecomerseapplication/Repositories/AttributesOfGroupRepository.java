package com.example.ecomerseapplication.Repositories;

import com.example.ecomerseapplication.CompositeIdClasses.AttributesOfGroupId;
import com.example.ecomerseapplication.Entities.AttributesOfGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AttributesOfGroupRepository extends JpaRepository<AttributesOfGroup, AttributesOfGroupId> {
}
