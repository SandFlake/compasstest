package com.mau.dalvi.malincompass;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


public class LogInActivity extends AppCompatActivity {

    private EditText userName, password;
    private Button logIn;
    private TextView errorText;
    private String userNameString, passwordString;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        db = new DatabaseHelper(this, null, null);
        userName = (EditText) findViewById(R.id.etUsername);
        password = (EditText) findViewById(R.id.etPassword);
        errorText = (TextView) findViewById(R.id.tvLogInError);
        logIn = (Button) findViewById(R.id.btnLogIn);
        logIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userNameString = userName.getText().toString();
                passwordString = password.getText().toString();
                if (!db.checkUserNameTaken(userNameString)) {
                                if (passwordString.length() >= 6 && userNameString.length() >= 4) {
                                    db.addNewUser(new User(userNameString, passwordString, 0));
                                    startCompassActivity();
                                } else if (passwordString.length() < 6) {
                                    errorText.setText("Password must be at least 6 characters long!");
                                } else if (userNameString.length() <= 4) {
                                    errorText.setText("Username must be at least 4 characters long!");
                                }
                } else {
                    if (passwordString.equals(db.getUserPassword(userNameString))) {
                        startCompassActivity();
                    } else {
                        errorText.setText("Username is taken and password doesn't match!");
                    }
                }
            }
        });
    }

    private void startCompassActivity() {
        Intent i = new Intent(this, CompassActivity.class);
        i.putExtra("Username", userNameString);
        i.putExtra("Password", passwordString);
        startActivity(i);
    }

}
