package com.tiens.meeting.util;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.google.common.collect.Lists;
import common.util.date.DateUtils;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

public class FreeTimeCalculatorUtil {
    public static void main(String[] args) {

        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));

        // 1、取得当前时间：
        Calendar calendar = Calendar.getInstance();
// 2、取得时间偏移量：
        int zoneOffset = calendar.get(java.util.Calendar.ZONE_OFFSET);
// 3、取得夏令时差：
        int dstOffset = calendar.get(java.util.Calendar.DST_OFFSET);

        // 已知时间段
        List<TimeRange> knownTimeRanges = new ArrayList<>();
        //        knownTimeRanges.add(new TimeRange(LocalTime.of(13, 30), LocalTime.of(15, 29))); // 上午工作
        knownTimeRanges.add(new TimeRange(LocalTime.of(14, 30), LocalTime.of(16, 29))); // 下午工作
//        knownTimeRanges.add(new TimeRange(LocalTime.of(21, 30), LocalTime.of(23, 29))); // 下午工作

        ZoneId userZoneId = ZoneId.of("GMT+10");
        // 计算空闲时间段
        List<TimeRange> freeTimeRanges =
            calculateFreeTimeRanges(knownTimeRanges, 1, 6, DateUtil.parse("2024-03-29 00:00:00"),
                DateUtil.parse("2024-07-04 23:59:59"), userZoneId);

        // 输出空闲时间段
        for (TimeRange range : freeTimeRanges) {
            System.out.println("空闲时间段： " + range);
        }

    }

    public static List<TimeRange> calculateFreeTimeRanges(List<TimeRange> knownTimeRanges, int minIntervalInHours,
        int maxIntervalInHours, Date targetDate, Date expireDate, ZoneId userZoneId) {

        expireDate = DateUtil.convertTimeZone(expireDate, userZoneId);
        //        1、列出所有已知的占用时间段：首先，我们需要知道哪些时间段是忙碌的。这可以通过查看时间表或数据库中的记录来完成。

        //        2、计算每个时间段的长度：对于每个占用的时间段，计算出它的结束时间和开始时间，从而得到它的长度。长度是结束时间减去开始时间。

        //        3、确定空闲时间段的范围：为了找到空闲时间段，我们需要从一天的开始（通常为0点）开始，到这一天的结束（通常为24点）结束。然后，我们从这个范围中减去所有已知的占用时间段的长度。

        //        4、筛选出符合条件的空闲时间段：我们筛选出长度在2小时到6小时之间的时间段。如果一个时间段的长度小于2小时或大于6小时，我们就将其排除在外。

        //        5、获取所有空闲时间段集合：最后，我们将所有的空闲时间段合并起来，形成一个完整的集合。
        //查询空闲时间段
        List<TimeRange> rangeList = calculateFreeTimeRange(knownTimeRanges, targetDate, expireDate, userZoneId);
        //
        return rangeList.stream()
            .map(s -> splitTimeInterval(s, targetDate, minIntervalInHours, maxIntervalInHours, userZoneId))
            .flatMap(Collection::stream).collect(Collectors.toList());
    }

    /**
     * 计算一天中的空闲时间段
     *
     * @param knownTimeRanges
     * @return
     */
    public static List<TimeRange> calculateFreeTimeRange(List<TimeRange> knownTimeRanges, Date targetDate,
        Date expireDate, ZoneId userZoneId) {
        //重排序，防止出问题
        knownTimeRanges.sort(Comparator.comparing(TimeRange::getStart));

        List<TimeRange> freeTimeRanges = new ArrayList<>();

        //用户当前时区时间
        DateTime now = DateUtils.roundToHalfHour(DateUtil.convertTimeZone(DateUtil.date(), userZoneId), userZoneId);

        //当前天
        boolean isToday = DateUtil.formatDate(targetDate).equals(DateUtil.formatDate(now));

        // 当前时间，则取当前时间时分，否则假设一天从0点开始，到23点59分结束
        LocalTime startOfDay =
            isToday ? LocalTime.of(DateUtil.hour(now, true), DateUtil.minute(now)) : LocalTime.of(0, 0);

        boolean isExpireDay =
            DatePattern.NORM_DATE_FORMAT.format(targetDate).equals(DatePattern.NORM_DATE_FORMAT.format(expireDate));

        //结束时间需要计算资源过期时间
        LocalTime endOfDay = isExpireDay ? LocalTime.of(DateUtil.hour(expireDate, true), DateUtil.minute(expireDate))
            : LocalTime.of(23, 59);
        if (CollectionUtil.isEmpty(knownTimeRanges)) {
            freeTimeRanges.add(new TimeRange(startOfDay, endOfDay));
            return freeTimeRanges;
        }

        // 初始化空闲时间段列表
        if (startOfDay.isBefore(knownTimeRanges.get(0).start)) {
            freeTimeRanges.add(new TimeRange(getAround(startOfDay), getAround(knownTimeRanges.get(0).start)));
        }
        for (int i = 0; i < knownTimeRanges.size() - 1; i++) {
            TimeRange currentRange = knownTimeRanges.get(i);
            if (startOfDay.isAfter(currentRange.getStart())) {
                continue;
            }
            TimeRange nextRange = knownTimeRanges.get(i + 1);
            freeTimeRanges.add(new TimeRange(getAround(currentRange.end), getAround(nextRange.start)));
        }
        //最后的空闲时间段列表
        if (!knownTimeRanges.get(knownTimeRanges.size() - 1).end.equals(endOfDay)) {
            freeTimeRanges.add(new TimeRange(getAround(knownTimeRanges.get(knownTimeRanges.size() - 1).end), endOfDay));
        }

        return freeTimeRanges;
    }

    public static List<TimeRange> splitTimeInterval(TimeRange timeRange, Date targetDate, int minIntervalInHours,
        int maxIntervalInHours, ZoneId userZoneId) {
        //计算开始时间和结束时间之间的小时数差。
        //将小时数差除以6，得到需要切割的时间段数量。
        //使用循环遍历每个时间段，计算每个时间段的开始和结束时间。
        //将每个时间段的开始和结束时间转换为分钟。

        //当前时间
        DateTime nowDateTime =
            DateUtils.roundToHalfHour(DateUtil.convertTimeZone(DateUtil.date(), userZoneId), userZoneId);

        //当前天
        boolean isToday = DateUtil.formatDate(targetDate).equals(DateUtil.formatDate(nowDateTime));

        LocalTime beginDay = LocalTime.of(0, 0);
        LocalTime endDay = LocalTime.of(23, 59);
        LocalTime start = timeRange.getStart();
        LocalTime end = timeRange.getEnd();
        //是今天，开始时间取当前时间
        if (isToday) {
            LocalTime now = LocalTime.of(DateUtil.hour(nowDateTime, true), DateUtil.minute(nowDateTime));
            if (now.compareTo(start) >= 0) {
                start = now;
            } else {
                LocalTime localTime = start.plusMinutes(30);
                if (localTime.getHour() != 0) {
                    start = localTime;
                }
            }
        } else {
            //不是今天
            if (timeRange.getStart().equals(beginDay)) {
                start = beginDay;
            } else {
                LocalTime localTime = start.plusMinutes(30);
                if (!LocalTime.MIN.equals(localTime)) {
                    start = localTime;
                }

            }
        }

        if (timeRange.getEnd().equals(endDay)) {
            end = endDay;
        } else {
            end = timeRange.getEnd().minusMinutes(30);
        }

        if (start.isAfter(endDay)) {
            return Collections.emptyList();
        }
        if (end.isBefore(beginDay)) {
            return Collections.emptyList();
        }
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
            //设置返回的展示时间
            if ((getSeconds(intervalEnd) - getSeconds(intervalStart)) >= minIntervalInHours * 60 * 60) {
                rangeList.add(new TimeRange(intervalStart, intervalEnd));
            }
        }
        //排序
        rangeList.sort(Comparator.comparing(TimeRange::getStart));
        return rangeList;
    }

    static int getSeconds(LocalTime localTime) {
        if (ObjectUtil.isNull(localTime)) {
            return 0;
        }
        return localTime.getHour() * 60 * 60 + localTime.getMinute() * 60 + localTime.getSecond();
    }

    static LocalTime getAround(LocalTime localTime) {
        if (ObjectUtil.isNull(localTime)) {
            return localTime;
        }

        int minute = localTime.getMinute();
        int hour = localTime.getHour();
        if (minute == 59 && hour == 23) {
            return localTime;
        }

        if (minute == 29) {
            minute++;
        }
        if (minute == 59) {
            minute = 0;
            hour++;
        }

        return LocalTime.of(hour, minute);
    }

    @Data
    public static class TimeRange implements Serializable {
        LocalTime start;
        LocalTime end;

        public TimeRange(LocalTime start, LocalTime end) {
            this.start = start;
            this.end = end;
        }

        public TimeRange(Date lockStartTime, Date lockEndTime, ZoneId userZoneId) {

            TimeZone userTimeZone = TimeZone.getTimeZone(userZoneId);
            TimeZone zeroTimeZone = TimeZone.getTimeZone(ZoneId.of("GMT"));
            int timeZoneOffset = userTimeZone.getRawOffset() - zeroTimeZone.getRawOffset();
            this.start = convertDateToLocalTime(lockStartTime, timeZoneOffset);
            this.end = convertDateToLocalTime(lockEndTime, timeZoneOffset);
        }

        public static LocalTime convertDateToLocalTime(Date date, int timeZoneOffset) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.add(Calendar.MILLISECOND, timeZoneOffset);
            Date newDate = calendar.getTime();
            // 从Date对象中获取毫秒值
            long millis = newDate.getTime();
            DateTime dateTime = DateUtil.beginOfDay(newDate);
            millis = millis - dateTime.getTime();
            // 将毫秒值转换为秒值
            long seconds = millis / 1000;

            // 将秒值转换为小时、分钟和秒
            long hours = seconds / 3600;
            long minutes = (seconds % 3600) / 60;
            long remainingSeconds = seconds % 60;

            // 使用这些值创建一个LocalTime对象
            return LocalTime.of((int)hours, (int)minutes, 0);
        }

        @Override
        public String toString() {
            return start + " - " + end;
        }
    }
}

