package com.example.xcode;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.media.Image;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentsActivity extends AppCompatActivity {

    private RecyclerView commentList;
    private ImageButton postCommentBtn;
    private EditText commentBox;
    private String PostKey, currentUID;
    private DatabaseReference uRef,postRef;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        PostKey =getIntent().getExtras().get("PostKey").toString();
        mAuth = FirebaseAuth.getInstance();
        currentUID = mAuth.getCurrentUser().getUid();
        uRef = FirebaseDatabase.getInstance().getReference().child("Users");
        postRef=FirebaseDatabase.getInstance().getReference().child("Posts").child(PostKey).child("Comments");

        commentList = (RecyclerView)findViewById(R.id.comments_recyclerView);
        commentList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        commentList.setLayoutManager(linearLayoutManager);

        commentBox = (EditText)findViewById(R.id.comments_commentBox);
        postCommentBtn = (ImageButton) findViewById(R.id.comments_bttn);

        postCommentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uRef.child(currentUID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists())
                        {
                            String fullname = snapshot.child("fullname").getValue().toString();
                            String username = snapshot.child("username").getValue().toString();
                            String pp = snapshot.child("profileimage").getValue().toString();
                            ValidateComment(fullname,username,pp);
                            commentBox.setText("");
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }
        });
    }

    private void ValidateComment(String fullname , String username, String pp) {
        String commentText = commentBox.getText().toString();
        if(TextUtils.isEmpty(commentText))
        {
            Toast.makeText(this,"Yorum Yapmak İçin Birşeyler Yazın",Toast.LENGTH_SHORT).show();

        }else
        {
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat formatdate = new SimpleDateFormat("dd MMM yyyy");
            final String postdate = formatdate.format(calendar.getTime());

            SimpleDateFormat formattime = new SimpleDateFormat("HH:mm");
            final String posttime = formattime.format(calendar.getTime());

            final String rKey = currentUID + postdate + posttime;

            HashMap commentsMap = new HashMap();
            commentsMap.put("uid",currentUID);
            commentsMap.put("comment",commentText);
            commentsMap.put("date",postdate);
            commentsMap.put("time",posttime);
            commentsMap.put("username",username);
            commentsMap.put("fullname",fullname);
            commentsMap.put("profileimage",pp);

            postRef.child(rKey).updateChildren(commentsMap)
                    .addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if(task.isSuccessful())
                            {
                                Toast.makeText(CommentsActivity.this, "Yorum Başarıyla Gönderildi", Toast.LENGTH_SHORT).show();
                            }else
                            {
                                Toast.makeText(CommentsActivity.this, "Yorum Başarısız, Tekrar Deneyin", Toast.LENGTH_SHORT).show();
                            }

                        }
                    });


        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<Comments> options =
                new FirebaseRecyclerOptions.Builder<Comments>()
                        .setQuery(postRef,Comments.class)
                        .build();
        FirebaseRecyclerAdapter<Comments,CommentsViewHolder>firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Comments, CommentsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull CommentsViewHolder holder, int position, @NonNull Comments model) {
                holder.fullname.setText(model.getFullname());
                holder.userName.setText(model.getUsername());
                holder.date.setText(model.getDate());
                holder.time.setText(model.getTime());
                holder.comment.setText(model.getComment());
                Picasso.get().load(model.getProfileimage()).into(holder.profileimage);

            }

            @NonNull
            @Override
            public CommentsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_comments_layout,parent,false);
                CommentsActivity.CommentsViewHolder viewHolder = new CommentsActivity.CommentsViewHolder(view);
                return viewHolder;
            }
        };

        commentList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    public class CommentsViewHolder extends RecyclerView.ViewHolder
    {
        TextView userName, fullname, comment, time, date;
        CircleImageView profileimage;
        View mView;
        public CommentsViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
            fullname= itemView.findViewById(R.id.allcomments_fullname);
            userName= itemView.findViewById(R.id.allcomments_username);
            date = itemView.findViewById(R.id.allcomments_date);
            time = itemView.findViewById(R.id.allcomments_time);
            comment = itemView.findViewById(R.id.allcomments_comment);
            profileimage = itemView.findViewById(R.id.allcomments_pp);
        }
    }
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
        return super.dispatchTouchEvent(ev);
    }
}