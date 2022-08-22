package com.log2c.cordova.plugin.fabulove;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.azhon.appupdate.config.UpdateConfiguration;
import com.azhon.appupdate.listener.OnDownloadListener;
import com.azhon.appupdate.manager.DownloadManager;
import com.google.gson.Gson;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;

public class FabuLovePlugin extends CordovaPlugin {
    private static final String TAG = FabuLovePlugin.class.getSimpleName();
    private static final String META_DATA_DOMAIN = "fabu_love_domain";
    private static final String META_DATA_TEAM_ID = "fabu_love_team_id";
    private DownloadManager downloadManager;

    @Override
    protected void pluginInitialize() {
        super.pluginInitialize();
        try {
            checkUpdate(false, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if ("checkUpdate".equalsIgnoreCase(action)) {
            JSONObject config = args.getJSONObject(0);
            boolean checkOnly = config.getBoolean("checkOnly");
            try {
                checkUpdate(checkOnly, callbackContext);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
                callbackContext.error(e.getMessage());
            }
            return true;
        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 1) {
                downloadManager.download();
            }
        } else {
            if (requestCode == 1) {
                if (!checkHasInstallPermission()) {
                    Toast.makeText(cordova.getContext().getApplicationContext(), getResourceId(cordova.getContext(), "tip_required_install_apk_permission", "string", cordova.getContext().getPackageName()), Toast.LENGTH_LONG).show();
                    requestInstallPermission();
                }
            }
        }
    }

    private void checkUpdate(boolean checkOnly, CallbackContext callbackContext) throws PackageManager.NameNotFoundException {
        final ApplicationInfo appInfo = cordova.getContext().getPackageManager().getApplicationInfo(cordova.getContext().getPackageName(), PackageManager.GET_META_DATA);
        final String domain = appInfo.metaData.getString(META_DATA_DOMAIN);
        final String teamId = appInfo.metaData.getString(META_DATA_TEAM_ID);
        final String versionName = getAppVersionName(cordova.getContext());
        Log.d(TAG, String.format("Domain: %1$s, Team ID: %2$s", domain, teamId));
        final String url = domain +
                "/api/app/checkupdate/" +
                teamId +
                "/android/" +
                cordova.getContext().getPackageName() +
                "/" +
                versionName;
        Log.d(TAG, "Final request URL: " + url);
        requestCheckUpdate(url, new RequestCallback() {
            @Override
            public void onResponse(ResponseModel model) throws JSONException {
                JSONObject result = new JSONObject();
                if (!model.isSuccess() && checkOnly && callbackContext != null) {    // 无更新
                    result.put("hasNewVersion", false);
                    callbackContext.success(result);
                    return;
                }
                ResponseModel.DataBean.VersionBean versionBean = model.getData().getVersion();
                final boolean forceUpdate = "force".equalsIgnoreCase(versionBean.getUpdateMode());
                final String logs = TextUtils.isEmpty(versionBean.getChangelog()) ? "" : versionBean.getChangelog();
                final String downloadUrl = domain + "/" + versionBean.getDownloadUrl();
                Log.d(TAG, "Download URL: " + downloadUrl);
                final long apkSize = versionBean.getSize();
                final int versionCode = Integer.parseInt(versionBean.getVersionCode());
                final String versionName = versionBean.getVersionStr();

                if (checkOnly && callbackContext != null) {
                    result.put("hasNewVersion", true);
                    result.put("changelog", logs);
                    result.put("version", versionName);
                    callbackContext.success(result);
                    return;
                }

                cordova.getActivity().runOnUiThread(() -> startDownloadAndInstall(callbackContext, forceUpdate, logs, downloadUrl, apkSize, versionCode, versionName, versionBean));
            }

            @Override
            public void onRequestError(Exception e) {
                if (callbackContext != null) {
                    callbackContext.error(e.getMessage());
                }
            }
        });
    }

    private void addDownloadCount(String appId, String versionId) {
        new Thread(() -> {
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            try {
                final ApplicationInfo appInfo = cordova.getContext().getPackageManager().getApplicationInfo(cordova.getContext().getPackageName(), PackageManager.GET_META_DATA);
                final String domain = appInfo.metaData.getString(META_DATA_DOMAIN);
                final String strUrl = domain +
                        "/api/count/" +
                        appId +
                        "/" +
                        versionId;
                URL url = new URL(strUrl);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(8000);
                connection.setReadTimeout(8000);
                InputStream in = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(in));
                StringBuilder sbd = new StringBuilder();
                String line;
                while (null != (line = reader.readLine())) {
                    sbd.append(line);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (null != reader) {
                        reader.close();
                    }
                    if (null != connection) {
                        connection.disconnect();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private boolean checkHasInstallPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            boolean hasInstallPermission = cordova.getActivity().getPackageManager().canRequestPackageInstalls();
            return hasInstallPermission;
        }
        return true;
    }

    private void startDownloadAndInstall(CallbackContext callbackContext, boolean forceUpdate, String logs, String downloadUrl, long apkSize, int verCode, String verName, ResponseModel.DataBean.VersionBean versionBean) {
        DownloadManager manager = DownloadManager.getInstance(cordova.getContext());
        DecimalFormat df = new DecimalFormat("0.00");
        BigDecimal size = new BigDecimal(apkSize)
                .divide(new BigDecimal(1024), 1, BigDecimal.ROUND_HALF_UP)
                .divide(new BigDecimal(1024), 1, BigDecimal.ROUND_HALF_UP);
        downloadManager = manager.setApkName(verName + ".apk")
                .setApkUrl(downloadUrl)
                .setSmallIcon(getSmallIcon())
                .setApkSize(df.format(size))
                .setApkDescription(logs)
                .setApkVersionCode(verCode)
                .setApkVersionName(verName)
                .setConfiguration(new UpdateConfiguration()
                        .setForcedUpgrade(forceUpdate)
                        .setShowNotification(true)
                        .setDialogButtonTextColor(Color.WHITE)
                        .setShowNotification(true)
                        .setUsePlatform(false)
                        .setOnDownloadListener(new OnDownloadListener() {
                            @Override
                            public void start() {

                            }

                            @Override
                            public void downloading(int max, int progress) {

                            }

                            @Override
                            public void done(File apk) {
                                addDownloadCount(versionBean.getAppId(), versionBean.get_id());
                            }

                            @Override
                            public void cancel() {

                            }

                            @Override
                            public void error(Exception e) {
                                if (callbackContext != null) {
                                    postErrorToCordova(-1, String.format("Download fail: %1$s", e.getMessage()), callbackContext);
                                }
                            }
                        }));

        if (checkHasInstallPermission()) {
            downloadManager.download();
        } else {
            requestInstallPermission();
        }
    }

    private void requestInstallPermission() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:" + cordova.getActivity().getPackageName()));
        cordova.setActivityResultCallback(this);
        cordova.getActivity().startActivityForResult(intent, 1, null);

    }

    private int getSmallIcon() {
        return getResourceId(cordova.getContext(), "ic_launcher", "mipmap", cordova.getContext().getPackageName());
    }

    private void postErrorToCordova(int code, String msg, CallbackContext callbackContext) {
        if (callbackContext == null) {
            return;
        }
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("code", code);
            jsonObject.put("msg", msg);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        callbackContext.error(jsonObject);
    }

    /**
     * 请求后台，检查更新
     *
     * @param strUrl   URL
     * @param callback 回调
     */
    private static void requestCheckUpdate(final String strUrl, final RequestCallback callback) {
        new Thread(() -> {
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            try {
                URL url = new URL(strUrl);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(8000);
                connection.setReadTimeout(8000);
                InputStream in = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(in));
                StringBuilder sbd = new StringBuilder();
                String line;
                while (null != (line = reader.readLine())) {
                    sbd.append(line);
                }
                String json = sbd.toString();
                callback.onResponse(new Gson().fromJson(json, ResponseModel.class));
            } catch (Exception e) {
                e.printStackTrace();
                callback.onRequestError(e);
            } finally {
                try {
                    if (null != reader) {
                        reader.close();
                    }
                    if (null != connection) {
                        connection.disconnect();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * 获取当前app version code
     */
    public static long getAppVersionCode(Context context) {
        long appVersionCode = 0;
        try {
            PackageInfo packageInfo = context.getApplicationContext()
                    .getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                appVersionCode = packageInfo.getLongVersionCode();
            } else {
                appVersionCode = packageInfo.versionCode;
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("", e.getMessage());
        }
        return appVersionCode;
    }

    /**
     * 获取当前app version name
     */
    public static String getAppVersionName(Context context) {
        String appVersionName = "";
        try {
            PackageInfo packageInfo = context.getApplicationContext()
                    .getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            appVersionName = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("", e.getMessage());
        }
        return appVersionName;
    }

    interface RequestCallback {
        void onResponse(ResponseModel model) throws JSONException;

        void onRequestError(Exception e);
    }

    /**
     * 获取资源 ID
     *
     * @param context       Context
     * @param pVariableName 资源名称
     * @param pResourceName 类型 "drawable || mipmap || string"
     * @param pPackageName  包名
     * @return ResID
     */
    public static int getResourceId(Context context, String pVariableName, String pResourceName, String pPackageName) {
        try {
            return context.getResources().getIdentifier(pVariableName, pResourceName, pPackageName);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
}
