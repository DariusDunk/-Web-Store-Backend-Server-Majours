package com.example.ecomerseapplication.Services;

import com.example.ecomerseapplication.Entities.ClientType;
import com.example.ecomerseapplication.Repositories.ClientTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class ClientTypeService {

    private final ClientTypeRepository clientTypeRepository;

    @Autowired
    public ClientTypeService(ClientTypeRepository clientTypeRepository) {
        this.clientTypeRepository = clientTypeRepository;
    }

    public ClientType getByTypeName(String typeName) {
        return clientTypeRepository.findClientTypeByClientTypeName(typeName).orElseThrow(() -> new ResourceNotFoundException("Client type not found"));
    }
}
