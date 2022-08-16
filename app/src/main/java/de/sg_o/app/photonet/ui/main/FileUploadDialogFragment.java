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
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import java.io.IOException;

import de.sg_o.app.photonet.R;
import de.sg_o.app.photonet.filetransfer.TransferService;
import de.sg_o.app.photonet.filetransfer.TransferWorker;
import de.sg_o.lib.photoNet.netData.FileListItem;
import de.sg_o.lib.photoNet.netData.FolderList;
import de.sg_o.lib.photoNet.printer.Folder;

public class FileUploadDialogFragment extends DialogFragment {
    private final Uri file;
    private final Folder folder;

    private EditText remoteName;

    public FileUploadDialogFragment(Uri file, Folder folder) {
        super();
        this.file = file;
        this.folder = folder;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View content = inflater.inflate(R.layout.content_file_upload, null);
        remoteName = content.findViewById(R.id.upload_file_name);
        TextView filePath = content.findViewById(R.id.upload_file_path);
        String[] projection = {MediaStore.MediaColumns.DISPLAY_NAME};
        Cursor metaCursor = getContext().getContentResolver().query(file, projection, null, null, null);
        if (metaCursor != null) {
            try {
                if (metaCursor.moveToFirst()) {
                    remoteName.setText(metaCursor.getString(0));
                    filePath.setText(getString(R.string.upload_dialog_selected) + metaCursor.getString(0));
                }
            } finally {
                metaCursor.close();
            }
        }

        builder.setTitle("Upload File");
        builder.setView(content)
                .setNegativeButton("Cancel", (dialog, id) -> {

                })
                .setPositiveButton("Upload", (dialog, id) -> uploadFile());
        return builder.create();
    }

    private void uploadFile() {
        String entered = remoteName.getText().toString();
        if (entered.length() < 1) return;
        FolderList list = folder.getFolderList();
        if (list == null) return;
        if (getActivity() == null) return;
        try {
            ParcelFileDescriptor pfd = getActivity().getContentResolver().openFileDescriptor(file, "r");
            ParcelFileDescriptor.AutoCloseInputStream fileInputStream = new ParcelFileDescriptor.AutoCloseInputStream(pfd);
            FileListItem newFile = list.newFile(entered, fileInputStream.available());
            TransferWorker.addTransfer(newFile.getUpload(fileInputStream, 2));
            TransferService.startService(getActivity());
        } catch (IOException ignore) {
        }
    }
}
