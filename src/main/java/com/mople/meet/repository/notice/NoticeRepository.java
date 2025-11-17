package com.mople.meet.repository.notice;

import com.mople.entity.meet.notice.MeetNotice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface NoticeRepository extends JpaRepository<MeetNotice, Long> {

    @Query(value = "select version from meet where notice_id = :noticeId", nativeQuery = true)
    long findVersion(Long noticeId);
}
