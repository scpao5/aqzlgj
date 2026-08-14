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

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class FloatWindowService extends Service {

    public static volatile boolean isRunning = false;
    public static final String EXTRA_MODE = "extra_mode";
    public static final int MODE_ADB = 1;
    private static final String PREFS_NAME = "app_prefs";
    private static final String KEY_FLOAT_MODE = "float_mode";

    private static final int HEIGHT_COLLAPSED_DP = 44;
    private static final int WIDTH_DP = 220;

    private WindowManager windowManager;
    private LinearLayout floatView;
    private WindowManager.LayoutParams layoutParams;

    private LinearLayout floatHeader;
    private TextView floatTitle;
    private TextView floatArrow;
    private Button floatCloseBtn;
    private LinearLayout floatExpandArea;
    private EditText floatInput;
    private Button floatSearchBtn;
    private LinearLayout floatListContainer;
    private ScrollView floatListScroll;
    private Button floatBackBtn;

    private boolean expanded = false;
    private int listState = 0;
    private String currentCategory = null;
    private int currentMode = MODE_ADB;

    private List<CodeItem> allCommands;
    private Handler mainHandler = new Handler(Looper.getMainLooper());

    private int initialX, initialY;
    private int initialTouchX, initialTouchY;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        isRunning = true;
        currentMode = readSavedMode();
        startInForeground();

        CodeData.init(this);
        allCommands = CodeData.getAllCommands(this);
        PrivilegeManager.init(this);

        showFloatWindow();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        if (intent != null && intent.hasExtra(EXTRA_MODE)) {
            currentMode = intent.getIntExtra(EXTRA_MODE, MODE_ADB);
            getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                    .edit()
                    .putInt(KEY_FLOAT_MODE, currentMode)
                    .apply();
            if (floatTitle != null) updateFloatTitle();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.hasExtra(EXTRA_MODE)) {
            currentMode = intent.getIntExtra(EXTRA_MODE, MODE_ADB);
        }
        try {
            if (floatView != null && expanded) updateLayoutByContent();
        } catch (Exception ignore) {}
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        isRunning = false;
        dismissCloseConfirmDialog();
        if (windowManager != null && floatView != null) {
            try {
                windowManager.removeView(floatView);
            } catch (Exception ignore) {}
        }
        floatView = null;
        super.onDestroy();
    }

    private int readSavedMode() {
        return getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .getInt(KEY_FLOAT_MODE, MODE_ADB);
    }

    private void startInForeground() {
        String channelId = "float_window_channel";
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId, "悬浮窗", NotificationManager.IMPORTANCE_LOW);
            if (nm != null) nm.createNotificationChannel(channel);
        }
        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new Notification.Builder(this, channelId);
        } else {
            builder = new Notification.Builder(this);
        }
        Notification notification = builder
                .setContentTitle("暗区指令工具")
                .setContentText("悬浮窗运行中")
                .setSmallIcon(android.R.drawable.ic_menu_info_details)
                .setOngoing(true)
                .build();
        startForeground(1, notification);
    }

    // ========== 动态构建悬浮窗 UI ==========
    private void showFloatWindow() {
        windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

        floatView = new LinearLayout(this);
        floatView.setOrientation(LinearLayout.VERTICAL);
        floatView.setBackground(createRoundDrawable(Color.WHITE, dp(12)));
        floatView.setPadding(0, 0, 0, 0);

        floatHeader = new LinearLayout(this);
        floatHeader.setOrientation(LinearLayout.HORIZONTAL);
        floatHeader.setGravity(Gravity.CENTER_VERTICAL);
        floatHeader.setPadding(dp(8), 0, dp(6), 0);
        floatHeader.setMinimumHeight(dp(HEIGHT_COLLAPSED_DP));
        floatView.addView(floatHeader, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        floatTitle = new TextView(this);
        floatTitle.setText("指令工具");
        floatTitle.setTextSize(13);
        floatTitle.setTypeface(Typeface.DEFAULT_BOLD);
        floatTitle.setTextColor(0xFF1976D2);
        floatTitle.setGravity(Gravity.CENTER);
        floatTitle.setMaxLines(1);
        floatTitle.setEllipsize(TextUtils.TruncateAt.END);
        floatHeader.addView(floatTitle, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

        floatArrow = new TextView(this);
        floatArrow.setText("⌄");
        floatArrow.setTextSize(16);
        floatArrow.setTextColor(0xFF2196F3);
        floatArrow.setGravity(Gravity.CENTER);
        floatArrow.setPadding(dp(8), 0, dp(8), 0);
        floatHeader.addView(floatArrow, new LinearLayout.LayoutParams(dp(28), dp(28)));

        floatCloseBtn = new Button(this);
        floatCloseBtn.setText("✕");
        floatCloseBtn.setTextSize(12);
        floatCloseBtn.setTextColor(0xFFFFFFFF);
        floatCloseBtn.setBackground(createRoundDrawable(0xFFF44336, dp(14)));
        floatCloseBtn.setPadding(dp(8), dp(2), dp(8), dp(2));
        LinearLayout.LayoutParams closeParams = new LinearLayout.LayoutParams(dp(28), dp(28));
        closeParams.leftMargin = dp(4);
        floatCloseBtn.setLayoutParams(closeParams);
        floatCloseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCloseConfirmDialog();
            }
        });
        floatHeader.addView(floatCloseBtn);

        floatExpandArea = new LinearLayout(this);
        floatExpandArea.setOrientation(LinearLayout.VERTICAL);
        floatExpandArea.setVisibility(View.GONE);
        floatExpandArea.setPadding(dp(6), dp(2), dp(6), dp(6));
        floatView.addView(floatExpandArea, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        LinearLayout inputRow = new LinearLayout(this);
        inputRow.setOrientation(LinearLayout.HORIZONTAL);
        inputRow.setGravity(Gravity.CENTER_VERTICAL);
        floatExpandArea.addView(inputRow, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        floatInput = new EditText(this);
        floatInput.setHint("搜索指令...");
        floatInput.setTextSize(12);
        floatInput.setTextColor(0xFF212121);
        floatInput.setHintTextColor(0xFF9E9E9E);
        floatInput.setBackground(createRoundDrawable(0xFFFFFFFF, dp(8)));
        floatInput.setPadding(dp(8), dp(6), dp(8), dp(6));
        floatInput.setMaxLines(1);
        floatInput.setImeOptions(6);
        floatInput.setInputType(1);
        inputRow.addView(floatInput, new LinearLayout.LayoutParams(0, dp(30), 1));

        floatSearchBtn = new Button(this);
        floatSearchBtn.setText("搜索");
        floatSearchBtn.setTextSize(12);
        floatSearchBtn.setTextColor(0xFFFFFFFF);
        floatSearchBtn.setBackground(createRoundDrawable(0xFF2196F3, dp(8)));
        floatSearchBtn.setPadding(dp(10), dp(4), dp(10), dp(4));
        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, dp(30));
        btnParams.leftMargin = dp(6);
        inputRow.addView(floatSearchBtn, btnParams);

        floatListScroll = new ScrollView(this);
        floatListScroll.setBackgroundColor(0xFFFFFFFF);
        floatListScroll.setFillViewport(true);
        LinearLayout.LayoutParams scrollParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(200));
        scrollParams.topMargin = dp(6);
        floatExpandArea.addView(floatListScroll, scrollParams);

        floatListContainer = new LinearLayout(this);
        floatListContainer.setOrientation(LinearLayout.VERTICAL);
        floatListContainer.setPadding(dp(2), dp(2), dp(2), dp(2));
        floatListScroll.addView(floatListContainer, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        floatArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                releaseInputFocusAndRestore();
                toggleExpand();
            }
        });

        floatSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                releaseInputFocusAndRestore();
                doSearch();
            }
        });

        floatInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, android.view.KeyEvent event) {
                doSearch();
                return true;
            }
        });

        floatView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                releaseInputFocusAndRestore();
            }
        });

        attachDragListener(floatHeader);
        attachInputClickListener();

        int widthPx = dp(WIDTH_DP);
        int heightPx = dp(HEIGHT_COLLAPSED_DP);
        layoutParams = new WindowManager.LayoutParams(
                widthPx, heightPx,
                getLayoutType(),
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT);
        layoutParams.gravity = Gravity.TOP | Gravity.START;
        layoutParams.x = 0;
        layoutParams.y = dp(80);

        try {
            windowManager.addView(floatView, layoutParams);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "悬浮窗添加失败", Toast.LENGTH_SHORT).show();
            stopSelf();
        }
    }

    private void toggleExpand() {
        expanded = !expanded;
        if (expanded) {
            floatExpandArea.setVisibility(View.VISIBLE);
            floatArrow.setText("⌃");
            updateLayoutByContent();
            if (floatListContainer.getChildCount() == 0) {
                showCategoryList();
            }
        } else {
            floatExpandArea.setVisibility(View.GONE);
            floatArrow.setText("⌄");
            layoutParams.height = dp(HEIGHT_COLLAPSED_DP);
            try {
                windowManager.updateViewLayout(floatView, layoutParams);
            } catch (Exception ignore) {}
        }
    }

    private void updateLayoutByContent() {
        try {
            int screenH;
            try {
                android.view.Display disp = windowManager.getDefaultDisplay();
                android.graphics.Point p = new android.graphics.Point();
                disp.getRealSize(p);
                screenH = p.y;
            } catch (Exception ex) {
                screenH = getResources().getDisplayMetrics().heightPixels;
            }
            int maxH = (int) (screenH * 0.85f);
            int widthSpec = View.MeasureSpec.makeMeasureSpec(layoutParams.width, View.MeasureSpec.EXACTLY);
            int heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
            floatView.measure(widthSpec, heightSpec);
            int naturalH = floatView.getMeasuredHeight();
            if (naturalH <= 0) naturalH = dp(HEIGHT_COLLAPSED_DP + 200);
            if (floatListScroll != null && naturalH > maxH) {
                int overflow = naturalH - maxH;
                int currentScrollH = floatListScroll.getHeight();
                int newScrollH = currentScrollH - overflow;
                if (newScrollH < dp(80)) newScrollH = dp(80);
                ViewGroup.LayoutParams lp = floatListScroll.getLayoutParams();
                if (lp != null) {
                    lp.height = newScrollH;
                    floatListScroll.setLayoutParams(lp);
                }
            }
            int h = Math.min(naturalH, maxH);
            if (h < dp(HEIGHT_COLLAPSED_DP + 80)) h = dp(HEIGHT_COLLAPSED_DP + 80);
            layoutParams.height = h;
            windowManager.updateViewLayout(floatView, layoutParams);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ========== 数据展示 ==========
    private void showCategoryList() {
        listState = 0;
        currentCategory = null;
        updateFloatTitle();
        floatListContainer.removeAllViews();
        if (floatBackBtn != null) floatBackBtn.setVisibility(View.GONE);
        List<String> categories = CodeData.getAllCategories();
        for (final String cat : categories) {
            // 跳过“大杂烩”分类
            if (CodeData.CATEGORY_MIX.equals(cat)) {
                continue;
            }
            View row = makeRowButton(cat);
            row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showCategoryItems(cat);
                }
            });
            floatListContainer.addView(row);
        }
        scrollListToTop();
        updateLayoutByContent();
    }

    private void showCategoryItems(final String category) {
        listState = 2;
        currentCategory = category;
        updateFloatTitle();
        floatListContainer.removeAllViews();
        ensureBackBtn();
        floatBackBtn.setVisibility(View.VISIBLE);
        List<CodeItem> items = CodeData.getCodesByCategory(this, category);
        if (items.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("该分区暂无指令");
            empty.setTextSize(12);
            empty.setTextColor(0xFF9E9E9E);
            empty.setPadding(dp(6), dp(8), dp(6), dp(6));
            floatListContainer.addView(empty);
        } else {
            for (CodeItem it : items) {
                addItemRow(null, it.title, it.code);
            }
        }
        scrollListToTop();
        updateLayoutByContent();
    }

    private void updateFloatTitle() {
        if (listState == 0) {
            floatTitle.setText("指令分类");
        } else if (listState == 1) {
            floatTitle.setText("搜索结果");
        } else if (listState == 2 && currentCategory != null) {
            floatTitle.setText(currentCategory);
        } else {
            floatTitle.setText("指令工具");
        }
    }

    private void doSearch() {
        String kw = floatInput.getText().toString().trim();
        if (TextUtils.isEmpty(kw)) {
            Toast.makeText(this, "请输入关键词", Toast.LENGTH_SHORT).show();
            return;
        }
        listState = 1;
        currentCategory = null;
        floatTitle.setText("搜索结果");
        floatListContainer.removeAllViews();
        ensureBackBtn();
        floatBackBtn.setVisibility(View.VISIBLE);
        List<CodeItem> results = CodeData.searchCodes(this, kw);
        if (results.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("未找到相关指令");
            empty.setTextSize(12);
            empty.setTextColor(0xFF9E9E9E);
            empty.setPadding(dp(6), dp(8), dp(6), dp(6));
            floatListContainer.addView(empty);
        } else {
            for (CodeItem item : results) {
                // 过滤掉大杂烩的搜索结果
                if (CodeData.CATEGORY_MIX.equals(item.category)) {
                    continue;
                }
                addItemRow(item.category, item.title, item.code);
            }
        }
        scrollListToTop();
        updateLayoutByContent();
    }

    private void ensureBackBtn() {
        if (floatBackBtn == null) {
            floatBackBtn = new Button(this);
            floatBackBtn.setText("←");
            floatBackBtn.setTextSize(12);
            floatBackBtn.setTextColor(0xFFFFFFFF);
            floatBackBtn.setBackground(createRoundDrawable(0xFF2196F3, dp(8)));
            floatBackBtn.setPadding(dp(8), dp(4), dp(8), dp(4));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(32), dp(30));
            lp.leftMargin = dp(4);
            floatBackBtn.setLayoutParams(lp);
            floatBackBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showCategoryList();
                }
            });
            ViewGroup inputRow = (ViewGroup) floatInput.getParent();
            if (inputRow != null) {
                inputRow.addView(floatBackBtn, 0);
            }
        }
    }

    private void addItemRow(String categoryTag, final String title, final String code) {
        // 如果 categoryTag 是大杂烩，不添加
        if (CodeData.CATEGORY_MIX.equals(categoryTag)) {
            return;
        }
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams cardLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        cardLp.topMargin = dp(6);
        card.setLayoutParams(cardLp);
        card.setBackground(createRoundDrawable(0xFFFFFFFF, dp(12)));
        int pad = dp(8);
        card.setPadding(pad, pad, pad, pad);
        card.setClickable(true);

        if (!TextUtils.isEmpty(categoryTag)) {
            TextView tagView = new TextView(this);
            tagView.setText(categoryTag);
            tagView.setTextSize(10);
            tagView.setTextColor(0xFF1976D2);
            tagView.setPadding(0, 0, 0, dp(2));
            card.addView(tagView);
        }
        TextView titleView = new TextView(this);
        titleView.setText(title);
        titleView.setTextSize(12);
        titleView.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        titleView.setTextColor(0xFF212121);
        card.addView(titleView);

        TextView codeView = new TextView(this);
        LinearLayout.LayoutParams codeLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        codeLp.topMargin = dp(4);
        codeView.setLayoutParams(codeLp);
        codeView.setText(code);
        codeView.setTextSize(11);
        codeView.setTextColor(0xFF424242);
        codeView.setTypeface(Typeface.MONOSPACE, Typeface.NORMAL);
        codeView.setBackground(createRoundDrawable(0xFFF5F5F5, dp(6)));
        codeView.setPadding(dp(8), dp(6), dp(8), dp(6));
        card.addView(codeView);

        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PrivilegeManager.sendCommand(code);
                Toast.makeText(FloatWindowService.this, "执行: " + title, Toast.LENGTH_SHORT).show();
            }
        };
        card.setOnClickListener(clickListener);
        codeView.setOnClickListener(clickListener);
        floatListContainer.addView(card);
    }

    private View makeRowButton(String text) {
        TextView btn = new TextView(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.topMargin = dp(6);
        btn.setLayoutParams(lp);
        btn.setText(text);
        btn.setTextSize(13);
        btn.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        btn.setTextColor(0xFFFFFFFF);
        btn.setBackground(createRoundDrawable(0xFF2196F3, dp(8)));
        btn.setGravity(Gravity.CENTER);
        btn.setPadding(dp(10), dp(8), dp(10), dp(8));
        return btn;
    }

    private void scrollListToTop() {
        if (floatListScroll != null) floatListScroll.scrollTo(0, 0);
    }

    // ========== 全局拖动 ==========
    private void attachDragListener(View dragView) {
        dragView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = layoutParams.x;
                        initialY = layoutParams.y;
                        initialTouchX = (int) event.getRawX();
                        initialTouchY = (int) event.getRawY();
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        int dx = (int) event.getRawX() - initialTouchX;
                        int dy = (int) event.getRawY() - initialTouchY;
                        layoutParams.x = initialX + dx;
                        layoutParams.y = initialY + dy;
                        try {
                            windowManager.updateViewLayout(floatView, layoutParams);
                        } catch (Exception ignore) {}
                        return true;
                }
                return false;
            }
        });
    }

    // ========== 输入法 ==========
    private void attachInputClickListener() {
        floatInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestInputFocusAndShowIme();
            }
        });
        floatInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    ensureImeShown();
                } else {
                    releaseInputFocusAndRestore();
                }
            }
        });
    }

    private void requestInputFocusAndShowIme() {
        try {
            int oldFlags = layoutParams.flags;
            int newFlags = oldFlags & ~WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            if (newFlags != oldFlags) {
                layoutParams.flags = newFlags;
                windowManager.updateViewLayout(floatView, layoutParams);
            }
            floatInput.setFocusable(true);
            floatInput.setFocusableInTouchMode(true);
            floatInput.requestFocus();
            ensureImeShown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void ensureImeShown() {
        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(floatInput, InputMethodManager.SHOW_IMPLICIT);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void releaseInputFocusAndRestore() {
        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(floatInput.getWindowToken(), 0);
            }
            floatInput.clearFocus();
            if (layoutParams != null) {
                layoutParams.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
                windowManager.updateViewLayout(floatView, layoutParams);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ========== 关闭确认弹窗 ==========
    private View closeConfirmView;
    private WindowManager.LayoutParams closeConfirmLp;

    private void showCloseConfirmDialog() {
        if (closeConfirmView != null) return;
        releaseInputFocusAndRestore();

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackground(createRoundDrawable(0xFFFFFFFF, dp(16)));
        int pad = dp(16);
        root.setPadding(pad, pad, pad, pad);

        TextView title = new TextView(this);
        title.setText("关闭悬浮窗");
        title.setTextSize(15);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setTextColor(0xFF212121);
        root.addView(title);

        TextView msg = new TextView(this);
        msg.setText("确认要关闭悬浮窗吗？");
        msg.setTextSize(13);
        msg.setTextColor(0xFF424242);
        LinearLayout.LayoutParams msgLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        msgLp.topMargin = dp(8);
        root.addView(msg, msgLp);

        LinearLayout btnRow = new LinearLayout(this);
        btnRow.setOrientation(LinearLayout.HORIZONTAL);
        btnRow.setGravity(Gravity.RIGHT);
        LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rowLp.topMargin = dp(16);
        root.addView(btnRow, rowLp);

        Button cancelBtn = new Button(this);
        cancelBtn.setText("取消");
        cancelBtn.setTextSize(13);
        cancelBtn.setTextColor(0xFF1565C0);
        cancelBtn.setBackground(createRoundDrawable(0xFFFFFFFF, dp(8)));
        cancelBtn.setPadding(dp(12), dp(6), dp(12), dp(6));
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissCloseConfirmDialog();
            }
        });
        btnRow.addView(cancelBtn);

        Button okBtn = new Button(this);
        okBtn.setText("确认");
        okBtn.setTextSize(13);
        okBtn.setTextColor(0xFFFFFFFF);
        okBtn.setBackground(createRoundDrawable(0xFF2196F3, dp(8)));
        okBtn.setPadding(dp(12), dp(6), dp(12), dp(6));
        LinearLayout.LayoutParams okLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        okLp.leftMargin = dp(8);
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissCloseConfirmDialog();
                stopSelf();
            }
        });
        btnRow.addView(okBtn, okLp);

        closeConfirmLp = new WindowManager.LayoutParams(
                dp(240), ViewGroup.LayoutParams.WRAP_CONTENT,
                getLayoutType(),
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT);
        closeConfirmLp.gravity = Gravity.CENTER;
        closeConfirmView = root;
        windowManager.addView(closeConfirmView, closeConfirmLp);
    }

    private void dismissCloseConfirmDialog() {
        try {
            if (closeConfirmView != null && windowManager != null) {
                windowManager.removeView(closeConfirmView);
            }
        } catch (Exception ignore) {}
        closeConfirmView = null;
        closeConfirmLp = null;
    }

    // ========== 工具 ==========
    private int getLayoutType() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            return WindowManager.LayoutParams.TYPE_PHONE;
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