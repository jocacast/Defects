package com.example.orderManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import java.util.HashMap;
import java.util.Map;

public class ScannerActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView productTitle;
    private Button addToCartButton, searchButton;
    private ProgressBar progressBar;
    private LinearLayout qtyLinearLayout;
    private FirebaseFirestore db;
    private String sessionId;
    private String productId;
    private String productName;
    private NumberPicker productQty;
    private int intProductQty;
    private EditText searchInput;

    @Override
    protected void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        sessionId = getIntent().getStringExtra("EXTRA_SESSION_ID");
        setContentView(R.layout.activity_scanner);

        Button scanButton = findViewById(R.id.scan_button);
        searchInput = findViewById(R.id.search_edit_view);
        addToCartButton = findViewById(R.id.add_to_cart_button);
        productTitle = findViewById(R.id.product_title);
        progressBar = findViewById(R.id.progress_bar);
        qtyLinearLayout = findViewById(R.id.qty_layout);
        searchButton = (Button)findViewById(R.id.search_button);

        Toolbar myToolBar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolBar);
        productQty = findViewById(R.id.qty);
        productQty.setMaxValue(50);
        productQty.setMinValue(1);

        scanButton.setOnClickListener(this);
        addToCartButton.setOnClickListener(this);
        searchButton.setOnClickListener(this);
        db = FirebaseFirestore.getInstance();
        searchInput.addTextChangedListener(searchTextWatcher);
        verifyCartExistence();

    }

    private void verifyCartExistence() {
        db.collection("carts").document(sessionId).collection("ProductsInCart").whereEqualTo("productId", "-1").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                progressBar.setVisibility(View.INVISIBLE);
                int cont = 0;
                Map<String, Object> resultDocument = null;
                String documentId = null;
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        cont++;
                    }
                    if(cont>=1){
                        Log.d("Firebase", "Cart for customer " + sessionId + " already exist");
                        /*Map<String, Object> data = new HashMap<>();
                        data.put("productId", "-1");
                        data.put("productName", "Dummy");
                        data.put("quantity", 0);
                        for(int i = 0; i<20; i++){
                            db.collection("carts").document(sessionId).collection("ProductsInCart").add(data);
                        }*/
                    }else{
                        Log.d("Firebase", "Cart for customer " + sessionId + " does not exist create");
                        Map<String, Object> data = new HashMap<>();
                        data.put("productId", "-1");
                        data.put("productName", "Dummy");
                        data.put("quantity", 0);
                        db.collection("carts").document(sessionId).collection("ProductsInCart").add(data);
                    }
                }else{
                    Log.d("Firebase", "get failed with ", task.getException());
                    Toast.makeText(ScannerActivity.this, "Error while executing get. Contact IT or try again.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.shopping_cart:
                openShoppingCartUI();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void openShoppingCartUI() {
        String accountEmail = sessionId;
        Intent intent = new Intent(getBaseContext(), ShoppingCartActivityV2.class);
        intent.putExtra("EXTRA_SESSION_ID", accountEmail);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public void onClick(View v){
        int viewId = v.getId();
        if(viewId == R.id.scan_button){
            scanCode();
        }else if(viewId == R.id.add_to_cart_button){
            addToCartV2();
        }else if(viewId == R.id.search_button){
            searchByInput();
        }

    }

    private TextWatcher searchTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            String productId = searchInput.getText().toString().trim();
            searchButton.setEnabled(!productId.isEmpty());
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };


    private void scanCode(){
        qtyLinearLayout.setVisibility(View.INVISIBLE);
        IntentIntegrator integrator = new IntentIntegrator(this );
        integrator.setCaptureActivity(CaptureAct.class);
        integrator.setOrientationLocked(true);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        integrator.setPrompt("Scanning");
        integrator.initiateScan();
        String noProductFoundTitle = "";
        productTitle.setText(noProductFoundTitle);
        productTitle.setVisibility(View.VISIBLE);
        searchInput.getText().clear();
    }

    private void addToCartV2(){
        //Validate that there is info provided on qty field
        intProductQty = productQty.getValue();
        //Look for the same product on the cart
        db.collection("carts").document(sessionId).collection("ProductsInCart").whereEqualTo("productId", productId).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                progressBar.setVisibility(View.INVISIBLE);
                int cont = 0;
                Map<String, Object> resultDocument = null;
                String documentId = null;
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        cont++;
                        resultDocument = document.getData();
                        documentId = document.getId();
                    }
                    if(cont>=1){
                        increaseQty(documentId);
                    }else{
                        addToFirebaseCartV2(documentId);
                    }
                }else{
                    Log.d("Firebase", "get failed with ", task.getException());
                    Toast.makeText(ScannerActivity.this, "Error while executing get. Contact IT or try again.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void increaseQty(String documentId) {
        Toast.makeText(ScannerActivity.this, "Increasing product quantity...", Toast.LENGTH_SHORT).show();
        DocumentReference documentReference = db.collection("carts").document(sessionId).collection("ProductsInCart").document(documentId);
        documentReference.update("quantity", FieldValue.increment(intProductQty)).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(ScannerActivity.this, "Product quantity increased", Toast.LENGTH_SHORT).show();
                decreaseQtyAfterToast();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("Firebase", "Error updating document", e);
                Toast.makeText(ScannerActivity.this, "Error while updating quantity. Contact IT or try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void searchByInput(){
        String scannedResult = searchInput.getText().toString();
        showLoading();
        getProductInfoAndRepaint(db, scannedResult );
    }

    private void addToFirebaseCartV2(String documentId){
        Toast.makeText(ScannerActivity.this, "Product not found on cart, adding to Cart...", Toast.LENGTH_SHORT).show();
        Map<String, Object> data1 = new HashMap<>();
        data1.put("quantity", intProductQty);
        data1.put("productName", productName);
        data1.put("productId", productId);
        db.collection("carts").document(sessionId)
                .collection("ProductsInCart")
                .add(data1)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText(ScannerActivity.this, "Product added to cart", Toast.LENGTH_SHORT).show();
                        decreaseQtyAfterToast();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("Firebase", "Error writing document", e);
                Toast.makeText(ScannerActivity.this, "Error while adding to cart.  Contact IT or try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        //showLoading();
        if(result != null){
            if(result.getContents()!= null){
                String scannedBarCode = result.getContents();
                searchInput.setText(scannedBarCode);
                //getProductInfoAndRepaint(db, scannedBarCode);
            }else{
                String noProductFoundTitle = "No results found";
                productTitle.setText(noProductFoundTitle);
                addToCartButton.setEnabled(false);
                progressBar.setVisibility(View.INVISIBLE);
                qtyLinearLayout.setVisibility(View.INVISIBLE);
                productTitle.setVisibility(View.VISIBLE);
            }
        }else{
            super.onActivityResult(requestCode, resultCode, data);
        }

    }

    private void getProductInfoAndRepaint(FirebaseFirestore db, String scannedResult){
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
                        qtyLinearLayout.setVisibility(View.VISIBLE);
                        productTitle.setText(productName);
                    } else {
                        String noProductFoundTitle = "No Product with id: " + scannedResult+ " found";
                        productTitle.setText(noProductFoundTitle);
                        addToCartButton.setEnabled(false);
                        progressBar.setVisibility(View.INVISIBLE);
                        qtyLinearLayout.setVisibility(View.INVISIBLE);
                    }
                    productTitle.setVisibility(View.VISIBLE);

                } else {
                    Log.d("Firebase", "get failed with ", task.getException());
                    Toast.makeText(ScannerActivity.this, "Error while executing get. Contact IT or try again.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void decreaseQtyAfterToast(){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                productQty.setValue(1);
            }
        }, 2000);

    }
    private void showLoading(){
        progressBar.setVisibility(View.VISIBLE);
        productTitle.setVisibility(View.INVISIBLE);
    }

}