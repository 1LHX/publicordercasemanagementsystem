package com.example.publicordercasemanagementsystem.service;

import com.example.publicordercasemanagementsystem.dto.ChangePasswordRequest;
import com.example.publicordercasemanagementsystem.dto.PageResult;
import com.example.publicordercasemanagementsystem.dto.UpdateUserNameRequest;
import com.example.publicordercasemanagementsystem.dto.UpdateUserRoleRequest;
import com.example.publicordercasemanagementsystem.dto.UpdateUserStatusRequest;
import com.example.publicordercasemanagementsystem.dto.UserInfo;
import com.example.publicordercasemanagementsystem.dto.UserListItem;

public interface UserService {

    PageResult<UserListItem> listUsers(String name,
                                       String role,
                                       String department,
                                       Long departmentId,
                                       Boolean isActive,
                                       Integer page,
                                       Integer size);

    UserInfo getUserInfoById(Long id);

    UserInfo updateUserName(Long id, UpdateUserNameRequest request, Long operatorUserId);

    void changeUserPassword(Long id, ChangePasswordRequest request, Long operatorUserId);

    UserInfo updateUserRole(Long id, UpdateUserRoleRequest request, Long operatorUserId);

    UserInfo updateUserStatus(Long id, UpdateUserStatusRequest request, Long operatorUserId);

    void deleteUser(Long id, Long operatorUserId);

    void deleteUsers(List<Long> ids, Long operatorUserId);
}
