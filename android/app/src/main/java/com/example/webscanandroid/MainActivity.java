package com.example.webscanandroid;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Button loginButton;
    private Button signupButton;
    private ImageView imageView;
    private AnimationDrawable animationDrawable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // init xml components
        loginButton = (Button)findViewById(R.id.login);
        signupButton = (Button)findViewById(R.id.signup);
        imageView = findViewById(R.id.my_image_view);
        animationDrawable = (AnimationDrawable) imageView.getDrawable();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        Log.d("USER", user.toString());

        // pair the buttons to the class
        loginButton.setOnClickListener(this);
        signupButton.setOnClickListener(this);

        // start the background animation
        animationDrawable.start();
    }

    /*
    Function starts new activity when the button pressed
    Input: none
    Output: none
     */
    @Override
    public void onClick(View view) {
        if (view == loginButton)
        {
            Intent myIntent = new Intent(this, LoginActivity.class);
            this.startActivity(myIntent);
        }
        else
        {
            Intent myIntent = new Intent(this, SignupActivity.class);
            this.startActivity(myIntent);
        }
    }
}