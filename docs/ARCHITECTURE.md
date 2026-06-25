# 예약·결제 도입 전 아키텍처

## 핵심 모델

```text
Member
  ├─ Reservation ── ReservationSlot ── Theme
  └─ WaitingReservation ┘                ├─ ReservationDate
                                        └─ ReservationTime
```

`ReservationSlot`은 실제로 판매하는 운영 회차다. 날짜·시간·테마 마스터 데이터가 존재해도 운영 회차가 생성되지 않았다면 예약할 수 없다.

## 상태와 삭제 정책

- 예약: `PENDING_PAYMENT`, `CONFIRMED`, `PAYMENT_FAILED`, `CANCELED`, `EXPIRED`
- 예약 대기: `WAITING`, `OFFERED`, `CONVERTED`, `CANCELED`, `EXPIRED`
- 취소·실패 이력이 필요한 예약과 대기는 상태를 변경하고 보존한다.
- 세션처럼 재생성 가능한 임시 데이터만 삭제한다.

## 동시성 기준

예약 전 조회 결과만으로 빈 슬롯을 보장할 수 없다.

```text
요청 A: 빈 슬롯 확인
요청 B: 빈 슬롯 확인
요청 A: INSERT 성공
요청 B: DB UNIQUE 위반
```

`reservation(slot_id, active_slot)` 유일성 제약이 최종 정합성을 보장한다.

- 슬롯 점유 상태는 `active_slot = true`
- 취소·실패 상태는 `active_slot = null`
- 서비스는 유일성 위반을 `RESERVATION_DUPLICATED`로 변환한다.
- 운영 회차 수정 충돌은 `@Version` 낙관적 락으로 감지한다.

단일 인스턴스 학습 프로젝트이므로 분산 락은 도입하지 않는다.

## 인증·인가 기준

- Spring Security 없이 인터셉터와 인자 리졸버를 사용한다.
- 세션에는 회원 ID와 역할만 저장한다.
- 예약 소유권은 URL·요청 이름이 아니라 `reservation.member_id`로 검증한다.
- 관리자 권한은 `@LoginRequired(managerOnly = true)`로 명시한다.
- 비밀번호는 PBKDF2 해시만 저장한다.
- 현재 세션 저장소는 인메모리이므로 다중 인스턴스 운영 대상은 아니다.

## 패키지 기준

현재 코드는 기존 미션 구조를 보존해 기능별 패키지 안에 컨트롤러·서비스·엔티티가 함께 있다. 결제 기능부터는 외부 시스템 경계를 명확히 하기 위해 다음 구조를 사용한다.

```text
payment
├── presentation
├── application
├── domain
│   ├── PaymentOrder
│   └── PaymentGateway
└── infrastructure
    └── toss
        └── TossPaymentGateway
```

기존 기능을 일괄 이동하는 리팩터링은 결제 미션과 무관한 변경량이 커서 진행하지 않는다.

## 결제 도입 시 연결점

- `Theme.price`: 기본 가격
- `ReservationSlot.price`: 해당 운영 회차의 판매 가격
- 결제 주문 생성 시 `ReservationSlot.price`를 `PaymentOrder.amount`에 복사
- 브라우저 콜백 금액은 `PaymentOrder.amount`와 비교
- 결제 전 예약 상태를 `PENDING_PAYMENT`로 생성
- 승인 성공 시 `CONFIRMED`, 실패 시 `PAYMENT_FAILED`
- 외부 API 호출 중에는 DB 트랜잭션과 락을 유지하지 않음
