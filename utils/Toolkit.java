package com.foxconn.utils;

import java.awt.Desktop;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;

import javax.swing.JOptionPane;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;

import com.alibaba.druid.util.StringUtils;


public abstract class Toolkit extends java.awt.Toolkit
{	
//	public static Map<String, String> product = new HashMap<String, String>();
//	public static List<String> productList = new ArrayList<String>();
//	public static List<String> productNameList = new ArrayList<String>();
////	public static List<MainWnd> mainWndList = new ArrayList<MainWnd>();
//	public static List<String> imac_product = new ArrayList<String>();
//	
//	static
//	{
//		initProduct();
//	}
	
//	private static void initLocalConfig()
//	{
//		try
//		{
//			String [] configFile = new String[]{
//					"Database/FATP",
//					"Database/INC",
//					"Database/RE",
//					"Database/FATP/FatpQueryWoOver_Config/ModuleType.txt",
//					"Database/FATP/FatpQueryWoOver_Config/FatpWoInfo.txt",
//					"Database/Product.ini"};
//			for(String configFileName:configFile)
//			{
//				if(!new File(configFileName).exists())
//				{
//					new File("Product.ini").delete();
//					delFile("Database");
//				}
//			}
//			File file = new File("Database");
//			if(!file.exists())
//			{
//				InputStream is = Thread.currentThread()
//					.getContextClassLoader().getResourceAsStream("localData.zip");
//				File f = new File("localData.zip");
//				if(!f.exists()) f.createNewFile();
//				OutputStream os = new FileOutputStream(f);
//				int len = 0;
//				byte [] b = new byte[1024];
//				while((len=is.read(b))!=-1)
//				{
//					os.write(b, 0, len);
//				}
//				os.close();
//				is.close();
//				Toolkit.unzip(f);
//				f.delete();
//			}
//		}
//		catch(Exception e)
//		{
//			e.printStackTrace();
//		}
//	}
	
	public static String getTimeNonSundays(String regx, int amount)
	{
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		calendar.add(5, amount);
		if(calendar.get(Calendar.DAY_OF_WEEK) == 1)
		{
			calendar.setTime(new Date());
			calendar.add(5, amount-1);
		}
		return new SimpleDateFormat(regx).format(calendar.getTime());
	}
	
