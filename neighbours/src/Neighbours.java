import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.Random;

import static java.lang.Math.round;
import static java.lang.Math.sqrt;
import static java.lang.System.*;

/*
 *  Program to simulate segregation.
 *  See : http://nifty.stanford.edu/2014/mccown-schelling-model-segregation/
 *
 * NOTE:
 * - JavaFX first calls method init() and then the method start() far below.
 * - The method updateWorld() is called periodically by a Java timer.
 * - To test uncomment call to test() first in init() method!
 *
 */
// Extends Application because of JavaFX (just accept for now)
public class Neighbours extends Application {

    // Enumeration type for the Actors
    enum Actor {
        BLUE, RED, NONE   // NONE used for empty locations
    }

    // Enumeration type for the state of an Actor
    enum State {
        UNSATISFIED,
        SATISFIED,
        NA     // Not applicable (NA), used for NONEs
    }

    // Below is the *only* accepted instance variable (i.e. variables outside any method)
    // This variable may *only* be used in methods init() and updateWorld()
    Actor[][] world;              // The world is a square matrix of Actors
    Random rand = new Random();
    // This is the method called by the timer to update the world
    // (i.e move unsatisfied) approx each 1/60 sec.
    void updateWorld() {
        // % of surrounding neighbours that are like me
        final double threshold = 0.7;

        // Copy of world
        Actor[][] nextWorld = deepCopy(world);

        // Update logical state of world
        for (int row = 0; row < world.length; row++) {
            for (int col = 0; col < world.length; col++) {
                // Ignore empty spots.
                if (world[row][col] == Actor.NONE) continue;
                // Check for satisfaction.
                if (!isActorSatisfied(world, col, row, threshold)) {
                    moveActorToRandomPos(nextWorld, row, col);
                }
            }
        }

        world = nextWorld;
        //new Scanner(System.in).nextLine();
    }

    private Actor[][] deepCopy(Actor[][] world) {
        Actor[][] copiedWorld = new Actor[world.length][];
        for (int row = 0; row < world.length; row++) {
            copiedWorld[row] = world[row].clone();
        }
        return copiedWorld;
    }

    // This method initializes the world variable with a random distribution of Actors
    // Method automatically called by JavaFX runtime (before graphics appear)
    // Don't care about "@Override" and "public" (just accept for now)
    @Override
    public void init() {
        //test();    // <---------------- Uncomment to TEST!

        // %-distribution of RED, BLUE and NONE
        double[] dist = {0.25, 0.25, 0.50};
        // Number of locations (places) in world (square)
        int nLocations = 900;

        // Create and populate world.
        // Origin is in top left.
        world = createWorld((int) Math.sqrt(nLocations));
        populateWorld(world, dist);

        // Should be last
        fixScreenSize(nLocations);
    }


    //---------------- Methods ----------------------------

    // Check if inside world
    boolean isValidLocation(int size, int row, int col) {
        return 0 <= row && row < size &&
                0 <= col && col < size;
    }

    private void populateWorld(Actor[][] world, double[] dist) {
        int count = world.length * world.length;
        double redAmount = round(count * dist[0]);
        double blueAmount = round(count * dist[1]);
        int iteratedCells = 0;
        for (int row = 0; row < world.length; row++) {
            for (int col = 0; col < world.length; col++) {
                int distribution = rand.nextInt(count - iteratedCells);
                // If 0 < distribution < red amount, place red actor.
                if (distribution < redAmount) {
                    redAmount--;
                    world[row][col] = Actor.RED;
                }
                // If red amount < distribution < total actor amount, place blue actor.
                else if (distribution < blueAmount + redAmount) {
                    blueAmount--;
                    world[row][col] = Actor.BLUE;
                }
                // If total actor amount < distribution, place no actor.
                else {
                    world[row][col] = Actor.NONE;
                }
                iteratedCells++;
            }
        }
    }

    private Actor[][] createWorld(int size) {
        Actor[][] world = new Actor[size][];
        for (int i = 0; i < size; i++) {
            world[i] = new Actor[size];
        }
        return world;
    }

    private void moveActorToRandomPos(Actor[][] world, int row, int col) {
        int[] newPos = findRandomEmptyPosition(world);
        int newRow = newPos[0];
        int newCol = newPos[1];
        // Identify actor.
        Actor actor = world[row][col];
        // Remove from old position.
        world[row][col] = Actor.NONE;
        // Move to new position.
        world[newRow][newCol] = actor;
    }

    private int[] findRandomEmptyPosition(Actor[][] world) {
        int row, col;
        do {
            row = rand.nextInt(world.length);
            col = rand.nextInt(world.length);
        } while (world[row][col] != Actor.NONE);
        return new int[]{row, col};
    }

    private boolean isActorSatisfied(Actor[][] world, int actorCol, int actorRow, double threshold) {
        //Restrictions
        int topRow = Math.max(actorRow - 1, 0);
        int botRow = Math.min(actorRow + 1, world.length - 1);
        int leftCol = Math.max(actorCol - 1, 0);
        int rightCol = Math.min(actorCol + 1, world.length - 1);
        //Counters
        int blue = 0;
        int red = 0;

        for (int row = topRow; row <= botRow; row++) {
            for (int col = leftCol; col <= rightCol; col++) {
                // Ignore own position.
                if (row == actorRow && col == actorCol) continue;
                switch (world[row][col]) {
                    case BLUE:
                        blue++;
                        break;
                    case RED:
                        red++;
                        break;
                }
            }
        }
        int sameColor = world[actorRow][actorCol] == Actor.BLUE ? blue : red;
        double satisfaction = (double)sameColor / (blue + red);
        return satisfaction >= threshold;
    }

