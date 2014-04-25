package com.example.pophello.app.view;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.pophello.app.R;
import com.example.pophello.app.model.ZoneManager;
import com.example.pophello.app.model.server.EndpointTagsPOST;
import com.example.pophello.app.utility.CurrentUser;

public class TagCreateFragment extends Fragment implements
        View.OnClickListener,
        EndpointTagsPOST.OnResponseListener {

    public interface OnTagCreateListener {
        public void onTagCreationSubmitted();
        public void onTagCreationSucceed();
        public void onTagCreationFailure();
    }

    private static final String TAG = "TagCreateFragment";
    private Button mButtonSubmit;
    private EditText mEditTextTag;
    private String mUserId;
    private ZoneManager mZoneManager;
    private OnTagCreateListener mListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mListener = (OnTagCreateListener) activity;
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_tag_create, container, false);
        if (view == null) {
            Log.e(TAG, "failed to inflate view");
            return null;
        }
        mButtonSubmit = (Button) view.findViewById(R.id.submit_button);
        if (mButtonSubmit == null) {
            Log.e(TAG, "failed to find submit button in view");
            return null;
        }
        mEditTextTag = (EditText) view.findViewById(R.id.tag_text);
        if (mEditTextTag == null) {
            Log.e(TAG, "failed to find edit text in view");
            return null;
        }

        CurrentUser currentUser = new CurrentUser(getActivity());
        mUserId = currentUser.getUserId();
        UserView userView = (UserView) view.findViewById(R.id.user_view);
        userView.setUser(currentUser.getUserId(), currentUser.getUserImageUrl());

        // set focus to the EditText control
        if (mEditTextTag.requestFocus()) {
            Activity activity = getActivity();
            if (activity == null) {
                Log.e(TAG, "failed to get activity");
                return null;
            }
            InputMethodManager inputMethodManager = (InputMethodManager)
                    activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.showSoftInput(mEditTextTag, InputMethodManager.SHOW_IMPLICIT);
        }

        mButtonSubmit.setOnClickListener(this);
        return view;
    }

    /** Begin regular high-precision location updates of the device.
     *
     * This is separate to `onResume` to better handle rendering the fragment as part of the
     * activity lifecycle and consuming location services which is largely an asynchronous process.
     */
    public void setZoneManager(ZoneManager zoneManager) {
        mZoneManager = zoneManager;
    }

    @Override
    public void onClick(View v) {
        if (v == mButtonSubmit) {
            onButtonSubmitClick();
        }
    }

    /**
     * Handle the user submitting their tag for creation.
     *
     * If the location service hasn't been able to obtain the device location then don't attempt to
     * submit the tag to the server and inform the user to try again shortly.
     *
     * If the submission can be made then disable the view to prevent the user from editing it
     * while the request is sent to the server.
     */
    private void onButtonSubmitClick() {

        Context context = getActivity();
        if (context == null) {
            Log.e(TAG, "Failed to access context from fragment");
            return;
        }

        Editable text = mEditTextTag.getText();
        if (text == null || text.length() == 0) {
            return;
        }

        Location location = null;
        if (mZoneManager != null) {
            location = mZoneManager.getLastPreciseLocation();
        }
        if (location == null) {
            Toast.makeText(context,
                    "Haven't got your location yet, try in a few seconds",
                    Toast.LENGTH_LONG).show();
            return;
        }

        mEditTextTag.setEnabled(false);
        mButtonSubmit.setEnabled(false);
        mListener.onTagCreationSubmitted();

        new EndpointTagsPOST(
                context, mUserId, location.getLongitude(), location.getLatitude(), text.toString(),
                this).call();
    }

    @Override
    public void onEndpointTagsPOSTResponseSuccess() {
        mListener.onTagCreationSucceed();
    }

    @Override
    public void onEndpointTagsPOSTResponseFailed() {
        mListener.onTagCreationFailure();
    }
}
