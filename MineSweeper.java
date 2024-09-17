import java.util.ArrayList;
import java.util.Random;

import tester.*;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;




//the class represents the real game
class Game extends World {

  int numRows;
  int numCols; 
  Random rand;
  ArrayList<ArrayList<Cell>> cells;
  int cellWidth = 25;
  int cellHeight = 25;


  //constructor of Game
  Game(int rows, int cols, int mines, Random r) {
    this.numRows = rows;
    this.numCols = cols;
    this.rand = r;

    //initialize the board at first
    this.cells = new ArrayList<ArrayList<Cell>>();
    for (int i = 0; i < cols; i += 1) {
      ArrayList<Cell> row = new ArrayList<>(); 
      for (int j = 0; j < rows; j += 1) {
        row.add(new Cell(false));
      }
      this.cells.add(row);
    }
    //place the mines inside the cells
    this.placeMines(rows, cols, mines);
    this.linkNeighbours();

  }




  //convenience constructor(for testing related to randomness)
  Game(int rows, int cols, int mines) {
    this(rows, cols, mines, new Random());
  }


  //third convenience constructor (only for testing makeScene)
  Game(int rows, int cols, int mines, ArrayList<ArrayList<Cell>> cells) {
    this.numRows = rows;
    this.numCols = cols;
    this.cells = cells;
  }


  //EFFECT: randomly place the mines within the game grid
  public void placeMines(int rows, int cols, int mines) {
    ArrayList<Posn> minesLocation = new ArrayList<Posn>();

    while (minesLocation.size() < mines) {
      int rowAt = rand.nextInt(rows);
      int colAt = rand.nextInt(cols);
      Posn position = new Posn(colAt, rowAt);

      if (!minesLocation.contains(position)) {
        minesLocation.add(position);
        cells.get(colAt).set(rowAt, new Cell(true));
      }
    }
  }


  //handles the drawing of the grid
  //make the scene of a customized game board
  public WorldScene makeScene() {
    WorldScene w = new WorldScene(cellWidth * numRows, cellHeight * numCols);

    for (int i = 0; i < numCols; i += 1) {
      for (int j = 0; j < numRows; j += 1) {
        Cell currentCell = cells.get(i).get(j);
        int xLoc = (i * cellWidth) + (cellWidth / 2);
        int yLoc = (j * cellHeight) + (cellHeight / 2);
        //Creates a black outline around cells:
        w.placeImageXY(new RectangleImage(cellWidth, cellHeight, 
            "solid", Color.BLACK), xLoc, yLoc);
        w.placeImageXY(currentCell.draw(cellWidth - 2, cellHeight - 2), xLoc, yLoc);

      }
    }
    return w; 
  }


  //determine whether the game is win
  public boolean win() {
    return (this.cellsRemain() == 0);
  }





  //determine whether the game is lost
  public boolean lost() {
    for (int i = 0; i < this.numCols; i += 1) {
      for (int j = 0; j < this.numRows; j += 1) { 
        Cell curCell = this.cells.get(i).get(j);
        if (curCell.isCellLoss()) {
          return true;
        }
      }
    }
    return false;
  }


  //check how many flat cells are unclicked
  public int cellsRemain() {
    int remain = 0;
    for (int i = 0; i < this.numCols; i++) {
      for (int j = 0; j < this.numRows; j++) {
        Cell curCell = this.cells.get(i).get(j);
        remain += curCell.singleCellRemain();
      }
    }
    return remain;
  }



  //EFFECT: link the neighbours between each other
  public void linkNeighbours() {
    for (int i = 0; i < this.numCols; i += 1) {
      for (int j = 0; j < this.numRows; j += 1) { 
        Cell curCell = this.cells.get(i).get(j); 
        int xMin = i - 1; 
        if (xMin < 0) { 
          xMin = 0; 
        }
        int xMax = i + 1; 
        if (xMax > numCols - 1) {
          xMax = numCols - 1;
        }
        int yMin = j - 1; 
        if (yMin < 0) {
          yMin = 0;
        }
        int yMax = j + 1; 
        if (yMax > numRows - 1) {
          yMax = numRows - 1;
        }
        for (int x = xMin; x <= xMax; x += 1) {
          for (int y = yMin; y <= yMax; y += 1) { 
            Cell neighbour = this.cells.get(x).get(y); 
            if (!(x == i && y == j)) { 
              curCell.addNeighbour(neighbour); 
            }
          }
        }
      }
    }
  }

  


