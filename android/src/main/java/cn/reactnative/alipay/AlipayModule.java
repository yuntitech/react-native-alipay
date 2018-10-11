package cn.reactnative.alipay;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.alipay.sdk.app.PayTask;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.CallbackImpl;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.PromiseImpl;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * Created by tdzl2003 on 3/31/16.
 */
public class AlipayModule extends ReactContextBaseJavaModule {

    private static final int SDK_PAY_FLAG = 1;
    private Promise mPromise;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SDK_PAY_FLAG: {
                    @SuppressWarnings("unchecked")
                    PayResult payResult = new PayResult((Map<String, String>) msg.obj);
                    /**
                     对于支付结果，请商户依赖服务端的异步通知结果。同步通知结果，仅作为支付结束的通知。
                     */
                    String resultInfo = payResult.getResult();// 同步返回需要验证的信息
                    String resultStatus = payResult.getResultStatus();
                    WritableMap result = Arguments.createMap();
                    result.putString("resultStatus", resultStatus);
                    // 判断resultStatus 为9000则代表支付成功
                    resolve(result);
                    break;
                }
                default:
                    break;
            }
        }

    };


    public AlipayModule(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Override
    public String getName() {
        return "RCTAlipay";
    }

    @Override
    public void initialize() {
    }

    @Override
    public void onCatalystInstanceDestroy() {
    }

    @ReactMethod
    public void pay(final String orderInfo, final boolean showLoading, Promise promise) {
        this.mPromise = promise;
        Runnable payRunnable = new Runnable() {

            @Override
            public void run() {
                Activity activity = getCurrentActivity();
                if (activity != null) {
                    PayTask alipay = new PayTask(activity);
                    Map<String, String> result = alipay.payV2(orderInfo, showLoading);

                    Message msg = new Message();
                    msg.what = SDK_PAY_FLAG;
                    msg.obj = result;
                    mHandler.sendMessage(msg);
                }
            }
        };
        // 必须异步调用
        Thread payThread = new Thread(payRunnable);
        payThread.start();
    }

    private void resolve(WritableMap result) {
        if (canInvoke("mResolve")) {
            mPromise.resolve(result);
        }
    }

    private boolean canInvoke(String fieldStr) {
        if (mPromise == null) {
            return false;
        }
        try {
            Field field;
            if (mPromise instanceof PromiseImpl) {
                field = PromiseImpl.class.getDeclaredField(fieldStr);
                field.setAccessible(true);
                Object callback = field.get(mPromise);
                if (callback instanceof CallbackImpl) {
                    field = CallbackImpl.class.getDeclaredField("mInvoked");
                    field.setAccessible(true);
                    return !(boolean) field.get(callback);
                }
            }
        } catch (Exception e) {
            //ignore
            e.printStackTrace();
        }
        return true;
    }
}
