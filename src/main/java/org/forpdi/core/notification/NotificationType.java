package org.forpdi.core.notification;

import java.io.Serializable;

public enum NotificationType implements Serializable{
	WELCOME(1, "http://cloud.progolden.com.br/file/8341", true),
	ACCESSLEVEL_CHANGED(2, "http://cloud.progolden.com.br/file/8711", false),
	PERMISSION_CHANGED(3, "http://cloud.progolden.com.br/file/8336", false),
	PLAN_MACRO_CREATED(4, "http://cloud.progolden.com.br/file/8340", false),
	PLAN_CREATED(5, "http://cloud.progolden.com.br/file/8708", false),
	ATTRIBUTED_RESPONSIBLE(6, "http://cloud.progolden.com.br/file/8706", false),
	GOAL_CLOSED(7, "http://cloud.progolden.com.br/file/8337", false),
	PLAN_CLOSE_TO_MATURITY(8, "http://cloud.progolden.com.br/file/8342", false),
	GOAL_CLOSE_TO_MATURITY(9, "http://cloud.progolden.com.br/file/8709", false),
	LATE_GOAL(10, "http://cloud.progolden.com.br/file/8710", false),
	INVITE_USER(11, "", true),
	RECOVER_PASSWORD(12, "", true),
	ACTION_PLAN_CLOSED(13, "http://cloud.progolden.com.br/file/8337", false),
	LATE_ACTION_PLAN(14, "http://cloud.progolden.com.br/file/8342", false),
	ACTION_PLAN_CLOSE_TO_MATURITY(15, "http://cloud.progolden.com.br/file/8343", false),
	SEND_MESSAGE(16, "http://cloud.progolden.com.br/file/8498", false),
	GOAL_ATTRIBUTE_UPDATED(17, " http://cloud.progolden.com.br/file/8707", false),
	GOAL_OPENED(18, "http://cloud.progolden.com.br/file/8337", false),
	FORRISCO_PROCESS_CREATED(19, "", true),
	FORRISCO_RISK_CLOSE_TO_MATURITY(20, "http://cloud.progolden.com.br/file/8343",false),
	FORRISCO_USER_LINKED_TO_RISK(20, "http://cloud.progolden.com.br/file/8336", false),
	FORRISCO_MANAGER_LINKED_TO_RISK_ITEM(21, "", false),
	FORRISCO_MANAGER_RISK_UPDATED(22, "", false),
	FORRISCO_MANAGER_RISK_ITEM_UPDATED(23, "", false),
	USER_LINKED_TO_ACTION_PLAN(24, "", false);
	
	private Integer value;
	private String imageUrl;
	private boolean onlyEmail;
	
	public Integer getValue() {
		return value;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public boolean isOnlyEmail() {
		return onlyEmail;
	}

	private NotificationType(int value, String imageUrl, boolean onlyEmail){
		this.imageUrl = imageUrl;
		this.value = value;
		this.onlyEmail = onlyEmail;
	}
}