  //EFFECT: handles the mouse event, the actual implementation
  //is delegated to Cell class
  public void onMouseClicked(Posn pos, String buttonName) {
    if (pos.x > 0 && pos.x < (cellWidth * numCols) 
        && pos.y > 0 && pos.y < (cellHeight * numRows)) {
      Cell curCell = this.cells.get(pos.x / cellWidth).get(pos.y / cellHeight);
      curCell.onMouseDelegate(buttonName);
    } 
  }


  //Method to create a scene when the player wins
  public WorldScene makeWinScene() {
    WorldScene winScene = this.makeScene(); 

    TextImage winMessage = new TextImage("NICE", 20, FontStyle.BOLD, Color.GREEN);
    winScene.placeImageXY(winMessage, (cellWidth * numCols / 2), (cellHeight * numRows / 2));
    return winScene;
  }


  //Method to create a scene when the player loses
  public WorldScene makeLostScene() {
    WorldScene lostScene = this.makeScene(); 

    // Reveal all mine cells
    for (int i = 0; i < this.numCols; i++) {
      for (int j = 0; j < this.numRows; j++) {
        Cell curCell = this.cells.get(i).get(j);
        if (curCell.revealFinal() == 1) {
          lostScene.placeImageXY(curCell.draw(cellWidth, cellHeight), 
                                  i * cellWidth + (cellWidth / 2), 
                                  j * cellHeight + (cellHeight / 2));
        }
      }
    }

    TextImage lostMessage = new TextImage("U BETTER TRY AGAIN", 20, FontStyle.BOLD, Color.RED);
    lostScene.placeImageXY(lostMessage, (cellWidth * numCols / 2), (cellHeight * numRows / 2));

    return lostScene;
  }


  //End the world/continue based on win/loss condition
  public WorldEnd worldEnds() {
    if (this.win()) {
      return new WorldEnd(true, this.makeWinScene());
    }
    if (this.lost()) {
      return new WorldEnd(true, this.makeLostScene());
    }
    else {
      return new WorldEnd(false, this.makeScene());
    }
  }



}




//represents the cells(empty or with mine)
class Cell {

  ArrayList<Cell> neighbours;
  boolean isClicked;
  boolean isFlagged;
  boolean isMine;
  int numMines;

  //primary constructor of Cell
  Cell(ArrayList<Cell> neighbours, boolean isClicked, 
      boolean isFlagged, boolean isMine, int numMines) {
    this.neighbours = neighbours;
    this.isClicked = isClicked;
    this.isFlagged = isFlagged;
    this.isMine = isMine;
    this.numMines = numMines;
  }


  //convenience constructor for testing
  Cell(boolean isMine) {
    this.neighbours = new ArrayList<Cell>();
    this.isClicked = false;
    this.isFlagged = false;
    this.isMine = isMine;
    this.numMines = 0;
  }



  //EFFECT: add neighbours to current cell
  //and link between each other
  public void addNeighbour(Cell c) {
    if (!this.neighbours.contains(c)) {
      this.neighbours.add(c);
      c.addNeighbourUnchecked(this);
    }
  }


  //EFFECT: add given cell to this cell's neighbours only
  public void addNeighbourUnchecked(Cell c) {
    this.neighbours.add(c);
  }


  //calculate the number of neighbor mines
  //around this
  public int numNeighborMines() {
    this.numMines = 0;
    for (Cell c : this.neighbours) {
      if (c.isMine) {
        this.numMines += 1;
      }
    }
    return this.numMines;
  }


