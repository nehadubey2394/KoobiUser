package views.calender.widget;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.mualab.org.user.R;
import com.mualab.org.user.dialogs.MyToast;

import views.calender.data.CalendarAdapter;
import views.calender.data.Day;
import views.calender.data.Event;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by neha on 15/2/18.
 */
public class FlexibleCalendar extends UICalendar {

    private CalendarAdapter mAdapter;
    private CalendarListener mListener;
    private String sCurrentDay = "";
    private int mInitHeight = 0;

    private Handler mHandler = new Handler();
    private boolean mIsWaitingForUpdate = false;

    private int mCurrentWeekIndex;

    public FlexibleCalendar(Context context) {
        super(context);
    }

    public FlexibleCalendar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FlexibleCalendar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void init(Context context) {
        super.init(context);

        if (isInEditMode()) {
            Calendar cal = Calendar.getInstance();
            CalendarAdapter adapter = new CalendarAdapter(context, cal);
            setAdapter(adapter);
        }

        setStateWithUpdateUI(getState());

        // bind events
        mBtnPrevMonth.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                prevMonth();
            }
        });

        mBtnNextMonth.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                nextMonth();
            }
        });

        mBtnPrevWeek.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                prevWeek();
            }
        });

        mBtnNextWeek.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                nextWeek();
            }
        });

        // bind events collaps/expand
        ivDropDown.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getState() == STATE_COLLAPSED) {
                    expand(500);
                }else {
                    collapse(500);
                }
            }
        });

        mTxtTitle.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getState() == STATE_COLLAPSED) {
                    expand(500);
                }else {
                    collapse(500);
                }
            }
        });

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        mInitHeight = mTableBody.getMeasuredHeight();

        if (mIsWaitingForUpdate) {
            redraw();
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    collapseTo(mCurrentWeekIndex);
                }
            });
            mIsWaitingForUpdate = false;
            if (mListener != null) {
                mListener.onDataUpdate();
            }
        }
    }

    @Override
    protected void redraw() {
        // redraw all views of week
        TableRow rowWeek = (TableRow) mTableHead.getChildAt(0);
        if (rowWeek != null) {
            for (int i = 0; i < rowWeek.getChildCount(); i++) {
                ((TextView) rowWeek.getChildAt(i)).setTextColor(getTextPrimaryColor());
            }
        }
        // redraw all views of day
        if (mAdapter != null) {
            for (int i = 0; i < mAdapter.getCount(); i++) {
                Day day = mAdapter.getItem(i);
                View view = mAdapter.getView(i);
                TextView txtDay = view.findViewById(R.id.txt_day);
                txtDay.setBackgroundColor(Color.TRANSPARENT);
                txtDay.setTextColor(getTextColor());

                SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM yyyy");
                dateFormat.setTimeZone(mAdapter.getCalendar().getTimeZone());

                // set today's item
                if (isToady(day)) {
                    // mTxtTitle.setText(sCurrentDay +" " + day.getDay()+" "+ dateFormat.format(mAdapter.getCalendar().getTime()));
                    mTxtTitle.setText(dateFormat.format(mAdapter.getCalendar().getTime()));
                    txtDay.setBackgroundDrawable(getTodayItemBackgroundDrawable());
                    txtDay.setTextColor(getTodayItemTextColor());
                }

                // set the selected item
                if (isSelectedDay(day)) {
                    Date currentDay = mAdapter.getCalendar().getTime();
                    Calendar todayCal = Calendar.getInstance();
                    int cYear  = todayCal.get(Calendar.YEAR);
                    int cMonth  = todayCal.get(Calendar.MONTH)+1;
                    int cDay  = todayCal.get(Calendar.DAY_OF_MONTH);

                    int year = day.getYear();
                    int month =  day.getMonth()+1;
                    int dayOfMonth =  day.getDay();

                    if (year>=cYear && month>=cMonth){
                        if (year==cYear && month==cMonth && dayOfMonth<=cDay){
                            MyToast.getInstance(mContext).showSmallCustomToast("You can't select previous date for booking.");
                        }else {
                            mTxtTitle.setText(dateFormat.format(mAdapter.getCalendar().getTime()));
                            txtDay.setBackgroundDrawable(getSelectedItemBackgroundDrawable());
                            txtDay.setTextColor(getSelectedItemTextColor());
                        }
                    }else {
                        MyToast.getInstance(mContext).showSmallCustomToast("You can't select previous date for booking.");
                    }
/*
                    if (dayOfMonth > cDay && cMonth==(month) && cYear== year){
                        //    txtDay.setAlpha(1f);
                        mTxtTitle.setText(dateFormat.format(mAdapter.getCalendar().getTime()));
                        txtDay.setBackgroundDrawable(getSelectedItemBackgroundDrawable());
                        txtDay.setTextColor(getSelectedItemTextColor());
                    }else if (dayOfMonth <= cDay && month >(cMonth) && year>=cYear){
                        txtDay.setAlpha(1f);
                        mTxtTitle.setText(dateFormat.format(mAdapter.getCalendar().getTime()));
                        txtDay.setBackgroundDrawable(getSelectedItemBackgroundDrawable());
                        txtDay.setTextColor(getSelectedItemTextColor());
                    }else {
                        // txtDay.setAlpha(0.5f);
                        MyToast.getInstance(mContext).showSmallCustomToast("YOu can't select previous date for booking.");
                    }*/

                   /* mTxtTitle.setText(dateFormat.format(mAdapter.getCalendar().getTime()));
                    txtDay.setBackgroundDrawable(getSelectedItemBackgroundDrawable());
                    txtDay.setTextColor(getSelectedItemTextColor());*/
                }
            }
        }
    }

    @Override
    protected void reload() {
        if (mAdapter != null) {
            mAdapter.refresh();

            // reset UI
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM yyyy");
            dateFormat.setTimeZone(mAdapter.getCalendar().getTimeZone());

            mTxtTitle.setText(dateFormat.format(mAdapter.getCalendar().getTime()));
            mTableHead.removeAllViews();
            mTableBody.removeAllViews();

            TableRow rowCurrent;

            // set day of week
            int[] dayOfWeekIds = {
                    R.string.sunday,
                    R.string.monday,
                    R.string.tuesday,
                    R.string.wednesday,
                    R.string.thursday,
                    R.string.friday,
                    R.string.saturday
            };
            rowCurrent = new TableRow(mContext);
            rowCurrent.setLayoutParams(new TableLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            for (int i = 0; i < 7; i++) {
                View view = mInflater.inflate(R.layout.layout_day_of_week, null);
                TextView txtDayOfWeek = view.findViewById(R.id.txt_day_of_week);
                txtDayOfWeek.setText(dayOfWeekIds[(i + getFirstDayOfWeek()) % 7]);
                sCurrentDay = txtDayOfWeek.getText().toString();
                txtDayOfWeek.setTextColor(getResources().getColor(R.color.colorPrimary));

                view.setLayoutParams(new TableRow.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        1));
                rowCurrent.addView(view);
            }
            mTableHead.addView(rowCurrent);

            // set day view
            for (int i = 0; i < mAdapter.getCount(); i++) {
                final int position = i;

                if (position % 7 == 0) {
                    rowCurrent = new TableRow(mContext);
                    rowCurrent.setLayoutParams(new TableLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));
                    mTableBody.addView(rowCurrent);
                }
                final View view = mAdapter.getView(position);
                view.setLayoutParams(new TableRow.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        1));
                view.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onItemClicked(v, mAdapter.getItem(position));
                    }
                });
                rowCurrent.addView(view);
            }

            redraw();
            mIsWaitingForUpdate = true;
        }
    }

    private int getSuitableRowIndex() {
        if (getSelectedItemPosition() != -1) {
            View view = mAdapter.getView(getSelectedItemPosition());
            TableRow row = (TableRow) view.getParent();

            return mTableBody.indexOfChild(row);
        } else if (getTodayItemPosition() != -1) {
            View view = mAdapter.getView(getTodayItemPosition());
            TableRow row = (TableRow) view.getParent();

            return mTableBody.indexOfChild(row);
        } else {
            return 0;
        }
    }

    private void onItemClicked(View view, Day day) {
        select(day);

        //  Day day1 = getSelectedDay();

        Calendar cal = mAdapter.getCalendar();

        int newYear = day.getYear();
        int newMonth = day.getMonth();
        int oldYear = cal.get(Calendar.YEAR);
        int oldMonth = cal.get(Calendar.MONTH);

        if (newMonth != oldMonth) {
            cal.set(day.getYear(), day.getMonth(), 1);

            if (newYear > oldYear || newMonth > oldMonth) {
                mCurrentWeekIndex = 0;
            }
            if (newYear < oldYear || newMonth < oldMonth) {
                mCurrentWeekIndex = -1;
            }
            if (mListener != null) {
                mListener.onMonthChange();
            }
            reload();
        }

        if (mListener != null) {
            mListener.onItemClick(view);
        }
    }

    // public methods
    public void setAdapter(CalendarAdapter adapter) {
        mAdapter = adapter;
        adapter.setFirstDayOfWeek(getFirstDayOfWeek());

        reload();

        // init week
        mCurrentWeekIndex = getSuitableRowIndex();
    }

    public void addEventTag(int numYear, int numMonth, int numDay) {
        mAdapter.addEvent(new Event(numYear, numMonth, numDay));

        reload();
    }

    public void prevMonth() {
        Calendar cal = mAdapter.getCalendar();
        if (cal.get(Calendar.MONTH) == cal.getActualMinimum(Calendar.MONTH)) {
            cal.set((cal.get(Calendar.YEAR) - 1), cal.getActualMaximum(Calendar.MONTH), 1);
        } else {
            cal.set(Calendar.MONTH, cal.get(Calendar.MONTH) - 1);
        }
        reload();
        if (mListener != null) {
            mListener.onMonthChange();
        }
    }

    public void nextMonth() {
        Calendar cal = mAdapter.getCalendar();
        if (cal.get(Calendar.MONTH) == cal.getActualMaximum(Calendar.MONTH)) {
            cal.set((cal.get(Calendar.YEAR) + 1), cal.getActualMinimum(Calendar.MONTH), 1);
        } else {
            cal.set(Calendar.MONTH, cal.get(Calendar.MONTH) + 1);
        }
        reload();
        if (mListener != null) {
            mListener.onMonthChange();
        }
    }

    public void prevWeek() {
        if (mCurrentWeekIndex - 1 < 0) {
            mCurrentWeekIndex = -1;
            prevMonth();
        } else {
            mCurrentWeekIndex--;
            collapseTo(mCurrentWeekIndex);
        }
    }

    public void nextWeek() {
        if (mCurrentWeekIndex + 1 >= mTableBody.getChildCount()) {
            mCurrentWeekIndex = 0;
            nextMonth();
        } else {
            mCurrentWeekIndex++;
            collapseTo(mCurrentWeekIndex);
        }
    }

    public int getYear() {
        return mAdapter.getCalendar().get(Calendar.YEAR);
    }

    public int getMonth() {
        return mAdapter.getCalendar().get(Calendar.MONTH);
    }

    public Day getSelectedDay() {
        return new Day(
                getSelectedItem().getYear(),
                getSelectedItem().getMonth(),
                getSelectedItem().getDay());
    }

    public boolean isSelectedDay(Day day) {
        return day != null
                && getSelectedItem() != null
                && day.getYear() == getSelectedItem().getYear()
                && day.getMonth() == getSelectedItem().getMonth()
                && day.getDay() == getSelectedItem().getDay();
    }

    public boolean isToady(Day day) {
        Calendar todayCal = Calendar.getInstance();
        return day != null
                && day.getYear() == todayCal.get(Calendar.YEAR)
                && day.getMonth() == todayCal.get(Calendar.MONTH)
                && day.getDay() == todayCal.get(Calendar.DAY_OF_MONTH);
    }

    public int getSelectedItemPosition() {
        int position = -1;
        for (int i = 0; i < mAdapter.getCount(); i++) {
            Day day = mAdapter.getItem(i);

            if (isSelectedDay(day)) {
                position = i;
                break;
            }
        }
        return position;
    }

    public int getTodayItemPosition() {
        int position = -1;
        for (int i = 0; i < mAdapter.getCount(); i++) {
            Day day = mAdapter.getItem(i);

            if (isToady(day)) {
                position = i;
                break;
            }
        }
        return position;
    }

    public void collapse(int duration) {
        if (getState() == STATE_EXPANDED) {
            setState(STATE_PROCESSING);

            mLayoutBtnGroupMonth.setVisibility(GONE);
            mLayoutBtnGroupWeek.setVisibility(VISIBLE);
            mBtnPrevWeek.setClickable(false);
            mBtnNextWeek.setClickable(false);

            int index = getSuitableRowIndex();
            mCurrentWeekIndex = index;

            final int currentHeight = mInitHeight;
            final int targetHeight = mTableBody.getChildAt(index).getMeasuredHeight();
            int tempHeight = 0;
            for (int i = 0; i < index; i++) {
                tempHeight += mTableBody.getChildAt(i).getMeasuredHeight();
            }
            final int topHeight = tempHeight;

            Animation anim = new Animation() {
                @Override
                protected void applyTransformation(float interpolatedTime, Transformation t) {

                    mScrollViewBody.getLayoutParams().height = (interpolatedTime == 1)
                            ? targetHeight
                            : currentHeight - (int) ((currentHeight - targetHeight) * interpolatedTime);
                    mScrollViewBody.requestLayout();

                    if (mScrollViewBody.getMeasuredHeight() < topHeight + targetHeight) {
                        int position = topHeight + targetHeight - mScrollViewBody.getMeasuredHeight();
                        mScrollViewBody.smoothScrollTo(0, position);
                    }

                    if (interpolatedTime == 1) {
                        setState(STATE_COLLAPSED);
                        mBtnPrevWeek.setClickable(true);
                        mBtnNextWeek.setClickable(true);
                    }
                }
            };
            anim.setDuration(duration);
            startAnimation(anim);
            ivDropDown.setRotation(360);
        }
    }

    private void collapseTo(int index) {
        if (getState() == STATE_COLLAPSED) {
            if (index == -1) {
                index = mTableBody.getChildCount() - 1;
            }
            mCurrentWeekIndex = index;

            final int targetHeight = mTableBody.getChildAt(index).getMeasuredHeight();
            int tempHeight = 0;
            for (int i = 0; i < index; i++) {
                tempHeight += mTableBody.getChildAt(i).getMeasuredHeight();
            }
            final int topHeight = tempHeight;

            mScrollViewBody.getLayoutParams().height = targetHeight;
            mScrollViewBody.requestLayout();

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mScrollViewBody.smoothScrollTo(0, topHeight);
                }
            });


            if (mListener != null) {
                mListener.onWeekChange(mCurrentWeekIndex);
            }
        }
    }

    public void expand(int duration) {
        if (getState() == STATE_COLLAPSED) {
            setState(STATE_PROCESSING);

            mLayoutBtnGroupMonth.setVisibility(VISIBLE);
            mLayoutBtnGroupWeek.setVisibility(GONE);
            mBtnPrevMonth.setClickable(false);
            mBtnNextMonth.setClickable(false);

            final int currentHeight = mScrollViewBody.getMeasuredHeight();
            final int targetHeight = mInitHeight;

            Animation anim = new Animation() {
                @Override
                protected void applyTransformation(float interpolatedTime, Transformation t) {

                    mScrollViewBody.getLayoutParams().height = (interpolatedTime == 1)
                            ? LayoutParams.WRAP_CONTENT
                            : currentHeight - (int) ((currentHeight - targetHeight) * interpolatedTime);
                    mScrollViewBody.requestLayout();

                    if (interpolatedTime == 1) {
                        setState(STATE_EXPANDED);

                        mBtnPrevMonth.setClickable(true);
                        mBtnNextMonth.setClickable(true);
                    }
                }
            };
            anim.setDuration(duration);
            startAnimation(anim);
            ivDropDown.setRotation(180);
        }
    }

    public void select(Day day) {
        setSelectedItem(new Day(day.getYear(), day.getMonth(), day.getDay()));
        redraw();

        if (mListener != null) {
            mListener.onDaySelect();
        }
    }

    public void setStateWithUpdateUI(int state) {
        setState(state);
        if (getState() != state) {
            mIsWaitingForUpdate = true;
            requestLayout();
        }
    }

    // callback
    public void setCalendarListener(CalendarListener listener) {
        mListener = listener;
    }

    public interface CalendarListener {

        // triggered when a day is selected programmatically or clicked by user.
        void onDaySelect();

        // triggered only when the views of day on calendar are clicked by user.
        void onItemClick(View v);

        // triggered when the data of calendar are updated by changing month or adding events.
        void onDataUpdate();

        // triggered when the month are changed.
        void onMonthChange();

        // triggered when the week position are changed.
        void onWeekChange(int position);
    }
}
