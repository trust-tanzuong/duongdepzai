package com.example.lab6;

import android.content.ContentProviderOperation;
import android.content.OperationApplicationException;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;

public class AddContactActivity extends AppCompatActivity {

    private EditText etFirstName, etLastName, etPhoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);

        // Handle SystemBars and Keyboard (IME) insets to prevent soft keyboard obscuring fields
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.rootAddContact), (v, insets) -> {
            Insets systemInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
            v.setPadding(systemInsets.left, systemInsets.top, systemInsets.right, systemInsets.bottom);
            return insets;
        });

        ImageButton btnBack = findViewById(R.id.btnBack);
        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        Button btnSave = findViewById(R.id.btnSave);

        btnBack.setOnClickListener(v -> finish());

        btnSave.setOnClickListener(v -> saveContact());
    }

    private void saveContact() {
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String phoneNumber = etPhoneNumber.getText().toString().trim();

        if (TextUtils.isEmpty(firstName) && TextUtils.isEmpty(lastName)) {
            Toast.makeText(this, "Vui lòng nhập họ hoặc tên!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(phoneNumber)) {
            Toast.makeText(this, "Vui lòng nhập số điện thoại!", Toast.LENGTH_SHORT).show();
            return;
        }

        ArrayList<ContentProviderOperation> ops = new ArrayList<>();

        // 1. RawContact insert
        ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                .build());

        // 2. Name insert
        ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, firstName)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME, lastName)
                .build());

        // 3. Phone insert
        ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phoneNumber)
                .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                .build());

        try {
            getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
            Toast.makeText(this, "Đã thêm liên hệ thành công!", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        } catch (OperationApplicationException | RemoteException e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi khi lưu liên hệ: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}