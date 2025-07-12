package kr.hhplus.be.server.domain.user.service;

import kr.hhplus.be.server.domain.user.cache.UserBalanceLock;
import kr.hhplus.be.server.domain.user.entity.User;
import kr.hhplus.be.server.domain.user.entity.UserBalance;
import kr.hhplus.be.server.domain.user.entity.UserBalanceHistory;
import kr.hhplus.be.server.domain.user.repository.UserBalanceRepository;
import kr.hhplus.be.server.domain.user.repository.UserBalanceHistoryRepository;
import kr.hhplus.be.server.domain.user.repository.UserRepository;
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
    private UserRepository userRepository;
    private User user;

    @BeforeEach
    void setUp() {
        userBalanceRepository = mock(UserBalanceRepository.class);
        userBalanceHistoryRepository = mock(UserBalanceHistoryRepository.class);
        userBalanceLock = mock(UserBalanceLock.class);
        userRepository = mock(UserRepository.class);

        userBalanceService = new UserBalanceService(
                userRepository,
                userBalanceRepository,
                userBalanceHistoryRepository,
                userBalanceLock
        );

        user = User.builder().id(1L).name("테스트 유저").build();

        // 모든 테스트에서 UserRepository.findById()가 호출될 때 이 user 반환
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
    }

    @Test
    @DisplayName("락 획득 후 기존 잔액에 충전금액 합산")
    void chargeBalance_whenLockAcquired_thenUpdateBalance() {
        // given
        long chargeAmount = 5000L;
        UserBalance existingBalance = UserBalance.builder()
                .user(user)
                .amount(10000L)
                .build();

        when(userBalanceLock.lock(user.getId())).thenReturn(true);
        when(userBalanceRepository.findById(user.getId())).thenReturn(Optional.of(existingBalance));

        // when
        long updatedAmount = userBalanceService.charge(user.getId(), chargeAmount);

        // then
        verify(userBalanceRepository).save(any(UserBalance.class));
        verify(userBalanceHistoryRepository).save(any(UserBalanceHistory.class));
        verify(userBalanceLock).unlock(user.getId());

        assertThat(updatedAmount).isEqualTo(15000L);
    }

    @Test
    @DisplayName("락 획득 실패 시 충전 거부")
    void chargeBalance_whenLockNotAcquired_thenThrowException() {
        // given
        long chargeAmount = 5000L;

        when(userBalanceLock.lock(user.getId())).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> userBalanceService.charge(user.getId(), chargeAmount))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("다른 충전 요청이 처리 중입니다.");

        verify(userBalanceRepository, never()).findById(anyLong());
        verify(userBalanceRepository, never()).save(any(UserBalance.class));
        verify(userBalanceHistoryRepository, never()).save(any(UserBalanceHistory.class));
        verify(userBalanceLock, never()).unlock(anyLong());
    }

    @Test
    @DisplayName("기존 잔액 없으면 신규 잔액 생성 후 충전")
    void chargeBalance_whenNoExistingBalance_thenCreateNewBalance() {
        // given
        long chargeAmount = 5000L;

        when(userBalanceLock.lock(user.getId())).thenReturn(true);
        when(userBalanceRepository.findById(user.getId())).thenReturn(Optional.empty());

        // when
        long updatedAmount = userBalanceService.charge(user.getId(), chargeAmount);

        // then
        verify(userBalanceRepository).save(any(UserBalance.class));
        verify(userBalanceHistoryRepository).save(any(UserBalanceHistory.class));
        verify(userBalanceLock).unlock(user.getId());

        assertThat(updatedAmount).isEqualTo(chargeAmount);
    }

    @Test
    @DisplayName("사용자 잔액 조회 시 잔액 존재하면 반환")
    void getBalance_whenBalanceExists_thenReturnAmount() {
        // given
        UserBalance existingBalance = UserBalance.builder()
                .user(user)
                .amount(20000L)
                .build();

        when(userBalanceRepository.findById(user.getId())).thenReturn(Optional.of(existingBalance));

        // when
        long amount = userBalanceService.getBalance(user.getId());

        // then
        verify(userBalanceRepository).findById(user.getId());
        assertThat(amount).isEqualTo(20000L);
    }

    @Test
    @DisplayName("사용자 잔액 조회 시 잔액 없으면 0 반환")
    void getBalance_whenNoBalance_thenReturnZero() {
        // given
        when(userBalanceRepository.findById(user.getId())).thenReturn(Optional.empty());

        // when
        long amount = userBalanceService.getBalance(user.getId());

        // then
        verify(userBalanceRepository).findById(user.getId());
        assertThat(amount).isEqualTo(0L);
    }
}
