package com.example.demo.dto.commentdto;

import com.example.demo.dto.reactionsummarydto.ReactionSummaryDTO;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

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

    private List<ReactionSummaryDTO> reactionSummary;

    private long totalReactions;

}
