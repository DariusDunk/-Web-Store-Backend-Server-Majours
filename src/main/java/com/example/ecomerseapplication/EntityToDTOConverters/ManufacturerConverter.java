package com.example.ecomerseapplication.EntityToDTOConverters;

import com.example.ecomerseapplication.DTOs.ManufacturerDTOResponse;
import java.util.HashSet;
import java.util.Set;

public class ManufacturerConverter {

    public static Set<ManufacturerDTOResponse> objectArrSetToDtoSet(Set<Object[]> objects) {

        if (objects.isEmpty())
            return null;

        Set<ManufacturerDTOResponse> manufacturerDTOResponseSet = new HashSet<>();

        for (Object[] objectsArr : objects) {

            ManufacturerDTOResponse manufacturerDTOResponse = new ManufacturerDTOResponse();
            manufacturerDTOResponse.id = Integer.parseInt((objectsArr[0]).toString());
            manufacturerDTOResponse.name = objectsArr[1].toString();

            manufacturerDTOResponseSet.add(manufacturerDTOResponse);
        }

        return manufacturerDTOResponseSet;
    }
}
