package com.example.ecomerseapplication.Entities;

import com.example.ecomerseapplication.CompositeIdClasses.AttributesOfGroupId;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "attributes_of_group", schema = "online_shop")
@Data
public class AttributesOfGroup {

    @EmbeddedId
    private AttributesOfGroupId attributesOfGroupId;

    @Column(name = "measurement_unit")
    private String measurementUnit;
}
