package com.example.minibazaar.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.minibazaar.NetworkSync.CheckInternetConnection;
import com.example.minibazaar.R;
import com.example.minibazaar.UserSession.UserSession;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kaopiz.kprogresshud.KProgressHUD;

import es.dmoral.toasty.Toasty;

public class LoginActivity extends AppCompatActivity {

    private EditText edtemail,edtpass;
    private String email,pass;
    private TextView appname,forgotpass,registernow;
    private UserSession session;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseUser user;
    private String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Log.e("Login CheckPoint","LoginActivity started");
        //check Internet Connection
        new CheckInternetConnection(this).checkConnection();

        FirebaseApp.initializeApp(LoginActivity.this);
        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        Typeface typeface = ResourcesCompat.getFont(this, R.font.blacklist);
        appname = findViewById(R.id.appname);
        appname.setTypeface(typeface);

        edtemail= findViewById(R.id.email);
        edtpass= findViewById(R.id.password);

        session= new UserSession(getApplicationContext());



        //if user wants to register
        registernow= findViewById(R.id.register_now);
        registernow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this,RegisterActivity.class));
                finish();
            }
        });

        //if user forgets password
        forgotpass=findViewById(R.id.forgot_pass);
        forgotpass.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this,ForgotPasswordActivity.class));
            }
        });

        //Validating login details
        Button login_button=findViewById(R.id.login_button);

        login_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                email=edtemail.getText().toString();
                pass=edtpass.getText().toString();

                if(validateUsername(email) && validatePassword(pass)){

                    final KProgressHUD progressDialog=  KProgressHUD.create(LoginActivity.this)
                            .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                            .setLabel("Please wait")
                            .setCancellable(false)
                            .setAnimationSpeed(2)
                            .setDimAmount(0.5f)
                            .show();

                    mAuth.signInWithEmailAndPassword(email,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if(task.isSuccessful()){

                                boolean isEmailVerified = checkIfEmailVerified();

                                if(!isEmailVerified){
                                    progressDialog.dismiss();
                                    Toasty.error(LoginActivity.this,"Email Not Verified.",2000).show();
                                } else {
                                    userID = mAuth.getCurrentUser().getUid();
                                    firebaseFirestore.collection("Users").document(userID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                            if (task.isSuccessful()) {

                                                if (task.getResult().exists()) {

                                                    String sessionname = task.getResult().getString("name");
                                                    String sessionmobile = task.getResult().getString("number");
                                                    String sessionphoto = task.getResult().getString("image");
                                                    String sessionemail = task.getResult().getString("email");

                                                    session.createLoginSession(sessionname,sessionemail,sessionmobile,sessionphoto);

                                                    //count value of firebase cart and wishlist
                                                    //countFirebaseValues();

                                                    progressDialog.dismiss();

                                                    Intent loginSuccess = new Intent(LoginActivity.this, MainActivity.class);
                                                    startActivity(loginSuccess);
                                                    finish();


                                                }
                                            } else {
                                                progressDialog.dismiss();
                                                Toast.makeText(LoginActivity.this, "Login Error. Please try again.", Toast.LENGTH_SHORT).show();
                                            }

                                        }
                                    });
                                }

                            } else {
                                progressDialog.dismiss();
                                Toasty.error(LoginActivity.this,"Couldn't Log In. Please check your Email/Password",2000).show();
                            }
                        }
                    });

                }

            }
        });
    }

    private boolean checkIfEmailVerified() {

        user = FirebaseAuth.getInstance().getCurrentUser();

        if(user.isEmailVerified())
            return true;
        else
            return false;
    }

    /* To be implemented on Firebase Side

    private void countFirebaseValues() {

        mDatabaseReference.child("cart").child(sessionmobile).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.e(dataSnapshot.getKey(),dataSnapshot.getChildrenCount() + "");
                session.setCartValue((int)dataSnapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mDatabaseReference.child("wishlist").child(sessionmobile).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.e(dataSnapshot.getKey(),dataSnapshot.getChildrenCount() + "");
                session.setWishlistValue((int)dataSnapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    } */

    @Override
    protected void onResume() {
        super.onResume();
        Log.e("Login CheckPoint","LoginActivity resumed");
        //check Internet Connection
        new CheckInternetConnection(this).checkConnection();
    }

    @Override
    protected void onStop () {
        super.onStop();
        Log.e("Login CheckPoint","LoginActivity stopped");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private boolean validatePassword(String pass) {


        if (pass.length() < 4 || pass.length() > 20) {
            edtpass.setError("Password Must consist of 4 to 20 characters");
            return false;
        }
        return true;
    }

    private boolean validateUsername(String email) {

        if (email.length() < 4 || email.length() > 30) {
            edtemail.setError("Email Must consist of 4 to 30 characters");
            return false;
        } else if (!email.matches("^[A-za-z0-9.@]+")) {
            edtemail.setError("Only . and @ characters allowed");
            return false;
        } else if (!email.contains("@") || !email.contains(".")) {
            edtemail.setError("Email must contain @ and .");
            return false;
        }
        return true;
    }
}
