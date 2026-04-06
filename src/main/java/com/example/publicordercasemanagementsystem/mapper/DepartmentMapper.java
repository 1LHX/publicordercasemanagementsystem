package com.example.publicordercasemanagementsystem.mapper;

import com.example.publicordercasemanagementsystem.pojo.Department;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DepartmentMapper {

    Department findById(Long id);

    Department findByName(String name);

    List<Department> findAll(@Param("name") String name,
                             @Param("isActive") Boolean isActive,
                             @Param("parentId") Long parentId);

    int insert(Department department);

    int updateById(Department department);

    int updateActiveById(@Param("id") Long id, @Param("isActive") Boolean isActive);

    int deleteById(@Param("id") Long id);

    long countByNameExcludeId(@Param("name") String name, @Param("excludeId") Long excludeId);

    long countUsersByDepartmentId(@Param("id") Long id);

    long countCasesByDepartmentId(@Param("id") Long id);

    long countChildrenByParentId(@Param("id") Long id);
}

