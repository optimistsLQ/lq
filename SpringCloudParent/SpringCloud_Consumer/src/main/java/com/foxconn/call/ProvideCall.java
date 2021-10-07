package com.foxconn.call;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "spring-cloud-producer",fallback = HelloRemoteHystrix.class)
public interface ProvideCall {

	@RequestMapping("/aaa.do")
	public String aaa(@RequestParam(value = "name") String name);
}
