package com.othershe.calendarview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.SparseArray;

import com.othershe.calendarview.listener.CalendarViewAdapter;
import com.othershe.calendarview.listener.OnMonthItemChooseListener;
import com.othershe.calendarview.listener.OnMonthItemClickListener;
import com.othershe.calendarview.listener.OnPagerChangeListener;
import com.othershe.calendarview.utils.CalendarUtil;
import com.othershe.calendarview.utils.SolarUtil;

import java.util.HashSet;

public class CalendarView extends ViewPager {
    //记录当前PagerAdapter的position
    private int currentPosition;

    private OnPagerChangeListener pagerChangeListener;
    private OnMonthItemClickListener itemClickListener;
    private OnMonthItemChooseListener itemChooseListener;
    private CalendarViewAdapter calendarViewAdapter;
    private int item_layout;

    private int[] dateStart;//日历的开始年、月
    private int[] dateEnd;//日历的结束年、月
    private int[] dateInit;//默认展示、选中的日期（年、月、日）
    private boolean showLastNext = true;//是否显示上个月、下个月
    private boolean showLunar = true;//是否显示农历
    private boolean showHoliday = true;//是否显示节假日(不显示农历则节假日无法显示，节假日会覆盖农历显示)
    private boolean showTerm = true;//是否显示节气
    private boolean disableBefore = false;//是否禁用默认选中日期前的所有日期
    private boolean switchChoose = true;//单选时切换月份，是否选中上次的日期
    private int colorSolar = Color.BLACK;//阳历的日期颜色
    private int colorLunar = Color.parseColor("#999999");//阴历的日期颜色
    private int colorHoliday = Color.parseColor("#EC9729");//节假日的颜色
    private int colorChoose = Color.WHITE;//选中的日期文字颜色
    private int sizeSolar = 14;//阳历日期文字尺寸
    private int sizeLunar = 8;//阴历日期文字尺寸
    private int dayBg = R.drawable.blue_circle;//选中的背景

    private int count;//ViewPager的页数
    private int[] lastClickDate = new int[2];//上次点击的日期
    private SparseArray<HashSet<Integer>> chooseDate = new SparseArray<>();//记录多选时全部选中的日期

    private CalendarPagerAdapter calendarPagerAdapter;

    public CalendarView(Context context) {
        this(context, null);
    }

