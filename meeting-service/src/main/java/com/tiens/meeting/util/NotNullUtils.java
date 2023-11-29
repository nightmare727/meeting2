package com.tiens.meeting.util;

import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

public class NotNullUtils {

    /**
     * 校验字段非空
     * @param obj 实体
     * @param isNull 可以为空的字段
     * @return boolean
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public static boolean checkNull(Object obj, String... isNull) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Class cla = obj.getClass();
        Field[] fields = cla.getDeclaredFields();

        for (Field field : fields) {
            String name = field.getName();

            if (StringUtils.equals("serialVersionUID", name)) {
                continue;
            }

            boolean flag = Boolean.FALSE;
            for (String s:isNull) {
                if (StringUtils.equals(name, s)) {
                    flag = Boolean.TRUE;
                    break;
                }
            }
            if (flag) {
                continue;
            }

            String UpperCaseName = name.substring(0, 1).toUpperCase() + name.substring(1);

            Object value = cla.getMethod("get" + UpperCaseName).invoke(obj);

            String valueStr = value == null ? "" : String.valueOf(value);

            if (StringUtils.isEmpty(valueStr)) {
                return Boolean.FALSE;
            }
        }
        return Boolean.TRUE;
    }

}
