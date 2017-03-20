package com.android.shnellers.heartrate.threads;

/**
 * Created by Sean on 18/03/2017.
 */

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataItemBuffer;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;

import static com.google.android.gms.wearable.DataMap.TAG;

/**
 * The type Send message to data layer.
 */
public class SendMessageToDataLayer extends Thread {

    // the path
    private String path;
    // put data request
    private PutDataRequest putDataRequest;

    private GoogleApiClient mGoogleApiClient;

    private ArrayList<Node> mNodes;

    /**
     * Instantiates a new Send message to data layer.
     *
     * @param path           the path
     * @param putDataRequest the put data request
     */
    public SendMessageToDataLayer(String path, PutDataRequest putDataRequest,
                                  GoogleApiClient googleApiClient) {
        this.path = path;
        this.putDataRequest = putDataRequest;
        mGoogleApiClient = googleApiClient;
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
                    Log.d(TAG, "onResult: Item send: " + dataItems.getStatus().isSuccess());
                    Log.v(TAG, "Data Sent to: " + n.getDisplayName());
                    Log.v(TAG, "Data Node ID: " + n.getId());
                    //Log.v(TAG, "Data Nodes Size: " + nodes.getNodes().size());
                }
            });

        }
    }
}
