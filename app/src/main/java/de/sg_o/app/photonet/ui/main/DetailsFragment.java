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

package de.sg_o.app.photonet.ui.main;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Xml;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.vectordrawable.graphics.drawable.SeekableAnimatedVectorDrawable;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;
import org.videolan.libvlc.util.VLCVideoLayout;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;

import de.sg_o.app.photonet.MainActivity;
import de.sg_o.app.photonet.R;
import de.sg_o.lib.photoNet.netData.Status;
import de.sg_o.lib.photoNet.printer.Printer;

public class DetailsFragment extends Fragment {
    private Printer p;
    private final Handler handler = new Handler();

    private TextView name;
    private TextView ip;
    private TextView status;
    private TextView filename;
    private TextView progress;
    private TextView z;
    private TextView time;

    private ImageView statusImage;

    private ImageButton pauseResume;
    private ImageButton stop;

    private SharedPreferences prefs;

    private VLCVideoLayout mVideoLayout = null;

    private LibVLC mLibVLC;
    private MediaPlayer mMediaPlayer;

    public static DetailsFragment newInstance(int i) {
        DetailsFragment detailsFragment = new DetailsFragment();
        Bundle args = new Bundle();
        args.putInt("printerInt", i);
        detailsFragment.setArguments(args);
        return detailsFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int i = 0;
        if (getArguments() != null) {
            i = getArguments().getInt("printerInt", -1);
        }
        p = MainActivity.ENVIRONMENT.getConnected().get(i);
        Context context = getContext();
        if (p != null && context != null) {
            prefs = context.getSharedPreferences("printer_settings_" + p.getIp(), Context.MODE_PRIVATE);

            ArrayList<String> options = new ArrayList<>();
            options.add("--audio-time-stretch");
            options.add("--no-audio");
            options.add("--repeat");
            mLibVLC = new LibVLC(context, options);
            mMediaPlayer = new MediaPlayer(mLibVLC);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState){
        View v = inflater.inflate(R.layout.fragment_details_main, viewGroup, false);
        name = v.findViewById(R.id.details_name);
        ip = v.findViewById(R.id.details_ip);
        status = v.findViewById(R.id.details_status);
        filename = v.findViewById(R.id.details_filename);
        progress = v.findViewById(R.id.details_progress);
        z = v.findViewById(R.id.details_z);
        time = v.findViewById(R.id.details_time);

        statusImage = v.findViewById(R.id.details_status_image);

        mVideoLayout = v.findViewById(R.id.details_ip_cam);

        ContextThemeWrapper wrapper = new ContextThemeWrapper(getContext(), R.style.Theme_PhotoNet_PrinterStatus_Offline);
        Drawable drawable = VectorDrawableCompat.create(getResources(), R.drawable.ic_printer, wrapper.getTheme());
        statusImage.setImageDrawable(drawable);

        pauseResume = v.findViewById(R.id.details_pause_resume);
        stop = v.findViewById(R.id.details_stop);

        pauseResume.setOnClickListener(v1 -> {
            if (p.getStatus().getState() == Status.State.PRINTING) {
                p.pause();
            } else if (p.getStatus().getState() == Status.State.PAUSE){
                p.resume();
            }
        });

        stop.setOnClickListener(v1 -> {
            if (p.getStatus().getState() == Status.State.PRINTING || p.getStatus().getState() == Status.State.PAUSE) {
                p.stop();
            }
        });

        pauseResume.setEnabled(false);
        stop.setEnabled(false);
        return v;
    }

    private final Runnable updatePrinters = new Runnable() {
        @Override
        public void run() {
            name.post(() -> name.setText(p.getName()));
            ip.post(() -> ip.setText(p.getIp()));
            Status s = p.getStatus();
            status.post(() -> status.setText(s.getState().name()));
            filename.post(() -> filename.setText(formatOpenedFile(s.getOpenedFile())));
            progress.post(() -> progress.setText(MessageFormat.format("{0}%", (int) (s.getProgress() * 100.0f))));
            z.post(() -> z.setText(MessageFormat.format("{0}mm", s.getZ())));
            formatTime();

            Context context = getContext();
            if (context != null) {
                int animateTime = 100;
                Resources.Theme theme = new ContextThemeWrapper(context, R.style.Theme_PhotoNet_PrinterStatus_Offline).getTheme();
                if (s.getState() == Status.State.PRINTING || s.getState() == Status.State.PAUSE) {
                    animateTime = Math.max(Math.min(Math.round(s.getProgress() * 100.0f), 100), 0);
                    pauseResume.post(() -> pauseResume.setEnabled(true));
                    stop.post(() -> stop.setEnabled(true));
                    if (s.getState() == Status.State.PRINTING) {
                        pauseResume.post(() -> pauseResume.setImageResource(R.drawable.ic_baseline_pause_48));
                    } else {
                        pauseResume.post(() -> pauseResume.setImageResource(R.drawable.ic_baseline_play_arrow_48));
                    }
                    theme = new ContextThemeWrapper(context, R.style.Theme_PhotoNet_PrinterStatus_Printing).getTheme();
                } else {
                    if (s.getState() == Status.State.FINISHED) {
                        theme = new ContextThemeWrapper(context, R.style.Theme_PhotoNet_PrinterStatus_Finished).getTheme();
                    }
                    if (s.getState() == Status.State.IDLE) {
                        theme = new ContextThemeWrapper(context, R.style.Theme_PhotoNet_PrinterStatus_Idle).getTheme();
                    }
                    pauseResume.post(() -> pauseResume.setEnabled(false));
                    stop.post(() -> stop.setEnabled(false));
                }
                if (statusImage.getVisibility() == View.VISIBLE) {
                    try {
                        //noinspection AndroidLintResourceType - Parse drawable as XML.
                        final XmlPullParser parser = context.getResources().getXml(R.drawable.ic_printer);
                        final AttributeSet attrs = Xml.asAttributeSet(parser);
                        int type;
                        do {
                            type = parser.next();
                        } while (type != XmlPullParser.START_TAG && type != XmlPullParser.END_DOCUMENT);
                        if (type != XmlPullParser.START_TAG) {
                            throw new XmlPullParserException("");
                        }
                        SeekableAnimatedVectorDrawable drawable = SeekableAnimatedVectorDrawable.createFromXmlInner(context.getResources(), parser, attrs, theme);
                        drawable.setCurrentPlayTime(animateTime);
                        statusImage.post(() -> statusImage.setImageDrawable(drawable));
                    } catch (XmlPullParserException | IOException ignore) {
                    }
                }
            }

            handler.postDelayed(this, 2000);
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        if (p != null) {
            name.setText(p.getName());
            ip.setText(p.getIp());
            Status s = p.getStatus();
            status.setText(s.getState().name());
            filename.setText(formatOpenedFile(s.getOpenedFile()));
            progress.setText(MessageFormat.format("{0}%", (int) (s.getProgress() * 100.0f)));
            z.setText(MessageFormat.format("{0}mm", s.getZ()));
            formatTime();
        }
        mVideoLayout.setVisibility(View.INVISIBLE);
        statusImage.setVisibility(View.VISIBLE);
        if (prefs != null) {
            if (prefs.getBoolean("printer_settings_use_webcam", false)) {
                String url = prefs.getString("printer_settings_webcam_address", "");
                if (url.length() > 0) {
                    mVideoLayout.setVisibility(View.VISIBLE);
                    statusImage.setVisibility(View.INVISIBLE);
                    setUpStream(url);
                }
            }
        }
        handler.postDelayed(updatePrinters, 2000);
    }

    private void formatTime() {
        Status s = p.getStatus();
        if (s.getTime().getHours() > 0) {
            time.post(() -> time.setText(MessageFormat.format("{0}:{1}:{2}",
                    s.getTime().getHours(), s.getTime().getMinutes(), s.getTime().getSeconds())));
        } else if (s.getTime().getMinutes() > 0) {
            time.post(() -> time.setText(MessageFormat.format("{0}m {1}s",
                    s.getTime().getMinutes(), s.getTime().getSeconds())));
        } else {
            time.post(() -> time.setText(MessageFormat.format("{0}s",
                    s.getTime().getSeconds())));
        }
    }

    private String formatOpenedFile(String in) {
        if (in == null) return "";
        return in;
    }

    @Override
    public void onPause() {
        super.onPause();
        tearDownStream();
        handler.removeCallbacks(updatePrinters);
    }

    private void setUpStream(String url) {
        if (mMediaPlayer == null) return;
        if (mVideoLayout == null) return;
        mMediaPlayer.attachViews(mVideoLayout, null, false, false);
        Uri.Builder uri = Uri.parse(url).buildUpon();
        Media media = new Media(mLibVLC, uri.build());
        media.setHWDecoderEnabled(true, false);
        media.addOption(":network-caching=1000");

        mMediaPlayer.setMedia(media);
        mMediaPlayer.setVolume(0);
        media.release();
        mMediaPlayer.play();
    }

    private void tearDownStream(){
        if (mMediaPlayer == null) return;
        mMediaPlayer.stop();
        mMediaPlayer.detachViews();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        name = null;
        ip = null;
        status = null;
        progress = null;
        z = null;
        time = null;

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mLibVLC.release();
        }
    }
}
