package com.mople.test.repo.plan;

import com.mople.dto.response.meet.UserAllDateResponse;
import com.mople.dto.response.meet.UserPageResponse;
import com.mople.meet.repository.impl.plan.PlanRepositorySupport;
import com.mople.test.base.util.TestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;

@DataJpaTest
@Transactional(readOnly = true)
@ActiveProfiles("test")
@Import({TestConfig.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PlanRepositorySupportTest {

    @Autowired
    private PlanRepositorySupport planRepositorySupport;

    @Test
    public void 페이징() {
        UserPageResponse planAndReviewPages = planRepositorySupport.getPlanAndReviewPages(2L, YearMonth.from(LocalDate.now()));
    }

    @Test
    public void 전체날짜조회() {
        UserAllDateResponse allDate = planRepositorySupport.getAllDate(2L);
    }
}