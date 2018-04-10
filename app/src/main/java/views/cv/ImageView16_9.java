package views.cv;

import android.content.Context;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

/**
 * Created by dharmraj on 24/3/18.
 */

public class ImageView16_9 extends AppCompatImageView{
    public ImageView16_9(Context context) {
        super(context);
    }

    public ImageView16_9(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ImageView16_9(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = getMeasuredWidth();

        //force a 16:9 aspect ratio
        int height = Math.round(width * .58f);
        setMeasuredDimension(width, height);
    }
}
