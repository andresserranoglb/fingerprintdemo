package com.globant.andresserrano.fingerprintdemo.security;


import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.support.annotation.RequiresApi;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;

import javax.crypto.KeyGenerator;

public class KeyFingerprint {

    private KeyStore keyStore;
    private KeyGenerator keyGenerator;

    @RequiresApi(api = Build.VERSION_CODES.M)
    public KeyFingerprint() {
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

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void createKey(String keyName) {
        try {
            keyStore.load( null );
            KeyGenParameterSpec.Builder builder = new KeyGenParameterSpec.Builder( keyName,
                    KeyProperties.PURPOSE_ENCRYPT |
                            KeyProperties.PURPOSE_DECRYPT )
                    .setBlockModes( KeyProperties.BLOCK_MODE_CBC )
                    .setUserAuthenticationRequired( true )
                    .setEncryptionPaddings( KeyProperties.ENCRYPTION_PADDING_PKCS7 );
            keyGenerator.init( builder.build() );
            keyGenerator.generateKey();
        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException
                | CertificateException | IOException e) {
            throw new RuntimeException( e );
        }
    }

    public KeyStore getKeyStore() {
        return keyStore;
    }
}
