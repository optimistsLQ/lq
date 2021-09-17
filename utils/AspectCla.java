package com.foxconn.utils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Component
@Aspect
public class AspectCla {

	@AfterReturning("execution(* com.foxconn.controller.BillController.conpareData(..))")
	public void after() {
		Properties pro = new Properties();
		FileInputStream in = null;
		int i = 0;
		try {
			in = new FileInputStream("DataSource.properties");
			pro.load(in);
			String countNum = pro.getProperty("countNum");
			System.out.println("countNum:"+countNum);
			i = Integer.parseInt(countNum);
			i++;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		FileOutputStream out = null;
		try {
			out = new FileOutputStream("DataSource.properties");
			pro.setProperty("countNum", String.valueOf(i));
			pro.store(out, null);
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

	}
}
