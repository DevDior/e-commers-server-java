package kr.hhplus.be.server.domain.coupon.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "coupons")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long discountAmount;

    private Integer totalQuantity;

    private Integer issuedQuantity;

    private LocalDateTime validFrom;

    private LocalDateTime validTo;
}
