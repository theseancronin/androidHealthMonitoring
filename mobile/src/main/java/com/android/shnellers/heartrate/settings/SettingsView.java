package com.android.shnellers.heartrate.settings;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import com.android.shnellers.heartrate.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Sean on 20/02/2017.
 */

public class SettingsView extends AppCompatActivity {

    @BindView(R.id.name_layout)
    protected TextInputLayout mNameLayout;

    @BindView(R.id.name_val)
    TextInputEditText mNameValue;

    @BindView(R.id.age_val)
    protected TextInputEditText mAgeValue;

    @BindView(R.id.height_val)
    protected TextInputEditText mHeightValue;

    @BindView(R.id.weight_val)
    protected TextInputEditText mWeightValue;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_layout);

        ButterKnife.bind(this);

//       / mNameValue.addTextChangedListener(new MyTextWatcher(mNameValue));
    }

    private class MyTextWatcher implements TextWatcher {

        private View view;

        private MyTextWatcher (final View view) {
            this.view = view;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {

            switch (view.getId()) {
                case R.id.name_val:

                    break;
            }

        }
    }
}
