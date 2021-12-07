# GreenSpaces
An android mobile app that provides easier, better, faster access to green spaces.

## Methods Overview
Greenspaces is written in Java using Android Studio with a server and web scraper written in python. A MySQL database holds Location, User, Image, and Review tables.

## Running Locally
### Note: This app is configured to run on Android Studio Arctic Fox | 2020.3.1 Patch 3, other versions of android studio may not be compatible with the build. 

In order to run the app in a development state, you will need to download Android Studio and some IDE suitible for python I use PyCharm. 
In Android Studio, Create a new project from version control by going to File --> New... --> Project from version control. As pictured below. 
![Android Studio From Version Control Image](https://github.com/obaldwingeil/Greenspaces/blob/main/AS-from-version-control.png?raw=true)

Enter this url https://github.com/obaldwingeil/Greenspaces.git in the URL box and hit `Clone`.

Once you are loaded into android studio, you're ready to set up the backend. Repeat the steps from above in your python IDE to create a new python project from version control using the url for the Greenspaces-backend repo. 

For example, in pycharm, click the Get from Version Control Option displayed here:

Then paste this url: https://github.com/obaldwingeil/Greenspaces-backend.git into the URL box. Hit `Clone` and open the files!
Now navigate to the `read_data.py` file. Run this file and you should get a display message that looks like this: 

```
 * Serving Flask app 'read_data' (lazy loading)
 * Environment: production
   WARNING: This is a development server. Do not use it in a production deployment.
   Use a production WSGI server instead.
 * Debug mode: off
 * Running on all addresses.
   WARNING: This is a development server. Do not use it in a production deployment.
 * Running on http://134.69.212.136:5001/ (Press CTRL+C to quit)
 ```
 
 Copy the url where the server is running. In the example above, that is this url: 
 > http://134.69.212.136:5001/
 
 Navigate back to android studio and open the `strings.xml` file. The path to this file is `app/res/values/strings.xml`
 At the top of the file you should see a line that looks like this: 
 
 Replace the URL with the one you copied from your running server. 
 
 Now you are ready to run the android code and use the app! (Hint: hit the green play button at the top of Android Studio!)
 