  //draw the single cell based on click/flag/flat cell/mine property
  public WorldImage draw(int cellWidth, int cellHeight) {
    WorldImage flag = new EquilateralTriangleImage(cellWidth / 2, OutlineMode.SOLID,
        Color.GREEN);
    WorldImage mine = new CircleImage(cellWidth / 2, OutlineMode.SOLID, Color.ORANGE);
    WorldImage unFlipped = new RectangleImage(cellWidth, cellHeight, OutlineMode.SOLID, 
        Color.GRAY);
    WorldImage flipped = new RectangleImage(cellWidth, cellHeight, OutlineMode.SOLID, 
        Color.darkGray);

    //check for not clicked
    if (!this.isClicked && this.isFlagged) {
      return new OverlayImage(flag, unFlipped);
    }
    if (!this.isClicked) {
      return unFlipped;
    }
    else if (!this.isMine) {
      String valueOfMine = Integer.toString(this.numNeighborMines());
      if (this.numNeighborMines() == 0) {
        return flipped;
      } 
      if (this.numNeighborMines() <= 2) {
        return new OverlayImage(new TextImage(valueOfMine, cellWidth / 2, 
            FontStyle.BOLD, Color.CYAN), flipped);
      }
      else {
        return new OverlayImage(new TextImage(valueOfMine, cellWidth / 2, 
            FontStyle.BOLD, Color.RED), flipped);
      } 
    }
    else {
      return new OverlayImage(mine, unFlipped);
    }   
  }



  //EFFECT: recursively traverse and reveal 
  //neighboring cells in a Minesweeper grid
  //until have adjacent mines beside
  public void floodFill() {
    if (this.numNeighborMines() > 0) {
      this.isClicked = true;
    }
    else { 
      this.isClicked = true;
      for (int i = 0; i < this.neighbours.size(); i += 1) {
        Cell currentNeigh = this.neighbours.get(i);
        if (!currentNeigh.isClicked && !currentNeigh.isMine 
            && !currentNeigh.isFlagged) { 
          currentNeigh.floodFill();
        }
      }
    }
  }



  //EFFECT: change the mine's isClicked
  //state to true and check if it is mine cell
  //using value-expressions
  public int revealFinal() {
    int count = 0;
    if (this.isMine) {
      this.isClicked = true;
      count += 1;
    }
    return count;
  }



  //EFFECT: call floddFill effect or change
  //states corresponding to different scenarios
  public void onMouseDelegate(String buttonName) {
    if (buttonName.equals("LeftButton")) {
      if (!this.isMine && !this.isFlagged) {
        this.floodFill();
      }
      else {
        if (!this.isFlagged && this.isMine) {
          this.isClicked = true; 
        }
      }
    }
    if (buttonName.equals("RightButton")) {
      if (!this.isFlagged && !this.isClicked) {
        this.isFlagged = true;
      }
      else {
        this.isFlagged = false;
      }
    }
  }


  //check if the minecell is clicked
  public boolean isCellLoss() {
    return this.isMine && this.isClicked;
  }


  //check if the cell is not clicked
  //by incrementing by 1
  public int singleCellRemain() {
    int remainingCells = 0;
    if (!this.isClicked && !this.isMine) {
      remainingCells += 1;
    }
    return remainingCells;
  }



}




//the class for examples and tests
class ExamplesMineSweeper {

  Cell cell1;
  Cell cell2;
  Cell cell3;
  Cell cell4;
  Cell cell5; 
  Cell cell6;
  Cell cell7;
  Cell cell8;
  Cell cell9;
  Cell cell10;
  Cell cell11;
  Cell cell12;
  Cell cell13;
  Cell cell14;
  Cell cell15;
  Cell cell16;
  Cell cell17;

  ArrayList<Cell> neighbourL;
  ArrayList<Cell> neighbourL2;
  ArrayList<Cell> neighbourL3;
  ArrayList<Cell> neighbourL4;
  ArrayList<Cell> neighbourL5;

  WorldImage flag;
  WorldImage mine;
  WorldImage unFlipped;
  WorldImage flipped;

  Random ran;
  Game g1;
  Game g2;
  Game g3;
  Game g4;
  Game g5;
  Game g6;

  ArrayList<Cell> rows1Test1;
  ArrayList<Cell> rows2Test1;
  ArrayList<ArrayList<Cell>> cellsTest1;
  ArrayList<Cell> cols1Test2;
  ArrayList<ArrayList<Cell>> cellsTest2;
  ArrayList<Cell> cols1Test3;
  ArrayList<Cell> cols2Test3;
  ArrayList<ArrayList<Cell>> cellsTest3;

