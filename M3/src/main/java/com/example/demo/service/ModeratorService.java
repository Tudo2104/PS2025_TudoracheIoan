package com.example.demo.service;


import com.example.demo.builder.ModeratorActionBuilder;
import com.example.demo.dto.commentdto.CommentDTO;
import com.example.demo.dto.moderatorDTO.ModeratorDTO;
import com.example.demo.dto.moderatoractionDTO.ModeratorActionDTO;
import com.example.demo.dto.postDTO.PostDTO;
import com.example.demo.dto.userDTO.UserDTO;
import com.example.demo.entity.ModeratorAction;
import com.example.demo.repository.ModeratorActionRepository;
import com.example.demo.repository.ReactRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ModeratorService {
    private final ModeratorActionRepository moderatorActionRepository;
    @Autowired
    private WebClient.Builder webClientBuilder;
    private final ReactRepository reactRepository;
    public ResponseEntity<String> adminDeleteAction(ModeratorActionDTO moderatorActionDTO) {
        ResponseEntity<String> response = null;

        if (moderatorActionDTO.getTargetPostId() != null) {
            PostDTO post;
            try{
             post = webClientBuilder.build()
                    .get()
                    .uri("http://localhost:8082/api/post/getPostById/" + moderatorActionDTO.getTargetPostId())
                    .retrieve()
                    .bodyToMono(PostDTO.class)
                    .block();
            } catch (WebClientResponseException.NotFound e) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Post not found");
            }


            try {
                response = webClientBuilder.build()
                        .method(HttpMethod.DELETE)
                        .uri("http://localhost:8082/api/post/deletePostAdmin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(moderatorActionDTO)
                        .retrieve()
                        .toEntity(String.class)
                        .block();


            } catch (WebClientResponseException e) {
                return ResponseEntity.status(e.getStatusCode()).body("Delete failed: " + e.getResponseBodyAsString());

            }
            reactRepository.deleteByPostId(moderatorActionDTO.getTargetPostId());

            ModeratorAction action = ModeratorActionBuilder.generateEntityFromDTO(moderatorActionDTO);
            action.setTargetUserId(post.getUserId());
            action.setActionType("DELETE_POST with Id: " + moderatorActionDTO.getTargetPostId());
            moderatorActionRepository.save(action);

        }
        if (moderatorActionDTO.getTargetCommentId() != null) {
            CommentDTO comment;
            try {
                comment = webClientBuilder.build()
                        .get()
                        .uri("http://localhost:8082/api/post/getCommentById/" + moderatorActionDTO.getTargetCommentId())
                        .retrieve()
                        .bodyToMono(CommentDTO.class)
                        .block();
            } catch (WebClientResponseException e) {
                if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Comment not found");
                }
                return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
            }


            try {
                response = webClientBuilder.build()
                        .method(HttpMethod.DELETE)
                        .uri("http://localhost:8082/api/post/deleteCommentModerator")
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(moderatorActionDTO)
                        .retrieve()
                        .toEntity(String.class)
                        .block();
            } catch (WebClientResponseException e) {
                return ResponseEntity.status(e.getStatusCode()).body("Delete failed: " + e.getResponseBodyAsString());
            }
            reactRepository.deleteByCommentId(moderatorActionDTO.getTargetCommentId());

            ModeratorAction action = ModeratorActionBuilder.generateEntityFromDTO(moderatorActionDTO);
            action.setTargetUserId(comment.getUserId());
            action.setActionType("DELETE_COMMENT with Id: " + moderatorActionDTO.getTargetCommentId());
            moderatorActionRepository.save(action);

        }
        return response;
    }

    public ResponseEntity<String> adminBlockAction(ModeratorActionDTO moderatorActionDTO) {
        Optional<ModeratorAction> existingBlock = moderatorActionRepository.findByTargetUserIdAndBlocked(moderatorActionDTO.getTargetUserId(), true);

        if (existingBlock.isPresent()) {
            return ResponseEntity.badRequest().body("User is already blocked.");
        }

        moderatorActionDTO.setBlocked(true);
        moderatorActionDTO.setActionType("The account was blocked");
        ModeratorAction action = ModeratorActionBuilder.generateEntityFromDTO(moderatorActionDTO);
        moderatorActionRepository.save(action);
        return ResponseEntity.ok("User has been blocked");
    }

    public ResponseEntity<String> adminUnblockAction(ModeratorActionDTO moderatorActionDTO) {
        Optional<ModeratorAction> existingBlock = moderatorActionRepository.findByTargetUserIdAndBlocked(moderatorActionDTO.getTargetUserId(), true);

        if (existingBlock.isEmpty()) {
            return ResponseEntity.badRequest().body("User is not currently blocked.");
        }
        existingBlock.get().setBlocked(false);
        moderatorActionDTO.setBlocked(false);
        moderatorActionDTO.setActionType("The account was unblocked");
        ModeratorAction action = ModeratorActionBuilder.generateEntityFromDTO(moderatorActionDTO);
        moderatorActionRepository.save(action);
        moderatorActionRepository.save(existingBlock.get());
        return ResponseEntity.ok("User has been unblocked");
    }

    public ResponseEntity<Boolean> adminVerifyLogin(UserDTO userDTO) {
        boolean isBlocked = moderatorActionRepository.findByTargetUserIdAndBlocked(userDTO.getId(), true).isPresent();
        return ResponseEntity.ok(isBlocked);
    }
    public ResponseEntity<String> getNotification(UserDTO userDTO) {
        List<ModeratorAction> existingNotifications = moderatorActionRepository.findByTargetUserIdAndReadStatus(userDTO.getId(), false);

        if (existingNotifications.isEmpty()) {
            return ResponseEntity.ok("There are no notifications");
        }

        StringBuilder notifications = new StringBuilder();
        int count = 1;
        for (ModeratorAction action : existingNotifications) {
            notifications.append("Notification ").append(count).append(": ").append(action.getActionType()).append("\n");

            action.setReadStatus(true);
            count++;
        }

        moderatorActionRepository.saveAll(existingNotifications);
        return ResponseEntity.ok(notifications.toString());
    }

    public ResponseEntity<?> createAdmin(ModeratorDTO moderatorDTO) {

        UserDTO userDTO = moderatorDTO.getUserDTO();
        ModeratorActionDTO moderatorActionDTO = new ModeratorActionDTO();
        moderatorActionDTO.setModeratorId(moderatorDTO.getModeratorId());

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String token = request.getHeader("Authorization");

        UserDTO response;

        try {

            response = webClientBuilder.build()
                    .post()
                    .uri("http://localhost:8081/api/user/createAdmin")
                    .header("Authorization", token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(userDTO)
                    .retrieve()
                    .bodyToMono(UserDTO.class)
                    .block();

        }catch (WebClientResponseException e) {
            String errorBody = e.getResponseBodyAsString();
            return ResponseEntity.status(e.getStatusCode()).body(errorBody);
        }
        moderatorActionDTO.setActionType("The admin account was created. ");
        moderatorActionDTO.setTargetUserId(response.getId());
        ModeratorAction action = ModeratorActionBuilder.generateEntityFromDTO(moderatorActionDTO);
        moderatorActionRepository.save(action);

        return ResponseEntity.ok(response);

    }


}
