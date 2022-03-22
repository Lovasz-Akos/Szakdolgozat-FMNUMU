package com.fmnumu.szakdolgozat;

import android.content.Context;
import android.content.SharedPreferences;
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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {

    private final String clientId = MqttClient.generateClientId();
    private final List<String> allInteractTypes = new ArrayList<>(Arrays.asList("Text", "Switch", "Button", "Checkbox", "Input", "Slider"));
    private final MqttAndroidClient[] mqttClient = new MqttAndroidClient[1];
    private List<String> cardDataStore = new ArrayList<>();
    private String username = "";
    private AppBarConfiguration mAppBarConfiguration;
    private String mqttAddress;

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

    }

    public MqttAndroidClient getClient() {
        return mqttClient[0];
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

        writeToFile(this.username + ".txt", this.cardDataStore);
    }

    public void removedCardData(String cardData) {
        //TODO REMOVE CARD FROM cardDataStore
    }

    public void emptyCardMemory() {
        cardDataStore.clear();
    }

    public List<String> getCardDataStoreAll() {
        return this.cardDataStore;
    }

    public List<String> getAllInteractTypes() {
        return allInteractTypes;
    }

    public void connectMQTT() {
        this.connectMQTT(this.mqttAddress, true);                                                   //simplified connect call that uses the previously stored address
    }

    public void connectMQTT(String mqttAddress) {
        connectMQTT(mqttAddress, false);
    }
    public void connectMQTT(String mqttAddress, boolean isReconnect) {
        this.mqttAddress = mqttAddress;

        MqttAndroidClient client = new MqttAndroidClient(this.getApplicationContext(),
                "tcp://" + mqttAddress + ":1883", clientId);

        try {
            client.connect(null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d("CONNECTION", "onSuccess");
                    mqttClient[0] = client;
                    Toast toast = Toast.makeText(getBaseContext(),
                            "Connected to " + mqttAddress, Toast.LENGTH_SHORT);
                    toast.show();
                    if (isReconnect){

                    }
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

    public void populatePersistentDataFields(String username) throws IOException {                  //gets all the stored data from file and adds them to the runtime data storage
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);

        this.username = username;
        this.cardDataStore = readFile(username + ".txt");

        String defaultValue = getResources().getString(R.string._192_168_0_200);
        this.mqttAddress = sharedPref.getString(getString(R.string.mqttAdrr), defaultValue);
    }

    public void saveMqttAddress() throws IOException {
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.mqttAdrr), this.mqttAddress);
        editor.apply();
    }

    public void writeToFile(String filename, List<String> data) {
        try {
            FileWriter writer = new FileWriter(getFilesDir() + "/" + filename);
            for (int i = 0; i < data.size(); i++) {
                writer.write(data.get(i) + System.lineSeparator());
            }
            writer.flush();
            writer.close();
        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.getMessage());
        }
    }

    public List<String> readFile(String filename) throws IOException {
        Scanner s = new Scanner(new File(getFilesDir() + "/" + filename)).useDelimiter(System.lineSeparator());
        ArrayList<String> list = new ArrayList<>();
        File user = new File(getFilesDir() + "/" + filename);
        if (user.length() == 0) {
            Log.d("TAG", "readFile: ");
        }

        while (s.hasNext()) {
            list.add(s.next());
        }
        s.close();
        return list;
    }

    public void listAllFiles() {
        File folder = new File(getFilesDir() + "/");
        File[] listOfFiles = folder.listFiles();
        Log.d("LOOKUP", "listAllFiles: ");
        for (File listOfFile : listOfFiles) {
            if (listOfFile.isFile()) {
                Log.d("LOOKUP", "Files: " + listOfFile.getName());
            } else if (listOfFile.isDirectory()) {
                Log.d("LOOKUP", "Dirs: " + listOfFile.getName());
            }
        }
    }

    public void deleteAllFiles() {
        File folder = new File(getFilesDir() + "/");
        File[] listOfFiles = folder.listFiles();

        for (int i = 0; i < listOfFiles.length; i++) {
            listOfFiles[i].delete();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this,
                R.id.nav_host_fragment_content_main);

        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}