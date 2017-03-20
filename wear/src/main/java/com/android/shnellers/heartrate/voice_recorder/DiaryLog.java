package com.android.shnellers.heartrate.voice_recorder;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.android.shnellers.heartrate.Constants;
import com.android.shnellers.heartrate.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Sean on 05/02/2017.
 */

public class DiaryLog extends WearableActivity {

    private static final String TAG = System.getProperties().getClass().getSimpleName();

    private static final int SPEECH_REQUEST_CODE = 0;

    private View mView;

    @BindView(R.id.record_voice)
    ImageButton mRecordVoice;

    @BindView(R.id.speech)
    TextView mSpeechText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.diary_log_layout);

        ButterKnife.bind(this);
    }

    @OnClick(R.id.record_voice)
    public void startSpeechRecorder() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        startActivityForResult(intent, SPEECH_REQUEST_CODE);
    }

    /**
     * This method is invoked when the callback returns.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == SPEECH_REQUEST_CODE &&  resultCode == Activity.RESULT_OK) {
            List<String> result = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS
            );
            String spokenText = result.get(0);

            displayRecordedSpeech(spokenText);
            Log.d(TAG, "onActivityResult: " + spokenText);
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void displayRecordedSpeech(String spokenText) {
        Intent intent = new Intent(this, LogEntryReview.class);
        intent.putExtra(Constants.Const.DIARY_ENTRY, spokenText);
        intent.putExtra(Constants.Const.ENTRY_TIME, System.currentTimeMillis());
        startActivity(intent);
    }
}
