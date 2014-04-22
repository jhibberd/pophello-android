package com.example.pophello.app.view;

import android.app.Dialog;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.pophello.app.R;
import com.example.pophello.app.model.ServiceAvailabilityMonitor;
import com.google.android.gms.common.GooglePlayServicesUtil;

public class ServiceUnavailableFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "ServiceUnavailableFragment";
    private static final String ARGS_STATE = "state";

    public static ServiceUnavailableFragment instance(ServiceAvailabilityMonitor.State state) {
        ServiceUnavailableFragment fragment = new ServiceUnavailableFragment();
        Bundle args = new Bundle();
        args.putInt(ARGS_STATE, state.ordinal());
        fragment.setArguments(args);
        return fragment;
    }

    private Button mButtonFix;
    private ServiceAvailabilityMonitor.State mServiceUnavailableState;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Bundle args = getArguments();
        if (args == null) {
            Log.e(TAG, "failed to read arguments from ServiceUnavailableFragment instance");
            return null;
        }
        mServiceUnavailableState =
                ServiceAvailabilityMonitor.State.values()[args.getInt(ARGS_STATE)];

        View view = inflater.inflate(R.layout.fragment_service_unavailable, container, false);
        if (view == null) {
            Log.e(TAG, "failed to inflate tag view fragment");
            return null;
        }

        TextView message = (TextView) view.findViewById(R.id.message);
        if (message == null) {
            Log.e(TAG, "failed to find 'message' TextView in fragment");
            return null;
        }
        String messageText = "";
        switch (mServiceUnavailableState) {
            case GOOGLE_PLAY_SERVICES_MISSING:
                messageText = getResources().getString(
                        R.string.service_unavailable_google_play_services_missing);
                break;
            case LOCATION_PROVIDER_NETWORK_DISABLED:
                messageText = getResources().getString(
                        R.string.service_unavailable_location_provider_network_missing);
                break;
        }
        message.setText(messageText);

        mButtonFix = (Button) view.findViewById(R.id.button_fix);
        if (mButtonFix == null) {
            Log.e(TAG, "failed to find fix button in view");
            return null;
        }
        mButtonFix.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        if (v == mButtonFix) {
            onButtonFixClick();
        }
    }

    private void onButtonFixClick() {
        switch (mServiceUnavailableState) {
            case GOOGLE_PLAY_SERVICES_MISSING:
                fixGooglePlayServicesMissing();
                break;
            case LOCATION_PROVIDER_NETWORK_DISABLED:
                fixLocationProviderNetworkDisabled();
                break;
        }
    }

    private void fixGooglePlayServicesMissing() {
        int requestCode = 10; // TODO: poor
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity());
        Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, getActivity(), requestCode);
        dialog.show();
    }

    private void fixLocationProviderNetworkDisabled() {
        Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(intent);
    }
}
