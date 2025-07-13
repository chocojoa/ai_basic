# 메뉴 권한 어노테이션 사용법

이 프로젝트에서는 3가지 방법으로 메뉴 권한을 확인할 수 있습니다.

## 1. 기본 @PreAuthorize 방식 (가장 간단)

```java
@GetMapping("/users")
@PreAuthorize("@menuPermissionService.canReadUsers()")
public ResponseEntity<List<User>> getAllUsers() {
    // 구현
}

@PostMapping("/users")
@PreAuthorize("@menuPermissionService.canManageUsers()")
public ResponseEntity<User> createUser(@RequestBody User user) {
    // 구현
}

@DeleteMapping("/users/{id}")
@PreAuthorize("@menuPermissionService.canDeleteUsers()")
public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
    // 구현
}
```

## 2. 일반적인 권한 메서드 방식

```java
@GetMapping("/dashboard")
@PreAuthorize("@menuPermissionService.canRead('DASHBOARD')")
public ResponseEntity<Dashboard> getDashboard() {
    // 구현
}

@PutMapping("/menus/{id}")
@PreAuthorize("@menuPermissionService.canWrite('MENU_MANAGEMENT')")
public ResponseEntity<Menu> updateMenu(@PathVariable Long id, @RequestBody Menu menu) {
    // 구현
}

@DeleteMapping("/logs/{id}")
@PreAuthorize("@menuPermissionService.canDelete('LOG_MANAGEMENT')")
public ResponseEntity<Void> deleteLog(@PathVariable Long id) {
    // 구현
}
```

## 3. 커스텀 어노테이션 방식 (가장 깔끔)

### 3-1. 특화 어노테이션 (권장)

```java
import com.basic.project.annotation.MenuPermissions.*;

@GetMapping("/users")
@ReadUsers
public ResponseEntity<List<User>> getAllUsers() {
    // 구현
}

@PostMapping("/users")
@ManageUsers
public ResponseEntity<User> createUser(@RequestBody User user) {
    // 구현
}

@DeleteMapping("/users/{id}")
@DeleteUsers
public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
    // 구현
}

@GetMapping("/dashboard")
@ReadDashboard
public ResponseEntity<Dashboard> getDashboard() {
    // 구현
}

@GetMapping("/logs")
@ReadLogs
public ResponseEntity<List<Log>> getLogs() {
    // 구현
}
```

### 3-2. 일반 권한 어노테이션

```java
import com.basic.project.annotation.HasMenuPermission.*;

@GetMapping("/users")
@Read("USER_MANAGEMENT")
public ResponseEntity<List<User>> getAllUsers() {
    // 구현
}

@PostMapping("/users")
@Write("USER_MANAGEMENT")
public ResponseEntity<User> createUser(@RequestBody User user) {
    // 구현
}

@DeleteMapping("/users/{id}")
@Delete("USER_MANAGEMENT")
public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
    // 구현
}
```

## 권한 레벨

### canRead / @ReadXxx / @Read
- 메뉴에 대한 읽기 권한만 확인
- GET 엔드포인트에 주로 사용

### canManage / @ManageXxx / @Write  
- 메뉴에 대한 읽기 + 쓰기 권한 확인
- POST, PUT 엔드포인트에 주로 사용

### canDelete / @DeleteXxx / @Delete
- 메뉴에 대한 삭제 권한 확인
- DELETE 엔드포인트에 사용

### canAccess / @Access
- 메뉴 접근 권한만 확인 (canRead와 동일)
- 일반 사용자도 접근 가능한 기능

### hasFullAccess / @Full
- 모든 권한 (읽기 + 쓰기 + 삭제) 확인
- 관리자 전용 기능

## 메뉴 코드 상수

```java
MenuCode.DASHBOARD            // 대시보드
MenuCode.USER_MANAGEMENT      // 회원 관리
MenuCode.ROLE_MANAGEMENT      // 역할 관리
MenuCode.MENU_MANAGEMENT      // 메뉴 관리
MenuCode.PERMISSION_MANAGEMENT // 권한 관리
MenuCode.LOG_MANAGEMENT       // 로그 관리
MenuCode.MY_PROFILE          // 내 정보
MenuCode.SYSTEM_MANAGEMENT   // 시스템 관리
```

## 권장 사용법

1. **간단한 CRUD**: `@ReadUsers`, `@ManageUsers`, `@DeleteUsers` 등 특화 어노테이션 사용
2. **복잡한 로직**: `@PreAuthorize("@menuPermissionService.canXxx()")` 사용  
3. **새로운 메뉴**: MenuCode에 상수 추가 후 해당 메서드들 추가

이렇게 하면 코드가 매우 깔끔하고 이해하기 쉬워집니다!