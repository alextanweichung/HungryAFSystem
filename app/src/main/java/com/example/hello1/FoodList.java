package com.example.hello1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.hello1.Common.Common;
import com.example.hello1.Database.Database;
import com.example.hello1.Interface.ItemClickListener;
import com.example.hello1.Model.Category;
import com.example.hello1.Model.Food;
import com.example.hello1.ViewHolder.FoodViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class FoodList extends AppCompatActivity {

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference foodList;

    String categoryId="";

    FirebaseRecyclerAdapter<Food, FoodViewHolder> adapter;


    //Search Functionality
    FirebaseRecyclerAdapter<Food,FoodViewHolder> searchAdapter;
    List<String> suggestList = new ArrayList<>();
    MaterialSearchBar materialSearchBar;

    //Favourites
    Database localDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_list);

        //Firebase
        database = FirebaseDatabase.getInstance();
        foodList = database.getReference("Foods");

        //Local DB
        localDB = new Database(this);

        recyclerView = (RecyclerView)findViewById(R.id.recycler_food);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        //Get Intent here from Home
        if(getIntent()!=null)
            categoryId = getIntent().getStringExtra("CategoryId");
        if(!categoryId.isEmpty()&&categoryId!=null)
        {
            if(Common.isConnectedToInterner(getBaseContext()))
                loadListFood(categoryId);
            else {
                Toast.makeText(FoodList.this,"Please check your connection!!!",Toast.LENGTH_SHORT).show();
                return;
            }
        }
        
        //Search
        materialSearchBar = (MaterialSearchBar)findViewById(R.id.searchBar);
        materialSearchBar.setHint("Enter your food");
        //materialSearchBar.setSpeechMode(false); No need, bcoz we dy define it at XML
        loadSuggest();

        materialSearchBar.setLastSuggestions(suggestList);
        materialSearchBar.setCardViewElevation(10);
        materialSearchBar.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                //When user type their text, we will change suggest list
                List<String> suggest = new ArrayList<>();
                for(String search:suggestList)
                {
                    if(search.toLowerCase().contains(materialSearchBar.getText().toLowerCase()))suggest.add(search);
                }
                materialSearchBar.setLastSuggestions(suggest);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        materialSearchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {
                //When Search Bar is close
                //Restore original adapter
                if(!enabled)
                    recyclerView.setAdapter(adapter);
            }

            @Override
            public void onSearchConfirmed(CharSequence text) {

                //When search finish
                //Show result of search adapter
                startSearch(text);
            }

            @Override
            public void onButtonClicked(int buttonCode) {

            }
        });

    }

    private void startSearch(CharSequence text) {

        //Create Query by name
        Query searchByName = foodList.orderByChild("name").equalTo(text.toString());
        //Create Options with query
        FirebaseRecyclerOptions<Food> foodOptions = new FirebaseRecyclerOptions.Builder<Food>().setQuery(searchByName,Food.class).build();

        searchAdapter = new FirebaseRecyclerAdapter<Food, FoodViewHolder>(foodOptions) {
            @Override
            protected void onBindViewHolder(@NonNull FoodViewHolder viewHolder, int i, @NonNull Food model) {
                viewHolder.food_name.setText(model.getName());
                Picasso.with(getBaseContext()).load(model.getImage()).into(viewHolder.food_image);

                final Food local = model;
                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        //Start new Activity
                        Intent foodDetail = new Intent(FoodList.this,FoodDetail.class);
                        foodDetail.putExtra("FoodId",searchAdapter.getRef(position).getKey());
                        startActivity(foodDetail);

                    }
                });
            }

            @NonNull
            @Override
            public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.food_item,parent,false);
                return new FoodViewHolder(itemView);
            }
        };
        searchAdapter.startListening();
        recyclerView.setAdapter(searchAdapter);// Set adapter for Recycler View is Search Result
    }



    private void loadSuggest() {
        foodList.orderByChild("menuId").equalTo(categoryId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot postSnapshot:dataSnapshot.getChildren())
                {
                    Food item = postSnapshot.getValue(Food.class);
                    suggestList.add(item.getName());//Add name of food to suggest list
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void loadListFood(String categoryId) {
        FirebaseRecyclerOptions<Food> options2 = new FirebaseRecyclerOptions.Builder<Food>().setQuery(foodList.orderByChild("menuId").equalTo(categoryId),Food.class).build();
        adapter = new FirebaseRecyclerAdapter<Food, FoodViewHolder>(options2) {
            @Override
            protected void onBindViewHolder(@NonNull final FoodViewHolder viewHolder, final int i, @NonNull final Food model) {
                viewHolder.food_name.setText(model.getName());
                Picasso.with(getBaseContext()).load(model.getImage()).into(viewHolder.food_image);

                //Add Favourites
                if(localDB.isFavourite(adapter.getRef(i).getKey()))
                    viewHolder.fav_image.setImageResource(R.drawable.ic_favorite_black_24dp);

                //Click to change state of Favourites
                viewHolder.fav_image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(!localDB.isFavourite(adapter.getRef(i).getKey()))
                        {
                            localDB.addToFavourites(adapter.getRef(i).getKey());
                            viewHolder.fav_image.setImageResource(R.drawable.ic_favorite_black_24dp);
                            Toast.makeText(FoodList.this,""+model.getName()+" was added to Favorites",Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            localDB.removeFromFavourites(adapter.getRef(i).getKey());
                            viewHolder.fav_image.setImageResource(R.drawable.ic_favorite_border_black_24dp);
                            Toast.makeText(FoodList.this,""+model.getName()+" was removed from Favorites",Toast.LENGTH_SHORT).show();
                        }

                    }
                });

                final Food local = model;
                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        //Start new Activity
                        Intent foodDetail = new Intent(FoodList.this,FoodDetail.class);
                        foodDetail.putExtra("FoodId",adapter.getRef(position).getKey());
                        startActivity(foodDetail);

                    }
                });
            }

            @Override
            public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.food_item,parent,false);
                return new FoodViewHolder(itemView);
            }

        };
        //Set Adapter
        adapter.startListening();
        recyclerView.setAdapter(adapter);
    }


    @Override
    protected void onResume(){
        super.onResume();
        adapter.startListening();

    }

    @Override
    protected void onStop(){
        super.onStop();
        adapter.stopListening();
        
    }
}
