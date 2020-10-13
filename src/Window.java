import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class Window extends Application{

    //Window properties:
    private final int windowWidth = 900;
    private final int windowHeight = 600;

    //Players properties:
    private final int playerWidth = 10;
    private final int playerHeight = 90;

    //Player 1:
    private int playerOneXPos = 30;
    private int playerOneYPos = 30;
    private int scorePlayerOne;

    //Player 2:
    private int playerTwoXPos = windowWidth - 30;
    private int playerTwoYPos = 300;
    private int scorePlayerTwo;

    //Ball properties:
    private final int ballRadius = 20;
    private int ballXPos = 90;
    private int ballYPos = 150;
    private double ballSpeedX = 3;
    private double ballSpeedY = 3;

    private ArrayList<String> input;

    //connection variables:

    final static int PORT = 9000;

    private ServerSocket server;
    private Socket socketPlayerOne;
    private Socket socketPlayerTwo;
    private DataInputStream disOne;
    private DataInputStream disTwo;


    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException, InterruptedException {

        input = new ArrayList<>();

        scorePlayerOne = 0;
        scorePlayerTwo = 0;

        //Establishing connections:

        server = new ServerSocket(9000);

        System.out.println("waiting for clients...");

        //Player 1:
        socketPlayerOne = server.accept();
        System.out.println("Player 1 connected");
        disOne = new DataInputStream(socketPlayerOne.getInputStream());
        Thread playerOne = new Thread(() -> {
            while (true) {
                try {
                    String key = disOne.readUTF() + "P1";
                    System.out.print("adding " + key + " to the input list");
                    //"Close command":
                    if(key.equals("CloseP1")){
                        try{
                            disOne.close();
                            socketPlayerOne.close();
                            System.out.println("Player 1 disconnected");
                            break;
                        }catch (IOException i){
                            i.printStackTrace();
                        }
                    }
                    //Key released:
                    else if (key.charAt(0) == 'R') {
                        System.out.println("Removing " + key + " from the list");
                        input.remove(key.substring(1));
                    }
                    //Key pressed:
                    else {
                        System.out.println("Adding " + key + " to the list");
                        input.add(key);
                    }
                } catch (IOException i) {}
            }
        });
        playerOne.start();


        //Player 2:
        System.out.println("waiting for player 2:");
        socketPlayerTwo = server.accept();
        System.out.println("Player 2 connected");
        disTwo = new DataInputStream(socketPlayerTwo.getInputStream());
        Thread playerTwo = new Thread(() -> {

            while (true) {
                try {
                    String key = disTwo.readUTF() + "P2";
                    //"Close command":
                    if(key.equals("CloseP2")){
                        try{
                            disTwo.close();
                            socketPlayerTwo.close();
                            System.out.println("Player 2 disconnected");
                            break;
                        }catch (IOException i){
                            i.printStackTrace();
                        }
                    }
                    //Key released:
                    else if (key.charAt(0) == 'R') {
                        System.out.println("Removing " + key + " from the list");
                        input.remove(key.substring(1));
                    }
                    //Key pressed:
                    else {
                        System.out.println("Adding " + key + " to the list");
                        input.add(key);
                    }
                } catch (IOException i) {}
            }
        });
        playerTwo.start();



        TimeUnit.SECONDS.sleep(5);

        //Window:
        primaryStage.setTitle("Pong");

        Canvas canvas = new Canvas(windowWidth, windowHeight);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        primaryStage.show();

        Group group = new Group(canvas);

        Scene scene = new Scene(group);

        primaryStage.setScene(scene);

        //close operation:
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent windowEvent) {
                try {
                    System.out.println("Closing game...");
                    disOne.close();
                    socketPlayerOne.close();
                    //disTwo.close();
                    //socketPlayerTwo.close();
                    Platform.exit();
                    System.exit(0);
                } catch (IOException e) {}
            }
        });

        //Handling the user input
        //input = new ArrayList<>();

        scene.setOnKeyPressed(e -> {
            String code = e.getCode().toString();
            if(!input.contains(code))
                input.add(code);
        });

        scene.setOnKeyReleased(e -> {
            String code = e.getCode().toString();
            input.remove(code);
        });



        new AnimationTimer()
        {
            public void handle(long currentNanoTime) {

                //Player 1 keys:
                if (input.contains("UPP1") && playerTwoYPos > 0)
                    playerTwoYPos -= 5;
                if (input.contains("DOWNP1") && playerTwoYPos < windowHeight - playerHeight)
                    playerTwoYPos += 5;
                //Player 2 keys:
                if (input.contains("UPP2") && playerOneYPos > 0)
                    playerOneYPos -= 5;
                if (input.contains("DOWNP2") && playerOneYPos < windowHeight - playerHeight)
                    playerOneYPos += 5;

                //Update the ball location and properties
                try {
                    moveBall();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                //Setting the background color to black:
                gc.setFill(Color.BLACK);
                gc.fillRect(0,0, canvas.getWidth(), canvas.getHeight());

                //drawing score:
                gc.setFill(Color.CYAN);
                gc.setFont(Font.font("Verdana", FontWeight.BOLD, 50));
                //score.setFont(Font.font("Verdana", 50));
                //score.setFill(Color.AZURE);
                gc.fillText(scorePlayerTwo +" : " + scorePlayerOne, 400, 60);

                gc.setFill(Color.WHITE);
                //Drawing player 1:
                gc.fillRect(playerOneXPos, playerOneYPos, playerWidth, playerHeight);
                //Drawing player 2:
                gc.fillRect(playerTwoXPos, playerTwoYPos, playerWidth, playerHeight);
                //Drawing the ball:
                gc.fillOval(ballXPos, ballYPos, ballRadius, ballRadius);

            }
        }.start();

    }

    public void moveBall() throws InterruptedException {
        //Ball bumped the upper wall:
        if (ballYPos <= ballRadius && ballSpeedY < 0)
            ballSpeedY *= -1;
        //Ball bumped the bottom wall:
        if (ballYPos >= windowHeight - ballRadius && ballSpeedY > 0)
            ballSpeedY *= -1;
        //Player 1 scored a goal:
        if (ballXPos <= ballRadius){
            scorePlayerOne++;
            restartBall();
        }
        //Player 2 scored a goal:
        if (ballXPos >= windowWidth - ballRadius){
            scorePlayerTwo++;
            restartBall();
        }
        //The ball hit player 2:
        if (ballXPos - ballRadius <= playerOneXPos - playerWidth &&
                ballXPos + ballRadius >= playerOneXPos - playerWidth - ballRadius &&
                ballYPos >= playerOneYPos && ballYPos <= playerOneYPos + playerHeight) {
            if (ballSpeedX < 0) {
                ballSpeedX *= -1;
                ballSpeedX *= 1.05;
                ballSpeedY *= 1.05;
            }
        }
        //the ball hit player 1:
        if (ballXPos + ballRadius >= playerTwoXPos && ballXPos + ballRadius <= playerTwoXPos + ballRadius &&
                ballYPos >= playerTwoYPos && ballYPos <= playerTwoYPos + playerHeight) {
            if (ballSpeedX > 0) {
                ballSpeedX *= -1;
                ballSpeedX *= 1.05;
                ballSpeedY *= 1.05;
            }
        }
        //updating ball location:
        ballXPos += ballSpeedX;
        ballYPos += ballSpeedY;


    }

    private void restartBall() throws InterruptedException {
        ballXPos = windowWidth / 2;
        ballYPos = windowHeight / 2;
        ballSpeedX = 3;
        ballSpeedY = 3;

        TimeUnit.SECONDS.sleep(2);
    }

}
