package com.basic.project.enums;

/**
 * 메뉴 코드 상수 정의
 * 메뉴명이 변경되어도 코드는 변경되지 않도록 관리
 */
public final class MenuCode {
    
    // 메뉴 코드 상수
    public static final String DASHBOARD = "DASHBOARD";
    public static final String USER_MANAGEMENT = "USER_MANAGEMENT";
    public static final String ROLE_MANAGEMENT = "ROLE_MANAGEMENT";
    public static final String MENU_MANAGEMENT = "MENU_MANAGEMENT";
    public static final String PERMISSION_MANAGEMENT = "PERMISSION_MANAGEMENT";
    public static final String LOG_MANAGEMENT = "LOG_MANAGEMENT";
    public static final String MY_PROFILE = "MY_PROFILE";
    public static final String SYSTEM_MANAGEMENT = "SYSTEM_MANAGEMENT";
    public static final String SYSTEM_MONITORING = "SYSTEM_MONITORING";
    public static final String ADVANCED_SEARCH = "ADVANCED_SEARCH";
    
    // 인스턴스 생성 방지
    private MenuCode() {}
}