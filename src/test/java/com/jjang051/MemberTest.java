package com.jjang051;

import com.jjang051.config.QueryDslConfig;
import com.jjang051.entity.Member;
import com.jjang051.entity.QMember;
import com.jjang051.entity.Team;
import com.jjang051.repository.MemberRepository;
import com.jjang051.repository.TeamRepository;
import com.jjang051.service.MemberService;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.jjang051.entity.QMember.member;

@SpringBootTest
//@Transactional
//@Commit
public class MemberTest {
    @Autowired
    TeamRepository teamRepository;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    JPAQueryFactory queryFactory;

//    @Autowired
//    MemberService memberService;

    @Test
    public void testMember() {
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");

        teamRepository.save(teamA);
        teamRepository.save(teamB);

        Member member01 = new Member("member01",10,teamA);
        Member member02 = new Member("member02",20,teamA);
        Member member03 = new Member("member03",30,teamB);
        Member member04 = new Member("member04",40,teamB);

        memberRepository.save(member01);
        memberRepository.save(member02);
        memberRepository.save(member03);
        memberRepository.save(member04);
    }

    @Test
    public void squeryDsl() {
        Member findMember =
                queryFactory
                .selectFrom(member)
                .where(member.userName.eq("member01"))
                .fetchOne();
        Assertions.assertThat(findMember.getUserName())
                .isEqualTo("member01");
    }

    @Test
    public void queryDsl02() {
        List<Member> findMember =
                queryFactory
                        .selectFrom(member)
                        .where(member.userName.like("member%")
                              ,member.age.eq(20))
                        .fetch();
//        Assertions.assertThat(findMember.size())
//                .isEqualTo(4);
        for(int i=0;i<findMember.size();i++) {
            System.out.println(findMember.get(i).getUserName());
        }
    }

    @Test
    public void queryDslSort() {
        List<Member> memberList = queryFactory
                .selectFrom(member)
                .where(member.age.gt(20))
                .orderBy(member.age.desc(),member.userName.asc())
                .fetch();

        for(int i=0;i<memberList.size();i++) {
            System.out.println(memberList.get(i).getUserName());
        }
    }

}


