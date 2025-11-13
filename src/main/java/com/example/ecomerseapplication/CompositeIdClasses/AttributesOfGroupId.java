package com.example.ecomerseapplication.CompositeIdClasses;

import com.example.ecomerseapplication.Entities.AttributeGroup;
import com.example.ecomerseapplication.Entities.AttributeName;
import jakarta.persistence.Embeddable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Getter
@NoArgsConstructor
@EqualsAndHashCode
@AllArgsConstructor
public class AttributesOfGroupId implements Serializable {

    @JoinColumn(name = "attribute_group_id")
    @ManyToOne
    private AttributeGroup attributeGroup;

    @JoinColumn(name = "attribute_name_id")
    @ManyToOne
    private AttributeName attributeName;


}
