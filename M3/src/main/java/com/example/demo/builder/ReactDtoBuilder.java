package com.example.demo.builder;

import com.example.demo.dto.reactDTO.ReactDTO;
import com.example.demo.entity.React;

public class ReactDtoBuilder {

    public static ReactDTO  generateDTOFromEntity(React entity) {
        return ReactDTO.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .postId(entity.getPostId())
                .commentId(entity.getCommentId())
                .type(entity.getType().name())
                .build();
    }
}
