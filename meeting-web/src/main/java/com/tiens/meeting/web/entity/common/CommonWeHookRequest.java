package com.tiens.meeting.web.entity.common;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.tiens.meeting.web.entity.CustomDateTypeAdapter;
import com.tiens.meeting.web.entity.MeetingCreateResult;
import io.micrometer.core.instrument.util.StringUtils;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @Author: 蔚文杰
 * @Date: 2023/10/28
 * @Version 1.0
 */

@Data
public class CommonWeHookRequest implements Serializable {

    private String data;
    private static final Gson GSON =
        new GsonBuilder().registerTypeAdapter(Date.class, new CustomDateTypeAdapter()).create();

    protected <T> T parseCallbackData(Class<T> clazz) {
        if (StringUtils.isBlank(getCallbackData())) {
            return null;
        }
        try {
            return GSON.fromJson(getCallbackData(), clazz);
        } catch (Exception e) {
            throw new RuntimeException("parse callback data fails", e);
        }
    }

    protected <T> T parseCallbackData(TypeToken<T> typeToken) {
        if (StringUtils.isBlank(getCallbackData())) {
            return null;
        }
        try {
            return GSON.fromJson(getCallbackData(), typeToken.getType());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("parse callback data fails", e);
        }
    }

    protected <T> List<T> parseCallbackData(Class<T> clazz, TypeToken<List<T>> typeToken) {
        if (StringUtils.isBlank(getCallbackData())) {
            return null;
        }
        try {
            JsonElement jsonElement = JsonParser.parseString(getCallbackData());
            if (jsonElement.isJsonArray()) {
                return GSON.fromJson(getCallbackData(), typeToken.getType());
            } else {
                T t = GSON.fromJson(getCallbackData(), clazz);
                List<T> callbackDataList = new ArrayList<>(1);
                callbackDataList.add(t);
                return callbackDataList;
            }
        } catch (Exception e) {
            throw new RuntimeException("parse callback data fails", e);
        }
    }

    private String getCallbackData() {
        return data;
    }

    public static void main(String[] args) {

        String str =
            "{\"event\":\"meeting.created\",\"trace_id\":\"125ba619-9f03-4e59-bfda-ddf044d3986f\"," + "\"payload" +
                "\":[{\"operate_time\":1698465860387,\"operator\":{\"userid\":\"weiwj1\",\"user_name\":\"文杰\"," +
                "\"uuid\":\"WMWnXDrZnZBbXrHtBM\",\"instance_id\":\"1\"," + "\"ms_open_id\":\"7x2/sJu+ZvF" +
                "+yUe3AUktKtbZi60Owgt3N9fxQS3v0AjricZzKuKk1Q9rUiHnwbD0+upvpFIURXhLzgOhVGAsWQ" + "==\"},\"meeting_info"
                + "\":{\"meeting_id\":\"6761292831434059923\",\"meeting_code\":\"796935630\"," + "\"subject" +
                "\":\"文杰的快速会议\",\"creator\":{\"userid\":\"weiwj1\",\"user_name\":\"文杰\"," + "\"uuid" +
                "\":\"WMWnXDrZnZBbXrHtBM\",\"instance_id\":\"1\"," + "\"ms_open_id\":\"7x2/sJu+ZvF" +
                "+yUe3AUktKtbZi60Owgt3N9fxQS3v0AjricZzKuKk1Q9rUiHnwbD0+upvpFIURXhLzgOhVGAsWQ" + "==\"},\"meeting_type"
                + "\":0,\"start_time\":1698465860,\"end_time\":1698469460,\"meeting_create_mode\":1," +
                "\"meeting_create_from\":1}}]}";

        JSONObject entries = JSONUtil.parseObj(str);
        String event = (String)entries.get("event");
        System.out.println(event);

        CommonWeHookRequest commonWeHookRequest = new CommonWeHookRequest();
        commonWeHookRequest.setData(str);
        TypeToken<CommonWebHookDTO<MeetingCreateResult>> typeToken =
            new TypeToken<CommonWebHookDTO<MeetingCreateResult>>() {
            };
        CommonWebHookDTO<MeetingCreateResult> meetingCreateResultCommonWebHookDTO =
            commonWeHookRequest.parseCallbackData(typeToken);
        System.out.println(JSON.toJSONString(meetingCreateResultCommonWebHookDTO));
    }

}
