package com.example.pophello.app.model.server;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.example.pophello.app.model.Tag;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class EndpointTagsGET extends Endpoint {

    public interface OnResponseListener {
        public void onEndpointTagsGETResponseSuccess(Tag[] tags);
        public void onEndpointTagsGETResponseFailed();
    }

    private static final String TAG = "EndpointTagsGET";
    private final String mUserId;
    private final double mLongitude;
    private final double mLatitude;
    private final OnResponseListener mListener;

    public EndpointTagsGET(
            Context context, String userId, double longitude, double latitude,
            OnResponseListener listener) {

        super(context, HTTPMethod.GET);
        mUserId = userId;
        mLongitude = longitude;
        mLatitude = latitude;
        mListener = listener;
    }

    @Override
    protected void buildURI(Uri.Builder uriBuilder) {
        uriBuilder.path("/tags");
        uriBuilder.appendQueryParameter("user_id", mUserId);
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
                String userId = dataElement.getString("user_id");
                String userImageUrl = dataElement.getString("user_image_url");
                tags[i] = new Tag(id, lat, lng, text, userId, userImageUrl);
            }
            mListener.onEndpointTagsGETResponseSuccess(tags);

        } catch (JSONException e) {
            Log.e(TAG, "badly formed response from server");
            mListener.onEndpointTagsGETResponseFailed();
        }
    }

    @Override
    protected void onResponseFailed(JSONObject data) {
        mListener.onEndpointTagsGETResponseFailed();
    }
}