package com.tiens.meeting.util;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.google.common.collect.Lists;
import common.util.date.DateUtils;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

public class FreeTimeCalculatorUtil {
    public static void main(String[] args) {
        // 已知时间段
        List<TimeRange> knownTimeRanges = new ArrayList<>();
        knownTimeRanges.add(new TimeRange(LocalTime.of(9, 0), LocalTime.of(12, 0))); // 上午工作
        knownTimeRanges.add(new TimeRange(LocalTime.of(13, 0), LocalTime.of(17, 0))); // 下午工作
        knownTimeRanges.add(new TimeRange(LocalTime.of(18, 0), LocalTime.of(21, 0))); // 下午工作

        // 计算空闲时间段
        List<TimeRange> freeTimeRanges = calculateFreeTimeRanges(knownTimeRanges, 2, 6, true);

        // 输出空闲时间段
        for (TimeRange range : freeTimeRanges) {
            System.out.println("空闲时间段： " + range);
        }

    }

    public static List<TimeRange> calculateFreeTimeRanges(List<TimeRange> knownTimeRanges, int minIntervalInHours,
        int maxIntervalInHours, boolean isToday) {
//        1、列出所有已知的占用时间段：首先，我们需要知道哪些时间段是忙碌的。这可以通过查看时间表或数据库中的记录来完成。

//        2、计算每个时间段的长度：对于每个占用的时间段，计算出它的结束时间和开始时间，从而得到它的长度。长度是结束时间减去开始时间。

//        3、确定空闲时间段的范围：为了找到空闲时间段，我们需要从一天的开始（通常为0点）开始，到这一天的结束（通常为24点）结束。然后，我们从这个范围中减去所有已知的占用时间段的长度。

//        4、筛选出符合条件的空闲时间段：我们筛选出长度在2小时到6小时之间的时间段。如果一个时间段的长度小于2小时或大于6小时，我们就将其排除在外。

//        5、获取所有空闲时间段集合：最后，我们将所有的空闲时间段合并起来，形成一个完整的集合。
        //查询空闲时间段
        List<TimeRange> rangeList = calculateFreeTimeRanges(knownTimeRanges, isToday);
        //
        return rangeList.stream().map(s -> splitTimeInterval(s, minIntervalInHours, maxIntervalInHours))
            .flatMap(Collection::stream).collect(Collectors.toList());
    }

    /**
     * 计算一天中的空闲时间段
     *
     * @param knownTimeRanges
     * @param isToday
     * @return
     */
    public static List<TimeRange> calculateFreeTimeRanges(List<TimeRange> knownTimeRanges, boolean isToday) {
        //重排序，防止出问题
        knownTimeRanges.sort(Comparator.comparing(TimeRange::getStart));

        List<TimeRange> freeTimeRanges = new ArrayList<>();
        DateTime now = DateUtils.roundToHalfHour(DateUtil.date());

        // 当前时间，则取当前时间时分，否则假设一天从0点开始，到23点59分结束
        LocalTime startOfDay = isToday ? LocalTime.of(now.getHours(), now.getMinutes()) : LocalTime.of(0, 0);

        LocalTime endOfDay = LocalTime.of(23, 59);
        if (CollectionUtil.isEmpty(knownTimeRanges)) {
            freeTimeRanges.add(new TimeRange(startOfDay, endOfDay));
            return freeTimeRanges;
        }

        // 初始化空闲时间段列表
        freeTimeRanges.add(new TimeRange(startOfDay, knownTimeRanges.get(0).start));
        for (int i = 0; i < knownTimeRanges.size() - 1; i++) {
            TimeRange currentRange = knownTimeRanges.get(i);
            if (startOfDay.isAfter(currentRange.getStart())) {
                continue;
            }
            TimeRange nextRange = knownTimeRanges.get(i + 1);
            freeTimeRanges.add(new TimeRange(currentRange.end, nextRange.start));
        }
        freeTimeRanges.add(new TimeRange(knownTimeRanges.get(knownTimeRanges.size() - 1).end, endOfDay));

        return freeTimeRanges;
    }

    public static List<TimeRange> splitTimeInterval(TimeRange timeRange, int minIntervalInHours,
        int maxIntervalInHours) {
        //计算开始时间和结束时间之间的小时数差。
        //将小时数差除以6，得到需要切割的时间段数量。
        //使用循环遍历每个时间段，计算每个时间段的开始和结束时间。
        //将每个时间段的开始和结束时间转换为分钟。

        LocalTime start = timeRange.getStart();
        LocalTime end = timeRange.getEnd();
        int hoursDifference = end.toSecondOfDay() - start.toSecondOfDay();
        int numberOfIntervals = hoursDifference / (maxIntervalInHours * 3600);
        List<TimeRange> rangeList = Lists.newArrayList();
        for (int i = 0; i <= numberOfIntervals; i++) {
            LocalTime intervalStart = start.plusHours(i * maxIntervalInHours);
            LocalTime intervalEnd = intervalStart.plusHours(maxIntervalInHours);
            if (i == numberOfIntervals) {
                //最后一个，结束时间取最后一个
                intervalEnd = end;
            }
            if ((getSeconds(intervalEnd) - getSeconds(intervalStart)) >= minIntervalInHours * 60 * 60) {
              /*  intervalStart = intervalStart.plusMinutes(30);
                intervalEnd = intervalEnd.minusMinutes(30);*/
                rangeList.add(new TimeRange(intervalStart, intervalEnd));
            }
        }
        return rangeList;
    }

    static int getSeconds(LocalTime localTime) {
        if (ObjectUtil.isNull(localTime)) {
            return 0;
        }
        return localTime.getHour() * 60 * 60 + localTime.getMinute() * 60 + localTime.getSecond();
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
            DateTime dateTime = DateUtil.beginOfDay(date);
            millis = millis - dateTime.getTime();
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

