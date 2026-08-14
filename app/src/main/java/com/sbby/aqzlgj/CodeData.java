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
import android.os.Handler;
import android.os.Looper;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class CodeData {
    public static final String CATEGORY_BASIC = "刀皮类";
    public static final String CATEGORY_STORAGE = "战术装备（含食品）";
    public static final String CATEGORY_NETWORK = "钥匙类";
    public static final String CATEGORY_FILE = "针剂类";
    public static final String CATEGORY_TOOLS = "操作指令";
    public static final String CATEGORY_MIX = "大杂烩";

    private static List<CodeItem> allCodes;
    private static boolean isLoading = false;
    private static LoadListener loadListener;

    public interface LoadListener {
        void onLoadComplete();
        void onLoadError(String error);
    }

    public static void setLoadListener(LoadListener listener) {
        loadListener = listener;
    }

    public static List<CodeItem> getAllCommands(Context context) {
        init(context);
        int waitCount = 0;
        while (isLoading && waitCount < 30) {
            try {
                Thread.sleep(100);
                waitCount++;
            } catch (InterruptedException e) {
                break;
            }
        }
        if (allCodes == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(allCodes);
    }

    private static void addFromFile(Context context, String fileName, String category) {
        try {
            InputStream is = context.getAssets().open(fileName);
            BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.length() == 0) continue;
                int index = line.indexOf("|");
                if (index > 0) {
                    String title = line.substring(0, index).trim();
                    String code = line.substring(index + 1).trim();
                    allCodes.add(new CodeItem(title, code, category));
                }
            }
            br.close();
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void init(final Context context) {
        if (allCodes != null) return;
        if (isLoading) return;

        isLoading = true;
        final Handler handler = new Handler(Looper.getMainLooper());

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final List<CodeItem> tempList = new ArrayList<>();

                    loadFileTo(context, "刀皮类.txt", CATEGORY_BASIC, tempList);
                    loadFileTo(context, "战术装备含食物.txt", CATEGORY_STORAGE, tempList);
                    loadFileTo(context, "钥匙类.txt", CATEGORY_NETWORK, tempList);
                    loadFileTo(context, "针剂类.txt", CATEGORY_FILE, tempList);
                    loadFileTo(context, "操作指令.txt", CATEGORY_TOOLS, tempList);
                    loadFileTo(context, "大杂烩.txt", CATEGORY_MIX, tempList);

                    allCodes = tempList;
                    isLoading = false;

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (loadListener != null) {
                                loadListener.onLoadComplete();
                            }
                        }
                    });

                } catch (final Exception e) {
                    isLoading = false;
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (loadListener != null) {
                                loadListener.onLoadError(e.getMessage());
                            }
                        }
                    });
                }
            }
        }).start();
    }

    private static void loadFileTo(Context context, String fileName, String category, List<CodeItem> target) {
        try {
            InputStream is = context.getAssets().open(fileName);
            BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.length() == 0) continue;
                int index = line.indexOf("|");
                if (index > 0) {
                    String title = line.substring(0, index).trim();
                    String code = line.substring(index + 1).trim();
                    target.add(new CodeItem(title, code, category));
                }
            }
            br.close();
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<String> getAllCategories() {
        List<String> list = new ArrayList<>();
        list.add(CATEGORY_BASIC);
        list.add(CATEGORY_STORAGE);
        list.add(CATEGORY_NETWORK);
        list.add(CATEGORY_FILE);
        list.add(CATEGORY_TOOLS);
        list.add(CATEGORY_MIX);
        return list;
    }

    public static List<CodeItem> getCodesByCategory(Context context, String category) {
        init(context);
        List<CodeItem> result = new ArrayList<>();
        if (allCodes == null) return result;
        for (CodeItem item : allCodes) {
            if (item.category.equals(category)) {
                result.add(item);
            }
        }
        return result;
    }

    public static List<CodeItem> searchCodes(Context context, String keyword) {
        init(context);
        List<CodeItem> result = new ArrayList<>();
        if (allCodes == null) return result;
        keyword = keyword.toLowerCase();
        for (CodeItem item : allCodes) {
            if (item.title.toLowerCase().contains(keyword) || item.code.toLowerCase().contains(keyword)) {
                result.add(item);
            }
        }
        return result;
    }

    public static void clearCache() {
        allCodes = null;
        isLoading = false;
    }
}