package com.example.minibazaar.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.example.minibazaar.Models.PlacedOrderModel;
import com.example.minibazaar.Models.SingleProductModel;
import com.example.minibazaar.R;
import com.example.minibazaar.UserSession.UserSession;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;

public class OrderTrackActivity extends AppCompatActivity {


    private final String TAG = this.getClass().getSimpleName()+"_xlr8";
    @BindView(R.id.tick_1)
    ImageView OR_T_IV;
    @BindView(R.id.tick_2)
    ImageView OC_T_IV;
    @BindView(R.id.tick_3)
    ImageView OFD_T_IV;
    @BindView(R.id.tick_4)
    ImageView OS_T_IV;

    @BindView(R.id.view_1)
    View v1;
    @BindView(R.id.view_2)
    View v2;
    @BindView(R.id.view_3)
    View v3;

    @BindView(R.id.tv_7)
    TextView OR_H_TV;
    @BindView(R.id.tv_8)
    TextView OR_D_TV;

    @BindView(R.id.tv_5)
    TextView OC_H_TV;
    @BindView(R.id.tv_6)
    TextView OC_D_TV;

    @BindView(R.id.tv_3)
    TextView OFD_H_TV;
    @BindView(R.id.tv_4)
    TextView OFD_D_TV;

    @BindView(R.id.tv_1)
    TextView OS_H_TV;
    @BindView(R.id.tv_2)
    TextView OS_D_TV;

    @BindView(R.id.order_placed_anim)
    LottieAnimationView OR_A;
    @BindView(R.id.order_confirmed_anim)
    LottieAnimationView OC_A;
    @BindView(R.id.order_out_anim)
    LottieAnimationView OFD_A;
    @BindView(R.id.order_received_anim)
    LottieAnimationView OS_A;

    @BindView(R.id.current_status)
    TextView current_status_header;

    @BindView(R.id.icon_4)
    ImageView OR_I;
    @BindView(R.id.icon_3)
    ImageView OC_I;
    @BindView(R.id.icon_2)
    ImageView OFD_I;
    @BindView(R.id.icon_1)
    ImageView OS_I;

    private RecyclerView mRecyclerView;
    private StaggeredGridLayoutManager mLayoutManager;

    private FirebaseFirestore firebaseFirestore;
    private HashMap<String,String> user;
    private UserSession session;
    private String snapshotId;
    private PlacedOrderModel placedOrderModel;
    private LottieAnimationView tv_no_item;
    private FirestoreRecyclerAdapter adapter;
    private RelativeLayout mainTrackLayout;

    private TextView totalAmt;
    private TextView totalItems;

