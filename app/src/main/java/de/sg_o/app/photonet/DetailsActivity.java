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

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import java.io.FileNotFoundException;
import java.io.OutputStream;

import de.sg_o.app.photonet.filetransfer.TransferService;
import de.sg_o.app.photonet.filetransfer.TransferWorker;
import de.sg_o.app.photonet.menu.SectionsPagerAdapter;
import de.sg_o.lib.photoNet.netData.FileListItem;

public class DetailsActivity extends AppCompatActivity {
    public static FileListItem toDownload = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        int i = intent.getIntExtra(MainActivity.EXTRA_MESSAGE, 0);

        setContentView(R.layout.activity_details);
        Toolbar toolbar = findViewById(R.id.details_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        sectionsPagerAdapter.setI(i);
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);
        Uri uri;
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            if (resultData != null) {
                uri = resultData.getData();
                writeFileContent(uri);
            }
        }
    }

    private void writeFileContent(Uri uri)
    {
        if (toDownload == null) return;
        FileListItem tmp = toDownload;
        toDownload = null;
        try {
            ParcelFileDescriptor pfd = getContentResolver().openFileDescriptor(uri, "w");
            OutputStream fileOutputStream = new ParcelFileDescriptor.AutoCloseOutputStream(pfd);
            TransferWorker.addTransfer(tmp.getDownload(fileOutputStream, 2));
            TransferService.startService(this);
        } catch (FileNotFoundException ignore) {
        }
    }
}