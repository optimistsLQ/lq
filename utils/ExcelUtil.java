package com.foxconn.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import com.foxconn.entity.LaHuoDetail;

public class ExcelUtil {
    private static final String EXCEL_XLS = "xls";
    private static final String EXCEL_XLSX = "xlsx";
    
    private static final Logger logger = LoggerFactory.getLogger(ExcelUtil.class);

    public static void writeExcel(List<List<String>> dataList, int cloumnCount, String finalXlsxPath) {
        OutputStream out = null;
        try {
            // 读取Excel文档
            File finalXlsxFile = new File(finalXlsxPath);
            Workbook workBook = getWorkbok(finalXlsxFile);
            // sheet 对应一个工作页
            Sheet sheet = workBook.getSheetAt(0);
            /**
             * 删除原有数据，除了属性列
             */
            int rowNumber = sheet.getLastRowNum(); // 第一行从0开始算
            System.out.println("原始数据总行数，除属性列：" + rowNumber);
            for (int i = 1; i <= rowNumber; i++) {
                Row row = sheet.getRow(i);
                sheet.removeRow(row);
            }
            // 创建文件输出流，输出电子表格：这个必须有，否则你在sheet上做的任何操作都不会有效
            out = new FileOutputStream(finalXlsxPath);
            workBook.write(out);
            /**
             * 往Excel中写新数据
             */
            for (int j = 0; j < dataList.size(); j++) {
                // 创建一行：从第二行开始，跳过属性列
                Row row = sheet.createRow(j + 1);
                // 得到要插入的每一条记录
                List<String> dataMap = dataList.get(j);
                String cell1 = dataMap.get(0).toString();
                String cell2 = dataMap.get(1).toString();
                String cell3 = dataMap.get(2).toString();
                String cell4 = dataMap.get(3).toString();
                String cell5 = dataMap.get(4).toString();
                String cell6 = dataMap.get(5).toString();
                String cell7 = dataMap.get(6).toString();

                // for (int k = 0; k <= columnNumCount; k++) {
                // 在一行内循环
                Cell first = row.createCell(0);
                first.setCellValue(cell1);

                Cell second = row.createCell(1);
                second.setCellValue(cell2);

                Cell third = row.createCell(2);
                third.setCellValue(cell3);
                Cell four = row.createCell(3);
                four.setCellValue(cell4);
                Cell five = row.createCell(4);
                five.setCellValue(cell5);
                Cell six = row.createCell(5);
                six.setCellValue(cell6);
                Cell seven = row.createCell(6);
                seven.setCellValue(cell7);
                // }
            }
            // 创建文件输出流，准备输出电子表格：这个必须有，否则你在sheet上做的任何操作都不会有效
            out = new FileOutputStream(finalXlsxPath);
            workBook.write(out);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.flush();
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("数据导出成功");
    }

    /**
     * 判断Excel的版本,获取Workbook
     * 
     * @param in
     * @param filename
     * @return
     * @throws IOException
     */
    public static Workbook getWorkbok(File file) throws IOException {
        Workbook wb = null;
        FileInputStream in = new FileInputStream(file);
        if (file.getName().endsWith(EXCEL_XLS)) { // Excel 2003
            wb = new HSSFWorkbook(in);
        } else if (file.getName().endsWith(EXCEL_XLSX)) { // Excel 2007/2010
            wb = new XSSFWorkbook(in);
        }
        return wb;
    }

    /**
     * 根据fileType不同读取excel文件
     *
     * @param path
     * @param path
     * @throws IOException
     */
    @SuppressWarnings({ "resource", "deprecation" })
    public static List<List<String>> readExcel(String path) {
        String fileType = path.substring(path.lastIndexOf(".") + 1);
        // return a list contains many list
        List<List<String>> lists = new ArrayList<List<String>>();
        // 读取excel文件
        InputStream is = null;
        try {
            is = new FileInputStream(path);
            // 获取工作薄
            Workbook wb = null;
            if (fileType.equals("xls")) {
                wb = new HSSFWorkbook(is);
            } else if (fileType.equals("xlsx")) {
                wb = new XSSFWorkbook(is);
            } else {
                return null;
            }

            // 读取第一个工作页sheet
            Sheet sheet = wb.getSheetAt(0);
            // 第一行为标题
            for (Row row : sheet) {
                ArrayList<String> list = new ArrayList<String>();
                for (Cell cell : row) {
                    // 根据不同类型转化成字符串
                    cell.setCellType(Cell.CELL_TYPE_STRING);
                    list.add(cell.getStringCellValue());
                }
                lists.add(list);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null)
                    is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return lists;
    }
    
    
    
    /**multipartFile转File
     * @param multipartFile
     * @return
     */
    public static File transferToFile(MultipartFile multipartFile) {
//      选择用缓冲区来实现这个转换即使用java 创建的临时文件 使用 MultipartFile.transferto()方法 。
       File file = null;
       try {   
          String originalFilename = multipartFile.getOriginalFilename();
          String[] filename = originalFilename.split("\\.");
          file=File.createTempFile(filename[0], filename[1]);
          multipartFile.transferTo(file);
           file.deleteOnExit();        
      } catch (IOException e) {
          e.printStackTrace();
      }
      return file;
  }
    
    
    /**
     * 根据fileType不同读取excel文件
     *
     * @param path
     * @param path
     * @throws IOException
     */
    @SuppressWarnings({ "resource", "deprecation" })
    public static <T> Map<String, Object> readExcel123(File file, Class<T> cla) {
    	Map<String, Object> map = new HashMap<String, Object>();
    	List<String> GRN_stockinCode = new ArrayList<String>();
    	//先获取cla的所有set方法
//    	Map<String, String> setMethodMap = getSetMethod(cla);
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    	String fileName = file.getName();
    	String fileType = fileName.substring(fileName.lastIndexOf(".") + 1);
    	// return a list contains many list
    	List<LaHuoDetail> lists = new ArrayList<LaHuoDetail>();
    	// 读取excel文件
    	InputStream is = null;
    	//{"VIE0010018", "1110"}
//    	Map<String, String> strMap = new HashMap<String, String>();
    	try {
    		is = new FileInputStream(file);
    		// 获取工作薄
    		Workbook wb = null;
    		if (fileType.equals("xls")) {
    			wb = new HSSFWorkbook(is);
    		} else if (fileType.equals("xlsx")) {
    			wb = new XSSFWorkbook(is);
    		} else {
    			return null;
    		}
    		
    		// 读取第一个工作页sheet
    		Sheet sheet = wb.getSheetAt(0);
    		ArrayList<String> fieldList = new ArrayList<String>();
    		// 第一行为标题
    		for (Row row : sheet) {
    			int rowNum = row.getRowNum();
    			if (rowNum == 0) {
    				continue;
    			}
    			
    			if (rowNum == 1) {//过滤前面2行的标题
    				for(Cell cell : row) {
    					cell.setCellType(Cell.CELL_TYPE_STRING);
    					String setMethod = cell.getStringCellValue();
    					setMethod = setMethod.substring(0, 1).toUpperCase() + setMethod.substring(1);
    					setMethod = "set" + setMethod;
    					fieldList.add(setMethod);
    				}
    			}else {
	    			ArrayList<String> list = new ArrayList<String>();
	    			for (Cell cell : row) {
	    				// 根据不同类型转化成字符串
	    				CellType cellType = cell.getCellTypeEnum();
	//    				判断处理时间类型
	    				if (CellType.NUMERIC.equals(cellType) && 
	    						org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(cell)) {
	    					Date date = DateUtil.getJavaDate(cell.getNumericCellValue());
	//    					cell.setCellType(Cell.CELL_TYPE_NUMERIC);
	    					list.add(sdf.format(date));
	    				}else {
	    					
	    					cell.setCellType(Cell.CELL_TYPE_STRING);
	    					String val = cell.getStringCellValue();
	    					if (null == val || val.length() == 0) {
	    						val = "*";
	    					}
	    					list.add(val);
	    				}
	    			}
	    			T bean = converJavaListBean(list,fieldList, cla);
	    			LaHuoDetail lahuo = (LaHuoDetail)bean;
	    			logger.info("lahuo>>>:"+lahuo);
//	    			String[] split = lahuo.getField4().split("~");
//	    			strMap.put(split[0], split[1]);
//	    			lahuo.setField4(null);
	    			GRN_stockinCode.add(lahuo.getStockinCode());
	    			lists.add(lahuo);
    			}
    		}
    	} catch (Exception e) {
    		e.printStackTrace();
    	} finally {
    		try {
    			if (is != null)
    				is.close();
    		} catch (IOException e) {
    			e.printStackTrace();
    		}
    	}
//    	System.out.println(strMap);
//    	System.out.println("lists>>"+lists);
//    	System.out.println("strMap>>"+strMap);
//    	System.out.println("GRN_stockinCode>>"+GRN_stockinCode);
    	map.put("data", lists);
//    	map.put("strMap", strMap);
    	map.put("GRNList", GRN_stockinCode);
    	return map;
    }

    
    public static <T> T converJavaListBean(List<String> list, List<String> fieldList, Class<T> cls) {
    	T obj = null;
    	String str = null;
		try {
			obj = cls.newInstance();
			for (Method m : cls.getDeclaredMethods()) {
				String mName = m.getName();
				if (!mName.startsWith("set")) {
					continue;
				}
				for (int i = 0 ;i < fieldList.size(); i++) {
					String val = fieldList.get(i);
					if (mName.contains(val)) {
						String realVal = list.get(i);
						if (m.getParameters()[0].getType().getSimpleName().equals("Double")) {
							if ("*".equals(realVal)) {
								m.invoke(obj, 0d);
							}else {
								m.invoke(obj, Double.parseDouble(realVal));
							}
						}else {
							
							if ("*".equals(realVal)) {
								m.invoke(obj, "");
								if (mName.contains("VendorCode")) {
									str = list.get(3) + "~" + list.get(0).split("-")[1];
								}
							}else {
								m.invoke(obj, realVal);
								if (mName.contains("VendorCode")) {
									str = realVal + "~" + list.get(0).split("-")[1];
								}
							}
						}
						break;
					}
				}
//				System.out.println(">>>"+mName);
				if (mName.contains("ield4")) {
					m.invoke(obj, str);
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
//		System.out.println("obj>>"+obj);
    	return obj;
    }
   
    /**
     * 按指定大小，分隔集合，将集合按规定个数分为n个部分
     *
     * @param list
     * @return
     */
    public static <T> List<List<T>> splitList(List<T> list,int length) {
        //数据库批量操作1000条数据会报错在in的情况下，
        int len = length;
        //判断非空
        if (list == null || list.isEmpty() || len < 1) {
            return Collections.emptyList();
        }
        //声明返回对象存值
        List<List<T>> result = new ArrayList<>();
        int size = list.size();
        //计算循环次数
        int count = (size + len - 1) / len;
        for (int i = 0; i < count; i++) {
            List<T> subList = list.subList(i * len, ((i + 1) * len > size ? size : len *                     (i + 1)));
            result.add(subList);
        }
        return result;
    }
    
    /**解析签回明细表
     * @param file
     * @return  《厂商代码，结报code》
     */
    @SuppressWarnings("resource")
	public static Map<String,String> readBackExcel(File file) {
    	HashMap<String,String> map = new HashMap<String, String>();
    	String fileName = file.getName();
    	String fileType = fileName.substring(fileName.lastIndexOf(".") + 1);
    	Workbook wb = null;
    	InputStream in = null;
    	try {
    		in = new FileInputStream(file);
	    	if (fileType.equals("xls")) {
					wb = new HSSFWorkbook(in);
			} else if (fileType.equals("xlsx")) {
				wb = new XSSFWorkbook(in);
			} else {
				return null;
			}
	    	Sheet sheet = wb.getSheetAt(2);
	    	String key = null, val = null;
	    	for (Row row : sheet) {
	    		int rowNum = row.getRowNum();
				if (rowNum == 0 || rowNum == 1 || rowNum == 2 || rowNum == 3 || rowNum == 4) {
					continue;
				}
				for (Cell cell : row) {
					int columnIndex = cell.getColumnIndex();
					if (columnIndex == 1) {
						key = cell.getStringCellValue();
					}
					if (columnIndex == 3) {
						val = cell.getStringCellValue();
					}
				}
				String oldVal = map.get(key);
				if (oldVal != null) {
					val = oldVal + "/" + val;
				}
//				System.out.println("rowNum>>"+rowNum + " key " +key+ " val " + val);
				map.put(key, val);
				key = null;
				val = null;
			}
    	} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			try {
				if (in != null) {
					in.close();
				}
				if (wb != null) {
					wb.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
//    	System.out.println("map>>"+map);
    	return map;
    }
    
    /**解析tiptop系统下载的结报单号
     * @return map<结报单号，GRN单号>
     */
    public static Map<String, String> readToptipExcel(MultipartFile file){
    	SimpleDateFormat sf = new SimpleDateFormat("yyyyMMdd");
    	HashMap<String,String> map = new HashMap<String, String>();
    	String fileType = file.getOriginalFilename();
    	Workbook wb = null;
    	InputStream in = null;
    	try {
    		in = file.getInputStream();
	    	if (fileType.endsWith(".xls")) {
					wb = new HSSFWorkbook(in);
			} else if (fileType.endsWith(".xlsx")) {
				wb = new XSSFWorkbook(in);
			} else {
				return null;
			}
	    	Sheet sheet = wb.getSheetAt(0);
	    	
	    	for (Row row : sheet) {
	    		int rowNum = row.getRowNum();
				if (rowNum == 0) {
					continue;
				}
				String key = null,val = null;
				for (Cell cell : row) {
					if (cell.getColumnIndex() == 0){
						val = cell.getStringCellValue();
					}
					if (cell.getColumnIndex() == 1) {
						// TODO 日期格式问题
						if (DateUtil.isCellDateFormatted(cell)) {
							Date javaDate = DateUtil.getJavaDate(cell.getNumericCellValue());
							System.out.println("javaDate:"+cell.getNumericCellValue()+"   "+sf.format(javaDate));
							val = val + "~" + sf.format(javaDate);
						// 不是日期,用正则表达式匹配是否符合
						} else {
							val = val + "~" +  Integer.toString((int) cell.getNumericCellValue());
						}
					}
					if (cell.getColumnIndex() == 30) {
						val = val + "~" + cell.getStringCellValue();
					}
					if (cell.getColumnIndex() == 10) {
						key = cell.getStringCellValue();
					}
					
				}
				map.put(key, val);
			}
    	} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			try {
				if (in != null) {
					in.close();
				}
				if (wb != null) {
					wb.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
    	return map;
    }
}