package com.foxconn.utils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ImgUtils {

	public static String imgToString(InputStream in) {
		String imgStr = null;
		try{  
	        // 将图片转换成字符串  
	        byte[] bytes = new byte[in.available()];  
	        in.read(bytes);  
	        // 生成字符串  
	        imgStr = byte2hex( bytes );  
	        System.out.println( imgStr);  
	          
	        // 将字符串转换成二进制，用于显示图片  
	        // 将上面生成的图片格式字符串 imgStr，还原成图片显示  
//	        byte[] imgByte = hex2byte( imgStr );  
//	        InputStream in = new ByteArrayInputStream( imgByte );  
//	          
//	        byte[] b = new byte[1024];  
//	        int nRead = 0;  
//	        while( ( nRead = in.read(b) ) != -1 ){  
//	            o.write( b, 0, nRead );  
//	        }  
//	        o.flush();  
//	        o.close();  
//	        in.close();  
	          
	          
	    }catch(Exception e){  
	        e.printStackTrace();  
	    }finally{  
	    	if (null != in) {
	    		try {
					in.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    	}
	    } 
		return imgStr;
	}
	
    public static String byte2hex(byte[] b) // 二进制转字符串  
    {  
       StringBuffer sb = new StringBuffer();  
       String stmp = "";  
       for (int n = 0; n < b.length; n++) {  
        stmp = Integer.toHexString(b[n] & 0XFF);  
        if (stmp.length() == 1){  
            sb.append("0" + stmp);  
        }else{  
            sb.append(stmp);  
        }  
          
       }  
       return sb.toString();  
    }  
      
    public static byte[] hex2byte(String str) { // 字符串转二进制  
        if (str == null)  
         return null;  
        str = str.trim();  
        int len = str.length();  
        if (len == 0 || len % 2 == 1)  
         return null;  
        byte[] b = new byte[len / 2];  
        try {  
         for (int i = 0; i < str.length(); i += 2) {  
          b[i / 2] = (byte) Integer.decode("0X" + str.substring(i, i + 2)).intValue();  
         }  
         return b;  
        } catch (Exception e) {  
         return null;  
        }  
     } 
    public static void main(String[] args) {
		File file = new File("C:\\Users\\C3410596\\Desktop\\checkcode.gif");
		try {
			InputStream in = new FileInputStream(file);
			String str = imgToString(in);
			System.out.println("验证码"+str);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
