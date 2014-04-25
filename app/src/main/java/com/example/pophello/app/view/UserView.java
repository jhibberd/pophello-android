package com.example.pophello.app.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.pophello.app.R;
import com.example.pophello.app.utility.DownloadImageTask;
import com.example.pophello.app.utility.ImageHelper;

public class UserView extends LinearLayout {

    private static final String TAG = "UserView";
    private TextView mUserName;
    private ImageView mUserImage;

    public UserView(Context context, AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater inflater =
                (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.component_user_view, this, true);

        mUserName = (TextView) findViewById(R.id.userName);

        // set placeholder image
        mUserImage = (ImageView) findViewById(R.id.userImage);
        Resources resources = getResources();
        if (resources == null ) {
            Log.e(TAG, "failed to get resources");
            return;
        }
        ViewGroup.LayoutParams layoutParams = mUserImage.getLayoutParams();
        if (layoutParams == null) {
            Log.e(TAG, "layout param for user image is null");
            return;
        }
        Bitmap image = Bitmap.createBitmap(
                layoutParams.width, layoutParams.height, Bitmap.Config.ARGB_8888);
        image.eraseColor(resources.getColor(R.color.user_image_placeholder_color));
        image = ImageHelper.getRoundedCornerBitmap(image, layoutParams.width / 2);
        mUserImage.setImageBitmap(image);
    }

    /**
     * Set the user name and image.
     *
     * The user image is loaded asynchronously by URL.
     */
    public void setUser(String userId, String userImageUrl) {
        mUserName.setText(userId);
        new DownloadImageTask(mUserImage).execute(userImageUrl);
        invalidate();
        requestLayout();
    }
}
