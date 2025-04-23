package com.example.demo.dto.commentdto;

import lombok.*;

import java.time.LocalDateTime;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentDTO {

    private Long id;

    private Long userId;

    private String username;

    private String content;

    private String imageName;

    private String imageType;

    private byte[] imageData;

    private LocalDateTime createdAt;

    private Long durationDays;

    private Long postId;
}
