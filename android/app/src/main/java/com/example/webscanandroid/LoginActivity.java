package com.example.webscanandroid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.firestore.QuerySnapshot;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener, FirebaseQueriesCallback {

    private EditText emailEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private FirebaseManager manager;

    /**
     Function init the activity, the class variables and more
     input: saved instance state
     output: none
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        manager = new FirebaseManager();

        // init xml component
        emailEditText = findViewById(R.id.email_edit_text);
        passwordEditText = findViewById(R.id.password_edit_text);
        loginButton = findViewById(R.id.login_button);

        // pair the button to the class
        loginButton.setOnClickListener(this);
    }

    /**
    Function logs the user using email and password when the button clicked
    Input: the button's View
    Output: none
     */
    @Override
    public void onClick(View v) {
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        // checks if the email and password are not empty
        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("Email is required.");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("Password is required.");
            return;
        }

        manager.login(email, password, this);
    }

    /**
     Function gets firebase login action's result and move to the dashboard screen if succeeded
     input: boolean - true if the action succeeded
     output: none
     */
    @Override
    public void onAuthenticationResult(boolean isSuccess)
    {
        if (isSuccess)
        {
            Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
            startActivity(intent);
            finish();
        }
        else {
            // If sign in fails, display a message to the user.
            Toast.makeText(LoginActivity.this, "Authentication failed.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     Function is empty but must be declared due to interface requirements
     */
    @Override
    public void onQueryResult(@NonNull Task<QuerySnapshot> task) {}

    /**
     Function is empty but must be declared due to interface requirements
     */
    public void onDeleteResult(@NonNull Task<QuerySnapshot> task) {}

    /**
     Function is empty but must be declared due to interface requirements
     */
    public void onUpdateResult(@NonNull Task<QuerySnapshot> task, int result) {}

    /**
     Function is empty but must be declared due to interface requirements
     */
    public void onSearchForEmailResult(@NonNull Task<SignInMethodQueryResult> task) {}

}