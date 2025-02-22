package com.android.NilaAp.Adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.NilaAp.Activity.ProductDetailsActivity;
import com.android.NilaAp.Admin.NewProductActivity;
import com.android.NilaAp.Model.Product;
import com.android.NilaAp.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.List;

import io.paperdb.Paper;

public class PreOrderProductsAdapter extends RecyclerView.Adapter<PreOrderProductsAdapter.MyViewHolder>
{
    List<Product> myJokesList;
    Activity context;
    boolean isAdmin;
    DatabaseReference myRootRef;


    public PreOrderProductsAdapter(List<Product> myJokesList, Activity context, boolean isAdmin) {
        this.myJokesList = myJokesList;
        this.context = context;
        this.isAdmin = isAdmin;
        myRootRef = FirebaseDatabase.getInstance().getReference();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.single_product_layout, parent, false);

        return new PreOrderProductsAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, @SuppressLint("RecyclerView") int position)
    {

        Product preOrderProduct= myJokesList.get(position);

        if (preOrderProduct.getPhotoUrl() != null) {
            if (!preOrderProduct.getPhotoUrl().equals("")) {
                holder.productImg.setVisibility(View.VISIBLE);
                Picasso.get().load(preOrderProduct.getPhotoUrl()).placeholder(R.drawable.no_background_icon).into(holder.productImg);
            }
        }
        holder.name.setText(preOrderProduct.getName());
        holder.price.setText("Ksh" + preOrderProduct.getPrice());

        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isAdmin) {
                    Intent intent = new Intent(context, ProductDetailsActivity.class);
                    intent.putExtra("product", preOrderProduct);
                    context.startActivity(intent);
                }
            }
        });


        String user = Paper.book().read("active", "user");

        if (!user.equals("user")) {
            holder.editBTn.setVisibility(View.VISIBLE);
            holder.delteBtn.setVisibility(View.VISIBLE);

            holder.editBTn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, NewProductActivity.class);
                    intent.putExtra("product", preOrderProduct);
                    context.startActivity(intent);
                }
            });

            holder.delteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    myRootRef.child("Pre-Order Products").child(preOrderProduct.getProductId()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(context, "Product removed Successfully", Toast.LENGTH_SHORT).show();
                            try {
                                myJokesList.remove(position);
                                notifyDataSetChanged();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("test", e.toString());
                        }
                    });
                }
            });
        }


    }

    @Override
    public int getItemCount() {
        return myJokesList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder
    {

        LinearLayout layout;
        ImageView productImg, delteBtn,editBTn;
        TextView name, price;

        public MyViewHolder(@NonNull View itemView)
        {
            super(itemView);
            layout = itemView.findViewById(R.id.layout);
            productImg = itemView.findViewById(R.id.category_image);
            name = itemView.findViewById(R.id.product_brand_name);
            price = itemView.findViewById(R.id.price_tv);
            delteBtn = itemView.findViewById(R.id.delete_btn);
            editBTn = itemView.findViewById(R.id.edit_btn);
        }
    }
}
