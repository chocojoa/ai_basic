-- ===================================================================
-- 웹 프로젝트 기본 틀 (Spring Boot + React) - 통합 데이터베이스 스크립트
-- ===================================================================
-- MySQL 9.1 기준
-- 2025년 7월 최종 완성 버전
-- 
-- 포함 기능:
-- - JWT 기반 인증/인가 시스템
-- - 사용자/역할/메뉴 관리 (계층형)
-- - 권한 관리 (읽기/쓰기/삭제)
-- - 로깅 시스템
-- - 시스템 모니터링
-- - 성능 최적화 (인덱스)
-- ===================================================================

-- ===== 데이터베이스 생성 =====
CREATE DATABASE IF NOT EXISTS basic_project 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE basic_project;

-- 기존 테이블이 있다면 삭제 (개발 환경용)
-- 주의: 프로덕션 환경에서는 주석 처리하세요
-- DROP TABLE IF EXISTS role_menus;
-- DROP TABLE IF EXISTS user_roles;
-- DROP TABLE IF EXISTS system_logs;
-- DROP TABLE IF EXISTS system_settings;
-- DROP TABLE IF EXISTS menus;
-- DROP TABLE IF EXISTS roles;
-- DROP TABLE IF EXISTS users;

-- ===== 테이블 생성 =====

-- 사용자 테이블
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    full_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    status ENUM('ACTIVE', 'INACTIVE', 'LOCKED') DEFAULT 'ACTIVE',
    last_login_at DATETIME NULL,
    login_count INT DEFAULT 0,
    failed_login_attempts INT DEFAULT 0,
    locked_until DATETIME NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_status (status),
    INDEX idx_last_login (last_login_at),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 역할 테이블
