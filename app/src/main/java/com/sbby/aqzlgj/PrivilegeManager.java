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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

public class PrivilegeManager {

    private static boolean isRishReady = false;
    private static String rishPath;

    public interface InitCallback {
        void onResult(boolean success, String errorMsg);
    }

    public interface ExecCallback {
        void onResult(boolean success, String output);
    }

    public static void init(final Context context, final InitCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String error = null;
                boolean ok = false;
                for (int attempt = 1; attempt <= 3; attempt++) {
                    error = setupRish(context);
                    if (error == null) {
                        ok = true;
                        break;
                    }
                    if (attempt < 3) {
                        try { Thread.sleep(300); } catch (InterruptedException e) {}
                    }
                }
                final boolean finalOk = ok;
                final String finalError = error;
                if (callback != null) {
                    android.os.Handler mainHandler = new android.os.Handler(context.getMainLooper());
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onResult(finalOk, finalError);
                        }
                    });
                }
            }
        }).start();
    }

    private static String setupRish(Context context) {
        try {
            // 使用内部私有目录 /data/user/0/com.sbby.aqzlgj/files
            File internalDir = context.getFilesDir();
            if (internalDir == null) return "无法获取内部私有目录";
            File rishFile = new File(internalDir, "1.sh");
            File dexFile = new File(internalDir, "1.dex");
            rishPath = rishFile.getAbsolutePath();

            copyAssetToFile(context, "1.sh", rishFile);
            copyAssetToFile(context, "1.dex", dexFile);
            dexFile.setReadOnly();

            Process p = Runtime.getRuntime().exec(new String[]{"sh", rishPath, "-c", "echo 'RISH_OK'"});
            int code = p.waitFor();
            StringBuilder output = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) output.append(line);
            reader.close();

            StringBuilder error = new StringBuilder();
            BufferedReader errReader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            while ((line = errReader.readLine()) != null) error.append(line);
            errReader.close();

            if (code != 0) {
                return "退出码 " + code + "，错误: " + error.toString();
            }
            if (output.indexOf("RISH_OK") == -1) {
                return "输出不匹配: " + output.toString() + " (错误: " + error.toString() + ")";
            }
            isRishReady = true;
            return null;
        } catch (Exception e) {
            return "初始化异常: " + e.getMessage();
        }
    }

    private static void copyAssetToFile(Context context, String assetName, File target) throws Exception {
        if (target.exists()) return;
        InputStream is = context.getAssets().open(assetName);
        FileOutputStream os = new FileOutputStream(target);
        byte[] buf = new byte[8192];
        int len;
        while ((len = is.read(buf)) != -1) os.write(buf, 0, len);
        os.close();
        is.close();
    }

    public static boolean isReady() {
        return isRishReady;
    }

    public static void execCommand(final String command, final ExecCallback callback) {
        if (!isRishReady) {
            if (callback != null) callback.onResult(false, "未提权");
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Process p = Runtime.getRuntime().exec(new String[]{"sh", rishPath, "-c", command});
                    p.waitFor();
                    StringBuilder output = new StringBuilder();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                    String line;
                    while ((line = reader.readLine()) != null) output.append(line).append("\n");
                    reader.close();
                    boolean success = p.exitValue() == 0;
                    if (callback != null) callback.onResult(success, output.toString());
                } catch (Exception e) {
                    if (callback != null) callback.onResult(false, e.getMessage());
                }
            }
        }).start();
    }

    public static void sendBroadcast(final String command) {
        String escaped = "'" + command.replace("'", "'\\''") + "'";
        execCommand("am broadcast -a android.intent.action.RUN -e cmd " + escaped, null);
    }
}