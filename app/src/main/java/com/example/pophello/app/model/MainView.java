package com.example.pophello.app.model;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;

import com.example.pophello.app.R;
import com.example.pophello.app.view.TagCreateFailureFragment;
import com.example.pophello.app.view.TagCreateFragment;
import com.example.pophello.app.view.TagCreateSuccessFragment;
import com.example.pophello.app.view.TagViewFragment;

/**
 * Manages the display of the fragments on the main activity.
 */
public class MainView {

    private final FragmentManager mFragmentManager;
    private Fragment mVisibleFragment;

    public MainView(FragmentManager fragmentManager) {
        mFragmentManager = fragmentManager;
    }

    public void presentTag(Tag tag) {

        Fragment fragment = TagViewFragment.instance(tag.text);

        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        fragmentTransaction.replace(R.id.container, fragment);
        fragmentTransaction.commit();

        mVisibleFragment = fragment;
    }

    public void presentTagCreate() {

        Fragment fragment = new TagCreateFragment();

        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        fragmentTransaction.replace(R.id.container, fragment);
        fragmentTransaction.commit();

        mVisibleFragment = fragment;
    }

    public void presentTagCreateSuccess() {

        Fragment fragment = new TagCreateSuccessFragment();

        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        fragmentTransaction.replace(R.id.container, fragment);
        fragmentTransaction.commit();

        mVisibleFragment = fragment;
    }

    public void presentTagCreateFailure() {

        Fragment fragment = new TagCreateFailureFragment();

        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        fragmentTransaction.replace(R.id.container, fragment);
        fragmentTransaction.commit();

        mVisibleFragment = fragment;
    }

    /**
     * Show no view.
     *
     * This isn't animated because when this occurs the app isn't visible to the user.
     *
     * TODO: the activity is still visible when this occurs so maybe animate it after all
     */
    public void presentNothing() {
        if (mVisibleFragment == null) {
            return;
        }
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        fragmentTransaction.remove(mVisibleFragment);
        fragmentTransaction.commit();
        mVisibleFragment = null;
    }

    public Fragment getVisibleFragment() {
        return mVisibleFragment;
    }
}
