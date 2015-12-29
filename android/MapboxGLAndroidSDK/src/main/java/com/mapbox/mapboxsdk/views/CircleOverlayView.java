package com.mapbox.mapboxsdk.views;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import com.mapbox.mapboxsdk.R;
import com.mapbox.mapboxsdk.geometry.LatLng;

/**
 * A {@code CircleOverlayView} provides a way to draw circle overlays above the Mapbox map view waiting the official feature.
 * Drawing is performed through canvas, as a result any circle will be drawn on top of any annotations.
 * <p/>
 * Use of {@code CircleOverlayView} is similar to standard Mapbox annotation ({@code Marker}, {@code Polygon},..), just
 * configure your circle overlay through a {@code CircleOptions}, define its position, radius, fill and stroke color.
 * Obtain an access token on the <a href="https://www.mapbox.com/account/apps/">Mapbox account page</a>.
 * <p/>
 * <strong>Warning:</strong> This proposed feature is only an alternative way to draw circle written in JAVA
 * but should be performed in C++ side for performance.
 *
 * @see MapView#setAccessToken(String)
 */
public class CircleOverlayView extends View {

    //region Attributes
    // Circle related attributes
    private double mRadius;
    private LatLng mPosition;
    private MapView mMapView;

    // Drawing attributes
    private Path mAccuracyPath;
    private RectF mAccuracyBounds;
    private Paint mAccuracyPaintFill;
    private Paint mAccuracyPaintStroke;

    private Rect mDirtyRect;
    private RectF mDirtyRectF;

    private PointF mCircleScreenPoint;
    private Matrix mCircleScreenMatrix;
    //endregion

    //region Constructors
    public CircleOverlayView(Context context) {
        super(context);
        initialize(context);
    }

    public CircleOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context);
    }

    public CircleOverlayView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context);
    }

    private void initialize(Context context) {

        // View configuration
        setEnabled(false);
        setWillNotDraw(false);

        // Layout params
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        setLayoutParams(lp);

        // Setup the custom paint
        // Default color values
        Resources resources = context.getResources();
        int defaultColor = resources.getColor(R.color.my_location_ring);
        float density= resources.getDisplayMetrics().density;

        mAccuracyPaintFill = new Paint();
        mAccuracyPaintFill.setAntiAlias(true);
        mAccuracyPaintFill.setStyle(Paint.Style.FILL);
        mAccuracyPaintFill.setColor(defaultColor);
        mAccuracyPaintFill.setAlpha((int) (255 * 0.25f));

        mAccuracyPaintStroke = new Paint();
        mAccuracyPaintStroke.setAntiAlias(true);
        mAccuracyPaintStroke.setStyle(Paint.Style.STROKE);
        mAccuracyPaintStroke.setStrokeWidth(0.5f * density);
        mAccuracyPaintStroke.setColor(defaultColor);
        mAccuracyPaintStroke.setAlpha((int) (255 * 0.5f));

        // Init paths
        mAccuracyPath = new Path();
        mAccuracyBounds = new RectF();

        // Init coordinates
        mPosition = new LatLng(0, 0);
        mCircleScreenMatrix = new Matrix();
    }
    //endregion

    //region Overriden View methods
    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.concat(mCircleScreenMatrix);

        boolean willDraw = !canvas.quickReject(mAccuracyPath, Canvas.EdgeType.AA);

        if (willDraw) {
            canvas.drawPath(mAccuracyPath, mAccuracyPaintFill);
            canvas.drawPath(mAccuracyPath, mAccuracyPaintStroke);
        }
    }
    //endregion

    //region Updating map display

    // Called as soon as the map view has been moved.
    public void update() {

        // compute new marker position
        // TODO add JNI method that takes existing pointf
        PointF screenPoint = mMapView.toScreenLocation(mPosition);
        mCircleScreenMatrix.reset();
        mCircleScreenMatrix.setTranslate(
                screenPoint.x,
                screenPoint.y);

        // adjust accuracy circle
        mAccuracyPath.reset();
        mAccuracyPath.addCircle(0.0f, 0.0f,
                (float) (mRadius / mMapView.getMetersPerPixelAtLatitude(
                        mPosition.getLatitude())), Path.Direction.CW);

        mAccuracyPath.computeBounds(mAccuracyBounds, false);
        mAccuracyBounds.inset(-1.0f, -1.0f);

        // invalidate changed pixels
        if (mDirtyRect == null) {
            mDirtyRect = new Rect();
            mDirtyRectF = new RectF();
        } else {
            // the old marker location
            invalidate(mDirtyRect);
        }

        mCircleScreenMatrix.mapRect(mDirtyRectF, mAccuracyBounds);
        mDirtyRectF.roundOut(mDirtyRect);
        invalidate(mDirtyRect); // the new marker location
    }

    void updateOnNextFrame() {

        if (mMapView != null) {
            mMapView.update();
        }
    }

    //endregion

    //region Getters
    public LatLng getPosition() {
        return mPosition;
    }

    public double getRadius() {
        return mRadius;
    }

    //endregion

    //region Setters
    public void setMapView(MapView mapView) {
        mMapView = mapView;
    }

    public void setPosition(LatLng position) {

        mPosition = position;

        updateOnNextFrame();
    }

    public void setRadius(double radius) {

        mRadius = radius;

        updateOnNextFrame();
    }

    public void setColor(int color){

        mAccuracyPaintFill.setColor(color);
        mAccuracyPaintStroke.setColor(color);
        mAccuracyPaintFill.setAlpha((int) (255 * 0.25f));
        mAccuracyPaintStroke.setAlpha((int) (255 * 0.5f));

        updateOnNextFrame();
    }
    //endregion
}