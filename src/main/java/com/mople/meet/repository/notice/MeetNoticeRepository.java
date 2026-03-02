package com.mople.meet.repository.notice;

import com.mople.entity.meet.notice.MeetNotice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface MeetNoticeRepository extends JpaRepository<MeetNotice, Long> {

    @Query(value = "select version from meet where notice_id = :noticeId", nativeQuery = true)
    long findVersion(Long noticeId);

    @Modifying
    @Query("update MeetNotice n set n.pinned = false where n.meetId = :meetId and n.pinned = true")
    void unpinAllByMeetId(Long meetId);

    @Query("select n from MeetNotice n where n.meetId = :meetId and n.pinned = true")
    MeetNotice findPinnedNotice(Long meetId);

    void deleteByMeetId(Long meetId);
}
