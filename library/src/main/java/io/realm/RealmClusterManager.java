package io.realm;

import android.content.Context;
import android.support.annotation.NonNull;


import com.google.android.gms.maps.GoogleMap;
import com.google.maps.android.MarkerManager;
import com.google.maps.android.clustering.ClusterManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import io.realm.internal.RealmObjectProxy;
import io.realm.internal.Row;
import io.realm.internal.Table;

/**
 * An implementation of the {@link ClusterManager} that handles processing a {@link RealmResults}
 * list and lookup of the respective columnName/Index to query the lat/long in order to store them
 * in the {@link RealmClusterWrapper}.
 */
public class RealmClusterManager<M extends RealmObject>
        extends ClusterManager<RealmClusterWrapper<M>> {

    private long titleColumnIndex = -1;

    public RealmClusterManager(Context context,
                               GoogleMap map

    ) {
        super(context, map);
    }

    public RealmClusterManager(Context context,
                               GoogleMap map,
                               @NonNull OnClusterItemClickListener<RealmClusterWrapper<M>> onClusterItemClickListener,
                               @NonNull GoogleMap.OnMapClickListener onMapClickListener


    ) {
        super(context, map);
        map.setOnMapClickListener(onMapClickListener);
        setOnClusterItemClickListener(onClusterItemClickListener);

    }

    public RealmClusterManager(Context context, GoogleMap map, MarkerManager markerManager) {
        super(context, map, markerManager);
    }

    public RealmClusterManager(Context context,
                               GoogleMap map,
                               MarkerManager markerManager,
                               @NonNull OnClusterItemClickListener<RealmClusterWrapper<M>> onClusterItemClickListener,
                               @NonNull GoogleMap.OnMapClickListener onMapClickListener

    ) {
        super(context, map, markerManager);
        map.setOnMapClickListener(onMapClickListener);
        setOnClusterItemClickListener(onClusterItemClickListener);
    }

    @Override
    public void addItems(Collection<RealmClusterWrapper<M>> items) {
        throw new IllegalStateException("Use updateRealmResults instead");
    }

    @Override
    public void addItem(RealmClusterWrapper<M> myItem) {
        throw new IllegalStateException("Use addRealmResultItems instead");
    }

    public void updateRealmResults(
            RealmResults<M> realmResults,
            String titleColumnName,
            String latitudeColumnName,
            String longitudeColumnName,
            String description,
            String id
            ) {
        super.clearItems();
        final Table table = realmResults.getTable () .getTable();

        titleColumnIndex = table.getColumnIndex(titleColumnName);
        if (titleColumnIndex == Table.NO_MATCH) {
            throw new IllegalStateException("titleColumnName not valid.");
        }
        long latIndex = table.getColumnIndex(latitudeColumnName);
        long descriptionIndex = table.getColumnIndex(description);
        long idIndex = table.getColumnIndex(id);
        if (latIndex == Table.NO_MATCH) {
            throw new IllegalStateException("latitudeColumnName not valid.");
        }
        long longIndex = table.getColumnIndex(longitudeColumnName);
        if (longIndex == Table.NO_MATCH) {
            throw new IllegalStateException("longitudeColumnName not valid.");
        }

        List<RealmClusterWrapper<M>> wrappedItems = new ArrayList<>(realmResults.size());
        for (int i = 0; i < realmResults.size(); i++) {
            M realmResult = realmResults.get(i);
            Row row = ((RealmObjectProxy) realmResult).realmGet$proxyState().getRow$realm();
            RealmClusterWrapper<M> wrappedItem = new RealmClusterWrapper<>(
                    realmResult,
                    getTitle(row, titleColumnIndex),
                    getValue(row, table.getColumnType(latIndex), latIndex),
                    getValue(row, table.getColumnType(longIndex), longIndex),
                    row.getString(descriptionIndex),
                    row.getLong(idIndex)
                    );
            wrappedItems.add(wrappedItem);
        }
        super.addItems(wrappedItems);
    }

    private double getValue(Row row, RealmFieldType columnType, long columnIndex) {
        if (columnType == RealmFieldType.DOUBLE) {
            return row.getDouble(columnIndex);
        } else if (columnType == RealmFieldType.FLOAT) {
            return row.getFloat(columnIndex);
        } else if (columnType == RealmFieldType.INTEGER) {
            return row.getLong(columnIndex);
        }
        throw new IllegalStateException("The value type needs to be of double, float or int");
    }

    public String getTitle(Row row, long columnIndex) {
        return row.getString(columnIndex);
    }



}
