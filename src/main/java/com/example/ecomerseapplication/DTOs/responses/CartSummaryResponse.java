package com.example.ecomerseapplication.DTOs.responses;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CartSummaryResponse(@JsonProperty("cart_total")
                                  Long cartTotalCoins,
                                  @JsonProperty("cart_quantity")
                                  Long cartQuantity) {

}
