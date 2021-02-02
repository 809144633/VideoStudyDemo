package com.ehi.videostudydemo.basic_knowledge.media_codec;

/**
 * @author: 37745 <a href="ziju.wang@1hai.cn">Contact me.</a>
 * @date: 2021/2/2 14:43
 * @desc: 解码状态
 */
public enum DecodeState {
    /**
     * 开始状态
     */
    START(0),
    /**
     * 解码中
     */
    DECODING(1),
    /**
     * 解码暂停
     */
    PAUSE(2),
    /**
     * 正在快进
     */
    SEEKING(3),
    /**
     * 解码完成
     */
    FINISH(4),
    /**
     * 解码器释放
     */
    STOP(5);

    private final int mState;

    DecodeState(int state) {
        mState = state;
    }

    public int getState() {
        return mState;
    }
}
