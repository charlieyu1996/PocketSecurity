package pocketsecurity.agroupofstudents.com.pocketsecurity.Recording;

import java.util.Calendar;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.app.TimePickerDialog;

import pocketsecurity.agroupofstudents.com.pocketsecurity.R;

public class LaunchRecordingActivity extends AppCompatActivity {
    boolean useBackCamera = true;

    Button mRecordButton = null;
    Button mTimeButton = null;
    Context mContext = null;
    ImageView backOfPhoneImageView = null;
    ImageView frontOfPhoneImageView = null;
    TextView mTextView = null;
    TextView mTimeTextView = null;

    private static final int MEDIA_RECORDER_REQUEST = 0;
    private final String[] requiredPermissions = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA,
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recording);

        setTitle("Configure recording");

        mRecordButton = findViewById(R.id.recordButton);
        mTimeButton = findViewById(R.id.timeButton);
        backOfPhoneImageView = findViewById(R.id.backOfPhone);
        frontOfPhoneImageView = findViewById(R.id.frontOfPhone);
        mTextView = findViewById(R.id.cameraTextView);
        mContext = this.getApplicationContext();

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                useBackCamera = !useBackCamera;
                if(useBackCamera) {
                    mTextView.setText(R.string.will_use_back_camera);
                    backOfPhoneImageView.setVisibility(View.VISIBLE);
                    frontOfPhoneImageView.setVisibility(View.GONE);
                }
                else {
                    mTextView.setText(R.string.will_use_front_camera);
                    backOfPhoneImageView.setVisibility(View.GONE);
                    frontOfPhoneImageView.setVisibility(View.VISIBLE);
                }
            }
        };

        backOfPhoneImageView.setOnClickListener(onClickListener);
        frontOfPhoneImageView.setOnClickListener(onClickListener);

        mRecordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (areCameraPermissionGranted()) {
                    Intent intent = new Intent(mContext, RecordingActivity.class);
                    intent.putExtra("useBackCamera", useBackCamera);
                    startActivity(intent);
                    finish();
                } else {
                    requestCameraPermissions();
                }
            }
        });

        mTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar c = Calendar.getInstance();
                int hour = c.get(Calendar.HOUR_OF_DAY);
                int minute = c.get(Calendar.MINUTE);

                TimePickerDialog timePickerDialog = new TimePickerDialog(LaunchRecordingActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        c.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        c.set(Calendar.MINUTE, minute);
                        c.set(Calendar.SECOND, 0);
                        if (c.before(Calendar.getInstance())) {
                            c.add(Calendar.DATE, 1);
                        }
                        long pickedTimeInMillis = c.getTimeInMillis();
                        Calendar c2 = Calendar.getInstance();
                        long timeDifference = pickedTimeInMillis - c2.getTimeInMillis();
                        String str1 = "";
                        String str2 = ":";
                        if (hourOfDay<10) {
                            str1 = "0";
                        }
                        if (minute<10) {
                            str2 = ":0";
                        }

                        mTimeTextView.setText(str1 + hourOfDay + str2 + minute);

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (areCameraPermissionGranted()) {
                                    Intent intent = new Intent(mContext, RecordingActivity.class);
                                    intent.putExtra("useBackCamera", useBackCamera);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    requestCameraPermissions();
                                }
                            }
                        }, timeDifference);
                    }
                }, hour, minute, false);
                timePickerDialog.show();
            }
        });

        if (!areCameraPermissionGranted()) {
            requestCameraPermissions();
        }
    }

    private boolean areCameraPermissionGranted() {

        for (String permission : requiredPermissions) {
            if (!(ActivityCompat.checkSelfPermission(this, permission) ==
                    PackageManager.PERMISSION_GRANTED)) {
                return false;
            }
        }
        if(!Settings.canDrawOverlays(this)) return false;
        return true;
    }

    private void requestCameraPermissions() {
        ActivityCompat.requestPermissions(
                this,
                requiredPermissions,
                MEDIA_RECORDER_REQUEST);

        if (!Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, 0);
        }
    }
}
