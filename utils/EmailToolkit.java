package com.foxconn.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.URLEncoder;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.mail.Message.RecipientType;
import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

import org.apache.log4j.Logger;
import org.springframework.web.multipart.MultipartFile;

/**
 * 电子邮件发送工具类
 **/
public class EmailToolkit
{
    // 邮件发送协议 
    private final static String PROTOCOL = "smtp"; 
    // SMTP邮件服务器 
    private final static String HOST = "10.134.28.95"; 
    // SMTP邮件服务器默认端口 
    private final static String PORT = "25"; 
    // 是否要求身份认证 
    private final static String IS_AUTH = "true"; 
    // 是否启用调试模式（启用调试模式可打印客户端与服务器交互过程时一问一答的响应消息） 
    private final static String IS_ENABLED_DEBUG_MOD = "true";
    // 初始化连接邮件服务器的会话信息
    private static Properties props = null;
    // 发件人 
    private static String from = "WorkingSystem@mail.foxconn.com";
    
    static 
    { 
        props = new Properties(); 
        props.setProperty("mail.transport.protocol", PROTOCOL); 
        props.setProperty("mail.smtp.host", HOST); 
        props.setProperty("mail.smtp.port", PORT); 
        props.setProperty("mail.smtp.auth", IS_AUTH); 
        props.setProperty("mail.debug",IS_ENABLED_DEBUG_MOD); 
        props.setProperty("mail.smtp.starttls.enable","true"); 
        props.setProperty("mail.smtp.EnableSSL.enable","true");
        props.setProperty("mail.smtp.ssl.trust", HOST);
    }
    
    /**
     * 发送邮件
     * @param mailSubject 邮件主题
     * @param mailContent 邮件内容(HTML格式)
     * @param addressee 收件人地址,多个用","隔开
     */ 
    public static void sendEmail(String mailSubject,String mailContent,String addressee)
    { 
    	try
    	{
	        // 创建Session实例对象 
	        Session session = Session.getInstance(props, new MyAuthenticator()); 
	        // 创建MimeMessage实例对象 
	        MimeMessage message = new MimeMessage(session); 
	        // 设置邮件主题 
	        message.setSubject(mailSubject); 
	        // 设置发送人 
	        message.setFrom(new InternetAddress(from)); 
	        // 设置发送时间 
	        message.setSentDate(new Date()); 
	        // 设置收件人 
	        message.setRecipients(RecipientType.TO, InternetAddress.parse(addressee)); 
	        // 设置html内容为邮件正文，指定MIME类型为text/html类型，并指定字符编码为utf-8 
	        message.setContent("<div style='color:red;'>溫馨提示﹕此郵件為系統自動發送,無須回復,謝謝!</div>"
	        		+ "<br>"
	        		+ "<div>主管/同仁您好：</div>"
	        		+ "<br>"
	        		+ "<div>&nbsp;&nbsp;&nbsp;&nbsp;"+mailContent+"</div>","text/html;charset=utf-8"); 
	        // 保存并生成最终的邮件内容 
	        message.saveChanges();
	        // 发送邮件 
	        Transport.send(message); 
    	}
    	catch(Exception e)
    	{
    		Logger.getLogger(EmailToolkit.class).debug("發送郵件發生異常:", e);
    	}
    } 
    
    /**
     * 发送邮件
     * @param mailSubject 邮件主题
     * @param mailContent 邮件内容(HTML格式)
     * @param addressee 收件人地址,多个用","隔开
     * @param listFile 邮件附件,这里面放附件的相对地址
     * @return 
     */ 
    public static <T> boolean sendEmail(String mailSubject,String mailContent,String addressee,List<T> listFile)
    { 
    	try
    	{
	        // 创建Session实例对象 
	        Session session = Session.getInstance(props, new MyAuthenticator()); 
	        // 创建MimeMessage实例对象 
	        MimeMessage message = new MimeMessage(session); 
	        // 设置邮件主题 
	        message.setSubject(mailSubject); 
	        // 设置发送人 
	        message.setFrom(new InternetAddress(from)); 
	        // 设置发送时间 
	        message.setSentDate(new Date()); 
	        // 设置收件人 
	        message.setRecipients(RecipientType.TO, InternetAddress.parse(addressee)); 
	        // 设置多个邮件内容
	        MimeMultipart multipart = new MimeMultipart();
	        // 设置html内容为邮件正文，指定MIME类型为text/html类型，并指定字符编码为utf-8 
	        BodyPart contentPart = new MimeBodyPart();
	        contentPart.setContent("<div style='color:red;'>溫馨提示﹕此郵件為系統自動發送,無須回復,謝謝!</div>"
	        		+ "<br>"
	        		+ "<div>主管/同仁您好：</div>"
	        		+ "<br>"
	        		+ "<div>&nbsp;&nbsp;&nbsp;&nbsp;"+mailContent+"</div>", "text/html;charset=utf-8");
	        multipart.addBodyPart(contentPart);
	        // 设置邮件附件
	        if (null != listFile && listFile.size() != 0) {
	        	
		        for(T u:listFile)
		        {
			        MimeBodyPart mbp = new MimeBodyPart();
			        DataHandler dataHandler = null;
			        if (u instanceof File) {
			        	System.out.println("file类型");
			        	dataHandler = new DataHandler(new FileDataSource((File) u));
			        } else {
			        	System.out.println("String类型");
			        	dataHandler = new DataHandler(new FileDataSource((String) u));
			        }
			        mbp.setDataHandler(dataHandler);
			        mbp.setFileName(MimeUtility.encodeWord(Utils.simplifiedConverter(dataHandler.getName()), "BIG5", "B"));
			        multipart.addBodyPart(mbp);
		        }
	        }
	        message.setContent(multipart);
	        // 保存并生成最终的邮件内容 
	        message.saveChanges();
	        // 发送邮件 
	        Transport.send(message); 
	        return true;
    	}
    	catch(Exception e)
    	{
    		Logger.getLogger(EmailToolkit.class).debug("發送郵件發生異常:", e);
    		
    		return false;
    	}
    }
    
