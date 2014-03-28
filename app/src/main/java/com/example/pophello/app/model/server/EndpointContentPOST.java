package com.example.pophello.app.model.server;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class EndpointContentPOST extends Endpoint {

    public interface OnResponseListener {
        public void onEndpointContentPOSTResponseSuccess();
        public void onEndpointContentPOSTResponseFailed();
    }

    private static final String TAG = "EndpointContentPOST";
    private final double mLongitude;
    private final double mLatitude;
    private final String mText;
    private final OnResponseListener mListener;

    public EndpointContentPOST(
            Context context, double longitude, double latitude, String text,
            OnResponseListener listener) {

        super(context, HTTPMethod.POST);
        this.mLongitude = longitude;
        this.mLatitude = latitude;
        this.mText = text;
        this.mListener = listener;
    }

    @Override
    protected void buildURI(Uri.Builder uriBuilder) {
        uriBuilder.path("/tags");
    }

    @Override
    protected JSONObject getRequestBody() {
        try {
            JSONObject result = new JSONObject();
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
        mListener.onEndpointContentPOSTResponseSuccess();
    }

    @Override
    protected void onResponseFailed(JSONObject data) {
        mListener.onEndpointContentPOSTResponseFailed();
    }
}
