package com.example.demo.dto.userDTO;


import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {

    private Long id;

    private String name;

    private String email;

    private String password;

    private String roleName;

}
