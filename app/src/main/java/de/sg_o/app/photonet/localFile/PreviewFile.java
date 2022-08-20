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

package de.sg_o.app.photonet.localFile;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.sg_o.lib.photoNet.photonFile.PhotonFile;
import de.sg_o.lib.photoNet.photonFile.PhotonFileMeta;
import de.sg_o.lib.photoNet.photonFile.PhotonFilePreview;
import de.sg_o.lib.photoNet.photonFile.PhotonLayer;

public class PreviewFile implements Runnable {
    public static final String PREVIEW_BROADCAST = "de.sg_o.app.photonet.preview.preview";
    public static final String LAYER_BROADCAST = "de.sg_o.app.photonet.preview.layer";
    public static final String META_BROADCAST = "de.sg_o.app.photonet.preview.layer";

    private final Context context;
    private PhotonFile file;
    ExecutorService updater = Executors.newSingleThreadExecutor();

    private Bitmap previewImg = null;
    private Bitmap layerImg = null;
    private PhotonFileMeta meta = null;

    private int selectedLayer = 0;

    public PreviewFile(Context context, PhotonFile file) {
        this.context = context;
        this.file = file;
    }

    public void selectLayer(int layer) {
        if (file == null) return;
        PhotonFileMeta meta = file.getMeta();
        if (meta == null) return;
        if (layer >= meta.getNrLayers()) return;
        this.selectedLayer = layer;
        update();
    }

    public void update() {
        updater.submit(this);
    }

    @Override
    public void run() {
        if (file == null) return;
        if (file.getMeta() == null) {
            file.parse();
            meta = file.getMeta();
        }
        Intent intent;
        if ((file.getPreview() != null) && (previewImg == null)) {
            PhotonFilePreview preview = file.getPreview();
            if (preview != null) {
                previewImg = Bitmap.createBitmap(preview.getImage(), preview.getImgWidth(), preview.getImgHeight(), Bitmap.Config.ARGB_8888);
                intent = new Intent();
                intent.setAction(PREVIEW_BROADCAST);
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
            }
        }
        if (meta != null) {
            intent = new Intent();
            intent.setAction(META_BROADCAST);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

            PhotonLayer pl = file.getLayers().get(selectedLayer);
            if (pl != null) pl.setBackgroundColor(0xFF000000);
            int[] layerImage = file.getLayerImage(selectedLayer);
            if (layerImage != null) {
                layerImg = Bitmap.createBitmap(layerImage, meta.getScreenWidth(), meta.getScreenHeight(), Bitmap.Config.ARGB_8888);
                intent = new Intent();
                intent.setAction(LAYER_BROADCAST);
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
            }
        }
    }

    public Bitmap getPreviewImg() {
        return previewImg;
    }

    public Bitmap getLayerImg() {
        return layerImg;
    }

    public PhotonFileMeta getMeta() {
        return meta;
    }

    public void close() {
        if (file == null) return;
        file.close();
        file = null;
    }
}
