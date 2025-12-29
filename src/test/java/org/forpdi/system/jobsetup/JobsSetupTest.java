package org.forpdi.system.jobsetup;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class JobsSetupTest {


    @Test
    @DisplayName("Deve verificar se todas as constantes JobsSetup tem valor correto")
    void test_job_setup_constants_initialization() {
        assertEquals(60_000, JobsSetup.EMAIL_SENDER_FIXED_RATE, "EMAIL_SENDER_FIXED_RATE should be 60000");
        assertEquals(5000, JobsSetup.GOALS_GENERATION_FIXED_RATE, "GOALS_GENERATION_FIXED_RATE should be 5000");
        assertEquals(60_000, JobsSetup.ON_LEVEL_INSTANCE_UPDATE_FIXED_RATE, "ON_LEVEL_INSTANCE_UPDATE_FIXED_RATE should be 60000");
        assertEquals(3_600_000, JobsSetup.RISK_TASK_FIXED_RATE, "RISK_TASK_FIXED_RATE should be 3600000");

        assertEquals("0 0 0 * * ?", JobsSetup.GOALS_INFO_CALCULATOR_TASK_CRON, "GOALS_INFO_CALCULATOR_TASK_CRON should be '0 0 0 * * ?'");
        assertEquals("0 0 13 * * ?", JobsSetup.SCHEDULE_DASHBOARD_CRON, "SCHEDULE_DASHBOARD_CRON should be '0 0 13 * * ?'");
        assertEquals("0 0 13 * * ?", JobsSetup.SCHEDULED_NOTIFICATION_CRON, "SCHEDULED_NOTIFICATION_CRON should be '0 0 13 * * ?'");
        assertEquals("0 0 23 * * ?", JobsSetup.CLEAN_REFRESH_TOKEN_BLACKLIST_CRON, "CLEAN_REFRESH_TOKEN_BLACKLIST_CRON should be '0 0 23 * * ?'");
    }
}
