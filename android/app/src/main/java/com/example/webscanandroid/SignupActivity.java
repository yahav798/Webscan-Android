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
import com.google.firebase.firestore.QuerySnapshot;

public class SignupActivity extends AppCompatActivity implements View.OnClickListener, AuthenticationCallback {
    private EditText nameEditText;
    private EditText emailEditText;
    private EditText passwordEditText;
    private EditText urlEditText;
    private Button signupButton;
    private FirebaseManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        manager = new FirebaseManager();

        // init xml component
        nameEditText = findViewById(R.id.name_edit_text);
        emailEditText = findViewById(R.id.email_edit_text);
        passwordEditText = findViewById(R.id.password_edit_text);
        urlEditText = findViewById(R.id.url_edit_text);
        signupButton = findViewById(R.id.signup_button);

        // pair the button to the class
        signupButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        String username = nameEditText.getText().toString();
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        String url = urlEditText.getText().toString();

        // check if all the input are not empty
        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("Email is required.");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("Password is required.");
            return;
        }

        if (TextUtils.isEmpty(username)) {
            passwordEditText.setError("Username is required.");
            return;
        }

        if (TextUtils.isEmpty(url)) {
            passwordEditText.setError("Domain is required.");
            return;
        }

        manager.signup(username, password, email, url, this);
    }
    @Override
    public void onAuthenticationResult(boolean isSuccess)
    {
        if (isSuccess)
        {
            Intent intent = new Intent(SignupActivity.this, DashboardActivity.class);
            startActivity(intent);
            finish();
        }
        else {
            // If sign in fails, display a message to the user.
            Toast.makeText(SignupActivity.this, "Authentication failed.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onQueryResult(@NonNull Task<QuerySnapshot> task) {}
    public void onDeleteResult(@NonNull Task<QuerySnapshot> task) {}
    public void onUpdateResult(@NonNull Task<QuerySnapshot> task) {}

}
