package ru.evendate.android.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;

import de.hdodenhof.circleimageview.CircleImageView;
import ru.evendate.android.R;

public class IconView extends FrameLayout {
    ImageView mImageView;
    CircleImageView mBorder;
    int color;
    int width;

    public IconView(Context context) {
        super(context, null);
    }


    public IconView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IconView(Context context, AttributeSet attrs, int defaultStyle) {
        super(context, attrs, defaultStyle);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.IconView,
                0, 0);

        try {
            color = a.getColor(R.styleable.IconView_border_color, Color.TRANSPARENT);
            width = a.getDimensionPixelSize(R.styleable.IconView_border_width, 0);
        } finally {
            a.recycle();
        }

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.view_icon, this, true);

        if (attrs == null) {
            int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8,
                    getResources().getDisplayMetrics());
            this.setPadding(padding, padding, padding, padding);
        }
        mImageView = (ImageView) getChildAt(1);
        mBorder = (CircleImageView) getChildAt(0);
        mBorder.setBorderColor(color);
        mBorder.setBorderWidth(width);
    }

    public void setImageBitmap(Bitmap bitmap) {
        mImageView.setImageBitmap(bitmap);
    }

    public void setImageDrawable(Drawable drawable) {
        mImageView.setImageDrawable(drawable);
    }
}