  WorldScene ws1;
  WorldScene ws2;
  WorldScene ws3;

  //initial conditions
  void initConditions() {
    this.cell1 = new Cell(false);
    this.cell2 = new Cell(false);
    this.cell3 = new Cell(true);
    this.cell4 = new Cell(true);
    this.cell9 = new Cell(true);

    this.neighbourL = new ArrayList<Cell>();
    this.neighbourL2 = new ArrayList<Cell>();
    this.neighbourL3 = new ArrayList<Cell>();
    this.neighbourL4 = new ArrayList<Cell>();
    this.neighbourL5 = new ArrayList<Cell>();

    //flat cell not clicked by flagged
    this.cell5 = new Cell(neighbourL, false, true, false, 0);
    //flat cell not clicked, not flagged
    this.cell6 = new Cell(neighbourL, false, false, false, 0);
    //flat cell clicked and flagged
    this.cell7 = new Cell(neighbourL, true, true, false, 0);
    //flat cell clicked and not flagged with 0 mine neighbour
    this.cell8 = new Cell(neighbourL, true, false, false, 0);

    neighbourL3.add(cell3);
    neighbourL3.add(cell4);
    //flat cell clicked and not flagged with 2 mine neighbour
    this.cell10 = new Cell(neighbourL3, true, false, false, 2);

    neighbourL4.add(cell9);
    neighbourL4.add(cell3);
    neighbourL4.add(cell4);
    neighbourL5.add(cell1);
    neighbourL5.add(cell2);
    //flat cell clicked and not flagged with 3 mine neighbour
    this.cell11 = new Cell(neighbourL4, true, false, false, 3);

    //Mine cell not clicked by flagged
    this.cell12 = new Cell(neighbourL2, false, true, true, 0);
    //Mine cell not clicked, not flagged
    this.cell13 = new Cell(neighbourL2, false, false, true, 0);
    //Mine cell clicked and flagged
    this.cell14 = new Cell(neighbourL2, true, true, true, 0);
    //Mine cell clicked and not flagged with 0 mine neighbour
    this.cell15 = new Cell(neighbourL2, true, false, true, 0);
    //Mine cell clicked and not flagged with > 0 mine neighbour
    this.cell16 = new Cell(neighbourL3, true, false, true, 2);

    this.cell17 = new Cell(neighbourL5, false, false, false, 0);

    //only for testing, set constant as 25 for now
    this.flag = new EquilateralTriangleImage(25 / 2, OutlineMode.SOLID,
        Color.GREEN);
    this.mine = new CircleImage(25 / 2, OutlineMode.SOLID, Color.ORANGE);
    this.unFlipped = new RectangleImage(25, 25, OutlineMode.SOLID, 
        Color.GRAY);
    this.flipped = new RectangleImage(25, 25, OutlineMode.SOLID, 
        Color.darkGray);

    this.ran = new Random(2);



    this.ws1 = new WorldScene(50, 50);
    this.ws2 = new WorldScene(50, 25);
    this.ws3 = new WorldScene(25, 50);

    this.rows1Test1 = new ArrayList<Cell>();
    this.rows2Test1 = new ArrayList<Cell>();
    this.cellsTest1 = new ArrayList<ArrayList<Cell>>();
    this.cols1Test2 = new ArrayList<Cell>();
    this.cellsTest2 = new ArrayList<ArrayList<Cell>>();
    this.cols1Test3 = new ArrayList<Cell>();
    this.cols2Test3 = new ArrayList<Cell>();
    this.cellsTest3 = new ArrayList<ArrayList<Cell>>();

    this.rows1Test1.add(new Cell(neighbourL2, true, false, false, 0));
    this.rows1Test1.add(new Cell(neighbourL2, false, false, false, 0));
    this.rows2Test1.add(new Cell(neighbourL2, false, false, false, 0));
    this.rows2Test1.add(new Cell(neighbourL2, false, true, false, 0));
    this.cellsTest1.add(rows1Test1);
    this.cellsTest1.add(rows2Test1);
    this.g1 = new Game(2, 2, 0, cellsTest1);

    this.cols1Test2.add(new Cell(neighbourL2, true, false, true, 0));
    this.cols1Test2.add(new Cell(neighbourL2, false, true, true, 0));
    this.cellsTest2.add(cols1Test2);
    this.g2 = new Game(2, 1, 2, cellsTest2);

    this.cols1Test3.add(new Cell(neighbourL2, true, false, true, 0));
    this.cols2Test3.add(new Cell(neighbourL2, false, true, false, 0));
    this.cellsTest3.add(cols1Test3);
    this.cellsTest3.add(cols2Test3);
    this.g3 = new Game(1, 2, 1, cellsTest3);

    this.g4 = new Game(2, 2, 2, ran);
    this.g5 = new Game(2, 1, 1, ran);
    this.g6 = new Game(1, 2, 0, ran);
  }



