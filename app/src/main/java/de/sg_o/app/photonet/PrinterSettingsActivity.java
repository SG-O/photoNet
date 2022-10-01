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

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.sg_o.app.photonet.util.ButtonPreference;
import de.sg_o.lib.photoNet.printer.Printer;

public class PrinterSettingsActivity extends AppCompatActivity {

    Printer printer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        int i = intent.getIntExtra(DetailsActivity.PRINTER_ID, 0);
        printer = MainActivity.ENVIRONMENT.getConnected().get(i);

        setContentView(R.layout.activity_printer_settings);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.content_printer_settings_settings, new SettingsFragment(printer.getIp()))
                    .commit();
        }
        Toolbar toolbar = findViewById(R.id.printer_settings_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }
    }

    public static class SettingsFragment extends PreferenceFragmentCompat{
        private final List<String> validProtocols = Arrays.asList("http", "https", "ftp", "ftps", "ftpes", "mms", "mmsh", "mmst", "mmsu", "rtp", "rtcp", "rtmp", "rtsp", "sdp", "tcp", "udp");

        private String printerIP;
        ButtonPreference verify_pref;

        private LibVLC mLibVLC;
        private MediaPlayer mMediaPlayer;

        public SettingsFragment(String printerIP) {
            super();
            this.printerIP = printerIP;
        }

        public SettingsFragment() {
            super();
        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            if (printerIP == null) {
                if (savedInstanceState == null) return;
                printerIP = savedInstanceState.getString("Printer_IP", null);
                if (printerIP == null) return;
            }
            getPreferenceManager().setSharedPreferencesName("printer_settings_" + printerIP);
            setPreferencesFromResource(R.xml.printer_settings, rootKey);
            EditTextPreference url = getPreferenceScreen().findPreference("printer_settings_webcam_address");
            verify_pref = findPreference("printer_settings_webcam_verify");
            if (verify_pref != null && url != null) {
                verify_pref.setOnClickListener(v -> {
                    ExecutorService executor = Executors.newSingleThreadExecutor();
                    Handler handler = new Handler(Looper.getMainLooper());
                    executor.execute(() -> {

                        boolean canConnect = verifyPlayback(url.getText(), 5000);

                        handler.post(() -> {
                            Context context = getContext();
                            CharSequence text;
                            if (canConnect) {
                                text = "Connection successful";
                            } else {
                                text = "Connection failed";
                            }
                            int duration = Toast.LENGTH_LONG;
                            Toast toast = Toast.makeText(context, text, duration);
                            toast.show();
                        });
                    });
                });
            }
            if (url != null) {
                url.setOnPreferenceChangeListener(urlValidator);
            }
            Context context = getContext();
            if (context != null) {
                ArrayList<String> options = new ArrayList<>();
                options.add("--audio-time-stretch");
                options.add("--no-audio");
                mLibVLC = new LibVLC(context, options);

                mMediaPlayer = new MediaPlayer(mLibVLC);
            }
        }

        public PreferenceManager getPreferenceManager() {
            PreferenceManager manager = super.getPreferenceManager();
            manager.setSharedPreferencesName("printer_settings_" + printerIP);
            manager.setSharedPreferencesMode(Context.MODE_PRIVATE);
            return manager;
        }

        private final Preference.OnPreferenceChangeListener urlValidator = (preference, newValue) -> {
            if (!verifyUrl((String) newValue)) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Invalid Url");
                builder.setMessage("Make sure the include the protocol like 'http://' or 'rtsp://'.");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.show();
                return false;
            }
            return true;
        };

        private boolean verifyUrl(String url) {
            if (url == null) return false;
            String[] split = url.split("://");
            if (split.length < 2) return false;
            return validProtocols.contains(split[0].toLowerCase(Locale.ROOT));
        }

        @SuppressWarnings("SameParameterValue")
        private boolean verifyPlayback(String url, int timeout) {
            if (mMediaPlayer == null) return false;
            Media media = new Media(mLibVLC, Uri.parse(url));
            media.setHWDecoderEnabled(true, false);

            mMediaPlayer.setMedia(media);
            mMediaPlayer.setVolume(0);
            media.release();
            long startTime = System.currentTimeMillis();
            mMediaPlayer.play();
            while (System.currentTimeMillis() < (startTime + timeout)) {
                if (mMediaPlayer.isPlaying()) {
                    tearDownStream();
                    return true;
                }
            }
            tearDownStream();
            return false;
        }

        private void tearDownStream(){
            if (mMediaPlayer == null) return;
            mMediaPlayer.stop();
            mMediaPlayer.detachViews();
        }

        @Override
        public void onDestroy() {
            super.onDestroy();

            if (mMediaPlayer != null) {
                mMediaPlayer.release();
                mLibVLC.release();
            }
        }

        @Override
        public void onSaveInstanceState(@NonNull Bundle outState) {
            super.onSaveInstanceState(outState);
            outState.putString("Printer_IP", printerIP);
        }
    }
}