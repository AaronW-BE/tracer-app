package vip.fastgo.tracer.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;

import de.greenrobot.event.EventBus;

import static vip.fastgo.tracer.utils.LoggerUtil.log;

public class TracerService extends Service implements ITracer {

    private LocalService mBinder = new LocalService();

    public AMapLocationClient aMapLocationClient = null;
    public AMapLocationClientOption aMapLocationClientOption = null;
    public AMapLocationListener mLocationListener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation aMapLocation) {
            if (aMapLocation != null) {
                if (aMapLocation.getErrorCode() == 0) {
                    int type = aMapLocation.getLocationType();
                    String province = aMapLocation.getProvince();
                    String city = aMapLocation.getCity();
                    double latitude = aMapLocation.getLatitude();
                    double longitude = aMapLocation.getLongitude();
                    log("type", String.valueOf(type));
                    log("province: " + province + "city: " + city);
                    log("坐标: " + latitude + "," + longitude);

                    EventBus.getDefault().post(aMapLocation);
                } else {
                    log("location error: ", aMapLocation.getErrorInfo());
                    log("error code: " + aMapLocation.getErrorCode());
                }
            } else {
                log("定位无数据");
            }
        }
    };

    public class LocalService extends Binder {
        public TracerService getService() {
            return TracerService.this;
        }
    }

    public TracerService() {
    }

    private void initLocation() {
        aMapLocationClient = new AMapLocationClient(getApplicationContext());
//设置定位回调监听
        aMapLocationClient.setLocationListener(mLocationListener);

        aMapLocationClientOption = new AMapLocationClientOption();
        aMapLocationClientOption.setLocationPurpose(AMapLocationClientOption.AMapLocationPurpose.SignIn);
        aMapLocationClientOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        aMapLocationClientOption.setLocationCacheEnable(false);
        aMapLocationClientOption.setInterval(1000);

        aMapLocationClient.setLocationOption(aMapLocationClientOption);
        log("初始化定位完成");
    }

    @Override
    public IBinder onBind(Intent intent) {
        initLocation();
        return mBinder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void start() {
        log("开始定位");
        aMapLocationClient.stopLocation();
        aMapLocationClient.startLocation();
    }

    @Override
    public void stop() {
        if (aMapLocationClient != null) {
            aMapLocationClient.stopLocation();
            aMapLocationClient = null;
            mLocationListener = null;
        }
    }

    @Override
    public void pause() {
        if (aMapLocationClient != null) {
            aMapLocationClient.stopLocation();
        }
    }
}
