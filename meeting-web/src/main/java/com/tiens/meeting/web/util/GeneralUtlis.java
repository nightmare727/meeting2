package com.tiens.meeting.web.util;

import lombok.Data;

import javax.servlet.http.HttpServletRequest;
import java.io.*;

/**
 * @Author: 蔚文杰
 * @Date: 2024/1/17
 * @Version 1.0
 * @Company: tiens
 */
@Data
public class GeneralUtlis {
    private static final int BUFFER_SIZE = 1024 * 8;

    public static String read(HttpServletRequest request) throws IOException {
        BufferedReader bufferedReader = request.getReader();
        StringWriter writer = new StringWriter();
        write(bufferedReader, writer);
        return writer.getBuffer().toString();
    }

    public static long write(Reader reader, Writer writer) throws IOException {
        return write(reader, writer, BUFFER_SIZE);
    }

    public static long write(Reader reader, Writer writer, int bufferSize) throws IOException {
        int read;
        long total = 0;
        char[] buf = new char[bufferSize];
        while ((read = reader.read(buf)) != -1) {
            writer.write(buf, 0, read);
            total += read;
        }
        return total;
    }
}
