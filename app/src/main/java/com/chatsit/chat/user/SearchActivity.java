package com.chatsit.chat.user;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.chatsit.chat.NightMode;
import com.chatsit.chat.R;
import com.chatsit.chat.adapter.AdapterCreateChat;
import com.chatsit.chat.model.UserModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

@SuppressWarnings("ALL")
public class SearchActivity extends AppCompatActivity {

    //List
    RecyclerView recyclerView;
    List<UserModel> createModels;
    AdapterCreateChat adapterCreateChat;
    List<String> numberList;

    NightMode sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sharedPref = new NightMode(this);
        if (sharedPref.loadNightModeState()){
            setNight();
            setTheme(R.style.DarkTheme);
        }else  setDay();
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        recyclerView = findViewById(R.id.fragment_container);
        numberList = new ArrayList<>();
        createModels = new ArrayList<>();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        EditText editText = findViewById(R.id.search);
        editText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                search(editText.getText().toString());
                return true;
            }
            return false;
        });

        checkPermission();

    }

    public static String PhoneNumberWithoutCountryCode(String phoneNumberWithCountryCode){
        Pattern compile = Pattern.compile("\\+(?:998|996|995|994|993|992|977|976|975|974|973|972|971|970|968|967|966|965|964|963|962|961|960|886|880|856|855|853|852|850|692|691|690|689|688|687|686|685|683|682|681|680|679|678|677|676|675|674|673|672|670|599|598|597|595|593|592|591|590|509|508|507|506|505|504|503|502|501|500|423|421|420|389|387|386|385|383|382|381|380|379|378|377|376|375|374|373|372|371|370|359|358|357|356|355|354|353|352|351|350|299|298|297|291|290|269|268|267|266|265|264|263|262|261|260|258|257|256|255|254|253|252|251|250|249|248|246|245|244|243|242|241|240|239|238|237|236|235|234|233|232|231|230|229|228|227|226|225|224|223|222|221|220|218|216|213|212|211|98|95|94|93|92|91|90|86|84|82|81|66|65|64|63|62|61|60|58|57|56|55|54|53|52|51|49|48|47|46|45|44\\D?1624|44\\D?1534|44\\D?1481|44|43|41|40|39|36|34|33|32|31|30|27|20|7|1\\D?939|1\\D?876|1\\D?869|1\\D?868|1\\D?849|1\\D?829|1\\D?809|1\\D?787|1\\D?784|1\\D?767|1\\D?758|1\\D?721|1\\D?684|1\\D?671|1\\D?670|1\\D?664|1\\D?649|1\\D?473|1\\D?441|1\\D?345|1\\D?340|1\\D?284|1\\D?268|1\\D?264|1\\D?246|1\\D?242|1)\\D?");
        return phoneNumberWithCountryCode.replaceAll(compile.pattern(), "");
    }

    private void search(String query) {
        FirebaseFirestore.getInstance().collection("users").addSnapshotListener((value, error) -> {
            createModels.clear();
            for (DocumentSnapshot ds : Objects.requireNonNull(value)){

                 if (PhoneNumberWithoutCountryCode(Objects.requireNonNull(ds.get("phone")).toString()).equals(PhoneNumberWithoutCountryCode(query))  &&
                         !Objects.requireNonNull(ds.get("id")).toString().equals(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())){
                    createModels.add(ds.toObject(UserModel.class));
                }else {
                     for (String no : numberList){
                         if (PhoneNumberWithoutCountryCode(Objects.requireNonNull(ds.get("phone")).toString()).equals(PhoneNumberWithoutCountryCode(no))  &&
                                 !Objects.requireNonNull(ds.get("id")).toString().equals(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())){
                             if (Objects.requireNonNull(ds.get("name")).toString().toLowerCase().contains(query.toLowerCase()) || PhoneNumberWithoutCountryCode(Objects.requireNonNull(ds.get("phone")).toString()).equals(PhoneNumberWithoutCountryCode(query))){
                                 createModels.add(ds.toObject(UserModel.class));
                             }
                             break;
                         }
                     }
                 }

                adapterCreateChat = new AdapterCreateChat(this, createModels);
                recyclerView.setAdapter(adapterCreateChat);
                adapterCreateChat.notifyDataSetChanged();
                if (adapterCreateChat.getItemCount() == 0){
                    findViewById(R.id.nothing).setVisibility(View.VISIBLE);
                }else {
                    findViewById(R.id.nothing).setVisibility(View.GONE);
                }
            }
        });
    }

    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(SearchActivity.this,
                Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(SearchActivity.this, new String[]{Manifest.permission.READ_CONTACTS}, 100);
        }else {
            getAllUsers();
        }
    }

    private void getAllUsers() {

        ContentResolver contentResolver = getContentResolver();
        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        @SuppressLint("Recycle") Cursor cursor = contentResolver.query(uri, null, null, null,null);
        if (cursor.getCount() > 0){
            while (cursor.moveToNext()){
                @SuppressLint("Range") String contactNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                numberList.add(contactNumber);
            }
            getAllUsersFromDatabase();
        }
    }

    private void getAllUsersFromDatabase() {

        FirebaseFirestore.getInstance().collection("users").addSnapshotListener((value, error) -> {
            createModels.clear();
            for (DocumentSnapshot ds : Objects.requireNonNull(value)){
                for (String no : numberList){
                    if (PhoneNumberWithoutCountryCode(Objects.requireNonNull(ds.get("phone")).toString()).equals(PhoneNumberWithoutCountryCode(no)) &&
                            !Objects.requireNonNull(ds.get("id")).toString().equals(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())){
                        createModels.add(ds.toObject(UserModel.class));
                        break;
                    }
                }
                adapterCreateChat = new AdapterCreateChat(this, createModels);
                recyclerView.setAdapter(adapterCreateChat);
                adapterCreateChat.notifyDataSetChanged();
                if (adapterCreateChat.getItemCount() == 0){
                    findViewById(R.id.nothing).setVisibility(View.VISIBLE);
                }else {
                    findViewById(R.id.nothing).setVisibility(View.GONE);
                }
            }
        });

    }

    private void setDay() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            Drawable background = getResources().getDrawable(R.drawable.gradient_bg);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(android.R.color.transparent));
            window.setBackgroundDrawable(background);
        }
    }

    private void setNight() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            Drawable background = getResources().getDrawable(R.drawable.gradient_bg_night);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(android.R.color.transparent));
            window.setBackgroundDrawable(background);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            getAllUsers();
        }else {
            Snackbar.make(findViewById(R.id.main), "Permission is needed", Snackbar.LENGTH_LONG).show();
          checkPermission();
        }
    }
}