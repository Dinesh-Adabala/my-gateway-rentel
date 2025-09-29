package com.ads.mygateway.loginservice;

import com.ads.mygateway.exception.ApiException;
import com.ads.mygateway.logindto.ChangePasswordRequest;
import com.ads.mygateway.logindto.EditProfileRequest;
import com.ads.mygateway.logindto.LoginRequest;
import com.ads.mygateway.logindto.RegistrationRequest;
import com.ads.mygateway.loginentity.AppUser;
import com.ads.mygateway.loginentity.VerificationToken;
import com.ads.mygateway.loginrepository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Log4j2
public class UserService {

    private final UserRepository userRepository;
    private final VerificationTokenService tokenService;
    private final EmailService emailService;

    @Value("${app.base-url}")
    private String baseUrl;

    public AppUser register(RegistrationRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new ApiException("Email already registered. Please login or use different email.");
        }

        AppUser user = AppUser.builder().firstName(req.getFirstName()).lastName(req.getLastName()).address(req.getAddress()).countryCode(req.getCountryCode()).phoneNumber(req.getPhoneNumber()).email(req.getEmail()).password(hash(req.getPassword())).profilePic(req.getProfilePic()).verified(false).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();

        user = userRepository.save(user);
        // create token & send email
        VerificationToken token = tokenService.createToken(user, 60 * 24); // 24 hours
        String verifyLink = baseUrl + "/api/auth/confirm?token=" + token.getToken();

        String html = com.ads.mygateway.util.MailTemplates.verificationEmail(user.getFirstName(), verifyLink);

        emailService.sendHtmlMessage(user.getEmail(), "Please verify your email", html);

        log.info("User registered: {} - verification email sent", user.getEmail());
        return user;
    }

    private String hash(String plain) {
        return BCrypt.hashpw(plain, BCrypt.gensalt());
    }

    public String confirmToken(String token) {
        VerificationToken vt = tokenService.findByToken(token).orElseThrow(() -> new ApiException("Invalid verification token"));

        if (vt.getExpiryDate().isBefore(LocalDateTime.now())) {
            tokenService.deleteByToken(token);
            throw new ApiException("Token expired. Please register again.");
        }

        AppUser user = vt.getUser();
        if (user.isVerified()) {
            return "Already verified.";
        }
        user.setVerified(true);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        tokenService.deleteByToken(token); // optional: remove used token
        log.info("User {} verified successfully", user.getEmail());
        return "Verified successfully.";
    }

    public AppUser login(LoginRequest req) {
        AppUser user = userRepository.findByEmail(req.getEmail()).orElseThrow(() -> new ApiException("User not found. Please register."));

        if (!user.isVerified()) {
            throw new ApiException("Email not verified. Please verify your email first.");
        }

        if (!BCrypt.checkpw(req.getPassword(), user.getPassword())) {
            throw new ApiException("Invalid credentials.");
        }

        return user;
    }

    public AppUser editProfile(String email, EditProfileRequest req) {
        AppUser user = userRepository.findByEmail(email).orElseThrow(() -> new ApiException("User not found with email: " + email));

        user.setFirstName(req.getFirstName());
        user.setLastName(req.getLastName());
        user.setAddress(req.getAddress());
        user.setCountryCode(req.getCountryCode());
        user.setPhoneNumber(req.getPhoneNumber());
        user.setProfilePic(req.getProfilePic());
        user.setAbout(req.getAbout());
        user.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    public void changePassword(String email, ChangePasswordRequest req) {
        AppUser user = userRepository.findByEmail(email).orElseThrow(() -> new ApiException("User not found with email: " + email));

        if (!BCrypt.checkpw(req.getCurrentPassword(), user.getPassword())) {
            throw new ApiException("Current password is incorrect");
        }

        if (!req.getNewPassword().equals(req.getConfirmPassword())) {
            throw new ApiException("New password and confirm password do not match");
        }

        user.setPassword(BCrypt.hashpw(req.getNewPassword(), BCrypt.gensalt()));
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);
    }
}
