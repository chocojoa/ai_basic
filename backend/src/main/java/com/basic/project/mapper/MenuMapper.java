package com.basic.project.mapper;

import com.basic.project.domain.Menu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface MenuMapper {
    
    Optional<Menu> findById(Long id);
    
    List<Menu> findAll();
    
    List<Menu> findByParentId(Long parentId);
    
    List<Menu> findRootMenus();
    
    List<Menu> findByUserId(Long userId);
    
    List<Menu> findVisibleMenus();
    
    List<Menu> findActiveMenus();
    
    int insert(Menu menu);
    
    int update(Menu menu);
    
    int delete(Long id);
    
    int updateSortOrder(@Param("id") Long id, @Param("sortOrder") Integer sortOrder);
    
    int updateVisibility(@Param("id") Long id, @Param("isVisible") Boolean isVisible);
    
    int count();
    
    List<Menu> findWithPagination(@Param("offset") int offset, @Param("limit") int limit);
    
    List<Menu> search(@Param("keyword") String keyword);
    
    boolean hasChildren(Long parentId);
    
    Menu findByMenuName(String menuName);
    
    Menu findByMenuCode(String menuCode);
    
    List<String> findAccessibleMenusByRoles(@Param("roles") List<String> roles);
}