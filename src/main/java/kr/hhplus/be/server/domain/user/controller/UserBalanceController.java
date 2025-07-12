package kr.hhplus.be.server.domain.user.controller;

import kr.hhplus.be.server.domain.user.dto.ChargeRequest;
import kr.hhplus.be.server.domain.user.dto.ChargeResponse;
import kr.hhplus.be.server.domain.user.dto.BalanceResponse;
import kr.hhplus.be.server.domain.user.service.UserBalanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/user/{userId}/balance")
@Slf4j
public class UserBalanceController {

    private final UserBalanceService userBalanceService;

    @PostMapping
    public ResponseEntity<ChargeResponse> chargeBalance(
            @PathVariable Long userId,
            @RequestBody ChargeRequest request
    ) {
        log.info("[UserBalanceController] 사용자 {} 잔액 {}원 충전 요청 수신", userId, request.amount());

        long updatedAmount = userBalanceService.charge(userId, request.amount());

        return ResponseEntity.ok(new ChargeResponse(userId, updatedAmount));
    }

    @GetMapping
    public ResponseEntity<BalanceResponse> getBalance(@PathVariable Long userId) {
        log.info("[UserBalanceController] 사용자 {} 잔액 조회 요청 수신", userId);

        long currentAmount = userBalanceService.getBalance(userId);

        return ResponseEntity.ok(new BalanceResponse(userId, currentAmount));
    }
}
