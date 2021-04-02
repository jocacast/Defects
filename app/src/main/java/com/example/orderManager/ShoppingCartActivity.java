package com.example.orderManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.MenuItem;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class ShoppingCartActivity extends AppCompatActivity {
    private RecyclerView mFirestoreList;
    private FirebaseFirestore fireBaseFireStore;
    private String sessionId;
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
        mFirestoreList = findViewById(R.id.list_view);
        Query query = fireBaseFireStore.collection("carts").document(sessionId).collection("7506227338993");

    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}