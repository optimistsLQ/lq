package com.foxconn.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.coyote.http11.OutputFilter;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.NTCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.formula.functions.Count;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFDataFormat;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.EasyExcelFactory;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.fasterxml.jackson.core.util.BufferRecycler;
import com.foxconn.controller.BillController;
import com.foxconn.entity.LaHuoDetail;

@Configuration
public class ERPClient {

	private String baseURL;
	private String loginURL;
	private String loginName;
	private String passWord;
	private String PCname;
	private HttpClient erpHttpClient;

	private static final Logger logger = LoggerFactory.getLogger(ERPClient.class);
	@Autowired
	private HttpClientUtil httpClientUtil;

	/**
	 * ERP???????????????
	 */
	public String login() {
		HashMap<String, String> map = new HashMap<String, String>();
		String readProperties = readProperties();
		if (readProperties != null) {
			return readProperties;
		}
		if (erpHttpClient == null) {
			System.out.println(loginName + " - " + passWord);
			PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();
			connManager.setMaxTotal(100);
			connManager.setDefaultMaxPerRoute(1000);
			CredentialsProvider credsProvider = new BasicCredentialsProvider();
			credsProvider.setCredentials(AuthScope.ANY, new NTCredentials(loginName, passWord, PCname, "IDSBG"));
			RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(3000).setRedirectsEnabled(false)
					.build();
			erpHttpClient = HttpClients.custom().setConnectionManager(connManager)
					.setDefaultRequestConfig(requestConfig).setDefaultCredentialsProvider(credsProvider).build();
		}
		String str = "";
		try {
			// ?????????????????????__VIEWSTATEGENERATOR???__VIEWSTATE??????
			Map<String, String> featureCode = Toolkit.FeatureCode(erpHttpClient, loginURL);
//			System.out.println("featureCode>>>"+featureCode);
			// ??????post????????????paramList
			ArrayList<NameValuePair> paramList = new ArrayList<NameValuePair>();
			paramList.add(new BasicNameValuePair("__VIEWSTATEGENERATOR", featureCode.get("__VIEWSTATEGENERATOR")));
			paramList.add(new BasicNameValuePair("__VIEWSTATE", featureCode.get("__VIEWSTATE")));
			paramList.add(new BasicNameValuePair("__EVENTVALIDATION", featureCode.get("__EVENTVALIDATION")));
			paramList.add(new BasicNameValuePair("__EVENTTARGET", "ADLogin"));
			paramList.add(new BasicNameValuePair("__EVENTARGUMENT", ""));
			paramList.add(new BasicNameValuePair("txtUserID", "??????????????????"));
			paramList.add(new BasicNameValuePair("txtPassword", "???????????????"));
			paramList.add(new BasicNameValuePair("txtPassword_t", ""));
			paramList.add(new BasicNameValuePair("txt_code", "?????????"));
			HttpPost post = new HttpPost(loginURL);
			post.setEntity(new UrlEncodedFormEntity(paramList, "utf-8"));
			HttpResponse response = erpHttpClient.execute(post);
//			System.out.println("????????????"+response.getStatusLine().getStatusCode());
			HttpEntity entity = response.getEntity();
			try (InputStream is = entity.getContent();
					InputStreamReader isr = new InputStreamReader(is);
					BufferedReader br = new BufferedReader(isr);) {
//				System.out.println(response.getHeaders("location")[0]);
				if (response.getStatusLine().getStatusCode() == 302) {
					// ????????????
					System.out.println("ERP????????????");
					str = "????????????";
				} else {
					// ????????????
					String lineHtml = "";
					while ((lineHtml = br.readLine()) != null) {
						if (lineHtml.contains("???????????????????????????")) {
							// ?????????????????????
							str = "???????????????";
							break;
						}
						if (lineHtml.contains("??????????????????????????????????????????")) {
							// ??????????????????
							str = "????????????????????????";
							break;
						}
						if (lineHtml.contains("The page cannot be found")) {
							// ??????????????????
							str = "srm??????????????????";
							break;
						}
						if (lineHtml.contains("401")) {
							// ??????????????????
							str = "ERP??????????????????";
							break;
						}
					}
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return str;
	}

	/**
	 * ??????properties??????????????????????????????
	 * 
	 * @return
	 */
	public String readProperties() {
		Properties pro = new Properties();

		try (FileInputStream in = new FileInputStream(new File("DataSource.properties"));) {
			pro.load(in);
			this.loginURL = pro.getProperty("erp_loginURL");
			this.baseURL = pro.getProperty("erp_baseURL");
			this.loginName = pro.getProperty("erp_loginName");
			this.passWord = pro.getProperty("erp_passWord");
			this.PCname = pro.getProperty("erp_PCname");
		} catch (Exception e) {
			// TODO Auto-generated catch block

			e.printStackTrace();
			return "????????????????????????";
		}
		if (this.loginURL == null || this.baseURL == null || this.loginName == null || this.passWord == null) {
			return "????????????????????????";
		}
		return null;
	}

	public Map<String, String> getFeatureCode(HttpClient httpClient, String url) throws Exception {
		Map<String, String> featureCode = Toolkit.FeatureCode(httpClient, url);
		return featureCode;
	}

	/**
	 * ?????????????????????
	 * 
	 * @return ??????map???????????????resultList??????????????????????????????GRN_stockinCode
	 */
	public Map<String, Object> getERPData_new() {
		List<LaHuoDetail> resultList = new ArrayList<LaHuoDetail>();// ?????????????????????????????????
		List<String> GRN_stockinCode = new ArrayList<String>();// ???????????????????????????

		String[] arr = { "WATCD@ON_TCP893", "WATMLBCD@ON_TCP893", "WATNPICD@ON_TCP883" };
		String[] BUarr = { "FATP", "MLB", "NPI" };
//		String [] BUarr = {"FATP-1110","MLB-1109","NPI-1115"};
		Map<String, String> featureCode = null;
		for (int i = 0; i < arr.length; i++) {
			try {
				// ???????????????,??????????????????ID
				featureCode = this.getFeatureCode(this.erpHttpClient,
						this.baseURL + "/Pages/TCSystem/TC_WATCHGRNReportCM.aspx?MENU_ID=MMR071");
				// ???????????????
				{
					HttpPost post = new HttpPost(
							this.baseURL + "/Reserved.ReportViewerWebControl.axd?OpType=SessionKeepAlive&ControlID="
									+ featureCode.get("ControlID"));
					post.setHeader("User-Agent",
							"Mozilla/5.0 (Windows NT 6.1; WOW64; Trident/7.0; rv:11.0) like Gecko");
					post.setHeader("X-Requested-With", "XMLHttpRequest");
					HttpResponse Response = erpHttpClient.execute(post);
					String html = EntityUtils.toString(Response.getEntity());
					System.out.println("???????????????:" + html);
				}
				// ???????????????
				{
					HttpPost post = new HttpPost(
							this.baseURL + "/Pages/TCSystem/TC_WATCHGRNReportCM.aspx?MENU_ID=MMR071");
					post.setHeader("User-Agent",
							"Mozilla/5.0 (Windows NT 6.1; WOW64; Trident/7.0; rv:11.0) like Gecko");
					post.setHeader("X-Requested-With", "XMLHttpRequest");
					ArrayList<NameValuePair> paramListSearch = new ArrayList<NameValuePair>();
					paramListSearch
							.add(new BasicNameValuePair("ScriptManager1", "ScriptManager1|ReportViewer1$ctl09$ctl00"));
					paramListSearch.add(new BasicNameValuePair("ScriptManager1_HiddenField", ""));
					paramListSearch.add(new BasicNameValuePair("__EVENTTARGET", ""));
					paramListSearch.add(new BasicNameValuePair("__EVENTARGUMENT", ""));
					paramListSearch.add(new BasicNameValuePair("__VIEWSTATE", featureCode.get("__VIEWSTATE")));
					paramListSearch.add(
							new BasicNameValuePair("__VIEWSTATEGENERATOR", featureCode.get("__VIEWSTATEGENERATOR")));
					paramListSearch
							.add(new BasicNameValuePair("__EVENTVALIDATION", featureCode.get("__EVENTVALIDATION")));
					paramListSearch.add(new BasicNameValuePair("ReportViewer1$ctl03$ctl00", ""));
					paramListSearch.add(new BasicNameValuePair("ReportViewer1$ctl03$ctl01", ""));
					paramListSearch.add(new BasicNameValuePair("ReportViewer1$isReportViewerInVs", ""));
					paramListSearch.add(new BasicNameValuePair("ReportViewer1$ctl15", ""));
					paramListSearch.add(new BasicNameValuePair("ReportViewer1$ctl16", "standards"));
					paramListSearch.add(new BasicNameValuePair("ReportViewer1$AsyncWait$HiddenCancelField", "False"));
					paramListSearch.add(new BasicNameValuePair("ReportViewer1$ctl09$ctl03$ddValue", arr[i]));
					paramListSearch.add(new BasicNameValuePair("ReportViewer1$ToggleParam$store", ""));
					paramListSearch.add(new BasicNameValuePair("ReportViewer1$ToggleParam$collapse", "false"));
					paramListSearch.add(new BasicNameValuePair("ReportViewer1$ctl13$ClientClickedId", ""));
					paramListSearch.add(new BasicNameValuePair("ReportViewer1$ctl12$store", ""));
					paramListSearch.add(new BasicNameValuePair("ReportViewer1$ctl12$collapse", "false"));
					paramListSearch.add(new BasicNameValuePair("ReportViewer1$ctl14$VisibilityState$ctl00", "None"));
					paramListSearch.add(new BasicNameValuePair("ReportViewer1$ctl14$ScrollPosition", ""));
					paramListSearch.add(new BasicNameValuePair("ReportViewer1$ctl14$ReportControl$ctl02", ""));
					paramListSearch.add(new BasicNameValuePair("ReportViewer1$ctl14$ReportControl$ctl03", ""));
					paramListSearch.add(new BasicNameValuePair("ReportViewer1$ctl14$ReportControl$ctl04", "100"));
					paramListSearch.add(new BasicNameValuePair("__ASYNCPOST", "true"));
					paramListSearch.add(new BasicNameValuePair("ReportViewer1$ctl09$ctl00", "View Report"));
					post.setEntity(new UrlEncodedFormEntity(paramListSearch, "utf-8"));
					HttpResponse Response = erpHttpClient.execute(post);
					if (Response.getStatusLine().getStatusCode() == 200) {
						try (InputStream in = Response.getEntity().getContent();
								InputStreamReader ir = new InputStreamReader(in);
								BufferedReader br = new BufferedReader(ir);) {
							String str = "";
							StringBuffer htmlAll = new StringBuffer();
							while ((str = br.readLine()) != null) {
								htmlAll.append(str + "\r\n");
							}
							featureCode.put("__VIEWSTATE", findStr("__VIEWSTATE\\|.+?\\|", htmlAll));
							featureCode.put("__VIEWSTATEGENERATOR", findStr("__VIEWSTATEGENERATOR\\|.+?\\|", htmlAll));
							featureCode.put("__EVENTVALIDATION", findStr("__EVENTVALIDATION\\|.+?\\|", htmlAll));
							featureCode.put("ReportSession",
									findStr("ReportSession=[a-zA-Z0-9]+", htmlAll).split("=")[1]);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					System.out.println("???????????????:");
				}
				// ???????????????
				{
					HttpPost post = new HttpPost(
							this.baseURL + "/Reserved.ReportViewerWebControl.axd?OpType=SessionKeepAlive&ControlID="
									+ featureCode.get("ControlID"));
					post.setHeader("User-Agent",
							"Mozilla/5.0 (Windows NT 6.1; WOW64; Trident/7.0; rv:11.0) like Gecko");
					post.setHeader("X-Requested-With", "XMLHttpRequest");
					HttpResponse Response = erpHttpClient.execute(post);
					String html = EntityUtils.toString(Response.getEntity());
					System.out.println("???????????????:" + html);
				}
				// ???????????????
				{
					HttpPost post = new HttpPost(
							this.baseURL + "/Pages/TCSystem/TC_WATCHGRNReportCM.aspx?MENU_ID=MMR071");
					post.setHeader("User-Agent",
							"Mozilla/5.0 (Windows NT 6.1; WOW64; Trident/7.0; rv:11.0) like Gecko");
					post.setHeader("X-Requested-With", "XMLHttpRequest");
					ArrayList<NameValuePair> paramListSearch = new ArrayList<NameValuePair>();
					paramListSearch.add(new BasicNameValuePair("ScriptManager1",
							"ScriptManager1|ReportViewer1$ctl14$Reserved_AsyncLoadTarget"));
					paramListSearch.add(new BasicNameValuePair("ReportViewer1$ctl15", "ltr"));
					paramListSearch.add(new BasicNameValuePair("ReportViewer1$ctl16", "standards"));
					paramListSearch.add(new BasicNameValuePair("ReportViewer1$AsyncWait$HiddenCancelField", "False"));
					paramListSearch.add(new BasicNameValuePair("ReportViewer1$ctl09$ctl03$ddValue", arr[i]));
					paramListSearch.add(new BasicNameValuePair("ReportViewer1$ToggleParam$collapse", "false"));
					paramListSearch.add(new BasicNameValuePair("null", "100"));
					paramListSearch.add(new BasicNameValuePair("ReportViewer1$ctl12$collapse", "false"));
					paramListSearch.add(new BasicNameValuePair("ReportViewer1$ctl14$VisibilityState$ctl00", "None"));
					paramListSearch.add(new BasicNameValuePair("ReportViewer1$ctl14$ReportControl$ctl04", "100"));
					paramListSearch.add(
							new BasicNameValuePair("__EVENTTARGET", "ReportViewer1$ctl14$Reserved_AsyncLoadTarget"));
					paramListSearch.add(new BasicNameValuePair("__EVENTARGUMENT", ""));
					paramListSearch.add(new BasicNameValuePair("__VIEWSTATE", featureCode.get("__VIEWSTATE")));
					paramListSearch.add(
							new BasicNameValuePair("__VIEWSTATEGENERATOR", featureCode.get("__VIEWSTATEGENERATOR")));
					paramListSearch
							.add(new BasicNameValuePair("__EVENTVALIDATION", featureCode.get("__EVENTVALIDATION")));
					paramListSearch.add(new BasicNameValuePair("__ASYNCPOST", "true"));
					paramListSearch.add(new BasicNameValuePair("ScriptManager1_HiddenField", ""));
					paramListSearch.add(new BasicNameValuePair("ReportViewer1$ctl03$ctl00", ""));
					paramListSearch.add(new BasicNameValuePair("ReportViewer1$ctl03$ctl01", ""));
					paramListSearch.add(new BasicNameValuePair("ReportViewer1$isReportViewerInVs", ""));
					paramListSearch.add(new BasicNameValuePair("ReportViewer1$ToggleParam$store", ""));
					paramListSearch.add(new BasicNameValuePair("ReportViewer1$ctl10$ctl00$CurrentPage", ""));
					paramListSearch.add(new BasicNameValuePair("ReportViewer1$ctl10$ctl03$ctl00", ""));
					paramListSearch.add(new BasicNameValuePair("ReportViewer1$ctl13$ClientClickedId", ""));
					paramListSearch.add(new BasicNameValuePair("ReportViewer1$ctl12$store", ""));
					paramListSearch.add(new BasicNameValuePair("ReportViewer1$ctl14$ScrollPosition", ""));
					paramListSearch.add(new BasicNameValuePair("ReportViewer1$ctl14$ReportControl$ctl02", ""));
					paramListSearch.add(new BasicNameValuePair("ReportViewer1$ctl14$ReportControl$ctl03", ""));
					post.setEntity(new UrlEncodedFormEntity(paramListSearch, "utf-8"));
					HttpResponse Response = erpHttpClient.execute(post);
					System.out.println("???5?????????:");
				}

				// ???6???????????????????????????
				{
					HttpGet get = new HttpGet(this.baseURL + "/Reserved.ReportViewerWebControl.axd?ReportSession="
							+ featureCode.get("ReportSession")
							+ "&Culture=1028&CultureOverrides=True&UICulture=1028&UICultureOverrides=True&ReportStack=1&ControlID="
							+ featureCode.get("ControlID")
							+ "&OpType=Export&FileName=MMR070_WATCH+GRN%e5%a0%b1%e8%a1%a8CM&ContentDisposition=OnlyHtmlInline&Format=EXCELOPENXML");
					HttpResponse response = erpHttpClient.execute(get);
					if (response.getStatusLine().getStatusCode() == 200) {
						AtomicInteger ii = new AtomicInteger(i);
						AtomicInteger count = new AtomicInteger(0);
						InputStream in = response.getEntity().getContent();
//						--------------------??????????????????
//						FileOutputStream out = new FileOutputStream(new File(BUarr[ii.intValue()]+"aaa.xlsx"));
//						byte [] b = new byte[1024];
//						int len = 0;
//						while ((len = in.read(b)) != -1) {
//							out.write(b, 0, len);
//						}
//						out.close();
//						in.close();
//						---------------------??????????????????
						EasyExcel.read(in, LaHuoDetail.class, new AnalysisEventListener<LaHuoDetail>() {

							@SuppressWarnings("deprecation")
							@Override
							public void invoke(LaHuoDetail data, AnalysisContext context) {
								// TODO Auto-generated method stub
								if (StringUtils.isEmpty(data.getStockinCode())) {
									return;
								}
								
								// ????????????KPS to ps ----------
								if (data.getUnit() != null && data.getUnit().toLowerCase().contains("k")) {
									data.setUnsettledNum(data.getUnsettledNum() * 1000);
									data.setUnit("ps");
								}
								// ????????????KPS to ps ----------
								String dantou = data.getFormHead();
								if (dantou.contains("?????????") || dantou.contains("con") || dantou.contains("Con")
									|| dantou.contains("Free") || dantou.contains("???") || dantou.contains("???")
									|| dantou.contains("free") || dantou.contains("????????? ")) {
									data.setFree("free");
								}
								count.getAndAdd(1);
								data.setBu(BUarr[ii.intValue()]);
//								logger.info("-------***----"+data);
								resultList.add(data);
								GRN_stockinCode.add(data.getStockinCode());
								
//								logger.info("#####>"+data.toString());
							}

							@Override
							public void doAfterAllAnalysed(AnalysisContext context) {
								// TODO Auto-generated method stub
							}
						}).sheet(0).headRowNumber(7).doRead();
						;
						System.out.println("??????????????????" + BUarr[ii.intValue()] + "  ~~  " + count.intValue() + "???");
					}

				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		Map<String, Object> resultMpa = new HashMap<String, Object>();
		resultMpa.put("data", resultList);
		resultMpa.put("GRNList", GRN_stockinCode);
		return resultMpa;
	}

	/**
	 * WATCD@ON_TCP893 WATMLBCD@ON_TCP893 WATNPICD@ON_TCP883 ??????ERP?????????????????????tiptop???????????????
	 */
	@SuppressWarnings("resource")
//	public void getERPData(String fileName) {
////		???????????????Excel
//		File file = new File(fileName);
//		XSSFWorkbook workbook = createExcel();
//		Sheet newSheet = workbook.getSheetAt(0);
//		int newRowIndex = 2;
//		
//		// ??????????????????????????????
//		XSSFCellStyle StyleDate = workbook.createCellStyle();
//		StyleDate.setDataFormat(workbook.createDataFormat().getFormat("yyyy-MM-dd"));
//
//		Map<String, String> featureCode = null;
//		String [] arr = {"WATCD@ON_TCP893","WATMLBCD@ON_TCP893","WATNPICD@ON_TCP883"};
//		String [] BUarr = {"FATP-1110","MLB-1109","NPI-1115"};
//		for (int i = 0; i < arr.length; i++) {
//			try {
//				// ???????????????,??????????????????ID
//				featureCode = this.getFeatureCode(this.erpHttpClient, this.baseURL + "/Pages/TCSystem/TC_WATCHGRNReportCM.aspx?MENU_ID=MMR071");
//				// ???????????????
//				{
//					HttpPost post = new HttpPost(this.baseURL + "/Reserved.ReportViewerWebControl.axd?OpType=SessionKeepAlive&ControlID="+featureCode.get("ControlID"));
//					post.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; Trident/7.0; rv:11.0) like Gecko");
//					post.setHeader("X-Requested-With", "XMLHttpRequest");
//					HttpResponse Response = erpHttpClient.execute(post);
//					String html = EntityUtils.toString(Response.getEntity());
//					System.out.println("???????????????:" + html);
//				}
//				// ???????????????
//				{
//					HttpPost post = new HttpPost(this.baseURL + "/Pages/TCSystem/TC_WATCHGRNReportCM.aspx?MENU_ID=MMR071");
//					post.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; Trident/7.0; rv:11.0) like Gecko");
//					post.setHeader("X-Requested-With", "XMLHttpRequest");
//					ArrayList<NameValuePair> paramListSearch = new ArrayList<NameValuePair>();
//					paramListSearch.add(new BasicNameValuePair("ScriptManager1", "ScriptManager1|ReportViewer1$ctl09$ctl00"));
//					paramListSearch.add(new BasicNameValuePair("ScriptManager1_HiddenField", ""));
//					paramListSearch.add(new BasicNameValuePair("__EVENTTARGET", ""));
//					paramListSearch.add(new BasicNameValuePair("__EVENTARGUMENT", ""));
//					paramListSearch.add(new BasicNameValuePair("__VIEWSTATE", featureCode.get("__VIEWSTATE")));
//					paramListSearch.add(new BasicNameValuePair("__VIEWSTATEGENERATOR", featureCode.get("__VIEWSTATEGENERATOR")));
//					paramListSearch.add(new BasicNameValuePair("__EVENTVALIDATION", featureCode.get("__EVENTVALIDATION")));
//					paramListSearch.add(new BasicNameValuePair("ReportViewer1$ctl03$ctl00", ""));
//					paramListSearch.add(new BasicNameValuePair("ReportViewer1$ctl03$ctl01", ""));
//					paramListSearch.add(new BasicNameValuePair("ReportViewer1$isReportViewerInVs", ""));
//					paramListSearch.add(new BasicNameValuePair("ReportViewer1$ctl15", ""));
//					paramListSearch.add(new BasicNameValuePair("ReportViewer1$ctl16", "standards"));
//					paramListSearch.add(new BasicNameValuePair("ReportViewer1$AsyncWait$HiddenCancelField", "False"));
//					paramListSearch.add(new BasicNameValuePair("ReportViewer1$ctl09$ctl03$ddValue", arr[i]));
//					paramListSearch.add(new BasicNameValuePair("ReportViewer1$ToggleParam$store", ""));
//					paramListSearch.add(new BasicNameValuePair("ReportViewer1$ToggleParam$collapse", "false"));
//					paramListSearch.add(new BasicNameValuePair("ReportViewer1$ctl13$ClientClickedId", ""));
//					paramListSearch.add(new BasicNameValuePair("ReportViewer1$ctl12$store", ""));
//					paramListSearch.add(new BasicNameValuePair("ReportViewer1$ctl12$collapse", "false"));
//					paramListSearch.add(new BasicNameValuePair("ReportViewer1$ctl14$VisibilityState$ctl00", "None"));
//					paramListSearch.add(new BasicNameValuePair("ReportViewer1$ctl14$ScrollPosition", ""));
//					paramListSearch.add(new BasicNameValuePair("ReportViewer1$ctl14$ReportControl$ctl02", ""));
//					paramListSearch.add(new BasicNameValuePair("ReportViewer1$ctl14$ReportControl$ctl03", ""));
//					paramListSearch.add(new BasicNameValuePair("ReportViewer1$ctl14$ReportControl$ctl04", "100"));
//					paramListSearch.add(new BasicNameValuePair("__ASYNCPOST", "true"));
//					paramListSearch.add(new BasicNameValuePair("ReportViewer1$ctl09$ctl00", "View Report"));
//					post.setEntity(new UrlEncodedFormEntity(paramListSearch, "utf-8"));
//					HttpResponse Response = erpHttpClient.execute(post);
//					if (Response.getStatusLine().getStatusCode() == 200) {
//						try(
//								InputStream in = Response.getEntity().getContent();
//								InputStreamReader ir = new InputStreamReader(in);
//								BufferedReader br = new BufferedReader(ir);
//								) {
//							String str = "";
//							StringBuffer htmlAll = new StringBuffer();
//							while((str = br.readLine()) != null) {
//								htmlAll.append(str + "\r\n");
//							}
//							featureCode.put("__VIEWSTATE", findStr("__VIEWSTATE\\|.+?\\|", htmlAll));
//							featureCode.put("__VIEWSTATEGENERATOR", findStr("__VIEWSTATEGENERATOR\\|.+?\\|", htmlAll));
//							featureCode.put("__EVENTVALIDATION", findStr("__EVENTVALIDATION\\|.+?\\|", htmlAll));
//							featureCode.put("ReportSession", findStr("ReportSession=[a-zA-Z0-9]+", htmlAll).split("=")[1]);
//						} catch (Exception e) {
//							e.printStackTrace();
//						}
//					}
//					System.out.println("???????????????:");
//				}
//				// ???????????????
//				{
//					HttpPost post = new HttpPost(this.baseURL + "/Reserved.ReportViewerWebControl.axd?OpType=SessionKeepAlive&ControlID="+featureCode.get("ControlID"));
//					post.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; Trident/7.0; rv:11.0) like Gecko");
//					post.setHeader("X-Requested-With", "XMLHttpRequest");
//					HttpResponse Response = erpHttpClient.execute(post);
//					String html = EntityUtils.toString(Response.getEntity());
//					System.out.println("???????????????:" + html);
//				}
//				// ???????????????
//				{
//					HttpPost post = new HttpPost(this.baseURL + "/Pages/TCSystem/TC_WATCHGRNReportCM.aspx?MENU_ID=MMR071");
//					post.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; Trident/7.0; rv:11.0) like Gecko");
//					post.setHeader("X-Requested-With", "XMLHttpRequest");
//					ArrayList<NameValuePair> paramListSearch = new ArrayList<NameValuePair>();
//					paramListSearch.add(new BasicNameValuePair("ScriptManager1", "ScriptManager1|ReportViewer1$ctl14$Reserved_AsyncLoadTarget"));
//					paramListSearch.add(new BasicNameValuePair("ReportViewer1$ctl15", "ltr"));
//					paramListSearch.add(new BasicNameValuePair("ReportViewer1$ctl16", "standards"));
//					paramListSearch.add(new BasicNameValuePair("ReportViewer1$AsyncWait$HiddenCancelField", "False"));
//					paramListSearch.add(new BasicNameValuePair("ReportViewer1$ctl09$ctl03$ddValue", arr[i]));
//					paramListSearch.add(new BasicNameValuePair("ReportViewer1$ToggleParam$collapse", "false"));
//					paramListSearch.add(new BasicNameValuePair("null", "100"));
//					paramListSearch.add(new BasicNameValuePair("ReportViewer1$ctl12$collapse", "false"));
//					paramListSearch.add(new BasicNameValuePair("ReportViewer1$ctl14$VisibilityState$ctl00", "None"));
//					paramListSearch.add(new BasicNameValuePair("ReportViewer1$ctl14$ReportControl$ctl04", "100"));
//					paramListSearch.add(new BasicNameValuePair("__EVENTTARGET", "ReportViewer1$ctl14$Reserved_AsyncLoadTarget"));
//					paramListSearch.add(new BasicNameValuePair("__EVENTARGUMENT", ""));
//					paramListSearch.add(new BasicNameValuePair("__VIEWSTATE", featureCode.get("__VIEWSTATE")));
//					paramListSearch.add(new BasicNameValuePair("__VIEWSTATEGENERATOR", featureCode.get("__VIEWSTATEGENERATOR")));
//					paramListSearch.add(new BasicNameValuePair("__EVENTVALIDATION", featureCode.get("__EVENTVALIDATION")));
//					paramListSearch.add(new BasicNameValuePair("__ASYNCPOST", "true"));
//					paramListSearch.add(new BasicNameValuePair("ScriptManager1_HiddenField", ""));
//					paramListSearch.add(new BasicNameValuePair("ReportViewer1$ctl03$ctl00", ""));
//					paramListSearch.add(new BasicNameValuePair("ReportViewer1$ctl03$ctl01", ""));
//					paramListSearch.add(new BasicNameValuePair("ReportViewer1$isReportViewerInVs", ""));
//					paramListSearch.add(new BasicNameValuePair("ReportViewer1$ToggleParam$store", ""));
//					paramListSearch.add(new BasicNameValuePair("ReportViewer1$ctl10$ctl00$CurrentPage", ""));
//					paramListSearch.add(new BasicNameValuePair("ReportViewer1$ctl10$ctl03$ctl00", ""));
//					paramListSearch.add(new BasicNameValuePair("ReportViewer1$ctl13$ClientClickedId", ""));
//					paramListSearch.add(new BasicNameValuePair("ReportViewer1$ctl12$store", ""));
//					paramListSearch.add(new BasicNameValuePair("ReportViewer1$ctl14$ScrollPosition", ""));
//					paramListSearch.add(new BasicNameValuePair("ReportViewer1$ctl14$ReportControl$ctl02", ""));
//					paramListSearch.add(new BasicNameValuePair("ReportViewer1$ctl14$ReportControl$ctl03", ""));
//					post.setEntity(new UrlEncodedFormEntity(paramListSearch, "utf-8"));
//					HttpResponse Response = erpHttpClient.execute(post);
//					System.out.println("???5?????????:");
//				}
//				
//				//???6???????????????????????????
//				{
//					HttpGet get = new HttpGet(this.baseURL + "/Reserved.ReportViewerWebControl.axd?ReportSession="+featureCode.get("ReportSession")+"&Culture=1028&CultureOverrides=True&UICulture=1028&UICultureOverrides=True&ReportStack=1&ControlID="+featureCode.get("ControlID")+"&OpType=Export&FileName=MMR070_WATCH+GRN%e5%a0%b1%e8%a1%a8CM&ContentDisposition=OnlyHtmlInline&Format=EXCELOPENXML");
//					HttpResponse response = erpHttpClient.execute(get);
//					if (response.getStatusLine().getStatusCode() == 200) {
//						try (InputStream in = response.getEntity().getContent();
//								FileOutputStream out = new FileOutputStream(file);) {
////							POI???????????????Excel
//							XSSFWorkbook erpWorkbook = new XSSFWorkbook(in);
//							XSSFSheet erpSheet = erpWorkbook.getSheetAt(0);
//							for (Row erpRow:erpSheet) {
//								if (erpRow.getRowNum() <= 6 || erpSheet.getLastRowNum() == erpRow.getRowNum()) {
//									continue;//???????????????????????????
//								}
//								Row newRow = newSheet.createRow(newRowIndex++);
//								newRow.createCell(0).setCellValue(BUarr[i]);
//								int newColumnIndex = 1;
//								for (Cell c:erpRow) {
//									if (c.getColumnIndex() == 23) {
//										continue;//???????????????
//									}
//									Cell newCell = newRow.createCell(newColumnIndex++);
//									switch(c.getCellTypeEnum()) {
//									case STRING:
//										newCell.setCellValue(c.getStringCellValue());
//										break;
//									case NUMERIC:
//										if (DateUtil.isCellDateFormatted(c)) {
//											newCell.setCellValue(c.getDateCellValue());
//											newCell.setCellStyle(StyleDate);
//										}else {
//											newCell.setCellValue(c.getNumericCellValue());
//										}
//										break;
//									}
//								}
//							}
//							workbook.write(out);
//							System.out.println("???6?????????:");
//						} catch (Exception e) {
//							// TODO: handle exception
//						}
//					}
//				}
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
//	}
//	
//	/**?????????????????????Excel??????
//	 * @return
//	 */
//	public static XSSFWorkbook createExcel() {
//		Map<String, String> topMap = new LinkedHashMap<String, String>();
//		topMap.put("BU", "bu");
//		topMap.put("????????????", "tradeType");
//		topMap.put("????????????", "formHead");
//		topMap.put("????????????", "changshangCode");
//		topMap.put("????????????", "changshangName");
//		topMap.put("HH VendorCode", "hhVendorCode");
//		topMap.put("????????????", "stockinCode");
//		topMap.put("????????????", "stockinTime");
//		topMap.put("????????????", "pruchaseCode");
//		topMap.put("??????", "materiel");
//		topMap.put("????????????", "unit");
//		topMap.put("???????????????", "unsettledNum");
//		topMap.put("??????", "lurrency");
//		topMap.put("PO Price", "poPrice");
//		topMap.put("????????????", "stockinMoney");
//		topMap.put("Actual Price", "actualPrice");
//		topMap.put("AP price", "apPrice");
//		topMap.put("Action", "action");
//		topMap.put("Effective Date", "effectiveDate");
//		topMap.put("Effective Way", "effectiveWay");
//		topMap.put("????????????(HH)", "hhPaycondition");
//		topMap.put("????????????(HFJ)", "hfjPaycondition");
//		topMap.put("????????????(PO)", "poPaycondition");
//		topMap.put("?????????", "madein");
//		topMap.put("AP DRI", "apDri");
//		XSSFWorkbook workbook = new XSSFWorkbook();
//		XSSFSheet sheet = workbook.createSheet();
//		XSSFRow row0 = sheet.createRow(0);
//		XSSFRow row1 = sheet.createRow(1);
//		List<String> keyList = new ArrayList<>(topMap.keySet());
//		for(int i = 0; i < keyList.size(); i ++) {
//			row0.createCell(i).setCellValue(keyList.get(i));
//			row1.createCell(i).setCellValue(topMap.get(keyList.get(i)));
//		}
//		
//		return workbook;
//	}

	public String findStr(String regex, StringBuffer sourceStr) {
		Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(sourceStr);
		if (m.find()) {
			String temp = m.group();
			if (temp.contains("|")) {
				return temp.substring(temp.indexOf("|") + 1, temp.lastIndexOf("|"));
			} else {
				return temp;
			}
		}
		return null;
	}

	/**
	 * ??????????????????????????????
	 */
	public void getBackData(String fileName) {
		/// Pages/CMReport/FATPWithoutDeductionWOReport.aspx?MENU_ID=CMR159 HTTP/1.1
		HttpGet get = new HttpGet(this.baseURL + "/Pages/CMReport/FATPWithoutDeductionWOReport.aspx?MENU_ID=CMR159");
		String ControlID = "";
		try {
			HttpResponse response = erpHttpClient.execute(get);
			System.out.println("????????????" + response.getStatusLine().getStatusCode());
			if (response.getStatusLine().getStatusCode() == 200) {
				try (InputStream in = response.getEntity().getContent();
						InputStreamReader ir = new InputStreamReader(in);
						BufferedReader br = new BufferedReader(ir);) {
					String str = "";
					while ((str = br.readLine()) != null) {
						if (str.contains("ControlID")) {
//							System.out.println("str>>>"+str);
							ControlID = StringUtils.subString(str, "ControlID=", "OpType");
							ControlID = ControlID.substring(0, ControlID.length() - 6);
							break;
						}
					}

				} catch (Exception e) {
					// TODO: handle exception
				}

			}
			System.out.println(ControlID);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

//		??????????????????
		HttpGet getExcel = new HttpGet(this.baseURL
				+ "/Reserved.ReportViewerWebControl.axd?Culture=1028&CultureOverrides=True&UICulture=1028&UICultureOverrides=True&ReportStack=1&ControlID="
				+ ControlID
				+ "&OpType=Export&FileName=CMR154_Watch%e5%b7%b2%e7%b0%bd%e5%9b%9e%e6%9c%aa%e6%89%a3%e6%ac%be%e5%a0%b1%e8%a1%a8&ContentDisposition=OnlyHtmlInline&Format=EXCELOPENXML");
		File file = new File(fileName);
		InputStream in = null;
		FileOutputStream out = null;

		try {
			HttpResponse response = erpHttpClient.execute(getExcel);
			in = response.getEntity().getContent();
			out = new FileOutputStream(file);
			byte[] b = new byte[1024];
			int len = 0;
			while ((len = in.read(b)) != -1) {
				out.write(b, 0, len);
				out.flush();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				if (in != null) {
					in.close();
				}
				if (out != null) {
					out.close();
				}
			} catch (Exception e2) {
				// TODO: handle exception
				e2.printStackTrace();
			}
		}
	}

//	public static void main(String[] args) {
//		System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");
//		ERPClient ERPClient = new ERPClient();
//		String loginMsg = ERPClient.login();
//		if ("????????????".equals(loginMsg)) {
//			ERPClient.getERPData();
////			ERPClient.getBackData();
//		}
//		
//	}
}
