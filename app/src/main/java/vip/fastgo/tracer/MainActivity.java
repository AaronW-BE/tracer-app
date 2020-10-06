package vip.fastgo.tracer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.amap.api.location.AMapLocation;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;
import vip.fastgo.tracer.service.TracerService;
import vip.fastgo.tracer.utils.LoggerUtil;

public class MainActivity extends AppCompatActivity {

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

    public void startTrace() {
        locateTextView.setText("正在定位。。。");
        mTracerService.start();
    }

    public void pauseTrace() {
        mTracerService.pause();
    }

    public void stopTrace() {
        unbindService(mConnection);
    }


    @Subscribe(threadMode = ThreadMode.MainThread)
    public void handleTraverMsg(AMapLocation location) {
        LoggerUtil.log("event bus message got");
        locateTextView.setText(location.getCity());
        speedTextView.setText(String.valueOf(location.getSpeed()));
    }
}