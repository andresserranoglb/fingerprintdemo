/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package com.globant.andresserrano.fingerprintdemo;

import android.app.Activity;
import android.app.KeyguardManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.globant.andresserrano.fingerprintdemo.security.CipherFingerptint;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;

/**
 * Main entry point for the sample, showing a backpack and "Purchase" button.
 */
public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String DIALOG_FRAGMENT_TAG = "FingerprintAuthenticationDialogFragment";
    private static final String SECRET_MESSAGE = "Very secret message";
    static final String DEFAULT_KEY_NAME = "default_key";


    CipherFingerptint cipherFingerptint;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );

        cipherFingerptint = new CipherFingerptint();

        KeyguardManager keyguardManager = getSystemService( KeyguardManager.class );
        FingerprintManager fingerprintManager = getSystemService( FingerprintManager.class );
        Button purchaseButton = (Button) findViewById( R.id.button_purchase );
        // The line below prevents the false positive inspection from Android Studio
        // noinspection ResourceType
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && fingerprintManager.isHardwareDetected()) {

            if (!keyguardManager.isKeyguardSecure()) {
                // Show a message that the user hasn't set up a fingerprint or lock screen.
                Toast.makeText( this,
                        "Secure lock screen hasn't set up.\n"
                                + "Go to 'Settings -> Security -> Fingerprint' to set up a fingerprint",
                        Toast.LENGTH_LONG ).show();
                purchaseButton.setEnabled( false );
                return;
            }
            // Now the protection level of USE_FINGERPRINT permission is normal instead of dangerous.
            // See http://developer.android.com/reference/android/Manifest.permission.html#USE_FINGERPRINT
            // The line below prevents the false positive inspection from Android Studio
            // noinspection ResourceType
            if (!fingerprintManager.hasEnrolledFingerprints()) {
                purchaseButton.setEnabled( false );
                // This happens when no fingerprints are registered.
                Toast.makeText( this,
                        "Go to 'Settings -> Security -> Fingerprint' and register at least one fingerprint",
                        Toast.LENGTH_LONG ).show();
                return;
            }
            cipherFingerptint.createKey( DEFAULT_KEY_NAME, true );
            purchaseButton.setEnabled( true );
            final Cipher cipher =cipherFingerptint.getDefaultCipher();
            purchaseButton.setOnClickListener( new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (cipherFingerptint.initCipher( cipher, DEFAULT_KEY_NAME )) {
                        // Show the fingerprint dialog. The user has the option to use the fingerprint with
                        // crypto, or you can fall back to using a server-side verified password.
                        FingerprintAuthenticationDialogFragment fragment
                                = new FingerprintAuthenticationDialogFragment();
                        fragment.setCryptoObject( new FingerprintManager.CryptoObject( cipher) );

                        fragment.show( getFragmentManager(), DIALOG_FRAGMENT_TAG );
                    }
                }
            } );
        }
        else{
            purchaseButton.setEnabled( false );
        }


    }

    /**
     * Proceed the purchase operation
     *
     * @param withFingerprint {@code true} if the purchase was made by using a fingerprint
     * @param cryptoObject    the Crypto object
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void onAuthenticatedWhitFingerPrint(boolean withFingerprint,
                                               @Nullable FingerprintManager.CryptoObject cryptoObject) {
        if (withFingerprint) {
            // If the user has authenticated with fingerprint, verify that using cryptography and
            // then show the confirmation message.
            assert cryptoObject != null;
            tryEncrypt( cryptoObject.getCipher() );
        } else {
            // Authentication happened with backup password. Just show the confirmation message.
            showConfirmation( null );
        }
    }

    // Show confirmation, if fingerprint was used show crypto information.
    private void showConfirmation(byte[] encrypted) {
        if (encrypted != null) {
            Toast.makeText( this,
                    Base64.encodeToString( encrypted, 0 /* flags */ ),
                    Toast.LENGTH_LONG ).show();
        }
    }

    /**
     * Tries to encrypt some data with the generated key in  which is
     * only works if the user has just authenticated via fingerprint.
     */
    private void tryEncrypt(Cipher cipher) {
        try {
            byte[] encrypted = cipher.doFinal( SECRET_MESSAGE.getBytes() );
            showConfirmation( encrypted );
        } catch (BadPaddingException | IllegalBlockSizeException e) {
            Toast.makeText( this, "Failed to encrypt the data with the generated key. "
                    + "Retry the purchase", Toast.LENGTH_LONG ).show();
            Log.e( TAG, "Failed to encrypt the data with the generated key." + e.getMessage() );
        }
    }
}
