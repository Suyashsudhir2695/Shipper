package com.example.foodordersupplier.Common;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.location.Location;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.foodordersupplier.Model.Request;
import com.example.foodordersupplier.Model.ShippingInfo;
import com.example.foodordersupplier.Model.Supplier;
import com.example.foodordersupplier.Remote.IGeoCoordinates;
import com.example.foodordersupplier.Remote.RetrofitClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class Common {
    public static Supplier currentSupplier;
    public static String SUPPLIER_ORDERS = "SupplierOrders";
    public static String codeToStatus(String status) {
        if (status.equals("0"))
            return "Placed";
        else if (status.equals("1"))
            return "Preparing";
        else if (status.equals("2"))
            return "Shipped";
        else
            return "Out For Delivery";

    }
    public static String replaceEmail(String email){
        return email.replace(".","_");


    }

    public static final String baseUrl = "https://maps.googleapis.com";

    public static Request currentRequest;
    public static  String currentKey;

    public static void createShippingOrder(String key, String email, Location mLastLocation) {
        ShippingInfo shippingInfo = new ShippingInfo();
        shippingInfo.setOrderId(key);
        shippingInfo.setSupplierEmail(email);
        shippingInfo.setLat(mLastLocation.getLatitude());
        shippingInfo.setLng(mLastLocation.getLongitude());

        FirebaseDatabase.getInstance()
                .getReference(SUPPLIER_ORDERS)
                .child(key)
                .setValue(shippingInfo)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception error) {
                        Log.d("Error",error.getMessage());
                    }
                });


    }

    public static Bitmap scaleBitmap(Bitmap bitmap, int newWidth, int newHeight) {
        Bitmap scaledBitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888);

        float scaleX = newWidth / (float) bitmap.getWidth();
        float scaleY = newHeight / (float) bitmap.getHeight();

        float pivotX=0,pivotY=0;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(scaleX,scaleY,pivotX,pivotY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bitmap,0,0,new Paint(Paint.FILTER_BITMAP_FLAG));

        return scaledBitmap;

    }

    public static void updateShippingInfo(String currentKey, Location mLastLocation) {

        Map<String,Object> map = new HashMap<>();
        map.put("lat",mLastLocation.getLatitude());
        map.put("lng",mLastLocation.getLongitude());

        FirebaseDatabase.getInstance()
                .getReference(SUPPLIER_ORDERS)
                .child(currentKey)
                .updateChildren(map)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i("Error", "onFailure: "+ e.getMessage());
                    }
                });

    }

    public static IGeoCoordinates getGeoCodeService() {
        return RetrofitClient.getClient(baseUrl).create(IGeoCoordinates.class);

    }
}
