package cz.innovare.workometer.view.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

import com.github.lzyzsd.circleprogress.DonutProgress;

import cz.innovare.workometer.R;
import cz.innovare.workometer.db.DbContract;
import cz.innovare.workometer.db.DbHelper;

public class StatusActivity extends Activity implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_status_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        return super.onMenuItemSelected(featureId, item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);
        ListView workers = (ListView) findViewById(R.id.workerListView);
        workers.setOnItemClickListener(this);
        workers.setOnItemLongClickListener(this);

        revalidate();

    }

    protected void setTotalTasks(int tasks) {
        DonutProgress prog = (DonutProgress)findViewById(R.id.totalTasksFinished);
        prog.setMax(tasks);
        prog.setProgress(tasks);
    }

    protected void setOverallProgress(int sumOfTasks, int taskGoal) {
        DonutProgress prog = (DonutProgress)findViewById(R.id.overallProgress);
        prog.setMax(100);
        prog.setProgress(Math.round(((float) sumOfTasks) / ((float) taskGoal) * 100));
    }


    public void addNewUser(String name) {
        ContentValues cv = new ContentValues();
        cv.put(DbContract.Worker.COLUMNN_NAME,name);
        cv.put(DbContract.Worker.COLUMNN_FINISHED_GOALS, 0);

        DbHelper dbHelper = new DbHelper(getApplicationContext());
        dbHelper.getWritableDatabase().insert(DbContract.Worker.TABLE_NAME, null, cv);
    }

    public void showNewUserDialog(View view) {
        AlertDialog alertDialog = new AlertDialog.Builder(getApplicationContext())
                .setView(getLayoutInflater().inflate(R.layout.fragment_dialog_add_worker,null,false))
                .setTitle("Add new employee")
                .setPositiveButton("Add",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                Log.i("AddUserDialog","Closed");
                            }
                        }
                )
                .setNegativeButton("Cancel", null)
                .create();
        alertDialog.show();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        DbHelper dbHelper = new DbHelper(getApplicationContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL("UPDATE " + DbContract.Worker.TABLE_NAME + " SET " + DbContract.Worker.COLUMNN_FINISHED_GOALS + " = " + DbContract.Worker.COLUMNN_FINISHED_GOALS + " + 1 WHERE "+ DbContract.Worker._ID + " = "+ Long.toString(l));
        db.close();
        revalidate();
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
        DbHelper dbHelper = new DbHelper(getApplicationContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DbContract.Worker.TABLE_NAME, DbContract.Worker._ID + " = ?", new String[]{Long.toString(l)});
        db.close();
        revalidate();
        return false;
    }

    public void revalidate() {
        ListView workers = (ListView) findViewById(R.id.workerListView);
        DbHelper dbHelper = new DbHelper(getApplicationContext());
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.query(DbContract.Worker.TABLE_NAME,new String[]{DbContract.Worker._ID,DbContract.Worker.COLUMNN_NAME, DbContract.Worker.COLUMNN_FINISHED_GOALS},null,null,null,null, DbContract.Worker.COLUMNN_NAME);
        int sumOfCompletedTasks = 0;
        c.moveToFirst();
        for(int x = 0; x < c.getCount(); x++) {
            sumOfCompletedTasks = sumOfCompletedTasks + c.getInt(c.getColumnIndex(DbContract.Worker.COLUMNN_FINISHED_GOALS));
            c.moveToNext();
        }
        c.moveToFirst();
        workers.setAdapter(new WorkersCursorAdapter(getApplicationContext(), c, PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getInt("taskGoal", 0),sumOfCompletedTasks));

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        setOverallProgress(sumOfCompletedTasks,sp.getInt("taskGoal",0));
        setTotalTasks(sumOfCompletedTasks);
    }




    protected class WorkersCursorAdapter extends CursorAdapter {

        int weekGoal = 0;
        int sumOfFinishedTasks;

        public WorkersCursorAdapter(Context context, Cursor c, int weekGoal, int sumOfFinishedTasks) {
            super(context,c);
            this.weekGoal = weekGoal;
            this.sumOfFinishedTasks = sumOfFinishedTasks;

        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
            return getLayoutInflater().inflate(R.layout.listview_workers_layout,viewGroup,false);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            TextView name = (TextView)view.findViewById(R.id.workerName);
            ProgressBar bar = (ProgressBar) view.findViewById(R.id.workerProgressBar);
            TextView title = (TextView) view.findViewById(R.id.workerProgressTitle);

            int tasks = cursor.getInt(cursor.getColumnIndex(DbContract.Worker.COLUMNN_FINISHED_GOALS));
            bar.setMax(sumOfFinishedTasks);
            name.setText(cursor.getString(cursor.getColumnIndex(DbContract.Worker.COLUMNN_NAME)));
            bar.setProgress(tasks);
            title.setText(Math.round((float)tasks*100f/(float)sumOfFinishedTasks) + "%");
        }


    }
}
