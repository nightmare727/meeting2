package common.util.io;

import com.alibaba.excel.EasyExcel;
import com.fasterxml.jackson.databind.JsonNode;
import common.util.servlet.ServletUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * <p>
 * 分批查表，统一合并zip时，使用这个
 * </p>
 *
 * @author zhank
 * @date 2024-08-05
 */
public class ExcelBatchDownloader {

    private static final ExcelBatchDownloader INSTANCE = new ExcelBatchDownloader();
    private final List<File> excelFiles = new CopyOnWriteArrayList<>();
    private final String tempDir = System.getProperty("java.io.tmpdir");

    private ExcelBatchDownloader() {
    }

    public static ExcelBatchDownloader getInstance() {
        return INSTANCE;
    }

    public void addExcel(JsonNode result, List<String> params, List<String> keys) throws IOException {
        List<List<Object>> data = ExcelUtil.buildExcelData(result, keys);

        String fileName = "excel_" + (excelFiles.size() + 1) + ".xlsx";
        File file = new File(tempDir, fileName);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            EasyExcel.write(fos)
                    .head(ExcelUtil.buildExcelHeads(params, null))
                    .registerWriteHandler(ExcelUtil.defaultStyle())
                    .sheet()
                    .doWrite(data);
        }
        excelFiles.add(file);
    }

    public void downloadZip() throws IOException {
        String zipFileName = URLEncoder.encode("excels.zip", "UTF-8").replaceAll("\\+", "%20");
        ServletUtils.getResponse().setCharacterEncoding("UTF-8");
        ServletUtils.getResponse().setContentType("application/zip");
        ServletUtils.getResponse().setHeader("Content-Disposition", "attachment;filename=" + zipFileName);

        try (ZipOutputStream zos = new ZipOutputStream(ServletUtils.getResponse().getOutputStream())) {
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
        excelFiles.clear();
    }
}
