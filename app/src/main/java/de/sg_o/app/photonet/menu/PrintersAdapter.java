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
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.RecyclerView;

import de.sg_o.app.photonet.R;
import de.sg_o.lib.photoNet.manager.Environment;
import de.sg_o.lib.photoNet.netData.Status;
import de.sg_o.lib.photoNet.printer.Printer;

public class PrintersAdapter extends RecyclerView.Adapter<PrintersAdapter.ViewHolder> {

    private final Environment mData;
    private final LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private ItemLongClickListener mLongClickListener;
    private final Drawable offline;
    private final Drawable online;

    // data is passed into the constructor
    public PrintersAdapter(Context context, Environment data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        offline = AppCompatResources.getDrawable(context, R.drawable.offline_thumb);
        online = AppCompatResources.getDrawable(context, R.drawable.main_thumb);
    }

    // inflates the row layout from xml when needed
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_printer, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String name = mData.getConnected().get(position).getName();
        String ip = mData.getConnected().get(position).getIp();
        Status.State status = mData.getConnected().get(position).getStatus().getState();
        holder.name.setText(name);
        holder.ip.setText(ip);
        holder.status.setText(status.name());
        if (holder.curStat != status) {
            holder.curStat = status;
            if (status == Status.State.OFFLINE || status == Status.State.UNKNOWN) {
                holder.thumb.setImageDrawable(offline);
            } else {
                holder.thumb.setImageDrawable(online);
            }
        }
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.getConnected().size();
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        TextView name;
        TextView ip;
        TextView status;
        ImageView thumb;

        Status.State curStat = Status.State.UNKNOWN;

        ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            ip = itemView.findViewById(R.id.ip);
            status = itemView.findViewById(R.id.status);
            thumb = itemView.findViewById(R.id.imageThumb);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }


        @Override
        public boolean onLongClick(View view) {
            if (mLongClickListener != null) mLongClickListener.onItemLongClick(view, getAdapterPosition());
            return true;
        }
    }

    // convenience method for getting data at click position
    public Printer getItem(int id) {
        return mData.getConnected().get(id);
    }

    // allows clicks events to be caught
    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    public void setLongClickListener(ItemLongClickListener itemLongClickListener) {
        this.mLongClickListener = itemLongClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int p);
    }

    public interface ItemLongClickListener {
        void onItemLongClick(View view, int p);
    }
}