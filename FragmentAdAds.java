package com.example.bumobile.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.bumobile.R;
import com.example.bumobile.models.AdPhoto;
import com.example.bumobile.models.AdsAdd;
import com.example.bumobile.models.Categories;
import com.example.bumobile.models.Token;
import com.example.bumobile.services.ByteConverter;
import com.example.bumobile.services.RequestBuilder;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class FragmentAdAds extends Fragment {

    String imageBytesString;
    public EditText manufacturer;
    public EditText model;
    public EditText description;
    public EditText address;
    public EditText year;
    public EditText price;
    public RadioButton guarantee;
    public RadioButton check;
    public RadioButton box;
    public Spinner category;
    public Button add, addphotos;
    int idad;
    int PICK_IMAGE_MULTIPLE = 1;
    public ArrayList<AdPhoto> photos = new ArrayList<AdPhoto>();
    Map<String, Categories> categoriesMap = new HashMap<>();

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ad_ads, container, false);
        Context context = getContext();

        setViews(view);

        requestCategories(context);

        setListeners(context);

        return view;
    }

    /**
     * Инициализация элементов интерфейса
     * @param view
     */
    public void setViews(View view) {
        manufacturer = view.findViewById(R.id.ads_manufacturer);
        model = view.findViewById(R.id.ads_model);
        description = view.findViewById(R.id.ads_description);
        address = view.findViewById(R.id.ads_address);
        year = view.findViewById(R.id.ads_year);
        guarantee = view.findViewById(R.id.ads_guarantee);
        check = view.findViewById(R.id.ads_check);
        box = view.findViewById(R.id.ads_box);
        category = view.findViewById(R.id.ads_category);
        price = view.findViewById(R.id.ads_price);
        add = view.findViewById(R.id.add_ad);
        addphotos = view.findViewById(R.id.add_adphotos);
    }

    /**
     * Получение категорий с сервера
     * @param context
     */
    private void requestCategories(Context context) {
        RequestBuilder.getApiClient().getCategories().enqueue(new Callback<ArrayList<Categories>>() {
            @Override
            public void onResponse(@NonNull Call<ArrayList<Categories>> call, @NonNull Response<ArrayList<Categories>> response) {
                if (response.isSuccessful()) {
                    for (Categories cat : response.body()) {
                        categoriesMap.put(cat.categoryName,cat);
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_dropdown_item, categoriesMap.keySet().stream().collect(Collectors.toList()));
                    category.setAdapter(adapter);
                } else Log.e("Error", response.message());
            }

            @Override
            public void onFailure(@NonNull Call<ArrayList<Categories>> call, @NonNull Throwable t) {
                Log.e("Error", t.getMessage());
            }
        });
    }

    /**
     * Установка обработчиков нажатия
     * @param context
     */
    private void setListeners(Context context) {
        add.setOnClickListener(v -> {
            AdsAdd ad = createAd();
            sendDataToServer(context, ad);
        });
        addphotos.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_MULTIPLE);
        });
    }

    /**
     * Создание объявления
     * @return
     */
    public AdsAdd createAd() {
        AdsAdd ad = new AdsAdd();
        ad.setManufacturerName(manufacturer.getText().toString());
        ad.setDescription(description.getText().toString());
        ad.setModel(model.getText().toString());
        ad.setIdCategory(category.getSelectedItem().toString());
        ad.setProductionYear(year.getText().toString());
        ad.setBox(box.isSelected());
        ad.setCheck(check.isSelected());
        ad.setGuarantee(guarantee.isSelected());
        Integer pr = Integer.parseInt(price.getText().toString());
        ad.setPrice(pr);
        ad.setIdStatus(1);
        ad.setPhotos(photos);
        ad.setAddressField(address.getText().toString());
        return ad;
    }

    /**
     * Отправка объявления на сервер
     * @param context
     * @param ad
     */
    private void sendDataToServer(Context context, AdsAdd ad) {
        RequestBuilder.getApiClient().postAd(Token.getToken().replace("Bearer ", ""), ad).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(context.getApplicationContext(), "Объявление добавлено", Toast.LENGTH_SHORT).show();
                } else Log.e("Error", response.message());
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                Log.e("Error", t.getMessage());
            }
        });
    }

    /**
     * обработка окна галереи
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            if (requestCode == PICK_IMAGE_MULTIPLE) {
                if (resultCode == Activity.RESULT_OK) {
                    handleSelectedImages(data);
                } else if (data.getData() != null) {
                    String imagePath = data.getData().getPath();
                }
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "Something went wrong", Toast.LENGTH_LONG).show();
            Log.e("Error", e.getMessage().toString());
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Обработка фотографий
     * @param data
     * @throws FileNotFoundException
     */
    private void handleSelectedImages(Intent data) throws FileNotFoundException {
        if (data.getClipData() != null) {
            int count = data.getClipData().getItemCount();
            for (int i = 0; i < count; i++) {
                Uri imageUri = data.getClipData().getItemAt(i).getUri();
                Bitmap bitmap = BitmapFactory.decodeStream(getContext().getContentResolver().openInputStream(imageUri));
                byte[] imageBytes = new ByteConverter().toByteArray(bitmap);
                imageBytesString = Base64.getEncoder().encodeToString(imageBytes);
                double imageSizeInKb = imageBytes.length / 1024;
                if (imageSizeInKb >= 1000000000) {
                    Log.e("Warning", "Слишком большая картинка");
                } else {
                    AdPhoto photo = new AdPhoto();
                    photo.setIdAd(0);
                    photo.setPhoto(imageBytesString);
                    photos.add(photo);
                }
            }
        }
    }
}

