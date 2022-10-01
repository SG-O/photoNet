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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.io.FileNotFoundException;
import java.io.OutputStream;

import de.sg_o.app.photonet.fileTransfer.TransferService;
import de.sg_o.app.photonet.fileTransfer.TransferWorker;
import de.sg_o.app.photonet.menu.SectionsPagerAdapter;
import de.sg_o.lib.photoNet.netData.FileListItem;

public class DetailsActivity extends AppCompatActivity {
    @StringRes
    private static final int[] TAB_TITLES = new int[]{R.string.tab_text_1, R.string.tab_text_2, R.string.tab_text_3};

    public static final String PRINTER_ID = "printer_id";
    private static int lastUsed = 0;

    public static FileListItem toDownload = null;
    private int printerID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        printerID = intent.getIntExtra(MainActivity.EXTRA_MESSAGE, -1);

        if (printerID < 0) {
            if (savedInstanceState != null) {
                printerID = savedInstanceState.getInt("Printer_ID", -1);
            }
            if (printerID < 0) printerID = lastUsed;
        }
        lastUsed = printerID;
        setContentView(R.layout.activity_details);
        Toolbar toolbar = findViewById(R.id.details_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this);
        sectionsPagerAdapter.setI(printerID);
        ViewPager2 viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        new TabLayoutMediator(tabs, viewPager,
                (tab, position) -> tab.setText(TAB_TITLES[position])
        ).attach();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.details_menu_settings) {
            Intent intent = new Intent(this, PrinterSettingsActivity.class);
            intent.putExtra(PRINTER_ID, printerID);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
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

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("Printer_ID", printerID);
    }
}