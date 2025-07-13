package com.basic.project.service;

import com.basic.project.domain.Menu;
import com.basic.project.mapper.MenuMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
public class MenuService {
    
    private final MenuMapper menuMapper;
    
    @Cacheable(value = "menus", key = "'all'")
    public List<Menu> getAllMenus() {
        return menuMapper.findAll();
    }
    
    @Cacheable(value = "menuTree", key = "'tree'")
    public List<Menu> getMenuTree() {
        List<Menu> allMenus = menuMapper.findAll();
        return buildMenuTree(allMenus);
    }
    
    @Cacheable(value = "menus", key = "'user_' + #userId")
    public List<Menu> getUserMenus(Long userId) {
        List<Menu> userMenus = menuMapper.findByUserId(userId);
        return buildMenuTree(userMenus);
    }
    
    public Optional<Menu> getMenuById(Long id) {
        return menuMapper.findById(id);
    }
    
    public List<Menu> getRootMenus() {
        return menuMapper.findRootMenus();
    }
    
    public List<Menu> getChildMenus(Long parentId) {
        return menuMapper.findByParentId(parentId);
    }
    
    @Transactional
    @CacheEvict(value = {"menus", "menuTree"}, allEntries = true)
    public Menu createMenu(Menu menu) {
        log.info("Creating new menu: {}", menu.getMenuName());
        
        // 정렬 순서 설정
        if (menu.getOrderNum() == null) {
            menu.setOrderNum(getNextOrderNum(menu.getParentId()));
        }
        
        // 기본값 설정
        if (menu.getIsVisible() == null) {
            menu.setIsVisible(true);
        }
        if (menu.getIsActive() == null) {
            menu.setIsActive(true);
        }
        
        menuMapper.insert(menu);
        log.info("Menu created with ID: {}", menu.getId());
        return menu;
    }
    
    @Transactional
    @CacheEvict(value = {"menus", "menuTree"}, allEntries = true)
    public Menu updateMenu(Menu menu) {
        log.info("Updating menu: {}", menu.getId());
        
        Optional<Menu> existingMenu = menuMapper.findById(menu.getId());
        if (existingMenu.isEmpty()) {
            throw new NoSuchElementException("Menu not found with id: " + menu.getId());
        }
        
        menuMapper.update(menu);
        log.info("Menu updated: {}", menu.getId());
        return menu;
    }
    
    @Transactional
    @CacheEvict(value = {"menus", "menuTree"}, allEntries = true)
    public void deleteMenu(Long id) {
        log.info("Deleting menu: {}", id);
        
        // 하위 메뉴가 있는지 확인
        if (menuMapper.hasChildren(id)) {
            throw new IllegalStateException("Cannot delete menu with children. Delete children first.");
        }
        
        menuMapper.delete(id);
        log.info("Menu deleted: {}", id);
    }
    
    @Transactional
    @CacheEvict(value = {"menus", "menuTree"}, allEntries = true)
    public void updateMenuOrder(Long id, Integer newOrder) {
        log.info("Updating menu order: {} to {}", id, newOrder);
        menuMapper.updateSortOrder(id, newOrder);
    }
    
    @Transactional
    @CacheEvict(value = {"menus", "menuTree"}, allEntries = true)
    public void toggleMenuVisibility(Long id) {
        Optional<Menu> menu = menuMapper.findById(id);
        if (menu.isPresent()) {
            boolean newVisibility = !menu.get().getIsVisible();
            menuMapper.updateVisibility(id, newVisibility);
            log.info("Menu visibility toggled: {} to {}", id, newVisibility);
        }
    }
    
    public List<Menu> searchMenus(String keyword) {
        return menuMapper.search(keyword);
    }
    
    public List<Menu> getMenusWithPagination(int page, int size) {
        int offset = page * size;
        return menuMapper.findWithPagination(offset, size);
    }
    
    public int getTotalMenuCount() {
        return menuMapper.count();
    }
    
    private List<Menu> buildMenuTree(List<Menu> allMenus) {
        // 루트 메뉴들 찾기
        List<Menu> rootMenus = allMenus.stream()
                .filter(menu -> menu.getParentId() == null)
                .toList();
        
        // 각 루트 메뉴에 대해 하위 메뉴들 설정
        for (Menu rootMenu : rootMenus) {
            setChildren(rootMenu, allMenus);
        }
        
        return rootMenus;
    }
    
    private void setChildren(Menu parentMenu, List<Menu> allMenus) {
        List<Menu> children = allMenus.stream()
                .filter(menu -> parentMenu.getId().equals(menu.getParentId()))
                .toList();
        
        parentMenu.setChildren(children);
        
        // 재귀적으로 하위 메뉴들도 처리
        for (Menu child : children) {
            setChildren(child, allMenus);
        }
    }
    
    private Integer getNextOrderNum(Long parentId) {
        List<Menu> siblings = parentId == null ? 
                menuMapper.findRootMenus() : 
                menuMapper.findByParentId(parentId);
        
        return siblings.stream()
                .mapToInt(Menu::getOrderNum)
                .max()
                .orElse(0) + 1;
    }
}