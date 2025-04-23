package com.example.demo.controller;

import com.example.demo.dto.moderatorDTO.ModeratorDTO;
import com.example.demo.dto.moderatoractionDTO.ModeratorActionDTO;
import com.example.demo.dto.userDTO.UserDTO;
import com.example.demo.errorhandler.UserException;
import com.example.demo.service.ModeratorService;
import com.example.demo.service.ReactService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequestMapping(value = "/api/mod")
@RequiredArgsConstructor
public class ModeratorController {

    private final ModeratorService moderatorService;

    @RequestMapping(method = RequestMethod.DELETE, value = "/deleteAdmin", consumes  = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> processAdminForm(@RequestBody ModeratorActionDTO moderatorActionDTO) {
        return moderatorService.adminDeleteAction(moderatorActionDTO);
    }
    @RequestMapping(method = RequestMethod.PUT, value = "/blockUser", consumes  = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> processAdminBlockForm(@RequestBody ModeratorActionDTO moderatorActionDTO) {
        return moderatorService.adminBlockAction(moderatorActionDTO);
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/unblockUser", consumes  = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> processAdminUnblockForm(@RequestBody ModeratorActionDTO moderatorActionDTO) {
        return moderatorService.adminUnblockAction(moderatorActionDTO);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/verifyUser", consumes  = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Boolean> processAdminVerifyForm(@RequestBody UserDTO userDTO) {
        return moderatorService.adminVerifyLogin(userDTO);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/getNotificationUser", consumes  = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> processGetMesForm(@RequestBody UserDTO userDTO) {
        return moderatorService.getNotification(userDTO);
    }

    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, value = "/createAdmin")
    public ResponseEntity<?> processCreateAdminForm(@RequestBody(required = false)ModeratorDTO moderatorDTO){
        return moderatorService.createAdmin(moderatorDTO);
    }


}
