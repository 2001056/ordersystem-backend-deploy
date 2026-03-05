package com.beyond.ordersystem.member.controller;

import com.beyond.ordersystem.member.dtos.*;
import com.beyond.ordersystem.member.domain.Member;
import com.beyond.ordersystem.member.service.MemberService;
import com.beyond.ordersystem.common.auth.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.sql.Ref;
import java.util.List;

@RestController
@RequestMapping("/member")
public class MemberController {
    private final MemberService memberService;
    private final JwtTokenProvider jwtTokenProvider;
    @Autowired
    public MemberController(MemberService memberService, JwtTokenProvider jwtTokenProvider) {
        this.memberService = memberService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping("/create")
    @Operation(
            summary = "회원가입", description = "이메일,비밀번호를 통한 회원가입"
    )
    public ResponseEntity<?> create(@RequestBody MemberCreateDto dto){
        Long id = memberService.save(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(id);
    }

    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN')")
    public List<MemberResDto> findAll(){
        List<MemberResDto> dtoList = memberService.findAll();
        return dtoList;
    }

    @GetMapping("/detail/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> findById(@PathVariable Long id){
        MemberResDto dto = memberService.findById(id);
        return ResponseEntity.status(HttpStatus.OK).body(dto);
    }

    @GetMapping("/myinfo")
    public ResponseEntity<?> myinfo(@AuthenticationPrincipal String email){
        MemberResDto dto = memberService.myinfo(email);
        return ResponseEntity.status(HttpStatus.OK).body(dto);
    }


    @PostMapping("/doLogin")
    public ResponseEntity<?> login(@RequestBody MemberLoginReqDto dto){
        Member member = memberService.login(dto);
        String accessToken = jwtTokenProvider.createToken(member);
//        refresh생성
        String refreshToken = jwtTokenProvider.createRtToken(member);
        MemberLoginResDto memberLoginResDto = MemberLoginResDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(memberLoginResDto);
    }
    @PostMapping("/refresh-at")
    public ResponseEntity<?> refreshAt(@RequestBody RefreshTokenDto dto){
//        rt검증(1. 토큰 자체 검증 2. redis 조회검증)
        Member member = jwtTokenProvider.validateRt(dto.getRefreshToken());
//        at신규 생성
        String accessToken = jwtTokenProvider.createToken(member);
        MemberLoginResDto memberLoginResDto = MemberLoginResDto.builder()
                .accessToken(accessToken)
                .refreshToken(null)
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(memberLoginResDto);
    }

}
