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
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.CheckBox;
import android.widget.LinearLayout;

public class MainActivity extends Activity {
    private static final String HOME_URL =
            "http://192.168.110.31:5178/screen/hanging-output?lang=zh-CN";
    private WebView webView;
    private long lastRightKeyUp;
    private static final String SETTINGS_NAME = "tvbrowser_settings";
    private static final String AUTO_START = "auto_start";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        hideSystemUi();
        webView = new WebView(this);
        webView.setFocusable(true);
        webView.setFocusableInTouchMode(true);
        webView.requestFocus(View.FOCUS_DOWN);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return false;
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request,
                                        WebResourceError error) {
                if (request.isForMainFrame()) {
                    view.loadDataWithBaseURL(null,
                            "<html><body style='background:#111;color:#fff;text-align:center;padding-top:20%;font-size:32px'>页面加载失败，请检查网络后按返回键重试</body></html>",
                            "text/html", "UTF-8", null);
                }
            }
        });
        webView.setWebChromeClient(new WebChromeClient());
        setContentView(webView);
        webView.loadUrl(HOME_URL);
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
            if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && webView != null
                    && webView.canGoBack()) {
                webView.goBack();
                return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    private void showExitDialog() {
        new AlertDialog.Builder(this).setTitle("退出看板").setMessage("确定退出看板吗？")
                .setNegativeButton("取消", null)
                .setPositiveButton("退出", (dialog, which) -> finish()).show();
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
        new AlertDialog.Builder(this).setTitle("设置").setView(container)
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
        getPackageManager().setComponentEnabledSetting(receiver,
                enabled ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                        : PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideSystemUi();
        if (webView != null) {
            webView.onResume();
        }
    }

    @Override
    protected void onPause() {
        if (webView != null) {
            webView.onPause();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.destroy();
        }
        super.onDestroy();
    }
}
