package org.forpdi.system.jobsetup;

// CRON: second, minute, hour, day of month, month, day(s) of week
public class JobsSetup {
	public static final long EMAIL_SENDER_FIXED_RATE = 60_000;
	public static final long GOALS_GENERATION_FIXED_RATE = 5000;
	public static final long ON_LEVEL_INSTANCE_UPDATE_FIXED_RATE = 60_000;
	public static final String GOALS_INFO_CALCULATOR_TASK_CRON = "0 0 0 * * ?";
	public static final int RISK_TASK_FIXED_RATE = 3_600_000; // 1 hour
	public static final String SCHEDULE_DASHBOARD_CRON = "0 0 13 * * ?";
	public static final String SCHEDULED_NOTIFICATION_CRON = "0 0 13 * * ?";
	public static final String CLEAN_REFRESH_TOKEN_BLACKLIST_CRON = "0 0 23 * * ?";
}
