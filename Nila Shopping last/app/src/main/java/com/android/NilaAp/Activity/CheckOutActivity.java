package com.android.NilaAp.Activity;

import static com.android.NilaAp.Model.Utils.TAG_medicine_list;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.android.NilaAp.Adapter.CartCustomAdapter;
import com.android.NilaAp.ConstantModules.MpesaActivity;
import com.android.NilaAp.Model.Order;
import com.android.NilaAp.Model.Product;
import com.android.NilaAp.Model.Report;
import com.android.NilaAp.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import io.paperdb.Paper;

public class CheckOutActivity extends AppCompatActivity {

    private ImageView checkOutBackBtn;
    private TextView orderPrice, shipmentPrice, totalPayablePrice, checkOutBtn, streetAddress;
    private EditText usercomments, shippingPhoneNumberEt;

    private ProgressDialog pd;
    private AlertDialog.Builder builder;

    private Order order;
    private ArrayList<Product> productArrayList;
    private CartCustomAdapter cartCustomAdapter;
    private RelativeLayout deliveryChargesLayout;

    private String street, comments, shippingPhoneNumber;

    private RadioGroup radioGroup;
    String movement = "Delivery";
    private CardView addressCardView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_out);
        initAll();

        //alert dialog
        builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm");
        builder.setMessage("Are you sure?");

        //setting up listeners
        setOnClickListeners();
    }

    private void setOnClickListeners() {
        checkOutBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        //checkout logic goes here
        checkOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                street = streetAddress.getText().toString();
                comments = usercomments.getText().toString();
                shippingPhoneNumber = shippingPhoneNumberEt.getText().toString();

                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (order.getTotalPrice() > 0) {
                            order.setStatus("Pending");
                            try {
                                settingDataOnServer();
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        } else {
                            Toast.makeText(CheckOutActivity.this, "No Item in Cart", Toast.LENGTH_SHORT).show();
                        }

                        dialog.dismiss();
                    }
                });

                builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                AlertDialog alert = builder.create();
                alert.show();
            }
        });
    }

    private void settingDataOnServer() throws ParseException {
        pd.show(this, "Please Wait..", "Submitting order..");

        DatabaseReference root = FirebaseDatabase.getInstance().getReference().child("Order");
        String key = root.push().getKey();
        order.setId(key);

        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
        order.setDateOfOrder(currentDateTimeString);

        if (movement.equals("Delivery")) {
            order.setTotalPrice(order.getTotalPrice() + 10);
            order.setStreet(street);
        } else {
            order.setTotalPrice(order.getTotalPrice() + 10);
        }

        order.setComments(comments);
        order.setCustomerId(FirebaseAuth.getInstance().getCurrentUser().getUid());
        order.setShippingPhoneNumber(shippingPhoneNumber);

        // Update product stock based on the number of products ordered
        for (Product product : order.getCartProductList()) {
            int orderedQuantity = product.getQuantityInCart();
            int remainingStock = Integer.parseInt(product.getStock()) - orderedQuantity;
            product.setStock(String.valueOf(remainingStock));
            // Update the product entry in the database with the new stock value
            DatabaseReference productRef = FirebaseDatabase.getInstance().getReference().child("Products").child(product.getProductId()).child("stock");
            productRef.setValue(String.valueOf(remainingStock));
        }


        root.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(key).setValue(order).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                updateSellReport(order.getTotalPrice());
                String orderId = order.getId();
                // Send SMS to the user upon successful order
                String recipientPhoneNumber = order.getShippingPhoneNumber(); // Use shipping phone number
                String smsMessage =  "Your order (ID: " + orderId + ") has been successfully placed. Thank you! \n For any pre order products ordered the goods take around 10 working days to arrive to you..";

                try {
                    String modifiedMessage = "Nila Shopping App: " + smsMessage;

                     SmsManager smsManager = SmsManager.getDefault();
                        smsManager.sendTextMessage(recipientPhoneNumber, null, modifiedMessage, null, null);
                    Toast.makeText(getApplicationContext(), "SMS sent.", Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "SMS failed, please try again.", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }


                order.getCartProductList().clear();
                order.setTotalPrice(0.0);
                Paper.book().delete("order");
                Paper.book().write("order", order);
            }
        });
    }

    private void updateSellReport(double totalPrice) {
        int alacarte = 0;
        int bento = 0;
        int handroll = 0;
        int beverage = 0;

        for (int i = 0; i < productArrayList.size(); i++) {
            String category = productArrayList.get(i).getCategory();
            if (category.equals("Alacarte")) {
                alacarte += 1;
            } else if (category.equals("Bento")) {
                bento += 1;
            } else if (category.equals("Handroll")) {
                handroll += 1;
            } else if (category.equals("Beverage")) {
                beverage += 1;
            }
        }

        Report report = new Report(alacarte, bento, handroll, beverage, totalPrice);
        DatabaseReference root = FirebaseDatabase.getInstance().getReference().child("Reports");
        String key = root.push().getKey();
        report.setId(key);

        DateFormat dateFormat = new SimpleDateFormat("MM");
        Date date = new Date();
        Log.d("Month", dateFormat.format(date));

        root.child(dateFormat.format(date)).child(key).setValue(report).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                productArrayList.clear();
                pd.dismiss();
//                Toast.makeText(CheckOutActivity.this, "Order Submitted" + totalPrice, Toast.LENGTH_LONG).show();
                // Add a toast message for successful stock reduction
                Toast.makeText(CheckOutActivity.this, "Product stock updated successfully", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(CheckOutActivity.this, MpesaActivity.class);
                int price = (int) totalPrice;
                intent.putExtra("itemPrice", price);

                startActivity(intent);
                finish();
            }
        });
    }

    private void initAll() {
        checkOutBackBtn = findViewById(R.id.checkout_back_btn);
        addressCardView = findViewById(R.id.address_card_view);
        orderPrice = findViewById(R.id.checkout_order_price_view);
        shipmentPrice = findViewById(R.id.checkout_shipping_price_view);
        totalPayablePrice = findViewById(R.id.checkout_total_price_view);
        streetAddress = findViewById(R.id.checkout_address_view);
        usercomments = findViewById(R.id.checkout_comment_view);
        shippingPhoneNumberEt = findViewById(R.id.shipping_form_number_et);
        checkOutBtn = findViewById(R.id.checkout_btn);
        radioGroup = findViewById(R.id.radioGroup);
        deliveryChargesLayout = findViewById(R.id.delivery_charges_layout);

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                movement = ((RadioButton) findViewById(radioGroup.getCheckedRadioButtonId())).getText().toString();

                if (movement.equals("Delivery")) {
                    deliveryChargesLayout.setVisibility(View.VISIBLE);
                    totalPayablePrice.setText("Ksh " + new DecimalFormat("##.##").format(order.getTotalPrice() + 10));
                    addressCardView.setVisibility(View.VISIBLE);
                } else {
                    deliveryChargesLayout.setVisibility(View.GONE);
                    totalPayablePrice.setText("Ksh " + new DecimalFormat("##.##").format(order.getTotalPrice()));
                    addressCardView.setVisibility(View.GONE);
                }
            }
        });

        pd = new ProgressDialog(this);
        order = new Order();
        Intent intent = getIntent();
        order = (Order) intent.getSerializableExtra("order");

        productArrayList = new ArrayList<>();
        Intent i = getIntent();
        productArrayList = (ArrayList<Product>) i.getSerializableExtra(TAG_medicine_list);
        streetAddress.setText(order.getAddress());
        orderPrice.setText("Ksh " + new DecimalFormat("##.##").format(order.getTotalPrice()));
        totalPayablePrice.setText("Ksh. " + new DecimalFormat("##.##").format(order.getTotalPrice() + 10));
    }
}
