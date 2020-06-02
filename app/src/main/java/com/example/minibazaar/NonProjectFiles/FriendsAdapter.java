package com.example.minibazaar.NonProjectFiles;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.minibazaar.R;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsAdapter extends FirestoreRecyclerAdapter<FriendsResponse, FriendsAdapter.FriendsHolder> {


    /**
     * Create a new RecyclerView adapter that listens to a Firestore Query.  See {@link
     * FirestoreRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public FriendsAdapter(@NonNull FirestoreRecyclerOptions<FriendsResponse> options) {
        super(options);
        Log.d("xlr8","Adapter Constructor called");

    }

    @Override
    protected void onBindViewHolder(@NonNull FriendsHolder holder, int position, @NonNull FriendsResponse model) {
        Log.d("xlr8","Populating Data");
        holder.textName.setText(model.getName());
        holder.textTitle.setText(model.getTitle());
        holder.textCompany.setText(model.getCompany());

    }

    @NonNull
    @Override
    public FriendsHolder onCreateViewHolder(@NonNull ViewGroup group, int viewType) {
        Log.d("xlr8","Inflating Layout");
        View view = LayoutInflater.from(group.getContext())
                .inflate(R.layout.list_item, group, false);

        return new FriendsHolder(view);
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
            Log.d("xlr8","ViewHolder Called ");
            //ButterKnife.bind(this, itemView);
            textName = itemView.findViewById(R.id.name);
            imageView = itemView.findViewById(R.id.image);
            textTitle = itemView.findViewById(R.id.title);
            textCompany = itemView.findViewById(R.id.company);

        }
    }

}
