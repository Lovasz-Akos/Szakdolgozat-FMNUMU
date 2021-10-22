 # Linkek és jegyzetek
 
## Ecliplse mosquitto
[MQTT Broker](https://mosquitto.org/download/) for self hosting mqtt server. This way I can use any device to handle mqtt packets

## Node.js
[Node.js](https://nodejs.org/en/). Needed to install Node-red on Windows.

## Node-RED
[Node-RED](https://nodered.org/) is the entire back-end of the project 

## MQTT broker config help
[Broker config with examples](http://www.steves-internet-guide.com/mossquitto-conf-file/)


[Broker config documentation](https://mosquitto.org/man/mosquitto-conf-5.html)

## Node-RED Web UI
[node-red-dashboard](https://flows.nodered.org/node/node-red-dashboard)
good for basic interaction and rapid deployment and testing of features

## MQTT alap docs
[MQTT docs](https://www.hivemq.com/mqtt-essentials/)


## Starting the servers:
### Node-RED Server:
     cd .\node_modules\node-red\
     node red


### MQTT Broker:
    mosquitto -c mqtt.conf -v

 >note that the mqtt config contains the host device's static ipv4 address, this may need to be changed for it to function as intended