package com.beyond.ordersystem.common.configs;

import com.beyond.ordersystem.common.auth.JwtTokenFilter;
import com.beyond.ordersystem.common.exception.JwtAuthenticationHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableMethodSecurity //PreAuthorize어노테이션을 사용하기 위한 설정
public class SecurityConfig {
    private final JwtTokenFilter jwtTokenFilter;
    private final JwtAuthenticationHandler jwtAuthenticationHandler;
    @Autowired
    public SecurityConfig(JwtTokenFilter jwtTokenFilter, JwtAuthenticationHandler jwtAuthenticationHandler) {
        this.jwtTokenFilter = jwtTokenFilter;
        this.jwtAuthenticationHandler = jwtAuthenticationHandler;
    }

    //    내가 만든 클래스와 객체는 @Component, 외부 클래스(라이브러리)를 활용한 객체는 @Configuration+@Bean
//    @Component는 클래스 위에 붙여 클래스기반에 객체를 싱글톤객체로 생성. @Bean은 메서드위에 붙여 Return 되는 객체를 싱글톤객체로 생성.
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .cors(c->c.configurationSource(corsConfigurationSource()))
//                csrf공격(일반적으로 쿠키를 활용한 공격)에 대한 방어 비활성화
                .csrf(AbstractHttpConfigurer::disable)
//                http basic은 email/pw를 인코딩하여 인증(전송)하는 간단한 인증방식. 비활성화.
                .httpBasic(AbstractHttpConfigurer::disable)
//                세션로그인방식 비활성화
                .sessionManagement(a->a.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//                token을 검증하고, Authentication객체 생성
                .addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(e->e.authenticationEntryPoint(jwtAuthenticationHandler))
//                지정한 특정 url을 제외한 모든 요청에 대해서 authenticated(인증처리)하겠다 라는 의미
                .authorizeHttpRequests(a->a.requestMatchers(
                        "/member/create",
                        "/member/doLogin",
                        "/product/list",
                        "/health",
                        "/member/refresh-at",
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html"
                ).permitAll().anyRequest().authenticated())
                .build();
    }

    public CorsConfigurationSource corsConfigurationSource(){
        CorsConfiguration configuration = new CorsConfiguration();
//        허용가능한 도메인 목록 설정
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000", "https://www.nahcb572.shop"));
//        모든 HTTP메서드(GET, POST, OPTIONS 등) 허용
        configuration.setAllowedMethods(Arrays.asList("*"));
//        모든 헤더요소(Authorization, Content-Type 등) 허용
        configuration.setAllowedHeaders(Arrays.asList("*"));
//        자격 증명 허용
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        모든 url패턴에 대해 위 cors 정책을 적용
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder pwEncoder(){

        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

}
