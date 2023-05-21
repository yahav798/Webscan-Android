package com.example.webscanandroid;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.QuerySnapshot;

public interface AuthenticationCallback {
    void onAuthenticationResult(boolean isSuccess);
    void onQueryResult(@NonNull Task<QuerySnapshot> task);
}
