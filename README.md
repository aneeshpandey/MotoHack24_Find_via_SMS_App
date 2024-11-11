App does the following:

1. Theft mode gets activated when 2 finger touch is detected on the main activity. (Alarm rings, warning sms is sent to hardcoded emergency number)
   a. "Unlock device, 1001!" sms should be sent to device to unlock the device when it is in Theft mode.
2. Calls will be auto answered and switched to loudspeaker mode.
3. Find my device through sms:
  a. Pressing button on main activity will start a 5 second timer to simulate leaving phone somewhere and sends update to hardcoded emergency number.
  b. "Send location, 1001!" sms should be sent to device to receive sms with battery level and location of the device.(the device will send sms to the number it receives the sms from)
  c. "Play alarm, 1001!" sms should be sent to the device to play a tone on the device so it can be located.
