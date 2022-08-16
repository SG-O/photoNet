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

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.sg_o.app.photonet.MainActivity;
import de.sg_o.app.photonet.R;
import de.sg_o.app.photonet.menu.DiscoveryAdapter;

public class DiscoveryDialogFragment extends DialogFragment implements DiscoveryAdapter.ItemClickListener{
    private static final String IPV4_REGEX =
            "^(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\." +
                    "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\." +
                    "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\." +
                    "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";

    private static final Pattern IPv4_PATTERN = Pattern.compile(IPV4_REGEX);

    DiscoveryAdapter adapter;

    RecyclerView recyclerView;
    SwipeRefreshLayout refreshLayout;
    LinearLayout manual;
    EditText ipText;
    Button manualAdd;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View content = inflater.inflate(R.layout.content_discover, null);

        recyclerView = content.findViewById(R.id.d_printers);
        manual = content.findViewById(R.id.d_manual);
        manualAdd = content.findViewById(R.id.d_button_add);
        manualAdd.setOnClickListener(v -> manualAdd());
        ipText = content.findViewById(R.id.d_ip_input);
        ipText.setFilters(ipFilter());
        ipText.setOnKeyListener((view, keyCode, keyevent) -> {
            if ((keyevent.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                manualAdd();
                return true;
            }
            return false;
        });

        manual.setVisibility(View.INVISIBLE);

        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        refreshLayout = content.findViewById(R.id.d_swipeRefreshLayout);
        adapter = new DiscoveryAdapter(this.getContext(), refreshLayout);
        recyclerView.setAdapter(adapter);
        adapter.setClickListener(this);

        builder.setTitle("Add Printer");
        builder.setView(content)
                .setNegativeButton("Cancel", (dialog, id) -> {

                })
                .setNeutralButton("Manual", null);
        return builder.create();
    }

    @Override
    public void onResume() {
        super.onResume();

        AlertDialog dialog = (AlertDialog) getDialog();
        if(dialog != null)
        {
            Button positiveButton = dialog.getButton(Dialog.BUTTON_NEUTRAL);
            positiveButton.setOnClickListener(v -> {
                if (manual.getVisibility() == View.INVISIBLE) {
                    refreshLayout.setVisibility(View.INVISIBLE);
                    manual.setVisibility(View.VISIBLE);
                } else {
                    manual.setVisibility(View.INVISIBLE);
                    refreshLayout.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    private InputFilter[] ipFilter() {
        InputFilter[] filters = new InputFilter[1];
        filters[0] = (source, start, end, dest, dstart, dend) -> {
            if (end > start) {
                String destTxt = dest.toString();
                String resultingTxt = destTxt.substring(0, dstart)
                        + source.subSequence(start, end)
                        + destTxt.substring(dend);
                if (!resultingTxt
                        .matches("^\\d{1,3}(\\.(\\d{1,3}(\\.(\\d{1,3}(\\.(\\d{1,3})?)?)?)?)?)?")) {
                    return "";
                } else {
                    String[] splits = resultingTxt.split("\\.");
                    for (String split : splits) {
                        if (Integer.parseInt(split) > 255) {
                            return "";
                        }
                    }
                }
            }
            return null;
        };
        return filters;
    }

    boolean verifyIP()
    {
        String ip = ipText.getText().toString();

        Matcher matcher = IPv4_PATTERN.matcher(ip);
        return matcher.matches();
    }

    private void manualAdd() {
        if (verifyIP()) {
            MainActivity.ENVIRONMENT.connect(ipText.getText().toString());
            SharedPreferences.Editor editor = MainActivity.PREFS.edit();
            editor.putString("connected", MainActivity.ENVIRONMENT.save());
            editor.apply();
            MainActivity.ADAPTER.notifyDataSetChanged();
            this.dismiss();
        }
    }

    public void onItemClick(View view, int p) {
        String ip = adapter.getItem(p);
        if (ip != null) {
            MainActivity.ENVIRONMENT.connect(ip);
            SharedPreferences.Editor editor = MainActivity.PREFS.edit();
            editor.putString("connected", MainActivity.ENVIRONMENT.save());
            editor.apply();
            MainActivity.ADAPTER.notifyDataSetChanged();
        }
        this.dismiss();
    }
}
