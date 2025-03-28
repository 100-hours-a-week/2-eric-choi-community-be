AmuMal Community Service
프로젝트 설명
AmuMal Community Service는 아무말(AmuMal) 서비스의 커뮤니티 기능을 담당하는 백엔드 애플리케이션입니다. 사용자들이 게시글을 작성하고, 댓글을 달고, 좋아요를 표시하는 등의 소통 기능을 제공하여 사용자 간의 활발한 커뮤니케이션을 지원합니다.
이 프로젝트는 Spring Boot 기반의 RESTful API 서비스로, 사용자 인증 및 권한 관리, 게시글 및 댓글 작성, 좋아요 기능, 미디어 파일 업로드 등 커뮤니티 서비스의 핵심 기능을 구현하였습니다.
프로젝트 동기 및 목표

사용자들이 서로의 경험과 정보를 공유할 수 있는 커뮤니티 플랫폼 필요성
확장 가능하고 유지보수가 용이한 백엔드 아키텍처 구축
테스트 주도 개발(TDD)을 통한 안정적인 서비스 제공
AWS 클라우드 인프라를 활용한 확장성 있는 서비스 구현

목차

기술 스택
주요 기능
시스템 아키텍처
설치 및 실행 방법
API 문서
테스트
프로젝트 구조
배운 점 및 향후 개선 사항

기술 스택
백엔드

Java 21
Spring Boot 3.x
Spring Security & JWT
Spring Data JPA
MySQL
AWS S3 (이미지 저장)

개발 도구

Gradle
JUnit 5, Mockito
Swagger/OpenAPI
Git & GitHub

주요 기능
사용자 관리

회원가입, 로그인, 로그아웃
JWT 기반 인증
사용자 프로필 관리 (닉네임 변경, 프로필 이미지 업로드)

게시글 관리

게시글 CRUD (생성, 조회, 수정, 삭제)
게시글 페이지네이션 (커서 기반)
게시글 이미지 업로드 (AWS S3 연동)

댓글 기능

게시글에 댓글 작성, 수정, 삭제
댓글 목록 조회

좋아요 기능

게시글 좋아요/좋아요 취소
좋아요 상태 확인

시스템 아키텍처
복사┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│ 클라이언트   │─────►   API 서버   │─────► 데이터베이스  │
└─────────────┘     └─────┬───────┘     └─────────────┘
                          │
                          ▼
                    ┌─────────────┐
                    │   AWS S3    │
                    └─────────────┘

레이어드 아키텍처: Controller - Service - Repository 계층 구조 구현
JWT 인증: 토큰 기반 사용자 인증 체계 구현
예외 처리: Global Exception Handler를 통한 일관된 예외 처리

설치 및 실행 방법
요구사항

JDK 21 이상
MySQL 8.0
AWS 계정 (S3 사용)

설치 단계

저장소 클론

bash복사git clone https://github.com/your-username/amumal-community.git
cd amumal-community

MySQL 설정

sql복사CREATE DATABASE amumal_db;
CREATE USER 'amumal_user'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON amumal_db.* TO 'amumal_user'@'localhost';
FLUSH PRIVILEGES;

application.properties 또는 application.yml 파일 설정

yaml복사spring:
  datasource:
    url: jdbc:mysql://localhost:3306/amumal_db
    username: amumal_user
    password: your_password
  jpa:
    hibernate:
      ddl-auto: update
    
cloud:
  aws:
    credentials:
      access-key: your_aws_access_key
      secret-key: your_aws_secret_key
    s3:
      bucket: your_bucket_name
    region:
      static: ap-northeast-2

jwt:
  secret: your_jwt_secret_key
  expiration: 86400000

애플리케이션 빌드 및 실행

bash복사./gradlew build
./gradlew bootRun

애플리케이션 접속

복사http://localhost:8080
API 문서
API 문서는 Swagger UI를 통해 접근할 수 있습니다:
복사http://localhost:8080/swagger-ui.html
주요 API 엔드포인트:

사용자: /users/*
게시글: /posts/*
댓글: /posts/{postId}/comments/*
좋아요: /posts/{postId}/likes/*

테스트
프로젝트는 단위 테스트와 통합 테스트를 모두 포함하고 있습니다:
bash복사# 모든 테스트 실행
./gradlew test

# 단위 테스트만 실행
./gradlew test --tests "*Test"

# 통합 테스트만 실행
./gradlew test --tests "*IntegrationTest"
프로젝트 구조
복사src
├── main
│   ├── java
│   │   └── com
│   │       └── amumal
│   │           └── community
│   │               ├── domain
│   │               │   ├── post
│   │               │   │   ├── controller
│   │               │   │   ├── dto
│   │               │   │   ├── entity
│   │               │   │   ├── repository
│   │               │   │   └── service
│   │               │   └── user
│   │               │       ├── controller
│   │               │       ├── dto
│   │               │       ├── entity
│   │               │       ├── repository
│   │               │       └── service
│   │               └── global
│   │                   ├── config
│   │                   ├── dto
│   │                   ├── enums
│   │                   ├── exception
│   │                   ├── s3
│   │                   └── util
│   └── resources
│       ├── application.yml
│       └── static
└── test
    └── java
        └── com
            └── amumal
                └── community
                    ├── domain
                    │   ├── post
                    │   │   ├── controller
                    │   │   ├── repository
                    │   │   └── service
                    │   └── user
                    │       ├── controller
                    │       ├── repository
                    │       └── service
                    └── global
배운 점 및 향후 개선 사항
배운 점

계층화된 아키텍처를 통한 코드 분리와 단일 책임 원칙 적용
테스트 주도 개발(TDD)을 통한 안정적인 코드 작성
JWT 기반 인증 구현 및 보안
AWS S3를 활용한 미디어 파일 관리
커서 기반 페이지네이션 구현으로 대량 데이터 효율적 처리

향후 개선 사항

캐시 적용을 통한 성능 최적화
마이크로서비스 아키텍처로의 전환 검토
알림 기능 추가
실시간 채팅 기능 구현
검색 기능 개선 (Elasticsearch 도입 검토)
CI/CD 파이프라인 구축


본 프로젝트는 AmuMal 서비스의 일부로, 사용자 간의 소통과 정보 공유를 돕는 커뮤니티 기능을 제공합니다. 계속해서 사용자 경험을 개선하고 새로운 기능을 추가할 예정이니 많은 관심과 피드백 부탁드립니다.
