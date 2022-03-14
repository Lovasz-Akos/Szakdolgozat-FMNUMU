package com.fmnumu.szakdolgozat.ui.home;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.fmnumu.szakdolgozat.MainActivity;
import com.fmnumu.szakdolgozat.R;
import com.fmnumu.szakdolgozat.databinding.ActivityMainBinding;
import com.fmnumu.szakdolgozat.databinding.FragmentHomeBinding;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.w3c.dom.Text;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private FragmentHomeBinding binding;
    private String dialogResult = "";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        CoordinatorLayout layout = (CoordinatorLayout) root.findViewById(R.id.snackRoot);

        MqttAndroidClient mqttAndroidClient = ((MainActivity)getActivity()).getClient();
        if (mqttAndroidClient != null && mqttAndroidClient.isConnected()){
            mqttNotifier(root, mqttAndroidClient);
        }

        Log.d("CARD", "onClick: attempting to add CARD");
        MaterialCardView materialCardView = new MaterialCardView(getContext());
        materialCardView.setCardElevation(8);
        materialCardView.setMinimumWidth(500);
        materialCardView.setMinimumHeight(500);
        materialCardView.setPadding(50,50,50,50);
        materialCardView.setCardBackgroundColor(Color.RED);
        materialCardView.setRadius(40);


        layout.addView(materialCardView);

        materialCardView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                snackBarMaker(view, "CLICC DA SQUARE");
            }
        });
        Log.d("CARD", "onClick: is it there yet?");

        FloatingActionButton fabSub = root.findViewById(R.id.fabSubscribe);
        fabSub.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Enter topic to subscribe to");

                final EditText input = new EditText(getContext());

                input.setInputType(InputType.TYPE_CLASS_TEXT);
                input.setWidth(10);
                builder.setView(input);

                builder.setPositiveButton("Subscribe", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialogResult = input.getText().toString();
                        subscribeMQTT(((MainActivity)getActivity()).getClient(), dialogResult);


                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }
        });

        return root;
    }

    private void subscribeMQTT(MqttAndroidClient mqttAndroidClient, String topic){
        try {
            if (!mqttAndroidClient.isConnected()) {
                mqttAndroidClient.connect();
            }
            mqttAndroidClient.subscribe(topic, 0, getContext(), new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    snackBarMaker(getView(), "Subscribed to " + topic);
                    ((MainActivity)getActivity()).addTopic(topic);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                   snackBarMaker(getView(), "Failed to subscribe");
                }
            });
        } catch (Exception e) {
            Log.d("tag","Error :" + e);
        }
    }


    private void mqttNotifier(View root, MqttAndroidClient mqttAndroidClient){

        try {
            if (!mqttAndroidClient.isConnected()) {
                mqttAndroidClient.connect();
            }
            mqttAndroidClient.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    Snackbar snackbar = Snackbar
                            .make(root.findViewById(R.id.snackRoot), "mqtt connection lost", Snackbar.LENGTH_SHORT);
                    snackbar.setAction("Reconnect", new snackbarReconnectListener());
                    snackbar.show();
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {

                    snackBarMaker(root, "received " + decodeMQTT(message) + " on topic: " + topic);
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {

                }
            });

        }
        catch (MqttException e){
           snackBarMaker(root, "mqtt connection lost");
        }

    }

    public class snackbarReconnectListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            MqttAndroidClient client = ((MainActivity)getActivity()).getClient();
            if (!client.isConnected()) {
               ((MainActivity)getActivity()).connectMQTT(v);
                snackBarMaker(v, "mqtt connection lost");
            }

        }
    }

    private String decodeMQTT(MqttMessage msg) throws UnsupportedEncodingException {
        return new String(msg.getPayload(), StandardCharsets.UTF_8);
    }

    private void snackBarMaker(View view, String message){
        Snackbar snackbar = Snackbar
                .make(view.findViewById(R.id.snackRoot), message, Snackbar.LENGTH_SHORT);
        snackbar.show();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}