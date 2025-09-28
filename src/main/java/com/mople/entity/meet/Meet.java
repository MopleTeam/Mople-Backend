package com.mople.entity.meet;

import com.mople.entity.common.BaseTimeEntity;
import com.mople.entity.meet.plan.MeetPlan;
import com.mople.entity.meet.review.PlanReview;
import com.mople.entity.user.User;

import jakarta.persistence.*;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "meet")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Meet extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "meet_id")
    private Long id;

    @Column(name = "name", length = 50, nullable = false)
    private String name;

    @Column(name = "meet_image")
    private String meetImage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id")
    private User creator;

    @OneToMany(mappedBy = "joinMeet", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MeetMember> members = new ArrayList<>();

    @OneToMany(mappedBy = "meet", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MeetPlan> plans = new ArrayList<>();

    @OneToMany(mappedBy = "meet", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PlanReview> reviews = new ArrayList<>();

    @Builder
    public Meet(Long id, String name, String meetImage, User creator) {
        this.id = id;
        this.name = name;
        this.meetImage = meetImage;
        this.creator = creator;
    }

    public void updateMeetInfo(String name, String imageName){
        this.name = name;
        this.meetImage = imageName;
    }

    public void addReview(PlanReview review){
        review.updateMeet(this);
        reviews.add(review);
    }

    public void addPlan(MeetPlan plan){
        plan.updateMeet(this);
        plans.add(plan);
    }

    public void removePlan(MeetPlan plan){
        plans.removeIf(p -> p.getId().equals(plan.getId()));
        plan.removeMeet();
    }

    public void addMember(MeetMember member){
        member.joinMeet(this);
        members.add(member);
    }

    public void removeMember(Long userId){
        plans.forEach(plan -> plan.removeParticipant(userId));
        members.removeIf(member -> member.getUser().getId().equals(userId));
    }

    public boolean meetMemberSearch(Long userId){
        return members.stream().anyMatch(member -> member.getUser().getId().equals(userId));
    }

    public boolean matchMember(Long userId){
        return members.stream().noneMatch(member -> member.findAnyUser(userId));
    }

    public boolean matchCreator(Long userId){
        return creator.getId().equals(userId);
    }

}
