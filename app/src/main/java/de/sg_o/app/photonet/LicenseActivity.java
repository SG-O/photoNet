/*
 *
 *   Copyright (C) 2022 Joerg Bayer (SG-O)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package de.sg_o.app.photonet;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import de.sg_o.app.photonet.databinding.ActivityLicenseBinding;

public class LicenseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        de.sg_o.app.photonet.databinding.ActivityLicenseBinding binding = ActivityLicenseBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.licToolbar);
        Button accept = findViewById(R.id.lic_accept);
        accept.setOnClickListener(view -> {
            SharedPreferences.Editor editor = MainActivity.PREFS.edit();
            editor.putBoolean("licAccept", true);
            editor.apply();
            finish();
        });

        Button reject = findViewById(R.id.lic_reject);
        reject.setOnClickListener(view -> {
            SharedPreferences.Editor editor = MainActivity.PREFS.edit();
            editor.putBoolean("licAccept", false);
            editor.apply();
            finishAffinity();
        });

        OnBackPressedCallback callback = new OnBackPressedCallback(true ) {
            @Override
            public void handleOnBackPressed() {
                boolean licAccepted = MainActivity.PREFS.getBoolean("licAccept", false);
                if (licAccepted) {
                    finish();
                } else {
                    finishAffinity();
                }
            }
        };
        this.getOnBackPressedDispatcher().addCallback(this, callback);
    }

}