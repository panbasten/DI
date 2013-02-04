package com.yonyou.bq8.di.web.model;

public enum Permission {
	R(4,"读"),
	W(2,"写"),
	X(1,"执行");
	
	private int val;
	private String desc;
	
	private Permission(int val, String desc){
		this.val = val;
		this.desc = desc;
	}
}
