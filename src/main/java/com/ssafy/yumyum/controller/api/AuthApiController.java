package com.ssafy.yumyum.controller.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.yumyum.dto.auth.AuthResponse;
import com.ssafy.yumyum.dto.auth.LoginRequest;
import com.ssafy.yumyum.dto.auth.SignupRequest;
import com.ssafy.yumyum.dto.common.MessageResponse;
import com.ssafy.yumyum.exception.CustomException;
import com.ssafy.yumyum.model.User;
import com.ssafy.yumyum.service.AuthService;
import com.ssafy.yumyum.util.ServiceResult;
import com.ssafy.yumyum.util.SessionUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Auth API", description = "Session authentication API. Demo: demo@yamyam.com / Demo1234!")
public class AuthApiController {

    private final AuthService authService;

    public AuthApiController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(
            summary = "Login",
            description = "Creates an HTTP session. In Swagger UI, execute this request with the demo account first.",
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = LoginRequest.class),
                            examples = @ExampleObject(
                                    name = "Demo account",
                                    value = """
                                            {
                                              "email": "demo@yamyam.com",
                                              "password": "Demo1234!"
                                            }
                                            """
                            )
                    )
            )
    )
    public ResponseEntity<AuthResponse> login(
            @org.springframework.web.bind.annotation.RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        ServiceResult<User> result = authService.login(request.email(), request.password());
        if (!result.isOk()) {
            throw new CustomException(HttpStatus.UNAUTHORIZED.value(), result.getMessage());
        }

        SessionUtils.login(httpRequest.getSession(), result.getData().getId());
        return ResponseEntity.ok(AuthResponse.of(result.getMessage(), result.getData()));
    }

    @PostMapping("/signup")
    @Operation(summary = "Signup", description = "Registers a user and creates a session immediately.")
    public ResponseEntity<AuthResponse> signup(
            @org.springframework.web.bind.annotation.RequestBody SignupRequest request,
            HttpServletRequest httpRequest) {
        ServiceResult<User> result = authService.register(
                request.email(),
                request.password(),
                request.nickname(),
                request.gender(),
                request.birthYear() == null ? 1998 : request.birthYear(),
                request.height() == null ? 165.0 : request.height(),
                request.weight() == null ? 60.0 : request.weight(),
                request.goal(),
                request.healthNote()
        );
        if (!result.isOk()) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(), result.getMessage());
        }

        SessionUtils.login(httpRequest.getSession(), result.getData().getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(AuthResponse.of(result.getMessage(), result.getData()));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout", description = "Invalidates the current session.")
    public ResponseEntity<MessageResponse> logout(HttpServletRequest request) {
        SessionUtils.logout(request.getSession(false));
        return ResponseEntity.ok(new MessageResponse("Logged out."));
    }
}
