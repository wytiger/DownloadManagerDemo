package com.wytiger.downloader.download;

/**
 * desc:
 *
 * @author wuyong_cd
 * @date 2020/9/15 0015
 */
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
