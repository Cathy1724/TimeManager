package com.tm.timemanager.Service;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.tm.timemanager.dao.DBOpenHelperdao;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by zhai on 2016/4/19.
 */
public class Lookservice extends Service {
    private ActivityManager ams;
    private String runpackagename = "com.android.zhai";
    private ActivityManager.RunningAppProcessInfo runningAppProcessInfo;
    private List<ActivityManager.RunningAppProcessInfo> runningServices;
    private String beforpackagename = "1";
    private long starttime;
    private int runningtime;
    private int forheadtime;
    private DateFormat dateFormatday;
    private String yearmouthday;
    private String todayhours;
    private PackageManager packageManager;
    private Drawable icon;
    private ApplicationInfo applicationInfo;
    private String appname;
    private DBOpenHelperdao dao;
    private SimpleDateFormat hourmin;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        ams = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        dateFormatday = new SimpleDateFormat("yyyyMMdd");
        hourmin = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //得到数据库的操作助手
        dao = new DBOpenHelperdao(getApplication());
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    //发送广播
                   /* Intent intent = new Intent();
                    intent.setAction("com.tm.timemanager.changeinfo");*/
                    //得到一个运行APP的管理者
                    runningServices = ams.getRunningAppProcesses();
                    //得到一个运行包的管理者
                    packageManager = getApplication().getPackageManager();
                    //得到最近刚打开的应用
                    runningAppProcessInfo = runningServices.get(0);
                    //得到它的名字
                    runpackagename = runningAppProcessInfo.processName;
//                    Log.i("哈哈",runpackagename+"--"+beforpackagename);

                    //如果正在运行的现在将要运行的不是同一个就进来  runningname正在运行   packagename马上要打开
                    if (!beforpackagename.equals(runpackagename)&&!beforpackagename.equals("1")) {

//                        Log.i("哈哈1",runpackagename+"--"+beforpackagename);
                        //得到这个名字的信息  从这里面拿icon appname
                        try {
                            applicationInfo = packageManager.getApplicationInfo(beforpackagename, 0);
                            icon = packageManager.getApplicationIcon(beforpackagename);

                        } catch (PackageManager.NameNotFoundException e) {
                            e.printStackTrace();
                        }


                        appname = beforpackagename;                      //防止没有appname
                        appname = (String)applicationInfo.loadLabel(packageManager);

//                    Log.i("哈哈", packagename +"---"+appname +"----" + runningtime + "---" + starttime + "---" + yearmouthday + "---" + todayhours);

                        //如果runningname不为空的话就进来
                        if (!beforpackagename.equals("1")) {
                            Log.i("哈哈", beforpackagename + "--" + appname + "--"+"应用运行时间："+runningtime+"--"+forheadtime + "--"+"开始时间："+ hourmin.format(starttime) + "---" + yearmouthday + "---" + todayhours);
                            //如果包名不是系统的应用  而且时间不为0的时候 就记录下来
                            if (!beforpackagename.startsWith("com.android.")&&starttime!=0) {
//                                forheadtime=runningtime;//将当前的时间赋给之前的时间  因为当打开其它应用时才能拿到之前的应用的执行的时间aaa
                                Log.i("哈哈数据库", beforpackagename + "--" + appname + "--"+"应用运行时间："+runningtime + "--"+"开始时间："+ hourmin.format(starttime) + "---" + yearmouthday + "---" + todayhours);
                                dao.insertBlackNumber(yearmouthday,beforpackagename,appname,starttime,runningtime,1);
                            }
                        }
                        starttime = new Date().getTime();               //开始的时间
                        yearmouthday =dateFormatday.format(starttime);//得到这个时间的日期
                        todayhours = String.valueOf(new Date().getHours());//得到这个时间的所属小时
                        //如果应用是系统的应用就不计时
                        if (!runpackagename.startsWith("com.android.")) {
                            runningtime = 0;
                        }
                        beforpackagename = runpackagename;
                    }else {
                        beforpackagename = runpackagename;
                    }



                    //如果正在运行的和将要运行的是同一个就计时
                    if (beforpackagename.equals(runpackagename)) {

                        runningtime = runningtime + 1;
                    }

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
        return super.onStartCommand(intent, flags, startId);
    }


}
