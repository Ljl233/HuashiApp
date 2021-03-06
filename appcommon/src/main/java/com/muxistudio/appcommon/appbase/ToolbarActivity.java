package com.muxistudio.appcommon.appbase;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;

import com.muxistudio.appcommon.R;

/**
 * Created by ybao on 16/4/26.
 */
public abstract class ToolbarActivity extends BaseAppActivity{

    protected Toolbar mToolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    //这个方法被后面的类继承 并且初始化Toolbar
    @Override
    public void setContentView(int layoutResId) {
        super.setContentView(layoutResId);
        initToolbar();

    }

    public void initToolbar() {
        mToolbar = findViewById(R.id.toolbar);
        if (mToolbar != null) {
            this.setSupportActionBar(mToolbar);
            mToolbar.setTitle("华师匣子");
            if (canBack()){
                ActionBar actionbar = getSupportActionBar();
                if (actionbar != null){
                    actionbar.setDisplayHomeAsUpEnabled(true);
                }
            }
        }

    }


    public void setTitle(String title){
        if (mToolbar != null){
            mToolbar.setTitle(title);
            setSupportActionBar(mToolbar);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0 ){
            getSupportFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    //如果当前的Activity不能退出则需要改写
    protected boolean canBack() {
        return true;
    }
}
