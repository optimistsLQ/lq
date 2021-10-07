package com.foxconn.call;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient(name = "spring-cloud-userserver", fallback = UserCallBack.class)
public interface UserCall {

	@RequestMapping("/userserver.do/{user}")
	public String getUser(@PathVariable("user") String user);
}
