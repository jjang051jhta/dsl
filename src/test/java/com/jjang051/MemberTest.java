package com.jjang051;

import com.jjang051.config.QueryDslConfig;
import com.jjang051.dto.MemberDto;
import com.jjang051.entity.Member;
import com.jjang051.entity.QMember;
import com.jjang051.entity.Team;
import com.jjang051.repository.MemberRepository;
import com.jjang051.repository.TeamRepository;
import com.jjang051.service.MemberService;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
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
import static com.jjang051.entity.QTeam.team;

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

        Member member01 = new Member("member01", 10, teamA);
        Member member02 = new Member("member02", 20, teamA);
        Member member03 = new Member("member03", 30, teamB);
        Member member04 = new Member("member04", 40, teamB);

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
                                , member.age.eq(20))
                        .fetch();
//        Assertions.assertThat(findMember.size())
//                .isEqualTo(4);
        for (int i = 0; i < findMember.size(); i++) {
            System.out.println(findMember.get(i).getUserName());
        }
    }

    @Test
    public void queryDslSort() {
        List<Member> memberList = queryFactory
                .selectFrom(member)
                .where(member.age.gt(20))
                .orderBy(member.age.desc(), member.userName.asc())
                .fetch();

        for (int i = 0; i < memberList.size(); i++) {
            System.out.println(memberList.get(i).getUserName());
        }
    }

    @Test
    public void paging() {
        List<Member> memberList = queryFactory
                .selectFrom(member)
                .orderBy(member.userName.desc())
                .offset(1)
                .limit(2)  //2건만 조회  end
                .fetch();
        System.out.println(memberList.size());
        Assertions.assertThat(memberList.size()).isEqualTo(2);
    }

    @Test
    public void paging02() {
        QueryResults<Member> memberList = queryFactory
                .selectFrom(member)
                .orderBy(member.userName.desc())
                .offset(1)
                .limit(2)  //2건만 조회  end
                .fetchResults();
        System.out.println(memberList.getTotal()); //전체 갯수 조회
        System.out.println(memberList.getOffset());
        System.out.println(memberList.getLimit());
        System.out.println(memberList.getResults().size()); //결과 갯수 조회
    }

    @Test
    public void 집합함수() {
        List<Tuple> result =
                queryFactory
                        .select(
                                member.count(),
                                member.age.sum(),
                                member.age.avg(),
                                member.age.max(),
                                member.age.min()
                        )
                        .from(member).fetch();
        Tuple tuple = result.get(0);
        System.out.println(tuple.get(member.count()));
        System.out.println(tuple.get(member.age.sum()));
        System.out.println(tuple.get(member.age.avg()));
        System.out.println(tuple.get(member.age.max()));
        System.out.println(tuple.get(member.age.min()));
    }

    @Test
    public void group() {
        List<Tuple> result = queryFactory
                .select(team.name, member.age.avg())
                .from(member)
                .join(member.team, team)
                .groupBy(team.name)
                .fetch();

        Tuple teamA = result.get(0);
        System.out.println("=====" + result.size());
        System.out.println(teamA.get(team.name));
        System.out.println(teamA.get(member.age.avg()));
    }

    @Test
    public void join() {
        // LAZY(가짜 proxy 샅애의 객체를 들고온다.)
        // 만약 그 속성을 참조할때 그때 들고온다. 즉 그때  n+1
        // 상태에서 연관관계에 있는 애들도 같이 들고 오기 ()
        // fetch join을 하면 진짜를 들고 온다.
        List<Member> result =
                queryFactory.selectFrom(member)
                        .join(member.team, team).fetchJoin()
                        .where(team.name.eq("teamA"))
                        .fetch();
        System.out.println(result.get(0).getUserName());
        System.out.println(result.get(1).getUserName());
        //jpa는 서브쿼리 없음
    }

    @Test
    public void subQuery() {
        QMember subMember = new QMember("subMember");
        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.eq(
                        JPAExpressions
                                .select(subMember.age.max())
                                .from(subMember)
                ))
                .fetch();
        System.out.println(result.size());
        //평균 나이이상 많은 멤버 찾기...
    }

    @Test
    public void subQuery02() {
        QMember subMember = new QMember("subMember");
        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.goe(
                        JPAExpressions
                                .select(subMember.age.avg())
                                .from(subMember)
                ))
                .fetch();
        System.out.println(result.size());
        //평균 나이이상 많은 멤버 찾기...
        //from절에 subquery안됨 native로 짜서 쓴다.
    }

    @Test
    public void toDto() {
        /*
        List<Tuple> result =
                queryFactory
                        .select(member.userName,member.age)
                        .from(member)
                        .fetch();
        for(int i=0;i<result.size();i++) {
            //System.out.println(result.get(i));
            String userName = result.get(i).get(member.userName);
            Integer age = result.get(i).get(member.age);
            System.out.println(userName+"==="+age);
        }
        */
//        List<MemberDto> result =
//                queryFactory
//                        .select(Projections.bean(MemberDto.class,
//                                member.userName.as("name"), member.age))
//                        .from(member)
//                        .fetch();
//        for (int i = 0; i < result.size(); i++) {
//            MemberDto memerDto = result.get(i);
//            System.out.println(memerDto.getName() + "==" + memerDto.getAge());
//        }
        List<MemberDto> result =
                queryFactory
                        .select(Projections.bean(MemberDto.class,
                                member.userName.as("name"), member.age))
                        .from(member)
                        .fetch();
        for (int i = 0; i < result.size(); i++) {
            MemberDto memerDto = result.get(i);
            System.out.println(memerDto.getName() + "==" + memerDto.getAge());
        }
    }
    //search   글쓴이, 제목 , 내용


    @Test
    public void 동적쿼리() {
        String paramUserName = null;
        Integer paramAge = 10;
        BooleanBuilder booleanBuilder = new BooleanBuilder();
        if(paramUserName!=null) {
            booleanBuilder.and(member.userName.eq(paramUserName));
        }
        if(paramAge!=null) {
            booleanBuilder.and(member.age.eq(paramAge));
        }
        //검색할때...
        List<Member> memberList =
                queryFactory
                        .select(member)
                        .from(member)
                        .where(booleanBuilder)
                        .fetch();
        System.out.println(memberList.size());
    }
}


