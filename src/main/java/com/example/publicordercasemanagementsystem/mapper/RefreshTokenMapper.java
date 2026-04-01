package com.example.publicordercasemanagementsystem.mapper;

import com.example.publicordercasemanagementsystem.pojo.RefreshToken;

public interface RefreshTokenMapper {

    int insert(RefreshToken token);

    RefreshToken findValidByHash(String tokenHash);

    int revokeByHash(String tokenHash);

    int revokeByUserId(Long userId);

    int updateLastUsed(Long id);
}
