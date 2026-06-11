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

import android.app.Activity;
import android.content.ClipboardManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchActivity extends Activity {

    private String keyword;
    private List<Map<String, String>> dataList;
    private ListView listView;
    private TextView emptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        keyword = getIntent().getStringExtra("keyword");

        RelativeLayout root = new RelativeLayout(this);
        root.setBackgroundColor(Color.parseColor("#F0F4F8"));

        LinearLayout topBar = new LinearLayout(this);
        topBar.setId(1);
        topBar.setOrientation(LinearLayout.VERTICAL);
        topBar.setBackgroundColor(Color.parseColor("#2196F3"));
        topBar.setPadding(dp(20), dp(15), dp(20), dp(15));
        RelativeLayout.LayoutParams topParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        topParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        topBar.setLayoutParams(topParams);
        root.addView(topBar);

        LinearLayout titleBar = new LinearLayout(this);
        titleBar.setOrientation(LinearLayout.HORIZONTAL);
        titleBar.setGravity(android.view.Gravity.CENTER_VERTICAL);
        topBar.addView(titleBar);

        TextView backBtn = new TextView(this);
        backBtn.setText("<");
        backBtn.setTextColor(Color.WHITE);
        backBtn.setTextSize(22);
        backBtn.setPadding(0, 0, dp(15), 0);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        titleBar.addView(backBtn);

        TextView title = new TextView(this);
        title.setText("搜索结果");
        title.setTextColor(Color.WHITE);
        title.setTextSize(20);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        titleBar.addView(title);

        View divider = new View(this);
        divider.setId(2);
        divider.setBackgroundColor(Color.parseColor("#1976D2"));
        RelativeLayout.LayoutParams dividerParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, dp(2));
        dividerParams.addRule(RelativeLayout.BELOW, 1);
        divider.setLayoutParams(dividerParams);
        root.addView(divider);

        LinearLayout keywordBar = new LinearLayout(this);
        keywordBar.setId(3);
        keywordBar.setOrientation(LinearLayout.HORIZONTAL);
        keywordBar.setBackgroundColor(Color.parseColor("#E3F2FD"));
        keywordBar.setPadding(dp(15), dp(12), dp(15), dp(12));
        RelativeLayout.LayoutParams keywordParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        keywordParams.addRule(RelativeLayout.BELOW, 2);
        keywordBar.setLayoutParams(keywordParams);
        root.addView(keywordBar);

        TextView keywordText = new TextView(this);
        keywordText.setText("搜索关键词: " + keyword);
        keywordText.setTextSize(14);
        keywordText.setTextColor(Color.parseColor("#1565C0"));
        keywordText.setGravity(android.view.Gravity.CENTER);
        keywordBar.addView(keywordText);

        LinearLayout contentArea = new LinearLayout(this);
        contentArea.setId(4);
        contentArea.setOrientation(LinearLayout.VERTICAL);
        contentArea.setBackgroundColor(Color.parseColor("#F0F4F8"));
        RelativeLayout.LayoutParams contentParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);
        contentParams.addRule(RelativeLayout.BELOW, 3);
        contentArea.setLayoutParams(contentParams);
        root.addView(contentArea);

        listView = new ListView(this);
        listView.setBackgroundColor(Color.parseColor("#F0F4F8"));
        listView.setDivider(null);
        listView.setDividerHeight(dp(12));
        listView.setPadding(dp(15), dp(15), dp(15), dp(15));
        listView.setVerticalScrollBarEnabled(false);
        contentArea.addView(listView);

        emptyView = new TextView(this);
        emptyView.setText("未找到相关指令");
        emptyView.setTextSize(18);
        emptyView.setTextColor(Color.parseColor("#9E9E9E"));
        emptyView.setGravity(android.view.Gravity.CENTER);
        emptyView.setVisibility(View.GONE);
        emptyView.setPadding(dp(20), dp(50), dp(20), dp(50));
        contentArea.addView(emptyView);

        setContentView(root);
        doSearch();
    }

    private void doSearch() {
        List<CodeItem> results = CodeData.searchCodes(this, keyword);
        if (results.isEmpty()) {
            listView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
            return;
        }
        listView.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);

        dataList = new ArrayList<>();
        for (CodeItem item : results) {
            Map<String, String> map = new HashMap<>();
            map.put("title", item.title);
            map.put("code", item.code);
            map.put("category", item.category);
            dataList.add(map);
        }
        listView.setAdapter(new SearchAdapter());
    }

    private class SearchAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return dataList.size();
        }

        @Override
        public Object getItem(int position) {
            return dataList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LinearLayout itemView;
            if (convertView == null) {
                itemView = new LinearLayout(SearchActivity.this);
                itemView.setOrientation(LinearLayout.VERTICAL);
                itemView.setBackground(createRoundDrawable(Color.WHITE, dp(15)));
                itemView.setPadding(dp(18), dp(15), dp(18), dp(15));
                itemView.setClickable(true);
                itemView.setFocusable(true);
            } else {
                itemView = (LinearLayout) convertView;
                itemView.removeAllViews();
            }

            final Map<String, String> data = dataList.get(position);
            LinearLayout titleLayout = new LinearLayout(SearchActivity.this);
            titleLayout.setOrientation(LinearLayout.HORIZONTAL);
            titleLayout.setGravity(android.view.Gravity.CENTER_VERTICAL);
            itemView.addView(titleLayout);

            // 复制图标（已删除 setColorFilter，使用矢量图自身颜色）
            ImageView copyIcon = new ImageView(SearchActivity.this);
            copyIcon.setImageResource(R.drawable.ic_copy);
            int iconSize = dp(32);
            copyIcon.setLayoutParams(new LinearLayout.LayoutParams(iconSize, iconSize));
            copyIcon.setPadding(0, 0, dp(10), 0);
            copyIcon.setScaleType(ImageView.ScaleType.FIT_CENTER);
            titleLayout.addView(copyIcon);

            TextView titleText = new TextView(SearchActivity.this);
            titleText.setText(data.get("title"));
            titleText.setTextSize(16);
            titleText.setTextColor(Color.parseColor("#424242"));
            titleText.setTypeface(Typeface.DEFAULT_BOLD);
            titleText.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
            titleLayout.addView(titleText);

            TextView categoryBadge = new TextView(SearchActivity.this);
            categoryBadge.setText(data.get("category"));
            categoryBadge.setTextSize(11);
            categoryBadge.setTextColor(Color.WHITE);
            categoryBadge.setBackgroundColor(Color.parseColor("#2196F3"));
            categoryBadge.setPadding(dp(8), dp(3), dp(8), dp(3));
            categoryBadge.setGravity(android.view.Gravity.CENTER);
            titleLayout.addView(categoryBadge);

            Button copyBtn = new Button(SearchActivity.this);
            copyBtn.setText("复制");
            copyBtn.setTextSize(12);
            copyBtn.setBackgroundColor(Color.parseColor("#4CAF50"));
            copyBtn.setTextColor(Color.WHITE);
            copyBtn.setPadding(dp(12), dp(4), dp(12), dp(4));
            copyBtn.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            titleLayout.addView(copyBtn);

            Button execBtn = new Button(SearchActivity.this);
            execBtn.setText("执行");
            execBtn.setTextSize(12);
            execBtn.setBackgroundColor(Color.parseColor("#2196F3"));
            execBtn.setTextColor(Color.WHITE);
            execBtn.setPadding(dp(12), dp(4), dp(12), dp(4));
            execBtn.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            titleLayout.addView(execBtn);

            TextView codeText = new TextView(SearchActivity.this);
            codeText.setText(data.get("code"));
            codeText.setTextSize(13);
            codeText.setTextColor(Color.parseColor("#666666"));
            codeText.setBackgroundColor(Color.parseColor("#F5F5F5"));
            codeText.setPadding(dp(12), dp(12), dp(12), dp(12));
            LinearLayout.LayoutParams codeParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            codeParams.setMargins(0, dp(10), 0, 0);
            codeText.setLayoutParams(codeParams);
            itemView.addView(codeText);

            final String code = data.get("code");
            final String title = data.get("title");

            copyBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                    cm.setText(code);
                    Toast.makeText(SearchActivity.this, "已复制: " + title, Toast.LENGTH_SHORT).show();
                }
            });

            execBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (PrivilegeManager.isReady()) {
                        PrivilegeManager.sendBroadcast(code);
                        Toast.makeText(SearchActivity.this, "执行: " + title, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(SearchActivity.this, "未提权，无法执行", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            return itemView;
        }
    }

    private GradientDrawable createRoundDrawable(int color, float radius) {
        GradientDrawable gd = new GradientDrawable();
        gd.setShape(GradientDrawable.RECTANGLE);
        gd.setCornerRadius(radius);
        gd.setColor(color);
        return gd;
    }

    private int dp(float dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
}