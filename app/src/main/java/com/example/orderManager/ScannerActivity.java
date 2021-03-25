package com.example.orderManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.HashMap;
import java.util.Map;

public class ScannerActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView productTitle;
    private Button addToCartButton;
    private ProgressBar progressBar;
    private FirebaseFirestore db;
    private String sessionId;
    private String productId;
    private String productName;
    @Override
    protected void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        sessionId = getIntent().getStringExtra("EXTRA_SESSION_ID");
        setContentView(R.layout.activity_scanner);

        Button scanButton = findViewById(R.id.scan_button);
        addToCartButton = findViewById(R.id.add_to_cart_button);
        TextView loginInfo = findViewById(R.id.log_in_info);
        productTitle = findViewById(R.id.product_title);
        progressBar = findViewById(R.id.progress_bar);

        scanButton.setOnClickListener(this);
        addToCartButton.setOnClickListener(this);
        String loginResult = "Logged in as " + sessionId;
        db = FirebaseFirestore.getInstance();

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
        Map<String, Object> data1 = new HashMap<>();
        data1.put("quantity", "1");
        data1.put("productName", productName);
        db.collection("carts").document(sessionId).collection(productId).document("ProductInfo").set(data1);
        db.collection("carts").document(sessionId).collection(productId).document("ProductInfo").set(data1);
        Toast.makeText(ScannerActivity.this, "Adding to Cart...", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        progressBar.setVisibility(View.VISIBLE);
        if(result != null){
            if(result.getContents()!= null){
                String scannedBarCode = result.getContents();
                setScanResults(db, scannedBarCode);
            }else{
                String noProductFoundTitle = "No results found";
                productTitle.setText(noProductFoundTitle);
                addToCartButton.setEnabled(false);
                progressBar.setVisibility(View.INVISIBLE);
            }
        }else{
            super.onActivityResult(requestCode, resultCode, data);
        }

    }

    private void setScanResults(FirebaseFirestore db,  String scannedResult){
        DocumentReference docRef = db.collection("Products").document(scannedResult);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                progressBar.setVisibility(View.INVISIBLE);
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d("Firebase", "DocumentSnapshot data: " + document.getData());
                        productId = (String)document.getData().get("productId");
                        productName = (String)document.getData().get("name");
                        addToCartButton.setEnabled(true);
                        productTitle.setText(productName);
                    } else {
                        String noProductFoundTitle = "No Product with id: " + scannedResult+ " found";
                        productTitle.setText(noProductFoundTitle);
                        addToCartButton.setEnabled(false);
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                } else {
                    Log.d("Firebase", "get failed with ", task.getException());
                }
            }
        });
    }


}