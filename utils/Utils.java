package com.foxconn.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.alibaba.fastjson.JSONObject;
import com.github.nobodxbodon.zhconverter.简繁转换类;

public class Utils {
	
	
	
    /**判断字符串是否全是数字
     * @param str
     * @return
     */
    public static boolean isNumeric(String str) {
        //Pattern pattern = Pattern.compile("^-?[0-9]+"); //这个也行
        Pattern pattern = Pattern.compile("^-?\\d+(\\.\\d+)?$");//这个也行
        Matcher isNum = pattern.matcher(str);
        if (!isNum.matches()) {
            return false;
        }
        return true;
    }
	
	/**obj類型轉換成想要的類型
	 * @param obj
	 * @param cla
	 * @return
	 */
	public static <T> List<T> objToList(Object obj, Class<T> t){
		List<T> list = new ArrayList<T>();
    	if (obj instanceof List<?>) {
	        for (Object o : (List<?>) obj) {
	            list.add(JSONObject.parseObject(JSONObject.toJSONString(o), t));
	        }
	        return list;
        }
        return null;
	}
	
	

	/**
	 * 下划线命名转为驼峰命名
	 * @param underlineByName 下面线命名
	 **/
	public static String underlineToHump(String underlineByName) {
		StringBuilder result=new StringBuilder();
		String tempArray[]=underlineByName.split("_");
		for(String elements:tempArray) {
			if(result.length() == 0) {
				result.append(elements.toLowerCase());
			} else {
				result.append(elements.substring(0, 1).toUpperCase());
				result.append(elements.substring(1).toLowerCase());
			}
		}
		return result.toString();
	}
		
	/**
	 * 驼峰转下划线-并轉為數據庫的大寫
	 * @param str
	 * @return
	 */
	public static String humpToLine(String str){
		Matcher matcher = Pattern.compile("[A-Z]").matcher(str);
		StringBuffer sb = new StringBuffer();
		while (matcher.find()) {
			matcher.appendReplacement(sb, "_"+matcher.group(0));
		}
		matcher.appendTail(sb);
		return sb.toString().toUpperCase();
	}
	
	/**解析Excel成Javabean对象
	 * @param <T>
	 * @param in
	 * @return
	 */
	public static <T> List<T> readExcel(InputStream in, Class<T> cla) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try (
				XSSFWorkbook workbook = new XSSFWorkbook(in);
			) {
			XSSFSheet sheet0 = workbook.getSheetAt(0);
//			处理公式的对象
			FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
			int maxRowNum = sheet0.getPhysicalNumberOfRows();//获取当前sheet页最大的行数
			int maxColNum = sheet0.getRow(0).getPhysicalNumberOfCells();//获取当前sheet页的第一行的最大列数
			Map<String, List<Object>> map = new HashMap<String, List<Object>>();
//			先遍历列（竖着遍历）
			for(int c = 0; c < maxColNum; c++) {
				ArrayList<Object> colList = new ArrayList<Object>();
				for(int r = 1; r < maxRowNum; r++) {
					XSSFRow rows = sheet0.getRow(r);
					XSSFCell cell = rows.getCell(c);
					if (null == cell) {
						colList.add("");
					}else {
						CellType cellType = cell.getCellTypeEnum();
						switch (cellType) {
						case STRING:
							colList.add(cell.getStringCellValue().trim());
							break;
						case NUMERIC:
							double value = cell.getNumericCellValue();
							if(HSSFDateUtil.isCellDateFormatted(cell)){
								Date dateValue = HSSFDateUtil.getJavaDate(value);
								colList.add(sdf.format(dateValue));
							//判断是否科学计数法
							} else if((Double.toString(value)).contains("E")) {
								//取消科学计数法
					            NumberFormat nf = NumberFormat.getInstance();
					            //设置保留多少位小数
					            nf.setMaximumFractionDigits(2);
					            // 取消科学计数法
					            nf.setGroupingUsed(false);
					            colList.add(nf.format(value));
							} else {
								colList.add(Double.toString(cell.getNumericCellValue()));
							}
							break;
						case FORMULA:
							CellValue evaluate = evaluator.evaluate(cell);//获取公式
							CellType cellTypeEnum = evaluate.getCellTypeEnum();//获取公式值的类型并做判断
							switch (cellTypeEnum) {
							case STRING:
								colList.add(cell.getStringCellValue());
								break;
							case NUMERIC:
								value = cell.getNumericCellValue();
								if(HSSFDateUtil.isCellDateFormatted(cell)){
									Date dateValue = HSSFDateUtil.getJavaDate(value);
									colList.add(sdf.format(dateValue));
								//判断是否科学计数法
								} else if((Double.toString(value)).contains("E")) {
									//取消科学计数法
						            NumberFormat nf = NumberFormat.getInstance();
						            //设置保留多少位小数
						            nf.setMaximumFractionDigits(2);
						            // 取消科学计数法
						            nf.setGroupingUsed(false);
						            colList.add(nf.format(value));
								} else {
									colList.add(Double.toString(cell.getNumericCellValue()));
								}
								break;
							default:
								colList.add("");
								break;
							}
							break;

						default:
							colList.add("");
							break;
						}
					}
				}
				//装数据
				String key = (String) colList.get(0);
				colList.remove(0);
				map.put(key, colList);
			}
//			map.forEach((key, val) ->{
//				System.out.println(key);
//				System.out.println(val);
//			});
			return converJavaListBean(map, cla);
//			return null;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (null != in) {
					in.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}
	
