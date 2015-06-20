package cz.innovare.workometer.db;

import android.provider.BaseColumns;

/**
 * Created by vasek on 20.6.15.
 */
public class DbContract {
    public class Worker implements BaseColumns {
        public static final String TABLE_NAME = "workers";
        public static final String COLUMNN_NAME = "name";
        public static final String COLUMNN_FINISHED_GOALS = "finished_goals";

    }

}
