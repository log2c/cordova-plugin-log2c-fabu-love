package com.log2c.cordova.plugin.fabulove;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@SuppressWarnings("unused")
public class FabuLovePlugin extends CordovaPlugin {
    private static final String TAG = FabuLovePlugin.class.getSimpleName();

    @Override
    protected void pluginInitialize() {
        super.pluginInitialize();
        UpdateHelper.getInstance(cordova.getContext()).checkUpdate(cordova.getActivity(), false, null);
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if ("checkUpdate".equalsIgnoreCase(action)) {
            JSONObject config = args.getJSONObject(0);
            boolean checkOnly = config.getBoolean("checkOnly");
            UpdateHelper.getInstance(cordova.getContext()).checkUpdate(cordova.getActivity(), checkOnly, callbackContext);
            return true;
        }
        return false;
    }
}
