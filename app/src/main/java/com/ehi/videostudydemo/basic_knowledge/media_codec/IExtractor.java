package com.ehi.videostudydemo.basic_knowledge.media_codec;

import android.media.MediaFormat;

import java.nio.ByteBuffer;

/**
 * @author: 37745 <a href="ziju.wang@1hai.cn">Contact me.</a>
 * @date: 2021/2/2 14:55
 * @desc: 音视频数据读取器
 */
public interface IExtractor {
    /**
     * 获取音视频格式参数
     */
    MediaFormat getFormat();

    /**
     * 读取音视频数据
     */
    int readBuffer(ByteBuffer byteBuffer);

    /**
     * 获取当前帧时间
     */
    long getCurrentTimeStamp();

    /**
     * Seek到指定位置，并返回实际帧的时间戳
     */
    long seek(long pos);

    void setStartPos(long pos);

    /**
     * 停止读取数据
     */
    void stop();
}
