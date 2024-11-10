import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import java.io.*;
import java.util.List;
import javax.swing.*;
import java.sql.*;

public class Fighter_Game extends JPanel implements ActionListener, KeyListener {
    int boardWidth = 1000;
    int boardHeight = 700;

    //images
    Image backgroundImg;
    Image PlaneImg;
    Image BImg;
    Image CImg;

    //Fighter class
    int PlaneX = boardWidth/8;
    int PlaneY = boardWidth/2;
    int PlaneWidth = 60;
    int PlaneHeight = 52;

    class Fighter {
        int x = PlaneX;
        int y = PlaneY;
        int width = PlaneWidth;
        int height = PlaneHeight;
        Image img;

        Fighter(Image img) {
            this.img = img;
        }
    }

    //Enemy class
    int EnemyX = boardWidth;
    int EnemyY = 0;
    int EnemyWidth = 64; 
    int EnemyHeight = 512;
    
    class Enemy {
        int x = EnemyX;
        int y = EnemyY;
        int width = EnemyWidth;
        int height = EnemyHeight;
        Image img;
        boolean passed = false;

        Enemy(Image img) {
            this.img = img;
        }
    }

    //game logic
    Fighter F;
    int velocityX = -4; //move left speed 
    int velocityY = 0; //move up/down speed.
    int gravity = 1;

    ArrayList<Enemy> enemies;
    Random random = new Random();

    Timer gameLoop;
    Timer placeEnemyTimer;
    boolean gameOver = false;
    double score = 0;

    private String playerName;
    private int latestScore;
    private int highScore;

