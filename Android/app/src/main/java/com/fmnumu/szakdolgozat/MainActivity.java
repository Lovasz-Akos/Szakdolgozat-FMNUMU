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
import com.fmnumu.szakdolgozat.ui.home.HomeFragment;
import com.google.android.material.navigation.NavigationView;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttToken;

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
    private String mqttAddress = "";

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

        SharedPreferences addressPref = getSharedPreferences("address", Context.MODE_PRIVATE);
        SharedPreferences usernamePref = getSharedPreferences("username", Context.MODE_PRIVATE);

        mqttAddress = addressPref.getString(getString(R.string.mqttAddressPersistent), null);
        username = usernamePref.getString(getString(R.string.usernamePersistent), null);

        /* FIXME auto connect on startup if there was an address saved beforehand
         ?  possible workaround for the async connection is wasting the user's time
         ?  by asking for their username on startup.
        if (mqttAddress != null && username != null) {
            connectMQTT(mqttAddress);
            //TODO wait for connection or 1 sec
            long startTime = System.currentTimeMillis();
            while ((System.currentTimeMillis() - startTime) < 1000) {
                Log.d("WAITING", " waiting until connection is established or 1 second pass");
            }
            emptyCardMemory();
            try {
                HomeFragment fragment = (HomeFragment) getSupportFragmentManager().findFragmentById(R.id.home_fragment);

                populatePersistentDataFields(username);
                saveUserAndServer(username, mqttAddress);
                fragment.subscribeAllTopics();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        */
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

    public void removeCardData(String topic, String cardType) {
        //TODO: find line with cardData and rewrite txt
        boolean found = false;
        int i = 0;

        do {
            String[] part = this.cardDataStore.get(i).split(":", 0);
            if ((part[0] + ":" + part[1]).equals(topic + ":" + cardType)) {
                this.cardDataStore.remove(i);
                found = true;
            }
            i++;
        }
        while (!found && i < cardDataStore.size());

        writeToFile(this.username + ".txt", this.cardDataStore);
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
        connectMQTT(this.mqttAddress);
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
                    mqttClient[0] = client;
                    Toast toast = Toast.makeText(getBaseContext(),
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

    public void populatePersistentDataFields(String username) throws IOException {                  //gets all the stored data from file and adds them to the runtime data storage
        SharedPreferences addressPref = getSharedPreferences("address", Context.MODE_PRIVATE);
        SharedPreferences usernamePref = getSharedPreferences("username", Context.MODE_PRIVATE);

        this.cardDataStore = readFile(username + ".txt");

        this.mqttAddress = addressPref.getString(getString(R.string.mqttAddressPersistent), null);
        this.username = usernamePref.getString(getString(R.string.usernamePersistent), null);

    }

    public void saveUserAndServer(String username, String mqttAddress) throws IOException {
        SharedPreferences addressPref = getSharedPreferences("address", Context.MODE_PRIVATE);
        SharedPreferences usernamePref = getSharedPreferences("username", Context.MODE_PRIVATE);

        this.username = username;
        this.mqttAddress = mqttAddress;

        addressPref.edit()
                .putString(getString(R.string.mqttAddressPersistent), this.mqttAddress)
                .commit();

        usernamePref.edit()
                .putString(getString(R.string.mqttAddressPersistent), this.username)
                .commit();


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


    public void reconnectToMQTT(MqttAndroidClient mqttAndroidClient) throws MqttException {
        mqttAndroidClient.connect();
        long startTime = System.currentTimeMillis();
        while (!mqttAndroidClient.isConnected() && (System.currentTimeMillis() - startTime) < 2000) {
            Log.d("WAITING", " waiting until connection is established or 2 seconds pass");
        }
        Toast toast = Toast.makeText(getBaseContext(),
                "Connected to " + mqttAddress, Toast.LENGTH_SHORT);
        toast.show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this,
                R.id.nav_host_fragment_content_main);

        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}