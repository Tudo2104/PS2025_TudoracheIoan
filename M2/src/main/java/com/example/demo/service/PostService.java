package com.example.demo.service;

import com.example.demo.builder.postbuilder.CommentBuilder;
import com.example.demo.builder.postbuilder.CommentDTOBuilder;
import com.example.demo.builder.postbuilder.PostBuilder;
import com.example.demo.builder.postbuilder.PostDTOBuilder;
import com.example.demo.dto.commentdto.CommentDTO;
import com.example.demo.dto.moderatoractionDTO.ModeratorActionDTO;
import com.example.demo.dto.postdto.PostDTO;
import com.example.demo.dto.userdto.UserDTO;
import com.example.demo.entity.Comment;
import com.example.demo.entity.Hashtag;
import com.example.demo.entity.Post;
import com.example.demo.errorhandler.UserException;
import com.example.demo.repository.CommentRepository;
import com.example.demo.repository.HashtagRepository;
import com.example.demo.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final HashtagRepository hashtagRepository;
    private final CommentRepository commentRepository;
    private Set<Hashtag> extractHashtags(String content) {
        Set<Hashtag> hashtags = new HashSet<>();
        Matcher matcher = Pattern.compile("\\B(\\#[a-zA-Z]+\\b)(?!;)").matcher(content);

        while (matcher.find()) {
            String tag = matcher.group(1).toLowerCase();
            Hashtag hashtag;

            Optional<Hashtag> optionalHashtag = hashtagRepository.findByName(tag);
            if (optionalHashtag.isPresent()) {
                hashtag = optionalHashtag.get();
            } else {
                hashtag = new Hashtag();
                hashtag.setName(tag);
                hashtag = hashtagRepository.save(hashtag);
            }

            hashtags.add(hashtag);
        }

        return hashtags;
    }


    public PostDTO createPost(PostDTO postDTO) {
        Set<Hashtag> hashtags = extractHashtags(postDTO.getContent());
        Post post = PostBuilder.generateEntityFromDTO(postDTO, hashtags);
        Post savedPost = postRepository.save(post);

        return PostDTOBuilder.generateDTOFromEntity(savedPost);
    }

    public List<PostDTO> getAllPosts() {
        List<Post> allPosts = postRepository.findAllByOrderByCreatedAtDesc();
        return allPosts.stream()
                .map(PostDTOBuilder::generateDTOFromEntity)
                .collect(Collectors.toList());
    }

    public ResponseEntity<String> deletePost(PostDTO postDTO) {
        Optional<Post> postOptional = postRepository.findById(postDTO.getId());

        if (postOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Post not found");
        }
        Optional<Post> myPost = postRepository.findByIdAndUserId(postDTO.getId(), postDTO.getUserId());

        if (myPost.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("You can only delete your own posts");
        }
        Post post = myPost.get();
        postRepository.delete(post);
        return ResponseEntity.ok("Post deleted successfully");
    }
    public ResponseEntity<String> deletePostAdmin(ModeratorActionDTO moderatorActionDTO) {
        Optional<Post> postOptional = postRepository.findById(moderatorActionDTO.getTargetPostId());

        if (postOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Post not found");
        }
        Post post = postOptional.get();
        postRepository.delete(post);
        return ResponseEntity.ok("Post deleted successfully");
    }

    public ResponseEntity<?> editPost(PostDTO postDTO) {
        Optional<Post> postOptional = postRepository.findById(postDTO.getId());

        if (postOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Post not found");
        }
        Optional<Post> myPost = postRepository.findByIdAndUserId(postDTO.getId(), postDTO.getUserId());

        if (myPost.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("You can only edit your own posts");
        }


        if(postDTO.getContent() != null){
            Set<Hashtag> hashtags = extractHashtags(postDTO.getContent());
            myPost.get().setContent(postDTO.getContent());
            myPost.get().setHashtags(hashtags);
        }
        else if(postDTO.getDurationDays() !=null && postDTO.getDurationDays() > 0 ){
            myPost.get().setDurationDays(postDTO.getDurationDays());
        }
        else if(postDTO.getImageData() != null){
            myPost.get().setImageData(postDTO.getImageData());
            myPost.get().setImageName(postDTO.getImageName());
            myPost.get().setImageType(postDTO.getImageType());
        }


        Post savedPost = postRepository.save(myPost.get());

        return ResponseEntity.ok(PostDTOBuilder.generateDTOFromEntity(savedPost));

    }

    public String cleanContentOfHashtags(String content) {
        if (content == null) return null;
        return content.replaceAll("\\B(\\#[a-zA-Z]+\\b)(?!;)", "").trim();
    }
    public List<PostDTO> filterAllPosts(PostDTO postDTO) {
        List<Post> allPosts = new ArrayList<>();

        if (postDTO.getContent() != null && !postDTO.getContent().isBlank()) {
            Set<Hashtag> hashtags = extractHashtags(postDTO.getContent());

            if (!hashtags.isEmpty()) {
                allPosts = postRepository.findAllByHashtagsIn(hashtags);
            } else {


                List<Post> rawMatchedPosts = postRepository.findAllByContentContaining(postDTO.getContent());

                List<Post> finalFilteredPosts = rawMatchedPosts.stream()
                        .filter(post -> {
                            String cleanedPostContent = cleanContentOfHashtags(post.getContent());
                            return cleanedPostContent.toLowerCase().contains(postDTO.getContent().toLowerCase());
                        })
                        .collect(Collectors.toList());
                allPosts = finalFilteredPosts;

            }
        } else if (postDTO.getUserId() != null) {
            allPosts = postRepository.findAllByUserId(postDTO.getUserId());
        }

        return allPosts.stream()
                .map(PostDTOBuilder::generateDTOFromEntity)
                .collect(Collectors.toList());
    }

    public ResponseEntity<?> createComment(CommentDTO commentDTO) {

        Optional<Post> postOptional = postRepository.findById(commentDTO.getPostId());

        if (postOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Post not found");
        }

        Comment comment = CommentBuilder.generateEntityFromDTO(commentDTO);

        Comment savedComment = commentRepository.save(comment);

        Post post = postOptional.get();
        post.getComments().add(savedComment);
        postRepository.save(post);

        return ResponseEntity.ok(CommentDTOBuilder.generateDTOFromEntity(savedComment));
    }

    public ResponseEntity<String> deleteComment(CommentDTO commentDTO) {
        Optional<Post> postOptional = postRepository.findById(commentDTO.getPostId());
        if (postOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Post not found");
        }

        Optional<Comment> commentOptional = commentRepository.findById(commentDTO.getId());
        if (commentOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Comment not found");
        }

        Comment comment = commentOptional.get();
        if (!comment.getUserId().equals(commentDTO.getUserId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You can only delete your own comments");
        }

        if (!comment.getPost().getId().equals(commentDTO.getPostId())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Comment does not belong to the specified post");
        }


        Post post = postOptional.get();
        post.getComments().remove(comment);
        commentRepository.delete(comment);

        return ResponseEntity.ok("Comment deleted successfully");
    }


    public ResponseEntity<String> deleteCommentAdmin(CommentDTO commentDTO) {
        Optional<Post> postOptional = postRepository.findById(commentDTO.getPostId());

        if (postOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Post not found");
        }

        Optional<Comment> commentOptional = commentRepository.findById(commentDTO.getId());

        if (commentOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Comment not found");
        }

        Comment comment = commentOptional.get();

        if (!comment.getPost().getId().equals(commentDTO.getPostId())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Comment does not belong to the specified post");
        }

        Post post = postOptional.get();
        post.getComments().remove(comment);
        commentRepository.delete(comment);

        return ResponseEntity.ok("Comment deleted successfully");
    }


    public List<CommentDTO> getAllComments() {
        List<Comment> allPosts = commentRepository.findAllByOrderByCreatedAtDesc();
        return allPosts.stream()
                .map(CommentDTOBuilder::generateDTOFromEntity)
                .collect(Collectors.toList());
    }

    public ResponseEntity<CommentDTO> getCommentById(Long id) {
        Comment comment = commentRepository.findById(id).orElse(null);

        if (comment == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        CommentDTO dto = CommentDTOBuilder.generateDTOFromEntity(comment);
        return ResponseEntity.ok(dto);
    }

    public ResponseEntity<PostDTO> getPostById(Long id) {
        Post post = postRepository.findById(id).orElse(null);

        if (post == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        PostDTO dto = PostDTOBuilder.generateDTOFromEntity(post);
        return ResponseEntity.ok(dto);
    }

    public ResponseEntity<String> deleteCommentModerator(ModeratorActionDTO moderatorActionDTO) {
        Optional<Comment> commentOptional = commentRepository.findById(moderatorActionDTO.getTargetCommentId());

        if (commentOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Comment not found");
        }
        Comment comment = commentOptional.get();
        commentRepository.delete(comment);
        return ResponseEntity.ok("Comment deleted successfully");
    }
}
