package com.android.NilaAp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.android.NilaAp.Activity.OrderDetails;
import com.android.NilaAp.Admin.OrderDetailsAdmin;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import com.android.NilaAp.Model.Order;
import com.android.NilaAp.R;

import java.util.ArrayList;

import io.paperdb.Paper;

public class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.ViewHolder> {
    private Context context;
    private ArrayList<Order> orderArrayList;
    DatabaseReference myRootRef;

    public OrdersAdapter(Context context, ArrayList<Order> orderArrayList) {
        this.context = context;
        this.orderArrayList = orderArrayList;
        myRootRef = FirebaseDatabase.getInstance().getReference();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_single_order, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        Order order = orderArrayList.get(position);
        holder.status.setText(order.getStatus());
        if (order.getStatus().equals("Completed")) {
            holder.status.setTextColor(context.getResources().getColor(R.color.green));
        }
        else if (order.getStatus().equals("Processing")) {
            holder.status.setTextColor(context.getResources().getColor(R.color.white));
        }
        else if (order.getStatus().equals("Canceled")) {
            holder.status.setTextColor(context.getResources().getColor(R.color.red));
        }
        holder.date.setText(order.getDateOfOrder());
        holder.totalPrice.setText("Ksh" + order.getTotalPrice());


        String user = Paper.book().read("active", "user");

        holder.detailsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //admin case
                if (!user.equals("user")){
                    Intent intent=new Intent(context, OrderDetailsAdmin.class);
                    intent.putExtra("order",order);
                    context.startActivity(intent);
                }
                //user case
                else{
                    Intent intent=new Intent(context, OrderDetails.class);
                    intent.putExtra("order",order);
                    context.startActivity(intent);
                }
            }
        });



        if (!user.equals("user")) {
            holder.layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.CustomAlertDialog);
                    ViewGroup viewGroup = view.findViewById(android.R.id.content);
                    View dialogView = LayoutInflater.from(view.getContext()).inflate(R.layout.dialoge_edit_order_status, viewGroup, false);

                    final Button processBtn = dialogView.findViewById(R.id.processing_btn);
                    final Button completedBtn = dialogView.findViewById(R.id.completed_btn);
                    final Button cencelBtn = dialogView.findViewById(R.id.cancel_btn);
                    final ProgressBar progressBar = dialogView.findViewById(R.id.dialoge_bar);
                    final ImageView closeBtn = dialogView.findViewById(R.id.close_btn);

                    builder.setView(dialogView);
                    final AlertDialog alertDialog = builder.create();

                    closeBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            alertDialog.dismiss();
                        }
                    });
                    processBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            progressBar.setVisibility(View.VISIBLE);
                            holder.status.setTextColor(context.getResources().getColor(R.color.white));
                            holder.status.setText("Processing");
                            Log.d("testOrder",order.toString());
                            updateStatus("Processing", order.getCustomerId(), order.getId(), order.getShippingPhoneNumber(), alertDialog, progressBar);
                        }
                    });
                    completedBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            progressBar.setVisibility(View.VISIBLE);
                            holder.status.setTextColor(context.getResources().getColor(R.color.green));
                            holder.status.setText("Completed");
                            updateStatus("Completed", order.getCustomerId(), order.getId(),order.getShippingPhoneNumber(), alertDialog, progressBar);
                        }
                    });
                    cencelBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            progressBar.setVisibility(View.VISIBLE);
                            holder.status.setTextColor(context.getResources().getColor(R.color.red));
                            holder.status.setText("Canceled");
                            updateStatus("Canceled", order.getCustomerId(), order.getId(), order.getShippingPhoneNumber(), alertDialog, progressBar);
                        }
                    });
                    alertDialog.show();
                }
            });
        }


    }

    private void updateStatus(String status, String customerId, String orderId, String shippingPhoneNumber, AlertDialog alertDialog, ProgressBar progressBar) {
        myRootRef.child("Order").child(customerId).child(orderId).child("status").setValue(status).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(context, "Order status Changed..!", Toast.LENGTH_SHORT).show();
                alertDialog.dismiss();

                // Get customer's phone number and send SMS
                // You need to replace "+1234567890" with the actual customer's phone number
                // Replace with actual phone number
                String message = "Your order status has been updated to: " + status + " order id: " + orderId;
                sendSMS(shippingPhoneNumber, message);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressBar.setVisibility(View.GONE);
                Log.d("test", e.toString());
            }
        });
    }

    @Override
    public int getItemCount() {
        return orderArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView status, date, totalPrice;
        LinearLayout layout;
        Button detailsBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            status = itemView.findViewById(R.id.order_status);
            date = itemView.findViewById(R.id.date_of_order);
            totalPrice = itemView.findViewById(R.id.order_total_price);
            detailsBtn = itemView.findViewById(R.id.order_details);
            layout = itemView.findViewById(R.id.layout);
        }
    }

    private void sendSMS(String phoneNumber, String message) {
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(phoneNumber, null, message, null, null);
    }

}