  //test the method addNeighbourUnchecked(ICell c) 
  void testAddNeighbourUnchecked(Tester t) {
    this.initConditions();
    //the size here is just for testing 
    //visualize the number of neighbours
    t.checkExpect(cell1.neighbours.size(), 0);
    cell1.addNeighbourUnchecked(cell2);
    t.checkExpect(cell1.neighbours.size(), 1);
    cell1.addNeighbourUnchecked(cell3);
    t.checkExpect(cell1.neighbours.size(), 2);

    t.checkExpect(cell2.neighbours.size(), 0);
    //only for testing
    cell2.addNeighbourUnchecked(cell2);
    cell2.addNeighbourUnchecked(cell3);
    cell2.addNeighbourUnchecked(cell1);
    t.checkExpect(cell2.neighbours.size(), 3);
  }



  //test the method addNeighbour(Cell c)
  void testAddNeighbour(Tester t) {
    this.initConditions();
    t.checkExpect(cell1.neighbours.size(), 0);
    t.checkExpect(cell2.neighbours.size(), 0);
    cell1.addNeighbour(cell2);
    t.checkExpect(cell1.neighbours.size(), 1);
    t.checkExpect(cell2.neighbours.size(), 1);
    t.checkExpect(cell3.neighbours.size(), 0);
    cell1.addNeighbour(cell3);
    t.checkExpect(cell1.neighbours.size(), 2);
    t.checkExpect(cell2.neighbours.size(), 1);
    t.checkExpect(cell3.neighbours.size(), 1);

    this.initConditions();
    t.checkExpect(cell3.neighbours.size(), 0);
    t.checkExpect(cell2.neighbours.size(), 0);
    cell3.addNeighbour(cell2);
    t.checkExpect(cell3.neighbours.size(), 1);
    t.checkExpect(cell2.neighbours.size(), 1);
    t.checkExpect(cell4.neighbours.size(), 0);
    cell3.addNeighbour(cell4);
    t.checkExpect(cell3.neighbours.size(), 2);
    t.checkExpect(cell2.neighbours.size(), 1);
    t.checkExpect(cell4.neighbours.size(), 1);
    t.checkExpect(cell1.neighbours.size(), 0);
    cell3.addNeighbour(cell1);
    t.checkExpect(cell3.neighbours.size(), 3);
    t.checkExpect(cell2.neighbours.size(), 1);
    t.checkExpect(cell4.neighbours.size(), 1);
    t.checkExpect(cell1.neighbours.size(), 1);
    cell2.addNeighbour(cell1);
    t.checkExpect(cell3.neighbours.size(), 3);
    t.checkExpect(cell2.neighbours.size(), 2);
    t.checkExpect(cell4.neighbours.size(), 1);
    t.checkExpect(cell1.neighbours.size(), 2);
  }


  //test method of numNeighborMines()
  void testNumNeighborMines(Tester t) {
    this.initConditions();
    t.checkExpect(cell3.numNeighborMines(), 0);
    cell3.addNeighbour(cell2);
    t.checkExpect(cell3.numNeighborMines(), 0);
    cell3.addNeighbour(cell4);
    t.checkExpect(cell3.numNeighborMines(), 1);
    cell3.addNeighbour(cell1);
    t.checkExpect(cell3.numNeighborMines(), 1);

    this.initConditions();
    t.checkExpect(cell1.numNeighborMines(), 0);
    cell1.addNeighbour(cell2);
    t.checkExpect(cell1.numNeighborMines(), 0);
    cell1.addNeighbour(cell3);
    t.checkExpect(cell1.numNeighborMines(), 1);
    cell1.addNeighbour(cell4);
    t.checkExpect(cell1.numNeighborMines(), 2);
  }


