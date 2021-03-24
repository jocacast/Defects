package com.example.orderManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class ScannerActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView productTitle;
    @Override
    protected void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        String sessionId = getIntent().getStringExtra("EXTRA_SESSION_ID");
        setContentView(R.layout.activity_scanner);

        Button scanButton = findViewById(R.id.scan_button);
        Button addToCartButton = findViewById(R.id.add_to_cart_button);
        TextView loginInfo = findViewById(R.id.log_in_info);
        productTitle = findViewById(R.id.product_title);

        scanButton.setOnClickListener(this);
        addToCartButton.setOnClickListener(this);
        String loginResult = "Logged in as " + sessionId;

        loginInfo.setText(loginResult);
    }

    @Override
    public void onClick(View v){
        int viewId = v.getId();
        if(viewId == R.id.scan_button){
            scanCode();
        }else if(viewId == R.id.add_to_cart_button){
            addToCart();
        }

    }

    private void scanCode(){
        IntentIntegrator integrator = new IntentIntegrator(this );
        integrator.setCaptureActivity(CaptureAct.class);
        integrator.setOrientationLocked(true);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        integrator.setPrompt("Scanning");
        integrator.initiateScan();
    }

    private void addToCart(){
        Toast.makeText(ScannerActivity.this, "Adding to Cart...", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null){
            if(result.getContents()!= null){
                Toast.makeText(ScannerActivity.this, "Result " + result.getContents(), Toast.LENGTH_SHORT).show();
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                DocumentReference docRef = db.collection("Products").document(result.getContents());
                docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                Log.d("Firebase", "DocumentSnapshot data: " + document.getData());
                                String productName = result.getContents() +": "+ (String)document.getData().get("name");
                                productTitle.setText(productName);
                            } else {
                                String noProductFoundTitle = "No Product with id: " + result.getContents()+ " found";
                                productTitle.setText(noProductFoundTitle);
                            }
                        } else {
                            Log.d("Firebase", "get failed with ", task.getException());
                        }
                    }
                });
            }else{
                Toast.makeText(ScannerActivity.this, "No results ", Toast.LENGTH_SHORT).show();
            }

        }else{
            super.onActivityResult(requestCode, resultCode, data);
        }

    }

    private Product getProductFromFireStore(String documentId){
        final Product[] product = new Product[1];
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("Products").document(documentId);
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                product[0] = documentSnapshot.toObject(Product.class);
            }
        });
        return product[0];
    }


}