package com.example.minibazaar.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.example.minibazaar.Helper.JSONParser;
import com.example.minibazaar.Models.PlacedOrderModel;
import com.example.minibazaar.Models.SingleProductModel;
import com.example.minibazaar.R;
import com.example.minibazaar.UserSession.UserSession;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.paytm.pgsdk.PaytmOrder;
import com.paytm.pgsdk.PaytmPGService;
import com.paytm.pgsdk.PaytmPaymentTransactionCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import es.dmoral.toasty.Toasty;

public class PaymentActivity extends AppCompatActivity implements PaytmPaymentTransactionCallback {

    private String custId = "", orderId = "", merchantId = "", amountToPay = "";

    private static String TAG = "PaymentActivity";

    private FirebaseFirestore firebaseFirestore;
    private PlacedOrderModel placedOrderModel;
    private String currdatetime;
    private ArrayList<SingleProductModel> cartcollect;
    private UserSession session;
    private HashMap<String, String> user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        session = new UserSession(PaymentActivity.this);
        user = session.getUserDetails();

        Intent intent = getIntent();
        Bundle args = intent.getBundleExtra("BUNDLE");
        cartcollect = (ArrayList<SingleProductModel>) args.getSerializable("cartcollect");
        Log.d("xlr8_PYTM_cc", String.valueOf(cartcollect));

        orderId = intent.getExtras().getString("orderid");
        Log.d("xlr8_PYTM_oid", String.valueOf(orderId));
        custId = intent.getExtras().getString("custid");
        Log.d("xlr8_PYTM_cid", String.valueOf(custId));
        amountToPay = intent.getExtras().getString("amount_to_pay");
        Log.d("xlr8_PYTM_atp", String.valueOf(amountToPay));
        placedOrderModel = (PlacedOrderModel) intent.getSerializableExtra("PlacedOrderModel");
        Log.d("xlr8_PYTM_pom", String.valueOf(placedOrderModel));
        currdatetime = intent.getExtras().getString("currdatetime");
        Log.d("xlr8_PYTM_cdt", String.valueOf(currdatetime));


        merchantId = "PUT YOUR ID";
        FirebaseApp.initializeApp(this);
        firebaseFirestore = FirebaseFirestore.getInstance();

