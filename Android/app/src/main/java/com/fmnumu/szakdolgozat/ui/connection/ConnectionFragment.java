package com.fmnumu.szakdolgozat.ui.connection;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.fmnumu.szakdolgozat.R;
import com.fmnumu.szakdolgozat.databinding.FragmentConnectionBinding;
import com.google.android.material.snackbar.Snackbar;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class ConnectionFragment extends Fragment{

    private final MqttAndroidClient[] connectMQTT = new MqttAndroidClient[1];

    // Getter
    public MqttAndroidClient getConnection() {
        return connectMQTT[0];
    }


    private ConnectionViewModel connectionViewModel;
    private FragmentConnectionBinding binding;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentConnectionBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final EditText textBoxMqttAddress = (EditText) root.findViewById(R.id.textboxMqttBrokerAddr);





        Button connect = (Button) root.findViewById(R.id.buttonMqttConnect);
        connect.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
                String mqttAddress = textBoxMqttAddress.getText().toString();
                connectMQTT[0] = connectMQTT(root, mqttAddress);
            }
        });



        connectionViewModel =
                new ViewModelProvider(this).get(ConnectionViewModel.class);

        return root;
    }


    public MqttAndroidClient connectMQTT(View view, String mqttAddress){

        String clientId = MqttClient.generateClientId();

        MqttAndroidClient client = new MqttAndroidClient(getContext() , "tcp://"+mqttAddress+":1883", clientId);

        byte[] encodedPayload = new byte[0];
        try {
            client.connect(null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {

                    //publishMessage(mqttMessage, client, mqttTopic);
                    Snackbar snackbar = Snackbar
                            .make(view, "Connection Success", Snackbar.LENGTH_LONG);
                    snackbar.show();
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
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}