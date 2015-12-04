package com.kennetht.annd.popularmovies.Data;

import net.simonvt.schematic.annotation.Database;
import net.simonvt.schematic.annotation.Table;

/**
 * Created by Kenneth on 12/2/2015.
 */

@Database(version = Databases.VERSION)
public final class Databases {
    private Databases(){}

    public static final int VERSION = 1;

    @Table(FavoritesColumns.class)
    public static final String FAVORITES = "favorites";

}
