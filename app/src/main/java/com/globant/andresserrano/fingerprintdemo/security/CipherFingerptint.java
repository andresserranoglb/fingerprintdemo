package com.globant.andresserrano.fingerprintdemo.security;


import android.os.Build;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.support.annotation.RequiresApi;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

public class CipherFingerptint {


    /**
     * Initialize the {@link Cipher} instance with the created key in the
     * {@link #createKey(String, boolean)} method.
     *
     * @param keyName the key name to init the cipher
     * @return {@code true} if initialization is successful, {@code false} if the lock screen has
     * been disabled or reset after the key was generated, or if a fingerprint got enrolled after
     * the key was generated.
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public static boolean initCipher(KeyStore keyStore, Cipher cipher, String keyName) {
        try {
            keyStore.load( null );
            SecretKey key = (SecretKey) keyStore.getKey( keyName, null );
            cipher.init( Cipher.ENCRYPT_MODE, key );
            return true;
        } catch (KeyPermanentlyInvalidatedException e) {
            return false;
        } catch (KeyStoreException | CertificateException | UnrecoverableKeyException | IOException
                | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException( "Failed to init Cipher", e );
        }
    }

    public static Cipher getDefaultCipher() {
        Cipher defaultCipher;
        try {
            defaultCipher = Cipher.getInstance( KeyProperties.KEY_ALGORITHM_AES + "/"
                    + KeyProperties.BLOCK_MODE_CBC + "/"
                    + KeyProperties.ENCRYPTION_PADDING_PKCS7 );
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new RuntimeException( "Failed to get an instance of Cipher", e );
        }
        return defaultCipher;
    }

}
