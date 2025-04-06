package com.example.demo.dto.postDTO;

import com.example.demo.dto.commentdto.CommentDTO;
import com.example.demo.dto.hashtagDTO.HashtagDTO;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostDTO {

    private Long id;

    private Long userId;

    private String content;

    private String imageName;

    private String imageType;

    private byte[] imageData;

    private LocalDateTime createdAt;

    private Set<HashtagDTO> hashtags;

    private Long durationDays;

    private Set<CommentDTO> comments;

}
