package com.example.demo.controller;

import com.example.demo.dto.reactDTO.ReactDTO;
import com.example.demo.dto.reactionsummarydto.ReactionSummaryDTO;
import com.example.demo.service.ReactService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping(value = "/api/react")
@RequiredArgsConstructor
public class ReactController {

    private final ReactService reactService;
    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, value = "/createReact")
    public ResponseEntity<?> processReactForm(@RequestBody(required = false)ReactDTO reactDTO) {
        return reactService.createReact(reactDTO);
    }
    @RequestMapping(method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, value = "/modifyReact")
    public ResponseEntity<?> processUpdateReactForm(@RequestBody(required = false)ReactDTO reactDTO) {
        return reactService.modifyReact(reactDTO);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/summaryByPostId/{id}")
    public ResponseEntity<List<ReactionSummaryDTO>> getReactionSummaryByPost(@PathVariable Long id) {
        return ResponseEntity.ok(reactService.getReactionSummaryByPostId(id));
    }

    @RequestMapping(method = RequestMethod.GET, value = "/summaryByCommentId/{id}")
    public ResponseEntity<List<ReactionSummaryDTO>> getReactionSummaryByComment(@PathVariable Long id) {
        return ResponseEntity.ok(reactService.getReactionSummaryByCommentId(id));
    }

}
