package com.example.webscanandroid;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class DashboardActivity extends AppCompatActivity implements View.OnClickListener, FirebaseQueriesCallback {

    private TextView usernameTextView;
    private EditText urlEditText;
    private Button startScanButton;
    private FirebaseManager manager;
    private AnimationDrawable animationDrawable;

    private static final int PERMISSION_REQUEST_CODE = new Random().nextInt(1000);

    /**
     Function init the activity, the class variables and more
     input: saved instance state
     output: none
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // init xml component
        usernameTextView = (TextView) findViewById(R.id.username);
        urlEditText = (EditText) findViewById(R.id.url);
        startScanButton = (Button) findViewById(R.id.start_scan);
        animationDrawable = (AnimationDrawable) ((ImageView)findViewById(R.id.background)).getDrawable();

        manager = new FirebaseManager();

        manager.getEmailAndUsernameFromDB(this, "");

        // creates notification channel for the notification when the scan in done
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "scan_done",
                    "Scan Notification",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("your scan is done :)");
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        // pair the button to the class and start the background animation
        startScanButton.setOnClickListener(this);
        animationDrawable.start();

    }

    /**
     Function creates thread to start the scan when the button is pressed
     Input: the button's View
     Output: none
     */
    @Override
    public void onClick(View v) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                scanThreadFunction();
            }
        }).start();

        Toast.makeText(DashboardActivity.this, "Scan Started, You will get notification when it ends", Toast.LENGTH_SHORT).show();
    }

    /**
    Function connect the menu to the activity
    input: the classes menu
    output: boolean - always true (means that the action succeeded)
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.dashboard_menu, menu);
        return true;
    }

    /**
    Function handles menu click and act as needed
    input: the item on the menu that pressed
    output: boolean - always true (means that the action succeeded)
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);

        int id = item.getItemId();

        if (id == R.id.update_url) {
            manager.updateURL(this);
        }
        else if (id == R.id.reset_pwd) {
            Intent myIntent = new Intent(this, LoginActivity.class);
            this.startActivity(myIntent);
        }
        else if (id == R.id.logout) {
            manager.logoutCurrentUser();

            Intent myIntent = new Intent(this, MainActivity.class);
            this.startActivity(myIntent);
            finish();
        }
        else if (id == R.id.delete_user) {
            manager.deleteCurrentUser(this);
        }

        return true;
    }

    /**
    Function starts the scan on the user's url
    Input: none
    Output: none
     */
    private void scanThreadFunction() {

        String fileContent = "";
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.MINUTES) // Set the connect timeout
                .readTimeout(15, TimeUnit.MINUTES) // Set the read timeout
                .build();

        Request request = new Request.Builder()
                .url("https://2ecc-2a00-a041-2a1a-4300-7815-23b6-3232-fe34.ngrok-free.app/scan?url=" + urlEditText.getText().toString())
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                // Successful response
                fileContent = response.body().string();
                // Process the response body
            }
        } catch (IOException e) {
            // Handle the exception
            Log.d("ERROR", e.toString());
        }

        String fileName = urlEditText.getText().toString().split("/")[2].replace("www.", "") + ".txt";

        String filePath = createAndWriteToFile(fileName, fileContent);

        // Check if permission is already granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, PERMISSION_REQUEST_CODE);
        } else {
            // Permission is already granted, show notifications
            showNotification(filePath, fileName);
        }
    }

    /**
    Callback Function when request for permission ends
    Input: none
    Output: none
     */
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission has been granted
                Toast.makeText(this, "Please start the scan again", Toast.LENGTH_SHORT).show();
            } else {
                // Permission has been denied or cancelled
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
    Function gets writes the scan's result to the file
    Input: the file name and the scan's result
    Output: the file's full path
     */
    private String createAndWriteToFile(String fileName, String fileContent)
    {
        // open the file
        File externalDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File file = new File(externalDir, fileName);

        Log.d("CONTENT", fileContent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if(Environment.isExternalStorageManager())
            {
                try {
                    if (!file.exists())
                    {
                        file.createNewFile();
                    }

                    // write the scan's result to the file
                    FileOutputStream outputStream = new FileOutputStream(file);
                    outputStream.write(fileContent.getBytes());
                    outputStream.close();

                } catch (IOException e) {
                    //Toast.makeText(this, "Error: " + e.toString(), Toast.LENGTH_LONG).show();
                    Log.d("ERROR", e.toString());
                }
            }
            else
            {
                // request for external storage access
                Intent permissionIntent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivity(permissionIntent);
            }
        }

        return file.getAbsolutePath();
    }


    /**
    Function shows notification for the end of the scan
    Input: the full path of the file and its name
    Output: none
     */
    private void showNotification(String path, String fileName) {

        // create an intent to open the new file
        File file = new File(path);

        Uri uri = FileProvider.getUriForFile(getBaseContext(), getBaseContext().getApplicationContext().getPackageName() + ".provider", file);

        Intent openFileIntent = new Intent(Intent.ACTION_VIEW);
        openFileIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        openFileIntent.setDataAndType(uri, "text/plain");

        // Create a pending intent for the notification
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, openFileIntent, PendingIntent.FLAG_MUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "scan_done")
                .setSmallIcon(R.drawable.notification_icon)
                .setContentTitle("Scan Done!")
                .setContentText("Scan Results saved on Downloads directory as '" + fileName + "'")
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(Notification.DEFAULT_ALL);


        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        // create the notification
        int notificationId = (int) System.currentTimeMillis();
        notificationManager.notify(notificationId, builder.build());
    }

    /**
    Function is empty but must be declared due to interface requirements
     */
    @Override
    public void onAuthenticationResult(boolean isSuccess) {}

    /**
    Function gets the result of query and updates the username and text
    input: the task with results
    output: none
     */
    @SuppressLint("SetTextI18n")
    @Override
    public void onQueryResult(@NonNull Task<QuerySnapshot> task) {
        if (task.isSuccessful()) {
            // Iterate over the matching documents and log their data
            for (QueryDocumentSnapshot document : task.getResult()) {
                usernameTextView.setText("Hello " + String.valueOf(document.get("username")) + "!");
                urlEditText.setText(String.valueOf(document.get("url")));
            }
        } else {
            Toast.makeText(DashboardActivity.this, "Error getting documents.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
    Function gets the result of query and deletes the user from the db
    input: the task with results
    output: none
     */
    public void onDeleteResult(@NonNull Task<QuerySnapshot> task)
    {
        if (task.isSuccessful()) {
            for (QueryDocumentSnapshot document : task.getResult()) {
                document.getReference().delete()
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(DashboardActivity.this, "User successfully deleted!", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(DashboardActivity.this, "Error deleting document", Toast.LENGTH_SHORT).show();
                        });
            }
        } else {
            Toast.makeText(DashboardActivity.this, "Error getting documents: ", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     Function gets the result of query and updated the url in the database
     input: the task with results
     output: none
     */
    public void onUpdateResult(@NonNull Task<QuerySnapshot> task)
    {
        if (task.isSuccessful()) {
            for (QueryDocumentSnapshot document : task.getResult()) {
                document.getReference().update("url", urlEditText.getText().toString())
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(DashboardActivity.this, "URL successfully updated!", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(DashboardActivity.this, "Error updating URL", Toast.LENGTH_SHORT).show();
                        });
            }
        } else {
            Toast.makeText(DashboardActivity.this, "Error getting documents: ", Toast.LENGTH_SHORT).show();
        }
    }

}