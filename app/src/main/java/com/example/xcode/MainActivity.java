package com.example.xcode;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity
{

    private CircleImageView nav_headerpp;
    private TextView nav_profilename,nav_status;
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private RecyclerView postlist;
    private Toolbar pToolbar;
    private ImageButton addNewPostBtn;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private FirebaseAuth mFirebaseAuth;
    private DatabaseReference uRef,postRef,favRef;
    String getCurrentUid;
    Boolean favStatus = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mFirebaseAuth = FirebaseAuth.getInstance();
        getCurrentUid = mFirebaseAuth.getCurrentUser().getUid();
        uRef = FirebaseDatabase.getInstance().getReference().child("Users");
        postRef=FirebaseDatabase.getInstance().getReference().child("Posts");
        favRef=FirebaseDatabase.getInstance().getReference().child("Favourites");
        setContentView(R.layout.activity_main);
        Toolbar pToolbar= (Toolbar)findViewById(R.id.page_toolbar);
        setSupportActionBar(pToolbar);
        getSupportActionBar().setTitle("Duvar");

        addNewPostBtn = (ImageButton)findViewById(R.id.add_new_post_btn);

        drawerLayout = (DrawerLayout)findViewById(R.id.main_drawer);
        actionBarDrawerToggle = new ActionBarDrawerToggle(MainActivity.this, drawerLayout, R.string.drawer_open,R.string.drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        navigationView=(NavigationView)findViewById(R.id.nav_view);
        View navView = navigationView.inflateHeaderView(R.layout.nav_header);
        nav_headerpp = (CircleImageView)navView.findViewById(R.id.header_pp);
        nav_profilename = (TextView) navView.findViewById(R.id.nav_username);
        nav_status = (TextView) navView.findViewById(R.id.nav_status);

        postlist = (RecyclerView)findViewById(R.id.news_user_posts);
        postlist.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        postlist.setLayoutManager(linearLayoutManager);

        uRef.child(getCurrentUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    if(dataSnapshot.hasChild("fullname"))
                    {
                        String fullname = dataSnapshot.child("fullname").getValue().toString();
                        String status = dataSnapshot.child("status").getValue().toString();
                        nav_profilename.setText(fullname);
                        nav_status.setText(status);
                    }
                    if(dataSnapshot.hasChild("profileimage"))
                    {
                        String image = dataSnapshot.child("profileimage").getValue().toString();
                        Picasso.get().load(image).placeholder(R.drawable.profile).into(nav_headerpp);
                    }
                    else
                    {
                        SendUserToSetupActivity();
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        //navMenu Selector
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener()
        {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item)
            {
                UserMenuSelector (item);
                return false;
            }
        });
        //Listen for Post Activity Button
        addNewPostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SendUserToPostActivity();
            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();
        Query SortPostDescending = postRef.orderByChild("counter");

        FirebaseRecyclerOptions<Posts> options =
                new FirebaseRecyclerOptions.Builder<Posts>()
                        .setQuery(SortPostDescending,Posts.class)
                        .build();
        FirebaseRecyclerAdapter<Posts,PostsViewHolder>firebaseRecyclerAdapter =
            new FirebaseRecyclerAdapter<Posts, PostsViewHolder>(options) {
                @Override
                protected void onBindViewHolder(@NonNull PostsViewHolder holder, int position, @NonNull Posts model)
                {
                    final String PostKey = getRef(position).getKey();
                    holder.userName.setText(model.getuFullName());
                    holder.date.setText(model.getDate()+"  ");
                    holder.time.setText(model.getTime()+"  ");
                    holder.description.setText(model.getDescription());
                    Picasso.get().load(model.getPostimage()).into(holder.postimage);
                    Picasso.get().load(model.getProfileimage()).into(holder.profileimage);
                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent editPostIntent = new Intent(MainActivity.this,PostDetailsonClick.class);
                            editPostIntent.putExtra("PostKey",PostKey);
                            startActivity(editPostIntent);

                        }
                    });

                    holder.CommentBttn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent commentIntent = new Intent(MainActivity.this,CommentsActivity.class);
                            commentIntent.putExtra("PostKey",PostKey);
                            startActivity(commentIntent);
                        }
                    });

                    holder.FavPostBttn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            favStatus = true;
                            favRef.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if(favStatus.equals(true))
                                    {
                                        if(snapshot.child(PostKey).hasChild(getCurrentUid))
                                        {
                                            favRef.child(PostKey).child(getCurrentUid).removeValue();
                                            favStatus = false;
                                        }
                                        else {
                                            favRef.child(PostKey).child(getCurrentUid).setValue(true);
                                            favStatus = false;
                                        }

                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

                        }
                    });
                    holder.setFavButtonStatus(PostKey);
                }

                @NonNull
                @Override
                public PostsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                    View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.all_posts_layout,viewGroup,false);
                    PostsViewHolder viewHolder = new PostsViewHolder(view);
                    return viewHolder;
                }
            };
        postlist.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();



        //Checking if user logged in

        FirebaseUser currentUser = mFirebaseAuth.getCurrentUser();

        if(currentUser==null)
        {
            SendUserToLoginActivity();
        }
        else
        {
            CheckUserExist();
        }
    }

    public static class PostsViewHolder extends RecyclerView.ViewHolder
    {
        TextView userName, description, time, date, countFavstext;
        ImageView postimage;
        CircleImageView profileimage;
        ImageButton FavPostBttn,LikePostBttn,CommentBttn;
        int countFavs;
        String currentUid;
        DatabaseReference favsRef;

        public PostsViewHolder(@NonNull View itemView) {
            super(itemView);;
            favsRef =FirebaseDatabase.getInstance().getReference().child("Favourites");
            currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            userName = itemView.findViewById(R.id.post_user_name);
            description = itemView.findViewById(R.id.post_desc);
            date = itemView.findViewById(R.id.post_date);
            time = itemView.findViewById(R.id.post_time);
            postimage = itemView.findViewById(R.id.post_image);
            profileimage = itemView.findViewById(R.id.post_profile_image);
            FavPostBttn = itemView.findViewById(R.id.fav_button);
            countFavstext = itemView.findViewById(R.id.fav_count);
            //LikePostBttn = itemView.findViewById(R.id.);
            CommentBttn = itemView.findViewById(R.id.comment_bttn);
        }

        public void setFavButtonStatus (final String PostKey) {
            favsRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.child(PostKey).hasChild(currentUid))
                    {
                        countFavs = (int) snapshot.child(PostKey).getChildrenCount();
                        FavPostBttn.setImageResource(R.drawable.redhearth);
                        countFavstext.setText(Integer.toString(countFavs));
                    }
                    else
                    {
                        countFavs = (int) snapshot.child(PostKey).getChildrenCount();
                        FavPostBttn.setImageResource(R.drawable.favourite);
                        countFavstext.setText(Integer.toString(countFavs));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });


        }
    }
    //Sending For New Post Activity /nav+iconTL
    private void SendUserToPostActivity() {
        Intent postIntent = new Intent(MainActivity.this,PostActivity.class);
        startActivity(postIntent);

    }
    // Checking User For DB
    private void CheckUserExist() {
        final String currentUid =  mFirebaseAuth.getCurrentUser().getUid();
        uRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if (!dataSnapshot.hasChild(currentUid))
                {
                    SendUserToSetupActivity();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }
    // Sends user to First Login Page So He can setup his profile before first use (Basics)
    private void SendUserToSetupActivity()
    {
        Intent setupIntent = new Intent(MainActivity.this,SetupActivity.class);
        setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setupIntent);
        finish();
    }

    // Sends user to Login
    private void SendUserToLoginActivity()
    {
        Intent loginIntent = new Intent(MainActivity.this,LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }

    private void SendUserToSettingsActivity() {
        Intent settingsIntent = new Intent(MainActivity.this,SettingsActivity.class);
        settingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(settingsIntent);
        finish();
    }

    private void SendUserToProfileActivity() {
        Intent profileIntent = new Intent(MainActivity.this,ProfileActivity.class);
        profileIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(profileIntent);
        finish();
    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(MainActivity.this , MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    private void SendUserToFindFriendsActivity() {
        Intent findfriendsIntent = new Intent(MainActivity.this , FindFriendsActivity.class);
        findfriendsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(findfriendsIntent);
        finish();
    }
    private void SendUserToScanActivity() {
        Intent scanIntent = new Intent(MainActivity.this , xCodeScanActivity.class);
        scanIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(scanIntent);
        finish();
    }
    private void SendUserToFriendsActivity() {
        Intent scanIntent = new Intent(MainActivity.this , FriendsActivity.class);
        scanIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(scanIntent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        if (actionBarDrawerToggle.onOptionsItemSelected(item))
        {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
//Navigation Menu List Switches
    private void UserMenuSelector(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.nav_xcode:
                SendUserToScanActivity();
                break;
            case R.id.nav_profile:
                SendUserToProfileActivity();
                break;
            case R.id.nav_post:
                SendUserToPostActivity();
                break;
            case R.id.nav_home:
                SendUserToMainActivity();
                break;
            case R.id.nav_friends:
                SendUserToFriendsActivity();
                break;
            case R.id.nav_find_friends:
                SendUserToFindFriendsActivity();
                break;
            case R.id.nav_messages:
                Toast.makeText(this, "Mesajlarım", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_settings:
                SendUserToSettingsActivity();
                break;
            case R.id.nav_Logout:
                mFirebaseAuth.signOut();
                SendUserToLoginActivity();
                break;
        }
    }




}