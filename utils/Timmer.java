package com.foxconn.utils;

import java.io.File;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.ResourceUtils;

import com.foxconn.controller.QueryController;

/**定时器每晚23点55分执行清除Excel文件
 * @author C3410596
 *
 */
@Configuration
@EnableScheduling
public class Timmer {

	@Autowired
	private QueryController query;
	/**
	 * 定时器，每晚23点55分00秒执行删除项目里的xlsx文件
	 */
	@Scheduled(cron = "0 55 23 * * ?")
//	@Scheduled(cron = "0/5 * * * * ?")
	public void configureTasks() {
//		LocalDateTime now = LocalDateTime.now();
//		System.out.println(now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
		File path;
		try {
			path = new File(ResourceUtils.getURL("classpath:").getPath());
			path = path.getParentFile().getParentFile();
			if (!path.exists()) {
				path = new File((new File("").getAbsolutePath() + "\\"));
				
			}
			File[] listFiles = path.listFiles();
			for (File file : listFiles) {
//				System.out.println(file.getName());
				if (file.getName().endsWith(".xlsx")) {
					file.delete();
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
		}
	}
	
	/**
	 * 定时器，每天自动回填结报单号
	 */
	@Scheduled(cron = "0 30 14 * * ?")
	public void setJBCode() {
		String dateStr = Utils.dateFormatStr(new Date(), "yyyyMM");
		String inputval = ">=" + dateStr + "01";
		String msg = query.getJieBaoCode(inputval, null);
		System.out.println("自动回写结报单号完毕："+msg);
	}
	
}
