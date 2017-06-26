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

import android.app.DialogFragment;
import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * A dialog which uses fingerprint APIs to authenticate the user, and falls back to password
 * authentication if fingerprint is not available.
 */
public class FingerprintAuthenticationDialogFragment extends DialogFragment
        implements FingerprintUiHelper.Callback {

    private Button buttonCancel;
    private View fingerprintContent;

    private FingerprintManager.CryptoObject cryptoObject;
    private FingerprintUiHelper fingerprintUiHelper;
    private MainActivity mainActivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setRetainInstance( true );
        setStyle( DialogFragment.STYLE_NORMAL, android.R.style.Theme_Material_Light_Dialog );
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getDialog().setTitle( getString( R.string.sign_in ) );
        View v = inflater.inflate( R.layout.fingerprint_dialog_container, container, false );
        buttonCancel = (Button) v.findViewById( R.id.button_cancel );
        buttonCancel.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        } );


        fingerprintContent = v.findViewById( R.id.fingerprint_container );
        fingerprintUiHelper = new FingerprintUiHelper(
                mainActivity.getSystemService( FingerprintManager.class ),
                (ImageView) v.findViewById( R.id.fingerprint_icon ),
                (TextView) v.findViewById( R.id.fingerprint_status ), this );
        updateStage();

        if (!fingerprintUiHelper.isFingerprintAuthAvailable()) {
            goToBackup();
        }
        return v;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onResume() {
        super.onResume();
        fingerprintUiHelper.startListening( cryptoObject );
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onPause() {
        super.onPause();
        fingerprintUiHelper.stopListening();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onAttach(Context context) {
        super.onAttach( context );
        mainActivity = (MainActivity) getActivity();
    }

    /**
     * Sets the crypto object to be passed in when authenticating with fingerprint.
     */
    public void setCryptoObject(FingerprintManager.CryptoObject cryptoObject) {
        this.cryptoObject = cryptoObject;
    }

    /**
     * Switches to backup (password) screen. This either can happen when fingerprint is not
     * available or the user chooses to use the password authentication method by pressing the
     * button. This can also happen when the user had too many fingerprint attempts.
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void goToBackup() {
        updateStage();
        // Fingerprint is not used anymore. Stop listening for it.
        fingerprintUiHelper.stopListening();
    }

    private void updateStage() {
        buttonCancel.setText( R.string.cancel );
        fingerprintContent.setVisibility( View.VISIBLE );

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onAuthenticated() {
        mainActivity.onAuthenticatedWhitFingerPrint( true /* withFingerprint */, cryptoObject );
        dismiss();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onError() {
        goToBackup();
    }

}
