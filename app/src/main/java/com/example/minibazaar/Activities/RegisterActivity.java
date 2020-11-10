package com.example.minibazaar.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.creativityapps.gmailbackgroundlibrary.BackgroundMail;
import com.example.minibazaar.NetworkSync.CheckInternetConnection;
import com.example.minibazaar.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;
import id.zelory.compressor.Compressor;


public class RegisterActivity extends AppCompatActivity {

    private EditText edtname, edtemail, edtpass, edtcnfpass, edtnumber;
    private String check,name,email,password,mobile,profile;
    CircleImageView image;
    ImageView upload;
    private Uri mainImageURI = null;
    boolean IMAGE_STATUS = false;
    Bitmap profilePicture;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private final String TAG = this.getClass().getSimpleName();
    private String userId;

    private StorageReference storageReference;
    private FirebaseFirestore firebaseFirestore;

    private Bitmap compressedImageFile;
    private KProgressHUD progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //check Internet Connection
        new CheckInternetConnection(this).checkConnection();

        Typeface typeface = ResourcesCompat.getFont(this, R.font.blacklist);
        TextView appname = findViewById(R.id.appname);
        appname.setTypeface(typeface);

        FirebaseApp.initializeApp(RegisterActivity.this);

        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();


        upload=findViewById(R.id.uploadpic);
        image=findViewById(R.id.profilepic);
        edtname = findViewById(R.id.name);
        edtemail = findViewById(R.id.email);
        edtpass = findViewById(R.id.password);
        edtcnfpass = findViewById(R.id.confirmpassword);
        edtnumber = findViewById(R.id.number);

        edtname.addTextChangedListener(nameWatcher);
        edtemail.addTextChangedListener(emailWatcher);
        edtpass.addTextChangedListener(passWatcher);
        edtcnfpass.addTextChangedListener(cnfpassWatcher);
        edtnumber.addTextChangedListener(numberWatcher);


        //validate user details and register user

        Button register_button=findViewById(R.id.register);

