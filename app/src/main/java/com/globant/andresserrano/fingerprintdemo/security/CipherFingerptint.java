package com.globant.andresserrano.fingerprintdemo.security;


import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.support.annotation.RequiresApi;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

public class CipherFingerptint {

    private KeyStore keyStore;
    private KeyGenerator keyGenerator;

    @RequiresApi(api = Build.VERSION_CODES.M)
    public CipherFingerptint() {
        try {
            keyStore = KeyStore.getInstance( "AndroidKeyStore" );
        } catch (KeyStoreException e) {
            throw new RuntimeException( "Failed to get an instance of KeyStore", e );
        }
        try {
            keyGenerator = KeyGenerator
                    .getInstance( KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore" );
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            throw new RuntimeException( "Failed to get an instance of KeyGenerator", e );
        }
    }
    /**
     * Creates a symmetric key in the Android Key Store which can only be used after the user has
     * authenticated with fingerprint.
     *
     * @param keyName                          the name of the key to be created
     * @param invalidatedByBiometricEnrollment if {@code false} is passed, the created key will not
     *                                         be invalidated even if a new fingerprint is enrolled.
     *                                         The default value is {@code true}, so passing
     *                                         {@code true} doesn't change the behavior
     *                                         (the key will be invalidated if a new fingerprint is
     *                                         enrolled.). Note that this parameter is only valid if
     *                                         the app works on Android N developer preview.
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void createKey(String keyName, boolean invalidatedByBiometricEnrollment) {
        try {
            keyStore.load( null );
            KeyGenParameterSpec.Builder builder = new KeyGenParameterSpec.Builder( keyName,
                    KeyProperties.PURPOSE_ENCRYPT |
                            KeyProperties.PURPOSE_DECRYPT )
                    .setBlockModes( KeyProperties.BLOCK_MODE_CBC )
                    .setUserAuthenticationRequired( true )
                    .setEncryptionPaddings( KeyProperties.ENCRYPTION_PADDING_PKCS7 );

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                builder.setInvalidatedByBiometricEnrollment( invalidatedByBiometricEnrollment );
            }
            keyGenerator.init( builder.build() );
            keyGenerator.generateKey();
        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException
                | CertificateException | IOException e) {
            throw new RuntimeException( e );
        }
    }

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
    public boolean initCipher(Cipher cipher, String keyName) {
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

    public Cipher getDefaultCipher() {
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
