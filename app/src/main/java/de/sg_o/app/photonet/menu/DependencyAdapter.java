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
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;

import de.sg_o.app.photonet.R;

public class DependencyAdapter extends RecyclerView.Adapter<DependencyAdapter.ViewHolder> {

    private ArrayList<Dependency> mData;
    private final LayoutInflater mInflater;
    private final Context context;

    public DependencyAdapter(Context context) {
        this.context = context;
        this.mInflater = LayoutInflater.from(context);
        try {
            this.mData = parse(context);
        } catch (XmlPullParserException | IOException ignored) {
            this.mData = new ArrayList<>();
        }
    }

    public synchronized ArrayList<Dependency> parse(Context context) throws XmlPullParserException, IOException {
        ArrayList<Dependency> data = new ArrayList<>();
        Resources res = context.getResources();
        XmlResourceParser xrp = res.getXml(R.xml.dependencies);
        xrp.next();
        int eventType = xrp.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT)
        {
            if(eventType == XmlPullParser.START_TAG)
            {
                if (xrp.getName().equals("dependency")) {
                    data.add(new Dependency(xrp));
                }
            }
            eventType = xrp.next();
        }
        return data;
    }

    // inflates the row layout from xml when needed
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_dependency, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.name.setText(mData.get(position).getName());
        holder.license.setText(mData.get(position).getLicense());
        holder.web.setText(mData.get(position).getWeb());
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView name;
        private final TextView license;
        private final TextView web;

        ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.dep_name);
            license = itemView.findViewById(R.id.dep_license);
            web = itemView.findViewById(R.id.dep_web);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getItem(getAdapterPosition()).getWeb()));
            context.startActivity(browserIntent);
        }
    }

    // convenience method for getting data at click position
    public Dependency getItem(int id) {
        if (id >= mData.size()) return null;
        return mData.get(id);
    }
}