	/**转换
	 * @param <T>
	 * @param map
	 * @param cla
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	private static <T> List<T> converJavaListBean(Map<String, List<Object>> map, Class<T> cla) throws Exception{
		List<T> result = new ArrayList<T>();
		String firstKey = null;
		Map<String, String> methodNameMap = new HashMap<String, String>();
		Map<String, String> methodParameterType = new HashMap<String, String>();
		for (Method m : cla.getDeclaredMethods()) {
			String methodName = m.getName();
			if (!methodName.startsWith("set")) {
				continue;
			}
			methodName = methodName.substring(3);
			methodName = (methodName.substring(0, 1).toLowerCase() + methodName.substring(1));
			if (map.containsKey(methodName)) {
				methodNameMap.put(methodName, m.getName());
				methodParameterType.put(methodName, m.getParameterTypes()[0].getName());
				if (firstKey == null) {
					firstKey = methodName;
				}
			}
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		for (int i = 0; i < map.get(firstKey).size(); i++) {
			Object newObj = cla.newInstance();
			Integer ii = new Integer(i);
			map.forEach((field,list) -> {
				try {
					Method setMethod = newObj.getClass().getMethod(methodNameMap.get(field), Class.forName(methodParameterType.get(field)));
					if (setMethod.getParameterCount() == 1) {
						switch (setMethod.getParameterTypes()[0].getSimpleName()) {
						case "String":
							setMethod.invoke(newObj, list.get(ii).toString());
							break;
						case "Integer":
							setMethod.invoke(newObj, (int) Double.parseDouble(list.get(ii).toString()));
							break;
						case "Double":
							setMethod.invoke(newObj, Double.parseDouble(list.get(ii).toString()));
							break;
						case "Date":
							setMethod.invoke(newObj, sdf.parse(list.get(ii).toString()));
							break;
						default:
							break;
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
			result.add((T) newObj);
		}
		return result;
	}
	
	/**
	 * map转Javabean
	 */
	@SuppressWarnings("unchecked")
	public static <T> T mapToJavabean(Map<String, Object> objMap, Class<T> cla) {
		Object obj = null;
		try {
			obj = cla.newInstance();
			Method[] methods = cla.getDeclaredMethods();
			for(Method m: methods) {
				if (m.getName().startsWith("set")) {
					String _name = m.getName().substring(3);//例子：JobCard
					String objMapKey = Utils.humpToLine(_name).substring(1);//例子：_Job_Card --> Job_Card
					Object value = objMap.get(objMapKey);
					if (value instanceof BigDecimal) {
						if (m.getParameters()[0].getType().getSimpleName().equals("Integer")) {
							m.invoke(obj, ((BigDecimal)value).intValue());
						} else {
							m.invoke(obj, ((BigDecimal)value).doubleValue());
						}
					} else if (value instanceof String) {
						m.invoke(obj, (String)value);
					} else {
						m.invoke(obj, value);
					}
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return (T)obj;
	}
	
	/**
	 * 簡體轉繁體
	 **/
	public static String simplifiedConverter(String str) {
		// 替换单引号的作用是防止sql注入
		return 简繁转换类.转换(str.trim().replaceAll("'", ""), 简繁转换类.目标.繁体);
	}
	
	/**
	 * 獲取系統當前日期
	 **/
	public static String systemDate(String format) {
		SimpleDateFormat dateFormat = new SimpleDateFormat(format);
		return dateFormat.format(new Date());
	}
	
	/**
	 * 將日期字符串轉換為日期對象
	 **/
	public static Date dateStrParse(String dateStr, String format) {
		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat(format);
			return dateFormat.parse(dateStr);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 將日期對象轉換為字符串
	 **/
	public static String dateFormatStr(Object date, String format) {
		SimpleDateFormat dateFormat = new SimpleDateFormat(format);
		return dateFormat.format(date);
	}
	
	/**
	 * List的深度拷貝
	 * @param src 需要拷貝的集合
	 * @return 返回拷貝對象的結果
	 **/
	public static <T> List<T> ListDeepCopy(List<T> src) {
		if(src==null) return null;
		ByteArrayOutputStream byteOut = null;
		ObjectOutputStream out = null;
		ByteArrayInputStream byteIn = null;
		ObjectInputStream in = null;
		try {
			byteOut = new ByteArrayOutputStream(); 
			out = new ObjectOutputStream(byteOut); 
			out.writeObject(src); 
			byteIn = new ByteArrayInputStream(byteOut.toByteArray()); 
			in = new ObjectInputStream(byteIn); 
			@SuppressWarnings("unchecked") 
			List<T> dest = (List<T>) in.readObject();
			return dest; 
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if(byteOut!=null) byteOut.close();
				if(out!=null) out.close();
				if(byteIn!=null) byteIn.close();
				if(in!=null) in.close();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	public static Map<String, String> readTXT() {
		Map<String, String> map = new HashMap<String, String>();
		Properties pro = new Properties();
		try (FileInputStream in = new FileInputStream("DataSource.properties");){
			pro.load(in);
			Enumeration enum1 = pro.propertyNames();
	        while(enum1.hasMoreElements()) {
	             String strKey = (String) enum1.nextElement();
	             String strValue = pro.getProperty(strKey);
	             map.put(strKey, strValue);
	        }
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return map;
	}
}
