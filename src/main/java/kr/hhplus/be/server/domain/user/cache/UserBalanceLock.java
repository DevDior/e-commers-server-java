package kr.hhplus.be.server.domain.user.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserBalanceLock {

    private final StringRedisTemplate redisTemplate;

    private static final String LOCK_KEY_PREFIX = "lock:user:balance:"; // Redis 락 키 prefix
    private static final long LOCK_EXPIRE_SECONDS = 5; // 락 만료 시간 (5초)

    /**
     * 사용자 잔액에 대한 락 획득 시도
     * @param userId 사용자 ID
     * @return 락 획득 성공 여부
     */
    public boolean lock(Long userId) {
        String lockKey = LOCK_KEY_PREFIX + userId;
        Boolean success = redisTemplate.opsForValue().setIfAbsent(
                lockKey,
                "locked",
                LOCK_EXPIRE_SECONDS,
                TimeUnit.SECONDS
        );

        if (Boolean.TRUE.equals(success)) {
            log.info("[UserBalanceLock] 사용자 {} 락 획득 성공", userId);
            return true;
        } else {
            log.warn("[UserBalanceLock] 사용자 {} 락 획득 실패 (이미 다른 요청 처리 중)", userId);
            return false;
        }
    }

    /**
     * 사용자 잔액에 대한 락 해제
     * @param userId 사용자 ID
     */
    public void unlock(Long userId) {
        String lockKey = LOCK_KEY_PREFIX + userId;
        redisTemplate.delete(lockKey);
        log.info("[UserBalanceLock] 사용자 {} 락 해제 완료", userId);
    }
}
