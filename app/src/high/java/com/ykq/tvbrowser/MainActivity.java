package com.ykq.tvbrowser;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.text.InputType;

import org.mozilla.geckoview.GeckoRuntime;
import org.mozilla.geckoview.GeckoRuntimeSettings;
import org.mozilla.geckoview.GeckoSession;
import org.mozilla.geckoview.GeckoView;

public class MainActivity extends Activity {
    private static final String DEFAULT_HOME_URL =
            "http://212.64.0.247:5178/screen/hanging-output?lang=zh-CN";
    private static final String SETTINGS_NAME = "tvbrowser_settings";
    private static final String AUTO_START = "auto_start";
    private static final String HOME_URL_SETTING = "home_url";

    private static GeckoRuntime runtime;
    private GeckoSession session;
    private TextView loadingView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        hideSystemUi();

        FrameLayout root = new FrameLayout(this);
        GeckoView geckoView = new GeckoView(this);
        geckoView.setFocusable(true);
        geckoView.setFocusableInTouchMode(true);

        loadingView = new TextView(this);
        loadingView.setText("启动中，请稍后…");
        loadingView.setTextColor(Color.WHITE);
        loadingView.setTextSize(28f);
        loadingView.setGravity(Gravity.CENTER);
        loadingView.setBackgroundColor(Color.rgb(20, 20, 20));

        root.addView(geckoView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        root.addView(loadingView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        setContentView(root);

        session = new GeckoSession();
        session.setContentDelegate(new GeckoSession.ContentDelegate() {
            @Override
            public void onTitleChange(GeckoSession session, String title) {
                hideLoading();
            }
        });
        if (runtime == null) {
            GeckoRuntimeSettings settings = new GeckoRuntimeSettings.Builder()
                    .consoleOutput(false)
                    .debugLogging(false)
                    .remoteDebuggingEnabled(false)
                    .webFontsEnabled(false)
                    .glMsaaLevel(0)
                    .build();
            runtime = GeckoRuntime.create(this, settings);
        }
        session.open(runtime);
        geckoView.setSession(session);
        geckoView.requestFocus(View.FOCUS_DOWN);
        session.loadUri(getHomeUrl());
        geckoView.postDelayed(this::hideLoading, 12000L);
    }

    private void hideLoading() {
        if (loadingView != null) {
            loadingView.setVisibility(View.GONE);
        }
    }

    private void hideSystemUi() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_UP) {
            if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT
                    || event.getKeyCode() == KeyEvent.KEYCODE_MENU
                    || event.getKeyCode() == KeyEvent.KEYCODE_SETTINGS) {
                showSettingsDialog();
                return true;
            }
            if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                showExitDialog();
                return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    private void showExitDialog() {
        new AlertDialog.Builder(this)
                .setTitle("退出看板")
                .setMessage("确定退出看板吗？")
                .setNegativeButton("取消", null)
                .setNeutralButton("设置", (dialog, which) -> showSettingsDialog())
                .setPositiveButton("退出", (dialog, which) -> finish())
                .show();
    }

    private void showSettingsDialog() {
        TextView homeUrlLabel = new TextView(this);
        homeUrlLabel.setText("首页启动地址");
        homeUrlLabel.setTextSize(18f);
        homeUrlLabel.setTextColor(Color.BLACK);

        EditText homeUrl = new EditText(this);
        homeUrl.setSingleLine(true);
        homeUrl.setTextSize(18f);
        homeUrl.setHint("首页地址 http://...");
        homeUrl.setTextColor(Color.BLACK);
        homeUrl.setHintTextColor(Color.GRAY);
        homeUrl.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
        homeUrl.setText(getHomeUrl());

        CheckBox autoStart = new CheckBox(this);
        autoStart.setText("开机自动启动看板");
        autoStart.setTextSize(22f);
        autoStart.setTextColor(Color.BLACK);
        autoStart.setPadding(16, 16, 16, 16);
        autoStart.setChecked(getPreferences().getBoolean(AUTO_START, true));

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(24, 8, 24, 8);
        container.addView(homeUrlLabel);
        container.addView(homeUrl);
        container.addView(autoStart);

        new AlertDialog.Builder(this)
                .setTitle("设置")
                .setView(container)
                .setNegativeButton("取消", null)
                .setPositiveButton("保存", (dialog, which) -> {
                    String url = homeUrl.getText().toString().trim();
                    if (url.length() == 0) {
                        url = DEFAULT_HOME_URL;
                    }
                    getPreferences().edit().putString(HOME_URL_SETTING, url).apply();
                    setAutoStart(autoStart.isChecked());
                    if (session != null) {
                        session.loadUri(url);
                    }
                })
                .show();
    }

    private SharedPreferences getPreferences() {
        return getSharedPreferences(SETTINGS_NAME, Context.MODE_PRIVATE);
    }

    private String getHomeUrl() {
        return getPreferences().getString(HOME_URL_SETTING, DEFAULT_HOME_URL);
    }

    private void setAutoStart(boolean enabled) {
        getPreferences().edit().putBoolean(AUTO_START, enabled).apply();
        ComponentName receiver = new ComponentName(this, BootReceiver.class);
        getPackageManager().setComponentEnabledSetting(receiver,
                enabled ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                        : PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideSystemUi();
    }

    @Override
    protected void onDestroy() {
        if (session != null) {
            session.close();
        }
        super.onDestroy();
    }
}
