# 웹 프로젝트 기본 틀 (Spring Boot + React)

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-18-blue.svg)](https://reactjs.org/)
[![MySQL](https://img.shields.io/badge/MySQL-9.1-orange.svg)](https://www.mysql.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

> 🚀 **엔터프라이즈급 웹 애플리케이션을 위한 완전한 개발 템플릿**
> 
> Spring Boot와 React를 기반으로 한 현대적이고 확장 가능한 웹 애플리케이션 보일러플레이트입니다.

## ✨ 주요 특징

### 🔐 **강력한 인증 시스템**
- JWT 기반 Stateless 인증
- 자동 토큰 갱신 (Refresh Token)
- 역할 기반 접근 제어 (RBAC)
- 보안 감사 로그

### 👥 **사용자 및 권한 관리**
- 계층형 사용자 역할 시스템
- 세분화된 메뉴별 권한 제어
- 동적 메뉴 생성
- 실시간 권한 검증

### 🔍 **고급 기능**
- 통합 검색 및 필터링
- 실시간 대시보드
- 성능 모니터링
- 포괄적 로깅 시스템

### 🏗️ **모던 아키텍처**
- RESTful API 설계
- 마이크로서비스 준비
- 컨테이너 친화적
- 클라우드 네이티브

## 🛠️ 기술 스택

### Backend
- **Framework**: Spring Boot 3.x
- **Database**: MySQL 9.1
- **ORM**: MyBatis
- **Security**: Spring Security + JWT
- **Documentation**: Swagger/OpenAPI 3
- **Monitoring**: Spring Actuator
- **Language**: Java 17+

### Frontend
- **Framework**: React 18
- **UI Library**: Ant Design
- **Build Tool**: Vite
- **State Management**: Redux Toolkit
- **Language**: JavaScript/TypeScript

## 📋 요구사항

### 개발 환경
- **Java**: 17 이상
- **Node.js**: 18 이상
- **MySQL**: 8.0 이상
- **Maven**: 3.8 이상

### 권장 도구
- **IDE**: IntelliJ IDEA / VS Code
- **Git**: 최신 버전
- **Docker**: 선택사항 (컨테이너 배포 시)

## 🚀 빠른 시작

### 1. 저장소 클론
```bash
git clone https://github.com/your-username/web-project-template.git
cd web-project-template
```

### 2. 데이터베이스 설정
```sql
-- MySQL에서 실행
CREATE DATABASE your_database_name DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'your_username'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON your_database_name.* TO 'your_username'@'localhost';
FLUSH PRIVILEGES;

-- 스키마 및 초기 데이터 적용
SOURCE database/complete_database.sql;
```

### 3. 백엔드 설정 및 실행
```bash
cd backend

# 설정 파일 준비 (application-secret.yml.sample 참고)
cp src/main/resources/application-secret.yml.sample src/main/resources/application-secret.yml
# application-secret.yml 파일을 실제 환경에 맞게 수정

# 의존성 설치 및 실행
mvn clean install
mvn spring-boot:run
```

### 4. 프론트엔드 설정 및 실행
```bash
cd frontend

# 환경 변수 설정
cp .env.example .env
# .env 파일을 실제 환경에 맞게 수정

# 의존성 설치 및 실행
npm install
npm run dev
```

### 5. 애플리케이션 접속
- **Frontend**: http://localhost:5173
- **Backend API**: http://localhost:8080
- **API 문서**: http://localhost:8080/swagger-ui.html
- **기본 로그인**: admin / password (초기 설정 후 변경 필요)

## 📁 프로젝트 구조

```
web-project-template/
├── 📁 backend/                 # Spring Boot 백엔드
│   ├── 📁 src/main/java/       # Java 소스 코드
│   └── 📁 src/main/resources/  # 설정 파일 및 리소스
├── 📁 frontend/                # React 프론트엔드
│   ├── 📁 src/components/      # 재사용 컴포넌트
│   ├── 📁 src/pages/          # 페이지 컴포넌트
│   └── 📁 src/services/       # API 서비스
├── 📁 database/               # 데이터베이스 스크립트
├── 📁 docs/                   # 개발자 문서
│   ├── 📁 frontend/           # 프론트엔드 개발 가이드
│   └── 📁 backend/            # 백엔드 개발 가이드
└── 📄 README.md               # 이 파일
```

## 📖 개발 가이드

새로운 개발자를 위한 상세한 개발 가이드가 준비되어 있습니다:

### 🎨 [Frontend 개발 가이드](docs/frontend/README.md)
- React 컴포넌트 개발
- Ant Design 활용법
- Redux 상태 관리
- API 통신 패턴
- 테스트 및 성능 최적화

### 🔧 [Backend 개발 가이드](docs/backend/README.md)
- Spring Boot API 개발
- MyBatis 데이터 접근
- JWT 보안 구현
- 데이터베이스 설계
- 테스트 및 모니터링

## 🔌 주요 API 엔드포인트

### 인증
- `POST /api/auth/login` - 로그인
- `POST /api/auth/refresh` - 토큰 갱신
- `GET /api/auth/profile` - 사용자 프로필

### 사용자 관리
- `GET /api/users` - 사용자 목록
- `POST /api/users` - 사용자 생성
- `PUT /api/users/{id}` - 사용자 수정
- `DELETE /api/users/{id}` - 사용자 삭제

### 시스템
- `GET /actuator/health` - 헬스체크
- `GET /swagger-ui.html` - API 문서

## 🧪 테스트

### 백엔드 테스트
```bash
cd backend
mvn test                    # 단위 테스트
mvn integration-test        # 통합 테스트
mvn jacoco:report          # 커버리지 리포트
```

### 프론트엔드 테스트
```bash
cd frontend
npm test                   # 단위 테스트
npm run test:coverage      # 커버리지 리포트
npm run test:e2e          # E2E 테스트
```

## 📦 빌드 및 배포

### 개발 빌드
```bash
# 백엔드
cd backend && mvn clean package

# 프론트엔드
cd frontend && npm run build
```

### 프로덕션 배포
```bash
# Docker를 이용한 배포 (선택사항)
docker-compose up -d

# 또는 전통적인 배포
# 1. JAR 파일 생성 (backend/target/)
# 2. React 빌드 파일 웹서버에 배포 (frontend/dist/)
```

## 🔒 보안 고려사항

- ✅ JWT 토큰 기반 인증
- ✅ CORS 설정 완료
- ✅ SQL 인젝션 방지
- ✅ XSS 방지
- ✅ 입력 검증 및 sanitization
- ✅ 보안 헤더 설정
- ✅ 감사 로그 기록

## 🤝 기여 방법

1. 이 저장소를 Fork 합니다
2. 새로운 기능 브랜치를 생성합니다 (`git checkout -b feature/AmazingFeature`)
3. 변경사항을 커밋합니다 (`git commit -m 'Add some AmazingFeature'`)
4. 브랜치에 푸시합니다 (`git push origin feature/AmazingFeature`)
5. Pull Request를 생성합니다

## 📝 라이선스

이 프로젝트는 MIT 라이선스 하에 배포됩니다. 자세한 내용은 [LICENSE](LICENSE) 파일을 참고하세요.

## 📞 지원 및 문의

- **이슈 리포트**: [GitHub Issues](https://github.com/your-username/web-project-template/issues)
- **기능 요청**: [GitHub Discussions](https://github.com/your-username/web-project-template/discussions)
- **문서**: [Wiki](https://github.com/your-username/web-project-template/wiki)

## 🎯 로드맵

- [ ] **v2.0**: GraphQL API 지원
- [ ] **v2.1**: 실시간 알림 시스템
- [ ] **v2.2**: 모바일 앱 (React Native)
- [ ] **v3.0**: 마이크로서비스 아키텍처

---

<div align="center">

**⭐ 이 프로젝트가 도움이 되었다면 Star를 눌러주세요! ⭐**

Made with ❤️ by [Your Name]

</div>