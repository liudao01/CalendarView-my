package com.othershe.calendarviewtest;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.othershe.calendarview.CalendarView;
import com.othershe.calendarview.DateBean;
import com.othershe.calendarview.listener.CalendarViewAdapter;
import com.othershe.calendarview.listener.OnMonthItemClickListener;
import com.othershe.calendarview.listener.OnPagerChangeListener;

public class MainActivity extends AppCompatActivity {

    private CalendarView calendarView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final TextView title = (TextView) findViewById(R.id.title);
        calendarView = (CalendarView) findViewById(R.id.calendar);
        calendarView.init();
//        calendarView.setOnCalendarViewAdapter(R.layout.item_layout, new CalendarViewAdapter() {
//            @Override
//            public TextView[] convertView(View view, DateBean date) {
//                TextView solarDay = (TextView) view.findViewById(R.id.solar_day);
//                TextView lunarDay = (TextView) view.findViewById(R.id.lunar_day);
//                return new TextView[]{solarDay, lunarDay};
//            }
//        });

        DateBean d = calendarView.getDateInit();

        title.setText(d.getSolar()[0] + "年" + d.getSolar()[1] + "月" + d.getSolar()[2] + "日");

        calendarView.setOnPagerChangeListener(new OnPagerChangeListener() {
            @Override
            public void onPagerChanged(int[] date) {
                title.setText(date[0] + "年" + date[1] + "月" + date[2] + "日");
            }
        });

        calendarView.setOnItemClickListener(new OnMonthItemClickListener() {
            @Override
            public void onMonthItemClick(View view, DateBean date) {
                title.setText(date.getSolar()[0] + "年" + date.getSolar()[1] + "月" + date.getSolar()[2] + "日");
            }
        });
    }

    public void someday(View v) {
        View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.input_layout, null);
        final EditText year = (EditText) view.findViewById(R.id.year);
        final EditText month = (EditText) view.findViewById(R.id.month);
        final EditText day = (EditText) view.findViewById(R.id.day);

        new AlertDialog.Builder(this)
                .setView(view)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (TextUtils.isEmpty(year.getText())
                                || TextUtils.isEmpty(month.getText())
                                || TextUtils.isEmpty(day.getText())) {
                            Toast.makeText(MainActivity.this, "请完善日期！", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        calendarView.toSpecifyDate(Integer.valueOf(year.getText().toString()),
                                Integer.valueOf(month.getText().toString()),
                                Integer.valueOf(day.getText().toString()));
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("取消", null).show();
    }

    public void today(View view) {
        calendarView.today();
    }

    public void lastMonth(View view) {
        calendarView.lastMonth();
    }

    public void nextMonth(View view) {
        calendarView.nextMonth();
    }

    public void start(View view) {
        calendarView.toStart();
    }

    public void end(View view) {
        calendarView.toEnd();
    }

    public void lastYear(View view) {
        calendarView.lastYear();
    }

    public void nextYear(View view) {
        calendarView.nextYear();
    }

    public void multiChoose(View view) {
        startActivity(new Intent(MainActivity.this, MultiChooseActivity.class));
    }
}
