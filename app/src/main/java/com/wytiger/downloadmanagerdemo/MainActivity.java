package com.wytiger.downloadmanagerdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DownloadManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;

import com.wytiger.downloader.ProgressListener;
import com.wytiger.downloader.Updater;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void down(View v) {
        String apkUrl = "https://v.meituan.net/mobile/app/Android/group-1000060401_1-meituan.apk/meituan";
        DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(apkUrl));
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "meituan.apk");
//        request.setTitle("meituan");
//        request.setDescription("meituan desc");
//        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
        request.setMimeType("application/vnd.android.package-archive")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        long downloadId = downloadManager.enqueue(request);
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
