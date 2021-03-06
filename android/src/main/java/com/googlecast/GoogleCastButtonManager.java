package com.googlecast;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.MediaRouteButton;
import android.util.AttributeSet;
import android.view.View;
import android.os.Handler;
import android.util.Log;

import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastState;
import com.google.android.gms.cast.framework.CastStateListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

public class GoogleCastButtonManager extends SimpleViewManager<MediaRouteButton> {

    public static final String REACT_CLASS = "RNGoogleCastButton";

    @Override
    public String getName() {
        return REACT_CLASS;
    }

    @Override
    public MediaRouteButton createViewInstance(ThemedReactContext context) {
      

        final MediaRouteButton button = new ColorableMediaRouteButton(context);
        if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS) {
               CastContext castContext = CastContext.getSharedInstance(context);
             CastButtonFactory.setUpMediaRouteButton(context, button);
               updateButtonState(button, castContext.getCastState());
                castContext.addCastStateListener(new CastStateListener() {
            @Override
            public void onCastStateChanged(int newState) {
                GoogleCastButtonManager.this.updateButtonState(button, newState);
            }
        });

        } else {
            Log.w("TAG", "Google Play services not installed on device. Cannot cast.");
        }

        return button;
    }

    @ReactProp(name = "tintColor", customType = "Color")
    public void setTintColor(ColorableMediaRouteButton button, Integer color) {
        if (color == null) return;
        button.applyTint(color);
    }

    private void updateButtonState(MediaRouteButton button, int state) {
        // hide the button when no device available (default behavior is show it disabled)
        if (CastState.NO_DEVICES_AVAILABLE == state) {
            button.setVisibility(View.GONE);
        } else {
            button.setVisibility(View.VISIBLE);
        }
    }

    // https://stackoverflow.com/a/41496796/384349
    private class ColorableMediaRouteButton extends MediaRouteButton {
        protected Drawable mRemoteIndicatorDrawable;

        public ColorableMediaRouteButton(Context context) {
            super(context);
        }

        public ColorableMediaRouteButton(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public ColorableMediaRouteButton(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
        }

        @Override
        public void setRemoteIndicatorDrawable(Drawable d) {
            mRemoteIndicatorDrawable = d;
            super.setRemoteIndicatorDrawable(d);
        }

        public void applyTint(final Integer color) {

            // mRemoteIndicatorDrawable is null when setRemoteIndicatorDrawable hasn't completed yet.
            // we exectue a delayed loop here every 100ms for 10 iterations to attempt to change the color
            // generally this happens within 1 iteration.
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                int totalAttemptsToChangeColor = 0;

                @Override
                public void run() {
                    if (mRemoteIndicatorDrawable != null) {
                        Drawable wrapDrawable = DrawableCompat.wrap(mRemoteIndicatorDrawable);
                        DrawableCompat.setTint(wrapDrawable, color);
                    } else {
                        if (totalAttemptsToChangeColor++ < 10)
                            handler.postDelayed(this, 100);
                    }

                }
            }, 100);
        }
    }
}
