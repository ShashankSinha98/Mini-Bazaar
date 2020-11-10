package com.example.minibazaar.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.example.minibazaar.Models.GenericProductModel;
import com.example.minibazaar.Models.SingleProductModel;
import com.example.minibazaar.NetworkSync.CheckInternetConnection;
import com.example.minibazaar.ProductsCategory.ShoesActivity;
import com.example.minibazaar.R;
import com.example.minibazaar.UserSession.UserSession;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.dmoral.toasty.Toasty;

public class IndividualProductActivity extends AppCompatActivity {

    @BindView(R.id.productimage)
    ImageView productimage;
    @BindView(R.id.productname)
    TextView productname;
    @BindView(R.id.productprice)
    TextView productprice;
    @BindView(R.id.add_to_cart)
    TextView addToCart;
    @BindView(R.id.buy_now)
    TextView buyNow;
    @BindView(R.id.productdesc)
    TextView productdesc;
    @BindView(R.id.quantityProductPage)
    EditText quantityProductPage;
    @BindView(R.id.add_to_wishlist)
    LottieAnimationView addToWishlist;
    @BindView(R.id.customheader)
    EditText customheader;
    @BindView(R.id.custommessage)
    EditText custommessage;


    private boolean alreadyInWishlist = false;

    private GenericProductModel model;
    private UserSession session;

    private String usermobile, useremail;
    private int quantity = 1;
    private String username;

    private FirebaseFirestore firestore;

