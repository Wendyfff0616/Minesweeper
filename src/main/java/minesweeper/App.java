package minesweeper;

import processing.core.PApplet;
import processing.core.PImage;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

import java.util.*;

public class App extends PApplet {

    public static final int CELLSIZE = 32;
    public static final int TOPBAR = 64;
    public static int WIDTH = 864;
    public static int HEIGHT = 640;
    public static final int BOARD_WIDTH = WIDTH / CELLSIZE;
    public static final int BOARD_HEIGHT = (HEIGHT - TOPBAR) / CELLSIZE;
    public static final int FPS = 30;

    static int mineCount = 100; // Default number of mines

    public long startTime;
    public long endTime; // Final time when the game ends
    public boolean timeRunning = true;
    public boolean gameOver = false;

    private Tile[][] board;
    public int frameCount;
    private HashMap<String, PImage> sprites = new HashMap<>();

    public String configPath;

    public static int[][] mineCountColour = new int[][] {
            {0,0,0}, // 0 is not shown
            {0,0,255},
            {0,133,0},
            {255,0,0},
            {0,0,132},
            {132,0,0},
            {0,132,132},
            {132,0,132},
            {32,32,32}
    };
    //----------------------------------------------------------------------------------------------
    public App() {
        this.configPath = "config.json";
    }

    /**
     * Initialise the setting of the window size.
     */
    @Override
    public void settings() {
        size(WIDTH, HEIGHT);
    }

    public Tile[][] getBoard() {
        return this.board;
    }

    public PImage getSprite(String s) {
        PImage result = sprites.get(s);
        if (result == null) {
            String path = this.getClass().getResource(s + ".png").getPath().toLowerCase(Locale.ROOT).replace("%20", " ");
            result = loadImage(path);
            sprites.put(s, result);
        }
        return result;
    }

    /**
     * Load all resources such as images. Initialise the elements such as the player and map elements.
     */
    @Override
    public void setup() {
        frameRate(FPS);

        // Load images
        String[] sprites = new String[] {"tile1", "tile2", "flag", "tile"};
        
        for (int i = 0; i < sprites.length; i++) {
            getSprite(sprites[i]);
        }
        for (int i = 0; i < 10; i++) {
            getSprite("mine"+i);
        }

        // Initialize the board
        this.board = new Tile[(HEIGHT - TOPBAR) / CELLSIZE][WIDTH / CELLSIZE];
        List<int[]> availablePositions = new ArrayList<>();

        for (int i = 0; i < this.board.length; i++) {
            for (int j = 0; j < this.board[i].length; j++) {
                this.board[i][j] = new Tile(j, i, this);
                availablePositions.add(new int[]{i, j});
            }
        }

        // Shuffle positions and place mines
        Collections.shuffle(availablePositions);
        for (int i = 0; i < mineCount; i++) {
            int[] pos = availablePositions.get(i);
            this.board[pos[0]][pos[1]].placeMine();
        }

        // Set mine numbers
        for (int i = 0; i < this.board.length; i++) {
            for (int j = 0; j < this.board[i].length; j++) {
                Tile tile = this.board[i][j];
                if (!tile.hasMine()) {
                    int count = 0;
                    for (Tile adjTile : tile.getAdjacentTiles(this.board)) {
                        if (adjTile.hasMine()) {
                            count++;
                        }
                    }
                    tile.setMineNumbers(count);
                }
            }
        }
        // If not pass the board, the method won't know the size of the board and the layout of the grid

        // Set start time
        startTime = millis();
    }

    /**
     * Receive key pressed signal from the keyboard.
     */
    @Override
    public void keyPressed(KeyEvent event){
        if (key == 'r' || key == 'R') {
            gameOver = false; // Reset the game over state
            timeRunning = true;
            startTime = millis();
            setup();
        }
    }

    /**
     * Receive key released signal from the keyboard.
     */
    @Override
    public void keyReleased(){

    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (gameOver || checkWinCondition()) {
            return; // Do nothing if the game is over
        }

        int mouseX = e.getX();
        int mouseY = e.getY() - TOPBAR;  // Adjust for the top bar

        // Ensure the click is within the game board
        if (mouseX >= 0 && mouseX < WIDTH && mouseY >= 0 && mouseY < HEIGHT - TOPBAR) {
            Tile t = board[mouseY / CELLSIZE][mouseX / CELLSIZE];

            if (mouseButton == LEFT && !t.isFlagged()) {
                revealTile(t);
            } else if (mouseButton == RIGHT) {
                t.toggleFlag();
            }
          
        }
    }

    public void revealTile(Tile t) {
        if (!t.isRevealed()) {
            if (t.hasMine()) {
                gameOver = true;
                revealAllMines();
            }

            t.reveal(board, BOARD_HEIGHT, BOARD_WIDTH);
        }

        if (checkWinCondition()) {
            timeRunning = false; // Stop the timer on win as well
            endTime = (millis() - startTime) / 1000;
        }
    }


    @Override
    public void mouseReleased(MouseEvent e) {

    }

    private void revealAllMines() {
        timeRunning = false;
        endTime = (millis() - startTime) / 1000;

        int frameOffset = 0; // Start offset for animation timing
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                if (board[i][j].hasMine()) {
                    // Start the explosion animation with a delay
                    board[i][j].startExplosion(this,frameCount + frameOffset);
                    board[i][j].reveal();
                    frameOffset += 3; // Increment offset to stagger the explosions
                }
            }
        }
    }

    private void displayGameOverMessage() {
        textSize(30);
        fill(0);
        text("You lost!", 150, App.TOPBAR - 25);
    }

    private boolean checkWinCondition() {
        int nonRevealedNonMines = 0;

        for (int i = 0; i < this.board.length; i++) {
            for (int j = 0; j < this.board[i].length; j++) {
                Tile tile = this.board[i][j];
                if ((!tile.isRevealed() || tile.isFlagged()) && !tile.hasMine()) {
                    nonRevealedNonMines += 1;
                }
            }
        }

        return nonRevealedNonMines == 0;
    }

    private void displayWinMessage() {
        textSize(30);
        fill(0);
        text("You win!", 150, App.TOPBAR - 25);
    }

    /**
     * Draw all elements in the game by current frame.
     */
    @Override
    public void draw() {
        background(200, 200, 200);
        frameCount++;

        for (int i = 0; i < this.board.length; i++) {
            for (int j = 0; j < this.board[i].length; j++) {
                this.board[i][j].draw(this);
            }
        }

        // Handle game over and win conditions
        if (gameOver) {
            displayGameOverMessage();
        } else if (checkWinCondition()) {
            displayWinMessage();
        }

        drawTimer();
    }

    private void drawTimer() {
        long elapsedTime = timeRunning ? (millis() - startTime) / 1000 : endTime; // Use endTime if time is not running
        textSize(30);
        fill(0);
        textAlign(CENTER, CENTER);
        text("Time: " + elapsedTime, 604, App.TOPBAR - 25);
    }

    public static void main(String[] args) {
        // Input "gradle run --args='1'"

        if (args.length > 0) {
            try {
                mineCount = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid number of mines. Using default value.");
            }
        }
        PApplet.main("minesweeper.App");
    }

}