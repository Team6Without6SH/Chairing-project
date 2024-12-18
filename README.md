# Chairing Project

### 프로젝트 개요
Chairing Project는 사용자가 식당에 직접 방문하지 않고 웨이팅을 신청하거나 특정 날짜에 예약할 수 있는 기능을 제공하는 플랫폼입니다. 본 프로젝트는 다음과 같은 주요 기능을 포함합니다:
- 회원가입 및 로그인
- 가게 조회 및 주문/예약 신청
- 주문 상태 알림 기능

### 기술 스택
- **WebSocket**: 실시간 알림 및 웨이팅 상태 업데이트
- **Redis**: Pub/Sub 구조 및 데이터 캐싱
- **FCM (Firebase Cloud Messaging)**: 모바일 알림 발송
- **Caffeine Cache**: 메시지 중복 전송 방지
- **Spring Security**: 인증 및 권한 관리
- **Amazon S3**: 파일 저장소

### 주요 기능
#### 1. 회원 관리
- Spring Security를 사용한 인증 및 권한 관리
- `BCryptPasswordEncoder`를 활용한 비밀번호 암호화

#### 2. 웨이팅 및 예약 기능
- 웨이팅 상태 및 입장 가능 여부 실시간 업데이트
- 주문 및 예약 정보의 CRUD 처리

#### 3. 주문 상태 알림 기능
- Redis를 사용하여 Pub/Sub 구조로 알림 메시지 처리
- WebSocket을 통해 실시간 알림 제공
- FCM을 활용한 모바일 푸시 알림

### 기술적 의사결정
#### 1. 동시성 제어
- **낙관적 락**: 높은 읽기 성능과 낮은 충돌 확률을 기대할 때 사용
- **비관적 락**: 충돌 가능성이 높은 경우 데이터의 일관성을 보장
- **Redis 락**: 분산 환경에서의 동시성 문제 해결

#### 2. Redis Pub/Sub 구현
- 주문 상태 변경 시 Redis를 활용하여 실시간으로 알림 발행
- `RedisMessageListenerContainer`를 이용해 구독 및 처리

#### 3. FCM 적용 방식
- FCM을 통해 푸시 알림 발송 기능 구현
- 현재 적용된 방식의 개선 방안:
  - 메시지 템플릿 관리
  - 사용자 환경에 따른 알림 커스터마이징

#### 4. S3 활용
- 파일 저장 및 관리에 S3 사용
- 구체적인 구현 방안 및 고민 사항은 팀원 추가 예정

#### 5. 캐싱 처리
- Redis와 Caffeine Cache를 사용하여 데이터 캐싱 및 메시지 중복 처리
- Redis Pub/Sub 구조를 통해 캐싱 데이터 갱신
- Caffeine Cache로 메시지 TTL(Time-To-Live) 설정

### 성능 개선
#### 1. WebSocket 최적화
- 클라이언트 세션 관리 개선
- 메시지 큐를 활용한 대규모 트래픽 처리

#### 2. Redis 사용 최적화
- Redis 클러스터링 도입 검토
- 키 스페이스 설계 최적화

#### 3. 테스트 커버리지
- CRUD 기능 테스트 커버리지 90% 이상 유지
- 주요 기능에 대한 통합 테스트 및 성능 테스트 수행

### 팀원 참여
- FCM 및 S3 관련 구현 사항 업데이트 예정
- 성능 개선과 기술적 의사결정 항목은 팀원이 지속적으로 갱신 예정
