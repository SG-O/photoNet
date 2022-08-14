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

import android.content.Context;
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
import java.util.HashMap;
import java.util.LinkedList;

import de.sg_o.app.photonet.R;
import de.sg_o.lib.photoNet.netData.FileListItem;
import de.sg_o.lib.photoNet.photonFile.PhotonFileMeta;
import de.sg_o.lib.photoNet.photonFile.PhotonFilePreview;
import de.sg_o.lib.photoNet.printer.RootFolder;

public class FilesAdapter extends RecyclerView.Adapter<FilesAdapter.ViewHolder> {

    private final RootFolder mData;
    private final HashMap<Integer, Bitmap> bitmaps;
    private final LayoutInflater mInflater;
    private final SwipeRefreshLayout swipeRefreshLayout;

    private int mExpandedPosition = -1;
    private int previousExpandedPosition = -1;

    // data is passed into the constructor
    public FilesAdapter(Context context, RootFolder data, SwipeRefreshLayout swipeRefreshLayout) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        this.bitmaps = new HashMap<>();
        this.swipeRefreshLayout = swipeRefreshLayout;
        swipeRefreshLayout.setEnabled(true);
        swipeRefreshLayout.setOnRefreshListener(() -> new Refresh().execute());
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
        if (mData == null) return;
        if (mData.getFolder() == null) return;
        LinkedList<FileListItem> items = mData.getFolder().getItems();
        if (items == null) return;
        int position = holder.getAdapterPosition();
        String name = items.get(position).getName();
        String path = items.get(position).getFullPath();
        if (name == null) name = "";
        if (path == null) path = "";
        holder.entryName.setText(name);
        holder.path.setText(path);
        final boolean isExpanded = position==mExpandedPosition;
        holder.details.setVisibility(isExpanded?View.VISIBLE:View.GONE);
        holder.itemView.setActivated(isExpanded);

        if (bitmaps.containsKey(position)) {
            holder.filePreview.setImageBitmap(bitmaps.get(position));
        } else {
            holder.filePreview.setImageResource(R.drawable.baseline_broken_image_black_48);
        }

        if (isExpanded) previousExpandedPosition = position;

        if (items.get(position).isFolder()) {
            holder.entryName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_folder_black_48, 0, 0, 0);
        } else {
            holder.entryName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.printer_3d_48, 0, 0, 0);
        }

        holder.itemView.setOnClickListener(v -> {
            if (items.get(position).isFolder()) {
                mExpandedPosition = -1;
                notifyItemChanged(previousExpandedPosition);
            } else {
                new LoadPreview().execute(position);
                mExpandedPosition = isExpanded ? -1 : position;
                notifyItemChanged(previousExpandedPosition);
                notifyItemChanged(position);
            }
        });

        holder.delete.setOnClickListener(v -> {
            if (!items.get(position).isFolder()) {
                try {
                    items.get(position).delete();
                    new Refresh().execute();
                } catch (UnsupportedEncodingException ignored) {
                }
            }
        });

        holder.print.setOnClickListener(v -> {
            if (!items.get(position).isFolder()) {
                try {
                    items.get(position).print();
                } catch (UnsupportedEncodingException ignored) {
                }
            }
        });
    }

    // total number of rows
    @Override
    public int getItemCount() {
        if (mData == null) return 0;
        if (mData.getFolder() == null) return 0;
        LinkedList<FileListItem> items = mData.getFolder().getItems();
        if (items == null) return 0;
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView entryName;
        LinearLayout details;
        TextView path;
        ImageView filePreview;

        ImageButton delete;
        ImageButton download;
        ImageButton print;

        ViewHolder(View itemView) {
            super(itemView);
            entryName = itemView.findViewById(R.id.entryName);
            details = itemView.findViewById(R.id.fileDetails);
            path = itemView.findViewById(R.id.path);
            filePreview = itemView.findViewById(R.id.file_preview);

            delete = itemView.findViewById(R.id.file_delete);
            download = itemView.findViewById(R.id.file_download);
            print = itemView.findViewById(R.id.file_print);
        }
    }

    // convenience method for getting data at click position
    @SuppressWarnings("unused")
    public FileListItem getItem(int id) {
        if (mData == null) return null;
        if (mData.getFolder() == null) return null;
        LinkedList<FileListItem> items = mData.getFolder().getItems();
        if (items == null) return null;
        return items.get(id);
    }

    private class LoadPreview extends AsyncTask<Integer, Integer, Integer> {
        private int entry;


        protected Integer doInBackground(Integer... entry) {
            this.entry = entry[0];
            if (mData == null) return 0;
            if (mData.getFolder() == null) return 0;
            LinkedList<FileListItem> items = mData.getFolder().getItems();
            if (items == null) return 0;
            try {
                PhotonFileMeta meta = items.get(this.entry).getMeta();
                PhotonFilePreview preview =  items.get(this.entry).getPreview(meta.getPreviewHeaderOffset());
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

    private class Refresh extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... voids) {
            try {
                mData.update();
                while (!mData.isUpToDate()) {
                }
            } catch (UnsupportedEncodingException ignored) {
            }
            return null;
        }
        protected void onPostExecute(Void result) {
            swipeRefreshLayout.setRefreshing(false);
            notifyDataSetChanged();
        }
    }
}