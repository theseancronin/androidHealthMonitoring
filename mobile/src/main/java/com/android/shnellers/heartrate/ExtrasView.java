package com.android.shnellers.heartrate;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.android.shnellers.heartrate.analysis.DataOverview;
import com.android.shnellers.heartrate.diary.DiaryEntriesContainer;
import com.android.shnellers.heartrate.weight.WeightView;

import butterknife.ButterKnife;

/**
 * Created by Sean on 17/01/2017.
 */

public class ExtrasView extends Activity implements AdapterView.OnItemClickListener {

    private static final String TAG = "ExtrasView";

    private static final boolean DEBUG = true;
    public static final String REMINDERS = "Reminders";
    public static final String WEIGHT = "Weight";

    public static final String DIARY_ENTRIES = "Diary Entries";
    public static final String ANALYSIS = "Analysis";

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
        }

    }
}
