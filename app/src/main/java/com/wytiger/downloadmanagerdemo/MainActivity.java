package com.wytiger.downloadmanagerdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;

import com.wytiger.downloader.download.DownloadUtil;
import com.wytiger.downloader.updater.ProgressListener;
import com.wytiger.downloader.updater.Updater;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void down(View v) {
        String apkUrl = "https://v.meituan.net/mobile/app/Android/group-1000060401_1-meituan.apk/meituan";
        String rootpath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
        if (!rootpath.endsWith("/")) {
            rootpath += "/";
        }
        String fullFilePath = rootpath + "meituan.apk";
        DownloadUtil.downloadFile(this, apkUrl, fullFilePath, null);
    }

    public void down2(View v) {
        String apkUrl = "https://v.meituan.net/mobile/app/Android/group-1000060401_1-meituan.apk/meituan";
        Updater updater = new Updater.Builder(this)
                .setDownloadUrl(apkUrl)
                .setApkFileName("meituan2.apk")
                .build();
        updater.addProgressListener(new ProgressListener() {
            @Override
            public void onProgressChange(long curBytes, long totalBytes, int progress) {
                Log.d("DownloadManager", "progress = " + progress);
            }
        });
        updater.start();
    }
}
