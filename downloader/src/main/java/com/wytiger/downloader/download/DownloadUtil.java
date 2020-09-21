package com.wytiger.downloader.download;

import android.app.DownloadManager;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;


import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * desc:
 *
 * @author wuyong_cd
 * @date 2020/9/15 0015
 */
public class DownloadUtil {
    public interface DownloadListener {
        /**
         * 开始
         */
        void onStart();

        /**
         * 进度更新
         *
         * @param curBytes   已下载字节数
         * @param totalBytes 总字节数
         * @param progress   进度
         */
        void onProgressChange(long curBytes, long totalBytes, int progress);

        /**
         * 完成
         */
        void onComplete();
    }

    /**
     * 下载文件
     *
     * @param context      上下文
     * @param url          文件url
     * @param fullFilePath 文件存放全路径
     */
    public static long downloadFile(Context context, String url, String fullFilePath, DownloadListener listener) {
        final DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setDestinationUri(Uri.fromFile(new File(fullFilePath)));
        String name = fullFilePath;
        if (fullFilePath.contains("/")) {
            name = fullFilePath.substring(fullFilePath.lastIndexOf("/") + 1);
        }
        request.setTitle(name);
        request.setDescription(name + "正在下载...");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
        //加入下载队列
        final long downloadId = downloadManager.enqueue(request);
        //查询下载进度
        query(downloadManager, downloadId, listener);

        return downloadId;
    }

    private static void query(final DownloadManager downloadManager, final long downloadId, final DownloadListener listener) {
        ThreadUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (listener != null) {
                    listener.onStart();
                }
            }
        });

        //定时查询下载进度
        final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                doQuery(downloadManager, downloadId, listener, executor);
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    private static void doQuery(DownloadManager downloadManager, long Id, final DownloadListener listener, ScheduledExecutorService executor) {
        DownloadManager.Query downloadQuery = new DownloadManager.Query();
        downloadQuery.setFilterById(Id);
        Cursor cursor = downloadManager.query(downloadQuery);
        if (cursor != null && cursor.moveToFirst()) {
            int totalSizeBytesIndex = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES);
            int bytesDownloadSoFarIndex = cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR);

            // 下载的文件总大小
            final int totalSizeBytes = cursor.getInt(totalSizeBytesIndex);
            // 截止目前已经下载的文件总大小
            final int bytesDownloadSoFar = cursor.getInt(bytesDownloadSoFarIndex);
            //下载进度
            ThreadUtil.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    int progress = bytesDownloadSoFar / totalSizeBytes;
                    if (listener != null) {
                        listener.onProgressChange(bytesDownloadSoFar, totalSizeBytes, progress);
                    }
                }
            });

            //下载完毕，关闭
            if (bytesDownloadSoFar >= totalSizeBytes) {
                ThreadUtil.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (listener != null) {
                            listener.onComplete();
                        }
                    }
                });
                cursor.close();
                executor.shutdown();
            }
        }
    }

    /**
     * 取消下载
     *
     * @param context    上下文
     * @param downloadId 下载id
     */
    public static void cancelDownload(Context context, long downloadId) {
        final DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        downloadManager.remove(downloadId);
    }
}
