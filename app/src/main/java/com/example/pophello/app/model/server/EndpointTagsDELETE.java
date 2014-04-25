package com.example.pophello.app.model.server;

import android.content.Context;
import android.net.Uri;

import org.json.JSONObject;

public class EndpointTagsDELETE extends Endpoint {

    public interface OnResponseListener {
        public void onEndpointTagsDELETEResponseSuccess();
        public void onEndpointTagsDELETEResponseFailed();
    }

    private final String mUserId;
    private final String mTagId;
    private final OnResponseListener mListener;

    public EndpointTagsDELETE(
            Context context, String userId, String tagId, OnResponseListener listener) {

        super(context, Endpoint.HTTPMethod.DELETE);
        mUserId = userId;
        mTagId = tagId;
        mListener = listener;
    }

    @Override
    protected void buildURI(Uri.Builder uriBuilder) {
        uriBuilder.path("/tags/" + mTagId);
        uriBuilder.appendQueryParameter("user_id", mUserId);
    }

    @Override
    protected void onResponseSuccess(JSONObject data) {
        mListener.onEndpointTagsDELETEResponseSuccess();
    }

    @Override
    protected void onResponseFailed(JSONObject data) {
        mListener.onEndpointTagsDELETEResponseFailed();
    }
}
