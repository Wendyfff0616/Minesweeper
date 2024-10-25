package minesweeper;

import processing.core.PConstants;
import processing.core.PImage;

import java.util.*;

public class Tile {
    // Tile attributes
    private boolean revealed = false;
    private boolean flagged = false;
    private boolean hasMine = false;
    private int mineNumbers = 0;
    private int explosionStartFrame; // Frame when the explosion starts
    private int x;
    private int y;

    // Images for different tile states
    private PImage tile;
    private PImage tile1;
    private PImage tile2;
    private PImage flagImage;
  

    public Tile(int x, int y, App app) {
        this.x = x;
        this.y = y;

        // Load images
        this.tile = app.getSprite("tile"); //
        this.tile1 = app.getSprite("tile1"); //
        this.tile2 = app.getSprite("tile2"); //
        this.flagImage = app.getSprite("flag");
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    // Reveal the tile
    public void reveal() {
        this.revealed = true;
    }

    // Place a mine on this tile
    public void placeMine() {
        hasMine = true;
    }

    public boolean hasMine() {
        return this.hasMine;
    }

    // Get the number of adjacent mines
    public int getMineNumbers() {
        return this.mineNumbers;
    }

    public void setMineNumbers(int mineNumbers) {
        this.mineNumbers = mineNumbers;
    }

    public int[] getMineCountColor(int number) {
        if (number < 0 || number >= App.mineCountColour.length) {
            return new int[]{0, 0, 0}; // Default to black if out of bounds
        }
        return App.mineCountColour[number];
    }

    // Check if the tile is revealed
    public boolean isRevealed() {
        return this.revealed;
    }

    public void toggleFlag() {

        this.flagged = !this.flagged; // Toggle the flag status
    }

    public boolean isFlagged() {
        return this.flagged;
    }

    // Get the adjacent tiles of the current tile
    public List<Tile> getAdjacentTiles(Tile[][] board) {
        ArrayList<Tile> result = new ArrayList<>();

        if (x + 1 < board[0].length) {
            result.add(board[y][x + 1]);
        }
        if (y + 1 < board.length && x + 1 < board[0].length) {
            result.add(board[y + 1][x + 1]);
        }
        if (y - 1 >= 0 && x + 1 < board[0].length) {
            result.add(board[y - 1][x + 1]);
        }
        if (y + 1 < board.length) {
            result.add(board[y + 1][x]);
        }
        if (y - 1 >= 0) {
            result.add(board[y - 1][x]);
        }
        if (x - 1 >= 0) {
            result.add(board[y][x - 1]);
        }
        if (x - 1 >= 0 && y + 1 < board.length) {
            result.add(board[y + 1][x - 1]);
        }
        if (x - 1 >= 0 && y - 1 >= 0) {
            result.add(board[y - 1][x - 1]);
        }
        return result;
    }
//    public List<Tile> getAdjacentTiles(Tile[][] board, int boardHeight, int boardWidth) {
//        List<Tile> adjacentTiles = new ArrayList<>();
//
//        for (int i = -1; i <= 1; i++) {
//            for (int j = -1; j <= 1; j++) {
//                int newX = this.x + i;
//                int newY = this.y + j;
//
//                if (newX >= 0 && newX < boardWidth && newY >= 0 && newY < boardHeight && !(i == 0 && j == 0)) {
//                    adjacentTiles.add(board[newY][newX]);
//                }
//            }
//        }
//
//        return adjacentTiles;
//    }

    // Deal with mine explosion
    public void startExplosion(App app, int startFrame) {
        this.explosionStartFrame = startFrame; // Record when this explosion should start
    }

    public void draw(App app) {
        PImage imageToDraw = app.getSprite("tile1"); // Default image

        if (revealed) {
            if (hasMine) {
                // Determine the current frame of the explosion animation
                int frameDiff = app.frameCount - explosionStartFrame;
                if (frameDiff >= 0 && frameDiff < 30) { // 10 images, 3 frames each
                    int explosionFrame = frameDiff / 3; // Determine which explosion image to show
                    imageToDraw = app.getSprite("mine" + explosionFrame);
                } else if (frameDiff >= 30) {
                    imageToDraw = app.getSprite("mine9"); // After the last frame, show the final mine image
                }
            } else {
                imageToDraw = tile; // Default revealed tile image
                app.image(imageToDraw, x * App.CELLSIZE, y * App.CELLSIZE + App.TOPBAR);
                if (flagged) { // If flagged, show the flag image
                    imageToDraw = tile1;
                    app.image(imageToDraw, x * App.CELLSIZE, y * App.CELLSIZE + App.TOPBAR);
                    imageToDraw = flagImage;
                    app.image(imageToDraw, x * App.CELLSIZE, y * App.CELLSIZE + App.TOPBAR);
                }
                // Draw number on tile
                if (mineNumbers > 0 ) {
                    app.fill(getMineCountColor(mineNumbers)[0], getMineCountColor(mineNumbers)[1], getMineCountColor(mineNumbers)[2]);
                    app.textSize(20); // Adjust size as needed
                    app.textAlign(PConstants.CENTER, PConstants.CENTER);
                    app.text(mineNumbers, x * App.CELLSIZE + App.CELLSIZE / 2, y * App.CELLSIZE + App.CELLSIZE / 2 + App.TOPBAR);
                    if (flagged) { // If flagged, show the flag image
                        imageToDraw = tile1;
                        app.image(imageToDraw, x * App.CELLSIZE, y * App.CELLSIZE + App.TOPBAR);
                        imageToDraw = flagImage;
                        app.image(imageToDraw, x * App.CELLSIZE, y * App.CELLSIZE + App.TOPBAR);
                    }
                    return; // Ensures number is drawn and exits method to avoid further drawing
                }

            }
        } else if (flagged) { // If flagged, show the flag image
            imageToDraw = tile1;
            app.image(imageToDraw, x * App.CELLSIZE, y * App.CELLSIZE + App.TOPBAR);
            imageToDraw = flagImage;
        }  //else
        if (app.mouseX >= x * App.CELLSIZE && app.mouseX < (x + 1) * App.CELLSIZE &&
                app.mouseY >= y * App.CELLSIZE + App.TOPBAR && app.mouseY < (y + 1) * App.CELLSIZE + App.TOPBAR) {
            if (app.mousePressed && app.mouseButton == PConstants.LEFT) {
                imageToDraw = tile;
            } else if(flagged){
                imageToDraw = tile2;
                app.image(imageToDraw, x * App.CELLSIZE, y * App.CELLSIZE + App.TOPBAR);
                imageToDraw = flagImage;
            }
            else if(imageToDraw!=tile){
                imageToDraw = tile2;
            }
        }

        // Draw the tile image
        app.image(imageToDraw, x * App.CELLSIZE, y * App.CELLSIZE + App.TOPBAR);
    }

    // To handle the reveal method being static, consider making it non-static or ensuring all necessary data is passed as arguments:
    public void reveal(Tile[][] board, int boardHeight, int boardWidth) {
        if (!revealed) {
            revealed = true;

            if (mineNumbers == 0) {
                for (Tile adjTile : getAdjacentTiles(board)) {
                    if (!adjTile.isRevealed() && !adjTile.hasMine()) {
                        adjTile.reveal(board, boardHeight, boardWidth);
                    }
                }
            }
        }
    }
}
