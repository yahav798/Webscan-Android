package com.example.webscanandroid;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.firestore.QuerySnapshot;

public interface FirebaseQueriesCallback {
    void onAuthenticationResult(boolean isSuccess);


    void onQueryResult(@NonNull Task<QuerySnapshot> task);
    void onDeleteResult(@NonNull Task<QuerySnapshot> task);
    void onUpdateResult(@NonNull Task<QuerySnapshot> task);
    void onSearchForEmailResult(@NonNull Task<SignInMethodQueryResult> task);
}
