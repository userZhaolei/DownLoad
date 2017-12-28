package com.aaron.download_demo;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import com.aaron.download_demo.utils.SPUtils;

/**
 * 作者：哇牛Aaron
 * 作者简书文章地址: http://www.jianshu.com/users/07a8b5386866/latest_articles
 * 时间: 2016/11/18
 * 功能描述: 设置ToggleButton开关状态 并保存到SP存储中
 */

public class SettingActivity extends Activity {
    private ToggleButton tb_auto_update;//自动更新开关

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        initView();
        setToggleButton();
        setListener();
    }

    //设置开关打开或关闭的显示情况
    private void setToggleButton() {
        //获取开关状态 默认值为false
        Boolean aBoolean = (Boolean) SPUtils.get(this,  SPUtils.WIFI_DOWNLOAD_SWITCH, false);

        Log.e("TAG", "SP开关状态 == " + aBoolean);
        if (aBoolean) {//打开状态 样式改为打开
            tb_auto_update.setChecked(true);
        } else {//关闭状态  样式改为关闭
            tb_auto_update.setChecked(false);
        }
    }

    //设置监听
    private void setListener() {
        tb_auto_update.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {//如果打开状态
                    //保存SP 开关状态值 开启
                    SPUtils.put(SettingActivity.this, SPUtils.WIFI_DOWNLOAD_SWITCH, true);
                } else {
                    //保存SP 开关状态值 关闭
                    SPUtils.put(SettingActivity.this, SPUtils.WIFI_DOWNLOAD_SWITCH, false);
                }
            }
        });
    }

    //初始化
    private void initView() {
        tb_auto_update = (ToggleButton) findViewById(R.id.tb_auto_update);
    }
}
