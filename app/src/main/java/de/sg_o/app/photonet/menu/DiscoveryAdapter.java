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
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.Map;
import java.util.Set;

import de.sg_o.app.photonet.MainActivity;
import de.sg_o.app.photonet.R;
import de.sg_o.lib.photoNet.printer.Discover;

public class DiscoveryAdapter extends RecyclerView.Adapter<DiscoveryAdapter.ViewHolder> {

    private final Discover mDiscover;
    private Map.Entry<String, String>[] mEntries;
    private final LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private final SwipeRefreshLayout swipeRefreshLayout;

    // data is passed into the constructor
    public DiscoveryAdapter(Context context, SwipeRefreshLayout swipeRefreshLayout) {
        this.mInflater = LayoutInflater.from(context);
        this.swipeRefreshLayout = swipeRefreshLayout;
        swipeRefreshLayout.setRefreshing(true);
        swipeRefreshLayout.setOnRefreshListener(() -> new Refresh().execute());
        mDiscover = MainActivity.ENVIRONMENT.getDiscover();
        new DiscoveryAdapter.Refresh().execute();
    }

    // inflates the row layout from xml when needed
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_discover, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (mEntries == null) return;
        if (position >= mEntries.length) return;
        String name = mEntries[position].getValue();
        String ip = mEntries[position].getKey();
        holder.name.setText(name);
        holder.ip.setText(ip);
    }

    // total number of rows
    @Override
    public int getItemCount() {
        if (mEntries == null) return 0;
        return mEntries.length;
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView name;
        TextView ip;

        ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.d_name);
            ip = itemView.findViewById(R.id.d_ip);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // convenience method for getting data at click position
    public String getItem(int id) {
        if (id >= mEntries.length) return null;
        return mEntries[id].getKey();
    }

    // allows clicks events to be caught
    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int p);
    }

    private class Refresh extends AsyncTask<Void, Void, Void> {
        private int entry;

        protected Void doInBackground(Void... voids) {
            mDiscover.update();
            while (mDiscover.isRunning()) {
            }
            Set<Map.Entry<String, String>> entrySet = MainActivity.ENVIRONMENT.notConnected().entrySet();
            mEntries = entrySet.toArray(new Map.Entry[entrySet.size()]);
            return null;
        }
        protected void onPostExecute(Void result) {
            swipeRefreshLayout.setRefreshing(false);
            notifyDataSetChanged();
        }
    }
}