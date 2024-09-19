//package com.personal_project.Next_to_read.model;
//
//import jakarta.persistence.*;
//import lombok.Data;
//
//import java.time.LocalDateTime;
//
//@Entity
//@Table(name = "password_reset")
//@Data
//public class PasswordReset {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name="user_id", nullable = false)
//    private User user;
//
//    @Column(name="token", nullable = false, unique = true)
//    private String token;
//
//    @Column(name="created_at", nullable = false)
//    private LocalDateTime createdAt;
//
//    @Column(name="expired_at", nullable = false)
//    private LocalDateTime expiresAt;
//
//    @Column(name="used", nullable = false)
//    private Boolean used = false;
//}
