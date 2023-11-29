package common.util.http;

import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.setting.dialect.Props;
import org.apache.commons.lang3.StringUtils;

import java.util.BitSet;

/**
 * @Author: 蔚文杰
 * @Date: 2023/3/22
 * @Version 1.0
 */
public class UrlUtils {

    private static BitSet dontNeedEncoding;

    static {
        dontNeedEncoding = new BitSet(256);
        int i;
        for (i = 'a'; i <= 'z'; i++) {
            dontNeedEncoding.set(i);
        }
        for (i = 'A'; i <= 'Z'; i++) {
            dontNeedEncoding.set(i);
        }
        for (i = '0'; i <= '9'; i++) {
            dontNeedEncoding.set(i);
        }
        dontNeedEncoding.set('+');
        dontNeedEncoding.set('-');
        dontNeedEncoding.set('_');
        dontNeedEncoding.set('.');
        dontNeedEncoding.set('*');
    }

    private static boolean isDigit16Char(char c) {
        return (c >= '0' && c <= '9') || (c >= 'A' && c <= 'F');
    }

    public static boolean hasUrlEncoded(String str) {
        boolean needEncode = false;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (dontNeedEncoding.get((int)c)) {
                continue;
            }
            if (c == '%' && (i + 2) < str.length()) {
// 判断是否符合urlEncode规范
                char c1 = str.charAt(++i);
                char c2 = str.charAt(++i);
                if (isDigit16Char(c1) && isDigit16Char(c2)) {
                    continue;
                }
            }
            // 其他字符，肯定需要urlEncode

            needEncode = true;

            break;
        }
        return !needEncode;
    }

    /**
     * 去除缩略图后缀
     *
     * @param picUrl
     * @param sl
     * @param props
     * @return
     */
    public static String excludeThumbSuffix(String picUrl, String sl, Props props) {
        if (StringUtils.isBlank(picUrl)) {
            return picUrl;
        }
        String suffifx = "?x-image-process=style/" + sl;
        String fileUrl = picUrl.replace(suffifx, "");
        if (props.values().contains(fileUrl)) {
            return "";
        }
        //https://china-middle-test.obs.cn-north-4.myhuaweicloud.com/isharing/images/2023032114228/%2525E5%252583%25258F%2525E7%2525B4%2525A04.png
        //判断文件是否已被编码，如果已经被编码，那需要解码
//        String fileName = StrUtil.subAfter(fileUrl, "/", true);
//        if (hasUrlEncoded(fileName)) {
//            //已被编码过
//            String decode = URLUtil.decode(fileName);
//            int lastIndexOf = fileUrl.lastIndexOf("/");
//            String sub = StrUtil.sub(fileUrl, 0, lastIndexOf + 1);
//            fileUrl = sub + decode;
//        }
        return fileUrl;
    }

    public static String encodeFileName(String fileUrl) {
        if (StringUtils.isBlank(fileUrl)) {
            return "";
        }
        int lastIndexOf = fileUrl.lastIndexOf("/");
        String fileName = StrUtil.subAfter(fileUrl, "/", true);
        if (hasUrlEncoded(fileName)) {
            //已编码过，无需编码
            return fileUrl;
        }
        String sub = StrUtil.sub(fileUrl, 0, lastIndexOf + 1);
        return sub + URLUtil.encodeAll(fileName);
    }
}
