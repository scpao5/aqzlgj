/*
 * Copyright (C) 2026 scpao5
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.sbby.aqzlgj;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class PrivilegeManager {
    private static final String TAG = "PrivilegeManager";
    private static Context sContext;
    private static boolean hasSu = false;
    private static boolean suChecked = false;

    public static void init(Context context) {
        sContext = context.getApplicationContext();
        new Thread(new Runnable() {
            @Override
            public void run() {
                hasSu = checkSu();
                suChecked = true;
                if (hasSu) {
                    Log.i(TAG, "Root available, will use su -c for commands.");
                    showToast("已检测到 root，将使用 su 执行指令");
                } else {
                    Log.i(TAG, "No root, will use normal broadcast.");
                    showToast("未检测到 Root，将使用普通广播");
                }
            }
        }).start();
    }

    private static boolean checkSu() {
        final boolean[] result = {false};
        final CountDownLatch latch = new CountDownLatch(1);

        Thread checker = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Process p = new ProcessBuilder("su", "-c", "echo test").start();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                    String line = reader.readLine();
                    int exitCode = p.waitFor();
                    if (exitCode == 0 && line != null && line.contains("test")) {
                        result[0] = true;
                    }
                } catch (Exception e) {
                    Log.w(TAG, "checkSu exception: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            }
        });
        checker.start();

        try {
            if (!latch.await(2, TimeUnit.SECONDS)) {
                Log.w(TAG, "checkSu timeout, assume no root.");
                checker.interrupt();
                return false;
            }
        } catch (InterruptedException e) {
            Log.w(TAG, "checkSu interrupted");
        }
        return result[0];
    }

    public static void sendCommand(final String command) {
        if (sContext == null) return;

        if (!suChecked) {
            sendBroadcast(command);
            return;
        }

        if (hasSu) {
            executeViaSu(command);
        } else {
            sendBroadcast(command);
        }
    }

    private static void executeViaSu(final String command) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String safeCmd = command.replace("'", "'\\''");
                    String amCmd = "am broadcast -a android.intent.action.RUN -e cmd '" + safeCmd + "'";
                    Process p = new ProcessBuilder("su", "-c", amCmd).start();
                    int exitCode = p.waitFor();
                    if (exitCode == 0) {
                        Log.i(TAG, "Su command executed: " + command);
                        // 成功时不显示 Toast（应用层会显示指令标题）
                    } else {
                        BufferedReader errReader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                        StringBuilder err = new StringBuilder();
                        String line;
                        while ((line = errReader.readLine()) != null) {
                            err.append(line);
                        }
                        errReader.close();
                        Log.e(TAG, "Su failed, exit=" + exitCode + ", error=" + err.toString());
                        showToast("su 执行失败，尝试普通广播");
                        sendBroadcast(command);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Exception in su command", e);
                    showToast("su 执行异常，使用普通广播");
                    sendBroadcast(command);
                }
            }
        }).start();
    }

    private static void sendBroadcast(String command) {
        if (sContext == null) return;
        Intent intent = new Intent("android.intent.action.RUN");
        intent.putExtra("cmd", command);
        intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
        sContext.sendBroadcast(intent);
        Log.i(TAG, "Broadcast sent: " + command);
        // 不显示 Toast，让应用层显示指令标题
    }

    private static void showToast(final String msg) {
        if (sContext == null) return;
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(sContext, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }
}