# ASCEND

Author: Fabrice Faustin

Note: This project is an updated version of the original ASCEND project which I was a part of, and which can be found <a href="https://github.com/ChanningAndrews/TeamProject">here.</a>
The main change involves using docker to containerize the server and database parts of the program, which eliminates the need for a user having to install and setup the mysql
database dependencies (xampp, database setup, etc...) in order to properly run the server side of the application. Now the user only has to run the docker image of the server and mysql services, and can start the client side thereafter in no time. 
Some other minor changes is the addition of a shortcut to run the client side directly (in windows so far). The user can copy/paste the shortcut to their desktop and simply click on the icon to boot up the app.


ASCEND is a multi-player game that is inspired by 
the game 'Doodle Jump'. The game involves multiple players who are racing 
to the top of a map. Players can move left to right and can jump vertically. 
Players get to the top by jumping on platforms. Players can also obtain 
collectibles on the way to the top that can be used to attack other players 
and accelerate them to the top of the map. If a player is the first to the top 
or is the last player standing, they are the winner


 ## Installation & Setup


### Client Setup
To setup the ASCEND client, follow the steps below.
```
1. Download the latest version of the ASCEND source.
2. Extract the .ZIP into your desired location for the game
3. Run the 'RunGameGUI.bat' from the 'bat_files' directory.
```

Note: Make sure there is a corresponding GameServer running properly before running the RunGameGUI.bat file to avoid errors.

### Server + DB Setup

The server setup is similar to the client setup but requires you to instantiate a local copy of the database prior to starting the server. The server setup portion utilizes XAMPP to manage the MySQL server.
```
1. Download the latest version of the ASCEND source.
2. Extract the .ZIP into your desired location for the game.
3. Launch XAMPP and start your MySQL server.
4. database part
 4.1 - open xammp and run the mysql server
 4.2 - navigate to  c:\xampp\mysql\bin in you cmd prompt (cd c:\xampp\mysql\bin)
 4.3 - run this statment if you have set up the sql server with student space (mysql -h localhost -u student -p)  password: hello
 4.4 - run the gameServer.sql script to create the table located in the rescources folder 
7. Run RunGameServer.bat from the 'bat_files' directory.
```
### Connecting to Remote Server/Computer

If you want your client session to connect to a server hosted in a different machine, follow these steps:
```
1. Locate the clientConfig.txt file in the resource folder located in the extracted project folder
2. Enter the IP address of the server/machine you wish to connect to.
3. Save the changes to the file
4. Run GameGUI.bat as intended ( refer to Client Setup section )
```


### Editing the Project in IntelliJ Idea

In case the batch files do not work for you, you can import the project into IntelliJ and run it through the IDE.

To edit the project follow these steps: -- should be changed----
```
1. Download and unzip the file in your workspace directory
2. Using import existing projects to import the project into your IDE
3. Add ocsf.jar and mysql-connector to the buildpath of the project
4. Setting up the Server/Client is the same except skip you run the ServerUI or ClientUI classes from within your IDE instead of through the batch files
5. thats it
```

## All that is left is for you to try out our game!!! Thanks for Playing!

```
