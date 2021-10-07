package com.foxconn.call;

import org.springframework.stereotype.Component;

@Component
public class UserCallBack implements UserCall {

	@Override
	public String getUser(String user) {
		// TODO Auto-generated method stub
		return user+"雪崩，熔断回调";
	}

}
