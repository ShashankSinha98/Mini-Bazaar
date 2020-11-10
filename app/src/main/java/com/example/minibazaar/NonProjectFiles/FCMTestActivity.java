package com.example.minibazaar.NonProjectFiles;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.minibazaar.R;
import com.google.firebase.messaging.FirebaseMessagingService;

public class FCMTestActivity extends AppCompatActivity {

    FirebaseMessagingService firebaseMessagingService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_f_c_m_test);
    }
}