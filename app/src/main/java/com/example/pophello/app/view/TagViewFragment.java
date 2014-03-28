package com.example.pophello.app.view;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.pophello.app.R;

public class TagViewFragment extends Fragment {

    private static final String TAG = "TagViewFragment";
    private static final String ARGS_TEXT = "text";

    /**
     * Instantiate a parameterized instance of the class following the factory pattern:
     * http://developer.android.com/reference/android/app/Fragment.html
     */
    public static TagViewFragment instance(String text) {
        TagViewFragment fragment = new TagViewFragment();
        Bundle args = new Bundle();
        args.putString(ARGS_TEXT, text);
        fragment.setArguments(args);
        return fragment;
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

        return view;
    }
}
