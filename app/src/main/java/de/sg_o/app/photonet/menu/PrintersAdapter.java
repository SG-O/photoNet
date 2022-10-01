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
import android.content.res.Resources;
import android.util.AttributeSet;
import android.util.Xml;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.vectordrawable.graphics.drawable.SeekableAnimatedVectorDrawable;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

import de.sg_o.app.photonet.R;
import de.sg_o.lib.photoNet.manager.Environment;
import de.sg_o.lib.photoNet.netData.Status;
import de.sg_o.lib.photoNet.printer.Printer;

public class PrintersAdapter extends RecyclerView.Adapter<PrintersAdapter.ViewHolder> {

    private final Environment mData;
    private final LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private ItemLongClickListener mLongClickListener;
    private final Context context;

    // data is passed into the constructor
    public PrintersAdapter(Context context, Environment data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        this.context = context;
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
        float progress = mData.getConnected().get(position).getStatus().getProgress();
        holder.name.setText(name);
        holder.ip.setText(ip);
        holder.status.setText(status.name());
        if (holder.curStat != status) {
            if (context != null) {
                int animateTime = 100;
                Resources.Theme theme = new ContextThemeWrapper(context, R.style.Theme_PhotoNet_PrinterStatus_Offline).getTheme();
                if (status == Status.State.PRINTING || status == Status.State.PAUSE) {
                    animateTime = Math.max(Math.min(Math.round(progress * 100.0f), 100), 0);
                    theme = new ContextThemeWrapper(context, R.style.Theme_PhotoNet_PrinterStatus_Printing).getTheme();
                } else {
                    if (status == Status.State.FINISHED) {
                        theme = new ContextThemeWrapper(context, R.style.Theme_PhotoNet_PrinterStatus_Finished).getTheme();
                    }
                    if (status == Status.State.IDLE) {
                        theme = new ContextThemeWrapper(context, R.style.Theme_PhotoNet_PrinterStatus_Idle).getTheme();
                    }
                }

                try {
                    //noinspection AndroidLintResourceType - Parse drawable as XML.
                    final XmlPullParser parser = context.getResources().getXml(R.drawable.ic_printer);
                    final AttributeSet attrs = Xml.asAttributeSet(parser);
                    int type;
                    do {
                        type = parser.next();
                    } while (type != XmlPullParser.START_TAG && type != XmlPullParser.END_DOCUMENT);
                    if (type != XmlPullParser.START_TAG) {
                        throw new XmlPullParserException("");
                    }
                    SeekableAnimatedVectorDrawable drawable = SeekableAnimatedVectorDrawable.createFromXmlInner(context.getResources(), parser, attrs, theme);
                    drawable.setCurrentPlayTime(animateTime);
                    holder.thumb.setImageDrawable(drawable);
                } catch (XmlPullParserException | IOException ignore) {
                }
            }
            holder.curStat = status;
        }
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.getConnected().size();
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        private final TextView name;
        private final TextView ip;
        private final TextView status;
        private final ImageView thumb;

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