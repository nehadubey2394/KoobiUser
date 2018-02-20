package views.popup;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mualab.org.user.R;

/**
 * Created by dharmraj on 28/11/17.
 **/

public class PopupHint extends LinearLayout {
   // private static final int CONTEXT_MENU_WIDTH = ScreenUtils.dpToPx(240);
    private View view;

    public PopupHint(Context context) {
        super(context);
        init();
    }

    private void init() {
        view = LayoutInflater.from(getContext()).inflate(R.layout.popup_hint_show, this, true);
        setOrientation(VERTICAL);
        setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    public void setText(String text){
        TextView textView = view.findViewById(R.id.tv_hint);
        if(!TextUtils.isEmpty(text))
            textView.setText(text);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    public void dismiss() {
        ((ViewGroup) getParent()).removeView(PopupHint.this);
    }
}
