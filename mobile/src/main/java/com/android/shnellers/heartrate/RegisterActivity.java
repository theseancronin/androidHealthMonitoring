package com.android.shnellers.heartrate;

import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.shnellers.heartrate.database.UserContract;
import com.android.shnellers.heartrate.database.UserDatabase;
import com.android.shnellers.heartrate.database.UserDatabaseHelper;


public class RegisterActivity extends AppCompatActivity {

    private EditText _name, _email, _confirmEmail,
            _password, _confirmPassword;
    private Button _registerButton;

    private UserDatabaseHelper mUserDatabaseHelper;
    // Gets the repository in write mode
    //private SQLiteDatabase db = mUserDatabaseHelper.getWritableDatabase();

    private UserDatabase mUserDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        SetVariables();

        // Get instance of the user database adapter.
        mUserDatabase = new UserDatabase(this);

    }

    /**
     * This method retrieves the users inputs and first authenticates
     * the details entered, before adding to the DB.
     */
    public void registerUser(View v) {

        String name = _name.getText().toString(),
                email = _email.getText().toString(),
                confirmEmail = _confirmEmail.getText().toString(),
                password = _password.getText().toString(),
                confirmPassword = _confirmPassword.getText().toString();

        if (validateDetails(name, email, confirmEmail, password, confirmPassword) == true) {
            Log.i("Validate OK", "All ok");
            // Create a new map of values, where column names are keys
            ContentValues values = new ContentValues();
            values.put(UserContract.UserEntry.COLUMN_NAME, name);
            values.put(UserContract.UserEntry.COLUMN_EMAIL, email);
            values.put(UserContract.UserEntry.COLUMN_PASSWORD, password);

            // Insert the new row, returning the primary key value.
            // The second argument null tells the framework what to do if the
            // content values are null.
            mUserDatabase.insert(name, email, password);
            Toast.makeText(getApplicationContext(), "Account created", Toast.LENGTH_LONG).show();

            loginActivity();



        } else {
            Log.i("Validate error", "Error");
        }

        //mUserDatabase.close();

//        mUserDatabase.printDB();


    }

    /**
     * Validates the information the user input.
     *
     * @param name
     * @param email
     * @param confirmEmail
     * @param password
     * @param confirmPassword
     * @return
     */
    private boolean validateDetails(final String name, final String email,
                                    final String confirmEmail, final String password,
                                    final String confirmPassword) {

        boolean validated = true;

        if (name.isEmpty() || email.isEmpty() || confirmEmail.isEmpty() ||
                password.isEmpty() || confirmPassword.isEmpty()) {
            Log.i("Empty error", "Empty element");
            validated = false;
        } else if (!email.equals(confirmEmail)) {
            Log.i("Email error", "Emails Don't match");
            validated = false;
        } else if (!password.equals(confirmPassword)) {
            Log.i("Password error", "passwords don't match");
            validated = false;
        } else if (name.length() < 3) {
            Log.i("Name error", "Name to short");
            validated = false;
        }

        return validated;
    }

    /**
     * Initialize the variables for the registration.
     */
    private void SetVariables() {
        _name = (EditText) findViewById(R.id.nameRegister);
        _email = (EditText) findViewById(R.id.emailRegister);
        _confirmEmail = (EditText) findViewById(R.id.emailRegister);
        _password = (EditText) findViewById(R.id.passwordRegister);
        _confirmPassword = (EditText) findViewById(R.id.confirmPassword);
        _registerButton = (Button) findViewById(R.id.registerBtn);
    }

    /**
     * Starts the login activity.
     *
     * @param v
     */
    public void loginToAccount (View v) {
        loginActivity();

    }

    private void loginActivity() {
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(intent);
    }
}
