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

    UserInfo getUserInfoByName(String name);

    UserInfo updateUserName(Long id, UpdateUserNameRequest request, String operatorName);

    void changeUserPassword(Long id, ChangePasswordRequest request, String operatorName);

    UserInfo updateUserRole(Long id, UpdateUserRoleRequest request, String operatorName);

    UserInfo updateUserStatus(Long id, UpdateUserStatusRequest request, String operatorName);

    void deleteUser(Long id, String operatorName);
}
