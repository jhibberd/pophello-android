package com.example.pophello.app.model;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;

import com.example.pophello.app.R;
import com.example.pophello.app.view.PendingFragment;
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
        presentFragment(fragment);
    }

    public void presentTagCreate() {
        Fragment fragment = new TagCreateFragment();
        presentFragment(fragment);
    }

    public void presentTagCreationSuccess() {
        Fragment fragment = new TagCreateSuccessFragment();
        presentFragment(fragment);
    }

    public void presentTagCreationFailure() {
        Fragment fragment = new TagCreateFailureFragment();
        presentFragment(fragment);
    }

    public void presentPending() {
        Fragment fragment = new PendingFragment();
        presentFragment(fragment);
    }

    /**
     * Present a fragment in the UI with an animation.
     */
    private void presentFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(
                R.animator.enter_from_right, R.animator.exit_to_left);
        fragmentTransaction.replace(R.id.container, fragment);
        fragmentTransaction.commit();
        mVisibleFragment = fragment;
    }

    /**
     * Show no view.
     *
     * This is animated because at the point when this is called the UI is still visible to the
     * user.
     */
    public void presentNothing() {
        if (mVisibleFragment == null) {
            return;
        }
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(
                R.animator.enter_from_right, R.animator.exit_to_left);
        fragmentTransaction.remove(mVisibleFragment);
        fragmentTransaction.commit();
        mVisibleFragment = null;
    }

    public Fragment getVisibleFragment() {
        return mVisibleFragment;
    }
}
