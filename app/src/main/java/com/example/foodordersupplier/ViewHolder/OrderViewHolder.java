package com.example.foodordersupplier.ViewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.foodordersupplier.R;

import mehdi.sakout.fancybuttons.FancyButton;

public class OrderViewHolder extends RecyclerView.ViewHolder {

    public TextView txtOrderId, txtOrderStatus, txtOrderPhone, txtOrderAddress,txtSupplier;
    public Button btnEdit,btnDetails;
    //private ItemClickListener itemClickListener;

    public OrderViewHolder(View itemView) {
        super(itemView);

        txtOrderId = itemView.findViewById(R.id.text_order_id);
        txtOrderAddress = itemView.findViewById(R.id.text_order_add);
        txtOrderPhone = itemView.findViewById(R.id.text_order_phone);
        txtOrderStatus = itemView.findViewById(R.id.text_order_status);
        txtSupplier = itemView.findViewById(R.id.text_order_supplier);
        btnEdit = itemView.findViewById(R.id.btnEdit);
        btnDetails = itemView.findViewById(R.id.btnDetails);

    }

}