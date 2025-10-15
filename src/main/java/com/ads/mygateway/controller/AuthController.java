package com.ads.mygateway.controller;

import com.ads.mygateway.exception.ApiException;
import com.ads.mygateway.logindto.*;
import com.ads.mygateway.loginentity.AppUser;
import com.ads.mygateway.loginservice.UserService;
import jakarta.validation.Valid;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Log4j2
public class AuthController {

    private final UserService userService;

    // explicit constructor
    public AuthController(UserService userService) {
        this.userService = userService;
    }


    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Object>> register(@Valid @RequestBody RegistrationRequest req) {
        AppUser user = userService.register(req);
        return ResponseEntity.ok(new ApiResponse<>(true,
                "Registered successfully. Verification email sent to " + user.getEmail(), null));
    }

//    @GetMapping("/confirm")
//    public ResponseEntity<ApiResponse<String>> confirmEmail(@RequestParam("token") String token) {
//        String result = userService.confirmToken(token);
//        return ResponseEntity.ok(new ApiResponse<>(true, result, null));
//    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Object>> login(@Valid @RequestBody LoginRequest req) {
        AppUser user = userService.login(req);
        // return user info (without password)
        user.setPassword(null);
        return ResponseEntity.ok(new ApiResponse<>(true, "Login successful", user));
    }


    @GetMapping("/confirm")
    public ResponseEntity<String> confirmEmail(@RequestParam("token") String token) {
        try {
            String result = userService.confirmToken(token);

            String successHtml = """
                        <html>
                          <head><title>Email Verified</title></head>
                          <body style="font-family:Arial;">
                            <h2>Email verified successfully 🎉</h2>
                            <p>You can now login.</p>
                          </body>
                        </html>
                    """;
            return ResponseEntity.ok(successHtml);

        } catch (ApiException e) {
            String errorHtml = """
                        <html>
                          <head><title>Verification Failed</title></head>
                          <body style="font-family:Arial; color:red;">
                            <h2>Verification failed ❌</h2>
                            <p>Invalid or expired token.</p>
                          </body>
                        </html>
                    """;
            return ResponseEntity.badRequest().body(errorHtml);
        }
    }

    @PutMapping("/edit-profile")
    public ResponseEntity<ApiResponse<AppUser>> editProfile(
            @RequestParam("email") String email,
            @Valid @RequestBody EditProfileRequest req) {
        AppUser updatedUser = userService.editProfile(email, req);
        updatedUser.setPassword(null); // hide password
        return ResponseEntity.ok(new ApiResponse<>(true, "Profile updated successfully", updatedUser));
    }

    @PutMapping("/change-password")
    public ResponseEntity<ApiResponse<Object>> changePassword(
            @RequestParam("email") String email,
            @Valid @RequestBody ChangePasswordRequest req) {
        userService.changePassword(email, req);
        return ResponseEntity.ok(new ApiResponse<>(true, "Password changed successfully", null));
    }

}
