package com.example.ecomerseapplication.Utils;

import com.example.ecomerseapplication.Services.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class Schedulers {

   private final SessionRevoker sessionRevoker;

   @Autowired
    public Schedulers(SessionRevoker sessionRevoker) {
        this.sessionRevoker = sessionRevoker;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void revokeExpiredOnStartup() {
        sessionRevoker.revokeExpiredSessions();
    }

    @Scheduled(fixedDelay = 300000)
    public void revokeExpiredPeriodically() {
        sessionRevoker.revokeExpiredSessions();
    }


}
