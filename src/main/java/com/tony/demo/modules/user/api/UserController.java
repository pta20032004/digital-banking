package com.tony.demo.modules.user.api;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tony.demo.modules.user.application.UserService;
import com.tony.demo.modules.user.dto.PendingUsersResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/manager/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @GetMapping("/pending")
    public ResponseEntity<PendingUsersResponse> getPendingUsers() {
        log.info("Manager requested list of pending users");
        PendingUsersResponse response = userService.getAllPendingUsers();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/kyc/files/{fileName}")
    public ResponseEntity<byte[]> getKycFile(@PathVariable String fileName) {
        log.info("Manager requested KYC file: {}", fileName);
        byte[] fileData = userService.getKycDocumentImage(fileName);
        
        HttpHeaders headers = new HttpHeaders();
        // Assuming image format, adjust content type if necessary
        headers.setContentType(MediaType.IMAGE_JPEG);
        
        return new ResponseEntity<>(fileData, headers, HttpStatus.OK);
    }
}
