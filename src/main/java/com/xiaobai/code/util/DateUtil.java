package com.xiaobai.code.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 日期工具类
 */
public class DateUtil {

    /**
     * 字符串转日期
     * @param str
     * @param format
     * @return
     * @throws ParseException
     */
    public static Date formatString(String str,String format) throws ParseException {
        if(StringUtil.isEmpty(str)){
            return null;
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        return dateFormat.parse(str);
    }

    /**
     * 日期对象转字符串
     */
    public static String formatDate(Date date,String format){
        String result="";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
        if (date != null) {
            result = simpleDateFormat.format(date);
        }
        return result;
    }

    /**
     * 获取当前时间字符串
     */
    public static String getCurrentDateStr() {
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        return simpleDateFormat.format(date);
    }
}
