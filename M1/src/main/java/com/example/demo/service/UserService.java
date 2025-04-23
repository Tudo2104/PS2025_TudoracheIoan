package com.example.demo.service;

import com.example.demo.builder.userbuilder.UserBuilder;
import com.example.demo.builder.userbuilder.UserViewBuilder;
import com.example.demo.dto.commentdto.CommentDTO;
import com.example.demo.dto.moderatorDTO.ModeratorDTO;
import com.example.demo.dto.moderatoractionDTO.ModeratorActionDTO;
import com.example.demo.dto.postDTO.PostDTO;
import com.example.demo.dto.reactDTO.ReactDTO;
import com.example.demo.dto.reactionsummarydto.ReactionSummaryDTO;
import com.example.demo.dto.userdto.UserDTO;
import com.example.demo.dto.userdto.UserViewDTO;
import com.example.demo.entity.*;
import com.example.demo.errorhandler.UserException;
import com.example.demo.repository.FriendshipRepository;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.validator.UserFieldValidator;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService   {

    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    private final FriendshipRepository friendshipRepository;

    private final RoleRepository roleRepository;

    private final UserRepository userRepository;
    @Autowired
    private JWTService jwtService;
    @Autowired
    AuthenticationManager authManager;
    @Autowired
    private WebClient.Builder webClientBuilder;

    public List<UserViewDTO> findAllUserView() {

        return userRepository.findAll().stream()
                .map(UserViewBuilder::generateDTOFromEntity)
                .collect(Collectors.toList());
    }

    public UserViewDTO findUserViewById(Long id) throws UserException {

        Optional<User> user  = userRepository.findById(id);

        if (user.isEmpty()) {
            throw new UserException("User not found with id field: " + id);
        }
        return UserViewBuilder.generateDTOFromEntity(user.get());
    }

    public UserViewDTO findUserViewByEmail(String email) throws UserException {
        Optional<User> user  = userRepository.findUserByEmail(email);

        if (user.isEmpty()) {
            throw new UserException("User not found with email field: " + email);
        }
        return UserViewBuilder.generateDTOFromEntity(user.get());
    }

    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException{
        User user = userRepository.findUserByName(username);

        if (user == null ) {
            System.out.println("User not found");
            throw new UsernameNotFoundException("User not found");
        }
        return new UserPrincipal(user);
    }

    public Long createUser(UserDTO userDTO) throws UserException {
        List<String> errors = UserFieldValidator.validateInsertOrUpdate(userDTO);

        if(!errors.isEmpty())
        {
            throw new UserException(StringUtils.collectionToDelimitedString(errors, "\n"));
        }

        Optional<Role> role = roleRepository.findRoleByName(userDTO.getRoleName().toUpperCase());

        if (role.isEmpty()) {
            throw new UserException("Role not found with name field: " + userDTO.getRoleName().toUpperCase());
        }

        Optional<User> user = userRepository.findUserByEmail(userDTO.getEmail());
        if(user.isPresent() ){
            throw new UserException("User record does not permit duplicates for email field: " + userDTO.getEmail());
        }

        User userSave = UserBuilder.generateEntityFromDTO(userDTO, role.get());

        return userRepository.save(userSave).getId();
    }


    public Long updateUser(UserDTO userDTO) throws UserException {
        List<String> errors = UserFieldValidator.validateInsertOrUpdate(userDTO);

        if(!errors.isEmpty())
        {
            throw new UserException(StringUtils.collectionToDelimitedString(errors, "\n"));
        }

        Optional<Role> role = roleRepository.findRoleByName(userDTO.getRoleName().toUpperCase());

        if (role.isEmpty()) {
            throw new UserException("Role not found with name field: " + userDTO.getRoleName().toUpperCase());
        }

        Optional<User> user = userRepository.findById(userDTO.getId());
        if(user.isEmpty()){
            throw new UserException("User not found with id field: " + userDTO.getId());
        }


        if(!user.get().getEmail().equals(userDTO.getEmail()))
        {
            Optional<User> verifyDuplicated = userRepository.findUserByEmail(userDTO.getEmail());
            if(verifyDuplicated.isPresent() ){
                throw new UserException("User record does not permit duplicates for email field: " + userDTO.getEmail());
            }
        }

        user.get().setName(userDTO.getName());
        user.get().setEmail(userDTO.getEmail());
        user.get().setPassword(userDTO.getPassword());
        user.get().setRole(role.get());

        return userRepository.save(user.get()).getId();
    }

    public void deleteUser(Long id) throws UserException {

        Optional<User> user = userRepository.findById(id);

        if (user.isEmpty()) {
            throw new UserException("User not found with id field: " + id);
        }

        this.userRepository.deleteById(id);
    }

    public List<UserViewDTO> findUserViewByRoleName(String roleName) throws UserException {
        List<User> userList  = userRepository.findUserByRoleName(roleName);

        if (userList.isEmpty()) {
            throw new UserException("User not found with role name field: " + roleName);
        }
        return  userList.stream()
                .map(UserViewBuilder::generateDTOFromEntity)
                .collect(Collectors.toList());
    }

    public Long registerUser(UserDTO userDTO) throws UserException {
        List<String> errors = UserFieldValidator.validateInsertOrUpdate(userDTO);

        if(!errors.isEmpty())
        {
            throw new UserException(StringUtils.collectionToDelimitedString(errors, "\n"));
        }

        Optional<Role> role = roleRepository.findRoleByName(userDTO.getRoleName().toUpperCase());

        if (role.isEmpty()) {
            throw new UserException("Role not found with name field: " + userDTO.getRoleName().toUpperCase());
        }

        Optional<User> user = userRepository.findUserByEmail(userDTO.getEmail());
        if(user.isPresent() ){
            throw new UserException("User record does not permit duplicates for email field: " + userDTO.getEmail());
        }

        User userSave = UserBuilder.generateEntityFromDTO(userDTO, role.get());
        userSave.setPassword(encoder.encode(userSave.getPassword()));

        return userRepository.save(userSave).getId();
    }

    public ResponseEntity<?> registerAdmin(UserDTO userDTO) {

        if (userDTO.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Username must be written");
        }

        if (userDTO.getEmail() == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Email must be written");
        }

        if (userDTO.getPassword() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Password must be written");
        }

        Optional<Role> role = roleRepository.findRoleByName("Admin");

        if (role.isEmpty()) {

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Role not found with name field: Admin");
        }

        Optional<User> user = userRepository.findUserByEmail(userDTO.getEmail());
        if(user.isPresent() ){

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User record does not permit duplicates for email field: " + userDTO.getEmail());
        }

        User userSave = UserBuilder.generateEntityFromDTO(userDTO, role.get());
        userSave.setPassword(encoder.encode(userSave.getPassword()));
        userRepository.save(userSave);

        return ResponseEntity.status(HttpStatus.OK).body(UserBuilder.generateDTOFromEntity(userSave));

    }

    public String verifyUser(UserDTO userDTO) throws UserException {
        try {
            authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(userDTO.getName(), userDTO.getPassword())
            );

            User user = userRepository.findUserByName(userDTO.getName());
            userDTO.setId(user.getId());

            Boolean isBlocked = webClientBuilder.build()
                    .post()
                    .uri("http://localhost:8083/api/mod/verifyUser")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(userDTO)
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .block();

            if (Boolean.TRUE.equals(isBlocked)) {
                return "Account is blocked";
            }
            String notification = webClientBuilder.build()
                    .post()
                    .uri("http://localhost:8083/api/mod/getNotificationUser")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(userDTO)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return "User logged in : " + jwtService.generateToken(userDTO.getName()) + "\n" + notification;

        } catch (BadCredentialsException ex) {
            return "Wrong credentials";
        } catch (WebClientResponseException e) {
            throw new UserException("Moderator check failed: " + e.getResponseBodyAsString());
        }
    }


    public ResponseEntity<?> updateStatus(UserDTO userDTO) {

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof UserDetails)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User must be authenticated!");
        }

        String username = ((UserDetails) principal).getUsername();
        User currentUser = userRepository.findUserByName(username);

        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found!");
        }


        if (currentUser.getStatus() == userDTO.getStatus()) {
            return ResponseEntity.badRequest().body("Invalid status: Status cannot be the same.");
        }
        if (!userDTO.getStatus().equals(PostStatus.FRIENDS) && !userDTO.getStatus().equals(PostStatus.PUBLIC)) {
            return ResponseEntity.badRequest().body("Invalid status: Status doesn't exist.");
        }

        currentUser.setStatus(userDTO.getStatus());
        userRepository.save(currentUser);

        return ResponseEntity.ok("Status updated successfully!");
    }

    public ResponseEntity<?> showStatus(UserDTO userDTO) {

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof UserDetails)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User must be authenticated!");
        }

        String username = ((UserDetails) principal).getUsername();
        User currentUser = userRepository.findUserByName(username);

        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found!");
        }


        return ResponseEntity.ok("Post Status for "+currentUser.getName()+" is "+currentUser.getStatus());
    }
    public ResponseEntity<?> showRole(UserDTO userDTO) {

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof UserDetails)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User must be authenticated!");
        }

        String username = ((UserDetails) principal).getUsername();
        User currentUser = userRepository.findUserByName(username);

        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found!");
        }

        return ResponseEntity.ok("Role Status for "+currentUser.getName()+" is "+currentUser.getRole().getName());
    }

    public ResponseEntity<?> updateRole(Map<String, String> request) {

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof UserDetails)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User must be authenticated!");
        }

        String username = ((UserDetails) principal).getUsername();
        User currentUser = userRepository.findUserByName(username);

        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found!");
        }

        if (!currentUser.getRole().getName().equals("Admin")) {
            return ResponseEntity.ok("Cannot modify role as a user!!!!!!");
        }

        String user = request.get("name");
        if (user == null) {
            return ResponseEntity.badRequest().body("User's name is required!");
        }

        User userChange = userRepository.findUserByName(user);
        if (userChange == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User to be updated not found!");
        }

        String roleName = request.get("role");
        if (roleName == null) {
            return ResponseEntity.badRequest().body("Role's name is required!");
        }

        if (!roleName.equals("Admin") && !roleName.equals("User")) {
            return ResponseEntity.badRequest().body("Role's name doesn't exist!");
        }

        Optional<Role> role = roleRepository.findRoleByName(roleName.toUpperCase());
        if (role.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Role not found in database!");
        }

        userChange.setRole(role.get());
        userRepository.save(userChange);

        return ResponseEntity.ok("The role has been modified!!!!!!");
    }

    public ResponseEntity<?> processPost(PostDTO postDTO, MultipartFile imageFile) {

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof UserDetails)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User must be authenticated!");
        }
        String username = ((UserDetails) principal).getUsername();
        User currentUser = userRepository.findUserByName(username);
        try {
            if (imageFile != null && !imageFile.isEmpty()) {
                postDTO.setImageData(imageFile.getBytes());
                postDTO.setImageName(imageFile.getOriginalFilename());
                postDTO.setImageType(imageFile.getContentType());
            } else {
                postDTO.setImageData(null);
                postDTO.setImageName(null);
                postDTO.setImageType(null);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error processing image file", e);
        }
        postDTO.setUserId(currentUser.getId());
        postDTO.setCreatedAt(LocalDateTime.now());


        PostDTO createdPost = webClientBuilder.build()
                .post()
                .uri("http://localhost:8082/api/post/create")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(postDTO)
                .retrieve()
                .bodyToMono(PostDTO.class)
                .block();
        createdPost.setImageData(null);

        return ResponseEntity.ok(createdPost);
    }

    public ResponseEntity<?> showPost() {

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof UserDetails)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User must be authenticated!");
        }

        String username = ((UserDetails) principal).getUsername();
        User currentUser = userRepository.findUserByName(username);

        List<PostDTO> allPosts = webClientBuilder.build()
                .get()
                .uri("http://localhost:8082/api/post/showPosts")
                .retrieve()
                .bodyToFlux(PostDTO.class)
                .collectList()
                .block();

        Iterator<PostDTO> postsAvailable = allPosts.iterator();
        while (postsAvailable.hasNext()) {
            PostDTO post = postsAvailable.next();
            Long userId = post.getUserId();
            User friendUser = userRepository.findUserById(userId);
            if (!friendshipRepository.existsByUserAndFriend(currentUser, friendUser)
                    && friendUser.getStatus().equals(PostStatus.FRIENDS)
                    && !friendUser.getId().equals(currentUser.getId())) {
                postsAvailable.remove();
            }
        }

        if (allPosts != null) {
            for (PostDTO post : allPosts) {
                post.setImageData(null);


                List<ReactionSummaryDTO> postReactions = webClientBuilder.build()
                        .get()
                        .uri("http://localhost:8083/api/react/summaryByPostId/" + post.getId())
                        .retrieve()
                        .bodyToFlux(ReactionSummaryDTO.class)
                        .collectList()
                        .block();

                post.setReactionSummary(postReactions);


                for (CommentDTO comment : post.getComments()) {
                    comment.setImageData(null);

                    List<ReactionSummaryDTO> commentReactions = webClientBuilder.build()
                            .get()
                            .uri("http://localhost:8083/api/react/summaryByCommentId/" + comment.getId())
                            .retrieve()
                            .bodyToFlux(ReactionSummaryDTO.class)
                            .collectList()
                            .block();
                    long totalReacts = 0;
                    for (ReactionSummaryDTO react : commentReactions) {
                        totalReacts += react.getCount();
                    }

                    comment.setTotalReactions(totalReacts);
                    comment.setReactionSummary(commentReactions);
                }
                if (post.getComments() != null && !post.getComments().isEmpty()) {

                    List<CommentDTO> sortedComments = new ArrayList<>(post.getComments());

                    sortedComments.sort((c1, c2) -> Long.compare(c2.getTotalReactions(), c1.getTotalReactions()));

                    Set<CommentDTO> sortedSet = new LinkedHashSet<>(sortedComments);

                    post.setComments(sortedSet);
                }

            }
        }

        return ResponseEntity.ok(allPosts);
    }

    public ResponseEntity<String> deletePost(Long postID) {

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof UserDetails)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User must be authenticated!");
        }

        String username = ((UserDetails) principal).getUsername();
        User currentUser = userRepository.findUserByName(username);

        PostDTO postDTO = new PostDTO();
        postDTO.setId(postID);
        postDTO.setUserId(currentUser.getId());

        try {
            ResponseEntity<String> response = webClientBuilder.build()
                    .method(HttpMethod.DELETE)
                    .uri("http://localhost:8082/api/post/delete")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(postDTO)
                    .retrieve()
                    .toEntity(String.class)
                    .block();

            return response;

        } catch (WebClientResponseException e) {
            String errorBody = e.getResponseBodyAsString();
            return ResponseEntity.status(e.getStatusCode()).body(errorBody);
        }

    }

    public ResponseEntity<?> editPost(PostDTO postDTO, MultipartFile imageFile) {

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof UserDetails)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User must be authenticated!");
        }
        String username = ((UserDetails) principal).getUsername();
        User currentUser = userRepository.findUserByName(username);
        try {
            if (imageFile != null && !imageFile.isEmpty()) {
                postDTO.setImageData(imageFile.getBytes());
                postDTO.setImageName(imageFile.getOriginalFilename());
                postDTO.setImageType(imageFile.getContentType());
            } else {
                postDTO.setImageData(null);
                postDTO.setImageName(null);
                postDTO.setImageType(null);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error processing image file", e);
        }
        postDTO.setUserId(currentUser.getId());

        try {
            ResponseEntity<PostDTO> response = webClientBuilder.build()
                    .put()
                    .uri("http://localhost:8082/api/post/editPost")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(postDTO)
                    .retrieve()
                    .toEntity(PostDTO.class)
                    .block();

            PostDTO editedPost = response.getBody();
            if (editedPost != null) {
                editedPost.setImageData(null);
            }

            return ResponseEntity.ok(editedPost);

        } catch (WebClientResponseException e) {
            String errorBody = e.getResponseBodyAsString();
            return ResponseEntity.status(e.getStatusCode()).body(errorBody);
        }

    }

    public ResponseEntity<?> filterPost(PostDTO postDTO) {

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof UserDetails)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User must be authenticated!");
        }

        String username = ((UserDetails) principal).getUsername();
        User currentUser = userRepository.findUserByName(username);

        List<PostDTO> allPosts = webClientBuilder.build()
                .post()
                .uri("http://localhost:8082/api/post/filterPosts")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(postDTO)
                .retrieve()
                .bodyToFlux(PostDTO.class)
                .collectList()
                .block();

        Iterator<PostDTO> postsAvailable = allPosts.iterator();
        while (postsAvailable.hasNext()) {
            PostDTO post = postsAvailable.next();
            Long userId = post.getUserId();
            User friendUser = userRepository.findUserById(userId);
            if (!friendshipRepository.existsByUserAndFriend(currentUser, friendUser)
                    && friendUser.getStatus().equals(PostStatus.FRIENDS)
                    && !friendUser.getId().equals(currentUser.getId())) {
                postsAvailable.remove();
            }
        }

        if (allPosts != null) {
            allPosts.forEach(post -> post.setImageData(null));
            allPosts.forEach(post ->
                    post.getComments().forEach(comment ->
                            comment.setImageData(null)
                    )
            );
        }


        return ResponseEntity.ok(allPosts);
    }

    public ResponseEntity<?> processComment(CommentDTO commentDTO, MultipartFile imageFile) {

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof UserDetails)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User must be authenticated!");
        }
        String username = ((UserDetails) principal).getUsername();
        User currentUser = userRepository.findUserByName(username);
        try {
            if (imageFile != null && !imageFile.isEmpty()) {
                commentDTO.setImageData(imageFile.getBytes());
                commentDTO.setImageName(imageFile.getOriginalFilename());
                commentDTO.setImageType(imageFile.getContentType());
            } else {
                commentDTO.setImageData(null);
                commentDTO.setImageName(null);
                commentDTO.setImageType(null);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error processing image file", e);
        }
        commentDTO.setUserId(currentUser.getId());
        commentDTO.setCreatedAt(LocalDateTime.now());
        commentDTO.setUsername(currentUser.getName());

        List<PostDTO> allPosts = webClientBuilder.build()
                .get()
                .uri("http://localhost:8082/api/post/showPosts")
                .retrieve()
                .bodyToFlux(PostDTO.class)
                .collectList()
                .block();

        Iterator<PostDTO> postsAvailable = allPosts.iterator();
        while (postsAvailable.hasNext()) {
            PostDTO post = postsAvailable.next();
            if(post.getId() == commentDTO.getPostId()){
                Long userId = post.getUserId();
                User friendUser = userRepository.findUserById(userId);
                if (!friendshipRepository.existsByUserAndFriend(currentUser, friendUser)
                        && friendUser.getStatus().equals(PostStatus.FRIENDS)
                        && !friendUser.getId().equals(currentUser.getId())) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Must be friends to comment");
                }
            }
        }

        try{
            CommentDTO createdComment = webClientBuilder.build()
                    .post()
                    .uri("http://localhost:8082/api/post/createComment")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(commentDTO)
                    .retrieve()
                    .bodyToMono(CommentDTO.class)
                    .block();

            createdComment.setImageData(null);

            return ResponseEntity.ok(createdComment);
        } catch (WebClientResponseException e) {
            String errorBody = e.getResponseBodyAsString();
            return ResponseEntity.status(e.getStatusCode()).body(errorBody);
        }
    }
    public ResponseEntity<String> deleteComment(CommentDTO commentDTO) {

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof UserDetails)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User must be authenticated!");
        }

        String username = ((UserDetails) principal).getUsername();
        User currentUser = userRepository.findUserByName(username);

        commentDTO.setUserId(currentUser.getId());

        String endpoint = (currentUser.getRole().getId() == 1)
                ? "http://localhost:8082/api/post/deleteCommentAdmin"
                : "http://localhost:8082/api/post/deleteComment";

        try {
            ResponseEntity<String> response = webClientBuilder.build()
                    .method(HttpMethod.DELETE)
                    .uri(endpoint)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(commentDTO)
                    .retrieve()
                    .toEntity(String.class)
                    .block();

            return response;

        } catch (WebClientResponseException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        }
    }

    public ResponseEntity<?> processReact(ReactDTO reactDto) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof UserDetails)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User must be authenticated!");
        }

        String username = ((UserDetails) principal).getUsername();
        User currentUser = userRepository.findUserByName(username);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found!");
        }
        if (reactDto.getType() == null) {
            return ResponseEntity.badRequest().body("Reaction type must be provided");
        }

        if (reactDto.getPostId() != null && reactDto.getCommentId() != null) {
            return ResponseEntity.badRequest().body("Cannot react to both a post and a comment at the same time");
        }

        if (reactDto.getCommentId() != null) {
            reactDto.setPostId(null);
        }

        if (reactDto.getPostId() != null) {
            reactDto.setCommentId(null);
        }


        reactDto.setUserId(currentUser.getId());

        //Post
        if (reactDto.getPostId() != null) {
            PostDTO post;
            try {
                post = webClientBuilder.build()
                        .get()
                        .uri("http://localhost:8082/api/post/getPostById/" + reactDto.getPostId())
                        .retrieve()
                        .bodyToMono(PostDTO.class)
                        .block();
            } catch (WebClientResponseException.NotFound e) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Post not found");
            }


            Long authorId = post.getUserId();
            User postAuthor = userRepository.findUserById(authorId);

            if (!authorId.equals(currentUser.getId()) &&
                    postAuthor.getStatus().equals(PostStatus.FRIENDS) &&
                    !friendshipRepository.existsByUserAndFriend(currentUser, postAuthor)) {


                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Must be friends to react to this post");
            }
        }

        //Comment
        if (reactDto.getCommentId() != null) {
            CommentDTO comment;
            try{
            comment = webClientBuilder.build()
                    .get()
                    .uri("http://localhost:8082/api/post/getCommentById/" + reactDto.getCommentId())
                    .retrieve()
                    .bodyToMono(CommentDTO.class)
                    .block();
            } catch (WebClientResponseException.NotFound e) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Comment not found");
            }


            PostDTO parentPost;
            try {
                parentPost = webClientBuilder.build()
                        .get()
                        .uri("http://localhost:8082/api/post/getPostById/" + comment.getPostId())
                        .retrieve()
                        .bodyToMono(PostDTO.class)
                        .block();
            } catch (WebClientResponseException.NotFound e) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Parent post not found");
            }


            Long postAuthorId = parentPost.getUserId();
            User postAuthor = userRepository.findUserById(postAuthorId);

            if (!postAuthorId.equals(currentUser.getId()) &&
                    postAuthor.getStatus().equals(PostStatus.FRIENDS) &&
                    !friendshipRepository.existsByUserAndFriend(currentUser, postAuthor)) {

                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Must be friends to react to this comment");
            }
        }

        try {
            ReactDTO createdReact = webClientBuilder.build()
                    .post()
                    .uri("http://localhost:8083/api/react/createReact")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(reactDto)
                    .retrieve()
                    .bodyToMono(ReactDTO.class)
                    .block();

            return ResponseEntity.ok(createdReact);
        } catch (WebClientResponseException e) {
            String errorBody = e.getResponseBodyAsString();
            return ResponseEntity.status(e.getStatusCode()).body(errorBody);
        }

    }

    public ResponseEntity<?> modifyReact(ReactDTO reactDto) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof UserDetails)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User must be authenticated!");
        }

        String username = ((UserDetails) principal).getUsername();
        User currentUser = userRepository.findUserByName(username);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found!");
        }
        if (reactDto.getType() == null) {
            return ResponseEntity.badRequest().body("Reaction type must be provided");
        }
        if (reactDto.getId() == null) {
            return ResponseEntity.badRequest().body("React Id must be provided");
        }

        if (reactDto.getCommentId() != null) {
            return ResponseEntity.badRequest().body("Comment Id must not be provided");
        }

        if (reactDto.getPostId() != null) {
            return ResponseEntity.badRequest().body("Comment Id must not be provided");
        }
        reactDto.setUserId(currentUser.getId());

        try {
            ReactDTO createdReact = webClientBuilder.build()
                    .put()
                    .uri("http://localhost:8083/api/react/modifyReact")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(reactDto)
                    .retrieve()
                    .bodyToMono(ReactDTO.class)
                    .block();

            return ResponseEntity.ok(createdReact);
        } catch (WebClientResponseException e) {
            String errorBody = e.getResponseBodyAsString();
            return ResponseEntity.status(e.getStatusCode()).body(errorBody);
        }

    }

    public ResponseEntity<String> adminDeleteAction(ModeratorActionDTO moderatorActionDTO) {

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof UserDetails)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User must be authenticated!");
        }

        String username = ((UserDetails) principal).getUsername();
        User currentUser = userRepository.findUserByName(username);
        if (currentUser.getRole().getId() != 1) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Must be an admin to delete posts with this action.");
        }

        moderatorActionDTO.setModeratorId(currentUser.getId());
        if(moderatorActionDTO.getTargetUserId() != null){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Method for deleting posts or comments, not blocking users.");
        }

        if(moderatorActionDTO.getTargetCommentId() != null &&moderatorActionDTO.getTargetPostId() != null ){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Cannot perform both deleting in the same time introduce them one by one.");
        }

        try {
            ResponseEntity<String> response = webClientBuilder.build()
                    .method(HttpMethod.DELETE)
                    .uri("http://localhost:8083/api/mod/deleteAdmin")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(moderatorActionDTO)
                    .retrieve()
                    .toEntity(String.class)
                    .block();

            return response;

        } catch (WebClientResponseException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        }
    }

    public ResponseEntity<String> adminBlockAction(ModeratorActionDTO moderatorActionDTO) {

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof UserDetails)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User must be authenticated!");
        }

        String username = ((UserDetails) principal).getUsername();
        User currentUser = userRepository.findUserByName(username);
        if (currentUser.getRole().getId() != 1) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Must be an admin to block.");
        }

        moderatorActionDTO.setModeratorId(currentUser.getId());

        if(moderatorActionDTO.getTargetCommentId() != null || moderatorActionDTO.getTargetPostId()!= null){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Method for blocking users,not deleting posts or comments.");
        }
        if(moderatorActionDTO.getTargetUserId() == null){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Add an user Id to block/unblock.");
        }
        Optional<User> targetUser = userRepository.findById(moderatorActionDTO.getTargetUserId());
        if(targetUser.isEmpty()){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("The user with this Id doesn't exist.");
        }

        try {
            ResponseEntity<String> response = webClientBuilder.build()
                    .method(HttpMethod.PUT)
                    .uri("http://localhost:8083/api/mod/blockUser")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(moderatorActionDTO)
                    .retrieve()
                    .toEntity(String.class)
                    .block();

            return response;

        } catch (WebClientResponseException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        }
    }

    public ResponseEntity<String> adminUnblockAction(ModeratorActionDTO moderatorActionDTO) {

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof UserDetails)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User must be authenticated!");
        }

        String username = ((UserDetails) principal).getUsername();
        User currentUser = userRepository.findUserByName(username);
        if (currentUser.getRole().getId() != 1) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Must be an admin to unblock.");
        }

        moderatorActionDTO.setModeratorId(currentUser.getId());

        if(moderatorActionDTO.getTargetCommentId() != null || moderatorActionDTO.getTargetPostId()!= null){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Method for unblocking users,not deleting posts or comments.");
        }
        if(moderatorActionDTO.getTargetUserId() == null){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Add an user Id to unblock.");
        }
        Optional<User> targetUser = userRepository.findById(moderatorActionDTO.getTargetUserId());
        if(targetUser.isEmpty()){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("The user with this Id doesn't exist.");
        }

        try {
            ResponseEntity<String> response = webClientBuilder.build()
                    .method(HttpMethod.PUT)
                    .uri("http://localhost:8083/api/mod/unblockUser")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(moderatorActionDTO)
                    .retrieve()
                    .toEntity(String.class)
                    .block();

            return response;

        } catch (WebClientResponseException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        }
    }

    public ResponseEntity<?> processRegisterAdmin(UserDTO userDTO) {

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof UserDetails)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User must be authenticated!");
        }

        String username = ((UserDetails) principal).getUsername();
        User currentUser = userRepository.findUserByName(username);
        if (currentUser.getRole().getId() != 1) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Must be an admin to create an admin account.");
        }
        ModeratorDTO moderatorDTO = new ModeratorDTO();
        moderatorDTO.setModeratorId(currentUser.getId());
        moderatorDTO.setUserDTO(userDTO);

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String token = request.getHeader("Authorization");

        if (token == null || token.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authorization token is missing");
        }


        try {
            UserDTO response = webClientBuilder.build()
                    .method(HttpMethod.POST)
                    .uri("http://localhost:8083/api/mod/createAdmin")
                    .header("Authorization", token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(moderatorDTO)
                    .retrieve()
                    .bodyToMono(UserDTO.class)
                    .block();

            return ResponseEntity.ok(response);

        }catch (WebClientResponseException e) {
            String errorBody = e.getResponseBodyAsString();
            return ResponseEntity.status(e.getStatusCode()).body(errorBody);
        }

    }

}
