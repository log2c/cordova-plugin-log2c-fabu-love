package com.log2c.cordova.plugin.fabulove;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.azhon.appupdate.listener.OnDownloadListener;
import com.azhon.appupdate.manager.DownloadManager;
import com.google.gson.Gson;

import org.apache.cordova.CallbackContext;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;

@SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal", "unused"})
public class UpdateHelper {
    private static final String TAG = UpdateHelper.class.getSimpleName();
    private static final String META_DATA_DOMAIN = "fabu_love_domain";
    private static final String META_DATA_TEAM_ID = "fabu_love_team_id";
    private DownloadManager mDownloadManager;
    private Context mContext;
    private static UpdateHelper instance;

    public static UpdateHelper getInstance(Context context) {
        if (instance == null) {
            instance = new UpdateHelper(context);
        }
        return instance;
    }

    public UpdateHelper(Context context) {
        this.mContext = context.getApplicationContext();
    }

    public void checkUpdate(Activity activity, boolean checkOnly, @Nullable CallbackContext callbackContext) {
        final ApplicationInfo appInfo;
        try {
            appInfo = activity.getPackageManager().getApplicationInfo(mContext.getPackageName(), PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            if (callbackContext != null) {
                callbackContext.error(e.getMessage());
            }
            return;
        }
        final String domain = appInfo.metaData.getString(META_DATA_DOMAIN);
        final String teamId = appInfo.metaData.getString(META_DATA_TEAM_ID);
        final String versionName = getAppVersionName(mContext);
        Log.d(TAG, String.format("Domain: %1$s, Team ID: %2$s", domain, teamId));
        final String url = domain +
                "/api/app/checkupdate/" +
                teamId +
                "/android/" +
                activity.getPackageName() +
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

                activity.runOnUiThread(() -> startDownloadAndInstall(activity, callbackContext, forceUpdate, logs, downloadUrl, apkSize, versionCode, versionName, versionBean));
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
                final ApplicationInfo appInfo = mContext.getPackageManager().getApplicationInfo(mContext.getPackageName(), PackageManager.GET_META_DATA);
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

    private void startDownloadAndInstall(Activity activity, @Nullable CallbackContext callbackContext, boolean forceUpdate, String logs, String downloadUrl, long apkSize, int verCode, String verName, ResponseModel.DataBean.VersionBean versionBean) {
        DecimalFormat df = new DecimalFormat("0.00");
        BigDecimal size = new BigDecimal(apkSize)
                .divide(new BigDecimal(1024), 1, RoundingMode.HALF_UP)
                .divide(new BigDecimal(1024), 1, RoundingMode.HALF_UP);
        mDownloadManager = new DownloadManager.Builder(activity)
                .apkUrl(downloadUrl)
                .apkName("v" + verCode + "_" + verName + ".apk")
                .smallIcon(getSmallIcon())
                .apkSize(df.format(size) + "MB")
                .apkDescription(logs)
                .apkVersionCode(verCode)
                .apkVersionName("V" + verName)
                .forcedUpgrade(forceUpdate)
                .showNotification(true)
                .dialogButtonTextColor(Color.WHITE)
                .onDownloadListener(new OnDownloadListener() {
                    @Override
                    public void start() {

                    }

                    @Override
                    public void downloading(int i, int i1) {

                    }

                    @Override
                    public void done(@NonNull File file) {
                        addDownloadCount(versionBean.getAppId(), versionBean.get_id());
                    }

                    @Override
                    public void cancel() {

                    }

                    @Override
                    public void error(@NonNull Throwable throwable) {
                        if (callbackContext != null) {
                            postErrorToCordova(-1, String.format("Download fail: %1$s", throwable.getMessage()), callbackContext);
                        }
                    }
                }).build();

        this.mDownloadManager.download();
    }

    private int getSmallIcon() {
        return getResourceId(mContext, "ic_launcher", "mipmap", mContext.getPackageName());
    }

    @SuppressWarnings("SameParameterValue")
    private void postErrorToCordova(int code, String msg, @Nullable CallbackContext callbackContext) {
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
