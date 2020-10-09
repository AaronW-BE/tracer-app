package vip.fastgo.tracer;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.amap.api.location.AMapLocation;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;
import vip.fastgo.tracer.api.ApiClient;
import vip.fastgo.tracer.service.TracerService;
import vip.fastgo.tracer.utils.LoggerUtil;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 1;

    private TracerService.LocalService mLocalService;
    private TracerService mTracerService;
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mLocalService = (TracerService.LocalService) service;
            mTracerService = mLocalService.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mLocalService = null;
            mTracerService = null;
        }
    };

    public TextView locateTextView;
    public TextView speedTextView;
    public Button button;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        checkPermission();

        Intent intent = new Intent(this, TracerService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        EventBus.getDefault().register(this);
    }

    private void initView() {
        locateTextView = findViewById(R.id.locateText);
        speedTextView = findViewById(R.id.speedText);

        button = findViewById(R.id.locateBtn);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTrace();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopTrace();
        EventBus.getDefault().unregister(this);
    }

    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
            }, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    finishAll();
                }
                break;
            default:
                break;
        }
    }

    public void startTrace() {
        locateTextView.setText("正在定位。。。");
        mTracerService.start();

        RequestBody form = new FormBody.Builder()
                .add("key", "9492cb21c64db39c6b403955332c7888")
                .add("name", "service_" + System.currentTimeMillis())
                .add("desc", "test_service")
                .build();
        ApiClient.getClient().post("https://tsapi.amap.com/v1/track/service/add", form, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {

            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                LoggerUtil.log("request ok");
                LoggerUtil.log(response.body().string());
            }
        });
    }

    public void finishAll() {
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    public void pauseTrace() {
        mTracerService.pause();
    }

    public void stopTrace() {
        unbindService(mConnection);
    }


    @Subscribe(threadMode = ThreadMode.MainThread)
    public void handleTraverMsg(AMapLocation location) {
        locateTextView.setText(location.getCity());
        speedTextView.setText(String.valueOf(location.getSpeed()));
    }
}