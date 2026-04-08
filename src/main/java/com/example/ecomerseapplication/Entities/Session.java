package com.example.ecomerseapplication.Entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "sessions", schema = "online_shop")
@Data
@NoArgsConstructor
public class Session {

    @Id
    private String sessionId;

    @ManyToOne()
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @ManyToOne()
    @JoinColumn(name = "client_type")
    private ClientType clientType;

    @Column(name = "is_guest")
    private Boolean isGuest;

    @Column(name = "refresh_token")
    private String refreshToken;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;

    @CreationTimestamp
    @Column(name = "last_activity_at")
    private Instant lastActivityAt;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "device_name")
    private String deviceName;

    @Column(name = "user_agent")
    private String userAgent;

    @Column(name = "is_revoked")
    private Boolean isRevoked;

    public boolean isExpired() {
        return expiresAt != null && Instant.now().isAfter(expiresAt);
    }

    @Column(name = "is_remember_me_session")
    private Boolean isRememberMeSession;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Override
    public String toString() {
        return "Session{" +
                "refreshToken='" + refreshToken + '\'' +
                ", sessionId='" + sessionId + '\'' +
                ", customer=" + ((customer!=null)? customer.getFirstName() + " " + customer.getLastName():" " )+
                ", clientType=" + (clientType!=null? clientType :" ") +
                ", isGuest=" + isGuest +
                ", expiresAt=" + expiresAt +
                ", createdAt=" + createdAt +
                ", isRevoked=" + isRevoked +
                ", isRememberMeSession=" + isRememberMeSession +
                '}';
    }

    //todo v byde6te i za location 6te ima ne6to
}
