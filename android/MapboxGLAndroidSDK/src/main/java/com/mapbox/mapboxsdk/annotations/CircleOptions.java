package com.mapbox.mapboxsdk.annotations;

import android.content.Context;

import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.views.CircleOverlayView;

public final class CircleOptions {

    private CircleOverlayView mCircle;

    public CircleOptions(Context c) {
        mCircle = new CircleOverlayView(c);
    }

    /**
     * Do not use this method. Used internally by the SDK.
     */
    public CircleOverlayView getCircle() {
        return mCircle;
    }

    public CircleOptions position(LatLng position) {
        mCircle.setPosition(position);
        return this;
    }

    public CircleOptions radius(double radius) {
        mCircle.setRadius(radius);

        return this;
    }

    public CircleOptions color(int color){

        mCircle.setColor(color);

        return this;
    }
}
