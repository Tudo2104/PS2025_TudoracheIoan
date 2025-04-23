package com.example.demo.controller;


import com.example.demo.dto.commentdto.CommentDTO;
import com.example.demo.dto.moderatoractionDTO.ModeratorActionDTO;
import com.example.demo.dto.postdto.PostDTO;
import com.example.demo.dto.userdto.UserDTO;
import com.example.demo.errorhandler.UserException;
import com.example.demo.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping(value = "/api/post")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, value = "/create")
    public ResponseEntity<PostDTO> processAddPostForm(@RequestBody(required = false) PostDTO postDTO) {
        return ResponseEntity.ok(postService.createPost(postDTO));
    }
    @RequestMapping(method = RequestMethod.GET, value = "/showPosts")
    public ResponseEntity<List<PostDTO>> processShowPostForm() {
        return ResponseEntity.ok(postService.getAllPosts());
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/delete", consumes  = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> processDeletePostForm(@RequestBody PostDTO postDTO) {
        return postService.deletePost(postDTO);
    }
    @RequestMapping(method = RequestMethod.DELETE, value = "/deletePostAdmin", consumes  = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> processDeletePostForm(@RequestBody ModeratorActionDTO moderatorActionDTO) {
        return postService.deletePostAdmin(moderatorActionDTO);
    }

    @RequestMapping(method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, value = "/editPost")
    public ResponseEntity<?> processEditPostForm(@RequestBody(required = false) PostDTO postDTO) {
        return postService.editPost(postDTO);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/filterPosts")
    public ResponseEntity<List<PostDTO>> processFilterPostForm(@RequestBody(required = false) PostDTO postDTO) {
        return ResponseEntity.ok(postService.filterAllPosts(postDTO));
    }

    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, value = "/createComment")
    public ResponseEntity<?> processAddPostForm(@RequestBody(required = false) CommentDTO commentDTO) {
        return postService.createComment(commentDTO);
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/deleteComment", consumes  = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> processDeleteCommentForm(@RequestBody CommentDTO commentDTO) {
        return postService.deleteComment(commentDTO);
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/deleteCommentAdmin", consumes  = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> processDeleteCommentAdminForm(@RequestBody CommentDTO commentDTO) {
        return postService.deleteCommentAdmin(commentDTO);
    }
    @RequestMapping(method = RequestMethod.DELETE, value = "/deleteCommentModerator", consumes  = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> processDeleteCommentAdminForm(@RequestBody ModeratorActionDTO moderatorActionDTO) {
        return postService.deleteCommentModerator(moderatorActionDTO);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/showComments")
    public ResponseEntity<List<CommentDTO>> processCommentsForm() {
        return ResponseEntity.ok(postService.getAllComments());
    }

    @RequestMapping(method = RequestMethod.GET, value = "/getCommentById/{id}")
    public ResponseEntity<CommentDTO> processGetCommentForm(@PathVariable Long id) {
        return postService.getCommentById(id);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/getPostById/{id}")
    public ResponseEntity<PostDTO> processGetPostForm(@PathVariable Long id) {
        return postService.getPostById(id);
    }



}