  //test the method of draw(int cellWidth, int cellHeight) 
  void testDraw(Tester t) {
    this.initConditions();
    t.checkExpect(cell5.draw(25, 25), new OverlayImage(flag, unFlipped));
    t.checkExpect(cell8.draw(25, 25), flipped);
    t.checkExpect(cell10.draw(25, 25), 
        new OverlayImage(new TextImage("2", 25 / 2, FontStyle.BOLD, Color.CYAN),
        flipped));
    t.checkExpect(cell11.draw(25, 25), 
        new OverlayImage(new TextImage("3", 25 / 2, FontStyle.BOLD, Color.RED),
        flipped));
    t.checkExpect(cell12.draw(25, 25), new OverlayImage(flag, unFlipped));
    t.checkExpect(cell13.draw(25, 25), unFlipped);
    t.checkExpect(cell15.draw(25, 25), new OverlayImage(mine, unFlipped));
    t.checkExpect(cell16.draw(25, 25), new OverlayImage(mine, unFlipped));
  }



  //test the method of makeScene() in Game
  void testMakeScene(Tester t) {
    this.initConditions();
    ws1.placeImageXY(new RectangleImage(25, 25, "solid", Color.BLACK), 12, 12);
    ws1.placeImageXY(new Cell(neighbourL2, true, false, false, 0).draw(23, 23), 12, 12);
    ws1.placeImageXY(new RectangleImage(25, 25, "solid", Color.BLACK), 12, 37);
    ws1.placeImageXY(new Cell(neighbourL2, false, false, false, 0).draw(23, 23), 12, 37);
    ws1.placeImageXY(new RectangleImage(25, 25, "solid", Color.BLACK), 37, 12);
    ws1.placeImageXY(new Cell(neighbourL2, false, false, false, 0).draw(23, 23), 37, 12);
    ws1.placeImageXY(new RectangleImage(25, 25, "solid", Color.BLACK), 37, 37);
    ws1.placeImageXY(new Cell(neighbourL2, false, true, false, 0).draw(23, 23), 37, 37);
    t.checkExpect(g1.makeScene(), ws1);

    ws2.placeImageXY(new RectangleImage(25, 25, "solid", Color.BLACK), 12, 12);
    ws2.placeImageXY(new Cell(neighbourL2, true, false, true, 0).draw(23, 23), 12, 12);
    ws2.placeImageXY(new RectangleImage(25, 25, "solid", Color.BLACK), 12, 37);
    ws2.placeImageXY(new Cell(neighbourL2, false, true, true, 0).draw(23, 23), 12, 37);
    t.checkExpect(g2.makeScene(), ws2);

    ws3.placeImageXY(new RectangleImage(25, 25, "solid", Color.BLACK), 12, 12);
    ws3.placeImageXY(new Cell(neighbourL2, true, false, true, 0).draw(23, 23), 12, 12);
    ws3.placeImageXY(new RectangleImage(25, 25, "solid", Color.BLACK), 37, 12);
    ws3.placeImageXY(new Cell(neighbourL2, false, true, false, 0).draw(23, 23), 37, 12);
    t.checkExpect(g3.makeScene(), ws3);  
  }


  //test the method of placeMines(int rows, int cols, int mines)
  void testPlaceMines(Tester t) {
    this.initConditions();
    //since the placeMines method is called 
    //inside the Game main constructor
    //here I use the game's cells each
    //cell to visualize and check the validity of
    //the method
    t.checkExpect(g4.cells.get(0).get(0).isMine, false);
    t.checkExpect(g4.cells.get(0).get(1).isMine, true);
    t.checkExpect(g4.cells.get(1).get(0).isMine, true);
    t.checkExpect(g4.cells.get(1).get(1).isMine, false);

    t.checkExpect(g5.cells.get(0).get(0).isMine, false);
    t.checkExpect(g5.cells.get(0).get(1).isMine, true);

    t.checkExpect(g5.cells.get(0).get(0).isMine, false);
    t.checkExpect(g5.cells.get(0).get(1).isMine, true);

    t.checkExpect(g6.cells.get(0).get(0).isMine, false);
    t.checkExpect(g6.cells.get(1).get(0).isMine, false);
  }



