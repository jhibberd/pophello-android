package com.example.pophello.app.model.server;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.example.pophello.app.model.Tag;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class EndpointContentGET extends Endpoint {

    public interface OnResponseListener {
        public void onEndpointContentGETResponseSuccess(Tag[] tags);
        public void onEndpointContentGETResponseFailed();
    }

    private static final String TAG = "EndpointContentGET";
    private final double mLongitude;
    private final double mLatitude;
    private final OnResponseListener mListener;

    public EndpointContentGET(
            Context context, double longitude, double latitude, OnResponseListener listener) {

        super(context, HTTPMethod.GET);
        this.mLongitude = longitude;
        this.mLatitude = latitude;
        this.mListener = listener;
    }

    @Override
    protected void buildURI(Uri.Builder uriBuilder) {
        uriBuilder.path("/tags");
        uriBuilder.appendQueryParameter("lng", String.valueOf(mLongitude));
        uriBuilder.appendQueryParameter("lat", String.valueOf(mLatitude));
    }

    @Override
    protected void onResponseSuccess(JSONObject data) {

        try {
            JSONArray dataArray = data.getJSONArray("data");
            Tag[] tags = new Tag[dataArray.length()];
            for (int i = 0; i < dataArray.length(); i++) {
                JSONObject dataElement = dataArray.getJSONObject(i);
                String id = dataElement.getString("id");
                double lat = dataElement.getDouble("lat");
                double lng = dataElement.getDouble("lng");
                String text = dataElement.getString("text");
                tags[i] = new Tag(id, lat, lng, text);
            }
            mListener.onEndpointContentGETResponseSuccess(tags);

        } catch (JSONException e) {
            Log.e(TAG, "badly formed response from server");
            mListener.onEndpointContentGETResponseFailed();
        }
    }

    @Override
    protected void onResponseFailed(JSONObject data) {
        mListener.onEndpointContentGETResponseFailed();
    }
}