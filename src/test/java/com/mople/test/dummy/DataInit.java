package com.mople.test.dummy;

import com.mople.entity.meet.Meet;
import com.mople.entity.meet.MeetMember;
import com.mople.entity.meet.plan.MeetPlan;
import com.mople.entity.meet.plan.PlanParticipant;
import com.mople.entity.meet.review.PlanReview;
import com.mople.entity.user.User;
import com.mople.global.enums.Role;
import com.mople.global.enums.SocialProvider;
import com.mople.meet.repository.MeetMemberRepository;
import com.mople.meet.repository.MeetRepository;
import com.mople.meet.repository.plan.MeetPlanRepository;
import com.mople.meet.repository.plan.PlanParticipantRepository;
import com.mople.meet.repository.review.PlanReviewRepository;
import com.mople.user.repository.UserRepository;
import net.datafaker.Faker;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@SpringBootTest
public class DataInit {
    @Autowired
    UserRepository userRepository;
    @Autowired
    MeetRepository meetingRepository;
    @Autowired
    MeetPlanRepository meetingPlanRepository;
    @Autowired
    PlanParticipantRepository planParticipantRepository;
    @Autowired
    PlanReviewRepository planReviewRepository;
    @Autowired
    MeetMemberRepository meetMemberRepository;
    Faker faker = new Faker(new Locale("ko", "ko"));

    @Test
    void insertUser() {
        List<User> users = new ArrayList<>(3000);
        for (int i = 0; i < 3000; i++) {
            users.add(userCreate());
        }

        userRepository.saveAll(users);
    }

    User userCreate() {
        String mail = faker.random().nextInt(200000) + faker.internet().emailAddress();
        User user =
                User.builder()
                        .email(mail)
                        .nickname(faker.regexify("[a-z0-9가-힣]{2,16}"))
                        .profileImg(faker.internet().image())
                        .lastLaunchAt(
                                LocalDateTime.from(
                                        faker.timeAndDate()
                                                .between(Instant.now().minus(Duration.ofDays(400)), Instant.now())
                                                .atZone(ZoneId.of("Asia/Seoul"))
                                )
                        )
                        .role(faker.options().option(Role.class))
                        .socialProvider(faker.options().option(SocialProvider.class))
                        .build();

        user.updateBadgeCount();

        return user;
    }

    @Test
    void insertMeeting() {
        List<User> users = userRepository.findAll();
        List<Meet> meetings = new ArrayList<>(3000);
        for (int i = 0; i < 3000; i++) {
            User user = users.get(i);
            meetings.add(meetingCreate(user));
        }

        meetingRepository.saveAll(meetings);
    }

    Meet meetingCreate(User user) {
        String joke = faker.joke().pun();

        return Meet.builder()
                .name(joke.length() > 49 ? joke.substring(0, 50) : joke)
                .creator(user)
                .meetImage(faker.internet().image())
                .build();
    }

//    @Test
//    void insertMeetingPlan() {
//        List<User> users = userRepository.findAll();
//        List<Meet> meetings = meetingRepository.findAll();
//        List<MeetPlan> meetPlans = new ArrayList<>(3000);
//        for (int i = 0; i < 3000; i++) {
//            meetPlans.add(meetingPlanCreate(users.get(faker.random().nextInt(3000)), meetings.get(faker.random().nextInt(3000))));
//        }
//        meetingPlanRepository.saveAll(meetPlans);
//    }

    MeetPlan meetingPlanCreate(User user, Meet meet) {
        LocalDateTime now = LocalDateTime.from(
                faker.timeAndDate()
                        .between(Instant.now(), Instant.now().plus(Duration.ofDays(10)))
                        .atZone(ZoneId.of("Asia/Seoul"))
        );
        return MeetPlan.builder()
                .name(faker.word().noun())
                .planTime(now)
                .address(faker.address().city())
                .latitude(BigDecimal.valueOf(Double.parseDouble(faker.address().latitude())))
                .longitude(BigDecimal.valueOf(Double.parseDouble(faker.address().longitude())))
                .creator(user)
                .meet(meet)
                .build();
    }

    PlanReview reviewCreate(User user, Meet meet) {
        LocalDateTime now = LocalDateTime.from(
                faker.timeAndDate()
                        .between(Instant.now().minus(Duration.ofDays(200)), Instant.now())
                        .atZone(ZoneId.of("Asia/Seoul"))
        );
        PlanReview build = PlanReview.builder()
                .name(faker.word().noun())
                .planTime(now)
                .address(faker.address().city())
                .latitude(BigDecimal.valueOf(Double.parseDouble(faker.address().latitude())))
                .longitude(BigDecimal.valueOf(Double.parseDouble(faker.address().longitude())))
                .build();
        build.updateMeet(meet);

        return build;
    }

    @Test
    void insertPlanAndReviewForPaging() {
        User user = userRepository.findById(2L).get();
        List<Meet> meetings = meetingRepository.findAll().subList(0, 200);
        List<MeetPlan> meetPlans = new ArrayList<>(200);


                List<MeetMember> meetMembers = new ArrayList<>(200);
        List<PlanParticipant> participants = new ArrayList<>(200);

        for(int i = 0; i < 200; i++){
            meetMembers.add(MeetMember.builder().user(user).joinMeet(meetings.get(i)).build());
        }
        meetMemberRepository.saveAll(meetMembers);

        for (int i = 0; i < 200; i++) {
            MeetPlan meetPlan = meetingPlanCreate(user, meetings.get(faker.random().nextInt(200)));
            meetPlans.add(meetPlan);
            participants.add(PlanParticipant.builder().plan(meetPlan).user(user).build());
        }
        meetingPlanRepository.saveAll(meetPlans);
        planParticipantRepository.saveAll(participants);


        List<PlanReview> reviews = new ArrayList<>(200);
        participants = new ArrayList<>(200);
        for (int i = 0; i < 200; i++) {
            PlanReview review = reviewCreate(user, meetings.get(faker.random().nextInt(200)));
            reviews.add(review);
            participants.add(PlanParticipant.builder().review(review).user(user).build());
        }
        planReviewRepository.saveAll(reviews);
        planParticipantRepository.saveAll(participants);

    }
}
