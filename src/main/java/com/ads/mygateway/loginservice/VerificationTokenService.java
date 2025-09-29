package com.ads.mygateway.loginservice;

import com.ads.mygateway.loginentity.AppUser;
import com.ads.mygateway.loginentity.VerificationToken;
import com.ads.mygateway.loginrepository.VerificationTokenRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VerificationTokenService {
    private final VerificationTokenRepository tokenRepository;

    public VerificationToken createToken(AppUser user, int minutesValid) {
        String token = UUID.randomUUID().toString();
        VerificationToken vt = VerificationToken.builder()
                .token(token)
                .user(user)
                .expiryDate(LocalDateTime.now().plusMinutes(minutesValid))
                .build();
        return tokenRepository.save(vt);
    }

    public Optional<VerificationToken> findByToken(String token) {
        return tokenRepository.findByToken(token);
    }

    @Transactional
    public void deleteByToken(String token) {
        tokenRepository.deleteByToken(token);
    }
}
