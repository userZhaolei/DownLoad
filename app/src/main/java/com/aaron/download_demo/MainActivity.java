package com.aaron.download_demo;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.aaron.download_demo.service.DownloadService;
import com.aaron.download_demo.utils.NetworkTypeUtils;
import com.aaron.download_demo.utils.SPUtils;
import com.aaron.download_demo.utils.VersionUtils;

/**
 * 作者：哇牛Aaron
 * 作者简书文章地址: http://www.jianshu.com/users/07a8b5386866/latest_articles
 * 时间: 2016/11/18
 * 功能描述: 主页面
 * 判断当前apk版本与服务器apk版本是否一致，不一致则弹出对话框，提示用户下载最新版本apk。
 * 如果wifi下自动更新开关处于关闭状态，用户点击更新，则启动服务下载apk。并在前台显示进度，下载完成后自动安装。
 * 如果wifi下自动更新开关处于打开状态，则在后台服务默认下载，下载完成后自动安装。
 * 如果用户点击忽略此版本，则当前版本将不再提示用户更新。
 */


public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private Button btn_wifi;
    private Boolean isCheck = false;

    private static final int FILE_PERMISSIONS_REQUEST_CODE = 1003;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initListener();
        progressVersion();
    }

    /**
     * 说明:
     * 比较服务器版本与当前apk版本,如果低于服务器版本
     * 1.wifi下自动更新开关打开 启动服务,后台静默下载apk,下载完毕后自动弹出安装界面
     * 2.wifi下自动更新开关关闭 启动服务,下载apk,并用notification通知栏显示下载进度等,下载完毕后自动弹出安装界面
     * --
     * 目前我没有线程的接口,去比较服务的版本,所以写一个假的服务器版本 用到的朋友自行显示比较即可
     */
    private void progressVersion() {
        //VersionUtils.getVersionCode(this)工具类里获取当前安装的apk版本号
        int version = VersionUtils.compareVersion(String.valueOf(VersionUtils.getVersionCode(this)),
                "1.2.0");//这里 1.2.0使我们伪造的 你完全可以得到自己服务器接口里的版本号 然后进行比对

        /**
         * 比较版本大小 version1为当前所安装的版本
         * version1 < version2  则  返回 -1
         * version1 > version2  则  返回 1
         * version1 == version2 则 返回  0
         */
        if (version == -1) {
            //判断 用户是否进入app主页面
            Intent intent = new Intent();
            intent.setClassName("com.aaron.download_demo", "MainActivity");

            if (intent.resolveActivity(getPackageManager()) == null) {
                Log.e(TAG, "不存在MainActivity");
                // 说明系统MainActivity没有被打开
                return;
            } else {
                Log.e(TAG, "存在MainActivity");

                /**
                 * wifi状态下自动下载
                 */
                if ((boolean) SPUtils.get(this, SPUtils.WIFI_DOWNLOAD_SWITCH, false)
                        && NetworkTypeUtils.getCurrentNetType(MainActivity.this).equals("wifi")) {

                    startService(new Intent(MainActivity.this, DownloadService.class));
                    //startService(new Intent(MainActivity.this, DownloadService2.class));
                    //Log.e("TAG", "startService");
                } else { //提示dialog

                    //判断 忽略的版本sp信息是否与当前版本相等 如果不相等 则显示更新的dialog
                    String spVersion = (String) SPUtils.get(this, SPUtils.APK_VERSION, "");
                    if (!spVersion.equals("1.2.0")) {//服务器版本 依旧填假数据 1.2.0

                        //下面是自定义dialog
                        View view = View.inflate(this, R.layout.download_layout, null);
                        final Dialog dialog = new AlertDialog.Builder(this).create();
                        dialog.show();

                        dialog.setContentView(view);
                        TextView content = (TextView) view.findViewById(R.id.tv_content);
                        content.setText("解决不能及时..." + "等其它版本信息");
                        //取消
                        TextView cancel = (TextView) view.findViewById(R.id.btn_cancel);
                        cancel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                //当true时 保存版本信息
                                if (isCheck) {
                                    SPUtils.put(MainActivity.this, SPUtils.APK_VERSION, "1.2.0");
                                }

                                //Log.e("TAG","isCheck == " + isCheck);

                                dialog.dismiss();
                            }
                        });

                        //确定
                        TextView Sure = (TextView) view.findViewById(R.id.btn_ok);
                        Sure.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                autoObtainCameraPermission();
                                //startService(new Intent(MainActivity.this, DownloadService2.class));
                                //当true时 保存版本信息
                                if (isCheck) {
                                    SPUtils.put(MainActivity.this, SPUtils.APK_VERSION, "1.2.0");
                                }
                                dialog.dismiss();
                            }
                        });

                        //忽略该版本
                        CheckBox checkBox = (CheckBox) view.findViewById(R.id.cb_ignore);
                        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                if (isChecked) {
                                    isCheck = true;
                                } else {
                                    isCheck = false;
                                }
                            }
                        });
                    }
                }
            }
        }
    }

    private void initListener() {
        btn_wifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SettingActivity.class));
            }
        });
    }

    private void initView() {
        btn_wifi = (Button) findViewById(R.id.btn_wifi);
    }

    private void autoObtainCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, FILE_PERMISSIONS_REQUEST_CODE);
        } else {//有权限直接调用系统相机拍照
            startService(new Intent(MainActivity.this, DownloadService.class));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case FILE_PERMISSIONS_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startService(new Intent(MainActivity.this, DownloadService.class));
                } else {
                    Toast.makeText(this, "没有文件下载的权限", Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }

    private void getAppDetailSettingIntent(Context context) {
        Intent localIntent = new Intent();
        localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= 9) {
            localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
            localIntent.setData(Uri.fromParts("package", getPackageName(), null));
        } else if (Build.VERSION.SDK_INT <= 8) {
            localIntent.setAction(Intent.ACTION_VIEW);
            localIntent.setClassName("com.android.settings","com.android.settings.InstalledAppDetails");
            localIntent.putExtra("com.android.settings.ApplicationPkgName", getPackageName());
        }
        startActivity(localIntent);
    }

}
