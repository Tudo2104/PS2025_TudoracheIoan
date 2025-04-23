package com.example.demo.builder;


import com.example.demo.dto.reactDTO.ReactDTO;
import com.example.demo.entity.React;
import com.example.demo.entity.ReactionType;

import java.time.LocalDateTime;

public class ReactBuilder {

    public static React generateEntityFromDTO(ReactDTO dto, ReactionType type) {
        return React.builder()
                .id(dto.getId())
                .userId(dto.getUserId())
                .postId(dto.getPostId())
                .commentId(dto.getCommentId())
                .type(type)
                .createdAt(LocalDateTime.now())
                .build();
    }
}