	public synchronized static File createOnlyFileNameTo_XLS(String newFileName)
	{
		String fileName = "Cache/"+newFileName;
		String endName = ".xls";
		File newFile = new File(fileName+endName);
		if(newFile.exists() && (!newFile.delete()))
		{
			for(int nameIndex = 1;;nameIndex++)
			{
				newFile = new File(fileName+"_"+nameIndex+endName);
				if(!newFile.exists()) break;
				if(newFile.exists() && newFile.delete()) break;
			}
		}
		return newFile;
	}
	
//	public static void updateProduct()
//	{
//		product.clear();
//		productList.clear();
//		productNameList.clear();
//		imac_product.clear();
//		initProduct();
//	}
//	
//	private static void initProduct()
//	{
//	    for (String str : readConfig("Database/SFC_Product.ini"))
//	    {
//	       String[] temp = str.split("=");
//	       product.put(temp[0], temp[1]);
//	       productList.add(temp[0]);
//	       if(!productNameList.contains(temp[1]))
//	       {
//	    	   productNameList.add(temp[1]);
//	       }
//	    }
//	    for (String str : readConfig("Database/IMAC_Product.ini"))
//	    {
//	    	imac_product.add(str);
//	    }
//	}
//	
	public static void desktopOpen(String fileName)
	{
		try 
		{
			Desktop.getDesktop().open(new File(fileName));
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
	
	private static List<String> readConfig(String configName)
	{
	    List<String> list = new ArrayList<String>();
	    try
	    {
	    	InputStream is = new FileInputStream(configName);
	    	InputStreamReader isr = new InputStreamReader(is,"BIG5");
	    	BufferedReader br = new BufferedReader(isr);
	    	for (String value = ""; (value = br.readLine()) != null;) 
	    	{
	    		if ((value.trim().length() > 1) && (!value.contains("#"))) 
	    		{
	    			list.add(value);
	    		}
	      	}
	    	br.close();
	    	isr.close();
	    	is.close();
	    	return list;
	    }
	    catch (Exception e)
	    {
	    	e.printStackTrace();
	    }
	    return null;
	}
	
	public static String getTime(String regx, int amount)
	{
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		calendar.add(5, amount);
		return new SimpleDateFormat(regx).format(calendar.getTime());
	}
	
	public static String timeComputeDay(String sourceTime, int amount) throws Exception
	{
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(simpleDateFormat.parse(sourceTime));
		calendar.add(5, amount);
		return simpleDateFormat.format(calendar.getTime());
	}
	
	public static void sleep(long millis)
	{
		try
		{
			Thread.sleep(millis);
		}
		catch (InterruptedException e)
		{
			e = null;
		}
	}
	
	public static Process exeCmd(String cmd)
	{
		try
		{
			return Runtime.getRuntime().exec(cmd);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return null;
	}
	
	public static Map<String, String> FeatureCode(HttpClient client, String url) 
		throws Exception
	{
		return FeatureCode(client, url, false);
	}
	
	public static Map<String, String> FeatureCode(HttpClient client, String url, boolean isCopyright) 
		throws Exception
	{
		Map<String, String> value = new HashMap<String, String>();
		HttpGet get = new HttpGet(url);
		RequestConfig config = RequestConfig.custom()
			.setConnectTimeout(2000)
			.build();
		get.setConfig(config);
		HttpResponse response = client.execute(get);
		HttpEntity entity = response.getEntity();
		InputStream is = entity.getContent();
		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader br = new BufferedReader(isr);
		for (String html = ""; (html = br.readLine()) != null;) 
		{
			if (html.indexOf("\"__VIEWSTATE\"") != -1)
			{
				value.put("__VIEWSTATE", getPageValue(html));
			} 
			if (html.indexOf("\"__EVENTVALIDATION\"") != -1) 
			{
				value.put("__EVENTVALIDATION", getPageValue(html));
			}
			if (html.indexOf("\"__VIEWSTATEGENERATOR\"") != -1)
			{
				value.put("__VIEWSTATEGENERATOR", getPageValue(html));
			}
			if (html.contains("ControlID")) 
			{
				String controlID = StringUtils.subString(html, "ControlID=", "\",\"");
				value.put("ControlID", controlID);
			}
			if (isCopyright && html.toLowerCase().indexOf("copyright") != -1)
			{
				html = html.trim();
				value.put("__Copyright", html.substring(html.indexOf(">")+1,html.lastIndexOf("<")));
			}
		}
		br.close();
		isr.close();
		is.close();
		return value;
	}
	
	public static Map<String, String> FeatureCode(String url)
	{
	    try
	    {
	    	Map<String, String> value = new HashMap<String, String>();
	    	HttpURLConnection get = (HttpURLConnection)new URL(url).openConnection();
	    	get.addRequestProperty("User-Agent", 
	        	"Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.1; Trident/4.0; .NET CLR 2.0.50727; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729; .NET4.0C; .NET4.0E)");
	    	InputStream is = get.getInputStream();
	    	InputStreamReader isr = new InputStreamReader(is);
	    	BufferedReader br = new BufferedReader(isr);
	      	for (String html = ""; (html = br.readLine()) != null;) 
	      	{
				if (html.indexOf("\"__VIEWSTATE\"") != -1)
				{
					value.put("__VIEWSTATE", getPageValue(html));
				} 
				if (html.indexOf("\"__EVENTVALIDATION\"") != -1) 
				{
					value.put("__EVENTVALIDATION", getPageValue(html));
				}
				if (html.indexOf("\"__VIEWSTATEGENERATOR\"") != -1)
				{
					value.put("__VIEWSTATEGENERATOR", getPageValue(html));
				}
	      	}
	    	br.close();
	    	isr.close();
	    	is.close();
	    	return value;
	    }
	    catch (Exception e)
	    {
	    	e.printStackTrace();
	    }
	    return null;
	}
	
	public static String getPageValue(String html)
	{
		String temp = html.substring(html.toLowerCase().indexOf("value="));
		temp = temp.substring(temp.indexOf("\"") + 1, temp.lastIndexOf("\""));
		return temp;
	}
	
	@SuppressWarnings("deprecation")
	public static void Logout(HttpClient client, String serviceAddress)
	{
		try
		{
			HttpGet httpGet = new HttpGet(serviceAddress + "Login.aspx?Logout=1");
			HttpResponse response = client.execute(httpGet);
			response.getEntity().getContent().close();
			client.getConnectionManager().shutdown();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static String getTdValue(String tr, int index)
	{
		String[] td = tr.split("</TD>");
		return td[index].replace("<TD>", "").replace("<TR>", "");
	}
	
	public static String getTdValueLowerCase(String tr, int index)
	{
		String[] td = tr.split("</td>");
		return td[index].replace("<td>", "").replace("<tr>", "").replace("</tr>", "");
	}
	
//	public static File loadConfigFile(MainWnd mainWnd,String configFileName)
//	{
//		try
//		{
//			File configFile = new File(configFileName);
//			if(!configFile.exists())
//			{
//				JOptionPane.showMessageDialog(mainWnd, "缺少"+configFileName+"配置文件,请重新安装程序!", "ERROR", JOptionPane.ERROR_MESSAGE);
//				System.exit(0);
//			}
//			else if((!configFile.canRead()) || (!configFile.canWrite()))
//			{
//				JOptionPane.showMessageDialog(mainWnd, "配置文件"+configFileName+"拒绝存取,请联系软件作者处理!", "ERROR", JOptionPane.ERROR_MESSAGE);
//				System.exit(0);
//			}
//			return configFile;
//		}
//		catch(Exception e)
//		{
//			e.printStackTrace();
//		}
//		JOptionPane.showMessageDialog(mainWnd, "加载配置文件发生错误!", "ERROR", JOptionPane.ERROR_MESSAGE);
//		System.exit(0);
//		return null;
//	}
	
	public static void waitThreadOver(ThreadGroup group)
	{
		while(true)
		{
			if(group.activeCount()<=0)
			{
				break;
			}
			sleep(100);
		}
	}
	
	public static void copySystem(String str)
	{
	    Clipboard clipboard = getDefaultToolkit().getSystemClipboard();
	    clipboard.setContents(new StringSelection(str), null);
	}
	
	public static void threadJoin(Thread t)
	{
		try
		{
			t.join();
		} 
		catch (InterruptedException e) 
		{
			e.printStackTrace();
		}
	}
	
	public static String getPropertiesValue(String fileName,String key)
	{
		InputStream is = null;
		try
		{
			is = new FileInputStream(fileName);
			Properties p = new Properties();
			p.load(is);
			return p.getProperty(key).trim();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if(is!=null) is.close();
			} 
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		return null;
	}
	
	public static String removeTableFirstTd(String tr)
	{
		return "<tr>"+tr.substring(tr.indexOf("</td>")+5, tr.length());
	}
	
	public static List<String> timeSplit(String stime,String shour,String etime,String ehour,int splitDay)
	{
		List<String> result = new ArrayList<String>();
		try 
		{
			String oldtime = "";
			SimpleDateFormat formatterMonth = new SimpleDateFormat("yyyy-MM-dd");
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			Calendar cl = Calendar.getInstance();
			long temp = ((formatter.parse(stime+" "+shour).getTime()-formatter.parse(etime+" "+ehour).getTime()) / 1000L / 3600L / 24L);
			if(temp > 0)
			{
				return null;
			}
			else if(temp >= ((splitDay-1)*-1))
			{
				result.add(stime+" "+shour+"="+etime+" "+ehour);
				return result;
			}
			else if(temp == (splitDay*-1))
			{
				if(!shour.equals(ehour))
				{
					result.add(stime+" "+shour+"="+etime+" "+shour);
					result.add(etime+" "+shour+"="+etime+" "+ehour);
					return result;
				}
				result.add(stime+" "+shour+"="+etime+" "+ehour);
				return result;
			}
			for(;;)
			{
				cl.setTime(formatterMonth.parse(stime));
				cl.add(6, splitDay);
			    if((cl.getTime().getTime()-formatterMonth.parse(etime).getTime())/ 1000 / 3600 / 24 > 0 )
			    {
			    	cl.setTime(formatterMonth.parse(oldtime));
			    	cl.add(6, splitDay);
			    	if(shour.equals(ehour) 
			    			&& (formatterMonth.format(cl.getTime()).equals(etime)))
			    	{
			    		break;
			    	}
			    	result.add(formatterMonth.format(cl.getTime())+" "+shour+"="+etime+" "+ehour);
			    	break;
			    }
			    oldtime = stime;
			    stime = formatterMonth.format(cl.getTime());
			    result.add(oldtime+" "+shour+"="+stime+" "+shour);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return result;
	}
	
	public static void addConifg(String fileName,String newKey,String newValue,String remake)
	{
		try
		{
			List<String> list = new ArrayList<String>();
			InputStream is = new FileInputStream(fileName);
			InputStreamReader isr = new InputStreamReader(is,"UTF-8");
			BufferedReader br = new BufferedReader(isr);
			for(String value = "";(value=br.readLine())!=null;)
			{
				list.add(value);
			}
			br.close();
			isr.close();
			is.close();
			list.add("\r\n#"+remake);
			list.add(newKey+"="+newValue);
			OutputStream os = new FileOutputStream(fileName);
			OutputStreamWriter osw = new OutputStreamWriter(os,"UTF-8");
			BufferedWriter bw = new BufferedWriter(osw);
			for(String value:list)
			{
				bw.write(value+"\r\n");
				bw.flush();
			}
			bw.close();
			osw.close();
			os.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void setConifg(String fileName,Map<String,String> keyValue)
	{
		try
		{
			List<String> list = new ArrayList<String>();
			InputStream is = new FileInputStream(fileName);
			InputStreamReader isr = new InputStreamReader(is,"UTF-8");
			BufferedReader br = new BufferedReader(isr);
			f:for(String value = "";(value=br.readLine())!=null;)
			{
				for(String key:keyValue.keySet())
				{
					if(value.contains("=") 
							&& value.split("=")[0].trim().equals(key.trim()))
					{
						list.add(key+"="+keyValue.get(key));
						continue f;
					}
				}
				list.add(value);
			}
			br.close();
			isr.close();
			is.close();
			
			OutputStream os = new FileOutputStream(fileName);
			OutputStreamWriter osw = new OutputStreamWriter(os,"UTF-8");
			BufferedWriter bw = new BufferedWriter(osw);
			for(String value:list)
			{
				bw.write(value+"\r\n");
				bw.flush();
			}
			bw.close();
			osw.close();
			os.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static String getPropertiesValueTo_UTF8(String fileName,String key)
	{
		try 
		{
			File f = new File(fileName);
			if(!f.exists()) return "Default";
			InputStream is = new FileInputStream(f);
			InputStreamReader isr = new InputStreamReader(is,"UTF-8");
			BufferedReader br = new BufferedReader(isr);
			for(String text = "";(text = br.readLine())!=null;)
			{
				if(text.contains("=") 
						&& text.split("=")[0].trim().equals(key))
				{
					return text.split("=")[1].trim();
				}
			}
			br.close();
			isr.close();
			is.close();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		return "Default";
	}
	
	public static void AppReset()
	{
		try
		{
			File restartFile = new File("RestartApp.exe");
			if(!restartFile.exists())
			{
				restartFile.createNewFile();
				InputStream is = Thread.currentThread()
					.getContextClassLoader().getResourceAsStream("com/zhcx/exe/RestartApp.exe");
				OutputStream os = new FileOutputStream(restartFile);
				int len = 0;
				byte [] b = new byte[1024];
				while((len=is.read(b))!=-1)
				{
					os.write(b, 0, len);
				}
				os.flush();
				os.close();
				is.close();
			}
			Desktop.getDesktop().open(restartFile);
			System.exit(0);
//			StringBuffer vbs = new StringBuffer("set ws = createobject(\"wscript.shell\")\r\n");
//	        vbs.append("wscript.sleep 1000\r\n");
//	        vbs.append("ws.run \"zhcx.exe\"\r\n");
//	        Writer w = new FileWriter("reset.vbs");
//	        w.write(vbs.toString());
//	        w.flush();
//	        w.close();
//	        File updateVbs = new File("reset.vbs");
//	        Desktop.getDesktop().open(updateVbs);
//	        System.exit(0);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
//	@SuppressWarnings("unchecked")
//	public static boolean checkZipFileDamage(String zipFileName)
//	{
//		try
//		{
//			ZipFile zipFile = new ZipFile(zipFileName);
//			for(Enumeration<ZipEntry> enumeration = zipFile.getEntries();enumeration.hasMoreElements();)
//			{
//				enumeration.nextElement();
//			}
//			zipFile.close();
//			return false;
//		}
//		catch(Exception e)
//		{
//			e = null;
//		}
//		return true;
//	}
	
//	@SuppressWarnings("unchecked")
//	public static void unzip(File zipName)
//	{
//		try
//		{
//			ZipFile zipFile = new ZipFile(zipName);//压缩文件的实列,并设置编码
//			//获取压缩文中的所以项
//			for(Enumeration<ZipEntry> enumeration = zipFile.getEntries();enumeration.hasMoreElements();)
//			{
//				ZipEntry zipEntry = enumeration.nextElement();//获取元素
//				if(zipEntry.getName().contains("\\"))
//				{
//					new File(zipEntry.getName().substring(0, zipEntry.getName().lastIndexOf("\\"))).mkdirs();
//				}
//				else if(zipEntry.getName().contains("/"))
//				{
//					new File(zipEntry.getName().substring(0, zipEntry.getName().lastIndexOf("/"))).mkdirs();
//				}
//				else if(zipEntry.isDirectory())
//				{
//					new File(zipEntry.getName()).mkdirs();
//				}
//				File f = new File(zipEntry.getName());
//				if(f.exists() && f.isDirectory()) continue;
//				OutputStream os = new FileOutputStream(f);//创建解压后的文件
//				BufferedOutputStream bos = new BufferedOutputStream(os);//带缓的写出流
//				InputStream is = zipFile.getInputStream(zipEntry);//读取元素
//				BufferedInputStream bis = new BufferedInputStream(is);//读取流的缓存流
//				CheckedInputStream cos = new CheckedInputStream(bis, new CRC32());//检查读取流，采用CRC32算法，保证文件的一致性
//				byte [] b = new byte[1024];//字节数组，每次读取1024个字节
//				int len = 0;
//				//循环读取压缩文件的值
//				while((len = cos.read(b))!=-1)
//				{
//					bos.write(b,0,len);//写入到新文件
//				}
//				cos.close();
//				bis.close();
//				is.close();
//				bos.close();
//				os.close();
//			}
//			zipFile.close();
//		}
//		catch(Exception e)
//		{
//			e.printStackTrace();
//		}
//	}
	
	public static String findStr(String text,String regex)
	{
		Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(text);
		if(m.find()) return m.group();
		return null;
	}
	
	public static void runExe(String fileName)
	{
		try
		{
			File file = new File(fileName);
			if(!file.exists())
			{
				file.createNewFile();
				InputStream is = Thread.currentThread()
					.getContextClassLoader().getResourceAsStream("com/zhcx/exe/"+fileName);
				OutputStream os = new FileOutputStream(file);
				int len = 0;
				byte [] b = new byte[1024];
				while((len=is.read(b))!=-1)
				{
					os.write(b, 0, len);
				}
				os.close();
				is.close();
			}
			Desktop.getDesktop().open(file);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static boolean checkProcessExists(String processName)
	{
		InputStream is = null;
		InputStreamReader isr = null;
		BufferedReader br = null;
		try
		{
			Process p = Runtime.getRuntime().exec("cmd /c tasklist");
			is = p.getInputStream();
			isr = new InputStreamReader(is);
			br = new BufferedReader(isr);
			for(String text = "";(text=br.readLine())!=null;)
			{
				if(text.contains(processName))
				{
					return true;
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if(br != null) br.close();
				if(isr != null) isr.close();
				if(is != null) is.close();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		return false;
	}
	
	private static StringBuffer byteTo16(byte[] b,int len)
	{
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < len; i++)
		{
			if ((b[i] & 0xFF) < 16)
			{
				buffer.append("0");
			}
			buffer.append(Long.toString(b[i] & 0xFF, 16).toUpperCase());
	    }
	    return buffer;
	}
	
	private static byte[] conver16HexToByte(String hex16Str)
	{
		char[] c = hex16Str.toCharArray();
		byte[] b = new byte[c.length / 2];
	    for (int i = 0; i < b.length; i++)
	    {
	    	int pos = i * 2;
	    	b[i] = ((byte)("0123456789ABCDEF".indexOf(c[pos]) << 4 | "0123456789ABCDEF".indexOf(c[(pos + 1)])));
	    }
	    return b;
	}
	
	public static File createTempFile(InputStream is, String tempFileName) throws Exception
	{
		File f = new File("Cache/"+tempFileName+".temp");
		OutputStream fos = new FileOutputStream(f);
		int len = 0;
		byte [] b = new byte[1024];
		while((len = is.read(b))!=-1)
		{
			String hex16 = byteTo16(b,len).toString().replaceAll("3C2F74723E", "3C2F74723E0D0A");
			fos.write(conver16HexToByte(hex16));
		}
		fos.flush();
		fos.close();
		is.close();
		return f;
	}
}