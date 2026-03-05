package com.beyond.ordersystem.common.auth;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

//토큰 검증 후 Authentication객체 생성
@Component
public class JwtTokenFilter extends GenericFilter {

    @Value("${jwt.secretKey}")
    private String st_secret_key;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        try {

            HttpServletRequest req = (HttpServletRequest) request;
//        관례적으로 Bearer라는 문자열을 토큰 붙여서 전송
            String bearerToken = req.getHeader("Authorization");
            if(bearerToken == null){
//            token이 없는 경우 검증을 할수가 없으므로, filter chain으로 return
                chain.doFilter(request, response);
                return;
            }
//        Bearer문자열을 제거한 후에 jwt token만을 검증
            String token = bearerToken.substring(7);
//        token 검증 및 claims 추출
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(st_secret_key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

//        claims를 기반으로 Authentication객체 생성
//        권한의 경우 다수의 권한을 가질수 있으므로 일반적으로 List로 설계
            List<GrantedAuthority> authorities = new ArrayList<>();
//        권한을 세팅할때 권한은 ROLE_라는 키워드를 붙임으로서 추후 권한체크 어노테이션사용 가능
            authorities.add(new SimpleGrantedAuthority("ROLE_"+claims.get("role")));

//        1)pricipal : email 2)credentials :토큰 3)authorities : 권한묶음
            Authentication authentication = new UsernamePasswordAuthenticationToken(claims.getSubject(), "", authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }catch(Exception e){
//            에러가 아닌경우 출력할 필요없으므로 주석처리
//            e.printStackTrace();
        }

//        다시 filterChain으로 돌아가는 로직
        chain.doFilter(request, response);
    }
}
