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

import static android.app.PendingIntent.FLAG_IMMUTABLE;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import java.text.DecimalFormat;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.sg_o.app.photonet.MainActivity;
import de.sg_o.app.photonet.R;
import de.sg_o.lib.photoNet.netData.DataTransfer;

public class TransferService extends Service {
    public static final String CHANNEL_ID = "FileTransferServiceChannel";
    private static final int PROGRESS_MAX = 100;
    private static final int NOTIFICATION_ID = 1;
    private static TransferService service;

    TransferWorker worker;
    ExecutorService executor;
    ExecutorService updater;

    PendingIntent pauseIntent;
    PendingIntent resumeIntent;
    PendingIntent stopIntent;
    PendingIntent retryIntent;
    PendingIntent ignoreIntent;

    @Override
    public void onCreate() {
        super.onCreate();
        service = this;

        Intent pause = new Intent(this, TransferNotificationActionReceiver.class);
        pause.setAction(TransferNotificationActionReceiver.PAUSE_ACTION);
        pauseIntent = PendingIntent.getBroadcast(this, 0, pause, FLAG_IMMUTABLE);

        Intent resume = new Intent(this, TransferNotificationActionReceiver.class);
        resume.setAction(TransferNotificationActionReceiver.RESUME_ACTION);
        resumeIntent = PendingIntent.getBroadcast(this, 0, resume, FLAG_IMMUTABLE);

        Intent stop = new Intent(this, TransferNotificationActionReceiver.class);
        stop.setAction(TransferNotificationActionReceiver.STOP_ACTION);
        stopIntent = PendingIntent.getBroadcast(this, 0, stop, FLAG_IMMUTABLE);

        Intent retry = new Intent(this, TransferNotificationActionReceiver.class);
        retry.setAction(TransferNotificationActionReceiver.RETRY_ACTION);
        retryIntent = PendingIntent.getBroadcast(this, 0, retry, FLAG_IMMUTABLE);

        Intent ignore = new Intent(this, TransferNotificationActionReceiver.class);
        ignore.setAction(TransferNotificationActionReceiver.IGNORE_ACTION);
        ignoreIntent = PendingIntent.getBroadcast(this, 0, ignore, FLAG_IMMUTABLE);

        createNotificationChannel();
        startForeground(NOTIFICATION_ID, getMyActivityNotification(0.0f, "", false, false, 0.0f));

        if (executor == null) {
            executor = Executors.newSingleThreadExecutor();
        }
        if (worker != null) {
            if (!worker.isRunning()) {
                worker = new TransferWorker();
                executor.submit(worker);
            }
        } else {
            worker = new TransferWorker();
            executor.submit(worker);
        }

        if (updater == null) {
            updater = Executors.newSingleThreadExecutor();
            updater.submit(updateNotification);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        if(TransferNotificationActionReceiver.PAUSE_ACTION.equals(action)) {
            worker.getTransfer().setPaused(true);
        } else if(TransferNotificationActionReceiver.RESUME_ACTION.equals(action)) {
            worker.getTransfer().setPaused(false);
        } else if(TransferNotificationActionReceiver.STOP_ACTION.equals(action)) {
            worker.stop();
        } else if(TransferNotificationActionReceiver.RETRY_ACTION.equals(action)) {
            worker.retry();
        } else if(TransferNotificationActionReceiver.IGNORE_ACTION.equals(action)) {
            worker.clearFailed();
        }

        return START_NOT_STICKY;
    }

    private final Runnable updateNotification = new Runnable() {
        @Override
        public void run() {
            while (worker.isRunning()) {
                DataTransfer transfer = worker.getTransfer();
                if (transfer == null) continue;
                NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                if (transfer.isRunning()) {
                    mNotificationManager.notify(NOTIFICATION_ID,
                            getMyActivityNotification(transfer.getProgress(), transfer.getName(), transfer.isPaused(), transfer.hasFailed(), transfer.getTransferSpeed()));
                } else {
                    if (worker.countPending() > 0) {
                        mNotificationManager.notify(NOTIFICATION_ID,
                                getMyActivityNotification(0, worker.getNext().getName(), false, false, 0.0f));
                    } else if (worker.hasFailed()){
                        mNotificationManager.notify(NOTIFICATION_ID,
                                getMyActivityNotification(worker.getFirstFailed().getProgress(), worker.getFirstFailed().getName(), false, true, 0.0f));
                    }
                }
            }
            stopService(service);
        }
    };

    private Notification getMyActivityNotification(float progress, String name, boolean paused, boolean failed, float speed){
        if (progress < 0.0f) progress = 0.0f;
        if (progress > 1.0f) progress = 1.0f;
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, FLAG_IMMUTABLE);
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.ic_launcher_foreground);

        NotificationCompat.Builder notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(getString(R.string.file_transfer_title))
                .setLargeIcon(bitmap)
                .setSmallIcon(R.drawable.ic_note_up)
                .setContentIntent(pendingIntent)
                .setProgress(PROGRESS_MAX, Math.round(progress*100), false);

        if (speed > 0.01) {
            notification.setContentText(name + "\n" + getFormattedTransferSpeed(speed));
        } else {
            notification.setContentText(name);
        }

        if (failed) {
            notification.addAction(R.drawable.ic_baseline_sync_24, getString(R.string.notification_retry), retryIntent);
            notification.addAction(R.drawable.ic_baseline_stop_24, getString(R.string.notification_ignore), ignoreIntent);
        } else {
            if (paused) {
                notification.addAction(R.drawable.ic_baseline_play_arrow_24, getString(R.string.notification_resume), resumeIntent);
            } else {
                notification.addAction(R.drawable.ic_baseline_pause_24, getString(R.string.notification_pause), pauseIntent);
            }
            notification.addAction(R.drawable.ic_baseline_stop_24, getString(R.string.notification_abort), stopIntent);
        }
        return notification.build();
    }

    private String getFormattedTransferSpeed(float transferSpeed) {
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(2);
        if (transferSpeed < 0.0f) transferSpeed = 0.0f;
        if (transferSpeed > 1000000.0f) {
            return df.format(transferSpeed / 1000000.0f) + " MB/s";
        }
        if (transferSpeed > 1000.0f) {
            return df.format(transferSpeed / 1000.0f) + " KB/s";
        }
        return df.format(transferSpeed) + " B/s";
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        worker.stop();
        executor.shutdown();
        updater.shutdown();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_LOW
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    public static void startService(Context context) {
        Intent serviceIntent = new Intent(context, TransferService.class);
        ContextCompat.startForegroundService(context, serviceIntent);
    }

    public static void stopService(Context context) {
        Intent serviceIntent = new Intent(context, TransferService.class);
        context.stopService(serviceIntent);
    }
}
