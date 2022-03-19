package com.fmnumu.szakdolgozat;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.fmnumu.szakdolgozat.databinding.ActivityMainBinding;
import com.google.android.material.snackbar.Snackbar;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private final String clientId = MqttClient.generateClientId();
    private List<String> topics = new ArrayList<String>();
    private String mqttAddress;

    private final MqttAndroidClient[] connectMQTT = new MqttAndroidClient[1];

    public MqttAndroidClient getClient() {
        return connectMQTT[0];
    }

    public void addTopic(String topic){
        this.topics.add(topic);
    }

    public void removeTopic(String topic){
        topics.remove(topic);
    }

    public List<String> getAllTopics(){
        return this.topics;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        com.fmnumu.szakdolgozat.databinding.ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
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
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        populateTopicList();
    }


    public void connectMQTT(View view){
        this.connectMQTT(view, this.mqttAddress);
    }

    public MqttAndroidClient connectMQTT(View view, String mqttAddress){
        this.mqttAddress = mqttAddress;

        MqttAndroidClient client = new MqttAndroidClient(this.getApplicationContext() , "tcp://"+mqttAddress+":1883", clientId);

        try {
            client.connect(null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {

                    Log.d("CONNECTION", "onSuccess");
                    connectMQTT[0] = client;
                    Toast toast = Toast.makeText(getApplicationContext(), "Connection Successful on " + mqttAddress, Toast.LENGTH_SHORT);
                    toast.show();
                    subscribeAllTopics();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Toast toast = Toast.makeText(getApplicationContext(), "Connection Failed on: " + mqttAddress, Toast.LENGTH_SHORT);
                    toast.show();
                }
            });

        } catch (MqttException e) {
            Log.d("CONNECTION", "ERROR");
        }
        return client;
    }

    private void populateTopicList(){

    }

    public void subscribeAllTopics(){
        MqttAndroidClient mqttAndroidClient = this.getClient();
        try {
            if (!mqttAndroidClient.isConnected()) {
                mqttAndroidClient.connect();
            }
            for (int i = 0; i < topics.size(); i++) {
                int finalI = i;
                mqttAndroidClient.subscribe(topics.get(finalI), 0, this.getApplicationContext(), new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        Toast toast = Toast.makeText(getApplicationContext(), "Connection Successful", Toast.LENGTH_SHORT);
                        toast.show();
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        Toast toast = Toast.makeText(getApplicationContext(), "Connection Failed", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                });
            }

        } catch (Exception e) {
            Log.d("connect exception","Error :" + e.getMessage());
        }
    }


    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

}