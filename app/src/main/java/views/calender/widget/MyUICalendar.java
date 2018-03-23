package views.calender.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import com.mualab.org.user.R;
import views.calender.data.Day;
import views.calender.view.LockScrollView;

/**
 * Created by AzureChen on 15/8/9.
 */
public abstract class MyUICalendar extends LinearLayout {

    // Style
    public static final int STYLE_LIGHT  = 0;
    public static final int STYLE_PINK   = 1;
    public static final int STYLE_ORANGE = 2;
    public static final int STYLE_BLUE   = 3;
    public static final int STYLE_GREEN  = 4;
    // Day of Week
    public static final int SUNDAY    = 0;
    public static final int MONDAY    = 1;
    public static final int TUESDAY   = 2;
    public static final int WEDNESDAY = 3;
    public static final int THURSDAY  = 4;
    public static final int FRIDAY    = 5;
    public static final int SATURDAY  = 6;
    // State
    public static final int STATE_EXPANDED   = 0;
    public static final int STATE_COLLAPSED  = 1;
    public static final int STATE_PROCESSING = 2;

    protected Context mContext;
    protected LayoutInflater mInflater;

    // UI
    protected LinearLayout mLayoutRoot;
    protected TextView mTxtTitle;
    protected ImageView ivDropDown;
    protected TableLayout mTableHead;
    protected LockScrollView mScrollViewBody;
    protected TableLayout mTableBody;
    protected RelativeLayout mLayoutBtnGroupMonth;
    protected RelativeLayout mLayoutBtnGroupWeek;
    protected RelativeLayout mBtnPrevMonth;
    protected RelativeLayout mBtnNextMonth;
    protected RelativeLayout mBtnPrevWeek;
    protected RelativeLayout mBtnNextWeek;

    // Attributes
    private int mStyle = STYLE_LIGHT;
    private boolean mShowWeek = true;
    private int mFirstDayOfWeek = SUNDAY;
    private int mState = STATE_EXPANDED;

    private int mTextColor = Color.BLACK;
    private int mPrimaryColor = Color.BLACK;
    private int mTodayItemTextColor = Color.WHITE;
    private int mSelectedItemTextColor = Color.WHITE;

    // private Drawable mTodayItemBackgroundDrawable = getResources().getDrawable(R.drawable.circle_black_stroke_background);
    private Drawable mTodayItemBackgroundDrawable =
            getResources().getDrawable(R.drawable.circle_blue_solid_background);
    private Drawable mSelectedItemBackgroundDrawable =
            getResources().getDrawable(R.drawable.circle_green_solid_background);
    private Drawable mButtonLeftDrawable =
            getResources().getDrawable(R.drawable.white_back_ico);
    private Drawable mButtonRightDrawable =
            getResources().getDrawable(R.drawable.white_back_ico);
    private Day mSelectedItem = null;

    public MyUICalendar(Context context) {
        this(context, null);
    }

