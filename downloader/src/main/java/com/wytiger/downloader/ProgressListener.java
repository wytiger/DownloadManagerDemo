package com.wytiger.downloader;


public interface ProgressListener {
    void onProgressChange(long curBytes, long totalBytes, int progress);
}
