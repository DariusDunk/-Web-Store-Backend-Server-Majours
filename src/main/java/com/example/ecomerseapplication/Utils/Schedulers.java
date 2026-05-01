package com.example.ecomerseapplication.Utils;

import com.example.ecomerseapplication.Utils.Revokers.SaleRevoker;
import com.example.ecomerseapplication.Utils.Revokers.SessionRevoker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class Schedulers {

   private final SessionRevoker sessionRevoker;
   private final SaleRevoker saleRevoker;

   @Autowired
    public Schedulers(SessionRevoker sessionRevoker, SaleRevoker saleRevoker) {
        this.sessionRevoker = sessionRevoker;
       this.saleRevoker = saleRevoker;
   }

    @EventListener(ApplicationReadyEvent.class)
    public void revokeExpiredOnStartup() {
        sessionRevoker.revokeExpiredSessions();
    }

    @Scheduled(fixedDelay = 300000, initialDelay = 300000)
    public void revokeExpiredPeriodically() {
        sessionRevoker.revokeExpiredSessions();
    }

    @EventListener(ApplicationReadyEvent.class)
    public void revokeExpiredSalesOnStartup() {
        saleRevoker.revokeExpiredSales();
    }

    @Scheduled(cron = "0 0 0 * * *", zone = "Europe/Sofia")
    public void revokeExpiredSalesPeriodically() {
        saleRevoker.revokeExpiredSales();
    }

}
