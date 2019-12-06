package com.example.hello1;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.hello1.Common.Common;
import com.example.hello1.Model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import io.paperdb.Paper;

public class MainActivity extends AppCompatActivity {

    Button btnSignIn,btnSignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnSignIn = (Button)findViewById(R.id.btnSignIn);
        btnSignUp = (Button)findViewById(R.id.btnSignUp);

        //Init Paper
        Paper.init(this);

        btnSignIn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent signIn = new Intent(MainActivity.this,SignIn.class);
                startActivity(signIn);
            }
        });

        btnSignUp.setOnClickListener(new View.OnClickListener(){
           @Override
           public void onClick(View view){
               Intent signUp = new Intent(MainActivity.this,SignUp.class);
               startActivity(signUp);
           }
        });

        //Check Remember
        String user = Paper.book().read(Common.USER_KEY);
        String pwd = Paper.book().read(Common.PWD_KEY);
        if(user != null && pwd != null){
            if(!user.isEmpty() && !pwd.isEmpty())
                login(user,pwd);
        }

    }

    private void login(final String phone, final String pwd) {

        if (Common.isConnectedToInterner(getBaseContext())) {

            //Init Firebase
            final FirebaseDatabase database = FirebaseDatabase.getInstance();
            final DatabaseReference table_user = database.getReference("User");


            final ProgressDialog mDialog = new ProgressDialog(MainActivity.this);
            mDialog.setMessage("Please waiting......");
            mDialog.show();

            table_user.addValueEventListener(new ValueEventListener() {


                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    //Check whether user is exists in database or not
                    if (dataSnapshot.child(phone).exists()) {

                        //Get User Information
                        mDialog.dismiss();
                        User user = dataSnapshot.child(phone).getValue(User.class);
                        user.setPhone(phone); //Set Phone
                        if (user.getPassword().equals(pwd)) {
                            Intent homeIntent = new Intent(MainActivity.this, Home.class);
                            Common.currentUser = user;
                            startActivity(homeIntent);
                            finish();
                        } else {
                            Toast.makeText(MainActivity.this, "Wrong Password", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        mDialog.dismiss();
                        Toast.makeText(MainActivity.this, "User is not available!", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
        else
        {
            Toast.makeText(MainActivity.this,"Please check your connection!!!",Toast.LENGTH_SHORT).show();
            return;
        }
    }
}