  //test the floodFill() method
  void testFloodFill(Tester t) {
    this.initConditions();
    t.checkExpect(cell6.isClicked, false);
    cell6.floodFill();
    t.checkExpect(cell6.isClicked, true);

    t.checkExpect(cell10.isClicked, true);
    cell10.floodFill();
    t.checkExpect(cell10.isClicked, true);

    t.checkExpect(cell11.isClicked, true);
    t.checkExpect(cell9.isClicked, false);
    t.checkExpect(cell3.isClicked, false);
    t.checkExpect(cell4.isClicked, false);
    cell11.floodFill();
    t.checkExpect(cell11.isClicked, true);
    t.checkExpect(cell9.isClicked, false);
    t.checkExpect(cell3.isClicked, false);
    t.checkExpect(cell4.isClicked, false);

    t.checkExpect(cell17.isClicked, false);
    t.checkExpect(cell1.isClicked, false);
    t.checkExpect(cell2.isClicked, false);
    cell17.floodFill();
    t.checkExpect(cell17.isClicked, true);
    t.checkExpect(cell1.isClicked, true);
    t.checkExpect(cell2.isClicked, true);
  }


  //Test the revealFinal() method
  void testRevealFinal(Tester t) {
    this.initConditions();
    t.checkExpect(cell3.isClicked, false);
    t.checkExpect(cell3.revealFinal(), 1);
    t.checkExpect(cell3.isClicked, true);

    t.checkExpect(cell6.isClicked, false);
    t.checkExpect(cell6.revealFinal(), 0);
    t.checkExpect(cell6.isClicked, false);
  }


  //Test the isCellLoss method
  void testIsCellLoss(Tester t) {
    this.initConditions();
    t.checkExpect(cell3.isCellLoss(), false);
    t.checkExpect(cell6.isCellLoss(), false);
    t.checkExpect(cell14.isCellLoss(), true);
    t.checkExpect(cell15.isCellLoss(), true);
  }


  //Test the singleCellRemain method
  void testSingleCellRemain(Tester t) {
    this.initConditions();
    t.checkExpect(cell6.singleCellRemain(), 1);
    t.checkExpect(cell3.singleCellRemain(), 0);
  }


  //Test the onMouseDelegate(String buttonName) method
  void testOnMouseDelegate(Tester t) {
    this.initConditions();
    t.checkExpect(cell6.isClicked, false);
    cell6.onMouseDelegate("LeftButton");
    t.checkExpect(cell6.isClicked, true);

    t.checkExpect(cell17.isClicked, false);
    t.checkExpect(cell1.isClicked, false);
    t.checkExpect(cell2.isClicked, false);
    cell17.onMouseDelegate("LeftButton");
    t.checkExpect(cell17.isClicked, true);
    t.checkExpect(cell1.isClicked, true);
    t.checkExpect(cell2.isClicked, true);

    t.checkExpect(cell7.isFlagged, true);
    cell7.onMouseDelegate("RightButton");
    t.checkExpect(cell7.isFlagged, false);

    t.checkExpect(cell9.isFlagged, false);
    cell9.onMouseDelegate("RightButton");
    t.checkExpect(cell9.isFlagged, true);
  }



  //Test the win() method in the Game class
  void testWin(Tester t) {
    this.initConditions();
    t.checkExpect(g1.win(), false);
    t.checkExpect(g2.win(), true);
  }


  //Test the lost() method in the Game class
  void testLost(Tester t) {
    this.initConditions();
    t.checkExpect(g3.lost(), true);
    t.checkExpect(g4.lost(), false); 
  }


  //test the cellsRemain() method in Game class
  void testCellsRemain(Tester t) {
    this.initConditions();
    t.checkExpect(g1.cellsRemain(), 3);
    t.checkExpect(g2.cellsRemain(), 0);
    t.checkExpect(g3.cellsRemain(), 1);
    t.checkExpect(g4.cellsRemain(), 2); 
  }
  


