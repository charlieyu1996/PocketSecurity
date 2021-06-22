package pocketsecurity.agroupofstudents.com.pocketsecurity.Recording;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;

import java.io.File;
import java.io.FileNotFoundException;

import pocketsecurity.agroupofstudents.com.pocketsecurity.R;
import pocketsecurity.agroupofstudents.com.pocketsecurity.services.BackgroundVideoService;

public class RecordingActivity extends Activity{
    private BackgroundVideoService mService = null;
    Uri outputFileUri = null;
    File mOutputFile = null;
    private boolean mBound;

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            Intent intent = getIntent();
            boolean useBackCamera = intent.getBooleanExtra("useBackCamera", true);

            BackgroundVideoService.LocalBinder binder = (BackgroundVideoService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
            try {
                mService.startRecord(
                        getContentResolver().openFileDescriptor(outputFileUri, "w").getFileDescriptor(), mOutputFile, useBackCamera);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recording_layout);

        File outputFile = CameraHelper.getOutputMediaFile();

        mOutputFile = outputFile;
        outputFileUri = Uri.fromFile(outputFile);
        Intent bgVideoServiceIntent = new Intent(this, BackgroundVideoService.class);
        startService(bgVideoServiceIntent);
        bindService(bgVideoServiceIntent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        onStopClick(null);
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }

    public void onStopClick(View view) {
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
        stopService(new Intent(this, BackgroundVideoService.class));
        if(view != null) finish();
    }
}