    /**
     * 发送邮件
     * @param mailSubject 邮件主题
     * @param mailContent 邮件内容(HTML格式)
     * @param addressee 收件人地址,多个用","隔开
     * @param listFile 邮件附件,这里面放附件的相对地址
     * @return 
     */ 
    public static boolean sendEmail(String mailSubject,String mailContent,List<File> imgList,String addressee,List<String> listFile)
    { 
    	try
    	{
	        // 创建Session实例对象 
	        Session session = Session.getInstance(props, new MyAuthenticator()); 
	        // 创建MimeMessage实例对象 
	        MimeMessage message = new MimeMessage(session); 
	        // 设置邮件主题 
	        message.setSubject(mailSubject); 
	        // 设置发送人 
	        message.setFrom(new InternetAddress(from)); 
	        // 设置发送时间 
	        message.setSentDate(new Date()); 
	        // 设置收件人 
	        message.setRecipients(RecipientType.TO, InternetAddress.parse(addressee)); 
	        // 设置多个邮件内容
	        MimeMultipart multipart = new MimeMultipart();
            String content = "<div style='color:red;'>溫馨提示﹕此郵件為系統自動發送,無須回復,謝謝!</div>"
	        		+ "<br>"
	        		+ "<div>主管/同仁您好：</div>"
	        		+ "<br>"
	        		+ "<div>&nbsp;&nbsp;&nbsp;&nbsp;"+mailContent+"</div>";
	        // 封装图片到邮件中
	        for (int i = 0; i<imgList.size(); i++) {
	            MimeBodyPart image = new MimeBodyPart();
	            image.setDataHandler(new DataHandler(new FileDataSource(imgList.get(i))));
	            image.setContentID("img_"+i);
	            multipart.addBodyPart(image);
	            content += "<br><img src='cid:"+("img_"+i)+"'>";
	        }
	        // 设置html内容为邮件正文，指定MIME类型为text/html类型，并指定字符编码为utf-8 
            MimeBodyPart contentPart = new MimeBodyPart();
	        contentPart.setContent(content, "text/html;charset=utf-8");
	        multipart.addBodyPart(contentPart);
	        // 设置邮件附件
	        for(String u:listFile)
	        {
		        MimeBodyPart mbp = new MimeBodyPart();
		        DataHandler dataHandler = new DataHandler(new FileDataSource(u));
		        mbp.setDataHandler(dataHandler);
		        mbp.setFileName(MimeUtility.encodeWord(dataHandler.getName()));
		        multipart.addBodyPart(mbp);
	        }
	        message.setContent(multipart);
	        // 保存并生成最终的邮件内容 
	        message.saveChanges();
	        // 发送邮件 
	        Transport.send(message); 
	        return true;
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    		Logger.getLogger(EmailToolkit.class).debug("發送郵件發生異常:", e);
    		return false;
    	}
    	finally
    	{
    		for (File f:imgList) {
    			f.delete();
    		}
    	}
    }
	
    /**
     * 向邮件服务器提交认证信息
     */ 
    static class MyAuthenticator extends Authenticator
    { 
 
        private String username = ""; 
 
        private String password = "";
 
        public MyAuthenticator() 
        { 
            super(); 
        } 
 
        public MyAuthenticator(String username, String password)
        { 
            super(); 
            this.username = username; 
            this.password = password; 
        } 
 
        @Override 
        protected PasswordAuthentication getPasswordAuthentication() 
        { 
            return new PasswordAuthentication(username, password); 
        } 
    }
}