        register_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //TODO AFTER VALDATION
                if (validateProfile() && validateName() && validateEmail() && validatePass() && validateCnfPass() && validateNumber()) {

                    name=edtname.getText().toString();
                    email=edtemail.getText().toString();
                    password=edtcnfpass.getText().toString();
                    mobile=edtnumber.getText().toString();

                    try{

                      progressDialog  =  KProgressHUD.create(RegisterActivity.this)
                                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                                .setLabel("Please wait")
                                .setCancellable(false)
                                .setAnimationSpeed(2)
                                .setDimAmount(0.5f)
                                .show();



                        //Validation Success
                        //convertBitmapToString(profilePicture);

                        // Creating New Account with Email and Password
                        Log.d(TAG+"_TXN","1. CREATING ACCOUNT");
                        mAuth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
                                            Log.d(TAG+"_TXN","2. ACCOUNT CREATED");

                                            // Sign in success, update UI with the signed-in user's information
                                            Log.d(TAG+"_SS", "createUserWithEmail:success");


                                            user = mAuth.getCurrentUser();
                                            userId = mAuth.getCurrentUser().getUid();
                                            Log.d(TAG+"_UID",userId);


                                            final StorageReference image_path = storageReference.child("profile_images").child(userId+".jpg");


                                            Log.d(TAG+"_TXN","3. SAVING MAIN IMG");
                                            // Saving Image to Firebase Storage
                                            image_path.putFile(mainImageURI).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                                                @Override
                                                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                                    if (!task.isSuccessful()) {
                                                        Log.d(TAG+"_TXN_ERR_1","ERROR IN SAVING MAIN IMAGE");
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

                                                        Log.d(TAG+"_TXN","4. MAIN IMAGE SAVED");
                                                        Uri downloadUri = task.getResult();

                                                        //compressBitmap(downloadUri,name, mobile,email);
                                                        storeToFirestore(name, mobile,downloadUri, email);


                                                    } else {
                                                        progressDialog.dismiss();
                                                        Log.d(TAG+"_TXN_ERR_2","ERROR IN SAVING MAIN IMAGE");
                                                        String errorMessage = task.getException().getMessage();
                                                        Log.d(TAG+"_ERR","UPLOAD Error : "+errorMessage);

                                                    }
                                                }

                                            });

                                        } else {
                                            progressDialog.dismiss();
                                            // If sign in fails, display a message to the user.
                                            Log.w(TAG+"_ERR", "createUserWithEmail:failure", task.getException());
                                            Toasty.error(RegisterActivity.this,"Failed to Register",Toast.LENGTH_LONG,true).show();
                                        }
                                    }
                                });

                    } catch (Exception e){
                        Log.w(TAG+"_ERR", "Error: "+ e.getMessage());
                        Toasty.error(RegisterActivity.this,"Failed to Register",Toast.LENGTH_LONG,true).show();

                    }


                }
            }
        });

        //Take already registered user to login page

        final TextView loginuser=findViewById(R.id.login_now);
        loginuser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RegisterActivity.this,LoginActivity.class));
                finish();
            }
        });

        //take user to reset password

        final TextView forgotpass=findViewById(R.id.forgot_pass);
        forgotpass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RegisterActivity.this,ForgotPasswordActivity.class));
                finish();
            }
        });

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {

                Dexter.withContext(RegisterActivity.this)
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



                //result will be available in onActivityResult which is overridden
            }
        });


    }

    private void bringImagePicker() {

        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1,1)
                .start(RegisterActivity.this);

    }

    private void sendRegistrationEmail(final String name, final String emails) {


        BackgroundMail.newBuilder(RegisterActivity.this)
                .withSendingMessage("Sending Welcome Greetings to Your Email !")
                .withSendingMessageSuccess("Kindly Check Your Email now !")
                .withSendingMessageError("Failed to send password ! Try Again !")
                .withUsername("sinhashashank.98@gmail.com")
                .withPassword("Shashank")
                .withMailto(emails)
                .withType(BackgroundMail.TYPE_PLAIN)
                .withSubject("Greetings from Mini Bazaar")
                .withBody("Hello Mr/Miss, "+ name + "\n " + getString(R.string.registermail1))
                .send();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                mainImageURI= result.getUri();
                image.setImageURI(mainImageURI);

                IMAGE_STATUS = true;

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error = result.getError();

            }
        }

        /*if (requestCode == 1000 && resultCode == Activity.RESULT_OK && data != null) {
            //Image Successfully Selected
            try {
                //parsing the Intent data and displaying it in the imageview
                Uri imageUri = data.getData();//Geting uri of the data
                InputStream imageStream = getContentResolver().openInputStream(imageUri);//creating an imputstrea
                profilePicture = BitmapFactory.decodeStream(imageStream);//decoding the input stream to bitmap
                image.setImageBitmap(profilePicture);
                IMAGE_STATUS = true;//setting the flag
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }*/
    }

    private void convertBitmapToString(Bitmap profilePicture) {
            /*
                Base64 encoding requires a byte array, the bitmap image cannot be converted directly into a byte array.
                so first convert the bitmap image into a ByteArrayOutputStream and then convert this stream into a byte array.
            */
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        profilePicture.compress(Bitmap.CompressFormat.PNG, 50, byteArrayOutputStream);
        byte[] array = byteArrayOutputStream.toByteArray();
        profile = Base64.encodeToString(array, Base64.DEFAULT);
    }

    private boolean validateNumber() {

        check = edtnumber.getText().toString();
        Log.e("inside number",check.length()+" ");
        if (check.length()>10) {
            return false;
        }else if(check.length()<10){
            return false;
        }
        return true;
    }

    private boolean validateCnfPass() {

        check = edtcnfpass.getText().toString();

        return check.equals(edtpass.getText().toString());
    }

    private boolean validatePass() {


        check = edtpass.getText().toString();

        if (check.length() < 4 || check.length() > 20) {
            return false;
        } else if (!check.matches("^[A-za-z0-9@]+")) {
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

    private boolean validateProfile() {
        if (!IMAGE_STATUS)
            Toasty.info(RegisterActivity.this,"Select A Profile Picture", Toast.LENGTH_LONG).show();
        return IMAGE_STATUS;
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

    //TextWatcher for pass -----------------------------------------------------

    TextWatcher passWatcher = new TextWatcher() {
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
                edtpass.setError("Password Must consist of 4 to 20 characters");
            } else if (!check.matches("^[A-za-z0-9@]+")) {
                edtemail.setError("Only @ special character allowed");
            }
        }

    };

    //TextWatcher for repeat Password -----------------------------------------------------

    TextWatcher cnfpassWatcher = new TextWatcher() {
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

            if (!check.equals(edtpass.getText().toString())) {
                edtcnfpass.setError("Both the passwords do not match");
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
                edtnumber.setError("Number cannot be grated than 10 digits");
            }else if(check.length()<10){
                edtnumber.setError("Number should be 10 digits");
            }
        }

    };

    @Override
    protected void onResume() {
        super.onResume();
        //check Internet Connection
        new CheckInternetConnection(this).checkConnection();
    }

    /*
    FOR COMPRESSING IMAGE
    private void compressBitmap(final Uri downloadUri, final String user_name, final String user_number, final String user_email) {

        Log.d(TAG+"_TXN","5. COMPRESSING IMAGE");


        Log.d(TAG+"_MAIN_URI", String.valueOf(downloadUri));

        File newImageFile = new File(mainImageURI.getPath());

        Log.d(TAG, String.valueOf(newImageFile));

        try {

            // Reducing Image Quality...
            compressedImageFile = new Compressor(RegisterActivity.this)
                    .setMaxWidth(100)
                    .setMaxHeight(100)
                    .setQuality(2)
                    .compressToBitmap(newImageFile);

        } catch (IOException e) {
            Log.d(TAG+"_ERR3","ERROR IN COMPRESSING IMAGE");
            progressDialog.dismiss();
            String errorMessage = e.getMessage();
            Toast.makeText(RegisterActivity.this,"Upload Error 1 : "+errorMessage,Toast.LENGTH_LONG).show();

        }

        Log.d(TAG+"_TXN","6. IMAGE COMPRESSED");
        Log.d(TAG, String.valueOf(compressedImageFile));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        compressedImageFile.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] thumbData = baos.toByteArray();


        // File path for Thumb Image...

        String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final StorageReference thumbFilePath = storageReference.child("profile_images/thumbs").child(user_id+".jpg");


        //Uploading of thumb img...
        final UploadTask uploadTask = thumbFilePath.putBytes(thumbData);

        //Getting URI of thumb img...
        Log.d(TAG+"_TXN","6. SAVING THUMB IMAGE");
        uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    Log.d(TAG+"_ERR_4","ERROR IN SAVINNG THUMB IMAGE");
                    progressDialog.dismiss();
                    throw task.getException();
                }

                return thumbFilePath.getDownloadUrl();

            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {

                if(task.isSuccessful())
                {


                    // Downloadable Thumb URI...
                    final Uri thumb_Uri = task.getResult();


                    // Success Listener for uploading Thumb URI...
                    uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            Log.d(TAG+"_TXN","7. THUMB IMAGE SAVED");
                            Toast.makeText(RegisterActivity.this,"The Image is Uploaded",Toast.LENGTH_LONG).show();
                            storeToFirestore(user_name, user_number,downloadUri, thumb_Uri, user_email);

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            Log.d(TAG+"_ERR_5","ERROR IN SAVINNG THUMB IMAGE");
                            String errorMessage = e.getMessage();
                            Toast.makeText(RegisterActivity.this,"Upload Error 2 : "+errorMessage,Toast.LENGTH_LONG).show();

                        }
                    });

                } else {
                    Log.d(TAG+"_ERR_6","ERROR IN SAVINNG THUMB IMAGE");
                    progressDialog.dismiss();
                    String errorMessage = task.getException().getMessage();
                    Toast.makeText(RegisterActivity.this,"Upload Error 3 : "+errorMessage,Toast.LENGTH_LONG).show();

                }
            }
        });
    } */

    private void storeToFirestore(String user_name, String user_number,  Uri downloadUri, String user_email) {


        Log.d(TAG+"_TXN","8. SAVING DATA TO FIRESTORE");
        // Storing data on Firestore...

        Map<String, String> userMap = new HashMap<>();
        userMap.put("name",user_name);
        userMap.put("image",downloadUri.toString());
        userMap.put("number", user_number);
        userMap.put("email",user_email);



        firebaseFirestore.collection("Users").document(userId).set(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {


                if(task.isSuccessful())
                {

                    // Sending Verification Link via Email
                    Log.d(TAG+"_TXN","9. DATA SAVED");
                    Log.d(TAG+"_TXN","10. SENDING EMAIL");
                    user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            progressDialog.dismiss();
                            if (task.isSuccessful()) {
                                Log.d(TAG+"_TXN","11. EMAIL SENT");
                                Toasty.success(RegisterActivity.this,"We've sent a verification link to your email.",Toast.LENGTH_SHORT,true).show();
                            }
                        }
                    });

                    new Handler().postDelayed(new Runnable() {

                        /*
                         * Showing splash screen with a timer. This will be useful when you
                         * want to show case your app logo / company
                         */

                        @Override
                        public void run() {
                            // This method will be executed once the timer is over
                            // Start your app main activity
                            startActivity(new Intent(RegisterActivity.this,LoginActivity.class));
                            finish();
                        }
                    }, 2000);



                } else {
                    Log.d(TAG+"_ERR5","ERROR IN SAVING DATA");
                    progressDialog.dismiss();
                    String errorMessage = task.getException().getMessage();
                    Toast.makeText(RegisterActivity.this,"FIRESTORE Error : "+errorMessage,Toast.LENGTH_LONG).show();

                }

            }
        });



    }
}
