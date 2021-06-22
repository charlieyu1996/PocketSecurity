package pocketsecurity.agroupofstudents.com.pocketsecurity.services;

import android.app.IntentService;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.TextureView;
import android.view.WindowManager;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.util.List;

import pocketsecurity.agroupofstudents.com.pocketsecurity.Recording.CameraHelper;
import pocketsecurity.agroupofstudents.com.pocketsecurity.Recording.RecordingState;

/**
 * Created by Daniel on 7/15/2019.
 */

public class BackgroundVideoService extends Service implements TextureView.SurfaceTextureListener{
    private WindowManager windowManager;
    private static final String TAG = "RecorderService";

    private Camera mCamera = null;
    private TextureView mPreview;
    private MediaRecorder mMediaRecorder = null;
    private FileDescriptor outputFileDescriptor = null;
    private File mOutputFile;
    private boolean mUseBackCamera = true;
    boolean mMediaRecorderIsRecording = false;
    private RecordingState mRecordingState;

    private boolean isSurfaceCreated = false;

    int counter = 0;

    @Override
    public void onCreate() {
        windowManager = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        mPreview = new TextureView(this);


        int LAYOUT_FLAG;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        }

        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(
                640, 480,
                LAYOUT_FLAG,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );
        layoutParams.gravity = Gravity.BOTTOM | Gravity.RIGHT;
        windowManager.addView(mPreview, layoutParams);
        mPreview.setSurfaceTextureListener(this);

        mRecordingState = new RecordingState(this.getApplicationContext(), ResumeVideoPumpCallback, PauseVideoPumpCallback);

        final Handler threadHandler = new Handler();
        final Camera.PictureCallback pipeFromAndroidCameraToFacialDetection = new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(final byte[] data, Camera camera) {

                //Get photo data from pipe and re-orient it accordingly
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                mRecordingState.analyzeNewPhoto(bitmap);
            }
        };

        final Runnable takePhotoRunnable = new Runnable() {
            @Override
            public void run() {
                if (isSurfaceCreated) {
                    counter++;
                    try {
                        mCamera.takePicture(null, null, pipeFromAndroidCameraToFacialDetection);
                    } catch (Exception e) {}
                }

                if (isSurfaceCreated) {
                    threadHandler.postDelayed(this, 500);
                }
            }
        };


        threadHandler.postDelayed(takePhotoRunnable, 500);
    }

    Handler.Callback ResumeVideoPumpCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            try {
                resumeVideoPump();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
            return false;
        }
    };

    Handler.Callback PauseVideoPumpCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            try {
                stopVideoPump();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
            return false;
        }
    };

    protected void resumeVideoPump() {
        try {
            if (!mMediaRecorderIsRecording) mMediaRecorder.resume();
            mMediaRecorderIsRecording = true;
        } catch (Exception e) {}
    }

    protected void stopVideoPump() {
        try {
            if (mMediaRecorderIsRecording) mMediaRecorder.pause();
            mMediaRecorderIsRecording = false;
        } catch (Exception e) {}
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "Binding a client");
        return mBinder;
    }

    private LocalBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        public BackgroundVideoService getService() {
            return BackgroundVideoService.this;
        }
    }

    public void startRecord(FileDescriptor out, File outputFile, boolean useBackCamera)
    {
        mOutputFile = outputFile;
        mUseBackCamera = useBackCamera;
        outputFileDescriptor = out;
        if (isSurfaceCreated)
            new MediaPrepareTask().execute(null, null, null);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
        isSurfaceCreated = true;
        if (outputFileDescriptor != null)
            new MediaPrepareTask().execute(null, null, null);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

    }

    @Override
    public void onDestroy() {
        if (mMediaRecorder != null) {
            try{
                mMediaRecorder.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
            releaseMediaRecorder();
            outputFileDescriptor = null;
        }

        if (mCamera != null) {
            mCamera.lock();
            releaseCamera();
        }

        windowManager.removeView(mPreview);
        isSurfaceCreated = false;
        mRecordingState.onDestroy();

        if(KeyManagementSingleton.getInstance().isEncryptionEnabled())
            new EncryptFileTask().execute(null, null, null);
    }


    private class EncryptFileTask extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... strings) {
            try {
                String encryptedFile = EncryptionFilter.encryptFile(mOutputFile);
                File file = new File(encryptedFile);

                if(!file.exists()) throw new Exception();

                if(mOutputFile.exists()) mOutputFile.delete();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    /*
        Prepares the Camera as a "Pump" for the video feed.
    */
    private boolean prepareCameraPump() {
        if (mUseBackCamera) mCamera = CameraHelper.getDefaultBackFacingCameraInstance();
        else mCamera = CameraHelper.getDefaultFrontFacingCameraInstance();

        mCamera.setDisplayOrientation(90);

        Camera.Parameters parameters = mCamera.getParameters();

        List<Camera.Size> mSupportedPreviewSizes = parameters.getSupportedPreviewSizes();
        List<Camera.Size> mSupportedVideoSizes = parameters.getSupportedVideoSizes();
        Camera.Size optimalSize = CameraHelper.getOptimalVideoSize(mSupportedVideoSizes,
                mSupportedPreviewSizes, mPreview.getWidth(), mPreview.getHeight());

        CamcorderProfile profile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
        profile.videoFrameWidth = optimalSize.width;
        profile.videoFrameHeight = optimalSize.height;

        parameters.setPreviewSize(profile.videoFrameWidth, profile.videoFrameHeight);
        mCamera.setParameters(parameters);

        try {
            mCamera.setPreviewTexture(mPreview.getSurfaceTexture());
        } catch (IOException e) {
            e.printStackTrace();
        }

        mMediaRecorder = new MediaRecorder();

        mCamera.unlock();

        mMediaRecorder.setCamera(mCamera);
        if(mUseBackCamera) mMediaRecorder.setOrientationHint(90);
        else mMediaRecorder.setOrientationHint(270);

        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        mMediaRecorder.setProfile(profile);

        if (mOutputFile == null) {
            return false;
        }
        mMediaRecorder.setOutputFile(outputFileDescriptor);

        try {
            mMediaRecorder.prepare();
        } catch (IllegalStateException e) {
            Log.d(TAG, "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            Log.d(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        }
        return true;
    }

    class MediaPrepareTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... voids) {
            if (prepareCameraPump()) {
                try {
                    mMediaRecorder.start();
                    mMediaRecorder.pause();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                releaseMediaRecorder();
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {

        }
    }

    private void releaseMediaRecorder(){
        if (mMediaRecorder != null) {
            mMediaRecorder.reset();
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
    }

    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }
}
