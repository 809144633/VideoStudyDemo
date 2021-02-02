package com.ehi.videostudydemo.basic_knowledge.media_codec;

import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.text.TextUtils;
import android.util.Log;

import com.ehi.videostudydemo.utils.FileUtils;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @author: 37745 <a href="ziju.wang@1hai.cn">Contact me.</a>
 * @date: 2021/2/2 14:44
 * @desc: 基础解码器
 */
public abstract class BaseDecoder implements IDecoder {
    private static final String TAG = "BaseDecoder";
    //-------------线程相关------------------------
    /**
     * 解码器是否在运行
     */
    private boolean mIsRunning = true;
    /**
     * 线程等待锁
     */
    private final Object mLock = new Object();
    /**
     * 是否可以进入解码
     */
    private boolean mReadyForDecode = false;
    //---------------解码相关-----------------------
    /**
     * 音视频解码器
     */
    protected MediaCodec mCodec;
    /**
     * 音视频数据读取器
     */
    protected IExtractor mExtractor;
    /**
     * 解码输入缓存区
     */
    protected ByteBuffer[] mInputBuffers;
    /**
     * 解码输出缓存区
     */
    protected ByteBuffer[] mOutputBuffers;
    /**
     * 解码数据信息
     */
    private MediaCodec.BufferInfo mBufferInfo = new MediaCodec.BufferInfo();

    private DecodeState mState = DecodeState.STOP;

    private IDecoderStateListener mStateListener;
    /**
     * 流数据是否结束
     */
    private boolean mIsEOS = false;

    protected int mVideoWidth = 0;

    protected int mVideoHeight = 0;

    private long mMediaDuration;

    private long mEndPos;

    private String mFilePath;

    private final Context mContext;

    public BaseDecoder(Context context) {
        mContext = context;
    }

    @Override
    public void run() {
        mState = DecodeState.START;
        if (mStateListener != null) {
            mStateListener.decoderPrepare(this);
        }
        if (!init()) {
            return;
        }
        while (mIsRunning) {
            if (mState != DecodeState.START
                    && mState != DecodeState.DECODING
                    && mState != DecodeState.SEEKING) {
                waitDecode();
            }
            if (!mIsRunning || mState == DecodeState.STOP) {
                mIsRunning = false;
                break;
            }
            if (!mIsEOS) {
                mIsEOS = pushBufferToDecoder();
            }
            final int index = pullBufferFromDecoder();
            if (index >= 0) {
                if (mOutputBuffers != null) {
                    render(mOutputBuffers[index], mBufferInfo);
                }
                if (mCodec != null) {
                    mCodec.releaseOutputBuffer(index, true);
                }
                if (mState == DecodeState.START) {
                    mState = DecodeState.PAUSE;
                }
            }
            if (mBufferInfo != null && mBufferInfo.flags == MediaCodec.BUFFER_FLAG_END_OF_STREAM) {
                mState = DecodeState.FINISH;
                if (mStateListener != null) {
                    mStateListener.decoderFinish(this);
                }
            }
        }
        doneDecode();
        release();
    }

