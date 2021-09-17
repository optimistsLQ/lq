package com.foxconn.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HttpClientUtil {

	private String baseURL;
	private String loginURL;
	private String loginName;
	private String passWord;
	private HttpClient httpClient;
	
	public String getBaseURL() {
		return baseURL;
	}

	/**获取验证码图片流
	 * @return
	 */
	public InputStream getSRMValidCode() {
		if (httpClient == null) {
			httpClient = HttpClients.createDefault();
		}
		InputStream in = null;
		try {
			//先去那驗證碼圖片
			HttpGet get = new HttpGet("http://srm-help.foxconn.com/checkcode.aspx");
			HttpResponse validateResponse = httpClient.execute(get);
			in = validateResponse.getEntity().getContent();
			
		} catch (Exception e) {
			// TODO: handle exception
		}
		return in;
	}
	
	public String readProperties() {
		Properties pro = new Properties();
		
		try (FileInputStream in = new FileInputStream(new File("DataSource.properties"));){
			pro.load(in);
			this.loginURL = pro.getProperty("srm_loginURL");
			this.baseURL = pro.getProperty("srm_baseURL");
			this.loginName = pro.getProperty("srm_loginName");
			this.passWord = pro.getProperty("srm_passWord");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			
			e.printStackTrace();
			return "讀取配置文件失敗";
		}
		if (this.loginURL == null || this.baseURL == null || this.loginName == null || this.passWord == null) {
			return "請先配置系統資料";
		}
		return null;
	}
	
	/**
	 * 执行登录
	 */
	public Map<String,Object> login(String validCode) {
		HashMap<String, Object> map = new HashMap<String, Object>();
		
		String readProperties = readProperties();
		if (readProperties != null) {
			map.put("msg", readProperties);
			return map;
		}
		if (httpClient == null) {
			httpClient = HttpClients.createDefault();
		}
		try {
			//去登陸首頁拿取__VIEWSTATEGENERATOR和__VIEWSTATE的值
			Map<String, String> featureCode = Toolkit.FeatureCode(httpClient, loginURL);
			//構建post參數放入paramList
			ArrayList<NameValuePair> paramList = new ArrayList<NameValuePair>();
			paramList.add(new BasicNameValuePair("__VIEWSTATEGENERATOR", featureCode.get("__VIEWSTATEGENERATOR")));
			paramList.add(new BasicNameValuePair("__VIEWSTATE", featureCode.get("__VIEWSTATE")));
			paramList.add(new BasicNameValuePair("txtUsername", loginName));
			paramList.add(new BasicNameValuePair("txtPassword", passWord));
			paramList.add(new BasicNameValuePair("txtCheckCode", validCode));//验证码
			paramList.add(new BasicNameValuePair("imgReset.x", "26"));//登陸按鈕X坐標
			paramList.add(new BasicNameValuePair("imgReset.y", "12"));//登陸按鈕Y坐標
			HttpPost post = new HttpPost(loginURL);
			post.setEntity(new UrlEncodedFormEntity(paramList, "utf-8"));
			HttpResponse response = httpClient.execute(post);
			HttpEntity entity = response.getEntity();
			try (
				InputStream is = entity.getContent();
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr);
		    ) {
//				System.out.println(response.getHeaders("location")[0]);
				if (response.getStatusLine().getStatusCode() == 302) {
					//成功爬取
					map.put("msg", "登录成功");
					map.put("httpClient", httpClient);
				} else {
					//爬取失敗
					String lineHtml = "";
					while ((lineHtml = br.readLine()) != null) {
						if (lineHtml.contains("請輸入正確的驗證碼")) {
							//驗證碼輸入錯誤
							map.put("msg", "驗證碼错误");
							break;
						}
						if (lineHtml.contains("用戶名或密碼錯誤，請重新輸入")) {
							//密碼輸入錯誤
							map.put("msg", "用戶名或密碼錯誤");
							break;
						}
						if (lineHtml.contains("The page cannot be found")) {
							//密碼輸入錯誤
							map.put("msg", "srm登錄網址錯誤");
							break;
						}
					}
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return map;
	}
	
	public Map<String, String> getFeatureCode(HttpClient httpClient, String url) throws Exception {
		Map<String, String> featureCode = Toolkit.FeatureCode(httpClient, url);
		return featureCode;
	}

}
