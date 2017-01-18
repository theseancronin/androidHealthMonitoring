package com.android.shnellers.heartrate;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * Created by Sean on 17/01/2017.
 */

public class ExtrasView extends Activity implements AdapterView.OnItemClickListener {

    private static final String TAG = "ExtrasView";

    private static final boolean DEBUG = true;
    public static final String REMINDERS = "Reminders";

    private ListView mListView;

    private ArrayAdapter<String> mArrayAdapter;

    private String[] extrasList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.extras_layout);

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
        }
        Log.d(TAG, "onItemClick: " + item);
    }
}
