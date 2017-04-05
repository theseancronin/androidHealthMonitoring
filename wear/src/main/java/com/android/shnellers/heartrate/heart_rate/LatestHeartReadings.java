package com.android.shnellers.heartrate.heart_rate;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.WearableRecyclerView;
import android.util.Log;
import android.view.View;

import com.android.shnellers.heartrate.R;
import com.android.shnellers.heartrate.activities.RecyclerAdapter;
import com.android.shnellers.heartrate.models.LatestObject;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Sean on 24/01/2017.
 */

public class LatestHeartReadings extends WearableActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        DataApi.DataListener {

    private static final String TAG = "LatestHeartReadings";
    private static final String LATEST_STATS = "/wear/latest/heart/data";

    private View mView;

    private GoogleApiClient mGoogleApiClient;

    @BindView(R.id.recycler_launcher_view)
    protected WearableRecyclerView mRecyclerView;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.latest_heart_readings);

        ButterKnife.bind(this);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();

        sendMessage();

        displayList();
    }

    private void displayList() {
        mRecyclerView.setCenterEdgeItems(true);
        mRecyclerView.setHasFixedSize(true);

        ArrayList<LatestObject> list = new ArrayList<>();
        list.add(new LatestObject(2345, "Resting Rates"));
        list.add(new LatestObject(78, "Avg BPM"));
        list.add(new LatestObject(98, "Max BPM"));

        RecyclerAdapter adapter = new RecyclerAdapter(list, this);
        mRecyclerView.setAdapter(adapter);

        RecyclerView.LayoutManager manager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(manager);
    }


    /**
     *
     */
    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    /**
     *
     */
    @Override
    public void onPause() {
        super.onPause();
        mGoogleApiClient.disconnect();
    }
    /**
     *
     */
    private void sendMessage() {
        Log.d(TAG, "sendMessage: ");

        new SendMessageToDataLayer(LATEST_STATS, "latest_stats").start();
    }


    @Override
    public void onConnected(@Nullable final Bundle bundle) {
        Wearable.DataApi.addListener(mGoogleApiClient, this);
    }

    @Override
    public void onConnectionSuspended(final int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull final ConnectionResult connectionResult) {

    }

    @Override
    public void onDataChanged(final DataEventBuffer dataEventBuffer) {

    }

    /**
     * The type Send message to data layer.
     */
    public class SendMessageToDataLayer extends Thread {
        /**
         * The Path.
         */
        String path;
        String text;
        PutDataRequest putDataRequest;

        /**
         * Instantiates a new Send message to data layer.
         *
         * @param path           the path
         */
        public SendMessageToDataLayer(String path, String text) {
            this.path = path;
            this.text = text;
        }

        @Override
        public void run() {
            NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi
                    .getConnectedNodes(mGoogleApiClient).await();

            // GEt the node we are sending message to
            for (Node node : nodes.getNodes()) {

                final Node n = nodes.getNodes().get(0);
                MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(
                        mGoogleApiClient, node.getId(), path, text.getBytes() ).await();

            }

        }
    }
}
