# Fighter-Jet-Game-in-Java

A simple 2D Java-based fighter game with high-score tracking using SQLite for persistence. The game is implemented with Swing for the GUI, and features an animated fighter character that the player must navigate to avoid obstacles.

Features
Single-player Gameplay: Navigate a fighter character using the keyboard to avoid obstacles.
Score Tracking: Maintains a database of player scores, storing each player's latest and high scores.
SQLite Database: Uses SQLite for persistent data storage, creating a table player_scores to save individual player records.
Dynamic Background: Changes based on score thresholds to enhance the visual experience.

Here's an outline of the main components:

App.java
Sets up the main game window using JFrame.
Prompts the user to enter a username.
Instantiates the Fighter_Game class with the entered username and displays it within the JFrame.

Fighter_Game.java
Implements the game's logic and graphical rendering.
Contains classes for the main Fighter character and Enemy entities, which appear as obstacles.
Handles game state, including score tracking, player movements, and enemy placements.
Uses SQLite to store player scores in a table called player_scores.
Allows loading and updating of each player’s latest and high scores in the database.
