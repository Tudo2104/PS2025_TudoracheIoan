package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
@Entity
@Table(name = "moderator_actions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModeratorAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "moderator_id", nullable = false)
    private Long moderatorId;

    @Column(name = "action_type", nullable = false)
    private String actionType;

    @Column(name = "target_user_id")
    private Long targetUserId;

    @Column(name = "target_post_id")
    private Long targetPostId;

    @Column(name = "target_comment_id")
    private Long targetCommentId;

    @Column(name = "read_status", nullable = false)
    private boolean readStatus = false;

    @Column(name = "is_blocked", nullable = false)
    private boolean blocked = false;

    @JsonFormat(pattern = "MM-dd-yyyy HH:mm:ss")
    @DateTimeFormat(pattern = "MM-dd-yyyy HH:mm:ss")
    @Column(name = "created_at", nullable = false, updatable = false, insertable = false, columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;
}
