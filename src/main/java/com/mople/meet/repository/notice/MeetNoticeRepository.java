package com.mople.meet.repository.notice;

import com.mople.entity.meet.notice.MeetNotice;
import com.mople.global.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MeetNoticeRepository extends JpaRepository<MeetNotice, Long> {

    @Query(value = "select version from meet_notice where notice_id = :noticeId", nativeQuery = true)
    long findVersion(Long noticeId);

    @Query("select n from MeetNotice n where n.id = :id and n.status = :status")
    Optional<MeetNotice> findByIdAndStatus(Long id, Status status);

    @Modifying
    @Query("update MeetNotice n set n.pinnedAt = null where n.meetId = :meetId and n.pinnedAt is not null")
    void unpinAllByMeetId(Long meetId);

    @Query("select n from MeetNotice n where n.meetId = :meetId and n.pinnedAt is not null")
    MeetNotice findPinnedNotice(Long meetId);

    @Modifying(flushAutomatically = true)
    @Query(
            "update MeetNotice n " +
                    "   set n.status = :status, " +
                    "       n.deletedAt = :deletedAt, " +
                    "       n.deletedBy = :userId " +
                    " where n.id in :noticeIds " +
                    "   and n.status <> :status"
    )
    int softDeleteAll(Status status, List<Long> noticeIds, Long userId, LocalDateTime deletedAt);

    @Query(
            "select n.id " +
                    "  from MeetNotice n " +
                    " where n.meetId = :meetId " +
                    "   and n.status = com.mople.global.enums.Status.ACTIVE"
    )
    List<Long> findIdsByMeetId(Long meetId);

    @Modifying(flushAutomatically = true)
    @Query(
            "delete from MeetNotice n " +
                    "      where n.id = :noticeId " +
                    "        and n.status = com.mople.global.enums.Status.DELETED"
    )
    void hardDeleteById(Long noticeId);
}
