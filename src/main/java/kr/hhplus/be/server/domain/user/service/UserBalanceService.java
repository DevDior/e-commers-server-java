package kr.hhplus.be.server.domain.user.service;

import kr.hhplus.be.server.domain.user.cache.UserBalanceLock;
import kr.hhplus.be.server.domain.user.entity.User;
import kr.hhplus.be.server.domain.user.entity.UserBalance;
import kr.hhplus.be.server.domain.user.entity.UserBalanceHistory;
import kr.hhplus.be.server.domain.user.repository.UserBalanceRepository;
import kr.hhplus.be.server.domain.user.repository.UserBalanceHistoryRepository;
import kr.hhplus.be.server.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserBalanceService {

    private final UserRepository userRepository;
    private final UserBalanceRepository userBalanceRepository;
    private final UserBalanceHistoryRepository userBalanceHistoryRepository;
    private final UserBalanceLock userBalanceLock;

    @Transactional
    public long charge(Long userId, long chargeAmount) {
        log.info("[UserBalanceService] 사용자 {} 잔액 {}원 충전 요청", userId, chargeAmount);

        if (!userBalanceLock.lock(userId)) {
            log.warn("[UserBalanceService] 사용자 {} 잔액 충전 중 다른 요청 감지 (락 획득 실패)", userId);
            throw new IllegalStateException("다른 충전 요청이 처리 중입니다.");
        }

        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. userID: " + userId));

            UserBalance balance = userBalanceRepository.findById(userId)
                    .orElseGet(() -> {
                        log.info("[UserBalanceService] 사용자 {} 잔액 정보 없음 → 신규 생성", userId);
                        return UserBalance.builder()
                                .user(user)
                                .amount(0L)
                                .build();
                    });

            long beforeAmount = balance.getAmount();
            balance.charge(chargeAmount);
            userBalanceRepository.save(balance);

            log.info("[UserBalanceService] 사용자 {} 잔액 {}원 → {}원 업데이트 완료",
                    userId, beforeAmount, balance.getAmount());

            saveChargeHistory(user, chargeAmount);

            return balance.getAmount();
        } finally {
            userBalanceLock.unlock(userId);
            log.info("[UserBalanceService] 사용자 {} 락 해제 완료", userId);
        }
    }

    @Transactional(readOnly = true)
    public long getBalance(Long userId) {
        log.info("[UserBalanceService] 사용자 {} 잔액 조회 요청", userId);

        return userBalanceRepository.findById(userId)
                .map(UserBalance::getAmount)
                .orElse(0L);
    }

    private void saveChargeHistory(User user, long chargeAmount) {
        UserBalanceHistory history = UserBalanceHistory.builder()
                .user(user)
                .amount(chargeAmount)
                .type(UserBalanceHistory.Type.CHARGE)
                .createdAt(LocalDateTime.now())
                .build();

        userBalanceHistoryRepository.save(history);
        log.info("[UserBalanceService] 사용자 {} 충전 이력 기록 완료", user.getId());
    }
}
