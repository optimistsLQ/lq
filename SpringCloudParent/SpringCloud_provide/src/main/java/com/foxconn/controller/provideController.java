package com.foxconn.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class provideController {

	@RequestMapping("/aaa.do")
	@ResponseBody
	public String aaa(@RequestParam String name) {
		System.out.println(name + "：开始提供服务");
		return "为-"+name+"-服务提供中。。。123123123";
	}
}
