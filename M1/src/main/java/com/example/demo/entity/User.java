package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(
            name = "id",
            updatable = false
    )
    private Long id;

    @Column(
            name = "name",
            nullable = false,
            length = 100
    )
    private String name;

    @Column(
            name = "email",
            nullable = false,
            unique = true
    )
    private String email;


    @Getter
    @JsonIgnore
    @Column(
            name = "password",
            nullable = false
    )
    private String password;

    @JsonFormat(pattern = "MM-dd-yyy hh:mm:ss")
    @DateTimeFormat(pattern = "MM-dd-yyy hh:mm:ss")
    @Column(name = "timeStamp", nullable = false)
    private LocalDateTime timeStamp;

    @ManyToOne
    @JoinColumn(name = "role_id")
    private Role role;

    @Enumerated(EnumType.STRING)
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Column(
            name = "status",
            nullable = false
    )
    private PostStatus status;


    @PrePersist
    protected void onCreate() {
        this.status = PostStatus.FRIENDS;
    }


}
