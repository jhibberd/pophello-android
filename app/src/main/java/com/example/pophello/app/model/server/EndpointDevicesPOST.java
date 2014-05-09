package com.example.pophello.app.model.server;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class EndpointDevicesPOST extends Endpoint {

    public interface OnResponseListener {
        public void onEndpointDevicesPOSTResponseSuccess();
        public void onEndpointDevicesPOSTResponseFailed();
    }

    private static final String TAG = "EndpointDevicesPOST";
    private final String mUserId;
    private final String mDeviceId;
    private final OnResponseListener mListener;

    public EndpointDevicesPOST(
            Context context, String userId, String deviceId, OnResponseListener listener) {

        super(context, HTTPMethod.POST);
        mUserId = userId;
        mDeviceId = deviceId;
        mListener = listener;
    }

    @Override
    protected void buildURI(Uri.Builder uriBuilder) {
        uriBuilder.path("/devices");
    }

    @Override
    protected JSONObject getRequestBody() {
        try {
            JSONObject result = new JSONObject();
            result.put("user_id", mUserId);
            result.put("device_id", mDeviceId);
            result.put("device_type", "google");
            return result;

        } catch (JSONException e) {
            Log.d(TAG, "failed to create request body object");
            return null;
        }
    }

    @Override
    protected void onResponseSuccess(JSONObject data) {
        mListener.onEndpointDevicesPOSTResponseSuccess();
    }

    @Override
    protected void onResponseFailed(JSONObject data) {
        mListener.onEndpointDevicesPOSTResponseFailed();
    }
}
