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


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import de.sg_o.app.photonet.databinding.ActivityFilePreviewBinding;
import de.sg_o.app.photonet.fileTransfer.TransferService;
import de.sg_o.app.photonet.fileTransfer.TransferWorker;
import de.sg_o.app.photonet.localFile.PreviewFile;
import de.sg_o.app.photonet.ui.main.FilesFragment;
import de.sg_o.lib.photoNet.netData.FileListItem;
import de.sg_o.lib.photoNet.netData.FolderList;
import de.sg_o.lib.photoNet.printFile.PrintFileMeta;
import de.sg_o.lib.photoNet.printFile.photon.PhotonPrintFile;
import de.sg_o.lib.photoNet.printer.Folder;

public class FilePreviewActivity extends AppCompatActivity {
    private static final String FILE_NAME_TEXT = "fileNameText";
    private static final String SEEK_BAR_MAX = "seekBarMax";
    private static final String SEEK_BAR_PROGRESS = "seekBarProgress";
    private static final String SEEK_BAR_ENABLED = "seekBarEnabled";

    private EditText fileName;
    private ImageView preview;
    private ImageView layer;
    private SeekBar layerSeek;

    PreviewFile file;
    ParcelFileDescriptor pfd;
    Uri fileUri;
    Folder folder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        Uri uri = intent.getParcelableExtra(FilesFragment.EXTRA_FILE_URI);
        Folder folder = (Folder) intent.getSerializableExtra(FilesFragment.EXTRA_FILE_FOLDER);
        if (folder != null) {
            this.folder = folder;
            try {
                folder.update();
            } catch (UnsupportedEncodingException ignore) {
            }
        }

        de.sg_o.app.photonet.databinding.ActivityFilePreviewBinding binding = ActivityFilePreviewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.previewToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        this.fileName = findViewById(R.id.preview_fileName);
        this.preview = findViewById(R.id.preview_file_preview);
        this.layer = findViewById(R.id.preview_layer);
        this.layerSeek = findViewById(R.id.preview_layer_seek);
        ImageButton prevLayer = findViewById(R.id.preview_layer_prev);
        ImageButton nextLayer = findViewById(R.id.preview_layer_next);

        this.layerSeek.setEnabled(false);
        this.layerSeek.setMax(0);
        this.layerSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                file.selectLayer(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        prevLayer.setOnClickListener(v -> {
            int progress = layerSeek.getProgress();
            progress--;
            if (progress < 0) progress = 0;
            layerSeek.setProgress(progress);
        });

        nextLayer.setOnClickListener(v -> {
            int progress = layerSeek.getProgress();
            progress++;
            if (progress > layerSeek.getMax()) progress = layerSeek.getMax();
            layerSeek.setProgress(progress);
        });

        if (uri != null) {
            this.fileUri = uri;
            String[] projection = {MediaStore.MediaColumns.DISPLAY_NAME};
            Cursor metaCursor = getContentResolver().query(uri, projection, null, null, null);
            if (metaCursor != null) {
                try {
                    if (metaCursor.moveToFirst()) {
                        fileName.setText(metaCursor.getString(0));
                    }
                } finally {
                    metaCursor.close();
                }
            }
            try {
                pfd = getContentResolver().openFileDescriptor(uri, "r");
                if (pfd != null) {
                    long fileLength = pfd.getStatSize();
                    if (fileLength > 0) {
                        ParcelFileDescriptor.AutoCloseInputStream fileInputStream = new ParcelFileDescriptor.AutoCloseInputStream(pfd);
                        this.file = new PreviewFile(this, new PhotonPrintFile(fileInputStream, fileLength));
                        this.file.update();
                    }
                }
            } catch (FileNotFoundException ignore) {
            }
        }
    }

    private void updatePreview() {
        Bitmap img = file.getPreviewImg();
        if (img != null) {
            preview.setImageBitmap(img);
        }
    }

    private void updateLayer() {
        Bitmap img = file.getLayerImg();
        if (img != null) {
            layer.setImageBitmap(img);
        }
    }

    private void updateMeta() {
        PrintFileMeta meta = file.getMeta();
        if (meta != null) {
            this.layerSeek.setEnabled(true);
            this.layerSeek.setMax(meta.getNrLayers() - 1);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            unregisterReceiver(layerMessageReceiver);
        } catch (IllegalArgumentException ignore) {
        }
        try {
            unregisterReceiver(previewMessageReceiver);
        } catch (IllegalArgumentException ignore) {
        }
        try {
            unregisterReceiver(metaMessageReceiver);
        } catch (IllegalArgumentException ignore) {
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(previewMessageReceiver,
                new IntentFilter(PreviewFile.PREVIEW_BROADCAST));
        LocalBroadcastManager.getInstance(this).registerReceiver(layerMessageReceiver,
                new IntentFilter(PreviewFile.LAYER_BROADCAST));
        LocalBroadcastManager.getInstance(this).registerReceiver(metaMessageReceiver,
                new IntentFilter(PreviewFile.META_BROADCAST));

        updateMeta();
        updateLayer();
        updatePreview();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        file.close();
        try {
            pfd.close();
        } catch (IOException ignore) {
        }
    }

    private final BroadcastReceiver previewMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updatePreview();
        }
    };

    private final BroadcastReceiver layerMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateLayer();
        }
    };

    private final BroadcastReceiver metaMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateMeta();
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.file_preview, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.preview_menu_upload) {
            uploadFile();
        }
        return super.onOptionsItemSelected(item);
    }

    private void uploadFile() {
        if (this.fileUri == null) return;
        if (folder == null) return;
        String entered = fileName.getText().toString();
        if (entered.length() < 1) return;
        FolderList list = folder.getFolderList();
        if (list == null) return;
        try {
            file.close();
            pfd.close();
            ParcelFileDescriptor pfd = getContentResolver().openFileDescriptor(fileUri, "r");
            ParcelFileDescriptor.AutoCloseInputStream fileInputStream = new ParcelFileDescriptor.AutoCloseInputStream(pfd);
            FileListItem newFile = list.newFile(entered, fileInputStream.available());
            TransferWorker.addTransfer(newFile.getUpload(fileInputStream, 2));
            TransferService.startService(this);
        } catch (IOException ignore) {
        }
        finish();
    }

    @Override
    protected void onSaveInstanceState(@NonNull final Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save the state of item position
        outState.putString(FILE_NAME_TEXT, fileName.getText().toString());
        outState.putInt(SEEK_BAR_MAX, layerSeek.getMax());
        outState.putInt(SEEK_BAR_PROGRESS, layerSeek.getProgress());
        outState.putBoolean(SEEK_BAR_ENABLED, layerSeek.isEnabled());
    }

    @Override
    protected void onRestoreInstanceState(final Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // Read the state of item position
        fileName.setText(savedInstanceState.getString(FILE_NAME_TEXT));
        layerSeek.setMax(savedInstanceState.getInt(SEEK_BAR_MAX));
        layerSeek.setProgress(savedInstanceState.getInt(SEEK_BAR_PROGRESS));
        layerSeek.setEnabled(savedInstanceState.getBoolean(SEEK_BAR_ENABLED));
    }
}