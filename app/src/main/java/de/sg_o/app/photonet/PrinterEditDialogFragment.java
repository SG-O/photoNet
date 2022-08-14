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
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.fragment.app.DialogFragment;

import de.sg_o.lib.photoNet.printer.Printer;

public class PrinterEditDialogFragment extends DialogFragment {
    private Printer printer;

    private EditText printerName;
    private Button renameButton;

    public PrinterEditDialogFragment(Printer printer) {
        super();
        this.printer = printer;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View content = inflater.inflate(R.layout.content_printer_edit, null);

        renameButton = content.findViewById(R.id.edit_rename);
        renameButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                renamePrinter();
            }
        });
        printerName = content.findViewById(R.id.edit_printerName);
        printerName.setText(printer.getName());
        printerName.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View view, int keyCode, KeyEvent keyevent) {
                if ((keyevent.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    renamePrinter();
                    return true;
                }
                return false;
            }
        });

        builder.setTitle("Edit Printer");
        builder.setView(content)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                })
                .setNeutralButton("DELETE", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        deletePrinter();
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }

    private void renamePrinter() {
        String name = printerName.getText().toString();
        if (name == null) return;
        if (name.length() < 1) return;
        if (name.length() > 16) return;
        printer.setName(name);
        this.dismiss();
        MainActivity.ADAPTER.notifyDataSetChanged();
    }

    private void deletePrinter() {
        MainActivity.ENVIRONMENT.disconnect(printer.getIp());
        SharedPreferences.Editor editor = MainActivity.PREFS.edit();
        editor.putString("connected", MainActivity.ENVIRONMENT.save());
        editor.apply();
        MainActivity.ADAPTER.notifyDataSetChanged();
    }
}
