package com.ykq.tvbrowser;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import org.mozilla.geckoview.GeckoRuntime;
import org.mozilla.geckoview.GeckoRuntimeSettings;
import org.mozilla.geckoview.GeckoSession;
import org.mozilla.geckoview.GeckoView;

public class MainActivity extends Activity {
    private static final String HOME_URL =
            "http://192.168.110.31:5178/screen/hanging-output?lang=zh-CN";

    private static GeckoRuntime runtime;
    private GeckoSession session;
    private long lastRightKeyUp;
    private static final String SETTINGS_NAME = "tvbrowser_settings";
    private static final String AUTO_START = "auto_start";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        hideSystemUi();

        GeckoView geckoView = new GeckoView(this);
        geckoView.setFocusable(true);
        geckoView.setFocusableInTouchMode(true);

        session = new GeckoSession();
        session.setContentDelegate(new GeckoSession.ContentDelegate() {});

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
        setContentView(geckoView);
        geckoView.requestFocus(View.FOCUS_DOWN);
        session.loadUri(HOME_URL);
    }

    private void hideSystemUi() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_UP) {
            if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT) {
                long now = System.currentTimeMillis();
                if (now - lastRightKeyUp <= 750L) {
                    lastRightKeyUp = 0L;
                    showExitDialog();
                } else {
                    lastRightKeyUp = now;
                }
                return true;
            }
            if (event.getKeyCode() == KeyEvent.KEYCODE_MENU
                    || event.getKeyCode() == KeyEvent.KEYCODE_SETTINGS) {
                showSettingsDialog();
                return true;
            }
            if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && session != null) {
                session.goBack();
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
                .setPositiveButton("退出", (dialog, which) -> finish())
                .show();
    }

    private void showSettingsDialog() {
        CheckBox autoStart = new CheckBox(this);
        autoStart.setText("开机自动启动看板");
        autoStart.setTextSize(22f);
        autoStart.setPadding(16, 16, 16, 16);
        autoStart.setChecked(getPreferences().getBoolean(AUTO_START, true));

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(24, 8, 24, 8);
        container.addView(autoStart);

        new AlertDialog.Builder(this)
                .setTitle("设置")
                .setView(container)
                .setNegativeButton("取消", null)
                .setPositiveButton("保存", (dialog, which) -> setAutoStart(autoStart.isChecked()))
                .show();
    }

    private SharedPreferences getPreferences() {
        return getSharedPreferences(SETTINGS_NAME, Context.MODE_PRIVATE);
    }

    private void setAutoStart(boolean enabled) {
        getPreferences().edit().putBoolean(AUTO_START, enabled).apply();
        ComponentName receiver = new ComponentName(this, BootReceiver.class);
        getPackageManager().setComponentEnabledSetting(
                receiver,
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
