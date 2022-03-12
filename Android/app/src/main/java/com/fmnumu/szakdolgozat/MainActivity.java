package com.fmnumu.szakdolgozat;

import android.os.Bundle;
import android.view.View;

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

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;

    private final MqttAndroidClient[] connectMQTT = new MqttAndroidClient[1];

    public MqttAndroidClient getClient() {
        return connectMQTT[0];
    }

    public MqttAndroidClient setClient(MqttAndroidClient connectMQTT) {
        return this.connectMQTT[0] = connectMQTT;
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
    }

    public MqttAndroidClient connectMQTT(View view, String mqttAddress){

        String clientId = MqttClient.generateClientId();

        MqttAndroidClient client = new MqttAndroidClient(this.getApplicationContext() , "tcp://"+mqttAddress+":1883", clientId);

        try {
            client.connect(null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    
                    Snackbar snackbar = Snackbar
                            .make(view, "Connection Success", Snackbar.LENGTH_LONG);
                    snackbar.show();

                    connectMQTT[0] = client;
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {

                    Snackbar snackbar = Snackbar
                            .make(view, "Connection Failed", Snackbar.LENGTH_LONG);
                    snackbar.show();
                }
            });

        } catch (MqttException e) {
            Snackbar snackbar = Snackbar
                    .make(view, "Connection failed, please check host address", Snackbar.LENGTH_LONG);
            snackbar.show();
        }
        return client;
    }


    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

}