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

    public UserView(Context context, AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater inflater =
                (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.component_user_view, this, true);

        TextView userName = (TextView) findViewById(R.id.userName);
        userName.setText("James Hibberd");

        String imageURL = "https://fbcdn-profile-a.akamaihd.net/hprofile-ak-ash3/t1.0-1/c25.28.155.155/s50x50/946522_10151756173271454_1228308319_a.jpg";

        // set placeholder image
        ImageView userImage = (ImageView) findViewById(R.id.userImage);
        Resources resources = getResources();
        if (resources == null ) {
            Log.e(TAG, "failed to get resources");
            return;
        }
        ViewGroup.LayoutParams layoutParams = userImage.getLayoutParams();
        if (layoutParams == null) {
            Log.e(TAG, "layout param for user image is null");
            return;
        }
        Bitmap image = Bitmap.createBitmap(
                layoutParams.width, layoutParams.height, Bitmap.Config.ARGB_8888);
        image.eraseColor(resources.getColor(R.color.user_image_placeholder_color));
        image = ImageHelper.getRoundedCornerBitmap(image, layoutParams.width / 2);
        userImage.setImageBitmap(image);

        // async load image from URL
        new DownloadImageTask(userImage).execute(imageURL);
    }

}
