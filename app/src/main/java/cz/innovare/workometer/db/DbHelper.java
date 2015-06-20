package cz.innovare.workometer.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import cz.innovare.workometer.db.DbContract.Worker;

/**
 * Created by Vaclav Sraier on 20.6.15.
 */
public class DbHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "workometer.db";

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_WORKERS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL(DROP + Worker.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }


    public static final String CREATE_WORKERS = "CREATE TABLE " + Worker.TABLE_NAME + " (" + Worker._ID + " INTEGER PRIMARY KEY," +
            Worker.COLUMNN_NAME + " VARCHAR(64)," +
            Worker.COLUMNN_FINISHED_GOALS + " INTEGER" +
            ")";
    public static final String DROP = "DROP TABLE IF EXISTS ";

}
