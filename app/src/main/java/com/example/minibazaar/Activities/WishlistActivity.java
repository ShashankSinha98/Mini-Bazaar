package com.example.minibazaar.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.example.minibazaar.Models.SingleProductModel;
import com.example.minibazaar.NetworkSync.CheckInternetConnection;
import com.example.minibazaar.R;
import com.example.minibazaar.UserSession.UserSession;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class WishlistActivity extends AppCompatActivity {

    private final String TAG = this.getClass().getSimpleName();
    private String name,email,photo,mobile;
    private UserSession session;
    private HashMap<String,String> user;
    private RecyclerView mRecyclerView;
    private StaggeredGridLayoutManager mLayoutManager;

    private LottieAnimationView tv_no_item;
    private FrameLayout activitycartlist;
    private LottieAnimationView emptycart;
    private FirebaseFirestore firebaseFirestore;
    private FirestoreRecyclerAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wishlist);


        FirebaseApp.initializeApp(this);
        firebaseFirestore = FirebaseFirestore.getInstance();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Cart");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //check Internet Connection
        new CheckInternetConnection(this).checkConnection();



        //retrieve session values and display on listviews
        getValues();

        //SharedPreference for Cart Value
        session = new UserSession(getApplicationContext());

        //validating session
        session.isLoggedIn();

        mRecyclerView = findViewById(R.id.recyclerview);
        tv_no_item = findViewById(R.id.tv_no_cards);
        activitycartlist = findViewById(R.id.frame_container);
        emptycart = findViewById(R.id.empty_cart);

        if (mRecyclerView != null) {
            //to enable optimization of recyclerview
            mRecyclerView.setHasFixedSize(true);
        }

        //using staggered grid pattern in recyclerview
        mLayoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);

        populateRecyclerView();
        /*if(session.getWishlistValue()>0) {
            populateRecyclerView();
        }else if(session.getWishlistValue() == 0)  {
            tv_no_item.setVisibility(View.GONE);
            activitycartlist.setVisibility(View.GONE);
            emptycart.setVisibility(View.VISIBLE);
        }*/
    }

    private void getValues() {

        //create new session object by passing application context
        session = new UserSession(getApplicationContext());

        //validating session
        session.isLoggedIn();

        //get User details if logged in
        user = session.getUserDetails();

        name = user.get(UserSession.KEY_NAME);
        email = user.get(UserSession.KEY_EMAIL);
        mobile = user.get(UserSession.KEY_MOBiLE);
        photo = user.get(UserSession.KEY_PHOTO);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG,"Adapter Listening");
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG,"Adapter Not  Listening");
        adapter.stopListening();
    }

    public void viewProfile(View view) {
      //  startActivity(new Intent(Wishlist.this,Profile.class));
       // finish();
    }

    @Override
    protected void onResume() {
        super.onResume();

        //check Internet Connection
        new CheckInternetConnection(this).checkConnection();

    }


    private void populateRecyclerView() {

        Log.d("xlr8_wl","Populate Recycler view called");


        Query query = firebaseFirestore.collection("Wishlist").document(user.get(UserSession.KEY_MOBiLE))
                .collection(user.get(UserSession.KEY_NAME)+" Wishlist");

       // Query query = firebaseFirestore.collection("friends");



        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                if(task.isSuccessful()){

                    if(task.getResult().size()==0){
                        tv_no_item.setVisibility(View.GONE);
                        activitycartlist.setVisibility(View.GONE);
                        emptycart.setVisibility(View.VISIBLE);
                        return;
                    }

                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Log.d("xlr8_wl", document.getId() + " => " + document.getData());
                    }
                } else {
                    Log.d("xlr8_wl", "Error getting documents.", task.getException());
                }

            }
        });


        final FirestoreRecyclerOptions<SingleProductModel> response = new FirestoreRecyclerOptions.Builder<SingleProductModel>()
                .setQuery(query, SingleProductModel.class)
                .build();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d("xlr8_wl", String.valueOf(response.getSnapshots()));

            }
        },10000);



        adapter =new FirestoreRecyclerAdapter<SingleProductModel, WishlistActivity.WishlistViewHolder>(response) {
            @Override
            protected void onBindViewHolder(@NonNull WishlistActivity.WishlistViewHolder viewHolder, final int position, @NonNull SingleProductModel model) {
                Log.d(TAG,"Populating Data");

                if(tv_no_item.getVisibility()== View.VISIBLE){
                    tv_no_item.setVisibility(View.GONE);
                }
                viewHolder.cardname.setText(model.getPrname());
                viewHolder.cardprice.setText("₹ "+model.getPrprice());
                viewHolder.cardcount.setText("Quantity : "+model.getNo_of_items());
                Picasso.get().load(model.getPrimage()).into(viewHolder.cardimage);

                viewHolder.carddelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(WishlistActivity.this,getItem(position).getPrname(),Toast.LENGTH_SHORT).show();
                        getSnapshots().getSnapshot(position).getReference().delete();
                        session.decreaseWishlistValue();
                        Log.d("xlr8_wlv", String.valueOf(session.getWishlistValue()));
                        startActivity(new Intent(WishlistActivity.this,WishlistActivity.class));
                        finish();
                    }
                });
            }

            @NonNull
            @Override
            public WishlistActivity.WishlistViewHolder onCreateViewHolder(@NonNull ViewGroup group, int viewType) {
                Log.d(TAG,"Inflating Layout");
                View view = LayoutInflater.from(group.getContext())
                        .inflate(R.layout.wishlist_item_layout, group, false);

                return new WishlistActivity.WishlistViewHolder(view);
            }


        };


        adapter.notifyDataSetChanged();
        mRecyclerView.setAdapter(adapter);


        /*

        //Say Hello to our new FirebaseUI android Element, i.e., FirebaseRecyclerAdapter
        final FirestoreRecyclerAdapter<SingleProductModel,MovieViewHolder> adapter = new FirestoreRecyclerAdapter<SingleProductModel, MovieViewHolder>(
                SingleProductModel.class,
                R.layout.wishlist_item_layout,
                MovieViewHolder.class,
                //referencing the node where we want the database to store the data from our Object
                mDatabaseReference.child("wishlist").child(mobile).getRef()
        ) {
            @Override
            protected void populateViewHolder(final MovieViewHolder viewHolder, final SingleProductModel model, final int position) {
                if(tv_no_item.getVisibility()== View.VISIBLE){
                    tv_no_item.setVisibility(View.GONE);
                }
                viewHolder.cardname.setText(model.getPrname());
                viewHolder.cardprice.setText("₹ "+model.getPrprice());
                viewHolder.cardcount.setText("Quantity : "+model.getNo_of_items());
                Picasso.with(Wishlist.this).load(model.getPrimage()).into(viewHolder.cardimage);

                viewHolder.carddelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(Wishlist.this,getItem(position).getPrname(),Toast.LENGTH_SHORT).show();
                        getRef(position).removeValue();
                        session.decreaseWishlistValue();
                        startActivity(new Intent(Wishlist.this,Wishlist.class));
                        finish();
                    }
                });

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Wishlist.this,IndividualProduct.class);
                        intent.putExtra("product",new GenericProductModel(model.getPrid(),model.getPrname(),model.getPrimage(),model.getPrdesc(),Float.parseFloat(model.getPrprice())));
                        startActivity(intent);
                    }
                });
            }

        };
        mRecyclerView.setAdapter(adapter);*/
    }

    //viewHolder for our Firebase UI
    public static class WishlistViewHolder extends RecyclerView.ViewHolder{

        TextView cardname;
        ImageView cardimage;
        TextView cardprice;
        TextView cardcount;
        ImageView carddelete;

        View mView;
        public WishlistViewHolder(View v) {
            super(v);
            mView = v;
            cardname = v.findViewById(R.id.cart_prtitle);
            cardimage = v.findViewById(R.id.image_cartlist);
            cardprice = v.findViewById(R.id.cart_prprice);
            cardcount = v.findViewById(R.id.cart_prcount);
            carddelete = v.findViewById(R.id.deletecard);
        }
    }


    public void Notifications(View view) {

      //  startActivity(new Intent(Wishlist.this,NotificationActivity.class));
       // finish();
    }
}