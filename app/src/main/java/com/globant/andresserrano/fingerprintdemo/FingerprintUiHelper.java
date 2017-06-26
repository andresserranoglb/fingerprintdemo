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

import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.CancellationSignal;
import android.support.annotation.RequiresApi;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Small helper class to manage text/icon around fingerprint authentication UI.
 */
@RequiresApi(api = Build.VERSION_CODES.M)
public class FingerprintUiHelper extends FingerprintManager.AuthenticationCallback {

    private static final long ERROR_TIMEOUT_MILLIS = 1600;
    private static final long SUCCESS_DELAY_MILLIS = 1300;

    private final FingerprintManager fingerprintManager;
    private final ImageView fingerprintIcon;
    private final TextView errorFingerprintText;
    private final Callback callback;
    private CancellationSignal cancellationSignal;

    private boolean selfCancelled;

    /**
     * Constructor for {@link FingerprintUiHelper}.
     */
    FingerprintUiHelper(FingerprintManager fingerprintManager,
            ImageView icon, TextView errorTextView, Callback callback) {
        this.fingerprintManager = fingerprintManager;
        fingerprintIcon = icon;
        this.errorFingerprintText = errorTextView;
        this.callback = callback;
    }

    public boolean isFingerprintAuthAvailable() {
        // The line below prevents the false positive inspection from Android Studio
        // noinspection ResourceType
        return fingerprintManager.isHardwareDetected()
                && fingerprintManager.hasEnrolledFingerprints();
    }

    public void startListening(FingerprintManager.CryptoObject cryptoObject) {
        if (!isFingerprintAuthAvailable()) {
            return;
        }
        cancellationSignal = new CancellationSignal();
        selfCancelled = false;
        // The line below prevents the false positive inspection from Android Studio
        // noinspection ResourceType
        fingerprintManager
                .authenticate(cryptoObject, cancellationSignal, 0 /* flags */, this, null);
        fingerprintIcon.setImageResource(R.drawable.ic_fp_40px);
    }

    public void stopListening() {
        if (cancellationSignal != null) {
            selfCancelled = true;
            cancellationSignal.cancel();
            cancellationSignal = null;
        }
    }

    @Override
    public void onAuthenticationError(int errMsgId, CharSequence errString) {
        if (!selfCancelled) {
            showError(errString);
            fingerprintIcon.postDelayed( new Runnable() {
                @Override
                public void run() {
                    callback.onError();
                }
            }, ERROR_TIMEOUT_MILLIS);
        }
    }

    @Override
    public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
        showError(helpString);
    }

    @Override
    public void onAuthenticationFailed() {
        showError( fingerprintIcon.getResources().getString(
                R.string.fingerprint_not_recognized));
    }

    @Override
    public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
        errorFingerprintText.removeCallbacks(mResetErrorTextRunnable);
        fingerprintIcon.setImageResource(R.drawable.ic_fingerprint_success);
        errorFingerprintText.setTextColor(
                errorFingerprintText.getResources().getColor(R.color.success_color, null));
        errorFingerprintText.setText(
                errorFingerprintText.getResources().getString(R.string.fingerprint_success));
        fingerprintIcon.postDelayed( new Runnable() {
            @Override
            public void run() {
                callback.onAuthenticated();
            }
        }, SUCCESS_DELAY_MILLIS);
    }

    private void showError(CharSequence error) {
        fingerprintIcon.setImageResource(R.drawable.ic_fingerprint_error);
        errorFingerprintText.setText(error);
        errorFingerprintText.setTextColor(
                errorFingerprintText.getResources().getColor(R.color.warning_color, null));
        errorFingerprintText.removeCallbacks(mResetErrorTextRunnable);
        errorFingerprintText.postDelayed(mResetErrorTextRunnable, ERROR_TIMEOUT_MILLIS);
    }

    private Runnable mResetErrorTextRunnable = new Runnable() {
        @Override
        public void run() {
            errorFingerprintText.setTextColor(
                    errorFingerprintText.getResources().getColor(R.color.hint_color, null));
            errorFingerprintText.setText(
                    errorFingerprintText.getResources().getString(R.string.fingerprint_hint));
            fingerprintIcon.setImageResource(R.drawable.ic_fp_40px);
        }
    };

    public interface Callback {

        void onAuthenticated();

        void onError();
    }
}
