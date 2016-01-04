package me.relex.camerafilter;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import me.relex.camerafilter.util.FileFilterTest;
import me.relex.camerafilter.util.LogUtil;
import me.relex.camerafilter.util.Util;

public class PlayerActivity extends AppCompatActivity implements SurfaceHolder.Callback {
    private final static String TAG = "DecodeH264Activity";
    private final static String mRootPath = Environment.getExternalStorageDirectory()
            .getAbsolutePath() + "/test2/CameraFilter";

    private RelativeLayout mLayout;
    private SurfaceView mSurfaceView;
    private SurfaceHolder mHolder;
    private TextView mTvInfo;
    private Button mBtnClean;
    private Button mBtnCapture;
    private ListView mLvFile;

    private MediaPlayer mPlayer;
    private MediaController mController;

    private SimpleAdapter mAdapter;
    private List<HashMap<String, Object>> mFilelist;

    private Thread mThread;
    private boolean mStopping = false;
    private String mDecFilePath;
    private int mWidth, mHeight;
    private int mDecFrameRate;
    private int mDecFrameCnt;
    private long mStartMsec;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        mLayout = (RelativeLayout) this.findViewById(R.id.layout);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int screen_width = dm.widthPixels;
        int screen_height = dm.heightPixels;
        LogUtil.info(TAG, String.format("Java: screen %dx%d", screen_width, screen_height));
        int preview_height = screen_width * 3 / 4;
        mLayout.getLayoutParams().height = preview_height;

        mSurfaceView = (SurfaceView) this.findViewById(R.id.movie_view2);
        mTvInfo = (TextView) this.findViewById(R.id.tv_info);
        mBtnClean = (Button) this.findViewById(R.id.btn_clean);
        mBtnCapture = (Button) this.findViewById(R.id.btn_capture);
        mLvFile = (ListView) this.findViewById(R.id.lv_filelist);

        mController = new MediaController(this);

        mHolder = mSurfaceView.getHolder();
        mHolder.setKeepScreenOn(true);
        mHolder.addCallback(this);

