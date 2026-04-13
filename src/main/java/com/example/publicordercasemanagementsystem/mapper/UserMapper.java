package com.example.publicordercasemanagementsystem.mapper;

import com.example.publicordercasemanagementsystem.pojo.User;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserMapper {

    User findByName(String name);

    User findById(Long id);

    int insert(User user);

    int updateLoginSuccess(Long id);

    int incrementLoginAttempts(Long id);

    int lockUser(Long id);

    int unlockUser(Long id);

    long countActiveRoleByCode(@Param("code") String code);

    long countByNameExcludeId(@Param("name") String name, @Param("excludeId") Long excludeId);

    int updateNameById(@Param("id") Long id, @Param("name") String name);

    int updatePasswordById(@Param("id") Long id, @Param("password") String password);

    int updateRoleById(@Param("id") Long id, @Param("role") String role);

    int updateActiveById(@Param("id") Long id, @Param("isActive") Boolean isActive);

    int deleteById(@Param("id") Long id);

    int deleteByIds(@Param("ids") List<Long> ids);

    long countByFilters(@Param("name") String name,
                        @Param("role") String role,
                        @Param("department") String department,
                        @Param("departmentId") Long departmentId,
                        @Param("isActive") Boolean isActive);

    List<User> findPage(@Param("name") String name,
                        @Param("role") String role,
                        @Param("department") String department,
                        @Param("departmentId") Long departmentId,
                        @Param("isActive") Boolean isActive,
                        @Param("offset") int offset,
                        @Param("size") int size);
}
