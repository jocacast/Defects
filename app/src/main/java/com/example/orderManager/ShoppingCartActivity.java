package com.example.orderManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class ShoppingCartActivity extends AppCompatActivity {
    private RecyclerView mFirestoreList;
    private FirebaseFirestore fireBaseFireStore;
    private String sessionId;
    private FirestoreRecyclerAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionId = getIntent().getStringExtra("EXTRA_SESSION_ID");
        setContentView(R.layout.activity_shopping_cart);
        Toolbar myToolBar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolBar);
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        fireBaseFireStore = FirebaseFirestore.getInstance();
        mFirestoreList = findViewById(R.id.recycler_view);
        RecyclerView.ItemDecoration itemDecoration = new
                DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        mFirestoreList.addItemDecoration(itemDecoration);

        Query query = fireBaseFireStore.collection("carts").document(sessionId).collection("ProductsInCart");
        FirestoreRecyclerOptions<ShoppingCartItem> options  = new FirestoreRecyclerOptions.Builder<ShoppingCartItem>()
                .setQuery(query, ShoppingCartItem.class)
                .build();
        //Adapter
        adapter = new FirestoreRecyclerAdapter<ShoppingCartItem, ProductsInCartViewHolder>(options) {

            @NonNull
            @Override
            public ProductsInCartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_single, parent, false);
                return new ProductsInCartViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull ProductsInCartViewHolder holder, int position, @NonNull ShoppingCartItem model) {
                holder.list_name.setText(model.getProductName());
                holder.list_qty.setText(model.getQuantity() + "");
            }
        };
        mFirestoreList.setHasFixedSize(true);
        mFirestoreList.setLayoutManager(new LinearLayoutManager(this));
        mFirestoreList.setAdapter(adapter);
    }


    private class ProductsInCartViewHolder extends RecyclerView.ViewHolder{
        private TextView list_name;
        private TextView list_qty;

        public ProductsInCartViewHolder(@NonNull View itemView) {
            super(itemView);
            list_name = itemView.findViewById(R.id.list_name);
            list_qty = itemView.findViewById(R.id.list_qty);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    Toast.makeText(ShoppingCartActivity.this, "Item clicked", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}