package net.muxi.huashiapp.ui.schedule;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.TranslateAnimation;
import android.widget.GridLayout;
import android.widget.TextView;

import net.muxi.huashiapp.R;
import net.muxi.huashiapp.util.DimensUtil;

/**
 * Created by ybao on 17/1/29.
 * 选择周次的 view
 */

public class WeekSelectedView extends GridLayout {

    private TextView[] mTvWeeks;
    private int selectedWeekPos = 21;
    private OnWeekSelectedListener mOnWeekSelectedListener;

    public static final int SELECTED_VIEW_HEIGHT = (DimensUtil.getScreenWidth() - DimensUtil.dp2px(32)) / 7;

    public WeekSelectedView(Context context) {
        this(context, null);
    }

    public WeekSelectedView(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.setColumnCount(7);
        this.setRowCount(3);
        this.setPadding(DimensUtil.dp2px(16),0,DimensUtil.dp2px(16),0);
        this.setBackgroundColor(Color.WHITE);

        mTvWeeks = new TextView[21];

        for (int i = 0; i < 21; i++) {
            mTvWeeks[i] = new TextView(context);
            mTvWeeks[i].setText((i + 1) + "");
            mTvWeeks[i].setTextColor(getResources().getColor(android.R.color.primary_text_light));
            mTvWeeks[i].setGravity(Gravity.CENTER);
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(SELECTED_VIEW_HEIGHT,SELECTED_VIEW_HEIGHT);
            this.addView(mTvWeeks[i],params);
            final int selectedWeek = i;
            mTvWeeks[i].setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mOnWeekSelectedListener != null) {
                        mOnWeekSelectedListener.onWeekSelected(selectedWeek);
                    }
                    setSelectedWeek(selectedWeek);
                    slideUp();
                }
            });
        }

    }

    public void setSelectedWeek(int week) {
        if (selectedWeekPos != 21){
            mTvWeeks[selectedWeekPos].setBackground(null);
        }
        mTvWeeks[week].setBackgroundResource(R.drawable.bg_selected_week);
        selectedWeekPos = week;
    }

    public int getSelectedWeek(){
        return selectedWeekPos;
    }

    public void slideUp(){
        slide(DimensUtil.dp2px(SELECTED_VIEW_HEIGHT * 3));
    }

    public void slideDown(){
        slide(0);
    }

    public void slide(int toY){
        TranslateAnimation animation;
        if (toY < 0){
            animation = new TranslateAnimation(0,0,0,toY);
        }else {
            animation = new TranslateAnimation(0,0,toY,0);
        }
        animation.setDuration(250);
        animation.setFillAfter(true);
        this.startAnimation(animation);
    }

    public void setOnWeekSelectedListener(OnWeekSelectedListener onWeekSelectedListener){
        mOnWeekSelectedListener = onWeekSelectedListener;
    }

    public interface OnWeekSelectedListener{

        void onWeekSelected(int week);
    }
}