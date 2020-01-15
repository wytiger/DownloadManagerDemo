package com.wytiger.downloader;

import android.Manifest;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.widget.Toast;


import java.io.File;
import java.util.ArrayList;

/**
 * 下载器
 * 参考：https://github.com/simplepeng/Updater
 */
public class Updater {
    private String apkFileName;
    private String apkFilePath;
    private String apkDirName;
    private String title;
    private String desc;
    private String downloadUrl;

    private Context context;
    private DownloadManager downloadManager;
    private long mTaskId;
    private boolean hideNotification = false;
    private boolean allowedOverRoaming = false;
    //    private ProgressListener mProgressListener;
    private DownloadReceiver downloadReceiver;
    private DownloadObserver downloadObserver;
    private boolean claerCache = false;

    private String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE};
    private static final int RC_SDCARD = 123;

    private DownloadFailedReceiver downloadFailedReceiver = new DownloadFailedReceiver();


    private Updater(Context context) {
        this.context = context;
    }


    /**
     * 开始下载
     *
     * @return
     */
    public void start() {
        download();
    }

    private void download() {
        if (context == null) {
            throw new NullPointerException("context must not be null");
        }
        if (TextUtils.isEmpty(downloadUrl)) {
            throw new NullPointerException("downloadUrl must not be null");
        }

        if (downloadManager == null) {
            downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        }

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(downloadUrl));
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
        request.setAllowedOverRoaming(allowedOverRoaming);
        request.setTitle(TextUtils.isEmpty(title) ? apkFileName : title);
        request.setDescription(TextUtils.isEmpty(desc) ? apkFileName : desc);
        request.setNotificationVisibility(hideNotification ? DownloadManager.Request.VISIBILITY_HIDDEN
                : DownloadManager.Request.VISIBILITY_VISIBLE);

        if (TextUtils.isEmpty(apkFileName)) {
            apkFileName = Utils.getFileNameForUrl(downloadUrl);
        }
        if (!apkFileName.endsWith(".apk")) {
            apkFileName += ".apk";
        }

        //设置下载路径
        if (TextUtils.isEmpty(apkFilePath) && TextUtils.isEmpty(apkDirName)) {
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, apkFileName);
        } else if (!TextUtils.isEmpty(apkDirName)) {
            request.setDestinationInExternalPublicDir(apkDirName, apkFileName);
        } else {
            String apkAbsPath = apkFilePath + File.separator + apkFileName;
            request.setDestinationUri(Uri.fromFile(new File(apkAbsPath)));
        }

        //将下载请求加入下载队列
        //加入下载队列后会给该任务返回一个long型的id，
        //通过该id可以取消任务，重启任务等等
        mTaskId = downloadManager.enqueue(request);
        if (downloadFailedReceiver != null) {
            context.registerReceiver(downloadFailedReceiver,
                    new IntentFilter(DownloadFailedReceiver.tag));
        }
    }

    /**
     * 注册下载完成的监听
     */
    public void registerDownloadReceiver() {
        if (downloadReceiver == null) {
            downloadReceiver = new DownloadReceiver();
        }
        context.registerReceiver(downloadReceiver,
                new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    /**
     * 解绑下载完成的监听
     */
    public void unRegisterDownloadReceiver() {
        if (downloadReceiver != null) {
            context.unregisterReceiver(downloadReceiver);
        }
    }

    private ArrayList<ProgressListener> listeners;

    /**
     * 添加下载进度回调
     */
    public void addProgressListener(ProgressListener progressListener) {
        if (listeners == null) {
            listeners = new ArrayList<>();
        }
        if (!listeners.contains(progressListener)) {
            listeners.add(progressListener);
        }
        if (downloadObserver == null && handler != null && downloadManager != null) {
            downloadObserver = new DownloadObserver(handler, downloadManager, mTaskId);
            context.getContentResolver().registerContentObserver(Uri.parse("content://downloads/"),
                    true, downloadObserver);
        }
    }

    /**
     * 移除下载进度回调
     */
    public void removeProgressListener(ProgressListener progressListener) {
        if (!listeners.contains(progressListener)) {
            throw new NullPointerException("this progressListener not attch Updater");
        }
        if (listeners != null && !listeners.isEmpty()) {
            listeners.remove(progressListener);
            if (listeners.isEmpty() && downloadObserver != null)
                context.getContentResolver().unregisterContentObserver(downloadObserver);
        }
    }


    public static void showToast(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            Bundle data = msg.getData();
            long cutBytes = data.getLong(DownloadObserver.CUR_BYTES);
            long totalBytes = data.getLong(DownloadObserver.TOTAL_BYTES);
            int progress = data.getInt(DownloadObserver.PROGRESS);
            if (listeners != null && !listeners.isEmpty()) {
                for (ProgressListener listener : listeners) {
                    listener.onProgressChange(cutBytes, totalBytes, progress);
                }
            }
            return false;
        }
    });

    public static class Builder {
        private Updater mUpdater;

        public Builder(Context context) {
            synchronized (Updater.class) {
                if (mUpdater == null) {
                    synchronized (Updater.class) {
                        mUpdater = new Updater(context);
                    }
                }
            }
        }

        /**
         * 设置下载下来的apk文件名
         *
         * @param apkName apk文件的名字
         * @return
         */
        public Builder setApkFileName(String apkName) {
            mUpdater.apkFileName = apkName;
            return this;
        }

        /**
         * 设置apk下载的路径
         *
         * @param apkPath 自定义的全路径
         * @return
         */
        public Builder setApkPath(String apkPath) {
            mUpdater.apkFilePath = apkPath;
            return this;
        }

        /**
         * 设置下载apk的文件目录
         *
         * @param dirName sd卡的文件夹名字
         * @return
         */
        public Builder setApkDir(String dirName) {
            mUpdater.apkDirName = dirName;
            return this;
        }

        /**
         * 设置下载的链接地址
         *
         * @param downloadUrl apk的下载链接
         * @return
         */
        public Builder setDownloadUrl(String downloadUrl) {
            mUpdater.downloadUrl = downloadUrl;
            return this;
        }

        /**
         * 通知栏显示的标题
         *
         * @param title 标题
         * @return
         */
        public Builder setNotificationTitle(String title) {
            mUpdater.title = title;
            return this;
        }
        /**
         * 通知栏显示的描述
         *
         * @param desc 描述
         * @return
         */
        public Builder setNotificationDesc(String desc) {
            mUpdater.desc = desc;
            return this;
        }

        /**
         * 隐藏通知栏
         *
         * @return
         */
        public Builder hideNotification() {
            mUpdater.hideNotification = true;
            return this;
        }

        /**
         * 是否为debug模式，会输出很多log信息（手动斜眼）
         *
         * @return
         */
        public Builder debug() {
            LogUtils.isDebug = true;
            return this;
        }

        /**
         * 允许漫游网络可下载
         *
         * @return
         */
        public Builder allowedOverRoaming() {
            mUpdater.allowedOverRoaming = true;
            return this;
        }


        public Builder clearCache() {
            mUpdater.claerCache = true;
            return this;
        }

        public Updater build() {
            return mUpdater;
        }

    }


    public class DownloadFailedReceiver extends BroadcastReceiver {
        public static final String tag = "DownloadFailedReceiver";

        @Override
        public void onReceive(Context context, Intent intent) {
            LogUtils.debug("开始重新下载");
            download();
        }
    }

}
