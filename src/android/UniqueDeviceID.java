package hu.dpal.phonegap.plugins;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.Manifest;
import android.content.Context;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.lang.reflect.Method;

public class UniqueDeviceID extends CordovaPlugin {
    public static final String TAG = "UniqueDeviceID";

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        try {
            if (action.equals("get")) {
                final JSONObject ids = getDeviceId();
                callbackContext.success(ids);
            } else {
                callbackContext.error("Invalid action");
                return false;
            }
        } catch(Exception e) {
            callbackContext.error("Exception: " + e.getMessage());
            return false;
        }

        return true;
    }

    protected JSONObject getDeviceId() throws JSONException {
        final Context context = cordova.getActivity().getApplicationContext();
        final JSONObject ids = new JSONObject();

        String androidID = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
        if ("9774d56d682e549c".equals(androidID) || isBlank(androidID)) {
            androidID = "";
        } else {
            ids.put("androidID", androidID);
        }

        if (hasPermission(Manifest.permission.READ_PHONE_STATE)) {
            final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

            final String simID = tm.getSimSerialNumber();
            if (!isBlank(simID)) {
                ids.put("simSerial", simID);
            }

            final String meid = tm.getMeid();
            if (!isBlank(meid)) {
                ids.put("meid", meid);
            }

            final String imei = tm.getImei();
            if (!isBlank(imei)) {
                ids.put("imei", imei);
            }

            final String phoneNumber = tm.getLine1Number();
            if (!isBlank(phoneNumber)) {
                ids.put("phoneNumber", phoneNumber);
            }

            final String deviceID = tm.getDeviceId();
            if (!isBlank(deviceID)) {
                ids.put("deviceID", deviceID);
            }

            String uuid = androidID + deviceID + simID;
            uuid = String.format("%32s", uuid).replace(' ', '0');
            uuid = uuid.substring(0, 32);
            uuid = uuid.replaceAll("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5");
            ids.put("uuid", uuid);
        }

        return ids;
    }

    private static boolean isBlank(final String s) {
        return s == null || s.trim().length() < 1;
    }

    private boolean hasPermission(String permission) {
        try {
            final Method method = cordova.getClass().getMethod("hasPermission", permission.getClass());
            final Boolean isGranted = (Boolean) method.invoke(cordova, permission);
            return Boolean.TRUE.equals(isGranted);
        } catch (NoSuchMethodException e) {
            Log.w(TAG, "Cordova v" + CordovaWebView.CORDOVA_VERSION + " does not support API 23 runtime permissions so defaulting to GRANTED for " + permission);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }

        return true;
    }

}