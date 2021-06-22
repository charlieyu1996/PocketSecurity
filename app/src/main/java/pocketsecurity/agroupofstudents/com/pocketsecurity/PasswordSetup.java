package pocketsecurity.agroupofstudents.com.pocketsecurity;

import android.app.Activity;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import pocketsecurity.agroupofstudents.com.pocketsecurity.services.KeyManagementSingleton;
import pocketsecurity.agroupofstudents.com.pocketsecurity.services.Preferences;
import pocketsecurity.agroupofstudents.com.pocketsecurity.services.SecurityServices;

import static android.view.View.GONE;

public class PasswordSetup extends AppCompatActivity {
    private Context mContext = null;
    private Activity mActivity = null;
    private ProgressBar mProgressBar;
    private LinearLayout mPasswordSetupWrapper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_setup);
        setTitle("Configure password");

        mContext = this.getApplicationContext();
        mActivity = this;
        mProgressBar = findViewById(R.id.password_setup_progress);
        mPasswordSetupWrapper = findViewById(R.id.passwordSetupWrapper);

        LinearLayout linearLayout = findViewById(R.id.disablePasswordProtectionWrapper);
        Preferences preferences = new Preferences(mContext);
        if(!preferences.getIsAppPasswordProtected()) linearLayout.setVisibility(GONE);

        Button disablePasswordButton = findViewById(R.id.disablePasswordProtection);
        disablePasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SecurityServices.disableUserPasswordProtection(mContext);
                Toast.makeText(mContext, "Password protection disabled!", Toast.LENGTH_LONG).show();
                KeyManagementSingleton.getInstance().DisableEncryption();
                finish();
            }
        });

        Button setPasswordButton = findViewById(R.id.setPasswordButton);
        setPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                passwordSetupInProgress();
                EditText firstPassword = findViewById(R.id.initialPassword);
                EditText secondPassword = findViewById(R.id.confirmPassword);

                String firstPasswordText = firstPassword.getText().toString();
                String secondPasswordText = secondPassword.getText().toString();

                if(firstPasswordText.equals("")){
                    firstPassword.setError("Password cannot be empty!");
                    passwordSetupFailed();
                } else if(firstPasswordText.equals(secondPasswordText)) {
                    SecurityServices.configureUserPassword(firstPasswordText, mContext);
                    mActivity.finish();
                } else {
                    Toast.makeText(mContext, "Password's do not match!", Toast.LENGTH_SHORT).show();
                    firstPassword.setText("");
                    secondPassword.setText("");
                    passwordSetupFailed();
                }
            }
        });
    }

    private void passwordSetupInProgress() {
        mProgressBar.setVisibility(View.VISIBLE);
        mPasswordSetupWrapper.setVisibility(View.GONE);
    }

    private void passwordSetupFailed() {
        mProgressBar.setVisibility(View.GONE);
        mPasswordSetupWrapper.setVisibility(View.VISIBLE);
    }
}
