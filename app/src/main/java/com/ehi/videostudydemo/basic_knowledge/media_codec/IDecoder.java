package com.ehi.videostudydemo.basic_knowledge.media_codec;

import android.media.MediaFormat;

/**
 * @author: 37745 <a href="ziju.wang@1hai.cn">Contact me.</a>
 * @date: 2021/2/2 14:37
 * @desc: 解码器，继承Runnable，作为任务使用
 */
public interface IDecoder extends Runnable {
    /**
     * 暂停解码
     */
    void pause();

    /**
     * 继续解码
     */
    void resume();

    /**
     * 停止解码
     */
    void stop();

    /**
     * 是否正在解码
     */
    boolean isDecoding();

    /**
     * 是否正在快进
     */
    boolean isSeeking();

    /**
     * 是否停止解码
     */
    boolean isStop();

    /**
     * 设置状态监听器
     */
    void setStateListener(IDecoderStateListener listener);

    /**
     * 获取视频宽
     */
    int getWidth();

    /**
     * 获取视频高
     */
    int getHeight();

    /**
     * 获取视频长度
     */
    long getDuration();

    /**
     * 获取视频旋转角度
     */
    int getRotationAngle();

    /**
     * 获取音视频对应的格式参数
     */
    MediaFormat getMediaFormat();

    /**
     * 获取音视频对应的媒体轨道
     */
    int getTrack();

    /**
     * 获取解码的文件路径
     */
    String getFilePath();
}
