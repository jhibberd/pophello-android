package com.example.pophello.app.view;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.pophello.app.R;

public class TagCreateSuccessFragment extends Fragment {

    private static final String TAG = "TagCreateSuccessFragment";

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_tag_create_success, container, false);
    }
}
