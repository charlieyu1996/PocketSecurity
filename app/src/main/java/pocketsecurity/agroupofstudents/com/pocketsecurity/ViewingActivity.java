package pocketsecurity.agroupofstudents.com.pocketsecurity;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.MediaController;
import android.widget.VideoView;

import java.io.File;

import pocketsecurity.agroupofstudents.com.pocketsecurity.services.DecryptionFilter;

public class ViewingActivity extends AppCompatActivity {
    private static final int PICKFILE_RESULT_CODE = 8778;
    private VideoView dataDrain = null;
    File mDecryptedFile = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewing);
        setTitle("Video gallery");

        dataDrain = findViewById(R.id.playbackVideoView);
        Intent pipeForVideoFromDevice = new Intent(Intent.ACTION_GET_CONTENT);
        pipeForVideoFromDevice.setType("*/*");
        startActivityForResult(pipeForVideoFromDevice, PICKFILE_RESULT_CODE);
    }

    Intent mPipedData = null;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent pipedData) {
        mPipedData = pipedData;
        dataDrain.setMediaController(new MediaController(this));
        new DecryptFileTask().execute(null, null, null);
    }

    private class DecryptFileTask extends AsyncTask<String, String, String> {
        Uri videoURI = mPipedData.getData();

        @Override
        protected String doInBackground(String... strings) {
            try {
                String realFilePath = getRealPathFromURI(videoURI);

                if (realFilePath.contains("_encrypted")) {
                    File realFile = new File(realFilePath);
                    String filePath = DecryptionFilter.decryptFile(realFile);
                    mDecryptedFile = new File(filePath);
                    videoURI = Uri.fromFile(mDecryptedFile);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(String result) {
            dataDrain.setVideoURI(videoURI);
            dataDrain.requestFocus();
            dataDrain.start();
        }
    }

    public String getRealPathFromURI(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "PocketSecurity_Recordings");

        return mediaStorageDir.getPath() + File.separator + result;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(mDecryptedFile != null) {
            if(mDecryptedFile.exists()) {
                mDecryptedFile.delete();
            }
        }
    }

}
