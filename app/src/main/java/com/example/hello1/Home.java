package com.example.hello1;

import android.content.Intent;
import android.os.Bundle;

import com.example.hello1.Common.Common;
import com.example.hello1.Interface.ItemClickListener;
import com.example.hello1.Model.Category;
import com.example.hello1.Model.Token;
import com.example.hello1.ViewHolder.MenuViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.view.GravityCompat;
import androidx.navigation.ui.AppBarConfiguration;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.squareup.picasso.Picasso;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.Menu;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import io.paperdb.Paper;

public class Home extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private AppBarConfiguration mAppBarConfiguration;

    FirebaseDatabase database;
    DatabaseReference category;
    FirebaseRecyclerAdapter<Category, MenuViewHolder> adapter;

    TextView txtFullName;

    RecyclerView recycler_menu;
    RecyclerView.LayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);


        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Menu");
        setSupportActionBar(toolbar);

        //Init Firebase
        database = FirebaseDatabase.getInstance();
        category = database.getReference("Category");

        Paper.init(this);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent cartIntent = new Intent(Home.this,Cart.class);
                startActivity(cartIntent);
            }
        });
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,drawer,toolbar,R.string.navigation_drawer_open,R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView)findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);




        //Set Name for User
        View headerView;
        headerView = navigationView.getHeaderView(0);
        txtFullName = (TextView)headerView.findViewById(R.id.txtFullName);
        txtFullName.setText(Common.currentUser.getName());

        //Load Menu
        recycler_menu = (RecyclerView)findViewById(R.id.recycler_menu);
        recycler_menu.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recycler_menu.setLayoutManager(layoutManager);


        if(Common.isConnectedToInterner(this))
            loadMenu();
        else {
            Toast.makeText(this,"Please check your connection!!!",Toast.LENGTH_SHORT).show();
            return;
        }

        updateToken(FirebaseInstanceId.getInstance().getToken());


    }

    private void updateToken(String token) {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference tokens = db.getReference("Tokens");
        Token data = new Token(token,false);//False is becoz this token send from Client App
        tokens.child(Common.currentUser.getPhone()).setValue(data);
    }

    private void loadMenu()  {

        FirebaseRecyclerOptions<Category> options = new FirebaseRecyclerOptions.Builder<Category>().setQuery(category,Category.class).build();
        adapter = new FirebaseRecyclerAdapter<Category, MenuViewHolder>(options) {
            @NonNull
            @Override
            public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.menu_item, parent, false);
                return new MenuViewHolder(itemView);
            }

            @Override
            protected void onBindViewHolder(@NonNull MenuViewHolder menuViewHolder, int i, @NonNull Category model) {
                menuViewHolder.txtMenuName.setText(model.getName());
                Picasso.with(getBaseContext()).load(model.getImage()).into(menuViewHolder.imageView);
                final Category clickItem = model;
                menuViewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        //Get CategoryID and send to new Activity
                        Intent foodList = new Intent(Home.this,FoodList.class);
                        //Because CategoryId is key, so we just get key of this item can dy
                        foodList.putExtra("CategoryId",adapter.getRef(position).getKey());
                        startActivity(foodList);
                    }
                });
            }

        };
        adapter.startListening();
        recycler_menu.setAdapter(adapter);
    }


    @Override
    protected void onResume(){
        super.onResume();
        adapter.startListening();
    }

    @Override
    protected  void onStop(){
        super.onStop();
        adapter.stopListening();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        if(item.getItemId() == R.id.refresh)
            loadMenu();
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    public boolean onNavigationItemSelected(MenuItem item){

        int id = item.getItemId();
        if(id==R.id.nav_menu){
            Intent menuIntent = new Intent(Home.this,Home.class);
            startActivity(menuIntent);
        }
        else if(id==R.id.nav_cart){
            Intent cartIntent = new Intent(Home.this,Cart.class);
            startActivity(cartIntent);
        }
        else if (id == R.id.nav_orders) {
            Intent orderIntent = new Intent(Home.this,OrderStatus.class);
            startActivity(orderIntent);
        }
        else if (id == R.id.nav_log_out) {

            //Delete Remember user & password
            Paper.book().destroy();


            //Logout
            Intent signIn = new Intent(Home.this,SignIn.class);
            signIn.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(signIn);

        }

        DrawerLayout drawer = (DrawerLayout)findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}