        mSurfaceView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View view, MotionEvent event) {
                // TODO Auto-generated method stub
                if (event.getAction() == MotionEvent.ACTION_DOWN && mPlayer != null) {
                    if (mController.isShowing())
                        mController.hide();
                    else
                        mController.show(3000);

                    return true;
                }

                return false;
            }
        });

        mBtnClean.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                for (int i=0;i<mFilelist.size();i++) {
                    String path = (String)mFilelist.get(i).get("filepath");
                    File f = new File(path);
                    f.delete();
                }

                mFilelist.clear();
                mAdapter.notifyDataSetChanged();
                Toast.makeText(PlayerActivity.this, "媒体文件已全部删除", Toast.LENGTH_SHORT).show();
            }
        });

        mBtnCapture.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

            }
        });

        mFilelist = new ArrayList<HashMap<String, Object>>();
        File file = new File(mRootPath);
        String[] exts = new String[]{"h264", "264", "ts", "mpegts", "mp4", "flv"};
        File[] files = file.listFiles(new FileFilterTest(exts));
        Arrays.sort(files, new FileComparator());

        int index = 1;
        for (File f : files) {
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("id", index++);
            map.put("filename", f.getName());
            map.put("filepath", f.getAbsolutePath());
            map.put("filesize", Util.getFileSize(f.length()));
            mFilelist.add(map);
        }

        mAdapter = new SimpleAdapter(
                this, mFilelist, R.layout.item, new String[]{"id", "filename", "filesize"},
                new int[]{R.id.id, R.id.filename, R.id.filesize});
        mLvFile.setAdapter(mAdapter);

        mLvFile.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View view,
                                    int position, long id) {
                // TODO Auto-generated method stub
                if (mPlayer != null) {
                    mController.hide();

                    mPlayer.stop();
                    mPlayer.release();
                    mPlayer = null;
                }

                stopThread();

                mDecFilePath = mRootPath + "/" + mFilelist.get(position).get("filename");
                setup_player(mDecFilePath);
            }
        });

        mLvFile.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View view,
                                           int position, long id) {
                // TODO Auto-generated method stub
                mDecFilePath = mRootPath + "/" + mFilelist.get(position).get("filename");
                File f = new File(mDecFilePath);
                f.delete();
                Toast.makeText(PlayerActivity.this, mDecFilePath + " 已删除", Toast.LENGTH_SHORT).show();

                mFilelist.remove(position);
                mAdapter.notifyDataSetChanged();

                return true;
            }
        });
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();

        if (mPlayer != null) {
            mController.hide();

            mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
        }

        stopThread();
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }

    private boolean setup_player(String url) {
        mPlayer = new MediaPlayer();
        mPlayer.setDisplay(mSurfaceView.getHolder());
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mPlayer.setScreenOnWhilePlaying(true);
        //mPlayer.setLooping(true);
        mPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {

            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                // TODO Auto-generated method stub
                Toast.makeText(PlayerActivity.this, String.format("播放器 错误 %d %d", what, extra),
                        Toast.LENGTH_SHORT).show();
                return true; // false will cause onComplete as sequence
            }
        });

        mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

            @Override
            public void onPrepared(MediaPlayer mp) {
                // TODO Auto-generated method stub

                mp.start();

                mController.setMediaPlayer(mPlayerControl);
                mController.setAnchorView(mSurfaceView);
                mController.show(3000);
            }
        });

        mPlayer.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {

            @Override
            public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
                // TODO Auto-generated method stub
                mSurfaceView.getHolder().setFixedSize(width, height);

                mWidth          = width;
                mHeight         = height;
                mDecFrameRate   = 25;
                mTvInfo.setText(String.format("解码 %d x %d @%d fps",
                        mWidth, mHeight, mDecFrameRate));
            }
        });

        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mp) {
                // TODO Auto-generated method stub
                mController.hide();
                Toast.makeText(PlayerActivity.this, "播放器 播放完成", Toast.LENGTH_SHORT).show();
             }
        });

        try {
            mPlayer.setDataSource(url);
            mPlayer.prepareAsync();
            return true;
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return false;
    }

    private void stopThread() {
        if (mThread != null) {
            LogUtil.info(TAG, "Java: set mStopping true");
            mStopping = true;
            try {
                LogUtil.info(TAG, "Java: before join");
                mThread.join();

            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            mStopping = false;
            mThread = null;
        }
    }

    private MediaController.MediaPlayerControl mPlayerControl = new MediaController.MediaPlayerControl() {

        @Override
        public boolean canPause() {
            // TODO Auto-generated method stub
            return true;
        }

        @Override
        public boolean canSeekBackward() {
            // TODO Auto-generated method stub
            return true;
        }

        @Override
        public boolean canSeekForward() {
            // TODO Auto-generated method stub
            return true;
        }

        @Override
        public int getAudioSessionId() {
            // TODO Auto-generated method stub
            if (mPlayer != null)
                return mPlayer.getAudioSessionId();

            return 0;
        }

        @Override
        public int getBufferPercentage() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public int getCurrentPosition() {
            // TODO Auto-generated method stub
            if (mPlayer != null)
                return mPlayer.getCurrentPosition();

            return 0;
        }

        @Override
        public int getDuration() {
            // TODO Auto-generated method stub
            if (mPlayer != null)
                return mPlayer.getDuration();

            return 0;
        }

        @Override
        public boolean isPlaying() {
            // TODO Auto-generated method stub
            if (mPlayer != null)
                return mPlayer.isPlaying();

            return false;
        }

        @Override
        public void pause() {
            // TODO Auto-generated method stub
            if (mPlayer != null)
                mPlayer.pause();
        }

        @Override
        public void seekTo(int msec) {
            // TODO Auto-generated method stub
            if (mPlayer != null)
                mPlayer.seekTo(msec);
        }

        @Override
        public void start() {
            // TODO Auto-generated method stub
            if (mPlayer != null)
                mPlayer.start();
        }

    };

    private class FileComparator implements Comparator<File> {
        @Override
        public int compare(File f1, File f2) {
            if (f1.isFile() && f2.isDirectory())
                return 1;
            if (f2.isFile() && f1.isDirectory())
                return -1;

            String s1=f1.getName().toString().toLowerCase();
            String s2=f2.getName().toString().toLowerCase();
            int ret = s1.compareTo(s2);
            return -ret;
        }
    }
}
