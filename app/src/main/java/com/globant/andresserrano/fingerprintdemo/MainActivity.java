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
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.globant.andresserrano.fingerprintdemo.security.CipherFingerprint;
import com.globant.andresserrano.fingerprintdemo.security.KeyFingerprint;

import javax.crypto.Cipher;

public class MainActivity extends Activity {


    private static final String DIALOG_FRAGMENT_TAG = "FingerprintAuthenticationDialogFragment";
    static final String DEFAULT_KEY_NAME = "default_key";


    KeyFingerprint keyFingerprint;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );
        keyFingerprint = new KeyFingerprint();

        KeyguardManager keyguardManager = getSystemService( KeyguardManager.class );
        FingerprintManager fingerprintManager = getSystemService( FingerprintManager.class );
        Button purchaseButton = (Button) findViewById( R.id.button_purchase );
        // The line below prevents the false positive inspection from Android Studio
        // noinspection ResourceType
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && fingerprintManager.isHardwareDetected()) {

            if (!keyguardManager.isKeyguardSecure()) {
                Toast.makeText( this,
                        "Secure lock screen hasn't set up.\n"
                                + "Go to 'Settings -> Security -> Fingerprint' to set up a fingerprint",
                        Toast.LENGTH_LONG ).show();
                purchaseButton.setEnabled( false );
                return;
            }
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
            keyFingerprint.createKey( DEFAULT_KEY_NAME );
            purchaseButton.setEnabled( true );
            final Cipher cipher = CipherFingerprint.getDefaultCipher();
            purchaseButton.setOnClickListener( new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (CipherFingerprint.initCipher( keyFingerprint.getKeyStore(), cipher, DEFAULT_KEY_NAME )) {
                        FingerprintAuthenticationDialogFragment fragment
                                = new FingerprintAuthenticationDialogFragment();
                        fragment.setCryptoObject( new FingerprintManager.CryptoObject( cipher ) );

                        fragment.show( getFragmentManager(), DIALOG_FRAGMENT_TAG );
                    }
                }
            } );
        } else {
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
            byte[] encrypted = CipherFingerprint.tryEncrypt( cryptoObject.getCipher() );
            showConfirmation( encrypted );
        } else {
            showConfirmation( null );
        }
    }

    private void showConfirmation(byte[] encrypted) {
        if (encrypted != null) {
            Toast.makeText( this,
                    Base64.encodeToString( encrypted, 0 /* flags */ ),
                    Toast.LENGTH_LONG ).show();
        } else {
            Toast.makeText( this, "Failed to encrypt the data with the generated key. "
                    + "Retry the purchase", Toast.LENGTH_LONG ).show();
        }
    }


}
