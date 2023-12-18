package com.tiens.meeting.util;

import cn.hutool.core.collection.CollectionUtil;
import com.google.common.collect.Lists;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class FreeTimeCalculatorUtil {
    public static void main(String[] args) {
        // 已知时间段
        List<TimeRange> knownTimeRanges = new ArrayList<>();
        knownTimeRanges.add(new TimeRange(LocalTime.of(9, 0), LocalTime.of(12, 1))); // 上午工作
        knownTimeRanges.add(new TimeRange(LocalTime.of(14, 0), LocalTime.of(18, 2))); // 下午工作
        knownTimeRanges.add(new TimeRange(LocalTime.of(17, 0), LocalTime.of(19, 0))); // 下午工作
        knownTimeRanges.add(new TimeRange(LocalTime.of(20, 0), LocalTime.of(21, 0))); // 下午工作

        // 计算空闲时间段
        List<TimeRange> freeTimeRanges = calculateFreeTimeRanges(knownTimeRanges, 6);

        // 输出空闲时间段
        for (TimeRange range : freeTimeRanges) {
            System.out.println("空闲时间段： " + range);
        }

    }

    public static List<TimeRange> calculateFreeTimeRanges(List<TimeRange> knownTimeRanges, int intervalInHours) {
        List<TimeRange> rangeList = calculateFreeTimeRanges(knownTimeRanges);
        return rangeList.stream().map(s -> splitTimeInterval(s, intervalInHours)).flatMap(Collection::stream)
            .collect(Collectors.toList());
    }

    public static List<TimeRange> calculateFreeTimeRanges(List<TimeRange> knownTimeRanges) {
        List<TimeRange> freeTimeRanges = new ArrayList<>();
        // 假设一天从0点开始，到23点59分结束
        LocalTime startOfDay = LocalTime.of(0, 0);
        LocalTime endOfDay = LocalTime.of(23, 59);
        if (CollectionUtil.isEmpty(knownTimeRanges)) {
            freeTimeRanges.add(new TimeRange(startOfDay, endOfDay));
            return freeTimeRanges;
        }

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

    public static List<TimeRange> splitTimeInterval(TimeRange timeRange, int intervalInHours) {
        //计算开始时间和结束时间之间的小时数差。
        //将小时数差除以6，得到需要切割的时间段数量。
        //使用循环遍历每个时间段，计算每个时间段的开始和结束时间。
        //将每个时间段的开始和结束时间转换为分钟。

        LocalTime start = timeRange.getStart();
        LocalTime end = timeRange.getEnd();
        List<LocalTime> intervals = new ArrayList<>();
        int hoursDifference = end.toSecondOfDay() - start.toSecondOfDay();
        int numberOfIntervals = hoursDifference / (intervalInHours * 3600);
        List<TimeRange> rangeList = Lists.newArrayList();
        for (int i = 0; i <= numberOfIntervals; i++) {
            LocalTime intervalStart = start.plusHours(i * intervalInHours);
            LocalTime intervalEnd = intervalStart.plusHours(intervalInHours);
            if (i == numberOfIntervals) {
                //最后一个，结束时间取最后一个
                intervalEnd = end;
            }
            rangeList.add(new TimeRange(intervalStart, intervalEnd));
        }
        return rangeList;
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

