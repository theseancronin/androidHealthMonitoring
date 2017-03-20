package com.android.shnellers.heartrate.voice_recorder;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.android.shnellers.heartrate.Constants;
import com.android.shnellers.heartrate.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItemBuffer;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Sean on 05/02/2017.
 */

public class LogEntryReview extends Activity implements GoogleApiClient.OnConnectionFailedListener,
                GoogleApiClient.ConnectionCallbacks, DataApi.DataListener {

    private static final String TAG = System.getProperties().getClass().getSimpleName();

    private static final String WEARABLE_DATA_PATH = "/wearable/data/path";

    private View mView;

    @BindView(R.id.speech)
    TextView mSpeechTxt;

    @BindView(R.id.ok_btn)
    ImageButton mOkBtn;

    @BindView(R.id.cancel_btn)
    ImageButton mCancelBtn;

    private GoogleApiClient mGoogleApiClient;

    private Node mNode;

    private String spokenText;

    private long dateTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.diary_speech_layout);

        ButterKnife.bind(this);

        Intent intent = getIntent();

        spokenText = intent.getStringExtra(Constants.Const.DIARY_ENTRY);

        dateTime = intent.getLongExtra(Constants.Const.ENTRY_TIME, 0);

        displayDiaryEntry();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addOnConnectionFailedListener(this)
                .addConnectionCallbacks(this)
                .build();


    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    private void displayDiaryEntry() {

        mSpeechTxt.setText(getSpokenText());

    }

    public String getSpokenText() {
        return spokenText;
    }

    public long getDateTime() {
        return dateTime;
    }

    @OnClick(R.id.ok_btn)
    public void saveDiaryEntry() {

        Log.d(TAG, "saveDiaryEntry: ");
        
        PutDataMapRequest dataMap = PutDataMapRequest.create(WEARABLE_DATA_PATH);

        dataMap.getDataMap().putString(Constants.Const.DIARY_ENTRY, getSpokenText());

        dataMap.getDataMap().putLong(Constants.Const.ENTRY_TIME, getDateTime());

        PutDataRequest dataRequest = dataMap.asPutDataRequest();

        PendingResult<DataApi.DataItemResult> pendingResult =
                Wearable.DataApi.putDataItem(mGoogleApiClient, dataRequest);

        new SendDiaryEntryToDataLayer(WEARABLE_DATA_PATH, dataRequest).start();

        finish();

    }

    @OnClick(R.id.cancel_btn)
    public void cancelDiaryEntry() {
        finish();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        Wearable.NodeApi.getConnectedNodes(mGoogleApiClient)
                .setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                    @Override
                    public void onResult(@NonNull NodeApi.GetConnectedNodesResult nodes) {
                        for (Node node : nodes.getNodes()) {
                            mNode = node;
                        }
                    }
                });
        Wearable.DataApi.addListener(mGoogleApiClient, this);

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {

    }

    /**
     * The type Send message to data layer.
     */
    public class SendDiaryEntryToDataLayer extends Thread {

        String path;

        PutDataRequest putDataRequest;

        /**
         * Instantiates a new Send message to data layer.
         *
         * @param path           the path
         * @param putDataRequest the put data request
         */
        public SendDiaryEntryToDataLayer(String path, PutDataRequest putDataRequest) {
            this.path = path;
            this.putDataRequest = putDataRequest;
        }

        @Override
        public void run() {
            NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi
                    .getConnectedNodes(mGoogleApiClient).await();

            // GEt the node we are sending message to
            for (Node node : nodes.getNodes()) {

                final Node n = nodes.getNodes().get(0);
                PendingResult<DataItemBuffer> dataResult =
                        Wearable.DataApi.getDataItems(mGoogleApiClient);


                dataResult.setResultCallback(new ResultCallback<DataItemBuffer>() {
                    @Override
                    public void onResult(@NonNull DataItemBuffer dataItems) {
                        if (dataItems.getStatus().isSuccess()) {

                        }

                    }
                });

            }
        }
    }
}
