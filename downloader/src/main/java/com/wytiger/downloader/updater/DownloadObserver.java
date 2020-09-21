package com.wytiger.downloader.updater;

import android.app.DownloadManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class DownloadObserver extends ContentObserver {
    private DownloadManager mDownloadManager;
    private long mTaskId;
    private Handler mHandler;
    private Bundle bundle = new Bundle();
    private Message message;
    private DownloadManager.Query query;
    private Cursor cursor;

    public static final String CUR_BYTES = "curBytes";
    public static final String TOTAL_BYTES = "totalBytes";
    public static final String PROGRESS = "progress";

    /**
     * Creates a content observer.
     *
     * @param handler The handler to run {@link #onChange} on, or null if none.
     */
    public DownloadObserver(Handler handler, DownloadManager downloadManager, long taskId) {
        super(handler);
        this.mHandler = handler;
        this.mDownloadManager = downloadManager;
        this.mTaskId = taskId;
        query = new DownloadManager.Query().setFilterById(mTaskId);
    }

    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);
        try {
            cursor = mDownloadManager.query(query);
            if (cursor == null) {
                return;
            }
            cursor.moveToFirst();
            long curBytes = cursor
                    .getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
            long totalBytes = cursor
                    .getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
            int mProgress = (int) ((curBytes * 100) / totalBytes);
            if (totalBytes != 0) {
                LogUtils.debug("curBytes==" + curBytes);
                LogUtils.debug("totalBytes==" + totalBytes);
                LogUtils.debug("mProgress------->" + mProgress);
                message = mHandler.obtainMessage();
                bundle.putLong(CUR_BYTES, curBytes);
                bundle.putLong(TOTAL_BYTES, totalBytes);
                bundle.putInt(PROGRESS, mProgress);
                message.setData(bundle);
                mHandler.sendMessage(message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
}
