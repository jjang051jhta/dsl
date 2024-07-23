package com.jjang051.service;

import com.jjang051.entity.Member;
import com.jjang051.entity.QMember;
import com.jjang051.repository.MemberRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final JPAQueryFactory queryFactory;

    public Member getMember() {
        Member findMember =
                queryFactory
                        .selectFrom(QMember.member)
                        .where(QMember.member.userName.eq("member01"))
                        .fetchOne();
        return  findMember;
    }
}
