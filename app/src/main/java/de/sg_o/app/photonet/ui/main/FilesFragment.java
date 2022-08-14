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

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import de.sg_o.app.photonet.MainActivity;
import de.sg_o.app.photonet.R;
import de.sg_o.app.photonet.menu.FilesAdapter;
import de.sg_o.lib.photoNet.printer.RootFolder;

public class FilesFragment extends Fragment{
    RootFolder rFolder;
    FilesAdapter adapter;

    public static FilesFragment newInstance(int i) {
        FilesFragment detailsFragment = new FilesFragment();
        Bundle args = new Bundle();
        args.putInt("printerInt", i);
        detailsFragment.setArguments(args);
        return detailsFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int i = 0;
        if (getArguments() != null) {
            i = getArguments().getInt("printerInt", 0);
        }
        rFolder = MainActivity.ENVIRONMENT.getConnected().get(i).getRootFolder();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState){
        View v = inflater.inflate(R.layout.fragment_files, viewGroup, false);
        Context context = getActivity();
        RecyclerView recyclerView = v.findViewById(R.id.m_files);
        recyclerView.setLayoutManager(new WrapContentLinearLayoutManager(context));
        adapter = new FilesAdapter(context, rFolder, v.findViewById(R.id.swipeRefreshLayout));
        recyclerView.setAdapter(adapter);
        return v;
    }

    public static class WrapContentLinearLayoutManager extends LinearLayoutManager {
        public WrapContentLinearLayoutManager(Context context) {
            super(context);
        }

        @SuppressWarnings("unused")
        public WrapContentLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
            super(context, orientation, reverseLayout);
        }

        @SuppressWarnings("unused")
        public WrapContentLinearLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
            super(context, attrs, defStyleAttr, defStyleRes);
        }


        @Override
        public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
            try {
                super.onLayoutChildren(recycler, state);
            } catch (IndexOutOfBoundsException ignored) {
            }
        }
    }
}
