package me.relex.camerafilter;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import me.relex.camerafilter.camera.CameraRecordRenderer;
import me.relex.camerafilter.filter.FilterManager.FilterType;
import me.relex.camerafilter.video.EncoderConfig;
import me.relex.camerafilter.video.TextureMovieEncoder;
import me.relex.camerafilter.widget.CameraSurfaceView;

public class VideoRecordActivity extends AppCompatActivity implements View.OnClickListener {

    private final static int MSG_UPDATE_INFO = 1001;

    private CameraSurfaceView mCameraSurfaceView;
    private Button mRecordButton;
    private Spinner mSpinnerFilterType;
    private TextView mTvInfo;
    private boolean mIsRecordEnabled;
    private String mSaveFolder;
    private long mStartMsec;

    private Handler mHandler;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_record);
        mCameraSurfaceView = (CameraSurfaceView) findViewById(R.id.camera);
        mCameraSurfaceView.setAspectRatio(3, 4);

        findViewById(R.id.filter_normal).setOnClickListener(this);
        findViewById(R.id.view_clip).setOnClickListener(this);

        mSpinnerFilterType = (Spinner)findViewById(R.id.spinner_filter_type);
        ArrayAdapter<CharSequence> sourceAdapter = ArrayAdapter.createFromResource(
                this, R.array.filter_type_array, android.R.layout.simple_spinner_item);
        sourceAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        mSpinnerFilterType.setAdapter(sourceAdapter);
        mSpinnerFilterType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String name = parent.getItemAtPosition(position).toString();

/*
                <item>"普通"</item>
                <item>"叠加"</item>
                <item>"柔和叠加"</item>
                <item>"高斯模糊"</item>
                <item>"动态模糊"</item>
                <item>"色调曲线"</item>*/
                switch (position) {
                    case 0:
                    default:
                        mCameraSurfaceView.changeFilter(FilterType.Normal);
                        break;
                    case 1:
                        mCameraSurfaceView.changeFilter(FilterType.Blend);
                        break;
                    case 2:
                        mCameraSurfaceView.changeFilter(FilterType.SoftLight);
                        break;
                    case 3:
                        mCameraSurfaceView.changeFilter(FilterType.GaussianBlur);
                        break;
                    case 4:
                        mCameraSurfaceView.changeFilter(FilterType.MotionBlur);
                        break;
                    case 5:
                        mCameraSurfaceView.changeFilter(FilterType.ToneCurve);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mRecordButton = (Button) findViewById(R.id.record);
        mRecordButton.setOnClickListener(this);

        mTvInfo = (TextView) findViewById(R.id.tv_info);

        mIsRecordEnabled = TextureMovieEncoder.getInstance().isRecording();
        updateRecordButton();

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MSG_UPDATE_INFO:
                        long msec = System.currentTimeMillis() - mStartMsec;
                        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss SSS");
                        formatter.setTimeZone(TimeZone.getTimeZone("GMT+00:00")); // fix 08:00:01 322 bug
                        String str_time = formatter.format(msec);
                        mTvInfo.setText(str_time);

                        this.sendEmptyMessageDelayed(MSG_UPDATE_INFO, 100);
                        break;
                    default:
                        break;
                }
            }
        };

        mSaveFolder = Environment.getExternalStorageDirectory().getAbsolutePath() +
                "/test2/CameraFilter";
        File f = new File(mSaveFolder);
        if (!f.exists())
            f.mkdirs();
    }

    @Override protected void onResume() {
        super.onResume();
        mCameraSurfaceView.onResume();
        updateRecordButton();
    }

    @Override protected void onPause() {
        mCameraSurfaceView.onPause();
        mHandler.removeMessages(MSG_UPDATE_INFO);
        super.onPause();
    }

    @Override protected void onDestroy() {
        mCameraSurfaceView.onDestroy();
        super.onDestroy();
    }

    @Override public void onClick(View v) {
        switch (v.getId()) {
            case R.id.filter_normal:
                mCameraSurfaceView.changeFilter(FilterType.Normal);
                break;
            case R.id.record:
                if (!mIsRecordEnabled) {
                    mCameraSurfaceView.queueEvent(new Runnable() {
                        @Override public void run() {
                            CameraRecordRenderer renderer = mCameraSurfaceView.getRenderer();
                            renderer.setEncoderConfig(new EncoderConfig(new File(
                                    mSaveFolder,
                                    "video-" + System.currentTimeMillis() + ".mp4"), 480, 640,
                                    1024 * 1024 /* 1 Mb/s */));
                            mStartMsec = System.currentTimeMillis();
                            mHandler.sendEmptyMessage(MSG_UPDATE_INFO);
                        }
                    });
                }
                else {
                    mHandler.removeMessages(MSG_UPDATE_INFO);
                }
                mIsRecordEnabled = !mIsRecordEnabled;
                mCameraSurfaceView.queueEvent(new Runnable() {
                    @Override public void run() {
                        mCameraSurfaceView.getRenderer().setRecordingEnabled(mIsRecordEnabled);
                    }
                });
                updateRecordButton();
                break;
            case R.id.view_clip:
                startActivity(new Intent(this, PlayerActivity.class));
                break;
            default:
                break;
        }
    }

    public void updateRecordButton() {
        mRecordButton.setText(
                getString(mIsRecordEnabled ? R.string.record_stop : R.string.record_start));
    }
}
