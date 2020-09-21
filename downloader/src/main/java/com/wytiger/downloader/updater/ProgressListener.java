package com.wytiger.downloader.updater;


public interface ProgressListener {
    void onProgressChange(long curBytes, long totalBytes, int progress);
}
