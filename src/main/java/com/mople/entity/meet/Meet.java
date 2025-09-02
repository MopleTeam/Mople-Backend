package com.mople.entity.meet;

import com.mople.entity.common.BaseTimeEntity;

import com.mople.global.enums.Status;
import jakarta.persistence.*;

import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "meet")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Meet extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "meet_id")
    private Long id;

    @Version
    private Long version;

    @Column(name = "name", length = 50, nullable = false)
    private String name;

    @Column(name = "meet_image")
    private String meetImage;

    @Column(name = "creator_id", nullable = false)
    private Long creatorId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 15)
    private Status status;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by")
    private Long deletedBy;

    @Builder
    public Meet(String name, String meetImage, Long creatorId) {
        this.name = name;
        this.meetImage = meetImage;
        this.creatorId = creatorId;
        this.status = Status.ACTIVE;
    }

    public void updateMeetInfo(String name, String imageName){
        this.name = name;
        this.meetImage = imageName;
    }

    public boolean matchCreator(Long userId){
        return creatorId.equals(userId);
    }

}
