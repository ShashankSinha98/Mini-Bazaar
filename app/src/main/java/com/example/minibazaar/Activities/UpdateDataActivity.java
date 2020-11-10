package com.example.minibazaar.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.minibazaar.NetworkSync.CheckInternetConnection;
import com.example.minibazaar.R;
import com.example.minibazaar.UserSession.UserSession;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;

public class UpdateDataActivity extends AppCompatActivity {

    private Button button;
    private EditText edtname,edtemail,edtmobile;
    CircleImageView primage;
    private TextView namebutton;
    private ImageView changeprofilepic;
    private String name,email,photo,mobile,newemail;
    private String check;
    boolean IMAGE_STATUS = false;

    //to get user session data
    private UserSession session;
    private Uri mainImageURI = null;
    private HashMap<String,String> user;
    private Button updateProfileBtn;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth mAuth;
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_data);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //check Internet Connection
        new CheckInternetConnection(this).checkConnection();

        initialize();

        //retrieve session values and display on listviews
        getValues();



        changeprofilepic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {


                Dexter.withContext(UpdateDataActivity.this)
                        .withPermissions(android.Manifest.permission.READ_EXTERNAL_STORAGE,
                                android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .withListener(new MultiplePermissionsListener() {
                            @Override
                            public void onPermissionsChecked(MultiplePermissionsReport report) {
                                // check if all permissions are granted
                                if (report.areAllPermissionsGranted()) {
                                    // do you work now
                                    bringImagePicker();
                                }

                                // check for permanent denial of any permission
                                if (report.isAnyPermissionPermanentlyDenied()) {
                                    // permission is denied permenantly, navigate user to app settings
                                    Snackbar.make(view, "Kindly grant Required Permission", Snackbar.LENGTH_LONG)
                                            .setAction("Allow", null).show();
                                }
                            }

                            @Override
                            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                                token.continuePermissionRequest();
                            }
                        })
                        .onSameThread()
                        .check();

            }
        });


        updateProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

               /* */

                if (validateName() && validateEmail() && validateNumber()) {

                    final KProgressHUD progressDialog=  KProgressHUD.create(UpdateDataActivity.this)
                            .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                            .setLabel("Please wait")
                            .setCancellable(false)
                            .setAnimationSpeed(2)
                            .setDimAmount(0.5f)
                            .show();

                    name = edtname.getText().toString();
                    email = edtemail.getText().toString();
                    mobile = edtmobile.getText().toString();



                // If Profile Picture is NOT CHANGED
                if(mainImageURI==null) {
                    // update data only
                    //  Toast.makeText(UpdateDataActivity.this, "Img Not Updated", Toast.LENGTH_SHORT).show();

                    firebaseFirestore.collection("Users").document(mAuth.getCurrentUser().getUid()).update(
                            "name", name, "number", mobile
                    ).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            progressDialog.dismiss();
                            Toasty.success(UpdateDataActivity.this, "Profile Updated Successfully! Please Login again.", 2000).show();
                            session.createLoginSession(name, email, mobile, photo);
                            session.logoutUser();
                            finish();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toasty.success(UpdateDataActivity.this, "Profile Updated Successfully", 2000).show();
                        }
                    });


                } else {
                    //Toast.makeText(UpdateDataActivity.this, "Image Updated", Toast.LENGTH_SHORT).show();
                    final StorageReference image_path = storageReference.child("profile_images").child(mAuth.getCurrentUser().getUid()+".jpg");

                    image_path.putFile(mainImageURI).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                            if (!task.isSuccessful()) {
                                //Log.d(TAG+"_TXN_ERR_1","ERROR IN SAVING MAIN IMAGE");
                                progressDialog.dismiss();
                                throw task.getException();
                            }

                            // Continue with the task to get the download URL
                            return image_path.getDownloadUrl();
                        }
                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()) {

                                //Log.d(TAG+"_TXN","4. MAIN IMAGE SAVED");
                                final Uri downloadUri = task.getResult();

                                //compressBitmap(downloadUri,name, mobile,email);
                                firebaseFirestore.collection("Users").document(mAuth.getCurrentUser().getUid()).update(
                                        "name", name, "number", mobile,"image",String.valueOf(downloadUri)
                                ).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        progressDialog.dismiss();
                                        Toasty.success(UpdateDataActivity.this, "Profile Updated Successfully! Please Login again.", 2000).show();
                                        session.createLoginSession(name, email, mobile, String.valueOf(downloadUri));
                                        session.logoutUser();
                                        finish();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        progressDialog.dismiss();
                                        Toasty.success(UpdateDataActivity.this, "Profile Updated Successfully", 2000).show();
                                    }
                                });


                            } else {
                                progressDialog.dismiss();
                               // Log.d(TAG+"_TXN_ERR_2","ERROR IN SAVING MAIN IMAGE");
                                String errorMessage = task.getException().getMessage();
                                //Log.d(TAG+"_ERR","UPLOAD Error : "+errorMessage);

                            }
                        }
                    });
                }
                } else {

                    Toasty.warning(UpdateDataActivity.this,"Incorrect Details Entered",Toast.LENGTH_LONG).show();

                }
            }
        });
    }
    public void viewCart(View view) {
        //startActivity(new Intent(UpdateData.this,Cart.class));
        //finish();
    }

    public void viewProfile(View view) {
        startActivity(new Intent(UpdateDataActivity.this,ProfileActivity.class));
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();

        //check Internet Connection
        new CheckInternetConnection(this).checkConnection();
    }


    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }


    private boolean validateNumber() {

        check = edtmobile.getText().toString();
        Log.e("inside number",check.length()+" ");
        if (check.length()>10) {
            return false;
        }else if(check.length()<10){
            return false;
        }
        return true;
    }

    private boolean validateEmail() {

        check = edtemail.getText().toString();

        if (check.length() < 4 || check.length() > 40) {
            return false;
        } else if (!check.matches("^[A-za-z0-9.@]+")) {
            return false;
        } else if (!check.contains("@") || !check.contains(".")) {
            return false;
        }

        return true;
    }

    private boolean validateName() {

        check = edtname.getText().toString();

        return !(check.length() < 4 || check.length() > 20);

    }

    //TextWatcher for Name -----------------------------------------------------

    TextWatcher nameWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            //none
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            //none
        }

        @Override
        public void afterTextChanged(Editable s) {

            check = s.toString();

            if (check.length() < 4 || check.length() > 20) {
                edtname.setError("Name Must consist of 4 to 20 characters");
            }
        }

    };

    //TextWatcher for Email -----------------------------------------------------

    TextWatcher emailWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            //none
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            //none
        }

        @Override
        public void afterTextChanged(Editable s) {

            check = s.toString();

            if (check.length() < 4 || check.length() > 40) {
                edtemail.setError("Email Must consist of 4 to 20 characters");
            } else if (!check.matches("^[A-za-z0-9.@]+")) {
                edtemail.setError("Only . and @ characters allowed");
            } else if (!check.contains("@") || !check.contains(".")) {
                edtemail.setError("Enter Valid Email");
            }

        }

    };

    //TextWatcher for Mobile -----------------------------------------------------

    TextWatcher numberWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            //none
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            //none
        }

        @Override
        public void afterTextChanged(Editable s) {

            check = s.toString();

            if (check.length()>10) {
                edtmobile.setError("Number cannot be grated than 10 digits");
            }else if(check.length()<10){
                edtmobile.setError("Number should be 10 digits");
            }
        }

    };

    private void initialize() {

        namebutton =findViewById(R.id.name_button);
        primage = findViewById(R.id.profilepic);
        edtname =findViewById(R.id.name);
        edtemail =findViewById(R.id.email);
        edtmobile =findViewById(R.id.number);
        updateProfileBtn = findViewById(R.id.update);
        changeprofilepic = findViewById(R.id.changeprofilepic);
        firebaseFirestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        edtname.addTextChangedListener(nameWatcher);
        edtemail.addTextChangedListener(emailWatcher);
        edtmobile.addTextChangedListener(numberWatcher);



    }

    private void bringImagePicker() {

        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1,1)
                .start(UpdateDataActivity.this);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                mainImageURI = result.getUri();
                primage.setImageURI(mainImageURI);

                IMAGE_STATUS = true;

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error = result.getError();

            }
        }
    }

    private void getValues() {

        //create new session object by passing application context
        session = new UserSession(getApplicationContext());

        //validating session
        session.isLoggedIn();

        //get User details if logged in
        user = session.getUserDetails();

        name = user.get(UserSession.KEY_NAME);
        email = user.get(UserSession.KEY_EMAIL);
        mobile = user.get(UserSession.KEY_MOBiLE);
        photo = user.get(UserSession.KEY_PHOTO);

        //setting values
        edtemail.setText(email);
        edtmobile.setText(mobile);
        edtname.setText(name);
        namebutton.setText(name);

        Glide.with(getBaseContext()).load(photo).into(primage);
    }
}