    public CalendarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttr(context, attrs);
    }

    private void initAttr(Context context, AttributeSet attrs) {
        String dateStartStr = null;
        String dateEndStr = null;
        String dateInitStr = null;

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.CalendarView);
        for (int i = 0; i < ta.getIndexCount(); i++) {
            int attr = ta.getIndex(i);
            if (attr == R.styleable.CalendarView_date_start) {
                dateStartStr = ta.getString(attr);
            } else if (attr == R.styleable.CalendarView_date_end) {
                dateEndStr = ta.getString(attr);
            } else if (attr == R.styleable.CalendarView_date_init) {
                dateInitStr = ta.getString(attr);
            } else if (attr == R.styleable.CalendarView_show_last_next) {
                showLastNext = ta.getBoolean(attr, true);
            } else if (attr == R.styleable.CalendarView_show_lunar) {
                showLunar = ta.getBoolean(attr, true);
            } else if (attr == R.styleable.CalendarView_show_holiday) {
                showHoliday = ta.getBoolean(attr, true);
            } else if (attr == R.styleable.CalendarView_show_term) {
                showTerm = ta.getBoolean(attr, true);
            } else if (attr == R.styleable.CalendarView_disable_before) {
                disableBefore = ta.getBoolean(attr, false);
            } else if (attr == R.styleable.CalendarView_switch_choose) {
                switchChoose = ta.getBoolean(attr, true);
            } else if (attr == R.styleable.CalendarView_color_solar) {
                colorSolar = ta.getColor(attr, colorSolar);
            } else if (attr == R.styleable.CalendarView_size_solar) {
                sizeSolar = ta.getInteger(R.styleable.CalendarView_size_solar, sizeSolar);
            } else if (attr == R.styleable.CalendarView_color_lunar) {
                colorLunar = ta.getColor(attr, colorLunar);
            } else if (attr == R.styleable.CalendarView_size_lunar) {
                sizeLunar = ta.getDimensionPixelSize(R.styleable.CalendarView_size_lunar, sizeLunar);
            } else if (attr == R.styleable.CalendarView_color_holiday) {
                colorHoliday = ta.getColor(attr, colorHoliday);
            } else if (attr == R.styleable.CalendarView_color_choose) {
                colorChoose = ta.getColor(attr, colorChoose);
            } else if (attr == R.styleable.CalendarView_day_bg) {
                dayBg = ta.getResourceId(attr, dayBg);
            }
        }

        ta.recycle();

        dateStart = CalendarUtil.strToArray(dateStartStr);
        if (dateStart == null) {
            dateStart = new int[]{1900, 1};
        }
        dateEnd = CalendarUtil.strToArray(dateEndStr);
        if (dateEnd == null) {
            dateEnd = new int[]{2049, 12};
        }

        dateInit = CalendarUtil.strToArray(dateInitStr);
        if (dateInit == null) {
            dateInit = SolarUtil.getCurrentDate();
        }

        sizeSolar = CalendarUtil.getTextSize(context, sizeSolar);
        sizeLunar = CalendarUtil.getTextSize(context, sizeLunar);
    }

    public void init() {
        //根据设定的日期范围计算日历的页数
        count = (dateEnd[0] - dateStart[0]) * 12 + dateEnd[1] - dateStart[1] + 1;
        calendarPagerAdapter = new CalendarPagerAdapter(count);
        calendarPagerAdapter.setAttrValues(dateInit, dateStart,
                showLastNext, showLunar, showHoliday, showTerm, disableBefore,
                colorSolar, colorLunar, colorHoliday, colorChoose,
                sizeSolar, sizeLunar, dayBg);

        calendarPagerAdapter.setOnCalendarViewAdapter(item_layout, calendarViewAdapter);

        setAdapter(calendarPagerAdapter);

        currentPosition = CalendarUtil.dateToPosition(dateInit[0], dateInit[1], dateStart[0], dateStart[1]);
        lastClickDate[0] = currentPosition;
        lastClickDate[1] = dateInit[2];

        setLastChooseDate(dateInit[2], true);//因为有默认选中日期，所以需要此操作
        setCurrentItem(currentPosition, false);

        addOnPageChangeListener(new SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                refreshMonthView(position);
                currentPosition = position;
                if (pagerChangeListener != null) {
                    int[] date = CalendarUtil.positionToDate(position, dateStart[0], dateStart[1]);
                    pagerChangeListener.onPagerChanged(new int[]{date[0], date[1], lastClickDate[1]});
                }
            }
        });
    }

    /**
     * 计算 ViewPager 高度
     *
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int calendarHeight;
        if (getAdapter() != null) {
            MonthView view = (MonthView) getChildAt(0);
            if (view != null) {
                calendarHeight = view.getMeasuredHeight();
                setMeasuredDimension(widthMeasureSpec, MeasureSpec.makeMeasureSpec(calendarHeight, MeasureSpec.EXACTLY));
            }
        }
    }

    /**
     * 刷新MonthView
     *
     * @param position
     */
    private void refreshMonthView(int position) {
        MonthView monthView = calendarPagerAdapter.getViews().get(position);
        if (itemChooseListener != null) {
            if (chooseDate.get(position) != null)
                monthView.multiChooseRefresh(chooseDate.get(position));
        } else {
            boolean flag = (!switchChoose && lastClickDate[0] == position) || switchChoose;
            monthView.refresh(lastClickDate[1], flag);
        }
    }

    /**
     * 设置上次点击的日期
     *
     * @param day
     */
    public void setLastClickDay(int day) {
        lastClickDate[0] = currentPosition;
        lastClickDate[1] = day;
    }

    /**
     * 设置多选时选中的日期
     *
     * @param day
     * @param flag 多选时flag=true代表选中数据，flag=false代表取消选中
     */
    public void setLastChooseDate(int day, boolean flag) {
        HashSet<Integer> days = chooseDate.get(currentPosition);
        if (flag) {
            if (days == null) {
                days = new HashSet<>();
                chooseDate.put(currentPosition, days);
            }
            days.add(day);
        } else {
            days.remove(day);
        }
    }

    /**
     * 设置日期点击回调
     *
     * @param itemClickListener
     */
    public void setOnItemClickListener(OnMonthItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public OnMonthItemChooseListener getItemChooseListener() {
        return itemChooseListener;
    }

    /**
     * 设置日期多选回调
     *
     * @param itemChooseListener
     */
    public void setOnMonthItemChooseListener(OnMonthItemChooseListener itemChooseListener) {
        this.itemChooseListener = itemChooseListener;
    }

    public OnMonthItemClickListener getItemClickListener() {
        return itemClickListener;
    }

    /**
     * 设置月份切换回调
     *
     * @param pagerChangeListener
     */
    public void setOnPagerChangeListener(OnPagerChangeListener pagerChangeListener) {
        this.pagerChangeListener = pagerChangeListener;
    }

    /**
     * 设置自定义日期样式
     *
     * @param item_layout         自定义的日期item布局
     * @param calendarViewAdapter 解析item的接口
     */
    public void setOnCalendarViewAdapter(int item_layout, CalendarViewAdapter calendarViewAdapter) {
        this.item_layout = item_layout;
        this.calendarViewAdapter = calendarViewAdapter;

        init();
    }

    /**
     * 跳转到今天
     */
    public void today() {
        int destPosition = CalendarUtil.dateToPosition(SolarUtil.getCurrentDate()[0], SolarUtil.getCurrentDate()[1], dateStart[0], dateStart[1]);
        lastClickDate[0] = destPosition;
        lastClickDate[1] = SolarUtil.getCurrentDate()[2];
        if (destPosition == currentPosition) {
            refreshMonthView(destPosition);
        } else {
            setCurrentItem(destPosition, false);
        }
    }

    /**
     * 跳转到指定日期
     *
     * @param year
     * @param month
     * @param day
     */
    public void toSpecifyDate(int year, int month, int day) {
        int destPosition = CalendarUtil.dateToPosition(year, month, dateStart[0], dateStart[1]);
        if (!switchChoose && day != 0) {
            lastClickDate[0] = destPosition;
        }
        lastClickDate[1] = day != 0 ? day : lastClickDate[1];
        setCurrentItem(destPosition, false);
    }

    /**
     * 跳转到下个月
     */
    public void nextMonth() {
        if (currentPosition < count - 1)
            setCurrentItem(++currentPosition, false);
    }

    /**
     * 跳转到上个月
     */
    public void lastMonth() {
        if (currentPosition > 0)
            setCurrentItem(--currentPosition, false);
    }

    /**
     * 跳转到上一年的当前月
     */
    public void lastYear() {
        if (currentPosition - 12 >= 0) {
            setCurrentItem(currentPosition -= 12, false);
        }
    }

    /**
     * 跳转到下一年的当前月
     */
    public void nextYear() {
        if (currentPosition + 12 <= count) {
            setCurrentItem(currentPosition += 12, false);
        }
    }

    /**
     * 跳转到日历的开始年月
     */
    public void toStart() {
        toSpecifyDate(dateStart[0], dateStart[1], 0);
    }

    /**
     * 跳转到日历的结束年月
     */
    public void toEnd() {
        toSpecifyDate(dateEnd[0], dateEnd[1], 0);
    }

    /**
     * 得到默认选中的日期
     *
     * @return
     */
    public DateBean getDateInit() {
        return CalendarUtil.getDateBean(dateInit[0], dateInit[1], dateInit[2]);
    }
}
