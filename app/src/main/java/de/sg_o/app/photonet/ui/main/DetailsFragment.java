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
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import de.sg_o.app.photonet.MainActivity;
import de.sg_o.app.photonet.R;
import de.sg_o.lib.photoNet.netData.Status;
import de.sg_o.lib.photoNet.printer.Printer;

public class DetailsFragment extends Fragment {
    Printer p;
    Handler handler = new Handler();

    TextView name;
    TextView ip;
    TextView status;
    TextView filename;
    TextView progress;
    TextView z;
    TextView time;

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
            i = getArguments().getInt("printerInt", 0);
        }
        p = MainActivity.ENVIRONMENT.getConnected().get(i);
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
            progress.post(() -> progress.setText((int) (s.getProgress() * 100.0f) + "%"));
            z.post(() -> z.setText(s.getZ() + "mm"));
            time.post(() -> time.setText(s.getTime() + "s"));
            handler.postDelayed(this, 2000);
        }
    };

    @Override
    public void onStart() {
        super.onStart();
        if (p != null) {
            name.setText(p.getName());
            ip.setText(p.getIp());
            Status s = p.getStatus();
            status.setText(s.getState().name());
            filename.setText(formatOpenedFile(s.getOpenedFile()));
            progress.setText((int) (s.getProgress() * 100.0f) + "%");
            z.setText(s.getZ() + "mm");
            time.setText(s.getTime() + "s");
        }
        handler.postDelayed(updatePrinters, 2000);
    }

    private String formatOpenedFile(String in) {
        if (in == null) return "";
        return in;
    }

    @Override
    public void onStop() {
        super.onStop();
        handler.removeCallbacks(updatePrinters);
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
}
