package com.obscuredPreferences;

import android.content.Context;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import java.io.UnsupportedEncodingException;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

public class ObscuredPrefs {
    protected static final String UTF8 = "UTF-8";
    private static char[] PEPPER = null;
    private static byte[] SALT = null;

    public static void init(Context context) {
        PEPPER = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID).toCharArray();
        try {
            SALT = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID).getBytes(UTF8);
        } catch (UnsupportedEncodingException e) { throw new RuntimeException(e); }
    }

    public static String encryptString( String value ) {
        try {
            final byte[] bytes = value!=null ? value.getBytes(UTF8) : new byte[0];
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
            SecretKey key = keyFactory.generateSecret(new PBEKeySpec(PEPPER));
            Cipher pbeCipher = Cipher.getInstance("PBEWithMD5AndDES");
            pbeCipher.init(Cipher.ENCRYPT_MODE, key, new PBEParameterSpec(SALT, 20));
            return new String(Base64.encode(pbeCipher.doFinal(bytes), Base64.NO_WRAP),UTF8);
        } catch( Exception e ) {
            throw new RuntimeException(e);
        }
    }

    public static String decryptString(String value){
        try {
            final byte[] bytes = value!=null ? Base64.decode(value,Base64.DEFAULT) : new byte[0];
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
            SecretKey key = keyFactory.generateSecret(new PBEKeySpec(PEPPER));
            Cipher pbeCipher = Cipher.getInstance("PBEWithMD5AndDES");
            pbeCipher.init(Cipher.DECRYPT_MODE, key, new PBEParameterSpec(SALT, 20));
            return new String(pbeCipher.doFinal(bytes),UTF8);
        } catch( Exception e) {
            Log.e("Util.decryptString", "Warning, could not decrypt the value; It may be stored in plaintext.  " + e.getMessage());
            return value;
        }
    }
}
