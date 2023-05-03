package com.example.webscanandroid;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText nameEditText;
    private EditText emailEditText;
    private EditText passwordEditText;
    private EditText domainEditText;
    private Button signupButton;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // init firebase auth & db
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // init xml component
        nameEditText = findViewById(R.id.name_edit_text);
        emailEditText = findViewById(R.id.email_edit_text);
        passwordEditText = findViewById(R.id.password_edit_text);
        domainEditText = findViewById(R.id.domain_edit_text);
        signupButton = findViewById(R.id.signup_button);

        // pair the button to the class
        signupButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        String username = nameEditText.getText().toString();
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        String domain = domainEditText.getText().toString();

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

        if (TextUtils.isEmpty(domain)) {
            passwordEditText.setError("Domain is required.");
            return;
        }

        // creates the user's object and insert to the db
        Map<String, Object> data = new HashMap<>();

        data.put("username", username);
        data.put("email", email);
        data.put("domain", domain);

        db.collection("users")
                .add(data)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        signupForAuth(email, password);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(SignupActivity.this, "Authentication failed on db.", Toast.LENGTH_SHORT).show();
                    }
                });

    }

    /*
    Function signs up of firebase's auth system
    Input: the email and password to register
    Output: none
     */
    private void signupForAuth(String password, String email)
    {

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful())
                        {
                            Intent intent = new Intent(SignupActivity.this, DashboardActivity.class);
                            startActivity(intent);
                            finish();
                        }
                        else
                        {
                            // Signup failed, show error message
                            Toast.makeText(SignupActivity.this, "Authentication failed on auth.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

}
