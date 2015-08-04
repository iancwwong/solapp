== ACKNOWLEDGEMENTS
Team New South Solutions would like to acknowledge the original authors of any code that are used for the project.

The code to preparing bluetooth connection to a paired device was provided kindly by Harry Goodwill, with his permission to allow Team New South Solutions to use the code in this project. 
The code was adapted from the following site:
      			https://wingoodharry.wordpress.com/2014/04/15/android-sendreceive-data-with-arduino-using-bluetooth-part-2/

The front end code also adapts code released on github from other developers. In particular, the code to draw a circular progress bar was written by Pedram. The code is available at the following repository:					https://gitub.com/Pedramrn/CircularProgressBar.

There is a graph drawing library also used. Written by Philip Jahoda, the library and its code is available at:
		https://github.com/PhilJay/MPAndroidChart


The Arduino code related to UV sensor processing was provided by SparkFun, licensed as beerware.
The code was adapted from SparkFun's GitHub codebase for the ML8511 UV sensor:
		https://github.com/sparkfun/ML8511_Breakout


== THE CODE OVERALL
Java was the main programming language used to implement both the front end and back end of the mobile application. The IDE used to write this code was Android Studio. 
Because the files are arranged into the format of an Android Studio project, it is highly recommended to use Android Studio to open and compile this code.

The code was done throughout the week

== INSTRUCTIONS ON RUNNING THE CODE
For the mobile application, the code can be compiled and run by the following steps:
	1. Open Android Studio
	2. Select "Import project"
	3. Browse for and open the project folder (there should be a green Android Studio icon next to any project folders). Accepot any prompts to use another sdk or install missing files.
	4. Plug in an Android device, and ensure to turn on its "USB Debugging" option.
	5. Up near the top of the IDE, press the green Play button (this will compile the code).
	6. After compilation is complete, make sure the plugged-in android device is selected under "running devices".
	NOTE: It is strongly not advisable to run this app using a simulator, as by default, it has no bluetooth capabilities and the program will halt.
	7. Select OK. This will load the code to the chosen android device as an application, and will automatically be run.

For the Arduino program, the code can be compiled and run by the following steps:
	1. Download the Windows Installer for the latest Arduino IDE from https://www.arduino.cc/en/Main/Software.
	2. Install all components of the software.
	3. Open Sol.ino using the Arduino IDE.
	4. Plug the Arduino into the computer.
	5. In the IDE, go to Tools -> Board and select the Arduino model that is plugged in, then go to Tools -> Port and select the COM port that the Arduino is connected to.
	6. Click on Upload near the top left corner of the IDE. This should compile and upload the program to the Arduino.
	7. Once uploaded, the program will start running automatically.

= ARDUINO CODE
The Arduino is connected to a UV sensor. The UV Sensor outputs in units of volts. The Arduino takes multiple consecutive readings of the voltage and converts the average into units of milliwatts per centimetre squared by mapping the value ranges (from 0.99-2.8V to 0-15mW/cm^2). This makes one UV reading.

Every second, one UV reading is taken and stored. Every minute, the average readings of that minute is calculated and stored. This is how the Arduino measures and logs UV readings over time.

The Arduino can send and receive bytes of data to a smartphone wirelessly using a Bluetooth module. If the Bluetooth module receives a byte that corresponds to a single UV read command, the Arduino will take a UV reading and send the value back. If a sync command is received, the Arduino will send back all the calculated minute averages since the last sync and restart the logging process from the beginning.

There is a sunscreen re-apply reminder timer which counts up to 3 hours. When 3 hours is reached, the Arduino sends an alert message through Bluetooth, which corresponds to the user getting a sunscreen reminder message on the smartphone app, and will also start blinking an LED that is connected to the Arduino. A pushbutton connected to the Arduino is able to reset this timer when pushed. Pushing the button will also stop the LED from blinking, until the timer reaches 3 hours again.

= APP CODE (BACKEND)
To request information from the arduino, such as the current UV measurement or logged UV readings, the app writes a "command digit" into its bluetooth communication channel. 

The application handles bluetooth messages by running continuously a thread that "listens" for messages and deciphers them accordingly. When a "UV Measurement" is received, the app updates the interface to present that reading to the user. When "UV Logs" are received, the app writes those logs to a file that contains all the UV measurements for that day.

The graphing relies on reading files from internal storage of the device, processing one line at a time into the graph. Although the processing for the "readings" chart and "uv exposure" charts are slightly different, they both nevertheless rely on file reading.

= APP CODE (FRONTEND)
There are two main pages: the page containing the list of bluetooth devices (Bluetooth Page), and the page that displays information to the user (Main Page). Inside Main Page, there are subpages, each represented as a "fragment" inside Android Studio - main screen, UV Readings, Weekly Exposures, and Settings. 

The navigation drawer is the UI component on the Main Page that appears on the side, and provides the user access to the different fragments (or subpages) used in the app. It is triggered in the action menu bar.

The action (menu) bar houses the navigation drawer, measure UV button, and sync button. There is a listener that opens up the navigation drawer when the user clicks on the component

The circular progress bar in the main page of the app is made in a custom class called "Circular Progress Bar". The original author of the code is Pedram, who shared this code on github (https://github.com/Pedramrn/CircularProgressBar), and the code was adapted for our project. The value in "UV Reading" or "UV Exposure" provides the CircleProgressBar the portion of the circle to draw, which is animated using an Android ObjectAnimator. 

The charts, drawn in the "UV Readings" and "Weekly Exposures" fragments, are done so using a library named "MPAndroidChart", coded by Philip Jahoda (code is available from the Github repository at https://github.com/PhilJay/MPAndroidChart). The team simply used its provided functions to draw, populate, and customise the charts.