    public MyUICalendar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyUICalendar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init(context);
        TypedArray attributes = context.getTheme().obtainStyledAttributes(
                attrs, R.styleable.MyUICalendar, defStyleAttr, 0);
        setAttributes(attributes);
        attributes.recycle();
    }

    protected abstract void redraw();
    protected abstract void reload();

    protected void init(Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(mContext);

        // load rootView from xml
        View rootView = mInflater.inflate(R.layout.widget_flexible_calendar, this, true);

        // init UI
        mLayoutRoot          =  rootView.findViewById(R.id.layout_root);
        mTxtTitle            =  rootView.findViewById(R.id.txt_title);
        ivDropDown            =  rootView.findViewById(R.id.ivDropDownCal);
        mTableHead           =  rootView.findViewById(R.id.table_head);
        mScrollViewBody      =  rootView.findViewById(R.id.scroll_view_body);
        mTableBody           =  rootView.findViewById(R.id.table_body);
        mLayoutBtnGroupMonth =  rootView.findViewById(R.id.layout_btn_group_month);
        mLayoutBtnGroupWeek  =  rootView.findViewById(R.id.layout_btn_group_week);
        mBtnPrevMonth        =  rootView.findViewById(R.id.btn_prev_month);
        mBtnNextMonth        =  rootView.findViewById(R.id.btn_next_month);
        mBtnPrevWeek         =  rootView.findViewById(R.id.btn_prev_week);
        mBtnNextWeek         =  rootView.findViewById(R.id.btn_next_week);
    }

    protected void setAttributes(TypedArray attrs) {
        // set attributes by the values from XML
        setStyle(attrs.getInt(R.styleable.MyUICalendar_style, mStyle));
        setShowWeek(attrs.getBoolean(R.styleable.MyUICalendar_showWeek, mShowWeek));
        setFirstDayOfWeek(attrs.getInt(R.styleable.MyUICalendar_firstDayOfWeek, mFirstDayOfWeek));
        setState(attrs.getInt(R.styleable.MyUICalendar_state, mState));

        setTextColor(attrs.getColor(R.styleable.MyUICalendar_textColor, mTextColor));
        setPrimaryColor(attrs.getColor(R.styleable.MyUICalendar_primaryColor, mPrimaryColor));

        setTodayItemTextColor(attrs.getColor(
                R.styleable.MyUICalendar_todayItem_textColor, mTodayItemTextColor));
        Drawable todayItemBackgroundDrawable =
                attrs.getDrawable(R.styleable.MyUICalendar_todayItem_background);
        if (todayItemBackgroundDrawable != null) {
            setTodayItemBackgroundDrawable(todayItemBackgroundDrawable);
        } else {
            setTodayItemBackgroundDrawable(mTodayItemBackgroundDrawable);
        }

        setSelectedItemTextColor(attrs.getColor(
                R.styleable.MyUICalendar_selectedItem_textColor, mSelectedItemTextColor));
        Drawable selectedItemBackgroundDrawable =
                attrs.getDrawable(R.styleable.MyUICalendar_selectedItem_background);
        if (selectedItemBackgroundDrawable != null) {
            setSelectedItemBackgroundDrawable(selectedItemBackgroundDrawable);
        } else {
            setSelectedItemBackgroundDrawable(mSelectedItemBackgroundDrawable);
        }

        Drawable buttonLeftDrawable =
                attrs.getDrawable(R.styleable.MyUICalendar_buttonLeft_drawable);
        if (buttonLeftDrawable != null) {
            setButtonLeftDrawable(buttonLeftDrawable);
        } else {
            setButtonLeftDrawable(mButtonLeftDrawable);
        }

        Drawable buttonRightDrawable =
                attrs.getDrawable(R.styleable.MyUICalendar_buttonRight_drawable);
        if (buttonRightDrawable != null) {
            setButtonRightDrawable(buttonRightDrawable);
        } else {
            setButtonRightDrawable(mButtonRightDrawable);
        }

        Day selectedItem   = null;
    }

    // getters and setters
    public int getStyle() {
        return mStyle;
    }

    public void setStyle(int style) {
        this.mStyle = style;

        if (style == STYLE_LIGHT) {
            setTextColor(Color.BLACK);
            setPrimaryColor(Color.WHITE);
            setTodayItemTextColor(Color.WHITE);
            setTodayItemBackgroundDrawable(
                    getResources().getDrawable(R.drawable.circle_blue_solid_background));
            setSelectedItemTextColor(Color.WHITE);
            setSelectedItemBackgroundDrawable(
                    getResources().getDrawable(R.drawable.circle_green_solid_background));
            setButtonLeftDrawable(
                    getResources().getDrawable(R.drawable.white_back_ico));
            setButtonRightDrawable(
                    getResources().getDrawable(R.drawable.ic_navigate_next_white));
        } /*else {
            setTextColor(Color.WHITE);
            setTodayItemTextColor(Color.WHITE);
            setTodayItemBackgroundDrawable(
                    getResources().getDrawable(R.drawable.circle_white_stroke_background));
            setSelectedItemBackgroundDrawable(
                    getResources().getDrawable(R.drawable.circle_white_solid_background));
            setButtonLeftDrawable(
                    getResources().getDrawable(R.drawable.ic_navigate_before_white));
            setButtonRightDrawable(
                    getResources().getDrawable(R.drawable.ic_navigate_next_white));

            int color = 0;
            if (style == STYLE_PINK) {
                color = mContext.getResources().getColor(R.color.primary_pink);
            }
            if (style == STYLE_ORANGE) {
                color = mContext.getResources().getColor(R.color.primary_orange);
            }
            if (style == STYLE_BLUE) {
                color = mContext.getResources().getColor(R.color.primary_blue);
            }
            if (style == STYLE_GREEN) {
                color = mContext.getResources().getColor(R.color.primary_green);
            }
            setPrimaryColor(color);
            setSelectedItemTextColor(color);
        }*/
    }

    public boolean isShowWeek() {
        return mShowWeek;
    }

    public void setShowWeek(boolean showWeek) {
        this.mShowWeek = showWeek;

        if (showWeek) {
            mTableHead.setVisibility(VISIBLE);
        } else {
            mTableHead.setVisibility(GONE);
        }
    }

    public int getFirstDayOfWeek() {
        return mFirstDayOfWeek;
    }

    public void setFirstDayOfWeek(int firstDayOfWeek) {
        this.mFirstDayOfWeek = firstDayOfWeek;
        reload();
    }

    public int getState() {
        return mState;
    }

    public void setState(int state) {
        this.mState = state;

        if (mState == STATE_EXPANDED) {
            mLayoutBtnGroupMonth.setVisibility(VISIBLE);
            mLayoutBtnGroupWeek.setVisibility(GONE);
        }
        if (mState == STATE_COLLAPSED) {
            mLayoutBtnGroupMonth.setVisibility(GONE);
            mLayoutBtnGroupWeek.setVisibility(VISIBLE);
        }
    }

    public int getTextColor() {
        return mTextColor;
    }

    public int getTextPrimaryColor() {
        return getResources().getColor(R.color.colorPrimary);
    }

    public void setTextColor(int textColor) {
        this.mTextColor = textColor;
        redraw();
        mTxtTitle.setTextColor(getResources().getColor(R.color.colorPrimary));
    }

    public int getPrimaryColor() {
        return mPrimaryColor;
    }

    public void setPrimaryColor(int primaryColor) {
        this.mPrimaryColor = primaryColor;
        redraw();

        mLayoutRoot.setBackgroundColor(mPrimaryColor);
    }

    public int getTodayItemTextColor() {
        return mTodayItemTextColor;
    }

    public void setTodayItemTextColor(int todayItemTextColor) {
        this.mTodayItemTextColor = todayItemTextColor;
        redraw();
    }

    public Drawable getTodayItemBackgroundDrawable() {
        return mTodayItemBackgroundDrawable;
    }

    public void setTodayItemBackgroundDrawable(Drawable todayItemBackgroundDrawable) {
        this.mTodayItemBackgroundDrawable = todayItemBackgroundDrawable;
        redraw();
    }

    public int getSelectedItemTextColor() {
        return mSelectedItemTextColor;
    }

    public void setSelectedItemTextColor(int selectedItemTextColor) {
        this.mSelectedItemTextColor = selectedItemTextColor;
        redraw();
    }

    public Drawable getSelectedItemBackgroundDrawable() {
        return mSelectedItemBackgroundDrawable;
    }

    public void setSelectedItemBackgroundDrawable(Drawable selectedItemBackground) {
        this.mSelectedItemBackgroundDrawable = selectedItemBackground;
        redraw();
    }

    public Drawable getButtonLeftDrawable() {
        return mButtonLeftDrawable;
    }

    public void setButtonLeftDrawable(Drawable buttonLeftDrawable) {
        this.mButtonLeftDrawable = buttonLeftDrawable;
        // mBtnPrevMonth.setImageDrawable(buttonLeftDrawable);
        // mBtnPrevWeek.setImageDrawable(buttonLeftDrawable);
    }

    public Drawable getButtonRightDrawable() {
        return mButtonRightDrawable;
    }

    public void setButtonRightDrawable(Drawable buttonRightDrawable) {
        this.mButtonRightDrawable = buttonRightDrawable;
        //  mBtnNextMonth.setImageDrawable(buttonRightDrawable);
        //  mBtnNextWeek.setImageDrawable(buttonRightDrawable);
    }

    public Day getSelectedItem() {
        return mSelectedItem;
    }

    public void setSelectedItem(Day selectedItem) {
        this.mSelectedItem = selectedItem;
    }

}
