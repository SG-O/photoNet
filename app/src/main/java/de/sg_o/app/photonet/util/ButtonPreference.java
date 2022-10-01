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

package de.sg_o.app.photonet.util;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

import de.sg_o.app.photonet.R;

public class ButtonPreference extends Preference {
    private CharSequence text;
    private View.OnClickListener onClickListener;
    private Button button = null;

    public ButtonPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.setWidgetLayoutResource(R.layout.pref_button);
        final TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs, R.styleable.PreferenceButton, defStyleAttr, defStyleRes);
        text = a.getText(R.styleable.PreferenceButton_button_text);
        a.recycle();
    }

    public ButtonPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ButtonPreference(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.preferenceStyle);
    }

    @SuppressWarnings("unused")
    public ButtonPreference(Context context) {
        this(context, null);
    }

    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        button = (Button) holder.findViewById(R.id.pref_button_button);
        button.setOnClickListener(onClickListener);
        button.setText(text);
    }

    public void setText(String text) {
        this.text = text;
        if (button != null) {
            button.setText(text);
        }
    }

    public void setOnClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
        if (button != null) {
            button.setOnClickListener(onClickListener);
        }
    }
}
