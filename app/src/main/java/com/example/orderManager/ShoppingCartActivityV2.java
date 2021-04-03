package com.example.orderManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class ShoppingCartActivityV2 extends AppCompatActivity{
    private String sessionId;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ShoppingCartItemAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sessionId = getIntent().getStringExtra("EXTRA_SESSION_ID");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_cart_v2);
        Toolbar myToolBar = findViewById(R.id.toolbar);
        setSupportActionBar(myToolBar);
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        setUpRecyclerView();
    }
    private void setUpRecyclerView(){
        Query query = db.collection("carts").document(sessionId).collection("ProductsInCart").whereNotEqualTo("productId", "-1");
        FirestoreRecyclerOptions<ShoppingCartItem> options  = new FirestoreRecyclerOptions.Builder<ShoppingCartItem>()
                .setQuery(query, ShoppingCartItem.class)
                .build();
        adapter = new ShoppingCartItemAdapter(options);
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                adapter.deleteItem(viewHolder.getAdapterPosition());
            }
        }).attachToRecyclerView(recyclerView);

        adapter.setOnItemClickListener(new ShoppingCartItemAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(DocumentSnapshot documentSnapshot, int position) {
                ShoppingCartItem shoppingCartItem = documentSnapshot.toObject(ShoppingCartItem.class);
                String id = documentSnapshot.getId();
                Log.d("Firebase", "Clicked item document id " + id );
                Toast.makeText(ShoppingCartActivityV2.this, "Position: " + position + " Name " + shoppingCartItem.getProductName(), Toast.LENGTH_SHORT).show();
            }
        });
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

}