    @BindView(R.id.wishlist_red)
    ImageView wishlist_red;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_individual_product);
        ButterKnife.bind(this);



        //check Internet Connection
        new CheckInternetConnection(this).checkConnection();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        initialize();

        wishlist_red.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final KProgressHUD progressDialog=  KProgressHUD.create(IndividualProductActivity.this)
                        .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                        .setLabel("Please wait")
                        .setCancellable(false)
                        .setAnimationSpeed(2)
                        .setDimAmount(0.5f)
                        .show();


                firestore.collection("Wishlist").document(usermobile).collection(username+" Wishlist")
                        .document(String.valueOf(model.getCardid())).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        progressDialog.dismiss();
                        Log.d("xlr8_wlv", String.valueOf(session.getWishlistValue()));
                        addToWishlist.setVisibility(View.VISIBLE);
                        addToWishlist.cancelAnimation();
                        addToWishlist.clearAnimation();
                        addToWishlist.setProgress(0);
                        addToWishlist = findViewById(R.id.add_to_wishlist);
                        wishlist_red.setVisibility(View.GONE);
                        session.decreaseWishlistValue();
                        ShoesActivity.wishlist.remove(Long.valueOf(model.getCardid()));

                        Toasty.success(IndividualProductActivity.this,"Removed from Wishlist",2000).show();
                    }
                });
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        //check Internet Connection
        new CheckInternetConnection(this).checkConnection();
    }

    private void initialize() {

        firestore = FirebaseFirestore.getInstance();
        model = (GenericProductModel) getIntent().getSerializableExtra("product");

        productprice.setText("₹ " + Float.toString(model.getCardprice()));
        productname.setText(model.getCardname());
        productdesc.setText(model.getCarddiscription());
        quantityProductPage.setText("1");
        Picasso.get().load(model.getCardimage()).into(productimage);

        //SharedPreference for Cart Value
        session = new UserSession(getApplicationContext());

        //validating session
        session.isLoggedIn();
        usermobile = session.getUserDetails().get(UserSession.KEY_MOBiLE);
        useremail = session.getUserDetails().get(UserSession.KEY_EMAIL);
        username = session.getUserDetails().get(UserSession.KEY_NAME);

        Log.d("xlr8", String.valueOf(ShoesActivity.wishlist));
        Log.d("xlr8_id", String.valueOf(model.getCardid()));

        if(ShoesActivity.wishlist.contains(Long.valueOf(model.getCardid()))){
            Log.d("xlr8","True");
            wishlist_red.setVisibility(View.VISIBLE);
            addToWishlist.setVisibility(View.GONE);
        } else {
            Log.d("xlr8","False");
            wishlist_red.setVisibility(View.GONE);
            addToWishlist.setVisibility(View.VISIBLE);
        }


        //setting textwatcher for no of items field
        quantityProductPage.addTextChangedListener(productcount);




    }

    //check that product count must not exceed 500
    TextWatcher productcount = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            //none
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (quantityProductPage.getText().toString().equals("")) {
                quantityProductPage.setText("0");
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
            //none
            if (Integer.parseInt(quantityProductPage.getText().toString()) >= 500) {
                Toasty.error(IndividualProductActivity.this, "Product Count Must be less than 500", Toast.LENGTH_LONG).show();
            }
        }

    };

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public void Notifications(View view) {
        //startActivity(new Intent(IndividualProductActivity.this, NotificationActivity.class));
        //finish();
    }

    public void decrement(View view) {
        if (quantity > 1) {
            quantity--;
            quantityProductPage.setText(String.valueOf(quantity));
        }
    }

    public void increment(View view) {
        if (quantity < 500) {
            quantity++;
            quantityProductPage.setText(String.valueOf(quantity));
        } else {
            Toasty.error(IndividualProductActivity.this, "Product Count Must be less than 500", Toast.LENGTH_LONG).show();
        }
    }

    public void shareProduct(View view) {

        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        String shareBody = "Found amazing " + productname.getText().toString() + "on Mini Bazaar App";
        sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
        startActivity(Intent.createChooser(sharingIntent, "Share via"));
    }

    public void similarProduct(View view) {
        finish();
    }

    public void addToWishList(View view) {

        final KProgressHUD progressDialog=  KProgressHUD.create(IndividualProductActivity.this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setLabel("Please wait")
                .setCancellable(false)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
                .show();

        firestore.collection("Wishlist").document(usermobile).collection(username+" Wishlist")
                .document(String.valueOf(model.getCardid())).set(getProductObject()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                progressDialog.dismiss();
                addToWishlist.playAnimation();
                session.increaseWishlistValue();
                ShoesActivity.wishlist.add(Long.valueOf(model.getCardid()));
                Log.d("xlr8_wlv", String.valueOf(session.getWishlistValue()));
                Toasty.success(IndividualProductActivity.this,"Added to your Wishlist",2000).show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        addToWishlist.clearAnimation();
                        addToWishlist.setVisibility(View.GONE);
                        wishlist_red.setVisibility(View.VISIBLE);

                    }
                },500);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toasty.error(IndividualProductActivity.this,"Failed to add.",2000).show();
            }
        });

        /*firestore.collection("Wishlist").document(usermobile).collection(username+" Wishlist")
                .add(getProductObject()).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {

                progressDialog.dismiss();
                addToWishlist.playAnimation();
                session.increaseWishlistValue();
                ShoesActivity.wishlistProductIds.add(Long.valueOf(model.getCardid()));
                Toasty.success(IndividualProductActivity.this,"Added to your Wishlist",2000).show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        addToWishlist.setVisibility(View.GONE);
                        wishlist_red.setVisibility(View.VISIBLE);

                    }
                },1000);


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toasty.error(IndividualProductActivity.this,"Failed to add.",2000).show();
            }
        });*/
    }
    private SingleProductModel getProductObject() {

        return new SingleProductModel(model.getCardid(), Integer.parseInt(quantityProductPage.getText().toString()), useremail, usermobile, model.getCardname(), Float.toString(model.getCardprice()), model.getCardimage(), model.carddiscription,customheader.getText().toString(),custommessage.getText().toString());

    }

    public void goToCart(View view) {

        if ( customheader.getText().toString().length() == 0 ||  custommessage.getText().toString().length() ==0 ){

            Snackbar.make(view, "Header or Message Empty", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }else {
            addToCartProcess(false);
            startActivity(new Intent(IndividualProductActivity.this, CartActivity.class));
            finish();
        }

    }

    private void addToCartProcess(final boolean addToCart){
        final KProgressHUD progressDialog=  KProgressHUD.create(IndividualProductActivity.this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setLabel("Please wait")
                .setCancellable(false)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
                .show();

        firestore.collection("Cart").document(useremail).collection(username+" Cart")
                .document(String.valueOf(model.getCardid())).set(getProductObject()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                progressDialog.dismiss();
                session.increaseCartValue();
                if (addToCart==true) {
                    Toasty.success(IndividualProductActivity.this, "Added to Cart", 2000).show();
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toasty.error(IndividualProductActivity.this,"Failed to add.",2000).show();
            }
        });

    }

    public void addToCart(View view) {

        if ( customheader.getText().toString().length() == 0 ||  custommessage.getText().toString().length() ==0 ){

            Snackbar.make(view, "Header or Message Empty", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        } else {

            addToCartProcess(true);

        }
    }
}