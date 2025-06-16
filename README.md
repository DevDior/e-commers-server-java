# 📦 e-Commerce 주문 시스템 설계 문서
e-Commerce 주문 서비스의 핵심 설계 문서를 포함하고 있습니다. 백엔드 시스템을 설계하며 작성한 ERD, 인프라 아키텍처, 시퀀스 다이어그램, 요구사항 정의 등을 정리하였습니다.

## 📁 문서 목록

- [마일스톤](https://github.com/users/DevDior/projects/2)  
  └ 현재 진행 중인 프로젝트 마일스톤 및 작업 현황을 확인할 수 있습니다.

- [ERD 설계](./docs/erd.md)  
  └ 엔티티 간의 관계 및 필드 구조를 정의한 데이터베이스 설계 문서입니다.

- [시퀀스 다이어그램](./docs/sequence.md)  
  └ 각 기능(API)의 흐름을 시각화한 Mermaid 기반 시퀀스 다이어그램입니다.

- [요구사항 정의서](./docs/requirements.md)  
  └ 기능 단위의 상세 요구사항과 핵심 비즈니스 로직 설명이 포함되어 있습니다.

- [인프라 구성도](./docs/infra_architecture.md)  
  └ Kubernetes 기반 마이크로서비스 인프라 구조를 설명합니다.

---

## 🛠️ 기술 스택

- Java + Spring Boot
- MySQL, Redis
- Docker, Kubernetes (K8s)
- Kafka (외부 시스템 연동 시 고려)

---

## Getting Started

### Prerequisites

#### Running Docker Containers

`local` profile 로 실행하기 위하여 인프라가 설정되어 있는 Docker 컨테이너를 실행해주셔야 합니다.

```bash
docker-compose up -d
```