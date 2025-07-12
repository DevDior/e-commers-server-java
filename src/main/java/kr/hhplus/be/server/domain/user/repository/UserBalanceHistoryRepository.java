package kr.hhplus.be.server.domain.user.repository;

import kr.hhplus.be.server.domain.user.entity.UserBalanceHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserBalanceHistoryRepository extends JpaRepository<UserBalanceHistory, Long> {
}
