package kr.hhplus.be.server.domain.user.repository;

import kr.hhplus.be.server.domain.user.entity.UserBalance;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserBalanceRepository extends JpaRepository<UserBalance, Long> {
}
