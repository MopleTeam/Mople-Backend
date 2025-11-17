package com.mople.meet.repository.notice;

import com.mople.entity.meet.notice.MeetNotice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoticeRepository extends JpaRepository<MeetNotice, Long> {
}
