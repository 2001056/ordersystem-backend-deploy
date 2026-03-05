package com.beyond.ordersystem.member.service;

import com.beyond.ordersystem.member.domain.Member;
import com.beyond.ordersystem.member.dtos.*;
import com.beyond.ordersystem.member.repository.MemberRepository;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.S3Client;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
@Service
@Transactional
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final S3Client s3Client;
    @Value("${aws.s3.bucket1}")
    private String bucket;

//    생성자가 하나밖에 없을떄에는 Autowired생략가능
    @Autowired
    public MemberService(MemberRepository memberRepository, PasswordEncoder passwordEncoder, S3Client s3Client){
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
        this.s3Client = s3Client;
    }
    public Long save(MemberCreateDto dto){
        if(memberRepository.findByEmail(dto.getEmail()).isPresent()){
            throw new IllegalArgumentException("이미 존재하는 email입니다.");
        }
        Member member = dto.toEntity(passwordEncoder.encode(dto.getPassword()));
        memberRepository.save(member);
        return member.getId();
    }

    @Transactional(readOnly = true)
    public List<MemberResDto> findAll(){
        return memberRepository.findAll().stream().map(a-> MemberResDto.fromEntity(a))
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public MemberResDto findById(Long id){
        Optional<Member> optMember = memberRepository.findById(id);
        Member member = optMember.orElseThrow(()-> new EntityNotFoundException("entity is not found"));
        MemberResDto dto = MemberResDto.fromEntity(member);
        return dto;
    }

    @Transactional(readOnly = true)
    public MemberResDto myinfo(String email){
        Optional<Member> optMember = memberRepository.findByEmail(email);
        Member member = optMember.orElseThrow(()-> new EntityNotFoundException("entity is not found"));
        MemberResDto dto = MemberResDto.fromEntity(member);
        return dto;
    }

    public Member login(MemberLoginReqDto dto){
        Optional<Member> opt_member = memberRepository.findByEmail(dto.getEmail());
        boolean check = true;
        if(!opt_member.isPresent()){
            check=false;
        }else {
            if(!passwordEncoder.matches(dto.getPassword(), opt_member.get().getPassword())){
                check =false;
            }
        }
        if(!check){
            throw new IllegalArgumentException("email 또는 비밀번호가 일치하지 않습니다.");
        }
        return opt_member.get();
    }


}
