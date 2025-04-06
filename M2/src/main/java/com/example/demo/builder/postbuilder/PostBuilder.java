package com.example.demo.builder.postbuilder;

import com.example.demo.dto.postdto.PostDTO;
import com.example.demo.entity.Hashtag;
import com.example.demo.entity.Post;

import java.time.LocalDateTime;
import java.util.Set;

public class PostBuilder {

    public static Post generateEntityFromDTO(PostDTO postDTO, Set<Hashtag> hashtags) {
        return Post.builder()
                .id(postDTO.getId())
                .userId(postDTO.getUserId())
                .content(postDTO.getContent())
                .imageName(postDTO.getImageName())
                .imageType(postDTO.getImageType())
                .imageData(postDTO.getImageData())
                .createdAt(LocalDateTime.now())
                .hashtags(hashtags)
                .durationDays(postDTO.getDurationDays())
                .build();
    }


}
