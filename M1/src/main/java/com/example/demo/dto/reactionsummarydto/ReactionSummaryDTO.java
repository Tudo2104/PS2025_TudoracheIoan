package com.example.demo.dto.reactionsummarydto;

import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReactionSummaryDTO {
    private String type;
    private Long count;
}
