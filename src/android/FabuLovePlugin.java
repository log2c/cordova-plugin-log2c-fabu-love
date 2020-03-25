package com.log2c.cordova.plugin.fabulove;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.cretin.www.cretinautoupdatelibrary.model.TypeConfig;
import com.cretin.www.cretinautoupdatelibrary.utils.AppUpdateUtils;
import com.google.gson.Gson;
import com.log2c.cordova.plugin.appupdate.AdvanceUpdateConfig;
import com.log2c.cordova.plugin.appupdate.AppUpdate;
import com.log2c.cordova.plugin.appupdate.UpdateInfoModel;

import org.apache.cordova.CordovaPlugin;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class FabuLovePlugin extends CordovaPlugin {
    private static final String TAG = FabuLovePlugin.class.getSimpleName();
    private static final String META_DATA_DOMAIN = "fabu_love_domain";
    private static final String META_DATA_TEAM_ID = "fabu_love_team_id";

    @Override
    protected void pluginInitialize() {
        super.pluginInitialize();
        try {
            initUpdateConfig();
            checkUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initUpdateConfig() {
        AdvanceUpdateConfig updateConfig = new AdvanceUpdateConfig();
        updateConfig.setUiThemeType(TypeConfig.UI_THEME_I)
                .setMethodType(TypeConfig.METHOD_GET)
                .setDataSourceType(TypeConfig.DATA_SOURCE_TYPE_JSON)
                .setShowNotification(true)
                .setNeedFileMD5Check(false)
                .setAutoDownloadBackground(false);
        AppUpdate.setConfig(updateConfig, cordova.getActivity().getApplication(), null);
    }

    private void checkUpdate() throws PackageManager.NameNotFoundException {
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
            public void onResponse(ResponseModel model) {
                Log.d(TAG, model.toString());
                if (model.isSuccess()) {    // 有更新
                    ResponseModel.DataBean.VersionBean versionBean = model.getData().getVersion();
                    final boolean forceUpdate = "force".equalsIgnoreCase(versionBean.getUpdateMode());
                    final String logs = TextUtils.isEmpty(versionBean.getChangelog()) ? "" : versionBean.getChangelog();
                    final String downloadUrl = domain + "/" + versionBean.getDownloadUrl();
                    final long apkSize = versionBean.getSize();
                    final int versionCode = Integer.parseInt(versionBean.getVersionCode());
                    final String versionName = versionBean.getVersionStr();

                    UpdateInfoModel infoModel = new UpdateInfoModel();
                    infoModel.setForceUpdate(forceUpdate);
                    infoModel.setLogs(logs);
                    infoModel.setApkUrl(downloadUrl);
                    infoModel.setApkSize(apkSize);
                    infoModel.setVersionCode(versionCode);
                    infoModel.setVersionName(versionName);

                    final String json = new Gson().toJson(infoModel);
                    cordova.getActivity().runOnUiThread(() -> AppUpdateUtils.getInstance()
                            .checkUpdate(json));
                }
            }

            @Override
            public void onRequestError(Exception e) {
                Log.e(TAG, "onRequestError: ", e);
            }
        });
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
        void onResponse(ResponseModel model);

        void onRequestError(Exception e);
    }
}
