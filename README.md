## 프로젝트 개요
- **목표**: 이 프로젝트는 놀고 싶지만 마땅히 생각나지 않는 사람, 놀 사람이 없는 사람들을 위한 놀거리 추천 서비스이다.
- **주요 기능**:
  - 장소, 놀거리 검색 
  - 커뮤니티
    
![1](https://github.com/user-attachments/assets/45bf5ba3-74b5-4278-b608-4302759db467)
![2](https://github.com/user-attachments/assets/f0f49968-ac89-43c0-bd22-5a432c8fbb5a)
![3](https://github.com/user-attachments/assets/13332bf2-5108-4881-8023-f1583e93ffc1)
![4](https://github.com/user-attachments/assets/97fc88ba-8683-4247-805c-efc78fdf5011)
![5](https://github.com/user-attachments/assets/ecf54acc-ba94-4db5-aa58-da1e272250d0)

## 제작 기간 / 참여 인원

- **제작 기간:** 2024년 6월 - 2024년 9월
- **참여 인원:** FrontEnd 3명, BackEnd 4명
- **프로젝트 고도화:** 2024년 9월 - 진행중

## 기술 스택
### Backend
- Java, Spring Boot, MySQL, Redis, JWT, REST API

### DevOps
- Docker, Kubernetes, Jenkins, Prometheus, Grafana, Oracle Cloud

### Tools & Others
- IntelliJ, DataGrip, Postman, GitHub Webhook, Google API

### AI
- Gemini API

## ERD

<img width="513" alt="erd" src="https://github.com/user-attachments/assets/649b2d09-6fa6-4cbe-8549-9592aa05c7d4">

## 아키텍처

![image](https://github.com/user-attachments/assets/d8526be1-bab0-46a2-8925-e2b290360ec2)

## 역할
* Entire Infra Setting
  * Oracle Kubernetes Setting, Deployment
  * Jenkins CICD
  * Monitoring Tools(Prometheus, Grafana)
    
* Spring Application
  * Spring Security
  * Signup, Login API
  * Search API
    
## 성과 및 개선 결과
- Kubernetes 도입으로 서비스 확장성과 안정성을 강화하여, 트래픽 급증 시에도 무중단 운영이 가능하도록 자동화된 스케일링 환경을 구축
- Prometheus와 Grafana를 활용한 실시간 모니터링 도입으로 서비스 가동률을 99.9%로 유지
- Jenkins CICD 파이프라인을 통한 배포 자동화
- 보안 강화를 위해 Access Token의 만료 시간을 30분으로하고, Refresh Token을 자주 호출해야하기 때문에 Redis를 추가하여 회원 인증로직 최적화
- 게시글 댓글 ERD 및 구조 최적화(자기 참조로 무한 댓글 가능)

## 향후 개선 및 계획
- 검색 API에 AI 추천 기능을 추가하여 사용자 경험을 개선할 계획.
- 검색 API의 응답시간을 개선
- Security 관련 개선



