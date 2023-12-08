package com.tiens.meeting.util;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FreeTimeCalculatorUtil {
    public static void main(String[] args) {
        // 已知时间段
        List<TimeRange> knownTimeRanges = new ArrayList<>();
        knownTimeRanges.add(new TimeRange(LocalTime.of(9, 0), LocalTime.of(12, 1))); // 上午工作
        knownTimeRanges.add(new TimeRange(LocalTime.of(14, 0), LocalTime.of(18, 2))); // 下午工作
        knownTimeRanges.add(new TimeRange(LocalTime.of(17, 0), LocalTime.of(19, 0))); // 下午工作
        knownTimeRanges.add(new TimeRange(LocalTime.of(20, 0), LocalTime.of(21, 0))); // 下午工作

        // 计算空闲时间段
        List<TimeRange> freeTimeRanges = calculateFreeTimeRanges(knownTimeRanges);

        // 输出空闲时间段
        for (TimeRange range : freeTimeRanges) {
            System.out.println("空闲时间段： " + range);
        }
    }

    public static List<TimeRange> calculateFreeTimeRanges(List<TimeRange> knownTimeRanges) {
        List<TimeRange> freeTimeRanges = new ArrayList<>();

        // 假设一天从0点开始，到23点59分结束
        LocalTime startOfDay = LocalTime.of(0, 0);
        LocalTime endOfDay = LocalTime.of(23, 59);

        // 初始化空闲时间段列表
        freeTimeRanges.add(new TimeRange(startOfDay, knownTimeRanges.get(0).start));
        for (int i = 0; i < knownTimeRanges.size() - 1; i++) {
            TimeRange currentRange = knownTimeRanges.get(i);
            TimeRange nextRange = knownTimeRanges.get(i + 1);
            freeTimeRanges.add(new TimeRange(currentRange.end, nextRange.start));
        }
        freeTimeRanges.add(new TimeRange(knownTimeRanges.get(knownTimeRanges.size() - 1).end, endOfDay));

        return freeTimeRanges;
    }
    @Data
    public static class TimeRange implements Serializable {
        LocalTime start;
        LocalTime end;

        public TimeRange(LocalTime start, LocalTime end) {
            this.start = start;
            this.end = end;
        }

        public TimeRange(Date lockStartTime, Date lockEndTime) {
            this.start = convertDateToLocalTime(lockStartTime);
            this.end = convertDateToLocalTime(lockEndTime);
        }

        public static LocalTime convertDateToLocalTime(Date date) {
            // 从Date对象中获取毫秒值
            long millis = date.getTime();

            // 将毫秒值转换为秒值
            long seconds = millis / 1000;

            // 将秒值转换为小时、分钟和秒
            long hours = seconds / 3600;
            long minutes = (seconds % 3600) / 60;
            long remainingSeconds = seconds % 60;

            // 使用这些值创建一个LocalTime对象
            return LocalTime.of((int)hours, (int)minutes, (int)remainingSeconds);
        }

        @Override
        public String toString() {
            return start + " - " + end;
        }
    }
}

