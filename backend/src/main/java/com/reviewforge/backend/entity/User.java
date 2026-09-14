package com.reviewforge.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "email")
        }
)
public class User {

    // ==========================================
    // ID
    // ==========================================

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Long id;


    // ==========================================
    // USER NAME
    // ==========================================

    @Column(
            nullable = false
    )
    private String name;


    // ==========================================
    // EMAIL
    // ==========================================

    @Column(
            nullable = false,
            unique = true
    )
    private String email;


    // ==========================================
    // PASSWORD
    // ==========================================

    @Column(
            nullable = false
    )
    private String password;


    // ==========================================
    // GETTERS AND SETTERS
    // ==========================================

    public Long getId() {
        return id;
    }

    public void setId(
            Long id
    ) {
        this.id = id;
    }


    public String getName() {
        return name;
    }

    public void setName(
            String name
    ) {
        this.name = name;
    }


    public String getEmail() {
        return email;
    }

    public void setEmail(
            String email
    ) {
        this.email = email;
    }


    public String getPassword() {
        return password;
    }

    public void setPassword(
            String password
    ) {
        this.password = password;
    }
}