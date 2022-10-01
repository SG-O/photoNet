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

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import java.text.DecimalFormat;
import java.text.MessageFormat;

import de.sg_o.app.photonet.MainActivity;
import de.sg_o.app.photonet.R;
import de.sg_o.lib.photoNet.netData.Status;
import de.sg_o.lib.photoNet.printer.Printer;

public class ManualControlFragment extends Fragment {
    Printer p;
    private final Handler handler = new Handler();

    TextView distanceText;
    SeekBar distanceSeek;

    ImageButton upButton;
    ImageButton downButton;
    ImageButton stopButton;
    ImageButton homeButton;

    TextView unavailable;


    public static ManualControlFragment newInstance(int i) {
        ManualControlFragment detailsFragment = new ManualControlFragment();
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
            i = getArguments().getInt("printerInt", 0);
        }
        p = MainActivity.ENVIRONMENT.getConnected().get(i);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState){
        View v = inflater.inflate(R.layout.fragment_manual, viewGroup, false);
        distanceText = v.findViewById(R.id.manual_text_distance);
        distanceSeek = v.findViewById(R.id.manual_seekbar_distance);
        upButton = v.findViewById(R.id.manual_go_up);
        downButton = v.findViewById(R.id.manual_go_down);
        stopButton = v.findViewById(R.id.manual_stop);
        homeButton = v.findViewById(R.id.manual_go_home);
        unavailable = v.findViewById(R.id.manual_unavailable);

        distanceText.setVisibility(View.GONE);
        distanceSeek.setVisibility(View.GONE);
        upButton.setVisibility(View.GONE);
        downButton.setVisibility(View.GONE);
        stopButton.setVisibility(View.GONE);
        homeButton.setVisibility(View.GONE);
        unavailable.setVisibility(View.VISIBLE);

        upButton.setOnClickListener(view -> p.moveZ(getDistance(distanceSeek.getProgress())));

        downButton.setOnClickListener(view -> p.moveZ(-getDistance(distanceSeek.getProgress())));

        stopButton.setOnClickListener(view -> p.stopMove());

        homeButton.setOnClickListener(view -> p.homeZ());

        this.distanceSeek.setEnabled(true);
        this.distanceSeek.setMax(5);
        this.distanceSeek.setProgress(0);
        distanceText.setText(MessageFormat.format("{0}mm", new DecimalFormat("0.00").format(getDistance(0))));
        this.distanceSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                distanceText.setText(MessageFormat.format("{0}mm", new DecimalFormat("0.00").format(getDistance(progress))));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        return v;
    }

    private float getDistance(int progress) {
        switch (progress) {
            case 1:
                return 0.5f;
            case 2:
                return 1f;
            case 3:
                return 5f;
            case 4:
                return 10f;
            case 5:
                return 50f;
            default:
                return 0.1f;
        }
    }

    private final Runnable updatePrinter = new Runnable() {
        @Override
        public void run() {
            if (p != null) {
                if (p.getStatus().getState() == Status.State.IDLE || p.getStatus().getState() == Status.State.FINISHED) {
                    distanceText.post(() -> distanceText.setVisibility(View.VISIBLE));
                    distanceSeek.post(() -> distanceSeek.setVisibility(View.VISIBLE));
                    upButton.post(() -> upButton.setVisibility(View.VISIBLE));
                    downButton.post(() -> downButton.setVisibility(View.VISIBLE));
                    stopButton.post(() -> stopButton.setVisibility(View.VISIBLE));
                    homeButton.post(() -> homeButton.setVisibility(View.VISIBLE));
                    unavailable.post(() -> unavailable.setVisibility(View.GONE));
                } else {
                    distanceText.post(() -> distanceText.setVisibility(View.GONE));
                    distanceSeek.post(() -> distanceSeek.setVisibility(View.GONE));
                    upButton.post(() -> upButton.setVisibility(View.GONE));
                    downButton.post(() -> downButton.setVisibility(View.GONE));
                    stopButton.post(() -> stopButton.setVisibility(View.GONE));
                    homeButton.post(() -> homeButton.setVisibility(View.GONE));
                    unavailable.post(() -> unavailable.setVisibility(View.VISIBLE));
                }
            }
            handler.postDelayed(this, 2000);
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        handler.postDelayed(updatePrinter, 100);
    }

    @Override
    public void onPause() {
        super.onPause();
        handler.removeCallbacks(updatePrinter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
