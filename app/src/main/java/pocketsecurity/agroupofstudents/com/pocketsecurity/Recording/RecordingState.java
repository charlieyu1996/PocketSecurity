package pocketsecurity.agroupofstudents.com.pocketsecurity.Recording;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Handler;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.FaceDetector;

import java.util.Calendar;
import java.util.Observable;
import java.util.Observer;

import pocketsecurity.agroupofstudents.com.pocketsecurity.HomeModel;

public class RecordingState implements Observer{
    private Handler.Callback mResumeVideoCallback;
    private Handler.Callback mPauseVideoCallback;

    private Context mContext;
    private FaceDetector mFaceDetector;
    HomeModel homeModel;
    /*A new photo to analyze is sent every 750 ms. Since we plan to stop recording after approx. 3 seconds
     * of not seeing a face, if we don't see a face for three consecutive photos, we will pause the
     * video recording*/
    int noFaceCounter = 0;

    int totalFaceCounted = 0;

    public RecordingState(Context context, Handler.Callback resumeVideoCallback, Handler.Callback pauseVideoCallback) {
        mContext = context;
        mResumeVideoCallback = resumeVideoCallback;
        mPauseVideoCallback = pauseVideoCallback;
        homeModel = HomeModel.getInstance();
        homeModel.addObserver(this);

        mFaceDetector = new FaceDetector.Builder(mContext)
                .setLandmarkType(FaceDetector.NO_LANDMARKS)
                .setTrackingEnabled(false)
                .setProminentFaceOnly(false)
                .setMode(FaceDetector.FAST_MODE)
                .build();
    }

    private Bitmap currentBitmap = null;

    public void analyzeNewPhoto(final Bitmap bitmap) {
        currentBitmap = bitmap;
        new MyTask().execute(null, null, null);
    }

    private class MyTask extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... param) {
                /*
                    Google's facial recognition API has an annoying flaw that we discovered. For a face
                    to be properly detected, the photo needs to have the face upright in the field of view
                    (i.e. like a "selfie") if the face is upside down or horizontal (as a result of how the
                    phone is placed to record) the API will say there is no face even if there is one.
                    Therefore our solution is to analyze the photo on all four 90 degree rotations.
                */
            boolean faceSeen = pipeToFacialRecognition(currentBitmap);

            if (faceSeen) {
                mResumeVideoCallback.handleMessage(null);
                noFaceCounter = 0;
                totalFaceCounted++;
            } else {
                noFaceCounter++;
            }

            if (noFaceCounter == 2) mPauseVideoCallback.handleMessage(null);

            return null;
        }

        protected void onProgressUpdate(String... progress) {}

        @Override
        protected void onPostExecute(String file_url) {}
    }

    private boolean pipeToFacialRecognition(Bitmap bitmap) {
        try {
            if (mFaceDetector.isOperational() && bitmap != null) {
                for (int rotate = 0; rotate < 360; rotate = rotate + 90) {
                    bitmap = CameraHelper.rotateVideo(bitmap, rotate);

                    Frame frame = new Frame.Builder().setBitmap(bitmap).build();
                    if (mFaceDetector.detect(frame).size() > 0) return true;
                }
            }
        } catch (Exception e) {}
        return false;
    }

    public void onDestroy() {
        mFaceDetector.release();

        // after the user stops recording, gather the current data, and update to the observer
        int year = Calendar.getInstance().get(Calendar.YEAR);
        int month = Calendar.getInstance().get(Calendar.MONTH);
        int day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        homeModel.addEntry(year, month, day, totalFaceCounted, true);
        homeModel.setFinishedRecording(true);
    }

    @Override
    public void update(Observable o, Object arg) {}

}
