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

package de.sg_o.app.photonet.menu;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import de.sg_o.app.photonet.DetailsActivity;
import de.sg_o.app.photonet.R;
import de.sg_o.lib.photoNet.netData.FileListItem;
import de.sg_o.lib.photoNet.netData.FolderList;
import de.sg_o.lib.photoNet.printFile.PrintFileMeta;
import de.sg_o.lib.photoNet.printFile.PrintFilePreview;
import de.sg_o.lib.photoNet.printer.Folder;

public class FilesAdapter extends RecyclerView.Adapter<FilesAdapter.ViewHolder> {

    private Folder folder;
    private Map.Entry<String, FileListItem>[] items;
    private final HashMap<Integer, Bitmap> bitmaps;
    private final LayoutInflater mInflater;
    private final SwipeRefreshLayout swipeRefreshLayout;
    private final Context context;

    private int mExpandedPosition = -1;
    private int previousExpandedPosition = -1;

    // data is passed into the constructor
    public FilesAdapter(Context context, Folder data, SwipeRefreshLayout swipeRefreshLayout) {
        this.context = context;
        this.mInflater = LayoutInflater.from(context);
        this.folder = data;
        this.bitmaps = new HashMap<>();
        this.swipeRefreshLayout = swipeRefreshLayout;
        swipeRefreshLayout.setEnabled(true);
        swipeRefreshLayout.setRefreshing(true);
        swipeRefreshLayout.setOnRefreshListener(() -> new Refresh().execute());
        new Refresh().execute();
    }

    // inflates the row layout from xml when needed
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_file, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int p) {
        if (items == null) return;
        int position = holder.getAdapterPosition();
        String name = items[position].getValue().getName();
        long size = items[position].getValue().getSize();
        boolean supportsDownload = items[position].getValue().supportsDownload();
        if (name == null) name = "";
        holder.entryName.setText(name);
        holder.fileSize.setText(formatSize(size));
        final boolean isExpanded = position==mExpandedPosition;
        holder.details.setVisibility(isExpanded?View.VISIBLE:View.GONE);
        holder.itemView.setActivated(isExpanded);
        holder.download.setVisibility(supportsDownload?View.VISIBLE:View.GONE);

        if (bitmaps.containsKey(position)) {
            holder.filePreview.setImageBitmap(bitmaps.get(position));
        } else {
            holder.filePreview.setImageResource(R.drawable.ic_baseline_broken_image_48);
        }

        if (isExpanded) previousExpandedPosition = position;

        if (items[position].getValue().isFolder()) {
            holder.entryName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_folder_48, 0, 0, 0);
        } else {
            holder.entryName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_printer_3d_48, 0, 0, 0);
        }

        holder.itemView.setOnClickListener(v -> {
            FileListItem selected = items[position].getValue();
            if (selected.isFolder()) {
                mExpandedPosition = -1;
                notifyItemChanged(previousExpandedPosition);
                Folder tmp = selected.getFolder();
                if (tmp == null) return;
                folder = tmp;
                swipeRefreshLayout.setRefreshing(true);
                new Refresh().execute();
            } else {
                new LoadPreview().execute(position);
                mExpandedPosition = isExpanded ? -1 : position;
                notifyItemChanged(previousExpandedPosition);
                notifyItemChanged(position);
            }
        });

        holder.delete.setOnClickListener(v -> {
            if (!items[position].getValue().isFolder()) {
                items[position].getValue().delete();
                new Refresh().execute();
            }
        });

        holder.print.setOnClickListener(v -> {
            if (!items[position].getValue().isFolder()) {
                items[position].getValue().print();
            }
        });

        holder.download.setOnClickListener(v -> {
            FileListItem file = items[position].getValue();
            if (!file.isFolder()) {
                Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                String[] split = file.getName().split("\\.");
                if (split.length < 2) return;
                intent.setType("model/x." + split[split.length - 1]);
                intent.putExtra(Intent.EXTRA_TITLE, file.getName());
                DetailsActivity.toDownload = file;
                ((Activity) context).startActivityForResult(intent, 1);
            }
        });
    }

    // total number of rows
    @Override
    public int getItemCount() {
        if (items == null) return 0;
        return items.length;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView entryName;
        private final LinearLayout details;
        private final TextView fileSize;
        private final ImageView filePreview;

        private final ImageButton delete;
        private final ImageButton download;
        private final ImageButton print;

        ViewHolder(View itemView) {
            super(itemView);
            entryName = itemView.findViewById(R.id.entryName);
            details = itemView.findViewById(R.id.fileDetails);
            fileSize = itemView.findViewById(R.id.file_size);
            filePreview = itemView.findViewById(R.id.file_preview);

            delete = itemView.findViewById(R.id.file_delete);
            download = itemView.findViewById(R.id.file_download);
            print = itemView.findViewById(R.id.file_print);
        }
    }

    // convenience method for getting data at click position
    @SuppressWarnings("unused")
    public FileListItem getItem(int id) {
        if (items == null) return null;
        return items[id].getValue();
    }

    private class LoadPreview extends AsyncTask<Integer, Integer, Integer> {
        private int entry;


        protected Integer doInBackground(Integer... entry) {
            this.entry = entry[0];
            if (items == null) return 0;
            try {
                PrintFileMeta meta = items[this.entry].getValue().getMeta();
                PrintFilePreview preview =  items[this.entry].getValue().getPreview(meta.getPreviewHeaderOffset());
                Bitmap img = Bitmap.createBitmap(preview.getImage(), preview.getImgWidth(), preview.getImgHeight(), Bitmap.Config.ARGB_8888);
                bitmaps.put(this.entry, img);
            } catch (Exception ignored) {
                return 0;
            }
            return 1;
        }
        protected void onPostExecute(Integer result) {
            if (result == 1) {
                notifyItemChanged(this.entry);
            }
        }
    }

    public Folder getFolder() {
        return folder;
    }

    private String formatSize(long size) {
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(2);
        if (size < 0) size = 0;
        if (size > 1000000000L) {
            return df.format((float)size / 1000000000.0f) + " GB";
        }
        if (size > 1000000L) {
            return df.format((float)size / 1000000.0f) + " MB";
        }
        if (size > 1000L) {
            return df.format((float)size / 1000.0f) + " KB";
        }
        return size + " B";
    }

    private class Refresh extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... voids) {
            try {
                folder.update();
                while (!folder.isUpToDate()) {
                    Thread.sleep(100);
                }
                FolderList folderList = folder.getFolderList();
                if (folderList != null) {
                    Set<Map.Entry<String, FileListItem>> entrySet = folderList.getItems().entrySet();
                    items = entrySet.toArray(new Map.Entry[entrySet.size()]);
                }
            } catch (UnsupportedEncodingException | InterruptedException ignored) {
            }
            return null;
        }
        protected void onPostExecute(Void result) {
            swipeRefreshLayout.setRefreshing(false);
            notifyDataSetChanged();
        }
    }
}