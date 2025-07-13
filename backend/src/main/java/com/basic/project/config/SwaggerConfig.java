package com.basic.project.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("로컬 개발 서버"),
                        new Server().url("https://api.example.com").description("운영 서버")
                ))
                .tags(List.of(
                        new Tag().name("인증").description("로그인, 회원가입, 토큰 관리"),
                        new Tag().name("사용자 관리").description("사용자 CRUD 및 관리"),
                        new Tag().name("역할 관리").description("역할 및 권한 관리"),
                        new Tag().name("메뉴 관리").description("계층형 메뉴 관리"),
                        new Tag().name("권한 관리").description("메뉴별 권한 설정"),
                        new Tag().name("로그 관리").description("시스템 로그 및 감사 로그"),
                        new Tag().name("대시보드").description("통계 및 현황 정보"),
                        new Tag().name("검색").description("고급 검색 및 필터링"),
                        new Tag().name("세션 관리").description("사용자 세션 관리"),
                        new Tag().name("시스템").description("시스템 정보 및 관리")
                ))
                .addSecurityItem(new SecurityRequirement().addList("JWT"))
                .components(new Components()
                        .addSecuritySchemes("JWT", 
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("JWT 토큰을 입력하세요 (Bearer 접두사 제외)")
                        )
                );
    }

    private Info apiInfo() {
        return new Info()
                .title("웹 프로젝트 기본 틀 API")
                .description("""
                        Spring Boot + React 기반 웹 애플리케이션의 REST API 문서입니다.
                        
                        ## 주요 기능
                        - 사용자 인증 및 권한 관리
                        - 계층형 메뉴 및 역할 관리
                        - 고급 검색 및 필터링
                        - 세션 관리 및 보안 정책
                        - 시스템 로그 및 감사 추적
                        
                        ## 인증 방법
                        1. `/api/auth/login`으로 로그인하여 JWT 토큰을 획득
                        2. Authorization 헤더에 `Bearer {token}` 형식으로 토큰 전송
                        3. 토큰 만료 시 `/api/auth/refresh`로 토큰 갱신
                        
                        ## 권한 체계
                        - 계층형 역할 시스템
                        - 메뉴별 읽기/쓰기/삭제 권한 분리
                        - 동적 권한 검증
                        """)
                .version("1.0.0")
                .contact(new Contact()
                        .name("개발팀")
                        .email("dev@example.com")
                        .url("https://github.com/example/basic-project"))
                .license(new License()
                        .name("MIT License")
                        .url("https://opensource.org/licenses/MIT"));
    }
}