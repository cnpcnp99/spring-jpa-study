package study.datajpa.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.entity.Member;
import study.datajpa.entity.Team;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@Rollback(value = false)
class MemberRepositoryTest {

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    TeamRepository teamRepository;

    @PersistenceContext
    EntityManager em;

    @Autowired
    MemberQueryRepository memberQueryRepository;

    @Test
    public void testMember() throws Exception {
        // given
        Member member = new Member("memberA");
        Member savedMember = memberRepository.save(member);


        // when
        Member foundMember = memberRepository.findById(savedMember.getId()).get();

        // then
        assertThat(foundMember.getId()).isEqualTo(member.getId());
        assertThat(foundMember.getUsername()).isEqualTo(member.getUsername());
        assertThat(foundMember).isEqualTo(member);

    }

    @Test
    public void pagingTest() throws Exception {
        // given
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 10));
        memberRepository.save(new Member("member3", 10));
        memberRepository.save(new Member("member4", 10));
        memberRepository.save(new Member("member5", 10));

        int age = 10;
        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "username"));

        // when
        Page<Member> page = memberRepository.findByAge(age, pageRequest);
        // long totalCount = memberRepository.totalCount(age); => ????????? ?????????

        // then
        List<Member> content = page.getContent();

        assertThat(content.size()).isEqualTo(3);            // ????????? ????????? ??????
        assertThat(page.getTotalElements()).isEqualTo(5);   // ??? ?????? ??????
        assertThat(page.getNumber()).isEqualTo(0);          // ????????? ???????????? ??????
        assertThat(page.getTotalPages()).isEqualTo(2);      // ??? ????????? ??????
        assertThat(page.isFirst()).isTrue();                // ????????? ???????????? ??? ???????????????
        assertThat(page.hasNext()).isTrue();                // ????????? ??????????????? ?????? ???????????? ?????????
    }

    @Test
    public void pagingTestBySlice() throws Exception {
        // given
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 10));
        memberRepository.save(new Member("member3", 10));
        memberRepository.save(new Member("member4", 10));
        memberRepository.save(new Member("member5", 10));

        int age = 10;
        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "username"));

        // when
        Slice<Member> page = memberRepository.findSliceByAge(age, pageRequest);
        // long totalCount = memberRepository.totalCount(age); => ????????? ?????????

        // then
        List<Member> content = page.getContent();

        assertThat(content.size()).isEqualTo(3);            // ????????? ????????? ??????
//        assertThat(page.getTotalElements()).isEqualTo(5);   // ??? ?????? ?????? => Slice?????? ?????? ??????
        assertThat(page.getNumber()).isEqualTo(0);          // ????????? ???????????? ??????
//        assertThat(page.getTotalPages()).isEqualTo(2);      // ??? ????????? ?????? => Slice?????? ?????? ??????
        assertThat(page.isFirst()).isTrue();                // ????????? ???????????? ??? ???????????????
        assertThat(page.hasNext()).isTrue();                // ????????? ??????????????? ?????? ???????????? ?????????
    }

    @Test
    public void bulkUpdateJpaTest() throws Exception {
        // given
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 19));
        memberRepository.save(new Member("member3", 20));
        memberRepository.save(new Member("member4", 21));
        memberRepository.save(new Member("member5", 40));
        // when
        int resultCount = memberRepository.bulkAgePlus(20);
        // ?????? ????????? ????????? ??????????????? ???????????? ?????? db??? ??????????????? ?????? ?????? ?????? ?????? ?????? ????????? ??? ????????? ??????????????? ??????????????? or clearAutomatically = true ?????? d
        em.flush();
        em.clear();
        // then
        assertThat(resultCount).isEqualTo(3);
    }

    @Test
    public void findMemberLazy() throws Exception {
        // given
        Team teamA = new Team("team A");
        Team teamB = new Team("team B");

        teamRepository.save(teamA);
        teamRepository.save(teamB);
        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 10, teamB);
        memberRepository.save(member1);
        memberRepository.save(member2);
        em.flush();
        em.clear();
        // when
        List<Member> members = memberRepository.findMemberFetchJoin();
        for (Member member : members) {
            System.out.println("member.getUsername() = " + member.getUsername());
            System.out.println("member.getTeam().getClass() = " + member.getTeam().getClass());
            System.out.println("member.getTeam().getName() = " + member.getTeam().getName());
        }

        // then
    }

    @Test
    public void findMemberEntityGraph() throws Exception {
        // given
        Team teamA = new Team("team A");
        Team teamB = new Team("team B");

        teamRepository.save(teamA);
        teamRepository.save(teamB);
        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 10, teamB);
        memberRepository.save(member1);
        memberRepository.save(member2);
        em.flush();
        em.clear();

        // when
        List<Member> members = memberRepository.findAll();

        // then
        for (Member member : members) {
            System.out.println("member.getUsername() = " + member.getUsername());
            System.out.println("member.getTeam().getClass() = " + member.getTeam().getClass());
            System.out.println("member.getTeam().getName() = " + member.getTeam().getName());
        }

    }

    @Test
    public void queryHint() throws Exception {
        // given
        Member member1 = memberRepository.save(new Member("member1", 10));
        em.flush();
        em.clear();

        // when
        Member member = memberRepository.findReadOnlyByUsername("member1");
        member.setUsername("member2");

        em.flush();
        // then
    }

    @Test
    public void callCustom() throws Exception {
        // given
        List<Member> result = memberRepository.findMemberCustom();

        // when

        // then
    }

}