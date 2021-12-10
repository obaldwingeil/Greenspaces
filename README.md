# GreenSpaces
An android mobile app that provides easier, better, faster access to green spaces.

## Methods Overview
Greenspaces is written in Java using Android Studio with a server and web scraper written in python. A MySQL database holds Location, User, Image, and Review tables.

## Running Locally
### Note: This app is configured to run on Android Studio Arctic Fox | 2020.3.1 Patch 3, other versions of android studio may not be compatible with the build. 

In order to run the app in a development state, you will need to download Android Studio and some IDE suitible for python. I use PyCharm. You will also need to set up an instance of a MySQL database. To do this and monitor my database, I used MySQL workbench.

In Android Studio, Create a new project from version control by going to File --> New... --> Project from version control. As pictured below. 

![Android Studio From Version Control Image](https://github.com/obaldwingeil/Greenspaces/blob/main/AS-from-version-control.png?raw=true)

Enter this url https://github.com/obaldwingeil/Greenspaces.git in the URL box and hit `Clone`.

![Android Studio Enter URL Image](https://github.com/obaldwingeil/Greenspaces/blob/main/AS-enter-url.png?raw=true)

Once you are loaded into android studio, you're ready to set up the backend. Repeat the steps from above in your python IDE to create a new python project from version control using the url for the Greenspaces-backend repo. 

For example, in PyCharm, click the `Get from Version Control` option displayed here:

![PyCharm From Version Contrl Image](https://github.com/obaldwingeil/Greenspaces/blob/main/Py-from-version-control.png?raw=true)

Then paste this url: https://github.com/obaldwingeil/Greenspaces-backend.git into the URL box. Hit `Clone` and open the files!

![PyCharm Enter URL Image](https://github.com/obaldwingeil/Greenspaces/blob/main/Py-enter-url.png?raw=true)

To use my web scraper to populate your database and to use my server code to pull data from your database, you will need to set up a connection in the `server.py` file. 

Open the file and locate line 4: 

![DB Connection](https://github.com/obaldwingeil/Greenspaces/blob/main/DB-connection.png?raw=true)

Enter your MySQL credentials to connect to your database. If you are using MySQL workbench, you can find or create your credentials on the Users and Privilages page. 

![MySQL Credentials](https://github.com/obaldwingeil/Greenspaces/blob/main/MySQL-credentials.png?raw=true)

Now to populate your database, open the `write_data.py` file. Scroll down to the main method and uncomment each of the method calls. I suggest running each method one at a time and check that the data is properly uploading into your database. This way, if something goes wrong, you don't have to delete everything to run again. 

![Fill Database](https://github.com/obaldwingeil/Greenspaces/blob/main/fill-database.png?raw=true)

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
 Keep this file running while using the app. 
 
 Copy the url where the server is running. In the example above, that is this url: 
 > http://134.69.212.136:5001/
 
 Navigate back to android studio and open the `strings.xml` file. The path to this file is `app/res/values/strings.xml`
 At the top of the file you should see a line that looks like this: 
 
 ![Android Studio strings.xml Image](https://github.com/obaldwingeil/Greenspaces/blob/main/strings-xml-example.png?raw=true)
 
 Replace the URL with the one you copied from your running server. 
 
 Now you are ready to run the android code and use the app! (Hint: hit the green play button at the top of Android Studio!)
 
