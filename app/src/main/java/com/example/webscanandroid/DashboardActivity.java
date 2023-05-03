package com.example.webscanandroid;

import static android.app.PendingIntent.FLAG_MUTABLE;
import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.checkerframework.checker.index.qual.LengthOf;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Random;

public class DashboardActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView usernameTextView;
    private TextView urlTextView;
    private Button startScanButton;
    private FirebaseUser user;
    private FirebaseFirestore db;
    private ImageView imageView;
    private String url;
    private AnimationDrawable animationDrawable;

    private static final int PERMISSION_REQUEST_CODE = new Random().nextInt(1000);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // init Python
        Python.start(new AndroidPlatform(this));

        // init xml component
        usernameTextView = (TextView) findViewById(R.id.username);
        urlTextView = (TextView) findViewById(R.id.url);
        startScanButton = (Button) findViewById(R.id.start_scan);
        imageView = findViewById(R.id.background);
        animationDrawable = (AnimationDrawable) imageView.getDrawable();

        // init db
        user = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();

        getEmailAndUsernameFromDB();

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
    Function starts the scan on the user's url
    Input: none
    Output: none
     */
    private void scanThreadFunction() {

        Python py = Python.getInstance();

        PyObject pyobj = py.getModule("Main").callAttr("main", url);

        String fileContent = pyobj.toString().replace("\\n", "\n");

        String fileName = url.split("/")[2].replace("www.", "") + ".txt";

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
    Function gets the logged user's username and url and updates the TextViews with the values
    Input: none
    Output: none
     */
    private void getEmailAndUsernameFromDB()
    {

        Query query = db.collection("users").whereEqualTo("email", user.getEmail());

        // Execute the query and retrieve the matching documents
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    // Iterate over the matching documents and log their data
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        url = String.valueOf(document.get("url"));
                        String usernameText = "Username: \n" + document.get("username"), urlText = "URL: \n" + url;

                        usernameTextView.setText(usernameText);
                        urlTextView.setText(urlText);
                    }
                } else {
                    Toast.makeText(DashboardActivity.this, "Error getting documents.", Toast.LENGTH_SHORT).show();
                }
            }
        });
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
}