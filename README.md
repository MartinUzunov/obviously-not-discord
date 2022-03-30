# Obviously Not Discord

A fully functional Discord clone implemented in Java with the help of the JavaFX library. The application implements the Client-Server model and is using MySQL as a database. The user can create an account or log in if an account already exists, add other users as friends, create groups, invite users to the group, add categories and channels to the groups, send personal and group messages, change personal and group profile photos, block users, change his activity status and see friends activity status.
<br></br>
![ObviouslyNotDiscord](https://github.com/MartinUzunov/obviously-not-discord/blob/master/discord.gif)

## How to start the application
There are 2 options to run the game.

- Using your IDE (IntelliJ IDEA / Eclipse / Netbeans)
- Using gradle
<br></br>

### Run the Application in IntelliJ Idea(needs pre-downloaded javafx modules)

To run the applicatin from within IntelliJ IDEA you must first create a new project in IntelliJ and set the root
directory to the directory into which you have cloned this Git repository. 
<br></br>
Second, you must download JavaFX and unzip the distribution to some directory.
<br></br>
Third, you must add all the JAR files found in the "lib" directory to your project's classpath.
<br></br>
Fourth, start the server by running the Server class.

You start the client by executing the Main class.
- A client cannot be started if the server is shut down.
- It is possible to start multiple instances of the client on a single machine.
<br></br>

## License

Licensed under the [MIT License](https://github.com/MartinUzunov/obviously-not-discord/blob/master/LICENSE.md).
