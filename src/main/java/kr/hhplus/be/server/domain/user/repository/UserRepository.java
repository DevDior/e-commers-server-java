package kr.hhplus.be.server.domain.user.repository;

import kr.hhplus.be.server.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
