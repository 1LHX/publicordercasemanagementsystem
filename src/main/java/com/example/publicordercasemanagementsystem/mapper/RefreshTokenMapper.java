package com.example.publicordercasemanagementsystem.mapper;

import com.example.publicordercasemanagementsystem.pojo.RefreshToken;

public interface RefreshTokenMapper {

    int insert(RefreshToken token);

    RefreshToken findValidByHash(String tokenHash);

    RefreshToken findValidByHashAndUserId(String tokenHash, Long userId);

    int revokeByHash(String tokenHash);

    int revokeByHashAndUserId(String tokenHash, Long userId);

    int revokeByUserId(Long userId);

    int updateLastUsed(Long id);
}
