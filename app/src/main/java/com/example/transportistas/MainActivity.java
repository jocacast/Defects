package com.example.transportistas;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String sessionId = getIntent().getStringExtra("EXTRA_SESSION_ID");
        setContentView(R.layout.activity_main);
        Button submitButton = findViewById(R.id.submit_button);
        TextView driverName = findViewById(R.id.driverName);
        TextView productName = findViewById(R.id.productName);
        TextView description = findViewById(R.id.description);
        TextView loginInfo = findViewById(R.id.log_in_info);
        loginInfo.setText("Logged in as " + sessionId);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isConnectedToInternet = checkInternetConnection();
                Map<String, Object> defect = createDefectHashMap(driverName.getText().toString(),productName.getText().toString(),description.getText().toString());
                if(isConnectedToInternet){
                    Toast.makeText(MainActivity.this, "App connected to the internet, saving on cloud", Toast.LENGTH_SHORT).show();
                    saveInCloud(defect);
                }else{
                    Toast.makeText(MainActivity.this, "No internet connection, saving on local file", Toast.LENGTH_SHORT).show();
                    try{
                        saveOnInternalStorageDevice(defect);
                    }catch(Exception ex){
                        Log.d("Exceptions", "Exception caught while saving locally " + ex.getMessage());
                    }
                }
            }
        });
    }

    public Map<String,Object> createDefectHashMap(String driverName, String productName, String description){
        Map<String, Object> defectResult = new HashMap<>();
        defectResult.put("Driver", driverName);
        defectResult.put("Product", productName);
        defectResult.put("Description", description);
        return defectResult;

    }

    public boolean checkInternetConnection(){
        Context context = MainActivity.this;
        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        return isConnected;

    }

    public void saveInCloud(Map<String, Object> defect) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("defects")
            .add(defect)
            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                @Override
                public void onSuccess(DocumentReference documentReference) {
                    Toast.makeText(MainActivity.this, "Information correctly saved into Firebase. Transaction id: " + documentReference.getId(), Toast.LENGTH_SHORT).show();
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(MainActivity.this, "Error while trying to save into Firebase. Storing locally" , Toast.LENGTH_SHORT).show();
                    try{
                        saveOnInternalStorageDevice(defect);
                    }catch(Exception ex){
                        Log.d("Exceptions", "Exception caught while saving locally " + ex.getMessage());
                    }
                }
            });
    }

    public void saveOnInternalStorageDevice(Map<String, Object> defect) throws IOException {
        String baseDir = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
        String fileName = "AnalysisData.csv";
        String filePath = baseDir + File.separator + fileName;
        File f = new File(filePath);
        Log.d("SSD" , "File Path" + filePath);
        CSVWriter writer;

        writer = new CSVWriter(new FileWriter(filePath));
        String driver = (String) defect.get("Driver");
        String product = (String) defect.get("Product");
        String description = (String) defect.get("Description");
        String defectCSV = driver +"#" + product +"#" + description + "#";

        Log.d("SSD" , "Defect CSV " + defectCSV );
        String [] data = defectCSV.split("#");
        writer.writeNext(data);

        writer.close();
    }

}