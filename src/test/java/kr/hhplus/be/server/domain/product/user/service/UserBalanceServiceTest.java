package kr.hhplus.be.server.domain.product.user.service;

import kr.hhplus.be.server.domain.user.cache.UserBalanceLock;
import kr.hhplus.be.server.domain.user.entity.UserBalance;
import kr.hhplus.be.server.domain.user.entity.UserBalanceHistory;
import kr.hhplus.be.server.domain.user.repository.UserBalanceRepository;
import kr.hhplus.be.server.domain.user.repository.UserBalanceHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UserBalanceServiceTest {

    private UserBalanceService userBalanceService;
    private UserBalanceRepository userBalanceRepository;
    private UserBalanceHistoryRepository userBalanceHistoryRepository;
    private UserBalanceLock userBalanceLock;

    @BeforeEach
    void setUp() {
        userBalanceRepository = mock(UserBalanceRepository.class);
        userBalanceHistoryRepository = mock(UserBalanceHistoryRepository.class);
        userBalanceLock = mock(UserBalanceLock.class);
        userBalanceService = new UserBalanceService(userBalanceRepository, userBalanceHistoryRepository, userBalanceLock);
    }

    @Test
    @DisplayName("락 획득 성공 시 기존 잔액에 충전금액 합산")
    void chargeBalance_whenLockAcquired_thenUpdateBalance() {
        // given
        Long userId = 1L;
        long chargeAmount = 5000L;
        UserBalance existingBalance = UserBalance.builder()
                .userId(userId)
                .amount(10000L)
                .build();

        when(userBalanceLock.lock(userId)).thenReturn(true);
        when(userBalanceRepository.findById(userId)).thenReturn(Optional.of(existingBalance));

        // when
        long updatedAmount = userBalanceService.charge(userId, chargeAmount);

        // then
        verify(userBalanceRepository).updateAmount(userId, 15000L);
        verify(userBalanceHistoryRepository).save(any(UserBalanceHistory.class));
        verify(userBlanceLock).unlock(userId);

        assertThat(updatedAmount).isEqualTo(15000L);
    }

    @Test
    @DisplayName("락 획득 실패 시 충전 거부")
    void chargeBalance_whenLockNotAcquired_thenThrowException() {
        // given
        Long userId = 1L;
        long chargeAmount = 5000L;

        when(userBalanceLock.lock(userId)).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> userBalanceService.charge(userId, chargeAmount))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("다른 충전 요청이 처리 중입니다.");

        verify(userBalanceRepository, never()).findById(anyLong());
        verify(userBalanceRepository, never()).updatedAmount(anyLong(), anyLong());
        verify(userBalanceHistoryRepository, never()).save(any(UserBalanceHistory.class));
        verify(userBalanceLock, never()).unlock(anyLong());
    }

    @Test
    @DisplayName(("기존 잔액 없으면 신규 잔액 생성 후 충전"))
    void chargeBalance_whenNoExistingBalance_thenCreateNewBalance() {
        // given
        Long userId = 1L;
        long chargeAmount = 5000L;

        when(userBalanceLock.lock(userId)).thenReturn(true);
        when(userBalanceRepository.findById(userId)).thenReturn(Optional.empty());

        // when
        long updatedAmount = userBalanceService.charge(userId, chargeAmount);

        // then
        verify(userBalanceRepository).save(any(UserBalance.class));
        verify(userBalanceHistoryRepository).save(any(UserBalanceHistory.class));
        verify(userBalanceLock).unlock(userId);

        assertThat(updatedAmount).isEqualTo(chargeAmount);
    }
}
