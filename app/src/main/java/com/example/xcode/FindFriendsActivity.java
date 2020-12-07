package com.example.xcode;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindFriendsActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private ProgressDialog loadingBar;
    private ImageButton searchBttn;
    private EditText searchBox;
    private RecyclerView searchresultList;
    private FirebaseAuth mAuth;
    private DatabaseReference uRef;
    private String getCurrentUid;
    private String searchboxinput;
    private Query searchPeople;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);
        mAuth = FirebaseAuth.getInstance();
        getCurrentUid = mAuth.getCurrentUser().getUid();
        uRef = FirebaseDatabase.getInstance().getReference().child("Users");

        Toolbar mToolbar = (Toolbar) findViewById(R.id.nav_find_friends);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Arkadaş Bul");

        searchBttn = (ImageButton) findViewById(R.id.ff_searchbttn);
        searchBox = (EditText)findViewById(R.id.ff_searchbox);

        searchresultList = (RecyclerView) findViewById(R.id.ff_searchResults);
        searchresultList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        searchresultList.setLayoutManager(linearLayoutManager);

        searchBttn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               searchboxinput = searchBox.getText().toString();
                Toast.makeText(FindFriendsActivity.this,"Aranıyor...",Toast.LENGTH_SHORT).show();

                searchPeople = uRef.orderByChild("fullname")
                        .startAt(searchboxinput).endAt(searchboxinput+"\uf8ff");
                SearchPeople(searchPeople);
            }
        });
    }

    private void SearchPeople(Query searchPeople) {
        super.onStart();
        FirebaseRecyclerOptions<FindFriends> options =
                new FirebaseRecyclerOptions.Builder<FindFriends>()
                        .setQuery(searchPeople, FindFriends.class)
                        .build();
        FirebaseRecyclerAdapter<FindFriends,FindFriendsViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<FindFriends, FindFriendsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull FindFriendsViewHolder holder, int position, @NonNull FindFriends model) {
                        holder.fullname.setText(model.getFullname());
                        holder.status.setText(model.getStatus());
                        Picasso.get().load(model.getProfileimage()).into(holder.profileimage);;

                    }

                    @NonNull
                    @Override
                    public FindFriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_users_display_layout, parent, false);
                        FindFriendsViewHolder viewHolder = new FindFriendsViewHolder(view);
                        return viewHolder;
                    }
                };
        searchresultList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();
        if (id == android.R.id.home) {
            SendUserToMainActivity();
        }

        return super.onOptionsItemSelected(item);
    }

    public static class FindFriendsViewHolder extends RecyclerView.ViewHolder
    {
        TextView fullname,status;
        View mView;
        CircleImageView profileimage;

        public FindFriendsViewHolder(@NonNull View itemView) {
            super(itemView);
            fullname = itemView.findViewById(R.id.all_users_fullname);
            status = itemView.findViewById(R.id.all_users_status);
            profileimage = itemView.findViewById(R.id.all_users_pp);

        }
    }


    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(FindFriendsActivity.this , MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}