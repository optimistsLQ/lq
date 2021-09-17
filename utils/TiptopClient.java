package com.foxconn.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.net.ftp.FTPClient;

/**
 * Tiptop 客戶端實現
 * 整個通信只能是同步操作,故方法全部需要 synchronized
 * 
 **/
public class TiptopClient implements Runnable {
	
	private Socket tiptopSocket;
	private Socket realClientSocket;// 正真的客戶機,用於debug調試
	private String ip;
	private String loginResult;
	private Thread tiptopReadThread;
	private TiptopDown tiptopDown;
	
	/**
	 * 初始化常用指令集
	 **/
	private Map<String, String> instructionsMap = new HashMap<String, String>();
	
	public TiptopClient() {
		instructionsMap.put("[ESC]", "1b");// ESC
		instructionsMap.put("[CTRL+P]", "10");// Ctrl+P
		instructionsMap.put("[DELETE]", "7f");// Delete
		instructionsMap.put("[↓]", "1b5b42");// 下
		instructionsMap.put("[↑]", "1b5b41");// 上
		instructionsMap.put("[←]", "1b5b44");// 左
		instructionsMap.put("[→]", "1b5b43");// 右
	}
	
	/**
	 * 連接tiptop服務器
	 * @param ip ip地址
	 * @param port 端口
	 * @param isWaitingForRealClient 是否等待正在的客戶端連接,用於debug調試
	 * @return boolean 是否連接成功
	 **/
	public synchronized boolean connection(String ip, int port, boolean isWaitingForRealClient) {
		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket(23);
			System.out.println("DEBUG: Waiting for real client connection 23 port ");
			realClientSocket = serverSocket.accept();
			System.out.println("DEBUG: Real client connection success");
			return connection(ip, port);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (serverSocket != null) {
					serverSocket.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return false;
	}
	
	/**
	 * 連接tiptop服務器
	 * @param ip ip地址
	 * @param port 端口
	 * @return boolean 是否連接成功
	 **/
	public synchronized boolean connection(String ip, int port) {
		try {
			this.ip = ip;
			// 鏈接tiptop
			tiptopSocket = new Socket();
			InetSocketAddress endpoint = new InetSocketAddress(ip, port);
			tiptopSocket.connect(endpoint);
			// 連接完成后,啟動流讀取線程
			tiptopReadThread = new Thread(this);
			tiptopReadThread.start();
			// 文件下載線程
			tiptopDown = new TiptopDown();
			new Thread(tiptopDown).start();
			// 發送VT100鏈接指令
			sendSystemCommand(toByte("fffb18fffc20fffc23fffc27"));
			sendSystemCommand(toByte("fffa18007674313030fff0"));
			sendSystemCommand(toByte("fffd03fffc01fffb1ffffa1f00780023fff0"));
			sendSystemCommand(toByte("fffe05fffc21"));
			sendSystemCommand(toByte("fffd01"));
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * tiptop登錄
	 * @param userName 登錄賬號
	 * @param password 登錄密碼
	 * @param factoryCode 工廠代碼,可有可無
	 * @return 登錄結果,成功返回小寫ok，否則返回對應錯誤的消息信息
	 **/
	public synchronized String login(String userName, String password) {
		return login(userName, password, null);
	}
	public synchronized String login(String userName, String password, String factoryCode) {
		try {
			sendCommand(userName + "\n");
			sendCommand(password + "\n");
			Thread.sleep(2000);
			if (loginResult != null) {
				return loginResult;
			}
			if (factoryCode != null) {
				sendCommand(factoryCode + "\n");
			}
			while (loginResult == null) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			// tiptop登录成功,才登录ftp
			if (loginResult != null && loginResult.equals("ok")) {
				if (!tiptopDown.loginFtp(userName, password, ip)) {
					return "FTP登錄失敗";
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return loginResult;
	}
	
	/**
	 * tiptop 登錄结果
	 **/
	private String checkLoginResult(String msg) {
		if (msg.indexOf("incorrect") != -1) {
			return "帳號密碼錯誤!";
		} else if (msg.indexOf("Authentication failure") != -1) {
			return "身份验证失败!";
		} else if (msg.indexOf("您的連線數已經超出") != -1) {
			return "您的連線數已經超出!";
		} else if (msg.indexOf("change your password") != -1) {
			return "Tiptop密码已过期,请修改密码后重试!";
		} else if (msg.indexOf("請通過 idsbg-erp.efoxconn.com 登錄系統") != -1) {
			return "請通過 idsbg-erp.efoxconn.com 登錄系統!";
		} else if (msg.indexOf("記錄的唯一鍵有重複數值") != -1) {
			return "記錄的唯一鍵有重複數值!";
		} else if (msg.indexOf("系統主目錄") != -1) {
			return "ok";
		} else if (msg.replaceAll("\\s+", "").indexOf("通知") != -1) {
			new Thread(() -> {
				try {
					Thread.sleep(3000);
					tiptopSocket.getOutputStream().write("\r\n".getBytes());
					tiptopSocket.getOutputStream().flush();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}).start();
		}
		return "-1";
	}
	
	/**
	 * 釋放tiptop服務器連接
	 **/
	public synchronized void disconnect() {
		try {
			tiptopSocket.close();
			if (realClientSocket != null) {
				realClientSocket.close();
			}
			tiptopDown.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * 發送tiptop命令,默認命令間隔為500毫秒,模擬人為操縱
	 * @param command 字符串命令，常用指令有：[ESC][CTRL+P][DELETE][↓][↑][←][→]
	 **/
	public synchronized void sendCommand(String command) {
		try {
			sendCommand(command, 800);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 發送tiptop命令,默認命令間隔為500毫秒,模擬人為操縱
	 * @param command 字符串命令，常用指令有：[ESC][CTRL+P][DELETE][↓][↑][←][→]
	 * @param isReturnFtpFileName 指令执行完毕后,是否等待返回下载的文档
	 **/
	public synchronized String sendCommand(String command,boolean isReturnFtpFileName) {
		try {
			sendCommand(command, 300);
			return tiptopDown.getResultFileName();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 發送tiptop命令,可自定義命令間隔時間
	 * @param command 字符串命令，常用指令有：[ESC][CTRL+P][DELETE][↓][↑][←][→]
	 * @param sleep 間隔時間
	 **/
	private synchronized void sendCommand(String command, int sleep) throws Exception {
		if (tiptopSocket == null) {
			throw new Exception("Please call the connection method first");
		}
		boolean startInstructions = false;
		StringBuffer instructions = new StringBuffer();
		for (char c:command.toCharArray()) {
			if (c == '\n') {
				tiptopSocket.getOutputStream().write("\r\n".getBytes());
				tiptopSocket.getOutputStream().flush();
				Thread.sleep(sleep);
				continue;
			}
			if (c == '[') {
				startInstructions = true;
			}
			if (startInstructions) {
				instructions.append(Character.toString(c));
			}
			if (c == ']') {
				startInstructions = false;
				tiptopSocket.getOutputStream().write(toByte(instructionsMap.get(instructions.toString())));
				tiptopSocket.getOutputStream().flush();
				instructions.delete(0, instructions.length());
				Thread.sleep(3000);
				continue;
			}
			if (instructions.length() > 0) {
				continue;
			}
			tiptopSocket.getOutputStream().write(Character.toString(c).getBytes());
			tiptopSocket.getOutputStream().flush();
			Thread.sleep(sleep);
		}
	}
	
	/**
	 * 用於發送一些系統命令
	 **/
	private synchronized void sendSystemCommand(byte [] command) throws Exception {
  		tiptopSocket.getOutputStream().write(command);
  		tiptopSocket.getOutputStream().flush();
  		Thread.sleep(500);
	}

	/**
	 * 將16進制的字符串還原成byte[]
	 **/
	private synchronized byte[] toByte(String str) {
		char[] c = str.toUpperCase().toCharArray();
		byte[] b = new byte[c.length / 2];
		for (int i = 0; i < b.length; i++) {
			int pos = i * 2;
			b[i] = ((byte) ("0123456789ABCDEF".indexOf(c[pos]) << 4 | "0123456789ABCDEF".indexOf(c[(pos + 1)])));
		}
		return b;
	}


	/**
	 * Tiptop服務器返回讀取
	 **/
	public void run() {
		try {
			int len = 0;
			byte [] b = new byte[1024];
			InputStream is = tiptopSocket.getInputStream();
			OutputStream realClientOS = null;
			if (realClientSocket != null) {
				realClientOS = realClientSocket.getOutputStream();
			}
			while ((len = is.read(b)) != -1) {
				// 如果是真機調試,就回顯到真機
				if (realClientOS != null) {
					realClientOS.write(b, 0, len);
					realClientOS.flush();
				}
				// 程序逻辑处理
				String s = new String(b,0,len,"BIG5");
				if (s.length() > 1) {
					// 判断是否已登录
					if (loginResult == null) {
						String loginResultTemp = checkLoginResult(s);
						if (!loginResultTemp.equals("-1")) {
							loginResult = loginResultTemp;
						}
					} else {
					    // 查找最終檔名結果
						if (s.indexOf("檔名:") != -1) {
							s = s.substring(s.indexOf("檔名:") + 3);
							Pattern p = Pattern.compile("[a-zA-Z0-9]+\\.{1}[a-zA-Z0-9]+");
							Matcher m = p.matcher(s);
							if (m.find()) {
								String fileName = m.group();
								tiptopDown.addFtpFileName(fileName);
							}
						}
					}
				}
			}
		} catch (Exception e) {
			e = null;
		}
	}
	
	/**
	 * 下载文档的父级目录
	 **/
	public void setDownParentPath(String downParentPath) {
		if (tiptopSocket == null) {
			try {
				throw new Exception("Please call the connection method first");
			} catch (Exception e) {
				e.printStackTrace();
			}
			return;
		}
		tiptopDown.setDownParentPath(downParentPath);
	}
	
	/**
	 * 下载文档的文件前缀
	 **/
	public void setDownFilePrefix(String downFilePrefix) {
		tiptopDown.setDownFilePrefix(downFilePrefix);
	}
	
	/**
	 * TEST
	 **/
	public static void main(String[] args) {
		TiptopClient tiptopClient = new TiptopClient();
		tiptopClient.connection("idsbg-erp.efoxconn.com", 23, true);
		// tiptopClient.setDownParentPath("");
		String loginResult = tiptopClient.login("CM404979", "DSBG202104", "HFCCD0WMLB");
		System.out.println("登錄結果:" + loginResult);
		if (loginResult.equals("ok")) {
			System.out.println(tiptopClient.sendCommand("7\n29\n\n>=20210401\n\n\n\n\n\n\nY\n\n", true));
			tiptopClient.sendCommand("[ESC][DELETE][CTRL+P]");
			tiptopClient.sendCommand("HFCCD0WNPI\n");
			System.out.println(tiptopClient.sendCommand("\n\n>=20210401\n\n\n\n\n\n\nY\n\n", true));
			tiptopClient.sendCommand("[ESC][DELETE][CTRL+P]");
			tiptopClient.sendCommand("HFCCD0WAT \n");
			System.out.println(tiptopClient.sendCommand("\n\n>=20210401\n\n\n\n\n\n\nY\n\n", true));
		}
		tiptopClient.disconnect();
	}
}

/**
 * Tiptop FTP文檔下載實現
 **/
class TiptopDown implements Runnable {
	
	private FTPClient ftpClient;
	private volatile boolean isRun = true;
	private List<String> ftpFileNameList = Collections.synchronizedList(new ArrayList<String>());
	private List<String> resultFileNameList = Collections.synchronizedList(new ArrayList<String>());
	private String downParentPath;
	private String downFilePrefix;
	
	public boolean loginFtp(String userName,String password,String ip) {
		try {
			ftpClient = new FTPClient();
			ftpClient.connect(ip);
			return ftpClient.login(userName, password);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public void run() {
		try {
			while (isRun) {
				Thread.sleep(1000);
				if (ftpFileNameList.isEmpty() || ftpClient == null) {
					continue;
				}
				int len = 0;
				byte[] b = new byte[1024];
				for (Iterator<String> ite = ftpFileNameList.iterator();ite.hasNext();) {
					InputStream retrieveFileInputStream = null;
					OutputStream retrieveFileOutputStream = null;
					try {
						String downFileName = (downFilePrefix != null ? downFilePrefix+"_":"") + UUID.randomUUID().toString().toUpperCase() + ".txt";
						retrieveFileInputStream = ftpClient.retrieveFileStream(ite.next());
						if (downParentPath != null) {
							if (!downParentPath.endsWith(File.separator)) {
								downParentPath += File.separator;
							}
							retrieveFileOutputStream = new FileOutputStream(downParentPath + downFileName, true);
						} else {
							retrieveFileOutputStream = new FileOutputStream(downFileName, true);
						}
						while ((len = retrieveFileInputStream.read(b)) != -1) {
							retrieveFileOutputStream.write(b, 0, len);
							retrieveFileOutputStream.flush();
						}
						resultFileNameList.add(downFileName);
					} finally {
						if (retrieveFileOutputStream != null) {
							retrieveFileOutputStream.close();
						}
						if (retrieveFileInputStream != null) {
							retrieveFileInputStream.close();
							ftpClient.completePendingCommand();
						}
					}
					ite.remove();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void disconnect() {
		try {
			isRun = false;
			if (ftpClient != null) {
				ftpClient.abor();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void addFtpFileName(String ftpFileName) {
		ftpFileNameList.add(ftpFileName);
	}
	
	public String getResultFileName() {
		try {
			while (resultFileNameList.isEmpty()) {
				Thread.sleep(1000);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		String temp = resultFileNameList.get(0);
		resultFileNameList.clear();
		return temp;
	}
	
	public void setDownParentPath(String downParentPath) {
		this.downParentPath = downParentPath;
	}
	
	public void setDownFilePrefix(String downFilePrefix) {
		this.downFilePrefix = downFilePrefix;
	}
}
