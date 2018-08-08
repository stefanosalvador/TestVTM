package it.salvaste.testvtm;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.linearref.LinearGeometryBuilder;

import org.oscim.android.MapPreferences;
import org.oscim.android.MapView;
import org.oscim.backend.canvas.Color;
import org.oscim.core.GeoPoint;
import org.oscim.core.MapPosition;
import org.oscim.core.Tile;
import org.oscim.event.Gesture;
import org.oscim.event.MotionEvent;
import org.oscim.layers.tile.buildings.BuildingLayer;
import org.oscim.layers.tile.vector.VectorTileLayer;
import org.oscim.layers.tile.vector.labeling.LabelLayer;
import org.oscim.layers.vector.VectorLayer;
import org.oscim.layers.vector.geometries.Drawable;
import org.oscim.layers.vector.geometries.PointDrawable;
import org.oscim.layers.vector.geometries.PolygonDrawable;
import org.oscim.layers.vector.geometries.Style;
import org.oscim.map.Map;
import org.oscim.theme.VtmThemes;
import org.oscim.tiling.source.mapfile.MapFileTileSource;
import org.oscim.tiling.source.mapfile.MapInfo;
import org.oscim.utils.ColorUtil;
import org.oscim.utils.geom.GeomBuilder;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    MapView mMapView;
    Map mMap;
    MapPreferences mPrefs;
    // Name of the map file in device storage
    private static final String MAP_FILE = "alpeadria.map";

    protected final int mContentView;

    public MainActivity(int contentView) {
        mContentView = contentView;
    }

    public MainActivity() {
        this(R.layout.activity_main);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(mContentView);

        setTitle(getClass().getSimpleName());

        mMapView = findViewById(R.id.mapView);
        mMap = mMapView.map();
        mPrefs = new MapPreferences(MainActivity.class.getName(), this);

        MapFileTileSource tileSource = new MapFileTileSource();
        String mapPath = new File(getExternalFilesDir(""), MAP_FILE).getAbsolutePath();
        if (tileSource.setMapFile(mapPath)) {
            VectorTileLayer tileLayer = mMap.setBaseMap(tileSource);
            mMap.layers().add(new BuildingLayer(mMap, tileLayer));
            mMap.layers().add(new LabelLayer(mMap, tileLayer));
            mMap.setTheme(VtmThemes.DEFAULT);
            MapInfo info = tileSource.getMapInfo();
            if (!info.boundingBox.contains(mMap.getMapPosition().getGeoPoint())) {
                MapPosition pos = new MapPosition();
                pos.setByBoundingBox(info.boundingBox, Tile.SIZE * 4, Tile.SIZE * 4);
                mMap.setMapPosition(pos);
                mPrefs.clear();
            }
            Style.Builder sb = Style.builder()
                    .strokeColor(Color.BLUE)
                    .strokeWidth(3.0f)
                    .fillColor(Color.RED)
                    .fillAlpha(0.2f);
            Style style = sb.build();
            GeomBuilder gb = new GeomBuilder();
            Geometry g = gb
                    .point(13.0, 46.0)
                    .point(13.0, 46.1)
                    .point(13.1, 46.1)
                    .point(13.1, 46.0)
                    .point(13.0, 46.0).toPolygon();
            VectorLayer vectorLayer = new VectorLayer(mMap) {
                @Override
                public boolean onGesture(Gesture g, MotionEvent e) {
                    Log.e("VECTOR LAYER", "gesture ?");
                    if(g == Gesture.LONG_PRESS) {
                        GeoPoint geoPoint = mMap.viewport().fromScreenPoint(e.getX(), e.getY());
                        Point point = new GeomBuilder().point(geoPoint.getLongitude(), geoPoint.getLatitude()).toPoint();
                        for (Drawable drawable : tmpDrawables) {
                            if (drawable.getGeometry().contains(point))
                                Log.e("VECTOR LAYER", "found ?");
                            return true;
                        }
                    }
                    else {
                        return super.onGesture(g, e);
                    }
                    return false;
                }
            };
            vectorLayer.add(new PolygonDrawable(g, style));
            //style = sb.buffer(0.02).fillColor(Color.YELLOW).build();
            //vectorLayer.add(new PointDrawable(46.0,13.0, style));
            //vectorLayer.add(new PointDrawable(46.1,13.0, style));
            //vectorLayer.add(new PointDrawable(46.1,13.1, style));
            //vectorLayer.add(new PointDrawable(46.0,13.1, style));
            vectorLayer.update();
            mMap.layers().add(vectorLayer);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        mPrefs.load(mMapView.map());
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        mPrefs.save(mMapView.map());
        mMapView.onPause();

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mMapView.onDestroy();

        super.onDestroy();
    }}
