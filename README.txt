== ACKNOWLEDGEMENTS
Team New South Solutions would like to acknowledge the original authors of any code that are used for the project.


The code to preparing bluetooth connection to a paired device was provided kindly by Harry Goodwill, with his permission to allow Team New South Solutions to use the code in this project. 
The code was adapted from the following site:
            	https://wingoodharry.wordpress.com/2014/04/15/android-sendreceive-data-with-arduino-using-bluetooth-part-2/


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

= ARDUINO CODE

= APP CODE (BACKEND)
To request information from the arduino, such as the current UV measurement or logged UV readings, the app writes a "command digit" into its bluetooth communication channel. 

The application handles bluetooth messages by running continuously a thread that "listens" for messages and deciphers them accordingly. When a "UV Measurement" is received, the app updates the interface to present that reading to the user. When "UV Logs" are received, the app writes those logs to a file that contains all the UV measurements for that day.

The graphing relies on reading files from internal storage of the device, processing one line at a time into the graph. Although the processing for the "readings" chart and "uv exposure" charts are slightly different, they both nevertheless rely on file reading.

= APP CODE (FRONTEND)

