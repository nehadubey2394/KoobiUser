package com.popup;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.OvershootInterpolator;

/**
 * Created by dharmraj on 28/11/17.
 */

public class PopupHintManager implements View.OnAttachStateChangeListener{
    private static PopupHintManager instance;
    private PopupHint contextMenuView;
    private boolean isContextMenuDismissing;
    private boolean isContextMenuShowing;
    private Handler handler;
    private Runnable runnable;

    public static PopupHintManager getInstance() {
        if (instance == null) {
            instance = new PopupHintManager();
        }
        return instance;
    }

    private PopupHintManager() {

    }


 /*   public PopupHint getView(View openingView){
        if (contextMenuView == null) {
            contextMenuView = new PopupHint(openingView.getContext());
            contextMenuView.addOnAttachStateChangeListener(this);
        }
        return contextMenuView;
    }
*/
    public void show(View openingView, String txt) {
        if (contextMenuView == null) {
            showContextMenuFromView(openingView, txt);
            handler = new Handler();
            handler.postDelayed(runnable = new Runnable() {
                @Override
                public void run() {
                    dismiss();
                }
            }, 2000);
        } else {
            hideContextMenu();
        }
    }


    public void toggleFromView(View openingView) {
        if (contextMenuView == null) {
            showContextMenuFromView(openingView);
            handler = new Handler();
            handler.postDelayed(runnable = new Runnable() {
                @Override
                public void run() {
                  dismiss();
                }
            }, 2000);
        } else {
            hideContextMenu();
        }
    }

    private void showContextMenuFromView(final View openingView, String txtHint) {
        if (!isContextMenuShowing) {
            isContextMenuShowing = true;
            contextMenuView = new PopupHint(openingView.getContext());
            contextMenuView.addOnAttachStateChangeListener(this);
            contextMenuView.setText(txtHint);
            ((ViewGroup) openingView.getRootView().findViewById(android.R.id.content)).addView(contextMenuView);

            contextMenuView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    contextMenuView.getViewTreeObserver().removeOnPreDrawListener(this);
                    setupContextMenuInitialPosition(openingView);
                    performShowAnimation();
                    return false;
                }
            });
        }
    }


    private void showContextMenuFromView(final View openingView) {
        if (!isContextMenuShowing) {
            isContextMenuShowing = true;
            contextMenuView = new PopupHint(openingView.getContext());
            contextMenuView.addOnAttachStateChangeListener(this);
            ((ViewGroup) openingView.getRootView().findViewById(android.R.id.content)).addView(contextMenuView);

            contextMenuView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    contextMenuView.getViewTreeObserver().removeOnPreDrawListener(this);
                    setupContextMenuInitialPosition(openingView);
                    performShowAnimation();
                    return false;
                }
            });
        }
    }

    private void setupContextMenuInitialPosition(View openingView) {
        final int[] openingViewLocation = new int[2];
        openingView.getLocationOnScreen(openingViewLocation);
        int additionalBottomMargin = ScreenUtils.dpToPx(16);
        contextMenuView.setTranslationX(openingViewLocation[0] - contextMenuView.getWidth() / 3);
        contextMenuView.setTranslationY(openingViewLocation[1] - contextMenuView.getHeight() - additionalBottomMargin);
    }

    private void performShowAnimation() {
        contextMenuView.setPivotX(contextMenuView.getWidth() / 2);
        contextMenuView.setPivotY(contextMenuView.getHeight());
        contextMenuView.setScaleX(0.1f);
        contextMenuView.setScaleY(0.1f);
        contextMenuView.animate()
                .scaleX(1f).scaleY(1f)
                .setDuration(150)
                .setInterpolator(new OvershootInterpolator())
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        isContextMenuShowing = false;
                    }
                });
    }

    public void hideContextMenu() {
        if (!isContextMenuDismissing) {
            isContextMenuDismissing = true;
            performDismissAnimation();

            if(handler!=null)
                handler.removeCallbacks(runnable);
        }
    }

    private void performDismissAnimation() {
        contextMenuView.setPivotX(contextMenuView.getWidth() / 2);
        contextMenuView.setPivotY(contextMenuView.getHeight());
        contextMenuView.animate()
                .scaleX(0.1f).scaleY(0.1f)
                .setDuration(150)
                .setInterpolator(new AccelerateInterpolator())
                .setStartDelay(100)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (contextMenuView != null) {
                            contextMenuView.dismiss();
                        }
                        isContextMenuDismissing = false;
                    }
                });
    }

    public void dismiss() {
        if (contextMenuView != null) {
            hideContextMenu();
        }
    }

    @Override
    public void onViewAttachedToWindow(View v) {

    }

    @Override
    public void onViewDetachedFromWindow(View v) {
        contextMenuView = null;
    }
}