    private float totalcost=0;
    private int totalproducts=0;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_track);
        ButterKnife.bind(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Cart");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mRecyclerView = findViewById(R.id.recyclerview);
        mLayoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mRecyclerView.setNestedScrollingEnabled(false);

        Intent intent = getIntent();
        firebaseFirestore = FirebaseFirestore.getInstance();
        session = new UserSession(OrderTrackActivity.this);
        tv_no_item = findViewById(R.id.tv_no_cards);
        mainTrackLayout = findViewById(R.id.main_layout_track);
        mainTrackLayout.setVisibility(View.INVISIBLE);
        totalAmt = findViewById(R.id.total_amt_tv);
        totalItems = findViewById(R.id.total_items_tv);
        user = session.getUserDetails();

       snapshotId = intent.getExtras().getString("snapshot_id");
       placedOrderModel = (PlacedOrderModel) intent.getSerializableExtra("placed_order_model");

       // Toast.makeText(this, "Id: "+snapshotId, Toast.LENGTH_SHORT).show();


        updateTrackUIAccToStatus(placedOrderModel.getOrder_status());

        populateRecyclerView();


    }

    private void populateRecyclerView() {

        final Query bottomInvoiceQuery = firebaseFirestore.collection("Orders").document(user.get(UserSession.KEY_EMAIL))
                .collection(user.get(UserSession.KEY_NAME)+" Orders").document(snapshotId)
                .collection("Items");

        bottomInvoiceQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                if(task.isSuccessful()){
                    Log.d(TAG,"Task Success");
                    if(task.getResult().size()==0){
                        tv_no_item.setVisibility(View.GONE);
                        mainTrackLayout.setVisibility(View.INVISIBLE);
                        return;
                    }

                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Log.d(TAG, document.getId() + " => " + document.getData());
                    }

                } else {
                    Log.d(TAG, "Error getting documents.", task.getException());
                }
            }
        });

        final FirestoreRecyclerOptions<SingleProductModel> response = new FirestoreRecyclerOptions.Builder<SingleProductModel>()
                .setQuery(bottomInvoiceQuery, SingleProductModel.class)
                .build();

        adapter = new FirestoreRecyclerAdapter<SingleProductModel, OrderTrackActivity.MovieViewHolder>(response) {
            @Override
            protected void onBindViewHolder(@NonNull OrderTrackActivity.MovieViewHolder viewHolder, final int position, @NonNull SingleProductModel model) {
                Log.d(TAG,"onBindViewHolder called for: "+position);

                if(tv_no_item.getVisibility()== View.VISIBLE){
                    tv_no_item.setVisibility(View.GONE);
                    mainTrackLayout.setVisibility(View.VISIBLE);
                }

                viewHolder.cardname.setText(model.getPrname());
                viewHolder.cardprice.setText("₹ "+model.getPrprice());
                viewHolder.cardcount.setText("Quantity : "+model.getNo_of_items());
                viewHolder.totalCardAmt.setText("₹ "+model.getPrprice()+" x "+model.getNo_of_items()+" = ₹ "+(Float.valueOf(model.getPrprice())*model.getNo_of_items()));
                Picasso.get().load(model.getPrimage()).into(viewHolder.cardimage);

                totalcost += model.getNo_of_items()*Float.parseFloat(model.getPrprice());
                totalproducts += model.getNo_of_items();

                totalItems.setText("No. of Items- "+totalproducts);
                totalAmt.setText("Total Amount Paid- ₹"+totalcost);


            }

            @NonNull
            @Override
            public OrderTrackActivity.MovieViewHolder onCreateViewHolder(@NonNull ViewGroup group, int viewType) {
                Log.d(TAG,"Inflating Layout");
                View view = LayoutInflater.from(group.getContext())
                        .inflate(R.layout.order_track_item_layout, group, false);

                return new OrderTrackActivity.MovieViewHolder(view);

            }


        };


        adapter.notifyDataSetChanged();
        mRecyclerView.setAdapter(adapter);


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, String.valueOf(response.getSnapshots()));

            }
        },10000);



    }


    //viewHolder for our Firebase UI
    public static class MovieViewHolder extends RecyclerView.ViewHolder{

        TextView cardname;
        ImageView cardimage;
        TextView cardprice;
        TextView cardcount;
        TextView totalCardAmt;

        View mView;
        public MovieViewHolder(View v) {
            super(v);
            mView = v;
            cardname = v.findViewById(R.id.cart_prtitle);
            cardimage = v.findViewById(R.id.image_cartlist);
            cardprice = v.findViewById(R.id.cart_prprice);
            cardcount = v.findViewById(R.id.cart_prcount);
            totalCardAmt = v.findViewById(R.id.total_card_amount);
        }
    }


    private void updateTrackUIAccToStatus(String status){

        switch (status){

            case "ORDER_PLACED":
                // title
                current_status_header.setText("ORDER RECEIVED");

                // anims
                OR_A.setVisibility(View.VISIBLE);
                OC_A.setVisibility(View.INVISIBLE);
                OFD_A.setVisibility(View.INVISIBLE);
                OS_A.setVisibility(View.INVISIBLE);

                // ticks
                OR_T_IV.setImageResource(R.drawable.green_tick);
                OC_T_IV.setImageResource(R.drawable.grey_tick);
                OFD_T_IV.setImageResource(R.drawable.grey_tick);
                OS_T_IV.setImageResource(R.drawable.grey_tick);

                // line views
                v1.setBackgroundColor(getResources().getColor(R.color.md_blue_grey_500));
                v2.setBackgroundColor(getResources().getColor(R.color.md_blue_grey_500));
                v3.setBackgroundColor(getResources().getColor(R.color.md_blue_grey_500));

                // other text header
                OR_H_TV.setAlpha(1f);
                OC_H_TV.setAlpha(0.3f);
                OFD_H_TV.setAlpha(0.3f);
                OS_H_TV.setAlpha(0.3f);

                //other header
                OR_D_TV.setVisibility(View.VISIBLE);
                OC_D_TV.setVisibility(View.INVISIBLE);
                OFD_D_TV.setVisibility(View.INVISIBLE);
                OS_D_TV.setVisibility(View.INVISIBLE);
                OR_D_TV.setText(OR_D_TV.getText()+placedOrderModel.getOrder_date_time().split("-")[0]);

                // Icons
                OR_I.setAlpha(1f);
                OC_I.setAlpha(0.3f);
                OFD_I.setAlpha(0.3f);
                OS_I.setAlpha(0.3f);
                break;


            case "ORDER_CONFIRMED":
                // title
                current_status_header.setText("ORDER CONFIRMED");

                // anims
                OR_A.setVisibility(View.INVISIBLE);
                OC_A.setVisibility(View.VISIBLE);
                OFD_A.setVisibility(View.INVISIBLE);
                OS_A.setVisibility(View.INVISIBLE);

                // ticks
                OR_T_IV.setImageResource(R.drawable.green_tick);
                OC_T_IV.setImageResource(R.drawable.green_tick);
                OFD_T_IV.setImageResource(R.drawable.grey_tick);
                OS_T_IV.setImageResource(R.drawable.grey_tick);

                // line views
                v1.setBackgroundColor(getResources().getColor(R.color.md_green_500));
                v2.setBackgroundColor(getResources().getColor(R.color.md_blue_grey_500));
                v3.setBackgroundColor(getResources().getColor(R.color.md_blue_grey_500));

                // other text header
                OR_H_TV.setAlpha(1f);
                OC_H_TV.setAlpha(1f);
                OFD_H_TV.setAlpha(0.3f);
                OS_H_TV.setAlpha(0.3f);

                //details
                OR_D_TV.setVisibility(View.VISIBLE);
                OC_D_TV.setVisibility(View.VISIBLE);
                OFD_D_TV.setVisibility(View.INVISIBLE);
                OS_D_TV.setVisibility(View.INVISIBLE);
                OR_D_TV.setText(OR_D_TV.getText()+placedOrderModel.getOrder_date_time().split("-")[0]);

                // Icons
                OR_I.setAlpha(1f);
                OC_I.setAlpha(1f);
                OFD_I.setAlpha(0.3f);
                OS_I.setAlpha(0.3f);
                break;



            case "OUT_FOR_DELIVERY":
                // title
                current_status_header.setText("OUT FOR DELIVERY");

                // anims
                OR_A.setVisibility(View.INVISIBLE);
                OC_A.setVisibility(View.INVISIBLE);
                OFD_A.setVisibility(View.VISIBLE);
                OS_A.setVisibility(View.INVISIBLE);

                // ticks
                OR_T_IV.setImageResource(R.drawable.green_tick);
                OC_T_IV.setImageResource(R.drawable.green_tick);
                OFD_T_IV.setImageResource(R.drawable.green_tick);
                OS_T_IV.setImageResource(R.drawable.grey_tick);

                // line views
                v1.setBackgroundColor(getResources().getColor(R.color.md_green_500));
                v2.setBackgroundColor(getResources().getColor(R.color.md_green_500));
                v3.setBackgroundColor(getResources().getColor(R.color.md_blue_grey_500));

                // other text header
                OR_H_TV.setAlpha(1f);
                OC_H_TV.setAlpha(1f);
                OFD_H_TV.setAlpha(1f);
                OS_H_TV.setAlpha(0.3f);

                //details
                OR_D_TV.setVisibility(View.VISIBLE);
                OC_D_TV.setVisibility(View.VISIBLE);
                OFD_D_TV.setVisibility(View.VISIBLE);
                OS_D_TV.setVisibility(View.INVISIBLE);
                OR_D_TV.setText(OR_D_TV.getText()+placedOrderModel.getOrder_date_time().split("-")[0]);

                // Icons
                OR_I.setAlpha(1f);
                OC_I.setAlpha(1f);
                OFD_I.setAlpha(1f);
                OS_I.setAlpha(0.3f);
                break;



            case "ORDER_SHIPPED":
                // title
                current_status_header.setText("ORDER SHIPPED");

                // anims
                OR_A.setVisibility(View.INVISIBLE);
                OC_A.setVisibility(View.INVISIBLE);
                OFD_A.setVisibility(View.INVISIBLE);
                OS_A.setVisibility(View.VISIBLE);

                // ticks
                OR_T_IV.setImageResource(R.drawable.green_tick);
                OC_T_IV.setImageResource(R.drawable.green_tick);
                OFD_T_IV.setImageResource(R.drawable.green_tick);
                OS_T_IV.setImageResource(R.drawable.green_tick);

                // line views
                v1.setBackgroundColor(getResources().getColor(R.color.md_green_500));
                v2.setBackgroundColor(getResources().getColor(R.color.md_green_500));
                v3.setBackgroundColor(getResources().getColor(R.color.md_green_500));

                // other text header
                OR_H_TV.setAlpha(1f);
                OC_H_TV.setAlpha(1f);
                OFD_H_TV.setAlpha(1f);
                OS_H_TV.setAlpha(1f);

                //details
                OR_D_TV.setVisibility(View.VISIBLE);
                OC_D_TV.setVisibility(View.VISIBLE);
                OFD_D_TV.setVisibility(View.VISIBLE);
                OS_D_TV.setVisibility(View.VISIBLE);
                OS_D_TV.setText(OS_D_TV.getText()+placedOrderModel.getDelivery_date());
                OR_D_TV.setText(OR_D_TV.getText()+placedOrderModel.getOrder_date_time().split("-")[0]);

                // Icons
                OR_I.setAlpha(1f);
                OC_I.setAlpha(1f);
                OFD_I.setAlpha(1f);
                OS_I.setAlpha(1f);
                break;


        }

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }


    public void Notifications(View view) {
    }

    public void viewProfile(View view) {
        Intent i = new Intent(OrderTrackActivity.this, ProfileActivity.class);
        //i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("xlr8","Adapter Listening");
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("xlr8","Adapter Not  Listening");
        adapter.stopListening();
    }
}