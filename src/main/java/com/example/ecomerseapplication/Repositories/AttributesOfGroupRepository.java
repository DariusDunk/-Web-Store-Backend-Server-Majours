package com.example.ecomerseapplication.Repositories;

import com.example.ecomerseapplication.CompositeIdClasses.AttributesOfGroupId;
import com.example.ecomerseapplication.Entities.AttributesOfGroup;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttributesOfGroupRepository extends JpaRepository<AttributesOfGroup, AttributesOfGroupId> {
}
