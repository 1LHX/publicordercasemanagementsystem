package com.example.publicordercasemanagementsystem.service;

import com.example.publicordercasemanagementsystem.dto.PageResult;
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
}
