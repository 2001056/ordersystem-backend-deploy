package com.beyond.ordersystem.member.dtos;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class MemberLoginResDto {
    private String accessToken;
    private String refreshToken;
}
