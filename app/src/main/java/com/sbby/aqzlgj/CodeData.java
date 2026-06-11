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

    private static List<CodeItem> allCodes;

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

    public static void init(Context context) {
        if (allCodes == null) {
            allCodes = new ArrayList<>();
            addFromFile(context, "刀皮类.txt", CATEGORY_BASIC);
            addFromFile(context, "战术装备含食物.txt", CATEGORY_STORAGE);
            addFromFile(context, "钥匙类.txt", CATEGORY_NETWORK);
            addFromFile(context, "针剂类.txt", CATEGORY_FILE);
            addFromFile(context, "操作指令.txt", CATEGORY_TOOLS);
        }
    }

    public static List<String> getAllCategories() {
        List<String> list = new ArrayList<>();
        list.add(CATEGORY_BASIC);
        list.add(CATEGORY_STORAGE);
        list.add(CATEGORY_NETWORK);
        list.add(CATEGORY_FILE);
        list.add(CATEGORY_TOOLS);
        return list;
    }

    public static List<CodeItem> getCodesByCategory(Context context, String category) {
        init(context);
        List<CodeItem> result = new ArrayList<>();
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
    }
}