package com.example.Vi_vu_Viet.Service.Implement;

import com.example.Vi_vu_Viet.Repository.Auth.BlacklistTokenRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class BlacklistCleanupService {

    BlacklistTokenRepository blacklistTokenRepository;

    @Scheduled(fixedDelayString = "${app.auth.blacklist-cleanup-interval-ms:3600000}")
    @Transactional
    public void deleteExpiredTokens() {
        blacklistTokenRepository.deleteExpiredTokens(LocalDateTime.now());
    }
}
