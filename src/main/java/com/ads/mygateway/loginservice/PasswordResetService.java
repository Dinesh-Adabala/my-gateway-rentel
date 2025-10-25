package com.ads.mygateway.loginservice;


import com.ads.mygateway.loginentity.AppUser;
import com.ads.mygateway.loginentity.PasswordResetToken;
import com.ads.mygateway.loginrepository.PasswordResetTokenRepository;
import com.ads.mygateway.loginrepository.UserRepository;
import com.ads.mygateway.util.MailTemplates;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.bcrypt.BCrypt;
import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final PasswordResetTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    private static final int OTP_EXPIRY_MINUTES = 15;

    /**
     * Create OTP, save token and send email. If a token already exists, overwrite it.
     */
    @Transactional
    public void createAndSendOtp(String email) {
        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("No user registered with email: " + email));

        String otp = generateOtp();
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES);

        // remove any existing token for this email
        tokenRepository.deleteByEmail(email);

        PasswordResetToken token = new PasswordResetToken(email, otp, expiry);
        tokenRepository.save(token);

        // Build and send email
        String html = MailTemplates.forgotPasswordOtp(user.getFirstName() == null ? user.getEmail() : user.getFirstName(),
                otp, OTP_EXPIRY_MINUTES);
        emailService.sendHtmlMessage(email, "Your password reset OTP", html);
    }

    /**
     * Verify OTP and change password if valid.
     */
    @Transactional
    public void verifyOtpAndResetPassword(String email, String otp, String newPassword) {
        PasswordResetToken token = tokenRepository.findByEmailAndOtp(email, otp)
                .orElseThrow(() -> new IllegalArgumentException("Invalid OTP or email"));

        if (token.getExpiryAt().isBefore(LocalDateTime.now())) {
            tokenRepository.deleteByEmail(email);
            throw new IllegalArgumentException("OTP expired. Please request a new one.");
        }

        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // set new password
        String hashed = BCrypt.hashpw(newPassword, BCrypt.gensalt());
        user.setPassword(hashed);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        // remove token after successful reset
        tokenRepository.deleteByEmail(email);

        // send confirmation email
        String confirmHtml = MailTemplates.passwordResetSuccess(user.getFirstName() == null ? user.getEmail() : user.getFirstName());
        emailService.sendHtmlMessage(email, "Your password has been changed", confirmHtml);
    }

    private String generateOtp() {
        // Secure-ish 6-digit OTP
        Random rnd = new Random();
        int number = 100000 + rnd.nextInt(900000);
        return String.valueOf(number);
    }
}
