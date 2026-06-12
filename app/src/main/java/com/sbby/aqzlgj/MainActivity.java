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
//软件开发者为是白白吖（scpao5），使用此源代码请署名原作者
package com.sbby.aqzlgj;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.os.CountDownTimer;
import android.widget.Button;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.widget.ScrollView;
import android.os.CountDownTimer;
import android.app.Dialog;

public class MainActivity extends Activity {

    private List<Map<String, String>> categoryData;
    private ListView categoryList;
    private EditText searchBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 检查是否已同意用户协议
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        boolean agreed = prefs.getBoolean("agreement_accepted", false);
        if (!agreed) {
            showAgreementDialog();
            return; // 未同意时不加载主界面
        }

        // 已同意，正常加载界面
        initMainUI();
    }

    private void initMainUI() {
        CodeData.init(this);

        RelativeLayout root = new RelativeLayout(this);
        root.setBackgroundColor(Color.parseColor("#F0F4F8"));

        // 顶部蓝色区域
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

        TextView title = new TextView(this);
        title.setText("暗区指令工具");
        title.setTextColor(Color.WHITE);
        title.setTextSize(22);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setGravity(View.TEXT_ALIGNMENT_CENTER);
        topBar.addView(title);

        View divider = new View(this);
        divider.setId(2);
        divider.setBackgroundColor(Color.parseColor("#1976D2"));
        RelativeLayout.LayoutParams dividerParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, dp(2));
        dividerParams.addRule(RelativeLayout.BELOW, 1);
        divider.setLayoutParams(dividerParams);
        root.addView(divider);

        LinearLayout searchArea = new LinearLayout(this);
        searchArea.setId(3);
        searchArea.setOrientation(LinearLayout.HORIZONTAL);
        searchArea.setGravity(android.view.Gravity.CENTER_VERTICAL);
        searchArea.setPadding(dp(15), dp(15), dp(15), dp(15));
        RelativeLayout.LayoutParams searchAreaParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        searchAreaParams.addRule(RelativeLayout.BELOW, 2);
        searchArea.setLayoutParams(searchAreaParams);
        root.addView(searchArea);

        LinearLayout searchBoxContainer = new LinearLayout(this);
        searchBoxContainer.setBackground(createRoundDrawable(Color.WHITE, dp(25)));
        searchBoxContainer.setOrientation(LinearLayout.HORIZONTAL);
        searchBoxContainer.setGravity(android.view.Gravity.CENTER_VERTICAL);
        searchBoxContainer.setPadding(dp(15), dp(8), dp(15), dp(8));
        searchBoxContainer.setLayoutParams(new LinearLayout.LayoutParams(0, dp(60), 1));
        searchArea.addView(searchBoxContainer);

        // 搜索图标
        ImageView searchIcon = new ImageView(this);
        searchIcon.setImageResource(R.drawable.ic_search);
        searchIcon.setColorFilter(Color.parseColor("#9E9E9E"));
        int iconSize = dp(30);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(iconSize, iconSize);
        iconParams.setMargins(0, 0, dp(8), 0);
        searchIcon.setLayoutParams(iconParams);
        searchIcon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        searchBoxContainer.addView(searchIcon);

        searchBox = new EditText(this);
        searchBox.setHint("输入关键词搜索...");
        searchBox.setHintTextColor(Color.parseColor("#BDBDBD"));
        searchBox.setTextSize(16);
        searchBox.setTextColor(Color.parseColor("#424242"));
        searchBox.setBackgroundColor(Color.TRANSPARENT);
        searchBox.setSingleLine(true);
        LinearLayout.LayoutParams editParams = new LinearLayout.LayoutParams(0, dp(50), 1);
        editParams.setMargins(0, 0, 0, 0);
        searchBox.setLayoutParams(editParams);
        searchBoxContainer.addView(searchBox);

        final TextView searchBtn = new TextView(this);
        searchBtn.setText("搜索");
        searchBtn.setTextColor(Color.WHITE);
        searchBtn.setTextSize(16);
        searchBtn.setTypeface(Typeface.DEFAULT_BOLD);
        searchBtn.setBackground(createRoundDrawable(Color.parseColor("#1976D2"), dp(25)));
        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(dp(80), dp(50));
        btnParams.setMargins(dp(10), 0, 0, 0);
        searchBtn.setLayoutParams(btnParams);
        searchBtn.setGravity(android.view.Gravity.CENTER);
        searchBtn.setClickable(true);
        searchArea.addView(searchBtn);

        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String kw = searchBox.getText().toString().trim();
                if (kw.isEmpty()) {
                    Toast.makeText(MainActivity.this, "请输入关键词", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                    intent.putExtra("keyword", kw);
                    startActivity(intent);
                }
            }
        });

        searchBox.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == 3) {
                    searchBtn.performClick();
                    return true;
                }
                return false;
            }
        });

        LinearLayout listArea = new LinearLayout(this);
        listArea.setId(4);
        listArea.setOrientation(LinearLayout.VERTICAL);
        listArea.setBackgroundColor(Color.parseColor("#F0F4F8"));
        RelativeLayout.LayoutParams listAreaParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);
        listAreaParams.addRule(RelativeLayout.BELOW, 3);
        listArea.setLayoutParams(listAreaParams);
        root.addView(listArea);

        TextView hint = new TextView(this);
        hint.setText("点击分区查看指令");
        hint.setTextSize(14);
        hint.setTextColor(Color.parseColor("#9E9E9E"));
        hint.setPadding(dp(15), dp(10), dp(15), dp(5));
        listArea.addView(hint);

        categoryList = new ListView(this);
        categoryList.setBackgroundColor(Color.parseColor("#F0F4F8"));
        categoryList.setDivider(null);
        categoryList.setDividerHeight(dp(10));
        categoryList.setPadding(dp(15), dp(5), dp(15), dp(15));
        categoryList.setVerticalScrollBarEnabled(false);
        listArea.addView(categoryList);

        setContentView(root);
        loadCategories();

        PrivilegeManager.init(this, new PrivilegeManager.InitCallback() {
            @Override
            public void onResult(final boolean success, final String errorMsg) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (success) {
                            Toast.makeText(MainActivity.this, "提权成功", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity.this, "提权失败: " + errorMsg, Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });
    }

private void showAgreementDialog() {
    // 创建自定义 Dialog
    final Dialog dialog = new Dialog(this);
    dialog.setCancelable(false);
    
    if (dialog.getWindow() != null) {
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }
    
    LinearLayout mainLayout = new LinearLayout(this);
    mainLayout.setOrientation(LinearLayout.VERTICAL);
    mainLayout.setBackgroundColor(Color.WHITE);
    mainLayout.setPadding(dp(24), dp(20), dp(24), dp(20));
    GradientDrawable bg = new GradientDrawable();
    bg.setShape(GradientDrawable.RECTANGLE);
    bg.setCornerRadius(dp(16));
    bg.setColor(Color.WHITE);
    mainLayout.setBackground(bg);
    
    TextView title = new TextView(this);
    title.setText("用户协议");
    title.setTextSize(20);
    title.setTextColor(Color.parseColor("#1F2937"));
    title.setTypeface(Typeface.DEFAULT_BOLD);
    title.setGravity(Gravity.CENTER);
    title.setPadding(0, 0, 0, dp(16));
    mainLayout.addView(title);
    
    ScrollView scrollView = new ScrollView(this);
    scrollView.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, dp(400)));
    mainLayout.addView(scrollView);
    
    TextView content = new TextView(this);
    content.setText("欢迎使用暗区指令工具！\n\n"
            + "1. 本应用是开源软件，遵循 GNU General Public License v3.0 (GPL-3.0) 许可证。\n"
            + "   您有权自由使用、修改、分发本软件，但任何修改后的版本也必须以 GPL-3.0 开源。\n"
            + "   源代码地址：\n"
            + "   GitHub: https://github.com/scpao5/aqzlgj/\n"
            + "   Gitee: https://gitee.com/scpao5/aqzlgj/\n\n"
            + "2. 任何个人或组织不得将本应用或其修改版本用于商业盈利目的（即“圈钱”），包括但不限于：\n"
            + "   - 售卖 APK、收费提供下载\n"
            + "   - 内置广告、收取会员费\n"
            + "   - 以“代刷”、“代练”等名义变相收费\n\n"
            + "3. 您通过本应用执行的所有指令，请自行承担相应后果（包括但不限于游戏封号）。\n"
            + "4. 本应用不会收集您的任何个人信息。\n"
            + "5. 开发者保留对本协议的最终解释权。\n"
            + "6. 软件开发者：是白白吖（scpao5），QQ：771217201\n\n"
            + "您如果想重新查看本协议，请清除应用数据\n\n"
            + "如您继续使用本应用，视为同意本协议。");
    content.setTextSize(14);
    content.setTextColor(Color.parseColor("#4B5563"));
    content.setLineSpacing(dp(4), 1);
    scrollView.addView(content);
    
    View divider = new View(this);
    divider.setBackgroundColor(Color.parseColor("#E5E7EB"));
    LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, dp(1));
    dividerParams.setMargins(0, dp(20), 0, dp(16));
    divider.setLayoutParams(dividerParams);
    mainLayout.addView(divider);
    
    LinearLayout buttonBar = new LinearLayout(this);
    buttonBar.setOrientation(LinearLayout.HORIZONTAL);
    buttonBar.setGravity(Gravity.END);
    mainLayout.addView(buttonBar);
    
    Button rejectBtn = new Button(this);
    rejectBtn.setText("拒绝");
    rejectBtn.setTextColor(Color.parseColor("#6B7280"));
    rejectBtn.setBackgroundColor(Color.TRANSPARENT);
    rejectBtn.setPadding(dp(20), dp(8), dp(20), dp(8));
    rejectBtn.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            finish();
        }
    });
    buttonBar.addView(rejectBtn);
    
    final Button agreeBtn = new Button(this);
    agreeBtn.setText("同意 (10)");   // 改为10秒提示
    agreeBtn.setTextColor(Color.parseColor("#2196F3"));
    agreeBtn.setBackgroundColor(Color.TRANSPARENT);
    agreeBtn.setEnabled(false);
    agreeBtn.setPadding(dp(20), dp(8), dp(20), dp(8));
    buttonBar.addView(agreeBtn);
    
    dialog.setContentView(mainLayout);
    dialog.show();
    
    // 倒计时改为10秒 (10000毫秒)
    new CountDownTimer(10000, 1000) {
        @Override
        public void onTick(long millisUntilFinished) {
            int seconds = (int) (millisUntilFinished / 1000);
            agreeBtn.setText("同意 (" + seconds + ")");
        }
        @Override
        public void onFinish() {
            agreeBtn.setEnabled(true);
            agreeBtn.setText("同意");
            agreeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
                    prefs.edit().putBoolean("agreement_accepted", true).apply();
                    initMainUI();
                    dialog.dismiss();
                }
            });
        }
    }.start();
}

    private void loadCategories() {
        List<String> categories = CodeData.getAllCategories();
        categoryData = new ArrayList<>();
        for (String cat : categories) {
            Map<String, String> map = new HashMap<>();
            map.put("name", cat);
            categoryData.add(map);
        }
        categoryList.setAdapter(new CategoryAdapter());
    }

    private class CategoryAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return categoryData.size();
        }

        @Override
        public Object getItem(int position) {
            return categoryData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LinearLayout itemView;
            if (convertView == null) {
                itemView = new LinearLayout(MainActivity.this);
                itemView.setOrientation(LinearLayout.HORIZONTAL);
                itemView.setGravity(android.view.Gravity.CENTER_VERTICAL);
                itemView.setBackground(createRoundDrawable(Color.WHITE, dp(15)));
                itemView.setPadding(dp(20), dp(18), dp(20), dp(18));
                itemView.setClickable(true);
                itemView.setFocusable(true);
            } else {
                itemView = (LinearLayout) convertView;
                itemView.removeAllViews();
            }

            final Map<String, String> data = categoryData.get(position);
            TextView name = new TextView(MainActivity.this);
            name.setText(data.get("name"));
            name.setTextSize(17);
            name.setTextColor(Color.parseColor("#424242"));
            name.setTypeface(Typeface.DEFAULT_BOLD);
            name.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
            itemView.addView(name);

            TextView arrow = new TextView(MainActivity.this);
            arrow.setText(">");
            arrow.setTextSize(20);
            arrow.setTextColor(Color.parseColor("#BDBDBD"));
            itemView.addView(arrow);

            final String category = data.get("name");
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, CategoryActivity.class);
                    intent.putExtra("category", category);
                    startActivity(intent);
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
