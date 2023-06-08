package com.example.webscanandroid;

import android.util.Log;

import androidx.annotation.LongDef;
import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.Filter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

import kotlin.jvm.Synchronized;

public class FirebaseManager {

    private FirebaseUser user;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    /**
     Function C'tor that init the class variables
     Input: none
     Output: none
     */
    public FirebaseManager()
    {
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
    }

    /**
     Function logs in to the user with the given credentials and calls the callback with the result
     Input: email and password to login and the callback class
     Output: none
     */
    public void login(String email, String password, FirebaseQueriesCallback callback) {

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
                        callback.onAuthenticationResult(task.isSuccessful());
                    }
                });
    }

    /**
     Function insert the new user to database and calls the function of the authentication if succeeded
     Input: map of the data to register and the callback class
     Output: none
     */
    public void signup(String username, String password, String email, String url, FirebaseQueriesCallback callback)
    {
        // creates the user's object and insert to the db
        Map<String, Object> data = new HashMap<>();

        data.put("username", username);
        data.put("email", email);
        data.put("url", url);
        data.put("best_scan", 0);

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
                        callback.onAuthenticationResult(false);
                        Log.d("ERROR!", e.toString());
                    }
                });
    }

    /**
     Function insert the new user to authentication system and calls the callback with the result
     Input: email and password to register and the callback class
     Output: none
     */
    private void signupForAuth(String email, String password, FirebaseQueriesCallback callback)
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
     Function gets the given user's username and url and calls the callback with the result
     Input: the callback class and the email to get the info (empty means current logged in user)
     Output: none
     */
    public void getEmailAndUsernameFromDB(FirebaseQueriesCallback callback, String email)
    {
        if (email.isEmpty())
        {
            email = user.getEmail();
        }

        Log.d("ERROR!", email);

        Query query = db.collection("users").whereEqualTo("email", email);

        // Execute the query and retrieve the matching documents
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                callback.onQueryResult(task);
            }
        });
    }

    /**
     Function gets the logged user's url and calls the callback with the result in order to change its value
     Input: the callback class
     Output: none
     */
    public void updateURL(FirebaseQueriesCallback callback) {

        Query query = db.collection("users").whereEqualTo("email", user.getEmail());
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                callback.onUpdateResult(task);
            }
        });
    }

    /**
     Function logs out of the system
     Input: none
     Output: none
     */
    public void logoutCurrentUser()
    {
        auth.signOut();
    }

    /**
     Function gets the logged user and calls the callback with the result in order to delete him
     Input: the callback class
     Output: none
     */
    public void deleteCurrentUser(FirebaseQueriesCallback callback)
    {
        String email = user.getEmail();

        user.delete();

        CollectionReference collectionRef = db.collection("users");

        Query query = collectionRef.whereEqualTo("email", email);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                callback.onDeleteResult(task);
            }
        });
    }

    public void checkForEmail(String email, FirebaseQueriesCallback callback)
    {
        auth.fetchSignInMethodsForEmail(email)
                .addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                    @Override
                    public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                        callback.onSearchForEmailResult(task);
                    }
                });
    }

    public void resetPassword( FirebaseQueriesCallback callback) {
        auth.sendPasswordResetEmail(user.getEmail())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        callback.onAuthenticationResult(task.isSuccessful());
                    }
                });
    }


}

