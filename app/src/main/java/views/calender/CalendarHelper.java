package views.calender;

import android.annotation.SuppressLint;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by dharmraj on 30/12/17.
 */
@SuppressLint("WrongConstant")
public class CalendarHelper {


    public static int getDayShift(Calendar calendar, boolean z) {
        int i = 0;
        Calendar calendar2 = (Calendar) calendar.clone();
        calendar2.setMinimalDaysInFirstWeek(1);
        calendar2.set(5, 1);
        int i2 = calendar2.get(7);
        int firstDayOfWeek = calendar2.getFirstDayOfWeek();
        if (i2 != firstDayOfWeek) {
            if (i2 > firstDayOfWeek) {
                i = i2 - firstDayOfWeek;
            } else if (i2 < firstDayOfWeek) {
                i = (i2 + 7) - firstDayOfWeek;
            }
        }
        if (z) {
            return (calendar2.getActualMaximum(5) + i) % 7;
        }
        return i;
    }

    public static List<Calendar> getCalendarStartAndEndDateForDesiredPeriod(Calendar calendar) {
        Calendar calendar2 = (Calendar) calendar.clone();
        calendar2.setMinimalDaysInFirstWeek(1);
        int actualMaximum = calendar2.getActualMaximum(4) * 7;
        calendar2.set(5, 1);
        calendar2.add(5, -getDayShift(calendar2, false));
        List<Calendar> arrayList = new ArrayList();
        arrayList.add((Calendar) calendar2.clone());
        calendar2.add(5, actualMaximum);
        arrayList.add((Calendar) calendar2.clone());
        return arrayList;
    }

    public static int getMonthDifference(Calendar calendar, Calendar calendar2) {
        if (calendar == null || calendar2 == null) {
            return 0;
        }
        int i = calendar.get(1);
        return (((i - calendar2.get(1)) * 12) + calendar.get(2)) - calendar2.get(2);
    }

    public static boolean isSameMonth(Calendar calendar, Calendar calendar2) {
        if (calendar == null || calendar2 == null) {
            return false;
        }
        int i = calendar.get(2);
        int i2 = calendar2.get(2);
        int i3 = calendar.get(1);
        int i4 = calendar2.get(1);
        if (i == i2 && i3 == i4) {
            return true;
        }
        return false;
    }

    public static boolean isSameDay(Calendar calendar, Calendar calendar2) {
        if (calendar == null || calendar2 == null) {
            return false;
        }
        int i = calendar.get(1);
        int i2 = calendar.get(2);
        int i3 = calendar.get(5);
        int i4 = calendar2.get(1);
        int i5 = calendar2.get(2);
        int i6 = calendar2.get(5);
        if (i == i4 && i2 == i5 && i3 == i6) {
            return true;
        }
        return false;
    }

    public static boolean isToday(Calendar calendar) {
        return isSameDay(Calendar.getInstance(), calendar);
    }

    public static boolean isYesterday(Calendar calendar) {
        Calendar instance = Calendar.getInstance();
        instance.add(5, -1);
        return isSameDay(instance, calendar);
    }

    public static boolean isSameHour(Calendar calendar, Calendar calendar2) {
        if (calendar == null || calendar2 == null) {
            return false;
        }
        int i = calendar.get(11);
        int i2 = calendar.get(12);
        int i3 = calendar2.get(11);
        int i4 = calendar2.get(12);
        if (i == i3 && i2 == i4) {
            return true;
        }
        return false;
    }

    public static int compareDateWithoutTime(Date date, Date date2) {
        Calendar instance = Calendar.getInstance();
        instance.setTime(date);
        Calendar instance2 = Calendar.getInstance();
        instance2.setTime(date2);
        return compareDateWithoutTime(instance, instance2);
    }

    public static int compareDateWithoutTime(Calendar calendar, Calendar calendar2) {
        int i = calendar.get(1);
        int i2 = calendar2.get(1);
        if (i < i2) {
            return -1;
        }
        if (i > i2) {
            return 1;
        }
        i = calendar.get(2);
        i2 = calendar2.get(2);
        if (i < i2) {
            return -1;
        }
        if (i > i2) {
            return 1;
        }
        i = calendar.get(5);
        i2 = calendar2.get(5);
        if (i < i2) {
            return -1;
        }
        if (i > i2) {
            return 1;
        }
        return 0;
    }
}
