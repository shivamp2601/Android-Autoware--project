package com.example.autoware;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class Owner_AccountFragment extends Fragment {


    private EditText Name, Location, Garagename,address,phone;
    private ImageButton Save, Cancel;
    private FloatingActionButton ChooseImage;
    private ImageView ProfileImage;
    private NavigationView nav;
    private static final int IMAGE_PICK_CODE = 1000;
    private static final int PERMISSION_CODE = 1001;
    public Owner_AccountFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_owner__account, container, false);
        Name = (EditText) v.findViewById(R.id.Account_UserName);
        Location = (EditText) v.findViewById(R.id.Account_Location);
        Garagename =(EditText) v.findViewById(R.id.Account_Garagename);
        address =(EditText) v.findViewById(R.id.Account_Address);
        phone =(EditText) v.findViewById(R.id.Account_Phone);
        Save = (ImageButton) v.findViewById(R.id.Account_Savebtn);
        Cancel = (ImageButton) v.findViewById(R.id.Account_Cancelbtn);
        ProfileImage = (ImageView) v.findViewById(R.id.Account_ProfileImage);
        ChooseImage = (FloatingActionButton) v.findViewById(R.id.Account_EditPhotobtn);
        nav = (NavigationView) getActivity().findViewById(R.id.cust_navigationdrawer);
        File imgFile = new File(Environment.getExternalStorageDirectory(), "/AutowarePictures/OwnerProfilePicture.jpg");
        try {
            Glide.with(getActivity().getApplicationContext()).load(imgFile).into(ProfileImage);
        } catch (Exception e) {
            e.printStackTrace();
        }

        FirebaseFirestore.getInstance().collection("Owners").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).get().
                addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Owner o = documentSnapshot.toObject(Owner.class);
                        Name.setText(o.getName());
                        Location.setText(o.getLocation());
                        Garagename.setText(o.getGaragename());
                        address.setText(o.getAddress());
                        phone.setText(o.getPhone());
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getActivity().getApplicationContext(), "Operation Failure", Toast.LENGTH_SHORT).show();
            }
        });

        ChooseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //check runtime permission
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (getActivity().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                            == PackageManager.PERMISSION_DENIED) {
                        //permission not granted, request it.
                        String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE};
                        //show popup for runtime permission
                        requestPermissions(permissions, PERMISSION_CODE);
                    } else {
                        //permission already granted
                        pickImageFromGallery();
                    }
                } else {
                    //system os is less then marshmallow
                    pickImageFromGallery();
                }
            }
        });

        Save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(Name.getText())) {
                    Name.setError("Please enter a name");
                    return;
                }
                if (TextUtils.isEmpty(Location.getText())) {
                    Location.setError("Please Enter a location");
                    return;
                }
                if (TextUtils.isEmpty(Garagename.getText())) {
                    Garagename.setError("Please Enter name of your garage");
                    return;
                }
                if (TextUtils.isEmpty(address.getText())) {
                    address.setError("Please Enter address");
                    return;
                }
                if (TextUtils.isEmpty(phone.getText())) {
                    phone.setError("Please Enter phone number");
                    return;
                }
                FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                FirebaseUser user = firebaseAuth.getCurrentUser();
                BitmapDrawable draw = (BitmapDrawable) ProfileImage.getDrawable();
                Bitmap bitmap = draw.getBitmap();

                File storage = Environment.getExternalStorageDirectory();
                File dir = new File(storage.getAbsolutePath() + "/AutowarePictures");
                dir.mkdir();
                String fileName = "OwnerProfilePicture.jpg";
                File outFile = new File(dir, fileName);
                try {
                    FileOutputStream outStream = new FileOutputStream(outFile);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
                    outStream.flush();
                    outStream.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    Toast.makeText(getActivity().getApplicationContext(), "File Not Found:" + e.getMessage(), Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(getActivity().getApplicationContext(), "IO error", Toast.LENGTH_SHORT).show();
                }

                FirebaseFirestore db = FirebaseFirestore.getInstance();
                DocumentReference dref = db.collection("Owners").document(user.getUid());
                dref.set(new Owner(Name.getText().toString(),Garagename.getText().toString() ,Location.getText().toString(),address.getText().toString(),phone.getText().toString(),user.getEmail())).
                        addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Intent i = new Intent(getActivity(), HomeActivity.class);
                                getActivity().finish();
                                startActivity(i);
                                ImageView ProfileImage = nav.findViewById(R.id.drawer_ImageView);
                                File imgFile = new File(Environment.getExternalStorageDirectory(), "/AutowarePictures/OwnerProfilePicture.jpg");
                                Glide.with(getActivity().getApplicationContext()).load(imgFile).into(ProfileImage);
                                Toast.makeText(getActivity().getApplicationContext(), "User details saved", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getActivity().getApplicationContext(), "Operation Failure", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        Cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Navigate to customer home fragment
            }
        });
        return v;
    }

    private void pickImageFromGallery() {
        //intent to pick image
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_CODE);
    }

    //handle result of runtime permission
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_CODE: {
                if (grantResults.length > 0 && grantResults[0] ==
                        PackageManager.PERMISSION_GRANTED) {
                    //permission was granted
                    pickImageFromGallery();
                } else {
                    //permission was denied
                    Toast.makeText(getActivity().getApplicationContext(), "Permission denied...!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    //handle result of picked image
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == getActivity().RESULT_OK && requestCode == IMAGE_PICK_CODE) {
            //set image to image view
            ProfileImage.setImageURI(data.getData());

        }
    }
}