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

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import de.sg_o.app.photonet.menu.PrintersAdapter;
import de.sg_o.app.photonet.ui.main.DiscoveryDialogFragment;
import de.sg_o.app.photonet.ui.main.PrinterEditDialogFragment;
import de.sg_o.lib.photoNet.manager.Environment;
import de.sg_o.lib.photoNet.printer.Printer;


public class MainActivity extends AppCompatActivity implements PrintersAdapter.ItemClickListener, PrintersAdapter.ItemLongClickListener{
    public static final String EXTRA_MESSAGE = "de.sg_o.app.PrinterID";
    public static PrintersAdapter ADAPTER;

    public static Environment ENVIRONMENT;
    public static SharedPreferences PREFS;
    Handler handler = new Handler();
    MainActivity main;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        main = this;
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        PREFS = this.getSharedPreferences("PRINTER", Context.MODE_PRIVATE);

        String savedPrinters = PREFS.getString("connected",null);
        ENVIRONMENT = new Environment(savedPrinters, 2000);

        RecyclerView recyclerView = findViewById(R.id.m_printers);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        ADAPTER = new PrintersAdapter(this, ENVIRONMENT);
        recyclerView.setAdapter(ADAPTER);
        ADAPTER.setClickListener(this);
        ADAPTER.setLongClickListener(this);

        FragmentManager fm = getSupportFragmentManager();
        DiscoveryDialogFragment discoveryDialogFragment = new DiscoveryDialogFragment();

        FloatingActionButton add_new = findViewById(R.id.add_new);
        add_new.setOnClickListener(view -> discoveryDialogFragment.show(fm, "discovery"));
        handler.postDelayed(updatePrinters, 2000);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_settings) {
            return true;
        } else if (itemId == R.id.action_about) {
            Intent intent = new Intent(main, AboutActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ADAPTER.notifyDataSetChanged();
        boolean licAccept = PREFS.getBoolean("licAccept", false);
        if (!licAccept) {
            Intent intent = new Intent(main, LicenseActivity.class);
            startActivity(intent);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(updatePrinters);
    }

    private final Runnable updatePrinters = new Runnable() {
        @Override
        public void run() {
            for (int i = 0; i < ADAPTER.getItemCount(); i++) {
                if (ADAPTER.getItem(i).getStatus().isUpdated()) {
                    ADAPTER.notifyItemChanged(i);
                }
            }
            handler.postDelayed(this, 2000);
        }
    };

    @Override
    public void onItemClick(View view, int p) {
        Intent intent = new Intent(main, DetailsActivity.class);
        intent.putExtra(EXTRA_MESSAGE, p);
        startActivity(intent);
    }

    @Override
    public void onItemLongClick(View view, int p) {
        Printer selected = ADAPTER.getItem(p);
        PrinterEditDialogFragment printerEditDialogFragment = new PrinterEditDialogFragment(selected);
        FragmentManager fm = getSupportFragmentManager();
        printerEditDialogFragment.show(fm, "edit" + p);
    }
}