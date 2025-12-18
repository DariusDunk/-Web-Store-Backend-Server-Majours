package com.example.ecomerseapplication.Controllers;

import com.example.ecomerseapplication.Services.ClientTypeService;
import com.example.ecomerseapplication.Services.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("session/")
public class SessionController {

    private final SessionService sessionService;
    private final ClientTypeService clientTypeService;

    @Autowired
    public SessionController(SessionService sessionService, ClientTypeService clientTypeService) {
        this.sessionService = sessionService;
        this.clientTypeService = clientTypeService;
    }
}
