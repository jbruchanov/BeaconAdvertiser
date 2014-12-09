package com.scurab.beaconadvertiser;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.easibeacon.protocol.IBeacon;
import com.easibeacon.protocol.IBeaconListener;
import com.easibeacon.protocol.IBeaconProtocol;


/**
 * Created by jiri.bruchanov on 09/12/2014.
 */
public class ScannerService extends Service implements IBeaconListener {

    private static final String TAG = "ScannerService";
    private static final int RESTART_TIME = 30000;//30s
    private IBeaconProtocol mIBeaconProto;
    private RetryHandler mRetryHandler;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
        mRetryHandler = new RetryHandler();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new ScannerServiceBinder(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        onStartImpl();
        return Service.START_STICKY;
    }

    private void onStartImpl() {
        mIBeaconProto = IBeaconProtocol.getInstance(this);
        IBeaconProtocol.configureBluetoothAdapter(this);
        mIBeaconProto.setListener(this);
        onStartScan();
    }

    protected void onStartScan() {
        if (mIBeaconProto.isScanning()) {
            mIBeaconProto.stopScan();
            Log.d(TAG, "StopScan");
        }
        mIBeaconProto.reset();
        mIBeaconProto.startScan();
        Log.d(TAG, "StartScan");
    }

    @Override
    public void enterRegion(IBeacon ibeacon) {
        Log.d(TAG, "enterRegion:" + ibeacon.getUuidHexStringDashed());
    }

    @Override
    public void exitRegion(IBeacon ibeacon) {
        Log.d(TAG, "exitRegion:" + ibeacon.getUuidHexStringDashed());
    }

    @Override
    public void beaconFound(IBeacon ibeacon) {
        Log.d(TAG, "beaconFound:" + ibeacon.getUuidHexStringDashed() + " Major: " + ibeacon.getMajor() + " Minor: " + ibeacon.getMinor() + " Distance: " + ibeacon.getProximity() + "m.");
    }

    @Override
    public void searchState(int state) {
        String stateName;
        switch(state){
            case IBeaconProtocol.SEARCH_STARTED:
                stateName = "SEARCH_STARTED";
                break;
            case IBeaconProtocol.SEARCH_END_EMPTY:
                stateName = "SEARCH_END_EMPTY";
                break;
            case IBeaconProtocol.SEARCH_END_SUCCESS:
                stateName = "SEARCH_END_SUCCESS";
                break;
            default:
                stateName = "UNKNOWN";
                break;
        }

        Log.d(TAG, "searchState:" + stateName);
        if (IBeaconProtocol.SEARCH_STARTED != state) {
            mRetryHandler.postRetry();
        }
    }

    @Override
    public void operationError(int status) {
        Log.d(TAG, "operationError:" + status);
        mRetryHandler.postRetry();
    }

    public static class ScannerServiceBinder extends Binder {

        private ScannerService mService;

        public ScannerServiceBinder(ScannerService mService) {
            this.mService = mService;
        }

        public ScannerService getService() {
            return mService;
        }
    }

    private class RetryHandler extends Handler{
        private static final int MSG = 1;
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (MSG == msg.what) {
                onStartScan();
            }
        }

        public void postRetry() {
            sendEmptyMessageDelayed(MSG, RESTART_TIME);
            Log.d(TAG, String.format("Retry posted in %s s", RESTART_TIME / 1000));
        }
    }
}
