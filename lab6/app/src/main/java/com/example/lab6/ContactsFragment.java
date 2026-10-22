package com.example.lab6;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lab6.adapter.ContactAdapter;
import com.example.lab6.model.ContactModel;
import com.example.lab6.utils.SampleDataGenerator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ContactsFragment extends Fragment implements ContactAdapter.OnContactClickListener {

    private RecyclerView rvContacts;
    private ContactAdapter adapter;
    private ImageButton btnAddContact;
    private ProgressBar pbLoading;
    private LinearLayout llEmpty;
    private Button btnCreateSampleContacts;

    private final List<ContactModel> contactList = new ArrayList<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final ActivityResultLauncher<String[]> requestPermissionsLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), permissions -> {
                Boolean readContacts = permissions.getOrDefault(Manifest.permission.READ_CONTACTS, false);
                if (Boolean.TRUE.equals(readContacts)) {
                    loadContacts();
                } else {
                    Toast.makeText(getContext(), "Cần cấp quyền đọc danh bạ để hiển thị danh sách!", Toast.LENGTH_SHORT).show();
                }
            });

    private final ActivityResultLauncher<Intent> addContactLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    loadContacts();
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_contacts, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvContacts = view.findViewById(R.id.rvContacts);
        EditText etSearch = view.findViewById(R.id.etSearch);
        btnAddContact = view.findViewById(R.id.btnAddContact);
        pbLoading = view.findViewById(R.id.pbLoadingContacts);
        llEmpty = view.findViewById(R.id.llEmptyContacts);
        btnCreateSampleContacts = view.findViewById(R.id.btnCreateSampleContacts);

        rvContacts.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ContactAdapter(this);
        rvContacts.setAdapter(adapter);

        btnAddContact.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), AddContactActivity.class);
            addContactLauncher.launch(intent);
        });

        btnCreateSampleContacts.setOnClickListener(v -> {
            SampleDataGenerator.generateSampleContacts(requireContext());
            loadContacts();
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (adapter != null) {
                    adapter.filter(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        checkPermissionsAndLoadContacts();
    }

    private void checkPermissionsAndLoadContacts() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            loadContacts();
        } else {
            requestPermissionsLauncher.launch(new String[]{
                    Manifest.permission.READ_CONTACTS,
                    Manifest.permission.WRITE_CONTACTS,
                    Manifest.permission.CALL_PHONE
            });
        }
    }

    private void loadContacts() {
        pbLoading.setVisibility(View.VISIBLE);
        llEmpty.setVisibility(View.GONE);

        executor.execute(() -> {
            List<ContactModel> list = new ArrayList<>();
            if (getContext() == null) return;

            Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
            String[] projection = new String[]{
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER,
                    ContactsContract.CommonDataKinds.Phone.PHOTO_URI
            };
            String sortOrder = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC";

            Cursor cursor = requireContext().getContentResolver().query(uri, projection, null, null, sortOrder);

            if (cursor != null) {
                int idIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID);
                int nameIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                int numIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                int photoIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI);

                while (cursor.moveToNext()) {
                    String id = idIdx >= 0 ? cursor.getString(idIdx) : "";
                    String name = nameIdx >= 0 ? cursor.getString(nameIdx) : "";
                    String phone = numIdx >= 0 ? cursor.getString(numIdx) : "";
                    String photo = photoIdx >= 0 ? cursor.getString(photoIdx) : null;

                    if (name != null && !name.trim().isEmpty()) {
                        list.add(new ContactModel(id, name, phone, photo));
                    }
                }
                cursor.close();
            }

            // Sort alphabetically A-Z
            Collections.sort(list, (c1, c2) -> c1.getDisplayName().compareToIgnoreCase(c2.getDisplayName()));

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    contactList.clear();
                    contactList.addAll(list);
                    adapter.setContacts(contactList);
                    pbLoading.setVisibility(View.GONE);

                    if (contactList.isEmpty()) {
                        llEmpty.setVisibility(View.VISIBLE);
                    } else {
                        llEmpty.setVisibility(View.GONE);
                    }
                });
            }
        });
    }

    @Override
    public void onContactClick(ContactModel contact) {
        makeCall(contact.getPhoneNumber());
    }

    @Override
    public void onCallClick(ContactModel contact) {
        makeCall(contact.getPhoneNumber());
    }

    private void makeCall(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            Toast.makeText(getContext(), "Số điện thoại không hợp lệ!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.setData(Uri.parse("tel:" + phoneNumber));
            startActivity(intent);
        } else {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + phoneNumber));
            startActivity(intent);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}