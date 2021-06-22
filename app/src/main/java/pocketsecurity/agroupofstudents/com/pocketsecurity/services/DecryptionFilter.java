package pocketsecurity.agroupofstudents.com.pocketsecurity.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.NoSuchPaddingException;

/**
 * Created by Daniel on 7/15/2019.
 */

public class DecryptionFilter {

    public static String decryptFile(File file) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
        String outputDecryptedFilePath = file.getAbsolutePath().replace("_encrypted.mp4", "_decrypted.mp4");

        Key key = KeyManagementSingleton.getInstance().getAESKey();
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, key);

        FileInputStream fileInputStream = new FileInputStream(file);
        CipherInputStream cipherInputStream = new CipherInputStream(fileInputStream, cipher);

        FileOutputStream fileOutputStream = new FileOutputStream(outputDecryptedFilePath);

        byte[] b = new byte[1024];
        int numberOfBytedRead;
        while ((numberOfBytedRead = cipherInputStream.read(b)) >= 0) {
            byte[] written = Arrays.copyOf(b, numberOfBytedRead);
            fileOutputStream.write(written);
        }

        return outputDecryptedFilePath;
    }
}
