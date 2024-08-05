package common.util.io;

import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.metadata.style.WriteCellStyle;
import com.alibaba.excel.write.metadata.style.WriteFont;
import com.alibaba.excel.write.style.HorizontalCellStyleStrategy;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * <p>
 * excel工具类
 * </p>
 *
 * @author zhank
 * @date 2024-08-05
 */
public class ExcelUtil {

    /**
     * 构建表头
     */
    public static List<List<String>> buildExcelHeads(List<String> params, String expand) {
        List<List<String>> heads = new ArrayList<>();
        for (String param : params) {
            List<String> head = new ArrayList<>();
            if (!StrUtil.isEmpty(expand)) {
                head.add(expand);
            }
            head.add(param);
            heads.add(head);
        }
        return heads;
    }

    /**
     * 构建数据
     */
    public static List<List<Object>> buildExcelData(JsonNode items, List<String> keys) {
        List<List<Object>> dataList = new ArrayList();
        for (JsonNode item : items) {
            List<Object> son = new ArrayList<>();
            keys.forEach(key -> {
                son.add(item.get(key).asText());
            });
            dataList.add(son);
        }
        return dataList;
    }

    /**
     * 导出excel
     * <p>
     * 全量读，分批生成
     *
     * @param result    结果集
     * @param params    表头
     * @param keys      键
     * @param batchSize 每次导出的大小
     */
    public static void downloadExcelFullZip(HttpServletResponse response, JsonNode result, List<String> params, List<String> keys, int batchSize) throws IOException {
        List<List<Object>> data = buildExcelData(result, keys);
        int totalRecords = data.size();
        int numOfBatches = (totalRecords / batchSize) + (totalRecords % batchSize == 0 ? 0 : 1);

        List<File> excelFiles = new ArrayList<>();
        for (int i = 0; i < numOfBatches; i++) {
            int start = i * batchSize;
            int end = Math.min(start + batchSize, totalRecords);
            List<List<Object>> batchData = data.subList(start, end);

            String fileName = "excel_" + (i + 1) + ".xlsx";
            File file = new File(System.getProperty("java.io.tmpdir"), fileName);
            try (FileOutputStream fos = new FileOutputStream(file)) {
                EasyExcel.write(fos)
                        .head(buildExcelHeads(params, null))
                        .registerWriteHandler(defaultStyle())
                        .sheet()
                        .doWrite(batchData);
            }
            excelFiles.add(file);
        }

        String zipFileName = URLEncoder.encode("excels.zip", "UTF-8").replaceAll("\\+", "%20");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "attachment;filename=" + zipFileName);

        try (ZipOutputStream zos = new ZipOutputStream(response.getOutputStream())) {
            for (File excelFile : excelFiles) {
                try (FileInputStream fis = new FileInputStream(excelFile)) {
                    zos.putNextEntry(new ZipEntry(excelFile.getName()));
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = fis.read(buffer)) > 0) {
                        zos.write(buffer, 0, length);
                    }
                    zos.closeEntry();
                }
                excelFile.delete();
            }
        }
    }

    /**
     * 导出excel
     * <p>
     * 简单生成
     *
     * @param result 结果集
     * @param params 表头
     * @param keys   键
     */
    public static void downloadExcelZip(JsonNode result, List<String> params, List<String> keys) throws IOException {
        ExcelBatchDownloader batchDownloader = ExcelBatchDownloader.getInstance();
        batchDownloader.addExcel(result, params, keys);
    }

    /**
     * 导出excel
     * <p>
     * 简单生成
     *
     * @param result 结果集
     * @param params 表头
     * @param keys   键
     */
    public static void downloadExcel(HttpServletResponse response, JsonNode result, List<String> params, List<String> keys, String fileName) throws IOException {
        response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8") + ".xlsx");
        EasyExcel.write(response.getOutputStream())
                .head(buildExcelHeads(params, null))
                .registerWriteHandler(defaultStyle())
                .sheet()
                .doWrite(buildExcelData(result, keys));
    }

    /**
     * 默认样式
     */
    public static HorizontalCellStyleStrategy defaultStyle() {
        // 头的策略
        WriteCellStyle headWriteCellStyle = new WriteCellStyle();
        headWriteCellStyle.setHorizontalAlignment(HorizontalAlignment.LEFT);
        // 字体
        WriteFont headWriteFont = new WriteFont();
        headWriteFont.setFontHeightInPoints((short) 10);
        headWriteCellStyle.setWriteFont(headWriteFont);
        // 内容的策略
        WriteCellStyle contentWriteCellStyle = new WriteCellStyle();
        // 字体策略
        WriteFont contentWriteFont = new WriteFont();
        contentWriteFont.setFontHeightInPoints((short) 10);
        contentWriteCellStyle.setWriteFont(contentWriteFont);
        //设置 自动换行
        contentWriteCellStyle.setWrapped(true);
        //设置 垂直居中
        contentWriteCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        //设置 水平居中
        contentWriteCellStyle.setHorizontalAlignment(HorizontalAlignment.LEFT);
        //设置边框样式
        contentWriteCellStyle.setBorderLeft(BorderStyle.THIN);
        contentWriteCellStyle.setBorderTop(BorderStyle.THIN);
        contentWriteCellStyle.setBorderRight(BorderStyle.THIN);
        contentWriteCellStyle.setBorderBottom(BorderStyle.THIN);
        HorizontalCellStyleStrategy horizontalCellStyleStrategy = new HorizontalCellStyleStrategy(headWriteCellStyle, contentWriteCellStyle);
        return horizontalCellStyleStrategy;
    }
}