    private boolean pushBufferToDecoder() {
        if (mCodec == null) {
            return false;
        }
        int inputBufferIndex = mCodec.dequeueInputBuffer(2000);
        boolean isEndOfStream = false;
        if (inputBufferIndex >= 0) {
            ByteBuffer inputBuffer = null;
            if (mInputBuffers != null) {
                inputBuffer = mInputBuffers[inputBufferIndex];
            }
            if (inputBuffer != null && mExtractor != null) {
                int sampleSize = mExtractor.readBuffer(inputBuffer);
                if (sampleSize < 0) {
                    mCodec.queueInputBuffer(inputBufferIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                    isEndOfStream = true;
                } else {
                    mCodec.queueInputBuffer(inputBufferIndex, 0, sampleSize, mExtractor.getCurrentTimeStamp(), 0);
                }
            }
        }
        return isEndOfStream;
    }

    private int pullBufferFromDecoder() {
        if (mCodec == null || mBufferInfo == null) {
            return -1;
        }
        int index = mCodec.dequeueOutputBuffer(mBufferInfo, 1000);
        switch (index) {
            //输出格式改变了
            case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                //没有可用数据，等会再来
            case MediaCodec.INFO_TRY_AGAIN_LATER:
                break;
            //输入缓冲改变了
            case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
                mOutputBuffers = mCodec.getOutputBuffers();
                break;
            default:
                return index;
        }
        return -1;
    }

    private boolean init() {
        if (TextUtils.isEmpty(mFilePath) || !FileUtils.isFileExist(mContext, mFilePath)) {
            Log.w(TAG, "文件路径为空");
            if (mStateListener != null) {
                mStateListener.decoderError(this, "文件路径为空");
            }
            return false;
        }
        if (!check()) {
            return false;
        }
        mExtractor = initExtractor(mFilePath);
        if (mExtractor == null || mExtractor.getFormat() == null) {
            return false;
        }
        return initParams() && initRender() && initCodec();
    }

    private boolean initCodec() {
        if (mExtractor != null && mExtractor.getFormat() != null) {
            final MediaFormat mediaFormat = mExtractor.getFormat();
            final String mimeType = mediaFormat.getString(MediaFormat.KEY_MIME);
            try {
                mCodec = MediaCodec.createDecoderByType(mimeType);
                if (!configCodec(mCodec, mediaFormat)) {
                    waitDecode();
                }
                if (mCodec != null) {
                    mCodec.start();
                    mInputBuffers = mCodec.getInputBuffers();
                    mOutputBuffers = mCodec.getOutputBuffers();
                }
            } catch (IOException e) {
                return false;
            }
            return true;
        }
        return false;
    }

    private boolean initParams() {
        if (mExtractor != null && mExtractor.getFormat() != null) {
            final MediaFormat format = mExtractor.getFormat();
            mMediaDuration = format.getLong(MediaFormat.KEY_DURATION) / 1000L;
            if (mEndPos == 0L) {
                mEndPos = mMediaDuration;
            }
            initSpecParams(format);
            return true;
        }
        return false;
    }

    /**
     * 解码线程进入等待
     */
    protected void waitDecode() {
        try {
            if (mState == DecodeState.PAUSE) {
                if (mStateListener != null) {
                    mStateListener.decoderPause(this);
                }
            }
            synchronized (mLock) {
                mLock.wait();
            }
        } catch (InterruptedException ex) {
            //ignore
        }
    }

    /**
     * 通知解码线程继续运行
     */
    protected void notifyDecode() {
        synchronized (mLock) {
            mLock.notifyAll();
        }
        if (mState == DecodeState.DECODING) {
            if (mStateListener != null) {
                mStateListener.decoderRunning(this);
            }
        }
    }

    protected void release() {
        mState = DecodeState.STOP;
        mIsEOS = false;
        if (mExtractor != null) {
            mExtractor.stop();
        }
        if (mCodec != null) {
            mCodec.stop();
            mCodec.release();
        }
        if (mStateListener != null) {
            mStateListener.decoderDestroy(this);
        }
    }

    /**
     * 结束编码
     */
    protected abstract void doneDecode();

    /**
     * 初始化渲染器
     */
    protected abstract boolean initRender();

    /**
     * 渲染
     *
     * @param outputBuffer
     * @param bufferInfo
     */
    protected abstract void render(ByteBuffer outputBuffer, MediaCodec.BufferInfo bufferInfo);

    /**
     * 初始化子类自己特有的参数
     *
     * @param format
     */
    protected abstract void initSpecParams(MediaFormat format);

    /**
     * 初始化数据提取器
     *
     * @param filePath
     * @return
     */
    protected abstract IExtractor initExtractor(String filePath);

    /**
     * 配置解码器
     */
    protected abstract boolean configCodec(MediaCodec codec, MediaFormat mediaFormat);

    /**
     * 检查子类参数
     */
    protected abstract boolean check();

    public void setFilePath(String filePath) {
        mFilePath = filePath;
    }
}
