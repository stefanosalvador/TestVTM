package it.salvaste.testvtm;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import org.oscim.android.MapPreferences;
import org.oscim.android.MapView;
import org.oscim.core.MapPosition;
import org.oscim.core.Tile;
import org.oscim.layers.tile.buildings.BuildingLayer;
import org.oscim.layers.tile.vector.VectorTileLayer;
import org.oscim.layers.tile.vector.labeling.LabelLayer;
import org.oscim.map.Map;
import org.oscim.theme.VtmThemes;
import org.oscim.tiling.source.mapfile.MapFileTileSource;
import org.oscim.tiling.source.mapfile.MapInfo;

import java.io.File;

import it.salvaste.testvtm.layers.EditableVectorLayer;

public class MainActivity extends AppCompatActivity {
    MapView mMapView;
    Map mMap;
    MapPreferences mPrefs;
    EditableVectorLayer mEditableVectorLayer;
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
            mEditableVectorLayer = new EditableVectorLayer(mMap);
            mMap.layers().add(mEditableVectorLayer);
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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.geometry_type, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // action with ID action_refresh was selected
            case R.id.action_point:
                mEditableVectorLayer.startEditing(EditableVectorLayer.POINT);
                break;
            case R.id.action_line:
                mEditableVectorLayer.startEditing(EditableVectorLayer.LINE);
                break;
            case R.id.action_polygon:
                mEditableVectorLayer.startEditing(EditableVectorLayer.POLYGON);
                break;
            default:
                break;
        }
        return true;
    }
}
