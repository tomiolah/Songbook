package com.bence.songbook.utils;

import com.j256.ormlite.android.apptools.OrmLiteConfigUtil;

import java.io.IOException;
import java.sql.SQLException;

public class DatabaseConfigUtil extends OrmLiteConfigUtil {
//    private static final Class<?>[] classes = new Class[]{Song.class};

    public static void main(final String[] args) throws SQLException, IOException {
        writeConfigFile("ormlite_config.txt");
    }
}
