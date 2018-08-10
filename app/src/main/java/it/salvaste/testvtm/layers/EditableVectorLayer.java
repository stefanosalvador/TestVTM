package it.salvaste.testvtm.layers;

import android.util.Log;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

import org.oscim.backend.canvas.Color;
import org.oscim.core.GeoPoint;
import org.oscim.event.Gesture;
import org.oscim.event.MotionEvent;
import org.oscim.layers.vector.VectorLayer;
import org.oscim.layers.vector.geometries.Drawable;
import org.oscim.layers.vector.geometries.LineDrawable;
import org.oscim.layers.vector.geometries.PointDrawable;
import org.oscim.layers.vector.geometries.PolygonDrawable;
import org.oscim.layers.vector.geometries.Style;
import org.oscim.map.Map;
import org.oscim.utils.geom.GeomBuilder;

import java.util.ArrayList;
import java.util.List;

public class EditableVectorLayer extends VectorLayer {
    private boolean mEditingMode = false;
    private int mGeometryType;
    private Style mPointStyle;
    private Style mLineStyle;
    private Style mPolygonStyle;
    private GeometryFactory mFactory;
    private Drawable mLineDrawing;
    private Drawable mPolygonDrawing;
    private List<Coordinate> mCoordinates;

    public static final int POINT = 1;
    public static final int LINE = 2;
    public static final int POLYGON = 3;

    public EditableVectorLayer(Map map) {
        super(map);
        mPointStyle = Style.builder()
                .fillColor(Color.BLUE)
                .fillAlpha(1f)
                .buffer(3)
                .scaleZoomLevel(20).build();
        mLineStyle = Style.builder()
                .strokeColor(Color.BLUE)
                .strokeWidth(2.0f).build();
        mPolygonStyle = Style.builder()
                .fillColor(Color.RED)
                .fillAlpha(0.5f).build();
        mFactory = new GeometryFactory();
    }

    public void startEditing(int geometryType) {
        mEditingMode = true;
        mGeometryType = geometryType;
        mLineDrawing = null;
        mPolygonDrawing = null;
        mCoordinates = new ArrayList<>();
        for (Drawable drawable : tmpDrawables) {
            this.remove(drawable);
        }
        this.update();
    }

    public void stopEditing() {
        mEditingMode = false;
    }

    @Override
    public boolean onGesture(Gesture g, MotionEvent e) {
        if(!mEditingMode)
            return false;
        if(g == Gesture.LONG_PRESS) {
            GeoPoint geoPoint = mMap.viewport().fromScreenPoint(e.getX(), e.getY());
            Point point = new GeomBuilder().point(geoPoint.getLongitude(), geoPoint.getLatitude()).toPoint();
            for (Drawable drawable : tmpDrawables) {
                if (drawable.getGeometry().contains(point)){
                    Log.e("VECTOR LAYER", "found !!!");
                }
                return false;
            }
        }
        else if(g == Gesture.TAP) {
            GeoPoint geoPoint = mMap.viewport().fromScreenPoint(e.getX(), e.getY());
            if(mGeometryType == POINT){
                // draw vertex of current point
                this.add(new PointDrawable(geoPoint.getLatitude(), geoPoint.getLongitude(), mPointStyle));
                this.update();
                stopEditing();
                return true;
            }
            boolean clickedFirstPoint = false;
            boolean clickedLastPoint = false;
            Coordinate newVertex = new Coordinate();
            newVertex.x = geoPoint.getLongitude();
            newVertex.y = geoPoint.getLatitude();
            if(mCoordinates.size() >= 1) {
                Coordinate first = mCoordinates.get(0);
                Coordinate last = mCoordinates.get(mCoordinates.size() - 1);
                clickedFirstPoint = first.distance(newVertex) * mMap.getMapPosition().getScale() < 10;
                clickedLastPoint = last.distance(newVertex) * mMap.getMapPosition().getScale() < 10;
            }
            if(clickedLastPoint && mGeometryType == LINE) {
                stopEditing();
                return true;
            }
            else if(clickedLastPoint && mGeometryType == POLYGON) {
                return true;
            }
            else if(clickedFirstPoint && mGeometryType == POLYGON) {
                Coordinate first = mCoordinates.get(0);
                mCoordinates.add(first);
                stopEditing();
            }
            else {
                mCoordinates.add(newVertex);
            }

            if(!(clickedFirstPoint || clickedLastPoint)) {
                // draw vertex of current point
                this.add(new PointDrawable(geoPoint.getLatitude(), geoPoint.getLongitude(), mPointStyle));
                this.update();
            }
            if(mCoordinates.size() >= 2 && (mGeometryType == LINE || mGeometryType == POLYGON)) {
                // draw open contour line
                if(mLineDrawing != null) this.remove(mLineDrawing);
                if(mEditingMode == false) {
                    this.add(new LineDrawable(mFactory.createLineString(mCoordinates.toArray(new Coordinate[0])), mLineStyle));
                }
                else {
                    mLineDrawing = new LineDrawable(mFactory.createLineString(mCoordinates.toArray(new Coordinate[0])), mLineStyle);
                    this.add(mLineDrawing);
                }
                this.update();
            }
            if(mCoordinates.size() >= 3 && mGeometryType == POLYGON) {
                // draw polygon
                ArrayList<Coordinate> tmpCoordinates = new ArrayList<>(mCoordinates);
                if(!clickedFirstPoint)
                    tmpCoordinates.add(tmpCoordinates.get(0));
                if(mPolygonDrawing != null) this.remove(mPolygonDrawing);
                mPolygonDrawing = new PolygonDrawable(mFactory.createPolygon(tmpCoordinates.toArray(new Coordinate[0])), mPolygonStyle);
                this.add(mPolygonDrawing);
                this.update();
            }
            return true;
        }
        else {
            return super.onGesture(g, e);
        }
        return false;
    }

}
