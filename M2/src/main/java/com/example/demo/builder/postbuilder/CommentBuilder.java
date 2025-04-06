package com.example.demo.builder.postbuilder;

import com.example.demo.dto.commentdto.CommentDTO;
import com.example.demo.dto.postdto.PostDTO;
import com.example.demo.entity.Comment;
import com.example.demo.entity.Hashtag;
import com.example.demo.entity.Post;

import java.time.LocalDateTime;
import java.util.Set;

public class CommentBuilder {

    public static Comment generateEntityFromDTO(CommentDTO commentDTO) {
        Post post = Post.builder().id(commentDTO.getPostId()).build();
        return Comment.builder()
                .id(commentDTO.getId())
                .userId(commentDTO.getUserId())
                .username(commentDTO.getUsername())
                .content(commentDTO.getContent())
                .imageName(commentDTO.getImageName())
                .imageType(commentDTO.getImageType())
                .imageData(commentDTO.getImageData())
                .createdAt(LocalDateTime.now())
                .durationDays(commentDTO.getDurationDays())
                .post(post)
                .build();
    }
}
