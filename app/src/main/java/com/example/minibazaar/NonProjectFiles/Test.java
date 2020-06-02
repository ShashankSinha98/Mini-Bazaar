package com.example.minibazaar.NonProjectFiles;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.minibazaar.R;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import de.hdodenhof.circleimageview.CircleImageView;

public class Test extends AppCompatActivity {

    ProgressBar progressBar;

    RecyclerView friendList;

    private FirebaseFirestore db;
    private FirestoreRecyclerAdapter adapter;
    LinearLayoutManager linearLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        progressBar = findViewById(R.id.progress_bar);
        friendList = findViewById(R.id.friend_list);

        init();
        getFriendList();
    }

    private void init(){
        linearLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        friendList.setLayoutManager(linearLayoutManager);
        db = FirebaseFirestore.getInstance();
    }


    private void getFriendList(){
        Query query = db.collection("friends");

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                if(task.isSuccessful()){
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Log.d("xlr8", document.getId() + " => " + document.getData());
                    }
                } else {
                    Log.d("xlr8", "Error getting documents.", task.getException());
                }

            }
        });

        Log.d("xlr8","Creating Response");
        final FirestoreRecyclerOptions<FriendsResponse> response = new FirestoreRecyclerOptions.Builder<FriendsResponse>()
                .setQuery(query, FriendsResponse.class)
                .build();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d("xlr8", String.valueOf(response.getSnapshots()));

            }
        },2000);


        Log.d("xlr8","Response Created");
        Log.d("xlr8","Getting Data");
        adapter = new FirestoreRecyclerAdapter<FriendsResponse, FriendsHolder>(response) {
            @Override
            public void onBindViewHolder(FriendsHolder holder, int position, FriendsResponse model) {
                Log.d("xlr8","Populating Data");
                progressBar.setVisibility(View.GONE);
                holder.textName.setText(model.getName());
                holder.textTitle.setText(model.getTitle());
                holder.textCompany.setText(model.getCompany());
                Glide.with(getApplicationContext())
                        .load(model.getImage())
                        .into(holder.imageView);

            }

            @Override
            public FriendsHolder onCreateViewHolder(ViewGroup group, int i) {
                Log.d("xlr8","Inflating Layout");
                View view = LayoutInflater.from(group.getContext())
                        .inflate(R.layout.list_item, group, false);

                return new FriendsHolder(view);
            }

            @Override
            public void onError(FirebaseFirestoreException e) {
                Log.e("error", e.getMessage());
            }
        };

        adapter.notifyDataSetChanged();
        friendList.setAdapter(adapter);



    }

    public class FriendsHolder extends RecyclerView.ViewHolder {
        //@BindView(R.id.name)
        TextView textName;
        //@BindView(R.id.image)
        CircleImageView imageView;
        //@BindView(R.id.title)
        TextView textTitle;
        //@BindView(R.id.company)
        TextView textCompany;

        public FriendsHolder(View itemView) {
            super(itemView);
            //ButterKnife.bind(this, itemView);
            textName = itemView.findViewById(R.id.name);
            imageView = itemView.findViewById(R.id.image);
            textTitle = itemView.findViewById(R.id.title);
            textCompany = itemView.findViewById(R.id.company);

        }
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