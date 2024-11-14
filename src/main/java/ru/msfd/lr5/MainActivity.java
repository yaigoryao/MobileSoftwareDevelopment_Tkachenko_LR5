package ru.msfd.lr5;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.opengl.Matrix;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.Comparator;

public class MainActivity extends AppCompatActivity {

    static final int totalRows = 7;
    ListView contactsInfoListView;
    SimpleCursorAdapter contactsInfoListAdapter;
    private final ActivityResultLauncher<String> readContactsPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted)
                {
                    Toast.makeText(MainActivity.this, "Разрешение получено", Toast.LENGTH_SHORT).show();
                    Setup();
                }
                else Toast.makeText(MainActivity.this, "Разрешение отклонено", Toast.LENGTH_SHORT).show();
            });

    final String[] columns = new String[]
            {
                    "_id",
                    "name_col",
                    "full_name_col",
                    "email_col",
                    "home_phone_col",
                    "mobile_phone_col",
            };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        CheckPermissions();
    }

    private void Setup()
    {
        SetupViewReferences();
        SetupList();
    }

    private void SetupViewReferences()
    {
        contactsInfoListView = findViewById(R.id.contacts_info_list_lv);
    }

    private void SetupList()
    {
        if(contactsInfoListView != null)
        {
            Cursor contactInfoCursor = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
            ArrayList<ContactInfo> contactsInfo = new ArrayList<>();
            if(contactInfoCursor != null)
            {
                MatrixCursor matrixCursor = new MatrixCursor(columns);
                Log.d("123", "Matrix Cursor initialized");
                int idColIndex = contactInfoCursor.getColumnIndex(ContactsContract.CommonDataKinds.Im._ID);
                int contactNameColIndex = contactInfoCursor.getColumnIndex(ContactsContract.CommonDataKinds.Im.DISPLAY_NAME);
                int hasPhoneColIndex = contactInfoCursor.getColumnIndex(ContactsContract.CommonDataKinds.Identity.HAS_PHONE_NUMBER);

                while(contactInfoCursor.moveToNext())
                {
                    int id = contactInfoCursor.getInt(idColIndex);
                    String displayName = contactInfoCursor.getString(contactNameColIndex);
                    int hasPhone = contactInfoCursor.getInt(hasPhoneColIndex);

                    Cursor emailCursor = getContentResolver().query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
                            ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?", new String[] { String.valueOf(id) }, null);
                    Cursor phoneCursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[] { String.valueOf(id) }, null);
                    Cursor fullNameCursor = getContentResolver().query(ContactsContract.Data.CONTENT_URI, null,
                            ContactsContract.Data.CONTACT_ID + " = ?", new String[] { String.valueOf(id) }, null);
                    String email = "Email отсутствует";
                    if(emailCursor != null && emailCursor.moveToNext())
                    {
                        int emailColIndex = emailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA);
                        email = emailCursor.getString(emailColIndex);
                        emailCursor.close();
                    }

                    String homePhone = "Домашний телефон отсутствует";
                    String mobilePhone = "Мобильный телефон отсутствует";
                    if(phoneCursor != null)
                    {
                        int numberColIndex = phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                        int typeColIndex = phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE);

                        while (phoneCursor.moveToNext())
                        {
                            String number = phoneCursor.getString(numberColIndex);
                            int phoneType = phoneCursor.getInt(typeColIndex);

                            switch (phoneType)
                            {
                                case ContactsContract.CommonDataKinds.Phone.TYPE_HOME:
                                    homePhone = number;

                                    break;
                                case ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE:
                                    mobilePhone = number;

                                    break;
                                default: break;
                            }
                        }
                        phoneCursor.close();
                    }

                    String name = "", surname = "", patronymics = "";
                    if(fullNameCursor != null && fullNameCursor.moveToNext())
                    {
                        int nameColIndex = fullNameCursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME);
                        int surnameColIndex = fullNameCursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.MIDDLE_NAME);
                        int patronymicsColIndex = fullNameCursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME);
                        name = fullNameCursor.getString(nameColIndex);
                        surname = fullNameCursor.getString(surnameColIndex);
                        patronymics = fullNameCursor.getString(patronymicsColIndex);
                        fullNameCursor.close();
                    }

                    contactsInfo.add(new ContactInfo(id, displayName, name + " " + surname + " " + patronymics, email, homePhone, mobilePhone));
                }

                contactsInfo.sort(Comparator.comparing(c -> c.name));
                contactsInfo.forEach(c -> matrixCursor.addRow(new Object[]
                        {
                                c.id,
                                c.name,
                                c.fullName,
                                c.email,
                                c.homePhone,
                                c.mobilePhone
                        }));
                contactsInfo.clear();
                contactInfoCursor.close();

                contactsInfoListAdapter = new SimpleCursorAdapter(this, R.layout.contact_info_item, matrixCursor,
                        columns, new int[] { R.id.id_tv, R.id.name_tv, R.id.fio_tv, R.id.email_tv, R.id.home_phone_tv, R.id.mobile_phone_tv }, 0);
                contactsInfoListView.setAdapter(contactsInfoListAdapter);
            }
        }
    }

    private void CheckPermissions()
    {
        if(checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) readContactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS);
        Setup();
    }
}