    // ------- Testing -------------------------------------

    // Here you run your tests i.e. call your logic methods
    // to see that they really work
    void test() {
        // A small hard coded world for testing
        Actor[][] testWorld = new Actor[][]{
                {Actor.RED, Actor.RED, Actor.NONE},
                {Actor.NONE, Actor.BLUE, Actor.NONE},
                {Actor.RED, Actor.NONE, Actor.BLUE}
        };
        double th = 0.5;   // Simple threshold used for testing

        int size = testWorld.length;
        out.println(isValidLocation(size, 0, 0));
        out.println(!isValidLocation(size, -1, 0));
        out.println(!isValidLocation(size, 0, 3));
        out.println(isValidLocation(size, 2, 2));
        out.println();

        // More tests
        /* findRandomEmptyPosition */
        int[] randPos = findRandomEmptyPosition(testWorld);
        out.println((randPos[1] == 0 && randPos[0] == 1) ||
                (randPos[1] == 1 && randPos[0] == 2) ||
                (randPos[1] == 2 && (randPos[0] == 0 || randPos[0] == 1))
        );
        randPos = findRandomEmptyPosition(testWorld);
        out.println((randPos[1] == 0 && randPos[0] == 1) ||
                (randPos[1] == 1 && randPos[0] == 2) ||
                (randPos[1] == 2 && (randPos[0] == 0 || randPos[0] == 1))
        );
        randPos = findRandomEmptyPosition(testWorld);
        out.println((randPos[1] == 0 && randPos[0] == 1) ||
                (randPos[1] == 1 && randPos[0] == 2) ||
                (randPos[1] == 2 && (randPos[0] == 0 || randPos[0] == 1))
        );
        out.println();

        /* isSatisfied */
        out.println(isActorSatisfied(testWorld, 0, 0, th));
        out.println(!isActorSatisfied(testWorld, 1, 1, th));
        out.println(isActorSatisfied(testWorld, 2, 2, th));
        out.println();

        /* moveActorToRandomPos */
        out.println(testWorld[0][0] == Actor.RED);
        moveActorToRandomPos(testWorld, 0, 0);
        out.println(testWorld[0][0] == Actor.NONE);
        int blueCount = 0;
        int redCount = 0;
        for (int y = 0; y < testWorld.length; y++) {
            redCount += count(testWorld[y], Actor.RED);
            blueCount += count(testWorld[y], Actor.BLUE);
        }
        out.println(redCount == 3);
        out.println(blueCount == 2);
        out.println();

        /* count distributed actors */
        Actor[][] testWorld1 = createWorld(3);
        Actor[][] testWorld2 = createWorld(3);
        Actor[][] testWorld3 = createWorld(3);
        populateWorld(testWorld1, new double[]{0.25, 0.25, 0.5});
        populateWorld(testWorld2, new double[]{0.25, 0.25, 0.5});
        populateWorld(testWorld3, new double[]{0.25, 0.25, 0.5});
        out.println(count2DArray(testWorld1, Actor.BLUE) == 2);
        out.println(count2DArray(testWorld2, Actor.BLUE) == 2);
        out.println(count2DArray(testWorld3, Actor.BLUE) == 2);
        out.println(count2DArray(testWorld1, Actor.RED) == 2);
        out.println(count2DArray(testWorld2, Actor.RED) == 2);
        out.println(count2DArray(testWorld3, Actor.RED) == 2);
        out.println(count2DArray(testWorld1, Actor.NONE) == 5);
        out.println(count2DArray(testWorld2, Actor.NONE) == 5);
        out.println(count2DArray(testWorld3, Actor.NONE) == 5);

        exit(0);
    }

    // Helper method for testing (NOTE: reference equality)
    <T> int count(T[] arr, T toFind) {
        int count = 0;
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == toFind) {
                count++;
            }
        }
        return count;
    }

    <T> int count2DArray(T[][] arr, T toFind) {
        int sum = 0;
        for (int row = 0; row < arr.length; row++) {
            sum += count(arr[row], toFind);
        }
        return sum;
    }

    // ###########  NOTHING to do below this row, it's JavaFX stuff  ###########

    double width = 800;   // Size for window
    double height = 800;
    long previousTime = nanoTime();
    final long interval = 450_000_000;
    double dotSize;
    final double margin = 50;

    void fixScreenSize(int nLocations) {
        // Adjust screen window depending on nLocations
        dotSize = (width - 2 * margin) / sqrt(nLocations);
        if (dotSize < 1) {
            dotSize = 2;
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        // Build a scene graph
        Group root = new Group();
        Canvas canvas = new Canvas(width, height);
        root.getChildren().addAll(canvas);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        renderWorld(gc, world);
        // Create a timer
        AnimationTimer timer = new AnimationTimer() {
            // This method called by FX, parameter is the current time
            public void handle(long currentNanoTime) {
                long elapsedNanos = currentNanoTime - previousTime;
                if (elapsedNanos > interval) {
                    updateWorld();
                    renderWorld(gc, world);
                    previousTime = currentNanoTime;
                }
            }
        };

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Segregation Simulation");
        primaryStage.show();

        timer.start();  // Start simulation
    }


    // Render the state of the world to the screen
    public void renderWorld(GraphicsContext g, Actor[][] world) {
        g.clearRect(0, 0, width, height);
        int size = world.length;
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                double x = dotSize * col + margin;
                double y = dotSize * row + margin;

                if (world[row][col] == Actor.RED) {
                    g.setFill(Color.RED);
                } else if (world[row][col] == Actor.BLUE) {
                    g.setFill(Color.BLUE);
                } else {
                    g.setFill(Color.WHITE);
                }
                g.fillOval(x, y, dotSize, dotSize);
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

}
