package com.android.shnellers.heartrate.heart_rate;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.android.shnellers.heartrate.R;
import com.android.shnellers.heartrate.database.HeartRateDatabase;
import com.android.shnellers.heartrate.models.HeartRateObject;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Sean on 13/03/2017.
 */

public class History extends AppCompatActivity {

    @BindView(R.id.recycler_view)
    protected RecyclerView mRecyclerView;

    private ArrayList<HeartRateObject> mHeartRateObjects;

    private HeartRateDatabase mHeartRateDatabase;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.heart_rate_history);

        ButterKnife.bind(this);

        mHeartRateObjects = new ArrayList<>();

        mHeartRateDatabase = new HeartRateDatabase(this);

        setRecyclerView();
    }

    private void setRecyclerView() {


        mHeartRateObjects = mHeartRateDatabase.get7DayReadings();

        if (!mHeartRateObjects.isEmpty()) {
            mRecyclerView.setHasFixedSize(true);

            HistoryRecyclerAdapter adapter = new HistoryRecyclerAdapter(mHeartRateObjects);
            mRecyclerView.setAdapter(adapter);

            RecyclerView.LayoutManager manager = new LinearLayoutManager(this);
            mRecyclerView.setLayoutManager(manager);
        }

    }
}
