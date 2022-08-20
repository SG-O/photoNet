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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import de.sg_o.app.photonet.FilePreviewActivity;
import de.sg_o.app.photonet.MainActivity;
import de.sg_o.app.photonet.R;
import de.sg_o.app.photonet.menu.FilesAdapter;
import de.sg_o.lib.photoNet.printer.Folder;
import de.sg_o.lib.photoNet.printer.RootFolder;

public class FilesFragment extends Fragment{
    public static final String EXTRA_FILE_URI = "de.sg_o.app.FileUri";
    public static final String EXTRA_FILE_FOLDER = "de.sg_o.app.Folder";

    RootFolder rFolder;
    FilesAdapter adapter;

    ActivityResultLauncher<Intent> uploadResultLauncher;

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

        uploadResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // There are no request codes
                        if (adapter == null) return;
                        Folder folder = adapter.getFolder();
                        if (folder == null) return;
                        if (result.getData() != null) {
                            Uri uri = result.getData().getData();
                            if (getActivity() == null) return;
                            Intent intent = new Intent(getActivity(), FilePreviewActivity.class);
                            intent.putExtra(EXTRA_FILE_URI, uri);
                            intent.putExtra(EXTRA_FILE_FOLDER, folder);
                            startActivity(intent);
                        }
                    }
                });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState){
        View v = inflater.inflate(R.layout.fragment_files, viewGroup, false);
        Context context = getActivity();
        RecyclerView recyclerView = v.findViewById(R.id.m_files);
        recyclerView.setLayoutManager(new WrapContentLinearLayoutManager(context));
        adapter = new FilesAdapter(context, rFolder, v.findViewById(R.id.files_swipeRefreshLayout));
        FloatingActionButton fab = v.findViewById(R.id.files_upload);
        fab.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            uploadResultLauncher.launch(intent);
        });
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