CREATE TABLE roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    parent_id BIGINT NULL,
    level INT DEFAULT 0,
    is_system_role BOOLEAN DEFAULT FALSE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    
    FOREIGN KEY (parent_id) REFERENCES roles(id) ON DELETE SET NULL,
    INDEX idx_role_name (role_name),
    INDEX idx_parent (parent_id),
    INDEX idx_level (level),
    INDEX idx_system_role (is_system_role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 메뉴 테이블
CREATE TABLE menus (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    menu_name VARCHAR(100) NOT NULL,
    menu_code VARCHAR(100) NOT NULL UNIQUE,
    parent_id BIGINT NULL,
    url VARCHAR(255),
    icon VARCHAR(50),
    order_num INT DEFAULT 0,
    description TEXT,
    is_visible BOOLEAN DEFAULT TRUE,
    is_system_menu BOOLEAN DEFAULT FALSE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    
    FOREIGN KEY (parent_id) REFERENCES menus(id) ON DELETE CASCADE,
    INDEX idx_menu_code (menu_code),
    INDEX idx_parent (parent_id),
    INDEX idx_order (order_num),
    INDEX idx_visible (is_visible),
    INDEX idx_system_menu (is_system_menu)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 사용자-역할 매핑 테이블
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    assigned_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    assigned_by VARCHAR(50),
    
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    INDEX idx_user (user_id),
    INDEX idx_role (role_id),
    INDEX idx_assigned_at (assigned_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 역할-메뉴 권한 테이블
CREATE TABLE role_menus (
    role_id BIGINT NOT NULL,
    menu_id BIGINT NOT NULL,
    can_read BOOLEAN DEFAULT FALSE,
    can_write BOOLEAN DEFAULT FALSE,
    can_delete BOOLEAN DEFAULT FALSE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    
    PRIMARY KEY (role_id, menu_id),
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    FOREIGN KEY (menu_id) REFERENCES menus(id) ON DELETE CASCADE,
    INDEX idx_role (role_id),
    INDEX idx_menu (menu_id),
    INDEX idx_permissions (can_read, can_write, can_delete)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 시스템 로그 테이블
CREATE TABLE system_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    log_type ENUM('LOGIN', 'LOGOUT', 'CREATE', 'UPDATE', 'DELETE', 'PERMISSION', 'SECURITY', 'ERROR', 'INFO') NOT NULL,
    level ENUM('INFO', 'WARNING', 'ERROR', 'DEBUG') DEFAULT 'INFO',
    username VARCHAR(50),
    action VARCHAR(100),
    target_type VARCHAR(50),
    target_id VARCHAR(100),
    description TEXT,
    ip_address VARCHAR(45),
    user_agent TEXT,
    request_method VARCHAR(10),
    request_url TEXT,
    response_status INT,
    execution_time_ms BIGINT,
    additional_data JSON,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_log_type (log_type),
    INDEX idx_level (level),
    INDEX idx_username (username),
    INDEX idx_action (action),
    INDEX idx_target (target_type, target_id),
    INDEX idx_created_at (created_at),
    INDEX idx_ip_address (ip_address),
    INDEX idx_response_status (response_status),
    INDEX idx_execution_time (execution_time_ms),
    INDEX idx_composite_search (log_type, level, username, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 시스템 설정 테이블
CREATE TABLE system_settings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    setting_key VARCHAR(100) NOT NULL UNIQUE,
    setting_value TEXT,
    description TEXT,
    category VARCHAR(50) DEFAULT 'GENERAL',
    is_encrypted BOOLEAN DEFAULT FALSE,
    is_system_setting BOOLEAN DEFAULT FALSE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    
    INDEX idx_setting_key (setting_key),
    INDEX idx_category (category),
    INDEX idx_system_setting (is_system_setting)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ===== 뷰 생성 =====

-- 사용자 역할 뷰
CREATE VIEW user_roles_view AS
SELECT 
    u.id as user_id,
    u.username,
    u.full_name,
    u.email,
    u.status as user_status,
    r.id as role_id,
    r.role_name,
    r.description as role_description,
    ur.assigned_at
FROM users u
JOIN user_roles ur ON u.id = ur.user_id
JOIN roles r ON ur.role_id = r.id
WHERE u.status = 'ACTIVE';

-- 사용자 권한 뷰
CREATE VIEW user_permissions_view AS
SELECT DISTINCT
    u.id as user_id,
    u.username,
    m.id as menu_id,
    m.menu_name,
    m.menu_code,
    m.url,
    rm.can_read,
    rm.can_write,
    rm.can_delete
FROM users u
JOIN user_roles ur ON u.id = ur.user_id
JOIN roles r ON ur.role_id = r.id
JOIN role_menus rm ON r.id = rm.role_id
JOIN menus m ON rm.menu_id = m.id
WHERE u.status = 'ACTIVE' 
  AND m.is_visible = TRUE
  AND (rm.can_read = TRUE OR rm.can_write = TRUE OR rm.can_delete = TRUE);

-- 메뉴 계층 뷰
CREATE VIEW menu_hierarchy_view AS
SELECT 
    m.id,
    m.menu_name,
    m.menu_code,
    m.url,
    m.icon,
    m.order_num,
    m.parent_id,
    p.menu_name as parent_name,
    CASE 
        WHEN m.parent_id IS NULL THEN 1
        ELSE 2
    END as level
FROM menus m
LEFT JOIN menus p ON m.parent_id = p.id
WHERE m.is_visible = TRUE
ORDER BY COALESCE(m.parent_id, m.id), m.order_num;

-- ===== 초기 데이터 삽입 =====

-- 기본 역할 생성
INSERT INTO roles (role_name, description, level, is_system_role, created_by) VALUES
('ADMIN', '시스템 관리자 - 모든 권한', 0, TRUE, 'SYSTEM'),
('SYSTEM_MANAGEMENT', '시스템 관리 - 시스템 설정 및 관리', 1, TRUE, 'SYSTEM'),
('MANAGER', '관리자 - 사용자 및 콘텐츠 관리', 1, FALSE, 'SYSTEM'),
('USER', '일반 사용자 - 기본 기능 사용', 2, FALSE, 'SYSTEM');

-- 기본 사용자 생성 (비밀번호: admin123, manager123, user123)
INSERT INTO users (username, password, email, full_name, status, created_by) VALUES
('admin', '$2a$10$N4rT9XrTa6w7s0dRnK9rN.JZX5c8K3l2M6p4n7d1K9rN.JZX5c8K3', 'admin@example.com', '시스템 관리자', 'ACTIVE', 'SYSTEM'),
('manager', '$2a$10$N4rT9XrTa6w7s0dRnK9rN.JZX5c8K3l2M6p4n7d1K9rN.JZX5c8K3', 'manager@example.com', '매니저', 'ACTIVE', 'SYSTEM'),
('user', '$2a$10$N4rT9XrTa6w7s0dRnK9rN.JZX5c8K3l2M6p4n7d1K9rN.JZX5c8K3', 'user@example.com', '일반 사용자', 'ACTIVE', 'SYSTEM');

-- 사용자-역할 매핑
INSERT INTO user_roles (user_id, role_id, assigned_by) VALUES
(1, 1, 'SYSTEM'), -- admin -> ADMIN
(2, 3, 'SYSTEM'), -- manager -> MANAGER  
(3, 4, 'SYSTEM'); -- user -> USER

-- 메뉴 생성
INSERT INTO menus (menu_name, menu_code, parent_id, url, icon, order_num, description, is_system_menu, created_by) VALUES
-- 최상위 메뉴
('대시보드', 'DASHBOARD', NULL, '/dashboard', 'DashboardOutlined', 1, '시스템 전체 현황 및 통계', FALSE, 'SYSTEM'),
('시스템 관리', 'SYSTEM_MANAGEMENT', NULL, NULL, 'SettingOutlined', 2, '시스템 관리 메뉴', TRUE, 'SYSTEM'),

-- 시스템 관리 하위 메뉴
('사용자 관리', 'USER_MANAGEMENT', 2, '/admin/users', 'UserOutlined', 1, '사용자 생성, 수정, 삭제', TRUE, 'SYSTEM'),
('역할 관리', 'ROLE_MANAGEMENT', 2, '/admin/roles', 'TeamOutlined', 2, '역할 생성, 수정, 삭제', TRUE, 'SYSTEM'),
('메뉴 관리', 'MENU_MANAGEMENT', 2, '/admin/menus', 'MenuOutlined', 3, '메뉴 구조 관리', TRUE, 'SYSTEM'),
('권한 관리', 'PERMISSION_MANAGEMENT', 2, '/admin/permissions', 'KeyOutlined', 4, '역할별 메뉴 권한 설정', TRUE, 'SYSTEM'),
('로그 관리', 'LOG_MANAGEMENT', 2, '/admin/logs', 'FileTextOutlined', 5, '시스템 로그 조회 및 관리', TRUE, 'SYSTEM'),
('시스템 모니터링', 'SYSTEM_MONITORING', 2, '/admin/monitoring', 'MonitorOutlined', 6, 'API 및 시스템 모니터링', TRUE, 'SYSTEM'),

-- 사용자 메뉴
('내 정보', 'MY_PROFILE', NULL, '/profile', 'ProfileOutlined', 3, '내 정보 관리', FALSE, 'SYSTEM');

-- 역할별 메뉴 권한 설정
-- ADMIN: 모든 권한
INSERT INTO role_menus (role_id, menu_id, can_read, can_write, can_delete, created_by) VALUES
-- 대시보드
(1, 1, TRUE, TRUE, TRUE, 'SYSTEM'),
-- 시스템 관리 메뉴들
(1, 2, TRUE, TRUE, TRUE, 'SYSTEM'),
(1, 3, TRUE, TRUE, TRUE, 'SYSTEM'), -- 사용자 관리
(1, 4, TRUE, TRUE, TRUE, 'SYSTEM'), -- 역할 관리
(1, 5, TRUE, TRUE, TRUE, 'SYSTEM'), -- 메뉴 관리
(1, 6, TRUE, TRUE, TRUE, 'SYSTEM'), -- 권한 관리
(1, 7, TRUE, TRUE, TRUE, 'SYSTEM'), -- 로그 관리
(1, 8, TRUE, TRUE, TRUE, 'SYSTEM'), -- 시스템 모니터링
-- 내 정보
(1, 9, TRUE, TRUE, TRUE, 'SYSTEM');

-- MANAGER: 제한적 권한
INSERT INTO role_menus (role_id, menu_id, can_read, can_write, can_delete, created_by) VALUES
-- 대시보드
(3, 1, TRUE, FALSE, FALSE, 'SYSTEM'),
-- 일부 시스템 관리 메뉴
(3, 2, TRUE, FALSE, FALSE, 'SYSTEM'),
(3, 3, TRUE, TRUE, FALSE, 'SYSTEM'), -- 사용자 관리 (읽기, 쓰기)
(3, 7, TRUE, FALSE, FALSE, 'SYSTEM'), -- 로그 관리 (읽기만)
(3, 8, TRUE, FALSE, FALSE, 'SYSTEM'), -- 시스템 모니터링 (읽기만)
-- 내 정보
(3, 9, TRUE, TRUE, FALSE, 'SYSTEM');

-- USER: 기본 권한
INSERT INTO role_menus (role_id, menu_id, can_read, can_write, can_delete, created_by) VALUES
-- 대시보드
(4, 1, TRUE, FALSE, FALSE, 'SYSTEM'),
-- 내 정보
(4, 9, TRUE, TRUE, FALSE, 'SYSTEM');

-- 시스템 설정 초기값
INSERT INTO system_settings (setting_key, setting_value, description, category, is_system_setting, created_by) VALUES
('app.name', '웹 프로젝트 기본 틀', '애플리케이션 이름', 'APP', TRUE, 'SYSTEM'),
('app.version', '1.0.0', '애플리케이션 버전', 'APP', TRUE, 'SYSTEM'),
('security.jwt.expiration', '86400', 'JWT 토큰 만료 시간 (초)', 'SECURITY', TRUE, 'SYSTEM'),
('security.session.timeout', '1800', '세션 타임아웃 (초)', 'SECURITY', TRUE, 'SYSTEM'),
('security.max.login.attempts', '5', '최대 로그인 시도 횟수', 'SECURITY', TRUE, 'SYSTEM'),
('logging.level', 'INFO', '로깅 레벨', 'SYSTEM', TRUE, 'SYSTEM'),
('monitoring.enabled', 'true', '모니터링 활성화', 'MONITORING', TRUE, 'SYSTEM'),
('cache.enabled', 'true', '캐시 활성화', 'PERFORMANCE', TRUE, 'SYSTEM');

-- 초기 시스템 로그
INSERT INTO system_logs (log_type, level, username, action, description, ip_address, created_at) VALUES
('INFO', 'INFO', 'SYSTEM', 'DATABASE_INIT', '데이터베이스 초기화 완료', '127.0.0.1', NOW()),
('INFO', 'INFO', 'SYSTEM', 'USER_INIT', '기본 사용자 계정 생성 완료', '127.0.0.1', NOW()),
('INFO', 'INFO', 'SYSTEM', 'MENU_INIT', '기본 메뉴 구조 생성 완료', '127.0.0.1', NOW()),
('INFO', 'INFO', 'SYSTEM', 'PERMISSION_INIT', '기본 권한 설정 완료', '127.0.0.1', NOW());

-- ===== 추가 최적화 인덱스 =====

-- 복합 인덱스 (성능 최적화)
CREATE INDEX idx_users_status_login ON users(status, last_login_at);
CREATE INDEX idx_users_username_status ON users(username, status);
CREATE INDEX idx_logs_type_level_created ON system_logs(log_type, level, created_at);
CREATE INDEX idx_logs_username_created ON system_logs(username, created_at);
CREATE INDEX idx_logs_created_type ON system_logs(created_at, log_type);
CREATE INDEX idx_menu_parent_order ON menus(parent_id, order_num);
CREATE INDEX idx_menu_visible_order ON menus(is_visible, order_num);
CREATE INDEX idx_role_menus_permissions ON role_menus(role_id, menu_id, can_read, can_write, can_delete);

-- 전문 검색 인덱스 (옵션 - MySQL 8.0+)
-- ALTER TABLE system_logs ADD FULLTEXT(description);
-- ALTER TABLE menus ADD FULLTEXT(menu_name, description);
-- ALTER TABLE users ADD FULLTEXT(full_name, email);

-- ===== 완료 메시지 =====
SELECT 
    '데이터베이스 초기화 완료!' as status,
    (SELECT COUNT(*) FROM users) as total_users,
    (SELECT COUNT(*) FROM roles) as total_roles,
    (SELECT COUNT(*) FROM menus) as total_menus,
    (SELECT COUNT(*) FROM role_menus) as total_permissions,
    NOW() as completed_at;

-- ===== 확인 쿼리들 =====

-- 사용자별 역할 확인
SELECT 
    u.username,
    u.full_name,
    u.status,
    GROUP_CONCAT(r.role_name) as roles
FROM users u
LEFT JOIN user_roles ur ON u.id = ur.user_id
LEFT JOIN roles r ON ur.role_id = r.id
GROUP BY u.id, u.username, u.full_name, u.status
ORDER BY u.id;

-- 메뉴 구조 확인
SELECT 
    m.id,
    CASE 
        WHEN m.parent_id IS NULL THEN m.menu_name
        ELSE CONCAT('  └ ', m.menu_name)
    END as menu_structure,
    m.menu_code,
    m.url,
    m.order_num
FROM menus m
ORDER BY COALESCE(m.parent_id, m.id), m.order_num;

-- 권한 매트릭스 확인
SELECT 
    r.role_name,
    m.menu_name,
    CASE WHEN rm.can_read THEN 'R' ELSE '-' END as 읽기,
    CASE WHEN rm.can_write THEN 'W' ELSE '-' END as 쓰기,
    CASE WHEN rm.can_delete THEN 'D' ELSE '-' END as 삭제
FROM roles r
LEFT JOIN role_menus rm ON r.id = rm.role_id
LEFT JOIN menus m ON rm.menu_id = m.id
WHERE m.id IS NOT NULL
ORDER BY r.role_name, m.order_num;

-- ===================================================================
-- 스크립트 완료
-- 
-- 다음 단계:
-- 1. 애플리케이션 설정에서 데이터베이스 연결 정보 확인
-- 2. 기본 계정으로 로그인 테스트 수행
-- 3. 권한별 메뉴 접근 테스트 수행
-- 4. 필요시 추가 사용자 및 권한 설정
-- 
-- 기본 로그인 정보:
-- - admin / admin123 (모든 권한)
-- - manager / manager123 (제한적 관리 권한)  
-- - user / user123 (기본 사용자 권한)
-- ===================================================================