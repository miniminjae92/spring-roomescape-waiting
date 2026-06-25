# Roomescape API 명세

## 공통

- 요청·응답 형식: `application/json`
- 브라우저 인증: `ROOMESCAPE_SESSION` HttpOnly 쿠키
- 인증 실패: `401 Unauthorized`
- 권한 부족: `403 Forbidden`
- 오류 응답:

```json
{
  "code": "ERROR_CODE",
  "message": "개발자용 원인",
  "action": "권장 조치"
}
```

## 인증

### `POST /auth/signup`

```json
{
  "loginId": "whale",
  "password": "password123",
  "name": "고래"
}
```

- 성공: `201 Created`
- 성공 시 세션 쿠키 발급

### `POST /auth/login`

```json
{
  "loginId": "whale",
  "password": "password123"
}
```

- 성공: `200 OK`
- 성공 시 기존 인증 대신 사용할 새 세션 쿠키 발급

### `GET /auth/me`

- 로그인 필요

```json
{
  "id": 2,
  "loginId": "whale",
  "name": "고래",
  "role": "USER"
}
```

### `POST /auth/logout`

- 성공: `204 No Content`
- 서버 세션 삭제 및 쿠키 만료

## 운영 회차

운영 회차는 특정 날짜·시간·테마에 실제로 판매하는 방탈출 한 회차다.

### `GET /reservation-slots?themeId={themeId}&dateId={dateId}`

```json
[
  {
    "id": 100,
    "themeId": 3,
    "date": "2026-07-01",
    "timeId": 2,
    "startAt": "10:00",
    "price": 30000,
    "status": "OPEN",
    "available": true
  }
]
```

### `POST /admin/reservation-slots`

- 관리자 권한 필요

```json
{
  "dateId": 1,
  "timeId": 2,
  "themeId": 3,
  "price": 30000
}
```

- `price` 생략 시 테마 기본 가격 사용
- 동일한 날짜·시간·테마 조합은 중복 생성 불가

### `PATCH /admin/reservation-slots/{id}/open`

- 관리자 권한 필요
- 성공: `204 No Content`

### `PATCH /admin/reservation-slots/{id}/close`

- 관리자 권한 필요
- 성공: `204 No Content`

## 예약

모든 사용자 예약 API는 로그인이 필요하다. 예약자 이름이나 회원 ID를 요청에서 받지 않고 세션 회원을 사용한다.

### `POST /reservations`

```json
{
  "dateId": 1,
  "timeId": 2,
  "themeId": 3
}
```

- 운영 회차가 존재하고 `OPEN`이어야 한다.
- 활성 예약이 이미 있으면 `409 Conflict`
- 애플리케이션 사전 검사와 DB 유일성 제약을 모두 적용한다.

### `GET /reservations`

- 현재 로그인 회원의 다가오는 예약만 조회

### `PATCH /reservations/{id}`

```json
{
  "dateId": 2,
  "timeId": 4
}
```

- 본인 예약만 변경 가능
- 기존 테마와 요청 날짜·시간으로 생성된 운영 회차가 필요

### `DELETE /reservations/{id}`

- 본인 예약만 취소 가능
- 성공: `204 No Content`
- 행을 삭제하지 않고 `CANCELED` 상태와 취소 시각을 기록

## 예약 대기

### `POST /waiting-reservations`

```json
{
  "dateId": 1,
  "timeId": 2,
  "themeId": 3
}
```

- 로그인 필요
- 활성 예약이 존재하는 운영 회차에만 신청 가능
- 같은 회원의 동일 회차 `WAITING` 신청은 하나만 허용

### `GET /waiting-reservations`

- 현재 로그인 회원의 다가오는 예약 대기와 순번 조회

### `DELETE /waiting-reservations/{id}`

- 본인 대기만 취소 가능
- 행을 삭제하지 않고 `CANCELED` 상태와 취소 시각을 기록

## 관리자

다음 API는 `MANAGER` 역할이 필요하다.

- `/admin/themes/**`
- `/admin/reservation-dates/**`
- `/admin/times/**`
- `/admin/reservation-slots/**`
- `/admin/reservations/**`

개발용 계정:

- ID: `manager`
- 비밀번호: `admin1234`
