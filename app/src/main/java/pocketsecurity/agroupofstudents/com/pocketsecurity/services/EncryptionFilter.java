package pocketsecurity.agroupofstudents.com.pocketsecurity.services;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;

/**
 * Created by Daniel on 7/15/2019.
 */

public class EncryptionFilter {
    public static String encryptFile(File file) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
        String outputFilePath = file.getAbsolutePath().replace(".mp4", "_encrypted.mp4");

        Key key = KeyManagementSingleton.getInstance().getAESKey();
        Cipher aes = Cipher.getInstance("AES/ECB/PKCS5Padding");
        aes.init(Cipher.ENCRYPT_MODE, key);

        FileOutputStream fs = new FileOutputStream(outputFilePath);
        CipherOutputStream out = new CipherOutputStream(fs, aes);

        FileInputStream originalFIS = new FileInputStream(file);

        byte[] buffer = new byte[1024];
        int len;
        while ((len = originalFIS.read(buffer)) >= 0) {
            if(len > 0) {
                byte[] written = Arrays.copyOf(buffer, len);
                out.write(written);
            }
        }

        out.flush();
        out.close();

        return outputFilePath;
    }
}
