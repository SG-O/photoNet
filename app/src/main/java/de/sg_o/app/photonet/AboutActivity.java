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

import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

import de.sg_o.app.photonet.databinding.ActivityAboutBinding;
import de.sg_o.app.photonet.menu.DependencyAdapter;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        de.sg_o.app.photonet.databinding.ActivityAboutBinding binding = ActivityAboutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.aboutToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }
        TextView contributors = findViewById(R.id.about_contributors_list);

        Resources res = this.getResources();
        InputStream inputStream = res.openRawResource(R.raw.all_contributorsrc);
        Writer writer = new StringWriter();
        char[] buffer = new char[1024];
        try {
            Reader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
        } catch (IOException ignore) {
        } finally {
            try {
                inputStream.close();
            } catch (IOException ignore) {
            }
        }

        StringBuilder builder = new StringBuilder();

        try {
            JSONObject reader = new JSONObject(writer.toString());
            JSONArray con = reader.getJSONArray("contributors");
            for (int i = 0; i < con.length(); i++) {
                JSONObject c = con.getJSONObject(i);
                builder.append(c.getString("name"));
                builder.append(" (");
                builder.append(c.getString("login"));
                builder.append(")\n");
            }
        } catch (JSONException ignore) {
        }

        contributors.setText(builder.toString().trim());

        TextView sgo = findViewById(R.id.about_sgo);
        TextView version = findViewById(R.id.about_version);
        version.setText(String.format("v%s", BuildConfig.VERSION_NAME));
        RecyclerView recyclerView = findViewById(R.id.about_dependencies);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        DependencyAdapter adapter = new DependencyAdapter(this);
        recyclerView.setAdapter(adapter);
        sgo.setOnClickListener(view -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.sgo_web)));
            startActivity(browserIntent);
        });
        LinearLayout license = findViewById(R.id.about_license);
        license.setOnClickListener(view -> {
            Intent intent = new Intent(this, LicenseActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}