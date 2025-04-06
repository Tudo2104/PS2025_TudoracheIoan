package com.example.demo.dto.postdto;
import com.example.demo.dto.commentdto.CommentDTO;
import com.example.demo.dto.hastagdto.HashtagDTO;
import com.example.demo.entity.Hashtag;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    private Set<CommentDTO> comments;

    private Long durationDays;
}
