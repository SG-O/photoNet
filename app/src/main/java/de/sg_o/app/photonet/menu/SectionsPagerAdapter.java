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

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import de.sg_o.app.photonet.ui.main.DetailsFragment;
import de.sg_o.app.photonet.ui.main.FilesFragment;
import de.sg_o.app.photonet.ui.main.ManualControlFragment;

public class SectionsPagerAdapter extends FragmentStateAdapter {
    int i;

    public SectionsPagerAdapter(FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    public void setI(int i) {
        this.i = i;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        DetailsFragment details = DetailsFragment.newInstance(i);
        if (position == 1) {
            return FilesFragment.newInstance(i);
        }
        if (position == 2) {
            return ManualControlFragment.newInstance(i);
        }
        return details;
    }

    @Override
    public int getItemCount() {
        // Show 3 total pages.
        return 3;
    }
}