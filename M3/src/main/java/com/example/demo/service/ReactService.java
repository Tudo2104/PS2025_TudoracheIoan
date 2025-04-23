package com.example.demo.service;

import com.example.demo.builder.ReactBuilder;
import com.example.demo.builder.ReactDtoBuilder;
import com.example.demo.dto.reactDTO.ReactDTO;
import com.example.demo.dto.reactionsummarydto.ReactionSummaryDTO;
import com.example.demo.entity.React;
import com.example.demo.entity.ReactionType;
import com.example.demo.repository.ReactRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ReactService {

    private final ReactRepository reactRepository;
    private boolean isValidReactionType(String type) {
        return Arrays.stream(ReactionType.values())
                .anyMatch(valid -> valid.name().equalsIgnoreCase(type));
    }

    public ResponseEntity<?> createReact(ReactDTO reactDTO) {

        if (!isValidReactionType(reactDTO.getType())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Invalid reaction type: " + reactDTO.getType());
        }

        ReactionType reactionType = ReactionType.valueOf(reactDTO.getType().toUpperCase());


        if (reactDTO.getPostId() != null) {
            boolean alreadyReactedToPost = reactRepository.existsByUserIdAndPostId(
                    reactDTO.getUserId(), reactDTO.getPostId());

            if (alreadyReactedToPost) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("User has already reacted to this post.");
            }
        }

        if (reactDTO.getCommentId() != null) {
            boolean alreadyReactedToComment = reactRepository.existsByUserIdAndCommentId(
                    reactDTO.getUserId(), reactDTO.getCommentId());

            if (alreadyReactedToComment) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("User has already reacted to this comment.");
            }
        }

        React react = ReactBuilder.generateEntityFromDTO(reactDTO,reactionType);
        React savedReact = reactRepository.save(react);

        return ResponseEntity.ok(ReactDtoBuilder.generateDTOFromEntity(savedReact));
    }

    public ResponseEntity<?> modifyReact(ReactDTO reactDTO) {
        if (!isValidReactionType(reactDTO.getType())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Invalid reaction type: " + reactDTO.getType());
        }

        ReactionType reactionType = ReactionType.valueOf(reactDTO.getType().toUpperCase());

        Long reactId = reactDTO.getId();
        if (reactId == null) {
            return ResponseEntity.badRequest().body("Reaction ID is required to modify a reaction.");
        }

        Optional<React> optionalReact = reactRepository.findById(reactId);

        if (optionalReact.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Reaction Id was not found");
        }

        React react = optionalReact.get();

        if (!react.getUserId().equals(reactDTO.getUserId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You can only modify your own reaction.");
        }

        react.setType(reactionType);
        React updated = reactRepository.save(react);

        return ResponseEntity.ok(ReactDtoBuilder.generateDTOFromEntity(updated));
    }

    public List<ReactionSummaryDTO> getReactionSummaryByPostId(Long postId) {
        List<Object[]> rawData = reactRepository.countReactionsByTypeForPost(postId);
        List<ReactionSummaryDTO> summaryList = new ArrayList<>();

        for (Object[] obj : rawData) {
            ReactionType type = (ReactionType) obj[0];
            Long count = (Long) obj[1];

            ReactionSummaryDTO dto = new ReactionSummaryDTO(type.name(), count);
            summaryList.add(dto);
        }

        return summaryList;
    }

    public List<ReactionSummaryDTO> getReactionSummaryByCommentId(Long commentId) {
        List<Object[]> rawData = reactRepository.countReactionsByTypeForComment(commentId);
        List<ReactionSummaryDTO> summaryList = new ArrayList<>();

        for (Object[] obj : rawData) {
            ReactionType type = (ReactionType) obj[0];
            Long count = (Long) obj[1];

            ReactionSummaryDTO dto = new ReactionSummaryDTO(type.name(), count);
            summaryList.add(dto);
        }

        return summaryList;
    }


}
