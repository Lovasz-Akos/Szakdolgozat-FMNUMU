# Szakdolgozat-FMNUMU
Smarthome hub and controller. 

Thesis work of Lovász Ákos.
Eszterházy Károly Katolikus Egyetem.


## Starting the servers:
### Node-RED Server:
     cd .\node_modules\node-red\
     node red


### MQTT Broker:
    mosquitto -c custom.conf -v
    
>note that the mqtt config contains the host device's static ipv4 address, this may need to be changed for it to function as intended