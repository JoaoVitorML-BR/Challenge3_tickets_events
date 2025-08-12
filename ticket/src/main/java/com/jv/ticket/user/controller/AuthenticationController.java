package com.jv.ticket.user.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.jv.ticket.user.dto.UserLoginDTO;
import com.jv.ticket.user.exception.ErrorMessage;
import com.jv.ticket.user.jwt.JwtToken;
import com.jv.ticket.user.jwt.JwtUserDetailsService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Tag(name = "Authentication", description = "Operations related to user authentication")
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1")
public class AuthenticationController {
    private final JwtUserDetailsService detailsService;
    private final AuthenticationManager authenticationManager;

    @Operation(summary = "Authenticate", description = " Authenticates a user and returns a JWT token", responses = {
            @ApiResponse(responseCode = "200", headers = @Header(name = HttpHeaders.LOCATION, description = "Authentication location"), description = "Authenticated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid username or password", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
            @ApiResponse(responseCode = "422", description = "Invalid input data", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class)))
    })
    @PostMapping("/auth")
    public ResponseEntity<?> Login(@RequestBody @Valid UserLoginDTO userLoginDTO, HttpServletRequest request) {
        log.info("Autenticating user with email: {}", userLoginDTO.getEmail());
        try {
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                    userLoginDTO.getEmail(), userLoginDTO.getPassword());
            authenticationManager.authenticate(authenticationToken);
            JwtToken token = detailsService.getTokenAuthenticated(userLoginDTO.getEmail());
            return ResponseEntity.ok(token);
        } catch (Exception e) {
            log.warn("Authentication failed for user: {}", userLoginDTO.getEmail());
            log.error("⚠️ Authentication error:", e);
        }

        return ResponseEntity
                .badRequest()
                .body(new ErrorMessage(request, HttpStatus.BAD_REQUEST, "Invalid email or password!"));

    }
}
