package com.tiens.meeting.mgr.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.TimeZone;

/**
 * @Author: 蔚文杰
 * @Date: 2023/10/24
 * @Version 1.0
 */
public class CustomDateSerializer extends JsonSerializer<Date> {

    public static final CustomDateSerializer instance = new CustomDateSerializer();

    @Override
    public void serialize(Date date, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
        throws IOException {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            sdf.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
            String s = sdf.format(date);
            jsonGenerator.writeString(s);
        } catch (DateTimeParseException e) {
            e.printStackTrace();
            jsonGenerator.writeString("");
        }
    }
}
