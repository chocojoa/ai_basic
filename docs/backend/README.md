# Backend 개발자 가이드

## 목차
1. [개발 환경 설정](#개발-환경-설정)
2. [프로젝트 구조](#프로젝트-구조)
3. [개발 워크플로우](#개발-워크플로우)
4. [데이터베이스 설계](#데이터베이스-설계)
5. [API 개발 가이드](#api-개발-가이드)
6. [보안 구현](#보안-구현)
7. [테스트](#테스트)
8. [로깅 및 모니터링](#로깅-및-모니터링)
9. [성능 최적화](#성능-최적화)
10. [빌드 및 배포](#빌드-및-배포)
11. [트러블슈팅](#트러블슈팅)

## 개발 환경 설정

### 필수 도구
- **Java**: OpenJDK 17 이상
- **Maven**: 3.8.x 이상
- **MySQL**: 9.1
- **IntelliJ IDEA**: 권장 IDE

### 데이터베이스 설정
```sql
-- 데이터베이스 생성
CREATE DATABASE your_database_name DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 사용자 생성 및 권한 부여
CREATE USER 'your_username'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON your_database_name.* TO 'your_username'@'localhost';
FLUSH PRIVILEGES;
```

### 프로젝트 설정
```bash
cd backend
mvn clean install
mvn spring-boot:run
```

### IDE 설정
#### IntelliJ IDEA 플러그인
- Spring Boot
- MyBatis X
- Lombok
- SonarLint
- CheckStyle-IDEA

#### application.yml 설정
```yaml
# 개발 환경
spring:
  profiles:
    active: dev
  datasource:
    url: jdbc:mysql://localhost:3306/your_database_name?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
    username: ${DB_USERNAME:your_username}
    password: ${DB_PASSWORD:your_password}
    driver-class-name: com.mysql.cj.jdbc.Driver
  
logging:
  level:
    com.your.project: DEBUG
    org.springframework.security: DEBUG

# 실제 설정은 application-secret.yml 파일에서 관리
# application-secret.yml.sample 파일을 참고하여 설정
```

## 프로젝트 구조

```
backend/
├── src/
│   ├── main/
│   │   ├── java/com/your/project/
│   │   │   ├── BasicProjectApplication.java    # 메인 애플리케이션
│   │   │   ├── config/                        # 설정 클래스
│   │   │   │   ├── SecurityConfig.java        # Spring Security 설정
│   │   │   │   ├── SwaggerConfig.java         # API 문서화 설정
│   │   │   │   ├── DatabaseConfig.java        # 데이터베이스 설정
│   │   │   │   └── ActuatorConfig.java        # 모니터링 설정
│   │   │   ├── controller/                    # REST 컨트롤러
│   │   │   │   ├── AuthController.java        # 인증 API
│   │   │   │   ├── UserController.java        # 사용자 관리 API
│   │   │   │   ├── RoleController.java        # 역할 관리 API
│   │   │   │   ├── MenuController.java        # 메뉴 관리 API
│   │   │   │   └── LogController.java         # 로그 관리 API
│   │   │   ├── domain/                        # 엔티티 클래스
│   │   │   │   ├── User.java                  # 사용자 엔티티
│   │   │   │   ├── Role.java                  # 역할 엔티티
│   │   │   │   ├── Menu.java                  # 메뉴 엔티티
│   │   │   │   └── SystemLog.java             # 로그 엔티티
│   │   │   ├── dto/                           # 데이터 전송 객체
│   │   │   │   ├── request/                   # 요청 DTO
│   │   │   │   ├── response/                  # 응답 DTO
│   │   │   │   └── common/                    # 공통 DTO
│   │   │   ├── mapper/                        # MyBatis 매퍼 인터페이스
│   │   │   │   ├── UserMapper.java
│   │   │   │   ├── RoleMapper.java
│   │   │   │   ├── MenuMapper.java
│   │   │   │   └── LogMapper.java
│   │   │   ├── security/                      # 보안 관련 클래스
│   │   │   │   ├── JwtTokenProvider.java      # JWT 토큰 제공자
│   │   │   │   ├── JwtAuthenticationFilter.java # JWT 인증 필터
│   │   │   │   └── CustomUserDetailsService.java # 사용자 상세 서비스
│   │   │   ├── service/                       # 비즈니스 로직
│   │   │   │   ├── AuthService.java
│   │   │   │   ├── UserService.java
│   │   │   │   ├── RoleService.java
│   │   │   │   ├── MenuService.java
│   │   │   │   └── LogService.java
│   │   │   ├── exception/                     # 예외 처리
│   │   │   │   ├── GlobalExceptionHandler.java
│   │   │   │   ├── BusinessException.java
│   │   │   │   └── ErrorCode.java
│   │   │   └── util/                          # 유틸리티 클래스
│   │   │       ├── DateUtil.java
│   │   │       ├── PasswordUtil.java
│   │   │       └── SecurityUtil.java
│   │   └── resources/
│   │       ├── mapper/                        # MyBatis XML 매퍼
│   │       │   ├── UserMapper.xml
│   │       │   ├── RoleMapper.xml
│   │       │   ├── MenuMapper.xml
│   │       │   └── LogMapper.xml
│   │       ├── application.yml                # 기본 설정
│   │       ├── application-dev.yml            # 개발 환경 설정
│   │       ├── application-prod.yml           # 운영 환경 설정
│   │       └── logback-spring.xml             # 로깅 설정
│   └── test/                                  # 테스트 코드
│       ├── java/
│       └── resources/
├── pom.xml                                    # Maven 설정
└── README.md
```

## 개발 워크플로우

### 1. 새로운 API 개발 절차

#### Step 1: 도메인 모델 정의
```java
// domain/User.java
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    private Long id;
    private String username;
    private String email;
    private String password;
    private String name;
    private String phone;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdBy;
    private Long updatedBy;
}
```

#### Step 2: DTO 클래스 작성
```java
// dto/request/UserCreateRequest.java
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "사용자 생성 요청")
public class UserCreateRequest {
    @NotBlank(message = "사용자명은 필수입니다")
    @Size(min = 3, max = 50, message = "사용자명은 3-50자 사이여야 합니다")
    @ApiModelProperty(value = "사용자명", required = true, example = "john_doe")
    private String username;

    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    @ApiModelProperty(value = "이메일", required = true, example = "john@example.com")
    private String email;

    @NotBlank(message = "비밀번호는 필수입니다")
    @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다")
    @ApiModelProperty(value = "비밀번호", required = true)
    private String password;

    @NotBlank(message = "이름은 필수입니다")
    @ApiModelProperty(value = "이름", required = true, example = "홍길동")
    private String name;
    
    @ApiModelProperty(value = "전화번호", example = "010-1234-5678")
    private String phone;
}
```

#### Step 3: 매퍼 인터페이스 작성
```java
// mapper/UserMapper.java
@Mapper
public interface UserMapper {
    List<User> findAll(UserSearchRequest request);
    Long countAll(UserSearchRequest request);
    User findById(Long id);
    User findByUsername(String username);
    User findByEmail(String email);
    void insert(User user);
    void update(User user);
    void deleteById(Long id);
    void updateStatus(Long id, String status);
    List<User> findByRoleId(Long roleId);
}
```

#### Step 4: MyBatis XML 매퍼 작성
```xml
<!-- mapper/UserMapper.xml -->
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" 
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">

<mapper namespace="com.your.project.mapper.UserMapper">
    <!-- 결과 매핑 -->
    <resultMap id="UserResultMap" type="com.your.project.domain.User">
        <id property="id" column="id"/>
        <result property="username" column="username"/>
        <result property="email" column="email"/>
        <result property="password" column="password"/>
        <result property="name" column="name"/>
        <result property="phone" column="phone"/>
        <result property="status" column="status"/>
        <result property="createdAt" column="created_at"/>
        <result property="updatedAt" column="updated_at"/>
        <result property="createdBy" column="created_by"/>
        <result property="updatedBy" column="updated_by"/>
    </resultMap>

    <!-- 공통 컬럼 -->
    <sql id="userColumns">
        id, username, email, password, name, phone, status,
        created_at, updated_at, created_by, updated_by
    </sql>

    <!-- 검색 조건 -->
    <sql id="searchConditions">
        <where>
            <if test="username != null and username != ''">
                AND username LIKE CONCAT('%', #{username}, '%')
            </if>
            <if test="email != null and email != ''">
                AND email LIKE CONCAT('%', #{email}, '%')
            </if>
            <if test="name != null and name != ''">
                AND name LIKE CONCAT('%', #{name}, '%')
            </if>
            <if test="status != null and status != ''">
                AND status = #{status}
            </if>
            <if test="startDate != null">
                AND created_at >= #{startDate}
            </if>
            <if test="endDate != null">
                AND created_at &lt;= #{endDate}
            </if>
        </where>
    </sql>

    <!-- 전체 조회 -->
    <select id="findAll" resultMap="UserResultMap">
        SELECT <include refid="userColumns"/>
        FROM users
        <include refid="searchConditions"/>
        <if test="sortBy != null and sortBy != ''">
            ORDER BY ${sortBy}
            <if test="sortDir != null and sortDir != ''">
                ${sortDir}
            </if>
        </if>
        <if test="limit != null and limit > 0">
            LIMIT #{offset}, #{limit}
        </if>
    </select>

    <!-- 전체 개수 -->
    <select id="countAll" resultType="Long">
        SELECT COUNT(*)
        FROM users
        <include refid="searchConditions"/>
    </select>

    <!-- ID로 조회 -->
    <select id="findById" resultMap="UserResultMap">
        SELECT <include refid="userColumns"/>
        FROM users
        WHERE id = #{id}
    </select>

    <!-- 사용자명으로 조회 -->
    <select id="findByUsername" resultMap="UserResultMap">
        SELECT <include refid="userColumns"/>
        FROM users
        WHERE username = #{username}
    </select>

    <!-- 삽입 -->
    <insert id="insert" useGeneratedKeys="true" keyProperty="id">
        INSERT INTO users (
            username, email, password, name, phone, status,
            created_at, updated_at, created_by, updated_by
        ) VALUES (
            #{username}, #{email}, #{password}, #{name}, #{phone}, #{status},
            NOW(), NOW(), #{createdBy}, #{updatedBy}
        )
    </insert>

    <!-- 수정 -->
    <update id="update">
        UPDATE users SET
            email = #{email},
            name = #{name},
            phone = #{phone},
            status = #{status},
            updated_at = NOW(),
            updated_by = #{updatedBy}
        WHERE id = #{id}
    </update>

    <!-- 삭제 -->
    <delete id="deleteById">
        DELETE FROM users WHERE id = #{id}
    </delete>
</mapper>
```

#### Step 5: 서비스 계층 구현
```java
// service/UserService.java
@Service
@Transactional
@Slf4j
public class UserService {
    
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final LogService logService;

    public UserService(UserMapper userMapper, 
                      PasswordEncoder passwordEncoder,
                      LogService logService) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.logService = logService;
    }

    /**
     * 사용자 목록 조회 (페이징)
     */
    @Transactional(readOnly = true)
    public PagedResponse<UserResponse> getUsers(UserSearchRequest request) {
        log.info("사용자 목록 조회 시작: {}", request);
        
        // 총 개수 조회
        Long totalCount = userMapper.countAll(request);
        
        // 목록 조회
        List<User> users = userMapper.findAll(request);
        List<UserResponse> userResponses = users.stream()
                .map(this::convertToUserResponse)
                .collect(Collectors.toList());
        
        // 페이징 정보 생성
        PaginationInfo pagination = PaginationInfo.builder()
                .page(request.getPage())
                .size(request.getSize())
                .total(totalCount)
                .build();
        
        log.info("사용자 목록 조회 완료: {} 건", userResponses.size());
        
        return new PagedResponse<>(userResponses, pagination);
    }

    /**
     * 사용자 상세 조회
     */
    @Transactional(readOnly = true)
    public UserDetailResponse getUserById(Long id) {
        log.info("사용자 상세 조회: {}", id);
        
        User user = userMapper.findById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        
        return convertToUserDetailResponse(user);
    }

    /**
     * 사용자 생성
     */
    public UserResponse createUser(UserCreateRequest request) {
        log.info("사용자 생성 시작: {}", request.getUsername());
        
        // 중복 검사
        validateDuplicateUser(request.getUsername(), request.getEmail());
        
        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        
        // 사용자 엔티티 생성
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(encodedPassword)
                .name(request.getName())
                .phone(request.getPhone())
                .status("ACTIVE")
                .createdBy(SecurityUtil.getCurrentUserId())
                .updatedBy(SecurityUtil.getCurrentUserId())
                .build();
        
        // 데이터베이스 저장
        userMapper.insert(user);
        
        // 로그 기록
        logService.saveSystemLog("USER_CREATED", 
                String.format("사용자가 생성되었습니다: %s", user.getUsername()),
                user.getId().toString());
        
        log.info("사용자 생성 완료: {} (ID: {})", user.getUsername(), user.getId());
        
        return convertToUserResponse(user);
    }

    /**
     * 사용자 수정
     */
    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        log.info("사용자 수정 시작: {}", id);
        
        User existingUser = userMapper.findById(id);
        if (existingUser == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        
        // 이메일 중복 검사 (자신 제외)
        if (!existingUser.getEmail().equals(request.getEmail())) {
            User duplicateEmailUser = userMapper.findByEmail(request.getEmail());
            if (duplicateEmailUser != null) {
                throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
            }
        }
        
        // 사용자 정보 업데이트
        User user = User.builder()
                .id(id)
                .email(request.getEmail())
                .name(request.getName())
                .phone(request.getPhone())
                .status(request.getStatus())
                .updatedBy(SecurityUtil.getCurrentUserId())
                .build();
        
        userMapper.update(user);
        
        // 로그 기록
        logService.saveSystemLog("USER_UPDATED", 
                String.format("사용자 정보가 수정되었습니다: %s", existingUser.getUsername()),
                id.toString());
        
        log.info("사용자 수정 완료: {}", id);
        
        return convertToUserResponse(userMapper.findById(id));
    }

    /**
     * 사용자 삭제
     */
    public void deleteUser(Long id) {
        log.info("사용자 삭제 시작: {}", id);
        
        User user = userMapper.findById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        
        // 관리자 계정 삭제 방지
        if ("admin".equals(user.getUsername())) {
            throw new BusinessException(ErrorCode.ADMIN_USER_CANNOT_BE_DELETED);
        }
        
        userMapper.deleteById(id);
        
        // 로그 기록
        logService.saveSystemLog("USER_DELETED", 
                String.format("사용자가 삭제되었습니다: %s", user.getUsername()),
                id.toString());
        
        log.info("사용자 삭제 완료: {}", id);
    }

    private void validateDuplicateUser(String username, String email) {
        if (userMapper.findByUsername(username) != null) {
            throw new BusinessException(ErrorCode.USERNAME_ALREADY_EXISTS);
        }
        if (userMapper.findByEmail(email) != null) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
    }

    private UserResponse convertToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .name(user.getName())
                .phone(user.getPhone())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    private UserDetailResponse convertToUserDetailResponse(User user) {
        // 상세 정보 포함한 응답 객체 생성
        return UserDetailResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .name(user.getName())
                .phone(user.getPhone())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
```

#### Step 6: 컨트롤러 구현
```java
// controller/UserController.java
@RestController
@RequestMapping("/api/users")
@Api(tags = "사용자 관리")
@Slf4j
@Validated
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 사용자 목록 조회
     */
    @GetMapping
    @ApiOperation(value = "사용자 목록 조회", notes = "페이징과 검색 조건을 지원합니다.")
    @ApiResponses({
        @ApiResponse(code = 200, message = "조회 성공"),
        @ApiResponse(code = 400, message = "잘못된 요청"),
        @ApiResponse(code = 401, message = "인증 실패"),
        @ApiResponse(code = 403, message = "권한 없음")
    })
    public ResponseEntity<ApiResponse<PagedResponse<UserResponse>>> getUsers(
            @Valid @ModelAttribute UserSearchRequest request) {
        
        log.info("사용자 목록 조회 요청: {}", request);
        
        PagedResponse<UserResponse> users = userService.getUsers(request);
        
        return ResponseEntity.ok(
            ApiResponse.<PagedResponse<UserResponse>>builder()
                .success(true)
                .message("사용자 목록 조회 성공")
                .data(users)
                .build()
        );
    }

    /**
     * 사용자 상세 조회
     */
    @GetMapping("/{id}")
    @ApiOperation(value = "사용자 상세 조회")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "id", value = "사용자 ID", required = true, 
                         dataType = "long", paramType = "path")
    })
    public ResponseEntity<ApiResponse<UserDetailResponse>> getUserById(
            @PathVariable @Min(1) Long id) {
        
        log.info("사용자 상세 조회 요청: {}", id);
        
        UserDetailResponse user = userService.getUserById(id);
        
        return ResponseEntity.ok(
            ApiResponse.<UserDetailResponse>builder()
                .success(true)
                .message("사용자 상세 조회 성공")
                .data(user)
                .build()
        );
    }

    /**
     * 사용자 생성
     */
    @PostMapping
    @ApiOperation(value = "사용자 생성")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
            @Valid @RequestBody UserCreateRequest request) {
        
        log.info("사용자 생성 요청: {}", request.getUsername());
        
        UserResponse user = userService.createUser(request);
        
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.<UserResponse>builder()
                .success(true)
                .message("사용자 생성 성공")
                .data(user)
                .build());
    }

    /**
     * 사용자 수정
     */
    @PutMapping("/{id}")
    @ApiOperation(value = "사용자 수정")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable @Min(1) Long id,
            @Valid @RequestBody UserUpdateRequest request) {
        
        log.info("사용자 수정 요청: {}", id);
        
        UserResponse user = userService.updateUser(id, request);
        
        return ResponseEntity.ok(
            ApiResponse.<UserResponse>builder()
                .success(true)
                .message("사용자 수정 성공")
                .data(user)
                .build()
        );
    }

    /**
     * 사용자 삭제
     */
    @DeleteMapping("/{id}")
    @ApiOperation(value = "사용자 삭제")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @PathVariable @Min(1) Long id) {
        
        log.info("사용자 삭제 요청: {}", id);
        
        userService.deleteUser(id);
        
        return ResponseEntity.ok(
            ApiResponse.<Void>builder()
                .success(true)
                .message("사용자 삭제 성공")
                .build()
        );
    }

    /**
     * 사용자 상태 변경
     */
    @PatchMapping("/{id}/status")
    @ApiOperation(value = "사용자 상태 변경")
    public ResponseEntity<ApiResponse<Void>> updateUserStatus(
            @PathVariable @Min(1) Long id,
            @Valid @RequestBody UserStatusUpdateRequest request) {
        
        log.info("사용자 상태 변경 요청: {} -> {}", id, request.getStatus());
        
        userService.updateUserStatus(id, request.getStatus());
        
        return ResponseEntity.ok(
            ApiResponse.<Void>builder()
                .success(true)
                .message("사용자 상태 변경 성공")
                .build()
        );
    }
}
```

### 2. 코드 품질 관리

#### Maven 설정 (pom.xml)
```xml
<plugin>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-maven-plugin</artifactId>
</plugin>

<!-- 테스트 커버리지 -->
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.7</version>
    <executions>
        <execution>
            <goals>
                <goal>prepare-agent</goal>
            </goals>
        </execution>
        <execution>
            <id>report</id>
            <phase>test</phase>
            <goals>
                <goal>report</goal>
            </goals>
        </execution>
    </executions>
</plugin>

<!-- 정적 분석 -->
<plugin>
    <groupId>org.sonarsource.scanner.maven</groupId>
    <artifactId>sonar-maven-plugin</artifactId>
    <version>3.9.1.2184</version>
</plugin>
```

## 데이터베이스 설계

### ERD 개념
```
Users (사용자)
├── id (PK)
├── username (UNIQUE)
├── email (UNIQUE)
├── password
├── name
├── phone
├── status
├── created_at
├── updated_at
├── created_by
└── updated_by

Roles (역할)
├── id (PK)
├── name (UNIQUE)
├── description
├── parent_id (FK -> Roles.id)
├── level
├── sort_order
├── created_at
├── updated_at
├── created_by
└── updated_by

Menus (메뉴)
├── id (PK)
├── name
├── menu_code (UNIQUE)
├── url
├── icon
├── parent_id (FK -> Menus.id)
├── level
├── sort_order
├── status
├── created_at
├── updated_at
├── created_by
└── updated_by

User_Roles (사용자-역할 매핑)
├── id (PK)
├── user_id (FK -> Users.id)
├── role_id (FK -> Roles.id)
├── created_at
└── created_by

Role_Menus (역할-메뉴 권한 매핑)
├── id (PK)
├── role_id (FK -> Roles.id)
├── menu_id (FK -> Menus.id)
├── can_read
├── can_write
├── can_delete
├── created_at
└── created_by

System_Logs (시스템 로그)
├── id (PK)
├── log_type
├── action
├── message
├── user_id (FK -> Users.id)
├── ip_address
├── user_agent
├── request_url
├── request_method
├── response_status
├── execution_time
├── additional_data (JSON)
└── created_at
```

### 인덱스 전략
```sql
-- 성능 최적화를 위한 인덱스
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_status ON users(status);
CREATE INDEX idx_users_created_at ON users(created_at);

CREATE INDEX idx_roles_parent_id ON roles(parent_id);
CREATE INDEX idx_roles_level ON roles(level);
CREATE INDEX idx_roles_name ON roles(name);

CREATE INDEX idx_menus_parent_id ON menus(parent_id);
CREATE INDEX idx_menus_menu_code ON menus(menu_code);
CREATE INDEX idx_menus_level ON menus(level);
CREATE INDEX idx_menus_status ON menus(status);

CREATE INDEX idx_user_roles_user_id ON user_roles(user_id);
CREATE INDEX idx_user_roles_role_id ON user_roles(role_id);

CREATE INDEX idx_role_menus_role_id ON role_menus(role_id);
CREATE INDEX idx_role_menus_menu_id ON role_menus(menu_id);

CREATE INDEX idx_system_logs_user_id ON system_logs(user_id);
CREATE INDEX idx_system_logs_log_type ON system_logs(log_type);
CREATE INDEX idx_system_logs_action ON system_logs(action);
CREATE INDEX idx_system_logs_created_at ON system_logs(created_at);
```

## API 개발 가이드

### REST API 설계 원칙

#### URL 설계
```
GET    /api/users              # 사용자 목록 조회
GET    /api/users/{id}         # 사용자 상세 조회
POST   /api/users              # 사용자 생성
PUT    /api/users/{id}         # 사용자 전체 수정
PATCH  /api/users/{id}         # 사용자 부분 수정
DELETE /api/users/{id}         # 사용자 삭제

GET    /api/users/{id}/roles   # 사용자의 역할 목록
POST   /api/users/{id}/roles   # 사용자에게 역할 할당
DELETE /api/users/{id}/roles/{roleId} # 사용자 역할 제거
```

#### HTTP 상태 코드 활용
```java
// 성공 응답
200 OK          # 조회, 수정 성공
201 Created     # 생성 성공
204 No Content  # 삭제 성공

// 클라이언트 에러
400 Bad Request     # 요청 데이터 오류
401 Unauthorized    # 인증 실패
403 Forbidden       # 권한 없음
404 Not Found       # 리소스 없음
409 Conflict        # 데이터 충돌 (중복 등)
422 Unprocessable Entity # 검증 실패

// 서버 에러
500 Internal Server Error # 서버 내부 오류
```

### 공통 응답 구조
```java
// dto/common/ApiResponse.java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "API 공통 응답")
public class ApiResponse<T> {
    
    @ApiModelProperty(value = "성공 여부", required = true)
    private boolean success;
    
    @ApiModelProperty(value = "응답 메시지", required = true)
    private String message;
    
    @ApiModelProperty(value = "응답 데이터")
    private T data;
    
    @ApiModelProperty(value = "에러 정보")
    private ErrorInfo error;
    
    @ApiModelProperty(value = "타임스탬프", required = true)
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> success(String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .build();
    }

    public static <T> ApiResponse<T> error(String message, ErrorInfo error) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .error(error)
                .build();
    }
}
```

### 페이징 처리
```java
// dto/common/PagedResponse.java
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "페이징 응답")
public class PagedResponse<T> {
    
    @ApiModelProperty(value = "데이터 목록", required = true)
    private List<T> content;
    
    @ApiModelProperty(value = "페이징 정보", required = true)
    private PaginationInfo pagination;

    public PagedResponse(List<T> content, int page, int size, long totalElements) {
        this.content = content;
        this.pagination = PaginationInfo.builder()
                .page(page)
                .size(size)
                .total(totalElements)
                .totalPages((int) Math.ceil((double) totalElements / size))
                .hasNext(page < (int) Math.ceil((double) totalElements / size) - 1)
                .hasPrevious(page > 0)
                .build();
    }
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "페이징 정보")
public class PaginationInfo {
    
    @ApiModelProperty(value = "현재 페이지 (0부터 시작)", required = true)
    private int page;
    
    @ApiModelProperty(value = "페이지 크기", required = true)
    private int size;
    
    @ApiModelProperty(value = "전체 요소 수", required = true)
    private long total;
    
    @ApiModelProperty(value = "전체 페이지 수", required = true)
    private int totalPages;
    
    @ApiModelProperty(value = "다음 페이지 존재 여부", required = true)
    private boolean hasNext;
    
    @ApiModelProperty(value = "이전 페이지 존재 여부", required = true)
    private boolean hasPrevious;
}
```

## 보안 구현

### JWT 기반 인증

#### JWT 토큰 제공자
```java
// security/JwtTokenProvider.java
@Component
@Slf4j
public class JwtTokenProvider {

    private final Key key;
    private final long tokenValidityInMilliseconds;
    private final long refreshTokenValidityInMilliseconds;

    public JwtTokenProvider(@Value("${jwt.secret}") String secret,
                           @Value("${jwt.token-validity-in-seconds}") long tokenValidityInSeconds) {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.tokenValidityInMilliseconds = tokenValidityInSeconds * 1000;
        this.refreshTokenValidityInMilliseconds = tokenValidityInSeconds * 1000 * 7; // 7배 더 길게
    }

    /**
     * 액세스 토큰 생성
     */
    public String createToken(Authentication authentication) {
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        long now = System.currentTimeMillis();
        Date validity = new Date(now + this.tokenValidityInMilliseconds);

        return Jwts.builder()
                .setSubject(authentication.getName())
                .claim("auth", authorities)
                .claim("type", "access")
                .setIssuedAt(new Date(now))
                .setExpiration(validity)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 리프레시 토큰 생성
     */
    public String createRefreshToken(Authentication authentication) {
        long now = System.currentTimeMillis();
        Date validity = new Date(now + this.refreshTokenValidityInMilliseconds);

        return Jwts.builder()
                .setSubject(authentication.getName())
                .claim("type", "refresh")
                .setIssuedAt(new Date(now))
                .setExpiration(validity)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 토큰에서 인증 정보 추출
     */
    public Authentication getAuthentication(String token) {
        Claims claims = parseClaims(token);

        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(claims.get("auth").toString().split(","))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

        User principal = new User(claims.getSubject(), "", authorities);
        return new UsernamePasswordAuthenticationToken(principal, "", authorities);
    }

    /**
     * 토큰 유효성 검증
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.info("잘못된 JWT 서명입니다.");
        } catch (ExpiredJwtException e) {
            log.info("만료된 JWT 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            log.info("지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            log.info("JWT 토큰이 잘못되었습니다.");
        }
        return false;
    }

    private Claims parseClaims(String token) {
        try {
            return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }
}
```

#### Security 설정
```java
// config/SecurityConfig.java
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider tokenProvider;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            
            .exceptionHandling()
            .authenticationEntryPoint(jwtAuthenticationEntryPoint)
            .accessDeniedHandler(jwtAccessDeniedHandler)

            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/menus/public").permitAll()
                .anyRequest().authenticated()
            )

            .apply(new JwtSecurityConfig(tokenProvider));

        return http.build();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring()
                .requestMatchers("/h2-console/**", "/favicon.ico", "/error");
    }
}
```

### 권한 기반 접근 제어
```java
// 메서드 레벨 보안
@PreAuthorize("hasRole('ADMIN')")
public void adminOnlyMethod() {
    // 관리자만 접근 가능
}

@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
public void userOrAdminMethod() {
    // 사용자 또는 관리자 접근 가능
}

@PreAuthorize("@securityService.canAccessUser(#userId)")
public void accessSpecificUser(Long userId) {
    // 특정 사용자 접근 권한 검사
}
```

## 테스트

### 단위 테스트
```java
// service/UserServiceTest.java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private LogService logService;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("사용자 생성 성공")
    void createUser_Success() {
        // Given
        UserCreateRequest request = UserCreateRequest.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .name("테스트 사용자")
                .build();

        when(userMapper.findByUsername("testuser")).thenReturn(null);
        when(userMapper.findByEmail("test@example.com")).thenReturn(null);
        when(passwordEncoder.encode("password123")).thenReturn("encoded_password");
        
        // When
        UserResponse result = userService.createUser(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        
        verify(userMapper).insert(any(User.class));
        verify(logService).saveSystemLog(eq("USER_CREATED"), anyString(), anyString());
    }

    @Test
    @DisplayName("중복 사용자명으로 사용자 생성 실패")
    void createUser_DuplicateUsername_ThrowsException() {
        // Given
        UserCreateRequest request = UserCreateRequest.builder()
                .username("existinguser")
                .email("test@example.com")
                .password("password123")
                .name("테스트 사용자")
                .build();

        User existingUser = User.builder()
                .username("existinguser")
                .build();

        when(userMapper.findByUsername("existinguser")).thenReturn(existingUser);

        // When & Then
        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USERNAME_ALREADY_EXISTS);
    }
}
```

### 통합 테스트
```java
// controller/UserControllerTest.java
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestMethodOrder(OrderAnnotation.class)
@Transactional
@Rollback
class UserControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserMapper userMapper;

    @Test
    @Order(1)
    @DisplayName("사용자 목록 조회 API 테스트")
    void getUsersApi_Success() {
        // Given
        String url = "/api/users?page=0&size=10";
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getValidToken());
        HttpEntity<String> entity = new HttpEntity<>(headers);

        // When
        ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, String.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("success\":true");
    }

    @Test
    @Order(2)
    @DisplayName("사용자 생성 API 테스트")
    void createUserApi_Success() {
        // Given
        UserCreateRequest request = UserCreateRequest.builder()
                .username("newuser")
                .email("newuser@example.com")
                .password("password123")
                .name("새 사용자")
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getValidToken());
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<UserCreateRequest> entity = new HttpEntity<>(request, headers);

        // When
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/users", HttpMethod.POST, entity, String.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).contains("success\":true");
        
        // 데이터베이스 확인
        User createdUser = userMapper.findByUsername("newuser");
        assertThat(createdUser).isNotNull();
        assertThat(createdUser.getEmail()).isEqualTo("newuser@example.com");
    }

    private String getValidToken() {
        // 테스트용 토큰 생성 로직
        return "valid_jwt_token";
    }
}
```

### 테스트 설정
```yaml
# application-test.yml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
    username: sa
    password: 
  
  h2:
    console:
      enabled: true
      
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
    
logging:
  level:
    com.your.project: DEBUG
    org.springframework.security: DEBUG
```

## 로깅 및 모니터링

### Logback 설정
```xml
<!-- logback-spring.xml -->
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <springProfile name="dev">
        <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
            <encoder>
                <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
            </encoder>
        </appender>
        
        <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
            <file>logs/application.log</file>
            <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
                <fileNamePattern>logs/application.%d{yyyy-MM-dd}.%i.log</fileNamePattern>
                <timeBasedFileNamingAndTriggeringPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP">
                    <maxFileSize>100MB</maxFileSize>
                </timeBasedFileNamingAndTriggeringPolicy>
                <maxHistory>30</maxHistory>
            </rollingPolicy>
            <encoder>
                <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
            </encoder>
        </appender>

        <root level="INFO">
            <appender-ref ref="CONSOLE"/>
            <appender-ref ref="FILE"/>
        </root>
        
        <logger name="com.basic.project" level="DEBUG"/>
    </springProfile>

    <springProfile name="prod">
        <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
            <file>logs/application.log</file>
            <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
                <fileNamePattern>logs/application.%d{yyyy-MM-dd}.%i.log</fileNamePattern>
                <timeBasedFileNamingAndTriggeringPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP">
                    <maxFileSize>100MB</maxFileSize>
                </timeBasedFileNamingAndTriggeringPolicy>
                <maxHistory>30</maxHistory>
            </rollingPolicy>
            <encoder>
                <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
            </encoder>
        </appender>

        <root level="WARN">
            <appender-ref ref="FILE"/>
        </root>
        
        <logger name="com.basic.project" level="INFO"/>
    </springProfile>
</configuration>
```

### 커스텀 로깅 서비스
```java
// service/LogService.java
@Service
@Transactional
@Slf4j
public class LogService {

    private final LogMapper logMapper;
    private final HttpServletRequest request;

    public LogService(LogMapper logMapper, 
                     @Lazy HttpServletRequest request) {
        this.logMapper = logMapper;
        this.request = request;
    }

    /**
     * 시스템 로그 저장
     */
    public void saveSystemLog(String action, String message, String targetId) {
        try {
            SystemLog systemLog = SystemLog.builder()
                    .logType("SYSTEM")
                    .action(action)
                    .message(message)
                    .userId(SecurityUtil.getCurrentUserId())
                    .ipAddress(getClientIpAddress())
                    .userAgent(request.getHeader("User-Agent"))
                    .requestUrl(request.getRequestURI())
                    .requestMethod(request.getMethod())
                    .additionalData(targetId)
                    .build();

            logMapper.insert(systemLog);
            log.info("시스템 로그 저장: {} - {}", action, message);
        } catch (Exception e) {
            log.error("시스템 로그 저장 실패: {}", e.getMessage(), e);
        }
    }

    /**
     * 보안 로그 저장
     */
    public void saveSecurityLog(String action, String message, String targetId) {
        try {
            SystemLog securityLog = SystemLog.builder()
                    .logType("SECURITY")
                    .action(action)
                    .message(message)
                    .userId(SecurityUtil.getCurrentUserId())
                    .ipAddress(getClientIpAddress())
                    .userAgent(request.getHeader("User-Agent"))
                    .requestUrl(request.getRequestURI())
                    .requestMethod(request.getMethod())
                    .additionalData(targetId)
                    .build();

            logMapper.insert(securityLog);
            log.warn("보안 로그: {} - {}", action, message);
        } catch (Exception e) {
            log.error("보안 로그 저장 실패: {}", e.getMessage(), e);
        }
    }

    private String getClientIpAddress() {
        String[] headers = {
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"
        };

        for (String header : headers) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                return ip.split(",")[0];
            }
        }

        return request.getRemoteAddr();
    }
}
```

## 성능 최적화

### 데이터베이스 최적화

#### 연결 풀 설정
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      idle-timeout: 300000
      max-lifetime: 1200000
      connection-timeout: 20000
      validation-timeout: 3000
      leak-detection-threshold: 60000
```

#### 쿼리 최적화
```xml
<!-- N+1 문제 해결 -->
<select id="findUsersWithRoles" resultMap="UserWithRolesResultMap">
    SELECT u.*, r.id as role_id, r.name as role_name
    FROM users u
    LEFT JOIN user_roles ur ON u.id = ur.user_id
    LEFT JOIN roles r ON ur.role_id = r.id
    WHERE u.status = 'ACTIVE'
</select>

<!-- 배치 인서트 -->
<insert id="insertBatch">
    INSERT INTO users (username, email, password, name, phone, status, created_at, created_by)
    VALUES
    <foreach collection="users" item="user" separator=",">
        (#{user.username}, #{user.email}, #{user.password}, 
         #{user.name}, #{user.phone}, #{user.status}, NOW(), #{user.createdBy})
    </foreach>
</insert>
```

### 캐싱 전략
```java
// config/CacheConfig.java
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(Duration.ofMinutes(10))
                .recordStats());
        return cacheManager;
    }
}

// 서비스에서 캐시 사용
@Cacheable(value = "users", key = "#id")
public UserResponse getUserById(Long id) {
    // 캐시되는 메서드
}

@CacheEvict(value = "users", key = "#id")
public void updateUser(Long id, UserUpdateRequest request) {
    // 캐시 무효화
}
```

## 빌드 및 배포

### Maven 프로파일 설정
```xml
<profiles>
    <profile>
        <id>dev</id>
        <activation>
            <activeByDefault>true</activeByDefault>
        </activation>
        <properties>
            <spring.profiles.active>dev</spring.profiles.active>
        </properties>
    </profile>
    
    <profile>
        <id>prod</id>
        <properties>
            <spring.profiles.active>prod</spring.profiles.active>
        </properties>
    </profile>
</profiles>
```

### Docker 설정
```dockerfile
# Dockerfile
FROM openjdk:17-jre-slim

WORKDIR /app

COPY target/basic-project-*.jar app.jar

EXPOSE 8080

ENV SPRING_PROFILES_ACTIVE=prod

ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 빌드 스크립트
```bash
#!/bin/bash
# build.sh

echo "Building Basic Project..."

# 테스트 실행
mvn clean test

if [ $? -eq 0 ]; then
    echo "Tests passed. Building JAR..."
    mvn clean package -DskipTests
    
    if [ $? -eq 0 ]; then
        echo "Build successful!"
        echo "JAR file: target/basic-project-*.jar"
    else
        echo "Build failed!"
        exit 1
    fi
else
    echo "Tests failed!"
    exit 1
fi
```

## 트러블슈팅

### 일반적인 문제들

#### 1. 데이터베이스 연결 문제
```yaml
# 타임존 문제 해결
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/basic_project?serverTimezone=UTC&useSSL=false
```

#### 2. JWT 토큰 관련 문제
```java
// 토큰 만료 처리
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    
    @Override
    public void commence(HttpServletRequest request, 
                        HttpServletResponse response,
                        AuthenticationException authException) throws IOException {
        
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json;charset=UTF-8");
        
        String json = "{\"success\":false,\"message\":\"인증이 필요합니다.\",\"error\":\"UNAUTHORIZED\"}";
        response.getWriter().write(json);
    }
}
```

#### 3. MyBatis 매핑 문제
```xml
<!-- 결과 매핑 문제 해결 -->
<resultMap id="UserResultMap" type="com.basic.project.domain.User">
    <id property="id" column="id"/>
    <result property="createdAt" column="created_at" javaType="java.time.LocalDateTime"/>
    <result property="updatedAt" column="updated_at" javaType="java.time.LocalDateTime"/>
</resultMap>
```

#### 4. 성능 문제 진단
```java
// 메서드 실행 시간 측정
@Component
@Aspect
@Slf4j
public class PerformanceAspect {
    
    @Around("@annotation(Timed)")
    public Object measureTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        
        try {
            return joinPoint.proceed();
        } finally {
            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;
            
            log.info("Method {} executed in {} ms", 
                    joinPoint.getSignature().getName(), executionTime);
        }
    }
}
```

### 로그 분석 도구
```bash
# 에러 로그 분석
grep "ERROR" logs/application.log | tail -20

# 특정 사용자 활동 추적
grep "user_id=123" logs/application.log

# API 응답 시간 분석
grep "execution_time" logs/application.log | awk '{print $NF}' | sort -n
```

### 성능 모니터링
```java
// 커스텀 메트릭
@Component
public class CustomMetrics {
    
    private final MeterRegistry meterRegistry;
    private final Counter userLoginCounter;
    private final Timer apiResponseTimer;

    public CustomMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.userLoginCounter = Counter.builder("user.login.count")
                .description("Number of user logins")
                .register(meterRegistry);
        this.apiResponseTimer = Timer.builder("api.response.time")
                .description("API response time")
                .register(meterRegistry);
    }

    public void incrementLoginCount() {
        userLoginCounter.increment();
    }

    public void recordApiResponseTime(Duration duration) {
        apiResponseTimer.record(duration);
    }
}
```