package com.beyond.ordersystem.common.init;

import com.beyond.ordersystem.member.domain.Role;
import com.beyond.ordersystem.member.repository.MemberRepository;
import com.beyond.ordersystem.member.domain.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class InitialDataLoad implements CommandLineRunner {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    @Autowired
    public InitialDataLoad(MemberRepository memberRepository, PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }
    @Override
    public void run(String... args) throws Exception {
        if(memberRepository.findByEmail("admin@naver.com").isPresent()){
            return;
        }
        memberRepository.save(Member.builder()
                .name("admin")
                .email("admin@naver.com")
                .role(Role.ADMIN)
                .password(passwordEncoder.encode("12341234"))
                .build());
    }
}
