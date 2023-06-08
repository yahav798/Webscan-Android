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
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class SignupActivity extends AppCompatActivity implements View.OnClickListener, FirebaseQueriesCallback {
    private EditText[] userInput;
    private String[] details;
    private Button signupButton;
    private FirebaseManager manager;
    static final int USERNAME_INDEX = 0;
    static final int PASSWORD_INDEX = 1;
    static final int EMAIL_INDEX = 2;
    static final int URL_INDEX = 3;

    /**
     Function init the activity, the class variables and more
     input: saved instance state
     output: none
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        manager = new FirebaseManager();

        // init xml component
        userInput = new EditText[4];
        details = new String[4];

        userInput[USERNAME_INDEX] = findViewById(R.id.name_edit_text);
        userInput[PASSWORD_INDEX] = findViewById(R.id.password_edit_text);
        userInput[EMAIL_INDEX] = findViewById(R.id.email_edit_text);
        userInput[URL_INDEX] = findViewById(R.id.url_edit_text);
        signupButton = findViewById(R.id.signup_button);

        // pair the button to the class
        signupButton.setOnClickListener(this);
    }

    /**
     Function register the user using email, password, username and url when the button clicked
     Input: the button's View
     Output: none
     */
    @Override
    public void onClick(View v) {

        for (int i = 0; i < 4; i++)
        {
            details[i] = userInput[i].getText().toString().trim();

            if (TextUtils.isEmpty(details[i])) {
                userInput[i].setError("This field is required.");
                return;
            }
        }

        if (details[PASSWORD_INDEX].length() < 6)
        {
            userInput[PASSWORD_INDEX].setError("Password length must be at least 6 chars.");
            return;
        }

        manager.checkForEmail(details[EMAIL_INDEX], this);
    }

    /**
     Function gets firebase register action's result and move to the dashboard screen if succeeded
     input: boolean - true if the action succeeded
     output: none
     */
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
            Toast.makeText(SignupActivity.this, "Signup failed.",
                    Toast.LENGTH_SHORT).show();

            // manager.deleteCurrentUser(this);
        }
    }

    /**
     Function gets firebase search for another email action's result and alerts if found (email must be unique)
     input: the task (with results)
     output: none
     */
    @Override
    public void onQueryResult(@NonNull Task<QuerySnapshot> task) {}

    /**
     Function is empty but must be declared due to interface requirements
     */
    public void onSearchForEmailResult(@NonNull Task<SignInMethodQueryResult> task) {
        if (task.isSuccessful()) {
            SignInMethodQueryResult result = task.getResult();
            if (result != null && result.getSignInMethods() != null && !result.getSignInMethods().isEmpty()) {
                // The email already exists in the Firebase Authentication system
                Toast.makeText(this, "Email already exists", Toast.LENGTH_SHORT).show();
            }
            else {
                manager.signup(details[USERNAME_INDEX], details[PASSWORD_INDEX], details[EMAIL_INDEX], details[URL_INDEX], this);
            }
        } else {
            Toast.makeText(this, "Error getting documents: ", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     Function is empty but must be declared due to interface requirements
     */
    public void onDeleteResult(@NonNull Task<QuerySnapshot> task) {}

    /**
     Function is empty but must be declared due to interface requirements
     */
    public void onUpdateResult(@NonNull Task<QuerySnapshot> task, int result) {}

}
