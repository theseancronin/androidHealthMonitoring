package com.android.shnellers.heartrate;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.shnellers.heartrate.activities.RegisterActivity;
import com.android.shnellers.heartrate.database.UserDatabase;


public class LoginActivity extends AppCompatActivity {

    private EditText _email, _password;
    private UserDatabase mUserDatabase;
    private Cursor _loggedUser;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        session = new SessionManager(getApplicationContext());

        _email = (EditText) findViewById(R.id.emailLogin);
        _password = (EditText) findViewById(R.id.passwordLogin);
        mUserDatabase = new UserDatabase(getApplicationContext());
        _loggedUser = null;

    }

    /**
     * Logs into the account associated with the details entered.
     *
     * @param v
     */
    public void loginToAccount(View v) {

        String email = _email.getText().toString();
        String password = _password.getText().toString();

        if (validUserDetails(email, password) == true) {
            Toast.makeText(getBaseContext(), "You logged in", Toast.LENGTH_LONG).show();
        }

    }

    /**
     * Validates the details entered by the user.
     *
     * @param email
     * @param password
     * @return
     */
    private boolean validUserDetails(String email, String password) {
        boolean valid = true;

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(getBaseContext(), "Email and Password required.", Toast.LENGTH_LONG).show();
        } else {
            mUserDatabase.open();
            _loggedUser = mUserDatabase.getUser(email, password);

            if (_loggedUser != null) {

                session.setLogin(true);
                setLoggedInUser(_loggedUser);

                // Launch the main activity
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();


            } else {
                valid = false;
            }
        }

        return valid;
    }

    /**
     * If login details validate, set the logged user.
     *
     * @param user
     */
    private void setLoggedInUser(final Cursor user) {

        String name = user.getString(user.getColumnIndex("name")),
               email  = user.getString(user.getColumnIndex("email"));
        User loggedUser = new User(name, email);
        session.storeUserData(loggedUser);
        mUserDatabase.setLoggedInUser(loggedUser);
        mUserDatabase.setUserIsLoggedIn(true);

        Log.i("user", user.getString(user.getColumnIndex("id")));

    }

    /**
     * Starts the register activity.
     *
     * @param v
     */
    public void registerAccount (View v) {
        Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
        startActivity(intent);
    }
}
