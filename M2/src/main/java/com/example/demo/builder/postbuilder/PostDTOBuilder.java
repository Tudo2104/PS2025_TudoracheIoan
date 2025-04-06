package com.example.demo.builder.postbuilder;
import com.example.demo.dto.commentdto.CommentDTO;
import com.example.demo.dto.hastagdto.HashtagDTO;
import com.example.demo.dto.postdto.PostDTO;
import com.example.demo.entity.Hashtag;
import com.example.demo.entity.Post;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

public class PostDTOBuilder {

    public static PostDTO generateDTOFromEntity(Post post) {
        Set<HashtagDTO> hashtagDTOs = post.getHashtags().stream()
                .map(h -> HashtagDTO.builder()
                        .id(h.getId())
                        .name(h.getName())
                        .build())
                .collect(Collectors.toSet());
        Set<CommentDTO> commentDTOs = post.getComments().stream()
                .map(CommentDTOBuilder::generateDTOFromEntity)
                .collect(Collectors.toSet());

        return PostDTO.builder()
                .id(post.getId())
                .userId(post.getUserId())
                .content(post.getContent())
                .imageName(post.getImageName())
                .imageType(post.getImageType())
                .imageData(post.getImageData())
                .createdAt(post.getCreatedAt())
                .hashtags(hashtagDTOs)
                .durationDays(post.getDurationDays())
                .comments(commentDTOs)
                .build();
    }


}
