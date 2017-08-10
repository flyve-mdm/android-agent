package org.flyve.mdm.agent;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import org.flyve.mdm.agent.core.user.UserController;
import org.flyve.mdm.agent.core.user.UserModel;
import org.flyve.mdm.agent.utils.FlyveLog;
import org.flyve.mdm.agent.utils.Helpers;
import org.flyve.mdm.agent.utils.InputValidatorHelper;
import org.flyve.mdm.agent.utils.MultipleEditText;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/*
 *   Copyright © 2017 Teclib. All rights reserved.
 *
 *   This file is part of flyve-mdm-android-agent
 *
 * flyve-mdm-android-agent is a subproject of Flyve MDM. Flyve MDM is a mobile
 * device management software.
 *
 * Flyve MDM is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * Flyve MDM is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * ------------------------------------------------------------------------------
 * @author    Rafael Hernandez
 * @date      31/7/17
 * @copyright Copyright © 2017 Teclib. All rights reserved.
 * @license   GPLv3 https://www.gnu.org/licenses/gpl-3.0.html
 * @link      https://github.com/flyve-mdm/flyve-mdm-android-agent
 * @link      https://flyve-mdm.com
 * ------------------------------------------------------------------------------
 */
public class EditUserActivity extends AppCompatActivity {

    private TextView txtMessage;
    private EditText editName;
    private EditText editLastName;
    private EditText editAdministrative;
    private ImageView imgPhoto;
    private UserModel user;
    private MultipleEditText editEmail;
    private MultipleEditText editPhone;
    private Spinner spinnerLanguage;
    private int REQUEST_CAMERA = 0, SELECT_FILE = 1;
    private String strPicture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_form);

        user = new UserController(EditUserActivity.this).getCache();

        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
        }

        imgPhoto = (ImageView) findViewById(R.id.imgPhoto);
        if(!user.getPicture().equals("")) {
            imgPhoto.setImageBitmap(Helpers.StringToBitmap(user.getPicture()));
        }

        ImageView btnCamera = (ImageView) findViewById(R.id.btnCamera);
        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

        TextView txtTitle = (TextView) findViewById(R.id.txtTitle);
        txtTitle.setText("");

        txtMessage = (TextView) findViewById(R.id.txtMessage);

        editName = (EditText) findViewById(R.id.editName);
        editName.setText( user.getFirstName() );

        editLastName = (EditText) findViewById(R.id.editLastName);
        editLastName.setText( user.getLastName() );

        // Multiples Emails
        LinearLayout lnEmails = (LinearLayout) findViewById(R.id.lnEmails);
        editEmail = new MultipleEditText(this, lnEmails, getResources().getString(R.string.email));
        editEmail.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        editEmail.setSpinnerArray(R.array.email_array);

        // load store values
        List<String> arrEmails = new ArrayList<>();
        List<String> arrEmailTypes = new ArrayList<>();

        for(int i = 0; i < user.getEmails().size(); i++) {
            arrEmails.add( user.getEmails().get(i).getEmail() );
            arrEmailTypes.add( user.getEmails().get(i).getType() );
        }

        editEmail.setValue( arrEmails, arrEmailTypes );
        lnEmails.addView( editEmail.createEditText() );

        // 3 Phones
        LinearLayout lnPhones = (LinearLayout) findViewById(R.id.lnPhones);
        editPhone = new MultipleEditText(this, lnPhones, getResources().getString(R.string.phone));
        editPhone.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_PHONE);
        editPhone.setLimit(3);
        editPhone.setSpinnerArray(R.array.phone_array);

        // load store values
        List<String> arrPhones = new ArrayList<>();
        List<String> arrPhoneTypes = new ArrayList<>();

        if(!user.getMobilePhone().equals("")) {
            arrPhones.add(user.getMobilePhone());
            arrPhoneTypes.add("");
        }

        if(!user.getPhone().equals("")) {
            arrPhones.add(user.getPhone());
            arrPhoneTypes.add("");
        }

        if(!user.getPhone2().equals("")) {
            arrPhones.add(user.getPhone2());
            arrPhoneTypes.add("");
        }

        editPhone.setValue( arrPhones, arrPhoneTypes );
        lnPhones.addView( editPhone.createEditText() );

        // Language
        spinnerLanguage = (Spinner) findViewById(R.id.spinnerLanguage);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.language_array, android.R.layout.simple_spinner_item);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        spinnerLanguage.setAdapter(adapter);

        // select language stored on cache
        int spinnerPosition = adapter.getPosition(user.getLanguage());
        spinnerLanguage.setSelection(spinnerPosition);

        editAdministrative = (EditText) findViewById(R.id.editAdministrative);
        editAdministrative.setText(user.getAdministrativeNumber());
        editAdministrative.setImeOptions(EditorInfo.IME_ACTION_DONE);
        editAdministrative.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    validateForm();
                    return true;
                }
                return false;
            }
        });

        // Button Register
        ImageView btnRegister = (ImageView) findViewById(R.id.btnSave);
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateForm();
            }
        });
    }

    /**
     * Storage information
     */
    private void save() {

        // -------------
        // Emails
        // -------------
        ArrayList<UserModel.EmailsData> arrEmails = new ArrayList<>();
        UserModel.EmailsData emails = new UserModel().new EmailsData();

        List<EditText> emailEdit = editEmail.getEditList();
        List<Spinner> emailTypeEdit = editEmail.getSpinnList();

        for (int i=0; i<emailEdit.size(); i++) {
            EditText editText = emailEdit.get(i);
            Spinner spinner = emailTypeEdit.get(i);

            if(!editText.getText().toString().equals("")) {
                emails.setEmail(editText.getText().toString());
                emails.setType(spinner.getSelectedItem().toString());
                arrEmails.add(emails);
            }
        }

        // -------------
        // USER
        // -------------
        user = new UserModel();

        user.setFirstName( editName.getText().toString() );
        user.setLastName( editLastName.getText().toString() );
        user.setEmails(arrEmails);

        // Mobile Phone
        if(!editPhone.getEditList().isEmpty()) {
            String mobilePhone = editPhone.getEditList().get(0).getText().toString();
            if (!mobilePhone.equals("")) {
                user.setMobilePhone(mobilePhone);
            }
        }

        // Phone
        if(editPhone.getEditList().size() > 1) {
            String phone = editPhone.getEditList().get(1).getText().toString();
            if (!phone.equals("")) {
                user.setPhone(phone);
            }
        }

        // Phone 2
        if(editPhone.getEditList().size() > 2) {
            String phone2 = editPhone.getEditList().get(2).getText().toString();
            if (!phone2.equals("")) {
                user.setPhone(phone2);
            }
        }

        user.setPicture(strPicture);
        user.setLanguage( spinnerLanguage.getSelectedItem().toString() );
        user.setAdministrativeNumber( editAdministrative.getText().toString() );

        new UserController(EditUserActivity.this).save(user);

        Helpers.snack( EditUserActivity.this, "Saved" );
    }

    private void selectImage() {
        final CharSequence[] items = { "Take Photo", "Choose from Library",
                "Cancel" };

        AlertDialog.Builder builder = new AlertDialog.Builder(EditUserActivity.this);
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {

                if (items[item].equals("Take Photo")) {
                    cameraIntent();

                } else if (items[item].equals("Choose from Library")) {
                    galleryIntent();

                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    private void galleryIntent() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);//
        startActivityForResult(Intent.createChooser(intent, "Select File"),SELECT_FILE);
    }

    private void cameraIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_FILE)
                onSelectFromGalleryResult(data);
            else if (requestCode == REQUEST_CAMERA)
                onCaptureImageResult(data);
        }
    }

    private void onCaptureImageResult(Intent data) {
        Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes);

        File destination = new File(Environment.getExternalStorageDirectory(),
                System.currentTimeMillis() + ".jpg");

        FileOutputStream fo = null;
        try {
            destination.createNewFile();
            fo = new FileOutputStream(destination);
            fo.write(bytes.toByteArray());
            fo.close();
        } catch (Exception e) {
            FlyveLog.e(e.getMessage());
        } finally {
            if(fo!=null) {
                try {
                    fo.close();
                } catch (Exception ex) {
                    FlyveLog.d(ex.getMessage());
                }
            }
        }

        strPicture = Helpers.BitmapToString(thumbnail);
        imgPhoto.setImageBitmap(thumbnail);
    }

    private void onSelectFromGalleryResult(Intent data) {
        Bitmap bm=null;
        if (data != null) {
            try {
                bm = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());
            } catch (IOException e) {
                FlyveLog.e(e.getMessage());
            }
        }

        strPicture = Helpers.BitmapToString(bm);
        imgPhoto.setImageBitmap(bm);
    }

    /**
     * Send information to validateForm
     */
    private void validateForm() {
        StringBuilder errMsg = new StringBuilder("Please fix the following errors and try again.\n\n");
        txtMessage.setText("");

        // Hide keyboard
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

        //Validate and Save
        boolean allowSave = true;

        String name = editName.getText().toString().trim();
        String lastName = editLastName.getText().toString().trim();

        // Name
        if (InputValidatorHelper.isNullOrEmpty(name)) {
            errMsg.append("- First name should not be empty.\n");
            allowSave = false;
        }

        // Last name
        if (InputValidatorHelper.isNullOrEmpty(lastName)) {
            errMsg.append("- Last name should not be empty.\n");
            allowSave = false;
        }

        if(editEmail.getEditList().isEmpty()) {
            errMsg.append("- Please add one email at least.\n");
            allowSave = false;
        }

        if(allowSave){
            save();
        } else {
            txtMessage.setText(errMsg);
        }
    }
}