    Fighter_Game(String playerName) {
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setFocusable(true);
        addKeyListener(this);

        //load images
        backgroundImg = new ImageIcon(getClass().getResource("./sunset_game.png")).getImage();
        PlaneImg = new ImageIcon(getClass().getResource("./Hero_Fighter.png")).getImage();
        BImg = new ImageIcon(getClass().getResource("./top.png")).getImage();
        CImg = new ImageIcon(getClass().getResource("./bottom.png")).getImage();

        //Fighter
        F = new Fighter(PlaneImg);
        enemies = new ArrayList<Enemy>();

        //place Enemy timer
        placeEnemyTimer = new Timer(1500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
              // Code to be executed
              placeEnemy();
            }
        });
        placeEnemyTimer.start();
        
		//game timer
		gameLoop = new Timer(1000/60, this); //how long it takes to start timer, milliseconds gone between frames 
        gameLoop.start();
        
        this.playerName = playerName;
        loadPlayerData();
    }

    private void loadPlayerData() {
        String url = "jdbc:sqlite:game.db";
        
        try (Connection conn = DriverManager.getConnection(url)) {
            // Create table if it doesn't exist
            String createTable = """
                CREATE TABLE IF NOT EXISTS player_scores (
                    player_name TEXT PRIMARY KEY,
                    latest_score INTEGER,
                    high_score INTEGER
                )
                """;
            
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(createTable);
            }
            
            // Query player data
            String query = "SELECT latest_score, high_score FROM player_scores WHERE player_name = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, playerName);
                ResultSet rs = pstmt.executeQuery();
                
                if (rs.next()) {
                    latestScore = rs.getInt("latest_score");
                    highScore = rs.getInt("high_score");
                } else {
                    // New player, initialize scores to 0
                    latestScore = 0;
                    highScore = 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            latestScore = 0;
            highScore = 0;
        }
    }

    private void savePlayerData() {
        String url = "jdbc:sqlite:game.db";
        
        try (Connection conn = DriverManager.getConnection(url)) {
            String upsert = """
                INSERT INTO player_scores (player_name, latest_score, high_score)
                VALUES (?, ?, ?)
                ON CONFLICT(player_name) 
                DO UPDATE SET latest_score = ?, high_score = ?
                """;
                
            try (PreparedStatement pstmt = conn.prepareStatement(upsert)) {
                pstmt.setString(1, playerName);
                pstmt.setInt(2, latestScore);
                pstmt.setInt(3, highScore);
                pstmt.setInt(4, latestScore);
                pstmt.setInt(5, highScore);
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    void placeEnemy() {
        //(0-1) * EnemyHeight/2.
        // 0 -> -128 (EnemyHeight/4)
        // 1 -> -128 - 256 (EnemyHeight/4 - EnemyHeight/2) = -3/4 EnemyHeight
        int randomEnemyY = (int) (EnemyY - EnemyHeight/4 - Math.random()*(EnemyHeight/2));
        int openingSpace = boardHeight/4;
    
        Enemy topEnemy = new Enemy(BImg);
        topEnemy.y = randomEnemyY;
        enemies.add(topEnemy);
    
        Enemy bottomEnemy = new Enemy(CImg);
        bottomEnemy.y = topEnemy.y  + EnemyHeight + openingSpace;
        enemies.add(bottomEnemy);
    }
    
    
    public void paintComponent(Graphics g) {
		super.paintComponent(g);
		draw(g);
	}

	public void draw(Graphics g) {
        //background
        g.drawImage(backgroundImg, 0, 0, this.boardWidth, this.boardHeight, null);

        //Fighter
        g.drawImage(PlaneImg, F.x, F.y, F.width, F.height, null);

        //Enemy
        for (int i = 0; i< enemies.size(); i++) {
            Enemy enemy = enemies.get(i);
            g.drawImage(enemy.img, enemy.x, enemy.y, enemy.width, enemy.height, null);
        }

        //score
        g.setColor(Color.white);

        g.setFont(new Font("Arial", Font.PLAIN, 32));
        if (gameOver) {
            g.drawString("Game Over: " + String.valueOf((int) score), 775, 50);
        }
        else {
            g.drawString(String.valueOf((int) score), 950, 50);
        }
        
        // Draw player name, latest score, and high score
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("Player: " + playerName, 10, 20);
        g.drawString("Last Score: " + latestScore, 10, 45);
        g.drawString("High Score: " + highScore, 10, 70);
    }

    public void move() {
        //Fighter
        velocityY += gravity;
        F.y += velocityY;
        F.y = Math.max(F.y, 0); //apply gravity 

        //Enemy
        for (int i = 0; i < enemies.size(); i++) {
            Enemy enemy = enemies.get(i);
            enemy.x += velocityX;

            if (!enemy.passed && F.x > enemy.x + enemy.width) {
                score += 0.5; //0.5 because there are 2 set of Enemies! so 0.5*2 = 1
                enemy.passed = true;
            }

            if (collision(F, enemy)) {
                gameOver = true;
            }
        }

        if (F.y > boardHeight) {
            gameOver = true;
        }
    }

    boolean collision(Fighter a, Enemy b) {
        return a.x < b.x + b.width &&   //a's top left corner doesn't reach b's top right corner
               a.x + a.width > b.x &&   //a's top right corner passes b's top left corner
               a.y < b.y + b.height &&  //a's top left corner doesn't reach b's bottom left corner
               a.y + a.height > b.y;    //a's bottom left corner passes b's top left corner
    }

    @Override
    public void actionPerformed(ActionEvent e) { //called every x milliseconds by gameLoop timer
        move();
        repaint();
        if(score%10 >= 5 &&  score%10 <= 9) {
            backgroundImg = new ImageIcon(getClass().getResource("./day.png")).getImage();
        }   else {
            backgroundImg = new ImageIcon(getClass().getResource("./sunset_game.png")).getImage();
        }
        if (gameOver) {
            latestScore = (int) score;
            if (score > highScore) {
                highScore = (int) score;
            }
            savePlayerData();
            placeEnemyTimer.stop();
            gameLoop.stop();
        }
    }  

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            velocityY = -9;

            if (gameOver) {
                //restart game by resetting conditions
                F.y = PlaneY;
                velocityY = 0;
                enemies.clear();  
                gameOver = false;
                score = 0;
                gameLoop.start();
                placeEnemyTimer.start();
            }
        }
    }

    //not needed
    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {}
}
