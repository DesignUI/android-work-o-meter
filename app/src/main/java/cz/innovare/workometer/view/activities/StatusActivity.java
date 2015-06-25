package cz.innovare.workometer.view.activities;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.lzyzsd.circleprogress.DonutProgress;

import cz.innovare.workometer.R;
import cz.innovare.workometer.db.DbContract;
import cz.innovare.workometer.db.DbHelper;

public class StatusActivity extends Activity implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    public static final int TASK_MAXIMUM = 10000;
    public static final String TASK_GOAL_PREFERENCE = "taskGoal";
    public static final int DEFAULT_TASK_GOAL = 100;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_status_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        if (item.getItemId() == R.id.action_help) {
            Intent i = new Intent();
            i.setAction(Intent.ACTION_VIEW);
            i.setData(Uri.parse("https://github.com/vakabus/android-work-o-meter/blob/master/README.md#innovare-workometer"));
            startActivity(i);
        }
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


    /**
     * Updates number of finished tasks in UI. Changes value of the right progress circle
     *
     * @param tasks number of finished tasks
     */
    protected void setTotalTasks(int tasks) {
        DonutProgress prog = (DonutProgress) findViewById(R.id.totalTasksFinished);
        prog.setMax(tasks);
        prog.setProgress(tasks);
    }

    /**
     * Updates percentage in UI. Changes the left circular progress bar.
     *
     * @param sumOfTasks number of completed tasks
     * @param taskGoal   number of tasks required to finish a project
     */
    protected void setOverallProgress(int sumOfTasks, int taskGoal) {
        DonutProgress prog = (DonutProgress) findViewById(R.id.overallProgress);
        prog.setMax(100);
        if (sumOfTasks <= taskGoal) {
            prog.setProgress(Math.round(((float) sumOfTasks) / ((float) taskGoal) * 100));
        } else {
            prog.setProgress(prog.getMax());
        }
    }

    /**
     * Adds new employee to DB with 0 finished tasks.
     *
     * @param name name of the new employee to be added
     */
    public void addNewEmployee(String name) {
        ContentValues cv = new ContentValues();
        cv.put(DbContract.Worker.COLUMNN_NAME, name);
        cv.put(DbContract.Worker.COLUMNN_FINISHED_GOALS, 0);

        DbHelper dbHelper = new DbHelper(getApplicationContext());
        dbHelper.getWritableDatabase().insert(DbContract.Worker.TABLE_NAME, null, cv);
    }


    /**
     * Updates UI and shared preferences with new goal
     *
     * @param n number of tasks
     */
    public void setTaskTarget(int n) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        sp.edit().putInt(TASK_GOAL_PREFERENCE, n).commit();
        ((TextView) findViewById(R.id.currentTasksNumber)).setText(Integer.toString(n));
    }

    /**
     * Event handler. Displays dialog for adding new employee.
     *
     * @param view
     */
    public void showNewEmployeeDialog(View view) {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_add_worker);
        dialog.setTitle("New employee");
        dialog.findViewById(R.id.dialog_add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addNewEmployee(((EditText) dialog.findViewById(R.id.newEmployeeName)).getText().toString());
                dialog.dismiss();
                revalidate();
            }
        });
        dialog.findViewById(R.id.dialog_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    /**
     * Event handler. Displays dialog for setting new goal of tasks.
     *
     * @param view
     */
    public void showTargetChangeDialog(View view) {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_change_task_target);
        dialog.setTitle("Tasks");
        NumberPicker picker = (NumberPicker) dialog.findViewById(R.id.number_of_tasks);
        picker.setMaxValue(TASK_MAXIMUM);
        picker.setMinValue(0);
        picker.setValue(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getInt(TASK_GOAL_PREFERENCE, DEFAULT_TASK_GOAL));
        dialog.findViewById(R.id.dialog_change).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setTaskTarget(((NumberPicker) dialog.findViewById(R.id.number_of_tasks)).getValue());
                dialog.dismiss();
                revalidate();
            }
        });
        dialog.findViewById(R.id.dialog_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }


    /**
     * ListView event handler. Adds one one finished task to the employee that was clicked on.
     *
     * @param adapterView
     * @param view
     * @param i
     * @param l
     */
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        DbHelper dbHelper = new DbHelper(getApplicationContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL("UPDATE " + DbContract.Worker.TABLE_NAME + " SET " + DbContract.Worker.COLUMNN_FINISHED_GOALS + " = " + DbContract.Worker.COLUMNN_FINISHED_GOALS + " + 1 WHERE " + DbContract.Worker._ID + " = " + Long.toString(l));
        db.close();
        revalidate();
    }


    /**
     * ListView event handler. Removes employee that was long-clicked on.
     *
     * @param adapterView
     * @param view
     * @param i
     * @param l
     * @return
     */
    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
        DbHelper dbHelper = new DbHelper(getApplicationContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DbContract.Worker.TABLE_NAME, DbContract.Worker._ID + " = ?", new String[]{Long.toString(l)});
        db.close();
        revalidate();
        return false;
    }


    /**
     * Updates UI with saved data. Necessary to call whether something changes.
     */
    public void revalidate() {
        ListView workers = (ListView) findViewById(R.id.workerListView);
        DbHelper dbHelper = new DbHelper(getApplicationContext());
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.query(DbContract.Worker.TABLE_NAME, new String[]{DbContract.Worker._ID, DbContract.Worker.COLUMNN_NAME, DbContract.Worker.COLUMNN_FINISHED_GOALS}, null, null, null, null, DbContract.Worker.COLUMNN_NAME);
        int sumOfCompletedTasks = 0;
        c.moveToFirst();
        for (int x = 0; x < c.getCount(); x++) {
            sumOfCompletedTasks = sumOfCompletedTasks + c.getInt(c.getColumnIndex(DbContract.Worker.COLUMNN_FINISHED_GOALS));
            c.moveToNext();
        }
        c.moveToFirst();
        workers.setAdapter(new WorkersCursorAdapter(getApplicationContext(), c, PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getInt("taskGoal", 0), sumOfCompletedTasks));

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        setOverallProgress(sumOfCompletedTasks, sp.getInt(TASK_GOAL_PREFERENCE, DEFAULT_TASK_GOAL));
        setTotalTasks(sumOfCompletedTasks);
        setTaskTarget(sp.getInt(TASK_GOAL_PREFERENCE, DEFAULT_TASK_GOAL));
    }


    /**
     * Class for supplying employees ListView with data.
     */
    protected class WorkersCursorAdapter extends CursorAdapter {

        int weekGoal = 0;
        int sumOfFinishedTasks;

        public WorkersCursorAdapter(Context context, Cursor c, int weekGoal, int sumOfFinishedTasks) {
            super(context, c);
            this.weekGoal = weekGoal;
            this.sumOfFinishedTasks = sumOfFinishedTasks;

        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
            return getLayoutInflater().inflate(R.layout.listview_workers_layout, viewGroup, false);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            TextView name = (TextView) view.findViewById(R.id.workerName);
            ProgressBar bar = (ProgressBar) view.findViewById(R.id.workerProgressBar);
            TextView percentView = (TextView) view.findViewById(R.id.workerProgressPercent);
            TextView taskView = (TextView) view.findViewById(R.id.workerProgressTasks);

            int tasks = cursor.getInt(cursor.getColumnIndex(DbContract.Worker.COLUMNN_FINISHED_GOALS));
            bar.setMax(sumOfFinishedTasks);
            name.setText(cursor.getString(cursor.getColumnIndex(DbContract.Worker.COLUMNN_NAME)));
            bar.setProgress(tasks);
            percentView.setText(Math.round((float) tasks * 100f / (float) sumOfFinishedTasks) + "%");
            taskView.setText(tasks + " tasks");
        }


    }
}