        sendUserDetailToServer d1 = new sendUserDetailToServer(); 
        d1.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }

    @Override
    public void onTransactionResponse(Bundle inResponse) {

        Log.d(TAG, "onTransactionResponse: "+String.valueOf(inResponse));
        String status = inResponse.getString("STATUS");

        if(status.equals("TXN_SUCCESS")) {

            Toasty.success(PaymentActivity.this,"Payment Successfull",2000).show();


            saveDataToFirestoreServer();
        }



    }

    private void saveDataToFirestoreServer() {

        final KProgressHUD progressDialog = KProgressHUD.create(PaymentActivity.this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setLabel("Placing Order. Please Wait...")
                .setCancellable(false)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
                .show();


        //adding user details to the database under orders table
        firebaseFirestore.collection("Orders").document(placedOrderModel.getPlaced_user_email())
                .collection(user.get(UserSession.KEY_NAME)+" Orders")
                .document(currdatetime)
                .set(placedOrderModel).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                for (final SingleProductModel model : cartcollect) {

                    firebaseFirestore.collection("Orders").document(placedOrderModel.getPlaced_user_email())
                            .collection(user.get(UserSession.KEY_NAME)+" Orders")
                            .document(currdatetime).collection("Items").add(model).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            Log.d(TAG, "Model Added: " + model.getPrname());
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toasty.warning(PaymentActivity.this, "Server down! Please contact Administrator.", 2000).show();
                            Log.d(TAG, "Error: " + e.getMessage());
                        }
                    });
                }

                for (final SingleProductModel model : cartcollect) {
                    firebaseFirestore.collection("Cart").document(placedOrderModel.getPlaced_user_email())
                            .collection(user.get(UserSession.KEY_NAME) + " Cart").document(String.valueOf(model.getPrid()))
                            .delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "Model: " + model.getPrname() + " Deleted");
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "Deleting Error: " + e.getMessage());
                        }
                    });

                }
                session.setCartValue(0);
                progressDialog.dismiss();


                Toasty.success(PaymentActivity.this, "Order Placed Successfully", 2000).show();
                Intent intent = new Intent(PaymentActivity.this, OrderPlacedActivity.class);
                intent.putExtra("orderid", orderId);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toasty.warning(PaymentActivity.this, "Server down! Please contact Administrator.", 2000).show();
                Log.d(TAG, "Placed Order Model Error: " + e.getMessage());
            }
        });




    }

    @Override
    public void networkNotAvailable() {

    }

    @Override
    public void clientAuthenticationFailed(String inErrorMessage) {

    }

    @Override
    public void someUIErrorOccurred(String inErrorMessage) {

    }

    @Override
    public void onErrorLoadingWebPage(int iniErrorCode, String inErrorMessage, String inFailingUrl) {

    }

    @Override
    public void onBackPressedCancelTransaction() {

       /* Log.d(TAG, "onBackPressedCancelTransaction called");
        Intent intent = new Intent(PaymentActivity.this, FoodMenuActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();*/



    }

    @Override
    public void onTransactionCancel(String inErrorMessage, Bundle inResponse) {

    }

    public class sendUserDetailToServer extends AsyncTask<ArrayList<String>, Void, String> {


        //http://babayaga98.epizy.com/generateChecksum.php
        String url = "https://babayaga19.000webhostapp.com/paytm/generateChecksum.php";
        String verifyUrl = "https://pguat.paytm.com/paytmchecksum/paytmCallback.jsp";

        String CHECKSUMHASH = "";


        @Override
        protected void onPreExecute() {
            //Toast.makeText(PaymentActivity.this, "Please wait", Toast.LENGTH_SHORT).show();
            Log.d(TAG,"onPreExecute");

        }


        @Override
        protected String doInBackground(ArrayList<String>... arrayLists) {

            JSONParser jsonParser = new JSONParser(PaymentActivity.this);

            String param =
                    "MID=" + merchantId +
                            "&ORDER_ID=" + orderId +
                            "&CUST_ID=" + custId +
                            "&CHANNEL_ID=WAP&TXN_AMOUNT="+amountToPay+"&WEBSITE=WEBSTAGING" +
                            "&CALLBACK_URL=" + verifyUrl + "&INDUSTRY_TYPE_ID=Retail";

            Log.e(TAG,param);

            JSONObject jsonObject = jsonParser.makeHttpRequest(url, "POST", param);

            // yaha per checksum ke saht order id or status receive hoga..
//            Log.e("CheckSum result >>", jsonObject.toString());

            if (jsonObject != null) {
                Log.e("CheckSum result >>", jsonObject.toString());
                try {
                    CHECKSUMHASH = jsonObject.has("CHECKSUMHASH") ? jsonObject.getString("CHECKSUMHASH") : "";
                    Log.e(TAG,"Checksum: " + CHECKSUMHASH);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d(TAG,"Exception 2: "+e.getMessage());

                }
            } else {
                // Toast.makeText(getApplicationContext(), "json Obj is null", Toast.LENGTH_SHORT).show();
                Log.d(TAG,"Json Obj is null!!");
            }

            return CHECKSUMHASH;

        }

        @Override
        protected void onPostExecute(String result) {
            Log.d(TAG,"onPostExecute");
            Log.e(" setup acc ", "  signup result  " + result);
            //Toast.makeText(PaymentActivity.this, "Signup result : " + result, Toast.LENGTH_SHORT).show();

            PaytmPGService Service = PaytmPGService.getStagingService();

            HashMap<String, String> paramMap = new HashMap<String, String>();

            paramMap.put("MID", merchantId); //MID provided by paytm
            paramMap.put("ORDER_ID", orderId);
            paramMap.put("CUST_ID", custId);
            paramMap.put("CHANNEL_ID", "WAP");
            paramMap.put("TXN_AMOUNT", amountToPay);
            paramMap.put("WEBSITE", "WEBSTAGING");
            paramMap.put("CALLBACK_URL" ,verifyUrl);
            paramMap.put("CHECKSUMHASH" ,CHECKSUMHASH);
            paramMap.put("INDUSTRY_TYPE_ID", "Retail");


            PaytmOrder Order = new PaytmOrder(paramMap);
            Log.e("checksum ", "param "+ paramMap.toString());


            Service.initialize(Order,null);

            // start payment service call here
            Service.startPaymentTransaction(PaymentActivity.this, true, true,
                    PaymentActivity.this );
        }



    }
}
