package com.android.shnellers.heartrate;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.android.shnellers.heartrate.activities.ActivityMain;
import com.android.shnellers.heartrate.analysis.DataOverview;
import com.android.shnellers.heartrate.diary.DiaryEntriesContainer;
import com.android.shnellers.heartrate.heart_rate.History;
import com.android.shnellers.heartrate.passport.PassportMain;
import com.android.shnellers.heartrate.weight.WeightView;

import butterknife.ButterKnife;

/**
 * Created by Sean on 17/01/2017.
 */

public class ExtrasView extends Activity implements AdapterView.OnItemClickListener {

    public static final String HISTORY = "History";
    private static final String TAG = "ExtrasView";

    private static final boolean DEBUG = true;
    private static final String REMINDERS = "Reminders";
    private static final String WEIGHT = "Weight";

    private static final String DIARY_ENTRIES = "Diary Entries";
    private static final String ANALYSIS = "Analysis";
    public static final String ACTIVITY = "Activity";
    public static final String PASSPORT = "Passport";

    private ListView mListView;

    private ArrayAdapter<String> mArrayAdapter;

    private String[] extrasList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.extras_layout);

        ButterKnife.bind(this);

        extrasList = getResources().getStringArray(R.array.extrasListMenu);

        // Set the adapter
        mArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, extrasList);

        mListView = (ListView) findViewById(R.id.dialog_list_view);
        mListView.setAdapter(mArrayAdapter);
        mListView.setOnItemClickListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }



    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        String item = adapterView.getItemAtPosition(position).toString();
        if (item.equals(REMINDERS)) {
            Intent intent = new Intent(this, Reminders.class);
            startActivity(intent);

        } else if (item.equals(WEIGHT)) {
            Intent intent = new Intent(this, WeightView.class);
            startActivity(intent);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

        } else if (item.equals(DIARY_ENTRIES)) {
            Intent intent = new Intent(this, DiaryEntriesContainer.class);
            startActivity(intent);
        } else if (item.equals(ANALYSIS)){
            Intent intent = new Intent(this, DataOverview.class);
            startActivity(intent);
        } else if (item.equals(HISTORY)) {
            Intent intent = new Intent(this, History.class);
            startActivity(intent);
        } else if (item.equals(ACTIVITY)) {
            Intent intent = new Intent(this, ActivityMain.class);
            startActivity(intent);
        } else if (item.equals(PASSPORT)){
            Intent intent = new Intent(this, PassportMain.class);
            startActivity(intent);
        }

    }
}
