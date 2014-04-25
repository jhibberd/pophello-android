package com.example.pophello.app.view;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.pophello.app.R;
import com.example.pophello.app.model.Tag;
import com.example.pophello.app.model.server.EndpointTagsDELETE;
import com.example.pophello.app.utility.CurrentUser;

public class TagViewFragment extends Fragment implements
        View.OnClickListener,
        EndpointTagsDELETE.OnResponseListener {

    public interface OnTagViewListener {
        public void onTagAcknowledgementSubmitted();
        public void onTagAcknowledgementSucceed(String tagId);
        public void onTagAcknowledgementFailure();
    }

    private static final String TAG = "TagViewFragment";
    private static final String ARGS_TEXT = "text";
    private static final String ARGS_TAG_ID = "tag_id";
    private static final String ARGS_USER_NAME = "user_name";
    private static final String ARGS_USER_IMAGE_URL = "user_image_url";
    private Button mButtonAcknowledge;
    private String mTagId;
    private OnTagViewListener mListener;

    /**
     * Instantiate a parameterized instance of the class following the factory pattern:
     * http://developer.android.com/reference/android/app/Fragment.html
     */
    public static TagViewFragment instance(Tag tag) {

        TagViewFragment fragment = new TagViewFragment();
        Bundle args = new Bundle();
        args.putString(ARGS_TAG_ID, tag.id);
        args.putString(ARGS_TEXT, tag.text);
        args.putString(ARGS_USER_NAME, tag.userId);
        args.putString(ARGS_USER_IMAGE_URL, tag.userImageUrl);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mListener = (OnTagViewListener) activity;
    }

    /**
     * Create the fragment view.
     *
     * Set the text property of the TextView object to the text property of the tag.
     */
    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_tag_view, container, false);
        if (view == null) {
            Log.e(TAG, "failed to inflate tag view fragment");
            return null;
        }
        mButtonAcknowledge = (Button) view.findViewById(R.id.button_acknowledge);
        if (mButtonAcknowledge == null) {
            Log.e(TAG, "failed to find acknowledge button in view");
            return null;
        }
        TextView textTextView = (TextView) view.findViewById(R.id.tag_text);
        if (textTextView == null) {
            Log.e(TAG, "failed to find tag text view in fragment");
            return null;
        }

        Bundle args = getArguments();
        if (args == null) {
            Log.e(TAG, "failed to read arguments from TagViewFragment instance");
            return null;
        }
        String text = args.getString(ARGS_TEXT);
        textTextView.setText(text);

        String userName = args.getString(ARGS_USER_NAME);
        String userImageURL = args.getString(ARGS_USER_IMAGE_URL);
        UserView userView = (UserView) view.findViewById(R.id.user_view);
        userView.setUser(userName, userImageURL);

        mTagId = args.getString(ARGS_TAG_ID);
        mButtonAcknowledge.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        if (v == mButtonAcknowledge) {
            onButtonAcknowledgeClick();
        }
    }

    private void onButtonAcknowledgeClick() {

        Context context = getActivity();
        if (context == null) {
            Log.e(TAG, "Failed to access context from fragment");
            return;
        }

        mButtonAcknowledge.setEnabled(false);
        mListener.onTagAcknowledgementSubmitted();

        String userId = new CurrentUser(getActivity()).getUserId();
        new EndpointTagsDELETE(context, userId, mTagId, this).call();
    }

    @Override
    public void onEndpointTagsDELETEResponseSuccess() {
        mListener.onTagAcknowledgementSucceed(mTagId);
    }

    @Override
    public void onEndpointTagsDELETEResponseFailed() {
        mListener.onTagAcknowledgementFailure();
    }
}
