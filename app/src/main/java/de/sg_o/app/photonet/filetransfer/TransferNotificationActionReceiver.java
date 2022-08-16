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

package de.sg_o.app.photonet.filetransfer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.content.ContextCompat;

public class TransferNotificationActionReceiver extends BroadcastReceiver {
    public static final String PAUSE_ACTION = "pause";
    public static final String RESUME_ACTION = "resume";
    public static final String STOP_ACTION = "stop";
    public static final String RETRY_ACTION = "retry";
    public static final String IGNORE_ACTION = "ignore";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Intent forward = new Intent(context, TransferService.class);
        forward.setAction(action);
        ContextCompat.startForegroundService(context, forward);
    }
}
