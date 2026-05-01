package com.example.ecomerseapplication.Utils.Revokers;

import com.example.ecomerseapplication.Entities.Sale;
import com.example.ecomerseapplication.Services.SaleProductService;
import com.example.ecomerseapplication.Services.SaleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class SaleRevoker {

    private final SaleService saleService;
    private final SaleProductService saleProductService;

    @Autowired
    public SaleRevoker(SaleService saleService, SaleProductService saleProductService) {
        this.saleService = saleService;
        this.saleProductService = saleProductService;
    }

    @Transactional
    public void revokeExpiredSales()
    {
        List<Sale> expiredSales = saleService.getExpiredSales();

        if (!expiredSales.isEmpty())
        {
            saleProductService.unmarkAsMainBySales(expiredSales);
            saleService.markAsInActive(expiredSales);

            ZoneId zoneId = ZoneId.systemDefault();
            ZonedDateTime now = ZonedDateTime.now(zoneId);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            System.out.println("-------------------------[" + now.format(formatter) + "]" + " Revoking expired sales-------------------------");
            System.out.println("-------------------------Revoked " + expiredSales.size() + " expired sales-------------------------");
        }
    }
}
