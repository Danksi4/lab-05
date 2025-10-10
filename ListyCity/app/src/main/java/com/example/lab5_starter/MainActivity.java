package com.example.lab5_starter;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements CityDialogFragment.CityDialogListener {

    private Button addCityButton;
    private Button deleteCityButton;
    private ListView cityListView;

    private ArrayList<City> cityArrayList;
    private ArrayAdapter<City> cityArrayAdapter;
    private FirebaseFirestore db;
    private CollectionReference citiesRef;
    private City selectedCity = null;

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

        // Set views
        addCityButton = findViewById(R.id.buttonAddCity);
        cityListView = findViewById(R.id.listviewCities);
        // Add the delete city button
        deleteCityButton = findViewById(R.id.buttonDeleteCity);

        // create city array
        cityArrayList = new ArrayList<>();
        cityArrayAdapter = new CityArrayAdapter(this, cityArrayList);
        cityListView.setAdapter(cityArrayAdapter);

        //addDummyData();
        db = FirebaseFirestore.getInstance();
        citiesRef = db.collection("cities");

        citiesRef.addSnapshotListener((value, error) -> {
            if (error != null)
            {
                Log.e("Firestore", error.toString());
            }
            if (value != null && !value.isEmpty())
            {
                cityArrayList.clear();
                for (QueryDocumentSnapshot snapshot : value)
                {
                    String name = snapshot.getString("name");
                    String province = snapshot.getString("province");

                    cityArrayList.add(new City(name, province));
                }
                cityArrayAdapter.notifyDataSetChanged();
            }
        });

        // set listeners
        addCityButton.setOnClickListener(view -> {
            CityDialogFragment cityDialogFragment = new CityDialogFragment();
            cityDialogFragment.show(getSupportFragmentManager(),"Add City");
        });

        deleteCityButton.setOnClickListener(view -> {
            if (selectedCity != null)
            {
                deleteCity(selectedCity);
                selectedCity = null;
            }
            else
            {
                Toast.makeText(this, "Select a city to delete", Toast.LENGTH_LONG).show();
            }
        });

        cityListView.setOnItemClickListener((adapterView, view, i, l) -> {
            City city = cityArrayAdapter.getItem(i);
            selectedCity = cityArrayAdapter.getItem(i);

            // Make sure the TA knows how to use the app lmao
            Toast.makeText(this, "Hold down a city to update it", Toast.LENGTH_LONG).show();
            if (city != null)
            {
                deleteCityButton.setText("Delete " + city.getName());
            }
            else
            {
                deleteCityButton.setText("Delete City");
            }
            // CityDialogFragment cityDialogFragment = CityDialogFragment.newInstance(city);
            // cityDialogFragment.show(getSupportFragmentManager(),"City Details");
        });

        // Use long click instead to update the items
        cityListView.setOnItemLongClickListener((adapterView, view, i, l) -> {
            City city = cityArrayAdapter.getItem(i);
            selectedCity = cityArrayAdapter.getItem(i);
            CityDialogFragment cityDialogFragment = CityDialogFragment.newInstance(city);
            cityDialogFragment.show(getSupportFragmentManager(),"City Details");
            return true;
        });

    }

    @Override
    public void updateCity(City city, String title, String year) {
        // Delete the old document
        String prevCityName = city.getName();
        DocumentReference docRef = citiesRef.document(prevCityName);
        docRef.delete();

        city.setName(title);
        city.setProvince(year);
        cityArrayAdapter.notifyDataSetChanged();

        // Updating the database using delete + addition
        // Add the new document
        DocumentReference newDocRef = citiesRef.document(city.getName());
        newDocRef.set(city);
    }

    @Override
    public void addCity(City city){
        cityArrayList.add(city);
        cityArrayAdapter.notifyDataSetChanged();

        DocumentReference docRef = citiesRef.document(city.getName());
        docRef.set(city);
    }

    @Override
    public void deleteCity(City city)
    {
        cityArrayList.remove(city);
        cityArrayAdapter.notifyDataSetChanged();

        DocumentReference docRef = citiesRef.document(city.getName());
        docRef.delete();
    }


    public void addDummyData(){
        City m1 = new City("Edmonton", "AB");
        City m2 = new City("Vancouver", "BC");
        cityArrayList.add(m1);
        cityArrayList.add(m2);
        cityArrayAdapter.notifyDataSetChanged();
    }
}