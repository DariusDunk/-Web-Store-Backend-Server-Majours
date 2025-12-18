package com.example.ecomerseapplication.Entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "sessions", schema = "online_shop")
@Data
@NoArgsConstructor
public class Session {

    @Id
    private String sessionId;

    @ManyToOne()
    @JoinColumn(name = "customer_id", referencedColumnName = "k_id")
    private Customer customer;

    @ManyToOne()
    @JoinColumn(name = "client_type")
    private ClientType clientType;

    @Column(name = "is_guest")
    private Boolean isGuest;

    @Column(name = "refresh_token")
    private String refreshToken;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
