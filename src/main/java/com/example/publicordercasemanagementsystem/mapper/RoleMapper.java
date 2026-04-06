package com.example.publicordercasemanagementsystem.mapper;

import com.example.publicordercasemanagementsystem.pojo.Role;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface RoleMapper {

    Role findByCode(String code);

    List<Role> findAll(@Param("isActive") Boolean isActive);

    int insert(Role role);

    int updateByCode(Role role);

    int updateActiveByCode(@Param("code") String code, @Param("isActive") Boolean isActive);

    int deleteByCode(String code);

    long countUsersByRoleCode(@Param("code") String code);
}

