package com.example.publicordercasemanagementsystem.service.impl;

import com.example.publicordercasemanagementsystem.dto.PageResult;
import com.example.publicordercasemanagementsystem.dto.UserInfo;
import com.example.publicordercasemanagementsystem.dto.UserListItem;
import com.example.publicordercasemanagementsystem.mapper.UserMapper;
import com.example.publicordercasemanagementsystem.pojo.User;
import com.example.publicordercasemanagementsystem.service.UserService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_SIZE = 10;
    private static final int MAX_SIZE = 100;

    private final UserMapper userMapper;

    public UserServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public PageResult<UserListItem> listUsers(String name,
                                              String role,
                                              String department,
                                              Long departmentId,
                                              Boolean isActive,
                                              Integer page,
                                              Integer size) {
        int safePage = page == null || page < 1 ? DEFAULT_PAGE : page;
        int safeSize = size == null || size < 1 ? DEFAULT_SIZE : Math.min(size, MAX_SIZE);
        int offset = (safePage - 1) * safeSize;

        long total = userMapper.countByFilters(name, role, department, departmentId, isActive);
        if (total == 0) {
            return new PageResult<>(new ArrayList<>(), 0, safePage, safeSize);
        }

        List<User> users = userMapper.findPage(name, role, department, departmentId, isActive, offset, safeSize);
        List<UserListItem> items = new ArrayList<>(users.size());
        for (User user : users) {
            items.add(toListItem(user));
        }
        return new PageResult<>(items, total, safePage, safeSize);
    }

    @Override
    public UserInfo getUserInfoByName(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        User user = userMapper.findByName(name);
        return user == null ? null : toUserInfo(user);
    }

    private UserListItem toListItem(User user) {
        UserListItem item = new UserListItem();
        item.setId(user.getId());
        item.setName(user.getName());
        item.setRole(user.getRole());
        item.setRoleName(user.getRoleName());
        item.setDepartment(user.getDepartment());
        item.setDepartmentId(user.getDepartmentId());
        item.setIsActive(user.getIsActive());
        item.setLastLogin(user.getLastLogin());
        item.setCreatedAt(user.getCreatedAt());
        return item;
    }

    private UserInfo toUserInfo(User user) {
        UserInfo info = new UserInfo();
        info.setId(user.getId());
        info.setName(user.getName());
        info.setRole(user.getRole());
        info.setRoleName(user.getRoleName());
        info.setDepartment(user.getDepartment());
        info.setDepartmentId(user.getDepartmentId());
        return info;
    }
}
