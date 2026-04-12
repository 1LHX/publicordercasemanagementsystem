package com.example.publicordercasemanagementsystem.service.impl;

import com.example.publicordercasemanagementsystem.dto.ChangePasswordRequest;
import com.example.publicordercasemanagementsystem.dto.PageResult;
import com.example.publicordercasemanagementsystem.dto.UpdateUserNameRequest;
import com.example.publicordercasemanagementsystem.dto.UpdateUserRoleRequest;
import com.example.publicordercasemanagementsystem.dto.UpdateUserStatusRequest;
import com.example.publicordercasemanagementsystem.dto.UserInfo;
import com.example.publicordercasemanagementsystem.dto.UserListItem;
import com.example.publicordercasemanagementsystem.exception.AuthException;
import com.example.publicordercasemanagementsystem.mapper.RefreshTokenMapper;
import com.example.publicordercasemanagementsystem.mapper.UserMapper;
import com.example.publicordercasemanagementsystem.pojo.User;
import com.example.publicordercasemanagementsystem.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class UserServiceImpl implements UserService {

    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_SIZE = 10;
    private static final int MAX_SIZE = 100;

    private static final String ADMIN_ROLE = "admin";
    private static final String PERMISSION_DENIED_MESSAGE = "当前角色无此操作权限。";

    private final UserMapper userMapper;
    private final RefreshTokenMapper refreshTokenMapper;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserMapper userMapper,
                           RefreshTokenMapper refreshTokenMapper,
                           PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.refreshTokenMapper = refreshTokenMapper;
        this.passwordEncoder = passwordEncoder;
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
    public UserInfo getUserInfoById(Long id) {
        if (id == null) {
            return null;
        }
        User user = userMapper.findById(id);
        return user == null ? null : toUserInfo(user);
    }

    @Override
    public UserInfo updateUserName(Long id, UpdateUserNameRequest request, Long operatorUserId) {
        User operator = requireAdmin(operatorUserId);
        User target = requireUserById(id);

        if (operator.getId().equals(id)) {
            throw new AuthException(400, "Current operator cannot rename itself");
        }

        String newName = request.getName().trim();
        if (userMapper.countByNameExcludeId(newName, id) > 0) {
            throw new AuthException(400, "Name already exists");
        }
        userMapper.updateNameById(id, newName);

        // Active access tokens may still carry old display info; revoke refresh tokens to force re-login.
        if (!newName.equals(target.getName())) {
            refreshTokenMapper.revokeByUserId(id);
        }
        return toUserInfo(requireUserById(id));
    }

    @Override
    public void changeUserPassword(Long id, ChangePasswordRequest request, Long operatorUserId) {
        requireAdmin(operatorUserId);
        requireUserById(id);
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new AuthException(400, "Passwords do not match");
        }
        userMapper.updatePasswordById(id, passwordEncoder.encode(request.getPassword()));
        refreshTokenMapper.revokeByUserId(id);
    }

    @Override
    public UserInfo updateUserRole(Long id, UpdateUserRoleRequest request, Long operatorUserId) {
        requireAdmin(operatorUserId);
        requireUserById(id);

        String roleCode = request.getRole().trim().toLowerCase(Locale.ROOT);
        if (userMapper.countActiveRoleByCode(roleCode) <= 0) {
            throw new AuthException(400, "Invalid role code");
        }
        userMapper.updateRoleById(id, roleCode);
        refreshTokenMapper.revokeByUserId(id);
        return toUserInfo(requireUserById(id));
    }

    @Override
    public UserInfo updateUserStatus(Long id, UpdateUserStatusRequest request, Long operatorUserId) {
        User operator = requireAdmin(operatorUserId);
        requireUserById(id);

        if (operator.getId().equals(id) && !Boolean.TRUE.equals(request.getIsActive())) {
            throw new AuthException(400, "Current operator cannot disable itself");
        }

        userMapper.updateActiveById(id, request.getIsActive());
        if (!Boolean.TRUE.equals(request.getIsActive())) {
            refreshTokenMapper.revokeByUserId(id);
        }
        return toUserInfo(requireUserById(id));
    }

    @Override
    public void deleteUser(Long id, Long operatorUserId) {
        User operator = requireAdmin(operatorUserId);
        requireUserById(id);

        if (operator.getId().equals(id)) {
            throw new AuthException(400, "Current operator cannot delete itself");
        }
        userMapper.deleteById(id);
    }

    private User requireAdmin(Long operatorUserId) {
        if (operatorUserId == null) {
            throw new AuthException(401, "Unauthenticated");
        }
        User operator = userMapper.findById(operatorUserId);
        if (operator == null || !ADMIN_ROLE.equals(operator.getRole())) {
            throw new AuthException(403, PERMISSION_DENIED_MESSAGE);
        }
        return operator;
    }

    private User requireUserById(Long id) {
        User user = userMapper.findById(id);
        if (user == null) {
            throw new AuthException(404, "User not found");
        }
        return user;
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
