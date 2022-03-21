package com.fmnumu.szakdolgozat;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.fmnumu.szakdolgozat.databinding.ActivityMainBinding;
import com.google.android.material.navigation.NavigationView;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private final String clientId = MqttClient.generateClientId();
    private final List<String> cardDataStore = new ArrayList<>();
    //adding items to this list does not create new interaction types,
    // new types will need their own xml layout and cases added in HomeFragment.subscribeMQTT method
    private final List<String> allInteractTypes =
            new ArrayList<>(Arrays.asList("Text", "Switch", "Button",
                    "Checkbox", "Input", "Slider"));
    private final MqttAndroidClient[] connectMQTT = new MqttAndroidClient[1];
    private AppBarConfiguration mAppBarConfiguration;
    private String mqttAddress;

    public MqttAndroidClient getClient() {
        return connectMQTT[0];
    }

    public void addCardDataToPersistentStorage(String topic, String cardType, String cardData) {
        boolean found = false;
        int i = 0;

        if (cardDataStore.size() == 0) {
            this.cardDataStore.add(topic + ":" + cardType + ":" + cardData);                        //basically init. the list
        }
        do {
            String[] part = this.cardDataStore.get(i).split(":", 0);
            if ((part[0] + ":" + part[1]).equals(topic + ":" + cardType)) {                         //only add cards that are not duplicates
                this.cardDataStore.set(i, topic + ":" + cardType + ":" + cardData);
                found = true;
            }
            i++;
        }
        while (!found && i < cardDataStore.size());

        if (!found) {                                                                               //if no duplicates were found, add the new card
            this.cardDataStore.add(topic + ":" + cardType + ":" + cardData);
        }
    }

    public void removedCardData(String cardData) {
        if (this.cardDataStore.contains(cardData)) {
            cardDataStore.remove(cardData);
        }
    }

    public List<String> getCardDataStoreAll() {
        return this.cardDataStore;
    }

    public List<String> getAllInteractTypes() {
        return allInteractTypes;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        com.fmnumu.szakdolgozat.databinding.ActivityMainBinding binding =
                ActivityMainBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMain.toolbar);

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_publish, R.id.nav_connection)
                .setOpenableLayout(drawer)
                .build();

        NavController navController = Navigation.findNavController(this,
                R.id.nav_host_fragment_content_main);

        NavigationUI.setupActionBarWithNavController(this,
                navController,
                mAppBarConfiguration);

        NavigationUI.setupWithNavController(navigationView, navController);

        populatePersistentDataFields();
    }

    public void connectMQTT() {
        this.connectMQTT(this.mqttAddress);                                                         //simplified connect call that uses the previously stored address
    }

    public void connectMQTT(String mqttAddress) {
        this.mqttAddress = mqttAddress;

        MqttAndroidClient client = new MqttAndroidClient(this.getApplicationContext(),
                "tcp://" + mqttAddress + ":1883", clientId);

        try {
            client.connect(null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {

                    Log.d("CONNECTION", "onSuccess");
                    connectMQTT[0] = client;

                    Toast toast = Toast.makeText(getApplicationContext(),
                            "Connected to " + mqttAddress, Toast.LENGTH_SHORT);

                    toast.show();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "Failed to connect to " + mqttAddress, Toast.LENGTH_SHORT);

                    toast.show();
                }
            });

        } catch (MqttException e) {
            Log.d("CONNECTION", "ERROR");
        }
    }

    private void populatePersistentDataFields() {                                                   //gets all the stored data from file and adds them to the runtime data storage
        //TODO: read from file
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this,
                R.id.nav_host_fragment_content_main);

        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}