package com.threetree;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.os.RemoteException;
import android.telephony.TelephonyManager;

import com.android.internal.telephony.IEmulatorCheck;
import com.snail.antifake.deviceid.emulator.EmuCheckUtil;
import com.snail.antifake.jni.EmulatorCheckService;

import java.util.List;

/**
 * EmulatorCheckerSdk
 */

public final class EmulatorCheckerSdk {

    private static Context mContext;
    private static CheckerCallback mCheckerCallback;
    private static boolean EmuCheckUtilResult = false;
    private static boolean BinderCheckResult = false;
    private static boolean SensorCheckResult = false;
    private static boolean IsPhoneCheckResult = false;


    public static synchronized void checkIsEmulator(Context context, CheckerCallback checkerCallback){
        mContext = context;
        mCheckerCallback = checkerCallback;

        EmuCheckUtilResult = EmuCheckUtil.mayOnEmulator(mContext);
        SensorCheckResult = sensorCheck(mContext);
        IsPhoneCheckResult = !isPhone(mContext);

        Intent intent = new Intent(mContext, EmulatorCheckService.class);
        mContext.bindService(intent, serviceConnection, Service.BIND_AUTO_CREATE);

    }

    /**
     *
     * @return true is emulator ,false is not emulator
     * */
    private static boolean sensorCheck(Context context){
        //检测是否同时具备光感sensor和距离sensor,模拟器一般没有, TYPE_PROXIMITY TYPE_LIGHT
        boolean has_light_sensor = false;
        boolean has_proximity_sensor = false;
        SensorManager sm = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);  //获取系统的传感器服务并创建实例
        List<Sensor> list = sm.getSensorList(Sensor.TYPE_ALL);  //获取传感器的集合
        for (Sensor sensor:list){
            if (sensor.getType() == Sensor.TYPE_LIGHT) {
                has_light_sensor = true;
            } else if (sensor.getType() == Sensor.TYPE_PROXIMITY) {
                has_proximity_sensor = true;
            }
        }
        return !(has_light_sensor || has_proximity_sensor);
    }

    final static ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            IEmulatorCheck IEmulatorCheck = com.android.internal.telephony.IEmulatorCheck.Stub.asInterface(service);
            if (IEmulatorCheck != null) {
                try {
                    BinderCheckResult = IEmulatorCheck.isEmulator();
                    mCheckerCallback.onCheckDone(checkResult());
                    mContext.unbindService(this);
                } catch (RemoteException e) {
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    /**
     * 判断设备是否是手机
     *
     * @return {@code true}: 是<br>{@code false}: 否
     */
    public static boolean isPhone(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return tm != null && tm.getPhoneType() != TelephonyManager.PHONE_TYPE_NONE;
    }

    private static boolean checkResult(){
        if (SensorCheckResult) {
            return ((EmuCheckUtilResult || BinderCheckResult || IsPhoneCheckResult) && SensorCheckResult);
        } else {
            return ((EmuCheckUtilResult || BinderCheckResult || IsPhoneCheckResult) || SensorCheckResult);
        }
    }

    /**
     * Callback passed to the function.
     */
    public interface CheckerCallback {
        /**
         * Called when the result has been workout.
         *
         * @param result  true is emulator ,false is not emulator
         */
        void onCheckDone(boolean result);

    }
}
