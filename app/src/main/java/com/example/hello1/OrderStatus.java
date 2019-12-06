package com.example.hello1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.hello1.Common.Common;
import com.example.hello1.Model.Food;
import com.example.hello1.Model.Order;
import com.example.hello1.Model.Request;
import com.example.hello1.ViewHolder.FoodViewHolder;
import com.example.hello1.ViewHolder.OrderViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class OrderStatus extends AppCompatActivity {

    public RecyclerView recyclerView;
    public RecyclerView.LayoutManager layoutManager;

    FirebaseRecyclerAdapter<Request, OrderViewHolder> adapter22;

    FirebaseDatabase database;
    DatabaseReference requests;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_status);

        //Firebase
        database = FirebaseDatabase.getInstance();
        requests = database.getReference("Requests");
        
        recyclerView = (RecyclerView)findViewById(R.id.listOrders);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        if(getIntent().getExtras()==null)
            loadOrders(Common.currentUser.getPhone());
        else
            loadOrders(getIntent().getStringExtra("userPhone"));
    }

    private void loadOrders(String phone) {
        FirebaseRecyclerOptions<Request> options3 = new FirebaseRecyclerOptions.Builder<Request>().setQuery(requests.orderByChild("phone").equalTo(phone),Request.class).build();
        adapter22 = new FirebaseRecyclerAdapter<Request, OrderViewHolder>(options3) {
            @NonNull
            @Override
            protected void onBindViewHolder(@NonNull OrderViewHolder viewHolder, int i, @NonNull Request model) {
                viewHolder.txtOrderId.setText(adapter22.getRef(i).getKey());
                viewHolder.txtOrderStatus.setText(Common.convertCodeToStatus(model.getStatus()));
                viewHolder.txtOrderAddress.setText(model.getAddress());
                viewHolder.txtOrderPhone.setText(model.getPhone());
            }

            @Override
            public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.order_layout,parent,false);
                return new OrderViewHolder(itemView);
            }
        };
        adapter22.startListening();
        recyclerView.setAdapter(adapter22);
    }

    @Override
    protected void onResume(){
        super.onResume();
        adapter22.startListening();
    }

    @Override
    protected  void onStop(){
        super.onStop();
        adapter22.stopListening();
    }


}
