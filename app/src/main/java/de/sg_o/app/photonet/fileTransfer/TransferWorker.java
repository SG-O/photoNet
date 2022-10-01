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

package de.sg_o.app.photonet.fileTransfer;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicBoolean;

import de.sg_o.lib.photoNet.netData.DataTransfer;

public class TransferWorker implements Runnable{
    private static final LinkedBlockingDeque<DataTransfer> pending = new LinkedBlockingDeque<>();
    private static final LinkedBlockingDeque<DataTransfer> failed = new LinkedBlockingDeque<>();

    private final AtomicBoolean running = new AtomicBoolean(true);
    private DataTransfer transfer;

    public TransferWorker() {
        super();
    }

    @Override
    public void run() {
        while ((!failed.isEmpty()) || (!pending.isEmpty())) {
            while ((!pending.isEmpty()) && running.get()) {
                DataTransfer tmp = pending.poll();
                if (tmp == null) continue;
                transfer = tmp;
                transfer.run();
                if (transfer.hasFailed() && (!transfer.isAborted())) {
                    failed.add(tmp);
                } else {
                    transfer.end();
                }
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException ignore) {
            }
        }
        running.set(false);
    }

    public void stop() {
        for (DataTransfer trans : pending) {
            trans.end();
        }
        pending.clear();
        if (transfer != null) transfer.abort();
        running.set(false);
        clearFailed();
    }

    public static void addTransfer(DataTransfer dataTransfer) {
        pending.add(dataTransfer);
    }

    public int countPending() {
        return pending.size();
    }

    public DataTransfer getNext() {
        return pending.peek();
    }

    public boolean isRunning() {
        return running.get();
    }

    public boolean hasFailed() {
        return !failed.isEmpty();
    }

    public DataTransfer getFirstFailed() {
        return failed.peek();
    }

    public void retry() {
        pending.addAll(failed);
        failed.clear();
    }

    public void clearFailed() {
        for (DataTransfer fail : failed) {
            fail.end();
        }
        failed.clear();
    }

    public DataTransfer getTransfer() {
        return transfer;
    }
}