  //test the makeWinScene() in Game class
  void testMakeWinScene(Tester t) {
    this.initConditions();
    //this is only for testing this method only
    //the makescene method has already been tested
    //and thus will not be retested here
    WorldScene winScene = g1.makeScene(); 
    TextImage winMessage = new TextImage("NICE", 20, FontStyle.BOLD, Color.GREEN);
    winScene.placeImageXY(winMessage, 25, 25);
    t.checkExpect(g1.makeWinScene(), winScene);

    WorldScene winScene2 = g6.makeScene(); 
    winScene2.placeImageXY(winMessage, 25, 12);
    t.checkExpect(g6.makeWinScene(), winScene2);
  }
 

 
  
 

  //Test the onMouseClicked(Posn pos, String buttonName) method in Game class
  void testOnMouseClicked(Tester t) {
    this.initConditions();
    g1.onMouseClicked(new Posn(12, 12), "LeftButton");
    t.checkExpect(g1.cells.get(0).get(0).isClicked, true); 
    g1.onMouseClicked(new Posn(12, 37), "LeftButton");
    t.checkExpect(g1.cells.get(1).get(0).isClicked, false); 
    t.checkExpect(g1.cells.get(0).get(0).isClicked, true); 
    t.checkExpect(g1.cells.get(0).get(1).isClicked, true); 
    g1.onMouseClicked(new Posn(37, 37), "RightButton");
    t.checkExpect(g1.cells.get(1).get(1).isFlagged, false); 
    g1.onMouseClicked(new Posn(37, 12), "RightButton");
    t.checkExpect(g1.cells.get(1).get(0).isFlagged, true); 
    g1.onMouseClicked(new Posn(12, 12), "LeftButton");
    g1.onMouseClicked(new Posn(37, 12), "LeftButton");
  }


 


  //Test the makeLostScene() method in Game class
  void testMakeLostScene(Tester t) {
    this.initConditions();
    //this is only for testing this method only
    //the makescene method has already been tested
    //and thus will not be retested here
    TextImage lostMessage = new TextImage("U BETTER TRY AGAIN", 20, FontStyle.BOLD, Color.RED);
    WorldScene expected = g2.makeScene();
    for (int i = 0; i < 1; i++) {
      for (int j = 0; j < 2; j++) {
        Cell curCell = g2.cells.get(i).get(j);
        if (curCell.revealFinal() == 1) {
          expected.placeImageXY(curCell.draw(25, 25), 
                                  i * 25 + (25 / 2), 
                                  j * 25 + (25 / 2));
        }
      }
    }
    expected.placeImageXY(lostMessage, (25 * 1 / 2), (25 * 2 / 2));
    t.checkExpect(g2.makeLostScene(), expected);
  }
  

  //test the worldEnds() method in Game class
  void testWorldEnds(Tester t) {
    this.initConditions(); // Initialize game conditions
    WorldEnd test1 = new WorldEnd(false, g1.makeScene());
    t.checkExpect(g1.worldEnds(), test1);
    WorldEnd test2 = new WorldEnd(true, g2.makeWinScene());
    t.checkExpect(g2.worldEnds(), test2);
    WorldEnd test3 = new WorldEnd(true, g3.makeLostScene());
    t.checkExpect(g3.worldEnds(), test3);
  }

  //test the linkNeighbours() in Game class
  void testLinkNeighbours(Tester t) {
    this.initConditions(); 
    g1.linkNeighbours(); 
    Cell g1Cell1 = g1.cells.get(0).get(0); 
    t.checkExpect(g1Cell1.neighbours.size(), 6); 
    g2.linkNeighbours();
    Cell g2Cell1 = g2.cells.get(0).get(0); 
    t.checkExpect(g2Cell1.neighbours.size(), 8); 
    g3.linkNeighbours(); 
    Cell g3Cell1 = g3.cells.get(0).get(0); 
    t.checkExpect(g3Cell1.neighbours.size(), 10); 
  }



  /*

  Game gtest = new Game(30, 30, 150);
  void testBigBang(Tester t) {
     gtest.bigBang(750, 750, 0.1);
  }

  */

 
  


}
