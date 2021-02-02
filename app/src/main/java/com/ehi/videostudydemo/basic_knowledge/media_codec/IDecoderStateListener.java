package com.ehi.videostudydemo.basic_knowledge.media_codec;

/**
 * @author: 37745 <a href="ziju.wang@1hai.cn">Contact me.</a>
 * @date: 2021/2/2 14:40
 * @desc:
 */
public interface IDecoderStateListener {

    void decoderRunning(IDecoder decoder);

    void decoderPrepare(IDecoder decoder);

    void decoderPause(IDecoder decoder);

    void decoderError(IDecoder decoder, String errorMsg);

    void decoderDestroy(IDecoder decoder);

    void decoderFinish(IDecoder decoder);
}
