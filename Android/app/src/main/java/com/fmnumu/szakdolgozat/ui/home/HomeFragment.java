package com.fmnumu.szakdolgozat.ui.home;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.fmnumu.szakdolgozat.MainActivity;
import com.fmnumu.szakdolgozat.R;
import com.fmnumu.szakdolgozat.databinding.FragmentHomeBinding;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.slider.Slider;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private FragmentHomeBinding binding;
    private String topic = "";


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
        LinearLayout layout = (LinearLayout) root.findViewById(R.id.cardList);

        MqttAndroidClient mqttAndroidClient = ((MainActivity)getActivity()).getClient();

        if (mqttAndroidClient != null && mqttAndroidClient.isConnected()){
            mqttNotifier(root, mqttAndroidClient);
        }

        FloatingActionButton fabSub = root.findViewById(R.id.fabSubscribe);
        fabSub.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Enter topic to subscribe to");

                final EditText input = new EditText(getContext());

                input.setInputType(InputType.TYPE_CLASS_TEXT);
                input.setGravity(Gravity.CENTER);
                builder.setView(input);

                builder.setPositiveButton("Next", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        topic = input.getText().toString();
                        if (topic.equals("")){
                            Toast toast = Toast.makeText(getContext(), "Topic can't be empty", Toast.LENGTH_SHORT);
                            toast.show();
                        }
                        else{

                            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                            final String[] actionType = new String[1];

                            builder.setTitle("Pick the action type");

                            final Spinner typeSpinner = new Spinner(getContext());

                            List<String> allTypes = ((MainActivity)getActivity()).getAllInteractTypes();

                            typeSpinner.setAdapter(new ArrayAdapter<String>(getContext(), R.layout.subscribe_spinner, allTypes));

                            builder.setView(typeSpinner);

                            typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                                    actionType[0] = typeSpinner.getSelectedItem().toString();
                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> adapterView) {

                                }
                            });

                            builder.setPositiveButton("Subscribe", new DialogInterface.OnClickListener(){
                                @Override
                                public void onClick(DialogInterface dialog, int which){
                                    subscribeMQTT(((MainActivity)getActivity()).getClient(), topic, actionType[0]);
                                }
                            });

                            builder.show();
                        }
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

    @SuppressLint("NonConstantResourceId")
    private void createCard(LinearLayout layout, String topic, int type) {
        ViewGroup mqttCard = (ViewGroup) this.getLayoutInflater().inflate(type, null);
        TextView topicDisplay = (TextView) mqttCard.findViewById(R.id.text_topicDisplay);
        topicDisplay.setText(topic);
        layout.addView(mqttCard);

        switch (type) {
            case R.layout.mqtt_card_text:
                //TODO: add appropriate listener
                /* EXAMPLE LISTENER, ONLY ADD TO APPROPRIATE CARD TYPES

                    TextView text_data = (TextView) mqttCard.findViewById(R.id.text_data);
                    text_data.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            publishMessage(((MainActivity)getActivity()).getClient(), topic, "message");
                        }
                    });
                */
                break;
            case R.layout.mqtt_card_button:
                //TODO: add appropriate listener
                break;
            case R.layout.mqtt_card_switch:
                //TODO: add appropriate listener
                SwitchMaterial switch_data = (SwitchMaterial) mqttCard.findViewById(R.id.switch_data);
                switch_data.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String message = switch_data.isChecked() ? "on" : "off";
                        publishMessage(((MainActivity)getActivity()).getClient(), topic, message);
                    }
                });
                break;
            case R.layout.mqtt_card_input:
                //TODO: add appropriate listener
                break;
            //case "Checkbox":
                //TODO: Checkbox card
              //  break;
            case R.layout.mqtt_card_slider:
                //TODO: add appropriate listener

                //TODO: add more card types?

            default:
                Toast toast = Toast.makeText(getContext(), "Failed to create card", Toast.LENGTH_SHORT);
                toast.show();
                break;
        }
    }

    private void subscribeMQTT(MqttAndroidClient mqttAndroidClient, String topic, String type){
        LinearLayout layout = (LinearLayout) getView().findViewById(R.id.cardList);
        try {
            if (!mqttAndroidClient.isConnected()) {
                mqttAndroidClient.connect();
            }
            mqttAndroidClient.subscribe(topic, 0, getContext(), new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    if (!((MainActivity)getActivity()).getAllTopics().contains(topic+":"+type)){
                        ((MainActivity)getActivity()).addTopic(topic+":"+type);
                    }
                    switch (type) {
                        case "Text":
                            createCard(layout, topic, R.layout.mqtt_card_text);
                            break;
                        case "Button":
                            createCard(layout, topic, R.layout.mqtt_card_button);
                            break;
                        case "Switch":
                            createCard(layout, topic, R.layout.mqtt_card_switch);
                            break;
                        case "Input":
                            //TODO: Input card
                            break;
                        case "Checkbox":
                            //TODO: Checkbox card
                            break;

                        //TODO: add more card types?

                        default:
                            Toast toast = Toast.makeText(getContext(), "Failed to create card", Toast.LENGTH_SHORT);
                            toast.show();
                            break;
                    }

                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                   snackBarMaker(getView(), "Failed to subscribe");
                }
            });
        } catch (Exception e) {
            Toast toast = Toast.makeText(getContext(), "MQTT is not connected!", Toast.LENGTH_SHORT);
            toast.show();
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
                            .make(root.findViewById(R.id.snackRoot), "MQTT Connection lost!", Snackbar.LENGTH_LONG);
                    snackbar.setAction("Reconnect", new snackBarReconnectListener());
                    snackbar.show();
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    LinearLayout cardList = root.findViewById(R.id.cardList);
                    for (int i = 0; i < cardList.getChildCount(); i++) {
                        cardHandlerOnMessageReceived(topic, message, cardList, i);
                    }
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {

                }
            });

        }
        catch (MqttException e){
            Toast toast = Toast.makeText(getContext(), "MQTT is not connected!", Toast.LENGTH_SHORT);
            toast.show();
        }

    }

    private void cardHandlerOnMessageReceived(String topic, MqttMessage message, LinearLayout cardList, int i) throws UnsupportedEncodingException {

        String topicDisplay = (String) ((TextView) cardList.getChildAt(i).findViewById(R.id.text_topicDisplay)).getText();
        
        ViewGroup cardRoot = (ViewGroup) cardList.getChildAt(i);
        ViewGroup cardViewGroup = (ViewGroup) cardRoot.getChildAt(0);
        ViewGroup cardLayout = (ViewGroup) cardViewGroup.getChildAt(0);
        
        View activeElement = cardLayout.getChildAt(1);

        if (topicDisplay.equals(topic)){

            if (activeElement instanceof SwitchMaterial) {

                SwitchMaterial switchView = cardList.getChildAt(i).findViewById(R.id.switch_data);

                if (decodeMQTT(message).equals("on")) {
                    switchView.setChecked(true);
                } else if (decodeMQTT(message).equals("off")) {
                    switchView.setChecked(false);
                }
            }
            else if(activeElement instanceof TextInputLayout){
                TextInputEditText textInputEditText = cardList.getChildAt(i).findViewById(R.id.input_data);
                textInputEditText.setText(decodeMQTT(message));
            }
            else if(activeElement instanceof Slider){
                Slider sliderData = cardLayout.getChildAt(i).findViewById(R.id.slider_data);
                sliderData.setValue(Integer.parseInt(decodeMQTT(message)));
            }
            else if (activeElement instanceof MaterialCheckBox){
                //todo: do something useful with a checkbox?
            }

            else if(activeElement instanceof TextView){
                TextView dataDisplay = cardList.getChildAt(i).findViewById(R.id.text_data);
                dataDisplay.setText(decodeMQTT(message));
            }
        }
    }

    public class snackBarReconnectListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            MqttAndroidClient client = ((MainActivity)getActivity()).getClient();
            if (!client.isConnected()) {
               ((MainActivity)getActivity()).connectMQTT();
                snackBarMaker(getView(), "MQTT reconnected");
            }

        }
    }

    public void publishMessage(MqttAndroidClient mqttAndroidClient, String topic,  String payload) {
        try {
            if (!mqttAndroidClient.isConnected()) {
                mqttAndroidClient.connect();
            }

            MqttMessage message = new MqttMessage();
            message.setPayload(payload.getBytes());
            message.setQos(0);
            mqttAndroidClient.publish(topic, message,null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Toast toast = Toast.makeText(getContext(), "published " + message + " on topic " + topic, Toast.LENGTH_SHORT);
                    toast.show();
                    Log.d("PUBLISH", "onSuccess");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Toast toast = Toast.makeText(getContext(), "Publish failed! status: MQTT disconnected", Toast.LENGTH_SHORT);
                    toast.show();
                    Log.d("PUBLISH", "onFail");
                }
            });
        } catch (MqttException e) {
            Log.e("mqttException", e.toString());

            Toast toast = Toast.makeText(getContext(), "Fatal MQTT Error", Toast.LENGTH_SHORT);
            toast.show();
        }
        catch (NullPointerException e){
            Log.e("nullPointerException", e.toString());

            Toast toast = Toast.makeText(getContext(), "Publish Failed, MQTT not connected", Toast.LENGTH_SHORT);
            toast.show();
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
    public void onResume() {
        super.onResume();
        LinearLayout cardList = getView().findViewById(R.id.cardList);

        if (cardList.getChildCount() == 0) {
            ArrayList<String> allTopics = (ArrayList<String>) ((MainActivity) getActivity()).getAllTopics();

            for (int i = 0; i < allTopics.size(); i++) {
                //TODO: reformat resub if viewgroup restoration isn't implemented
                subscribeMQTT(((MainActivity) getActivity()).getClient(), allTopics.get(i), "Text");
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}