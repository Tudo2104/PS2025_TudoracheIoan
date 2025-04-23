package com.example.demo.dto.reactDTO;

import com.example.demo.entity.ReactionType;
import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReactDTO {

    private Long id;
    private Long userId;
    private Long postId;
    private Long commentId;
    private String type;
}
