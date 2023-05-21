package com.example.webscanandroid;

import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.NonNull;
import android.os.CountDownTimer;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

import kotlin.jvm.Synchronized;

public class FirebaseManager {

    private FirebaseUser user;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    public FirebaseManager()
    {


        user = FirebaseAuth.getInstance().getCurrentUser();
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    public FirebaseUser getUser()
    {
        return user;
    }

    @Synchronized
    public void login(String email, String password, AuthenticationCallback callback) {

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
                        callback.onAuthenticationResult(task.isSuccessful());
                    }
                });
    }

    public boolean signup(String username, String password, String email, String url, AuthenticationCallback callback)
    {
        final boolean[] result = new boolean[1];

        // creates the user's object and insert to the db
        Map<String, Object> data = new HashMap<>();

        data.put("username", username);
        data.put("email", email);
        data.put("url", url);

        db.collection("users")
                .add(data)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        signupForAuth(email, password, callback);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Toast.makeText(SignupActivity.this, "Authentication failed on db.", Toast.LENGTH_SHORT).show();
                        result[0] = false;
                    }
                });

        return result[0];
    }
    private void signupForAuth(String password, String email, AuthenticationCallback callback)
    {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        callback.onAuthenticationResult(task.isSuccessful());
                    }
                });
    }

    /**
     Function gets the logged user's username and url and updates the TextViews with the values
     Input: none
     Output: none
     */
    public void getEmailAndUsernameFromDB(AuthenticationCallback callback)
    {
        Query query = db.collection("users").whereEqualTo("email", user.getEmail());

        // Execute the query and retrieve the matching documents
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                callback.onQueryResult(task);
            }
        });
    }

}

