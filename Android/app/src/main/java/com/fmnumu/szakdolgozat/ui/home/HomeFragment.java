package com.fmnumu.szakdolgozat.ui.home;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
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

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
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

        MqttAndroidClient mqttAndroidClient = ((MainActivity) getActivity()).getClient();

        if (mqttAndroidClient != null) {
            mqttNotifier(root, mqttAndroidClient);
        }
        else{
            Log.d("CONNECTION STARTUP", "onCreateView: NULL CLIENT");
        }

        FloatingActionButton fabSub = root.findViewById(R.id.fabSubscribe);
        fabSub.setOnClickListener(view -> {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
            dialogBuilder.setTitle("Enter topic to subscribe to");

            final EditText input = new EditText(getContext());

            input.setInputType(InputType.TYPE_CLASS_TEXT);
            input.setGravity(Gravity.CENTER);
            dialogBuilder.setView(input);

            dialogBuilder.setPositiveButton("Next", (dialog, which) -> {
                topic = input.getText().toString();
                if (topic.equals("")) {
                    Toast toast = Toast.makeText(getContext(), "Topic can't be empty", Toast.LENGTH_SHORT);
                    toast.show();
                } else {

                    AlertDialog.Builder subDialogBuilder = new AlertDialog.Builder(getContext());
                    final String[] actionType = new String[1];

                    subDialogBuilder.setTitle("Pick the action type");

                    final Spinner typeSpinner = new Spinner(getContext());

                    List<String> allTypes = ((MainActivity) getActivity()).getAllInteractTypes();

                    typeSpinner.setAdapter(new ArrayAdapter<>(getContext(), R.layout.item_spinner, allTypes));

                    subDialogBuilder.setView(typeSpinner);

                    typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view1, int i, long l) {
                            actionType[0] = typeSpinner.getSelectedItem().toString();
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {

                        }
                    });

                    subDialogBuilder.setPositiveButton("Subscribe", (dialog1, which1) -> {

                        List<String> cardData = new ArrayList<>();
                        cardData.add(topic);
                        cardData.add(actionType[0]);
                        cardData.add("null");
                        subscribeMQTT(((MainActivity) getActivity()).getClient(), cardData);
                    });

                    subDialogBuilder.show();
                }
            });

            dialogBuilder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

            dialogBuilder.show();
        });
        return root;
    }

    //TODO: replace R.id's with dynamic type
    @SuppressLint("NonConstantResourceId")
    private void createCard(LinearLayout layout, List<String> savedCardData, int type) {
        ViewGroup mqttCard = (ViewGroup) this.getLayoutInflater().inflate(type, null);
        TextView topicDisplay = (TextView) mqttCard.findViewById(R.id.text_topicDisplay);
        registerForContextMenu(mqttCard);
        topicDisplay.setText(savedCardData.get(0));

        switch (type) {
            case R.layout.mqtt_card_text:
                TextView text_data = (TextView) mqttCard.findViewById(R.id.text_data);
                if (!savedCardData.get(2).equals("null")) {
                    text_data.setText(savedCardData.get(2));
                } else {
                    text_data.setText(R.string.waitingForData);
                }
                layout.addView(mqttCard);
                break;
            case R.layout.mqtt_card_button:
                layout.addView(mqttCard);
                Button button_data = (Button) mqttCard.findViewById(R.id.button_data);
                button_data.setOnClickListener(view -> {
                    publishMessage(((MainActivity) getActivity()).getClient(), savedCardData.get(0), "1");
                });
                break;
            case R.layout.mqtt_card_switch:
                SwitchMaterial switch_data = (SwitchMaterial) mqttCard.findViewById(R.id.switch_data);

                if (!savedCardData.get(2).equals("null")) {
                    switch_data.setChecked(savedCardData.get(2).equals("on"));
                }
                switch_data.setOnClickListener(view -> {
                    String message = switch_data.isChecked() ? "on" : "off";
                    publishMessage(((MainActivity) getActivity()).getClient(), savedCardData.get(0), message);
                });
                layout.addView(mqttCard);
                break;
            case R.layout.mqtt_card_input:
                TextInputLayout inputField = mqttCard.findViewById(R.id.input_data_container);
                Button inputButton = mqttCard.findViewById(R.id.input_send_button);

                if (savedCardData.size() == 3) {
                    inputField.getEditText().setText(savedCardData.get(2));
                }
                inputButton.setOnClickListener(view -> {
                    String inputFieldData = String.valueOf(inputField.getEditText().getText());
                    publishMessage(((MainActivity) getActivity()).getClient(), savedCardData.get(0), inputFieldData);
                });
                layout.addView(mqttCard);
                break;
            case R.layout.mqtt_card_checkbox:
                CheckBox checkBox = mqttCard.findViewById(R.id.checkbox_data);
                if (!savedCardData.get(2).equals("null")) {
                    checkBox.setChecked(savedCardData.get(2).equals("on"));
                }
                checkBox.setOnClickListener(View -> {
                    publishMessage(((MainActivity) getActivity()).getClient(),
                            savedCardData.get(0), checkBox.isChecked() ? "on" : "off");
                });
                layout.addView(mqttCard);
                break;
            case R.layout.mqtt_card_slider:
                Slider slider_data = (Slider) mqttCard.findViewById(R.id.slider_data);

                if (savedCardData.get(2).equals("null")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("Enter the slider's range");
                    ViewGroup sliderRangeView = (ViewGroup) this.getLayoutInflater().inflate(R.layout.subscribe_slider, null);

                    TextInputLayout rangeMaxView = (TextInputLayout) sliderRangeView.findViewById(R.id.slider_top_limit);
                    TextInputLayout rangeMinView = (TextInputLayout) sliderRangeView.findViewById(R.id.slider_bottom_limit);

                    builder.setView(sliderRangeView);
                    builder.setPositiveButton("Set", (dialog, which) -> {
                        TextView sliderDataDisplay = mqttCard.findViewById(R.id.slider_data_display);
                        String rangeMin = rangeMinView.getEditText().getText().toString();
                        String rangeMax = rangeMaxView.getEditText().getText().toString();
                        if (Integer.parseInt(rangeMax) <= Integer.parseInt(rangeMin))
                        {
                            Toast toast = Toast.makeText(getContext(),
                                    "Range max can't be less or equal to range min", Toast.LENGTH_LONG);
                            toast.show();
                        }
                        else{
                            slider_data.setValueTo(Float.parseFloat(rangeMax));
                            slider_data.setValue(Float.parseFloat(rangeMin));
                            slider_data.setValueFrom(Float.parseFloat(rangeMin));
                            sliderDataDisplay.setText(rangeMin);
                            layout.addView(mqttCard);
                        }
                    });
                    builder.show();

                } else {
                    List<String> sliderSubData = Arrays.asList(savedCardData.get(2).split("\\."));
                    TextView sliderDataDisplay = mqttCard.findViewById(R.id.slider_data_display);

                    String rangeMin = sliderSubData.get(0);
                    String rangeMax = sliderSubData.get(1);
                    String currentVal = sliderSubData.get(2);

                    slider_data.setValueTo(Float.parseFloat(rangeMax));
                    slider_data.setValue(Float.parseFloat(currentVal));
                    slider_data.setValueFrom(Float.parseFloat(rangeMin));

                    sliderDataDisplay.setText(currentVal);
                    layout.addView(mqttCard);
                }

                final Slider.OnSliderTouchListener touchListener =
                        new Slider.OnSliderTouchListener() {
                            @SuppressLint("RestrictedApi")
                            @Override
                            public void onStartTrackingTouch(@NonNull Slider slider) {

                            }

                            @SuppressLint("RestrictedApi")
                            @Override
                            public void onStopTrackingTouch(@NonNull Slider slider) {
                                String value = String.valueOf(slider_data.getValue()).replace(".0", "");
                                TextView sliderDataDisplay = mqttCard.findViewById(R.id.slider_data_display);
                                sliderDataDisplay.setText(value);
                                publishMessage(((MainActivity) getActivity()).getClient(), savedCardData.get(0), value);
                            }
                        };

                slider_data.addOnSliderTouchListener(touchListener);
                break;
            default:
                Toast toast = Toast.makeText(getContext(), "Failed to create card + " + savedCardData.get(0), Toast.LENGTH_SHORT);
                toast.show();
                break;
        }
    }

    public void subscribeMQTT(MqttAndroidClient mqttAndroidClient, List<String> cardData) {
        LinearLayout layout = (LinearLayout) getView().findViewById(R.id.cardList);
        try {
            if (!mqttAndroidClient.isConnected()) {
                ((MainActivity)getActivity()).reconnectToMQTT(mqttAndroidClient);
            }
            mqttAndroidClient.subscribe(cardData.get(0), 0, getContext(), new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d("SUB", "onSuccess: ");
                    switch (cardData.get(1)) {
                        case "Text":
                            createCard(layout, cardData, R.layout.mqtt_card_text);
                            break;
                        case "Button":
                            createCard(layout, cardData, R.layout.mqtt_card_button);
                            break;
                        case "Switch":
                            createCard(layout, cardData, R.layout.mqtt_card_switch);
                            break;
                        case "Input":
                            createCard(layout, cardData, R.layout.mqtt_card_input);
                            break;
                        case "Checkbox":
                            createCard(layout, cardData, R.layout.mqtt_card_checkbox);
                            break;
                        case "Slider":
                            createCard(layout, cardData, R.layout.mqtt_card_slider);
                        default:
                            break;
                    }
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.d("ONFAIL", "onFailure: " + mqttAndroidClient.isConnected());
                    snackBarMaker(getView(), "Connection timed out!");
                }
            });
        } catch (Exception e) {
            Toast toast = Toast.makeText(getContext(), "MQTT is not connected!", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    public void unsubscribeMQTT(MqttAndroidClient mqttAndroidClient, String topic) {
        try {
            if (!mqttAndroidClient.isConnected()) {
                ((MainActivity)getActivity()).reconnectToMQTT(mqttAndroidClient);
            }
            IMqttToken unsubscribeToken = mqttAndroidClient.unsubscribe(topic);
            unsubscribeToken.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    snackBarMaker(getView(), "Unsubscribed from topic " + topic);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    snackBarMaker(getView(), topic + " topic unsubscribe failed!");
                }
            });
        } catch (MqttException e) {
            Log.d("tag", "Error :" + e);
        }
    }

    private void mqttNotifier(View root, MqttAndroidClient mqttAndroidClient) {

        try {
            if (!mqttAndroidClient.isConnected()) {
                ((MainActivity)getActivity()).reconnectToMQTT(mqttAndroidClient);
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
                public void messageArrived(String topic, MqttMessage message) {
                    LinearLayout cardList = root.findViewById(R.id.cardList);
                    for (int i = 0; i < cardList.getChildCount(); i++) {
                        messageReceivedHandler(topic, message, cardList, i);
                    }
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {

                }
            });

        } catch (MqttException e) {
            Toast toast = Toast.makeText(getContext(), "MQTT is not connected!", Toast.LENGTH_SHORT);
            toast.show();
        }

    }

    private void messageReceivedHandler(String topic, MqttMessage message,
                                        LinearLayout cardList, int i) {

        String topicDisplay = (String) ((TextView) cardList.getChildAt(i).findViewById(R.id.text_topicDisplay)).getText();

        ViewGroup cardRoot = (ViewGroup) cardList.getChildAt(i);
        ViewGroup cardViewGroup = (ViewGroup) cardRoot.getChildAt(0);
        ViewGroup cardLayout = (ViewGroup) cardViewGroup.getChildAt(0);

        View activeElement = cardLayout.getChildAt(1);

        if (topicDisplay.equals(topic)) {

            if (activeElement instanceof SwitchMaterial) {
                SwitchMaterial switchView = cardList.getChildAt(i).findViewById(R.id.switch_data);
                switchView.setChecked(decodeMQTT(message).equals("on"));
            }
            else if (activeElement instanceof Slider) {
                Slider sliderData = cardList.getChildAt(i).findViewById(R.id.slider_data);
                TextView sliderDataDisplay = cardList.getChildAt(i).findViewById(R.id.slider_data_display);

                if ((Integer.parseInt(decodeMQTT(message)) >= sliderData.getValueFrom())
                        && (Integer.parseInt(decodeMQTT(message)) <= sliderData.getValueTo())) {
                    sliderDataDisplay.setText(decodeMQTT(message));
                    sliderData.setValue(Integer.parseInt(decodeMQTT(message)));
                } else {
                    sliderDataDisplay.setText("Data out of range: " + decodeMQTT(message));
                }

            }
            else if (activeElement instanceof TextInputLayout) {
                TextInputEditText textInputEditText = cardList.getChildAt(i).findViewById(R.id.input_data);
                textInputEditText.setText(decodeMQTT(message));
            }
            else if (activeElement instanceof MaterialCheckBox) {
                CheckBox checkBox = cardList.getChildAt(i).findViewById(R.id.checkbox_data);
                checkBox.setChecked(decodeMQTT(message).equals("on"));
            }
            else if (activeElement instanceof TextView) {
                TextView dataDisplay = cardList.getChildAt(i).findViewById(R.id.text_data);
                dataDisplay.setText(decodeMQTT(message));
            }
        }
    }

    public void publishMessage(MqttAndroidClient mqttAndroidClient, String topic, String payload) {
        try {
            if (!mqttAndroidClient.isConnected()) {
                ((MainActivity)getActivity()).reconnectToMQTT(mqttAndroidClient);
            }

            MqttMessage message = new MqttMessage();
            message.setPayload(payload.getBytes());
            message.setQos(0);
            mqttAndroidClient.publish(topic, message, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d("PUBLISH", "published on: " + topic + " message: " + payload);
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
        } catch (NullPointerException e) {
            Log.e("nullPointerException", e.toString());

            Toast toast = Toast.makeText(getContext(), "Publish Failed, MQTT not connected", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    public void subscribeAllTopics() {
        MqttAndroidClient mqttAndroidClient = ((MainActivity) getActivity()).getClient();
        List<String[]> splitCardData = new ArrayList<>();
        List<String> cardDataStore = ((MainActivity) getActivity()).getCardDataStoreAll();

        try {
            if (!mqttAndroidClient.isConnected()) {
                ((MainActivity)getActivity()).reconnectToMQTT(mqttAndroidClient);
            }
            for (int i = 0; i < cardDataStore.size(); i++) {
                splitCardData.add(cardDataStore.get(i).split(":", 0));
                subscribeMQTT(((MainActivity) getActivity()).getClient(), Arrays.asList(splitCardData.get(i)));
            }

        } catch (Exception e) {
            e.printStackTrace();
            Log.d("connect exception", "Error :" + e.getMessage());
        }
    }

    private String decodeMQTT(MqttMessage msg) {
        return new String(msg.getPayload(), StandardCharsets.UTF_8);
    }

    private void snackBarMaker(View view, String message) {
        Snackbar snackbar = Snackbar
                .make(view.findViewById(R.id.snackRoot), message, Snackbar.LENGTH_SHORT);
        snackbar.show();
    }

    @Override
    public void onResume() {
        super.onResume();
        LinearLayout cardList = getView().findViewById(R.id.cardList);
        if (cardList.getChildCount() == 0) {
            subscribeAllTopics();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        LinearLayout cardList = getView().findViewById(R.id.cardList);
        for (int i = 0; i < cardList.getChildCount(); i++) {
            ViewGroup cardInstance = (ViewGroup) cardList.getChildAt(i);

            String cardInstanceTopic = (String) ((TextView) cardInstance.findViewById(R.id.text_topicDisplay)).getText();
            String cardInstanceType = "null";
            String cardInstanceData = "null";

            int cardType = cardInstance.getId();
            Log.d("PAUSE", "onPause: " + cardType);
            switch (cardType) {
                case R.id.text_type_card:
                    cardInstanceType = "Text";
                    cardInstanceData = (String) ((TextView) cardInstance.findViewById(R.id.text_data)).getText();
                    break;
                case R.id.button_type_card:
                    cardInstanceType = "Button";
                    break;
                case R.id.switch_type_card:
                    cardInstanceType = "Switch";
                    cardInstanceData = ((SwitchMaterial) cardInstance.findViewById(R.id.switch_data)).isChecked() ? "on" : "off";
                    break;
                case R.id.input_type_card:
                    cardInstanceType = "Input";
                    cardInstanceData = String.valueOf(((TextInputEditText) cardInstance.findViewById(R.id.input_data)).getText());
                    break;
                case R.id.checkbox_type_card:
                    cardInstanceType = "Checkbox";
                    cardInstanceData = ((CheckBox) cardInstance.findViewById(R.id.checkbox_data)).isChecked() ? "on" : "off";
                    break;
                case R.id.slider_type_card:
                    Slider slider_data = cardInstance.findViewById(R.id.slider_data);
                    String currentValue = String.valueOf((slider_data.getValue())).replace(".0", "");
                    String minValue = String.valueOf((slider_data).getValueFrom()).replace(".0", "");
                    String maxValue = String.valueOf((slider_data).getValueTo()).replace(".0", "");
                    
                    cardInstanceType = "Slider";
                    cardInstanceData = minValue + "." + maxValue + "." + currentValue;
            }
            ((MainActivity) getActivity()).addCardDataToPersistentStorage(cardInstanceTopic, cardInstanceType, cardInstanceData);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View card,
                                    ContextMenu.ContextMenuInfo menuInfo) {

        menu.add(R.string.unsubscribeCard).setOnMenuItemClickListener(menuItem -> {

            String topic;
            String type = "null";

            View material_card_view = ((ViewGroup) card).getChildAt(0);
            View card_internal_layout = ((ViewGroup) material_card_view).getChildAt(0);
            TextView topicDisplay = (TextView) ((ViewGroup) card_internal_layout).getChildAt(0);

            View cardType = ((ViewGroup) card_internal_layout).getChildAt(1);

            if (cardType instanceof SwitchMaterial) {
                type = "Switch";
            } else if (cardType instanceof TextInputLayout) {
                type = "Input";
            } else if (cardType instanceof Slider) {
                type = "Slider";
                Slider slider = ((Slider) cardType);
            } else if (cardType instanceof CheckBox) {
                type = "Checkbox";

            } else if (cardType instanceof Button) {
                type = "Button";
            } else if (cardType instanceof TextView) {
                type = "Text";

            }

            topic = (String) topicDisplay.getText();
            unsubscribeMQTT(((MainActivity) getActivity()).getClient(), topic);

            ((MainActivity) getActivity()).removeCardData(topic, type);
            ((ViewGroup) card.getParent()).removeView(card);
            return true;
        });
    }

    public class snackBarReconnectListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            MqttAndroidClient client = ((MainActivity) getActivity()).getClient();
            if (!client.isConnected()) {
                ((MainActivity) getActivity()).connectMQTT();
                LinearLayout cardListAsLayout = getView().findViewById(R.id.cardList);
                cardListAsLayout.removeAllViews();
                onResume();
                snackBarMaker(getView(), "MQTT reconnected");
            }
        }
    }
}