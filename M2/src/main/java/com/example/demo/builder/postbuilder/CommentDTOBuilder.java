package com.example.demo.builder.postbuilder;

import com.example.demo.dto.commentdto.CommentDTO;
import com.example.demo.entity.Comment;

public class CommentDTOBuilder {

    public static CommentDTO generateDTOFromEntity(Comment comment) {
        return CommentDTO.builder()
                .id(comment.getId())
                .userId(comment.getUserId())
                .username(comment.getUsername())
                .content(comment.getContent())
                .imageName(comment.getImageName())
                .imageType(comment.getImageType())
                .imageData(comment.getImageData())
                .createdAt(comment.getCreatedAt())
                .durationDays(comment.getDurationDays())
                .postId(comment.getPost().getId())
                .build();
    }

}
