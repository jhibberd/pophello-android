package com.example.pophello.app.model.server;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class EndpointTagsPOST extends Endpoint {

    public interface OnResponseListener {
        public void onEndpointTagsPOSTResponseSuccess();
        public void onEndpointTagsPOSTResponseFailed();
    }

    private static final String TAG = "EndpointTagsPOST";
    private final String mUserId;
    private final double mLongitude;
    private final double mLatitude;
    private final String mText;
    private final OnResponseListener mListener;

    public EndpointTagsPOST(
            Context context, String userId, double longitude, double latitude, String text,
            OnResponseListener listener) {

        super(context, HTTPMethod.POST);
        mUserId = userId;
        mLongitude = longitude;
        mLatitude = latitude;
        mText = text;
        mListener = listener;
    }

    @Override
    protected void buildURI(Uri.Builder uriBuilder) {
        uriBuilder.path("/tags");
    }

    @Override
    protected JSONObject getRequestBody() {
        try {
            JSONObject result = new JSONObject();
            result.put("user_id", mUserId);
            result.put("lat", mLatitude);
            result.put("lng", mLongitude);
            result.put("text", mText);
            return result;

        } catch (JSONException e) {
            Log.d(TAG, "failed to create request body object");
            return null;
        }
    }

    @Override
    protected void onResponseSuccess(JSONObject data) {
        mListener.onEndpointTagsPOSTResponseSuccess();
    }

    @Override
    protected void onResponseFailed(JSONObject data) {
        mListener.onEndpointTagsPOSTResponseFailed();
    }
}
