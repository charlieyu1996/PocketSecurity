package pocketsecurity.agroupofstudents.com.pocketsecurity.services;

import android.content.Context;
import android.util.Log;

import org.spongycastle.crypto.PBEParametersGenerator;
import org.spongycastle.crypto.digests.SHA256Digest;
import org.spongycastle.crypto.generators.PKCS5S2ParametersGenerator;
import org.spongycastle.crypto.params.KeyParameter;

import java.security.SecureRandom;
import java.util.UUID;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import static org.spongycastle.util.encoders.Hex.encode;

/**
 * Created by Daniel on 6/24/2019.
 */

public class SecurityServices {
    private static final int ITERATIONS = 250000;

    public static void configureUserPassword(String password, Context context) {
        UUID uuid = UUID.randomUUID();
        String salt = uuid.toString();

        Preferences preferences = new Preferences(context);
        preferences.setPasswordSalt(salt);

        String hashedPassword = hashPassword(password.toCharArray(), salt.getBytes());
        preferences.setHashedPassword(hashedPassword);

        setupDeviceKey(context);

        preferences.setIsAppPasswordProtected(true);
    }

    public static void disableUserPasswordProtection(Context context) {
        Preferences preferences = new Preferences(context);
        preferences.setIsAppPasswordProtected(false);
        preferences.setHashedPassword(null);
        preferences.setPasswordSalt(null);
    }

    public static boolean isUserValid(String password, Context context) {
        Preferences preferences = new Preferences(context);
        String salt = preferences.getPasswordSalt();
        String storedHashedPassword = preferences.getHashedPassword();

        String calculatedHashedPassword = hashPassword(password.toCharArray(), salt.getBytes());

        if (storedHashedPassword.equals(calculatedHashedPassword)) {
            KeyManagementSingleton.getInstance().DeriveAesKey(context);
            return true;
        }
        return false;
    }

    private static String hashPassword(char[] password, byte[] salt) {
        PKCS5S2ParametersGenerator generator = new PKCS5S2ParametersGenerator(new SHA256Digest());
        generator.init(PBEParametersGenerator.PKCS5PasswordToUTF8Bytes(password), salt, ITERATIONS);
        return new String(encode(((KeyParameter) generator.generateDerivedParameters(256)).getKey()));
    }

    private static void setupDeviceKey(Context context) {
        try {
            Preferences preferences = new Preferences(context);
            String deviceKey = preferences.getDeviceKey();

            if (deviceKey == null) {
                deviceKey = generateRandomString(16);
                preferences.setDeviceKey(deviceKey);
            }

            byte[] deviceKeyAESKeyBytes = deviceKey.getBytes("UTF-8");
            SecretKey deviceKeyAESKey = new SecretKeySpec(deviceKeyAESKeyBytes, "AES");

            KeyManagementSingleton.getInstance().UpdateAesKey(deviceKeyAESKeyBytes, deviceKeyAESKey);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String generateRandomString(int length) {
        String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        SecureRandom rnd = new SecureRandom();
        StringBuilder sb = new StringBuilder( length );
        for( int i = 0; i < length; i++ )
            sb.append( AB.charAt( rnd.nextInt(AB.length()) ) );
        return sb.toString();
    }
}
