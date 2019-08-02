package com.example.foodordersupplier.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.renderscript.Sampler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.foodordersupplier.Common.Common;
import com.example.foodordersupplier.Model.Supplier;
import com.example.foodordersupplier.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;

public class MainActivity extends AppCompatActivity {
    MaterialEditText editSignInPassword,editSignInUsername;
    Button btnSignIn;
    FirebaseDatabase database;
    DatabaseReference supplierRef;
    Supplier supplier;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Init Firebase
        database = FirebaseDatabase.getInstance();
        supplierRef = database.getReference("supplier");

        editSignInPassword = findViewById(R.id.editSignInPassword);
        editSignInUsername = findViewById(R.id.editSignInUsername);

        btnSignIn = findViewById(R.id.btnSignIn);
        progressDialog = new ProgressDialog(MainActivity.this);

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.setMessage("Signing You In...");
                progressDialog.show();
                final String username = editSignInUsername.getText().toString();
                final String pass = String.valueOf(editSignInPassword.getText());
                supplierRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        supplier = dataSnapshot.child(username.replace(".","_")).getValue(Supplier.class);
                    if (dataSnapshot.child(username.replace(".","_")).exists()){
                            if (supplier.getPassword().equals(pass)){
                                Common.currentSupplier = supplier;
                                startActivity(new Intent(MainActivity.this,Home.class));
                                Toast.makeText(MainActivity.this, "Success", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                Toast.makeText(MainActivity.this, "Check Your Credentials", Toast.LENGTH_SHORT).show();
                            }
                        }

                        else {
                        Toast.makeText(MainActivity.this, "We can't find that user", Toast.LENGTH_SHORT).show();
                    }
                    progressDialog.dismiss();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });

    }





}
