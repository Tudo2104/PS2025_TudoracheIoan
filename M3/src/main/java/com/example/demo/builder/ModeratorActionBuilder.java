package com.example.demo.builder;

import com.example.demo.dto.moderatoractionDTO.ModeratorActionDTO;
import com.example.demo.entity.ModeratorAction;

public class ModeratorActionBuilder {

    public static ModeratorActionDTO generateDTOFromEntity(ModeratorAction entity) {
        return ModeratorActionDTO.builder()
                .id(entity.getId())
                .moderatorId(entity.getModeratorId())
                .actionType(entity.getActionType())
                .targetUserId(entity.getTargetUserId())
                .targetPostId(entity.getTargetPostId())
                .targetCommentId(entity.getTargetCommentId())
                .createdAt(entity.getCreatedAt())
                .blocked(entity.isBlocked())
                .readStatus(entity.isReadStatus())
                .build();
    }

    public static ModeratorAction generateEntityFromDTO(ModeratorActionDTO dto) {
        return ModeratorAction.builder()
                .id(dto.getId())
                .moderatorId(dto.getModeratorId())
                .actionType(dto.getActionType())
                .targetUserId(dto.getTargetUserId())
                .targetPostId(dto.getTargetPostId())
                .targetCommentId(dto.getTargetCommentId())
                .createdAt(dto.getCreatedAt())
                .blocked(dto.isBlocked())
                .readStatus(dto.isReadStatus())
                .build();
    }

}
