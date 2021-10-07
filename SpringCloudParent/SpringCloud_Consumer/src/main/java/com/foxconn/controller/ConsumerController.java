package com.foxconn.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.foxconn.call.ProvideCall;
import com.foxconn.call.UserCall;

@Controller
public class ConsumerController {

	@Autowired
	private ProvideCall call;
	@RequestMapping("/onsumerCall.do/{name}")
	@ResponseBody
	public String onsumerCall(@PathVariable("name") String name) {
		System.out.println(name+"开始远程调用");
		return call.aaa(name);
	}
	
	@Autowired
	private UserCall userCall;
	@RequestMapping("bbbuser.do/{username}")
	@ResponseBody
	public String getuser(@PathVariable("username")String username) {
		System.out.println(username+"111");
		return userCall.getUser(username);
	}
	
}
