package com.mople.meet.service.notice;

import com.mople.global.enums.notice.SystemNotice;
import com.mople.meet.repository.notice.NoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static com.mople.entity.meet.notice.MeetNotice.ofSystem;

@Service
@RequiredArgsConstructor
public class NoticeSystemService {

    private final NoticeRepository noticeRepository;

    @Transactional(propagation = Propagation.MANDATORY)
    public void publishSystemNotice(Long meetId, SystemNotice notice, Object... args) {
        noticeRepository.save(
                ofSystem(notice.format(args), meetId)
        );
    }
}
