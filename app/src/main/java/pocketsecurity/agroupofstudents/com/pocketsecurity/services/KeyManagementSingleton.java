package pocketsecurity.agroupofstudents.com.pocketsecurity.services;

import android.content.Context;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by Daniel on 7/15/2019.
 */

public class KeyManagementSingleton {
    private static volatile KeyManagementSingleton sSoleInstance = new KeyManagementSingleton();
    private SecretKey mAESKey = null;
    private byte[] mAESKeyBytes = null;

    private KeyManagementSingleton() {}

    public static KeyManagementSingleton getInstance() {
        return sSoleInstance;
    }

    public byte[] getAESKeyBytes() {
        return mAESKeyBytes;
    }

    public SecretKey getAESKey() {
        return mAESKey;
    }

    public boolean isEncryptionEnabled() {
        return mAESKey != null;
    }

    public void UpdateAesKey(byte[] keyBytes, SecretKey newKey) {
        mAESKeyBytes = keyBytes;
        mAESKey = newKey;
    }

    public void DeriveAesKey(Context context) {
        try {
            Preferences preferences = new Preferences(context);
            String deviceKey = preferences.getDeviceKey();

            if(deviceKey != null) {
                mAESKeyBytes = deviceKey.getBytes("UTF-8");
                mAESKey = new SecretKeySpec(mAESKeyBytes, "AES");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void DisableEncryption() {
        mAESKeyBytes = null;
        mAESKey = null;
    }
}
