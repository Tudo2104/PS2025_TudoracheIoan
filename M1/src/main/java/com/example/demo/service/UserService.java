package com.example.demo.service;

import com.example.demo.builder.userbuilder.UserBuilder;
import com.example.demo.builder.userbuilder.UserViewBuilder;
import com.example.demo.dto.commentdto.CommentDTO;
import com.example.demo.dto.postDTO.PostDTO;
import com.example.demo.dto.userdto.UserDTO;
import com.example.demo.dto.userdto.UserViewDTO;
import com.example.demo.entity.*;
import com.example.demo.errorhandler.UserException;
import com.example.demo.repository.FriendshipRepository;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.validator.UserFieldValidator;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import javax.management.relation.RoleStatus;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

    public String verifyUser(UserDTO userDTO) throws UserException {

        Authentication authentication = authManager.authenticate(new UsernamePasswordAuthenticationToken(userDTO.getName(), userDTO.getPassword()));
        if (authentication.isAuthenticated()) {
            return jwtService.generateToken(userDTO.getName());
        } else {
            return "fail";
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
            allPosts.forEach(post -> post.setImageData(null));
            allPosts.forEach(post ->
                    post.getComments().forEach(comment ->
                            comment.setImageData(null)
                    )
            );
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
}
