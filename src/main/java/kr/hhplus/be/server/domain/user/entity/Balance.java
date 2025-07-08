package kr.hhplus.be.server.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "balance")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Balance {

    @Id
    @Column(name = "user_id")
    private Long userId;

    private Long amount;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;
}
