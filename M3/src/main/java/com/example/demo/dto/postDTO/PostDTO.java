package com.example.demo.dto.postDTO;

import com.example.demo.dto.commentdto.CommentDTO;

import com.example.demo.dto.hastagdto.HashtagDTO;
import com.example.demo.dto.reactionsummarydto.ReactionSummaryDTO;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
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

    private List<ReactionSummaryDTO> reactionSummary;


}
