# 🛒 e-Commerce 주문 서비스 시퀀스 다이어그램
이 문서는 기능 요구사항에 따라 각 기능의 시퀀스 다이어그램을 Mermaid 문법으로 표현한 것입니다. DB는 MySQL, 필요 시 Redis를 활용합니다.

---

## ✅ 1. 잔액 충전 API

```mermaid
sequenceDiagram
    participant 사용자
    participant 잔액 서비스
    participant Redis
    participant MySQL
    
    사용자->>잔액 서비스: 잔액 충전 요청 (사용자 ID, 금액)
    잔액 서비스->>Redis: 사용자 락 획득 시도 (SETNX)
    alt 락 획득 실패
        잔액 서비스-->>사용자: 충전 실패 (충전 진행 중)
    else 락 획득 성공
        잔액 서비스->>MySQL: 사용자 잔액 조회
        MySQL-->>잔액 서비스: 사용자 잔액 반환
        잔액 서비스->>MySQL: 사용자 잔액 업데이트 및 충전 이력 기록
        잔액 서비스->>Redis: 사용자 락 해제 (DEL)
        잔액 서비스-->>사용자: 충전 완료 메시지 반환
    end
```

---

## ✅ 2. 잔액 조회 API
```mermaid
sequenceDiagram
    participant 사용자
    participant 잔액 서비스
    participant MySQL
    
    사용자->> 잔액 서비스: 잔액 조회 요청
    잔액 서비스->>MySQL: 사용자 잔액 조회
    MySQL-->>잔액 서비스: 잔액 데이터 반환
    잔액 서비스-->>사용자: 잔액 조회 결과 반환
```

---

## ✅ 3. 상품 조회 API

```mermaid
sequenceDiagram
    participant 사용자
    participant 상품 서비스
    participant Redis
    participant MySQL
    
    사용자->>상품 서비스: 상품 목록 조회 요청
    상품 서비스->>Redis: 캐시 조회 (products:all)
    alt 캐시 있음
        Redis-->>상품 서비스: 캐시된 상품 목록 반환
        상품 서비스-->>사용자: 상품 목록 반환
    else 캐시 없음
        상품 서비스->>MySQL: 상품 전체 조회
        MySQL-->>상품 서비스: 상품 목록 반환
        상품 서비스->>Redis: 캐시 저장 (products:all)
        상품 서비스-->>사용자: 상품 목록 반환
    end
```

---

## ✅ 4. 주문 / 결제 API

```mermaid
sequenceDiagram
    participant 사용자
    participant 주문 서비스
    participant 쿠폰 서비스
    participant 잔액 서비스
    participant 상품 서비스
    participant Redis
    participant MySQL
    participant 외부 플랫폼

    사용자->>주문 서비스: 주문 + 결제 요청

    주문 서비스->>Redis: 사용자 락 획득 (lock:user:{user_id})
    alt 실패
        주문 서비스-->>사용자: 실패 (사용자 락 경합 중)
    end

    loop 상품 목록
        주문 서비스->>Redis: 상품 락 획득 (lock:product:{product_id})
        alt 실패
            주문 서비스->>Redis: 사용자 락, 상품 락 해제
            주문 서비스-->>사용자: 실패 (상품 락 경합 중)
        end
    end

    주문 서비스->>Redis: 쿠폰 락 획득 (lock:coupon:{coupon_id})
    alt 실패
        주문 서비스->>Redis: 사용자, 상품 락 해제
        주문 서비스-->>사용자: 실패 (쿠폰 락 경합 중)
    end

    주문 서비스->>쿠폰 서비스: 쿠폰 유효성 확인
    쿠폰 서비스->>MySQL: 쿠폰 정보 조회
    쿠폰 서비스-->>주문 서비스: 할인 금액 반환

    주문 서비스->>상품 서비스: 재고 선점 요청
    상품 서비스->>Redis: 재고 차감
    alt 재고 부족
        주문 서비스->>Redis: 모든 락 해제
        주문 서비스-->>사용자: 실패 (재고 부족)
    end

    주문 서비스->>잔액 서비스: 잔액 차감 요청
    잔액 서비스->>MySQL: 잔액 확인 및 차감
    alt 잔액 부족
        주문 서비스->>상품 서비스: 재고 복구
        주문 서비스->>Redis: 모든 락 해제
        주문 서비스-->>사용자: 실패 (잔액 부족)
    end

    주문 서비스->>MySQL: 주문, 주문 항목, 결제 정보 저장
    주문 서비스->>쿠폰 서비스: 쿠폰 사용 처리
    주문 서비스->>MySQL: 커밋

    주문 서비스->>Redis: 상품 목록 캐시 무효화
    주문 서비스->>외부 플랫폼: 주문 정보 전송
    주문 서비스->>MySQL: 이벤트 로그 저장

    주문 서비스->>Redis: 모든 락 해제
    주문 서비스-->>사용자: 주문 + 결제 성공

```

---

## ✅ 5. 쿠폰 발급 API

```mermaid
sequenceDiagram
    participant 사용자
    participant 쿠폰 서비스
    participant Redis
    participant MySQL
    
    사용자->>쿠폰 서비스: 쿠폰 발급 요청 (coupon_id))
    
    쿠폰 서비스->>Redis: 쿠폰 락 획득 (lock:coupon:{coupon_id})
    alt 실패
        쿠폰 서비스-->>사용자: 실패 (쿠폰 락 경합 중)
    end
    
    쿠폰 서비스->>MySQL: 사용자 쿠폰 중복 확인
    alt 이미 발급됨
        쿠폰 서비스->>Redis: 쿠폰 락 해제
        쿠폰 서비스-->>사용자: 실패 (이미 발급된 쿠폰)
    end
    
    쿠폰 서비스->>MySQL: 쿠폰 수량 조회
    alt 수량 초과
        쿠폰 서비스->>Redis: 쿠폰 락 해제
        쿠폰 서비스-->>사용자: 실패 (쿠폰 소진)
    end
    
    쿠폰 서비스->>MySQL: 쿠폰 발급 처리
    쿠폰 서비스->>Redis: 쿠폰 락 해제
    쿠폰 서비스-->>사용자: 쿠폰 발급 성공
```

---

## ✅ 6. 보유 쿠폰 조회 API

```mermaid
sequenceDiagram
    participant 사용자
    participant 쿠폰 서비스
    participant MySQL
    
    사용자->. 쿠폰 서비스: 보유 쿠폰 조회 요청
    
    쿠폰 서비스->>MySQL: 사용자 쿠폰 목록 조회
    쿠폰 서비스-->>사용자: 쿠폰 목록 반환
```

---

## ✅ 7. 인기 상품 조회 API

```mermaid
sequenceDiagram
    participant 사용자
    participant 상품 서비스
    participant Redis
    participant MySQL
    
    사용자->> 상품 서비스: 인기 상품 조회 요청
    
    상품 서비스->>Redis: 캐시 조회 (products:popular)
    alt 캐시 있음
        Redis-->>상품 서비스: 캐시된 인기 상품 반환
        상품 서비스-->>사용자: 인기 상품 목록 반환
    else 캐시 없음
        상품 서비스->>MySQL: 인기 상품 목록 조회
        상품 서비스->>Redis: 캐시 저장 (products:popular)
        상품 서비스-->>사용자: 인기 상품 목록 반환
    end
```

---