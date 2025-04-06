package com.example.demo.dto.commentdto;

import com.example.demo.dto.hastagdto.HashtagDTO;

import java.time.LocalDateTime;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;

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
