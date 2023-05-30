package com.example.webscanandroid;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Button loginButton;
    private Button signupButton;
    private AnimationDrawable backgroundAnimation;

    /**
     Function init the activity, the class variables and more
     input: saved instance state
     output: none
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // init xml components
        loginButton = (Button)findViewById(R.id.login);
        signupButton = (Button)findViewById(R.id.signup);
        backgroundAnimation = (AnimationDrawable) ((ImageView)findViewById(R.id.my_image_view)).getDrawable();

        // pair the buttons to the class
        loginButton.setOnClickListener(this);
        signupButton.setOnClickListener(this);

        // start the background animation
        backgroundAnimation.start();
    }

    /**
    Function starts new activity when the button pressed
    Input: none
    Output: none
     */
    @Override
    public void onClick(View view) {
        Intent nextScreen;
        if (view == loginButton)
        {
            nextScreen = new Intent(this, LoginActivity.class);
        }
        else
        {
            nextScreen = new Intent(this, SignupActivity.class);
        }
        this.startActivity(nextScreen);
    }
}