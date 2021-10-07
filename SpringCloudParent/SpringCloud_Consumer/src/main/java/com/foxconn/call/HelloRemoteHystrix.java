package com.foxconn.call;

import org.springframework.stereotype.Component;

@Component
public class HelloRemoteHystrix implements ProvideCall{

	@Override
	public String aaa(String name) {
		// TODO Auto-generated method stub
		return name+"远程调用失败，这是熔断器准备的第二手方案。。";
	}

}
