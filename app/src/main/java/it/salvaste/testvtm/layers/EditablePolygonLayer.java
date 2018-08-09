package it.salvaste.testvtm.layers;

import android.util.Log;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

import org.oscim.backend.canvas.Color;
import org.oscim.core.GeoPoint;
import org.oscim.event.Gesture;
import org.oscim.event.MotionEvent;
import org.oscim.layers.vector.VectorLayer;
import org.oscim.layers.vector.geometries.Drawable;
import org.oscim.layers.vector.geometries.PolygonDrawable;
import org.oscim.layers.vector.geometries.Style;
import org.oscim.map.Map;
import org.oscim.utils.geom.GeomBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EditablePolygonLayer extends VectorLayer {
    private Style mStyle;
    private GeometryFactory mFactory;
    private Drawable mDrawing;
    private List<Coordinate> mCoordinates = new ArrayList<>();

    public EditablePolygonLayer(Map map) {
        super(map);
        Style.Builder sb = Style.builder()
                .stippleColor(Color.YELLOW)
                .stippleWidth(2.0f)
                .strokeColor(Color.BLUE)
                .strokeWidth(3.0f)
                .fillColor(Color.RED)
                .fillAlpha(0.2f);
        mStyle = sb.build();
        mFactory = new GeometryFactory();
    }

    @Override
    public boolean onGesture(Gesture g, MotionEvent e) {
        Log.e("VECTOR LAYER", g.toString());
        e.getAction();
        if(g == Gesture.LONG_PRESS) {
            GeoPoint geoPoint = mMap.viewport().fromScreenPoint(e.getX(), e.getY());
            Point point = new GeomBuilder().point(geoPoint.getLongitude(), geoPoint.getLatitude()).toPoint();
            for (Drawable drawable : tmpDrawables) {
                if (drawable.getGeometry().contains(point)){
                    Geometry geometry = drawable.getGeometry();
                    Coordinate[] coordinates = geometry.getCoordinates();
                    coordinates[1].x = 13.2;
                    Style style = drawable.getStyle();
                    this.remove(drawable);
                    this.add(geometry.getFactory().createPolygon(coordinates), style);
                    this.update();
                    Log.e("VECTOR LAYER", "points: " + coordinates.length);
                }
                return true;
            }
        }
        else if(g == Gesture.TAP) {
            GeoPoint geoPoint = mMap.viewport().fromScreenPoint(e.getX(), e.getY());
            Coordinate newVertex = new Coordinate();
            newVertex.x = geoPoint.getLongitude();
            newVertex.y = geoPoint.getLatitude();
            mCoordinates.add(newVertex);
            // draw vertex of current point
            if(mCoordinates.size() >= 2) {
                // draw open contour line
            }
            if(mCoordinates.size() >= 3) {
                // draw polygon
                ArrayList<Coordinate> tmpCoordinates = new ArrayList<>(mCoordinates);
                tmpCoordinates.add(tmpCoordinates.get(0));
                if(mDrawing != null) this.remove(mDrawing);
                mDrawing = new PolygonDrawable(mFactory.createPolygon(tmpCoordinates.toArray(new Coordinate[0])), mStyle);
                this.add(mDrawing);
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
