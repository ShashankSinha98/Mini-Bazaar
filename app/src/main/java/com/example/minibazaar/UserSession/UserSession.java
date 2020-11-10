package com.example.minibazaar.UserSession;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;

import com.airbnb.lottie.LottieAnimationView;
import com.example.minibazaar.Activities.LoginActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;

public class UserSession {

    private final String TAG = this.getClass().getSimpleName();
    // Shared Preferences
    SharedPreferences pref;

    // Editor for Shared preferences
    SharedPreferences.Editor editor;

    // Context
    Context context;

    // Shared pref mode
    int PRIVATE_MODE = 0;

    // Sharedpref file name
    private static final String PREF_NAME = "UserSessionPref";

    // First time login Check
    public static final String FIRST_TIME = "firsttime";

    // All Shared Preferences Keys
    private static final String IS_LOGIN = "IsLoggedIn";

    // User name (make variable public to access from outside)
    public static final String KEY_NAME = "name";

    // Email address (make variable public to access from outside)
    public static final String KEY_EMAIL = "email";

    // Mobile number (make variable public to access from outside)
    public static final String KEY_MOBiLE = "mobile";

    // user avatar (make variable public to access from outside)
    public static final String KEY_PHOTO = "photo";

    // number of items in our cart
    public static final String KEY_CART = "cartvalue";

    // number of items in our wishlist
    public static final String KEY_WISHLIST = "wishlistvalue";

    // check first time app launch
    public static final String IS_FIRST_TIME_LAUNCH = "IsFirstTimeLaunch";

    private FirebaseFirestore firebaseFirestore;


    // Constructor
    public UserSession(Context context){
        this.context = context;
        this.firebaseFirestore = FirebaseFirestore.getInstance();
        pref = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    /**
     * Create login session
     * */
    public void createLoginSession(String name, String email, String mobile, String photo){
        // Storing login value as TRUE
        editor.putBoolean(IS_LOGIN, true);

        // Storing name in pref
        editor.putString(KEY_NAME, name);

        // Storing email in pref
        editor.putString(KEY_EMAIL, email);

        // Storing phone number in pref
        editor.putString(KEY_MOBiLE, mobile);

        // Storing image url in pref
        editor.putString(KEY_PHOTO, photo);

        // commit changes
        editor.commit();
    }

    /**
     * Check login method wil check user login status
     * If false it will redirect user to login page
     * Else won't do anything
     * */
    public void checkLogin(){
        // Check login status
        if(!this.isLoggedIn()){
            // user is not logged in redirect him to Login Activity
            Intent i = new Intent(context, LoginActivity.class);
            // Closing all the Activities
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            // Add new Flag to start new Activity
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            // Staring Login Activity
            context.startActivity(i);
        }

    }

    /**
     * Get stored session data
     * */
    public HashMap<String, String> getUserDetails(){
        HashMap<String, String> user = new HashMap<>();
        // user name
        user.put(KEY_NAME, pref.getString(KEY_NAME, null));

        // user email id
        user.put(KEY_EMAIL, pref.getString(KEY_EMAIL, null));

        // user phone number
        user.put(KEY_MOBiLE, pref.getString(KEY_MOBiLE, null));

        // user avatar
        user.put(KEY_PHOTO, pref.getString(KEY_PHOTO, null)) ;

        // return user
        return user;
    }

    /**
     * Clear session details
     * */
    public void logoutUser(){
        // Clearing all data from Shared Preferences
        editor.putBoolean(IS_LOGIN,false);
        editor.commit();

        // After logout redirect user to Login Activity
        Intent i = new Intent(context, LoginActivity.class);
        // Closing all the Activities
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // Add new Flag to start new Activity
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        // Staring Login Activity
        context.startActivity(i);
    }

    /**
     * Quick check for login
     * **/
    // Get Login State
    public boolean isLoggedIn(){
        return pref.getBoolean(IS_LOGIN, false);
    }

    public int getCartValue(){
        return pref.getInt(KEY_CART,0);
    }

    public int getWishlistValue(){
        return pref.getInt(KEY_WISHLIST,0);
    }

    public Boolean  getFirstTime() {
        return pref.getBoolean(FIRST_TIME, true);
    }

    public void setFirstTime(Boolean n){
        editor.putBoolean(FIRST_TIME,n);
        editor.commit();
    }

    public void increaseCartValue(){
        int val = getCartValue()+1;
        editor.putInt(KEY_CART,val);
        editor.commit();
        Log.e("Cart Value PE", "Var value : "+val+"Cart Value :"+getCartValue()+" ");
    }

    public void increaseWishlistValue(){
        int val = getWishlistValue()+1;
        editor.putInt(KEY_WISHLIST,val);
        editor.commit();
        Log.e("Cart Value PE", "Var value : "+val+"Cart Value :"+getCartValue()+" ");
    }

    public void decreaseCartValue(){
        int val = getCartValue()-1;
        editor.putInt(KEY_CART,val);
        editor.commit();
        Log.e("Cart Value PE", "Var value : "+val+"Cart Value :"+getCartValue()+" ");
    }

    public void decreaseWishlistValue(){
        int val = getWishlistValue()-1;
        editor.putInt(KEY_WISHLIST,val);
        editor.commit();
        Log.e("Cart Value PE", "Var value : "+val+"Cart Value :"+getCartValue()+" ");
    }

    public void setCartValue(int count){
        editor.putInt(KEY_CART,count);
        editor.commit();
    }

    public void setWishlistValue(int count){
        editor.putInt(KEY_WISHLIST,count);
        editor.commit();
    }

    public void setFirstTimeLaunch(boolean isFirstTime) {
        editor.putBoolean(IS_FIRST_TIME_LAUNCH, isFirstTime);
        editor.commit();
    }

    public boolean isFirstTimeLaunch() {
        return pref.getBoolean(IS_FIRST_TIME_LAUNCH, true);
    }


    // Getting Id's of all the product that are in Wishlist database
    public ArrayList<Long> getWishlist(final LottieAnimationView view){

        final ArrayList<Long> wishlistProductIds = new ArrayList<>();
        wishlistProductIds.clear();
        String usermobile = this.getUserDetails().get(UserSession.KEY_MOBiLE);
        String username = this.getUserDetails().get(UserSession.KEY_NAME);

        firebaseFirestore.collection("Wishlist").document(usermobile).collection(username+" Wishlist").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        if (task.isSuccessful()) {

                            view.setVisibility(View.INVISIBLE);

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                wishlistProductIds.add((Long) document.getData().get("prid"));
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }});

        return wishlistProductIds;
    }
}


