package ido.pong;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.stage.Stage;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class Player extends Application {

    private static Socket socket;
    private static DataOutputStream dos;
    private static Scanner scn;

    private final static int PORT = 9000;
    private final static String IP = "127.0.0.1";

    private ArrayList<String> input;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {


        scn = new Scanner(System.in);

        socket = new Socket(IP, PORT);
        dos = new DataOutputStream(socket.getOutputStream());

        input = new ArrayList<>();

        //TODO: make new scene for key press handler
        Scene scene = new Scene(new Group(new Canvas(100,100)));
        primaryStage.setScene(scene);
        primaryStage.show();

        scene.setOnKeyPressed(e -> {
            String command = e.getCode().toString();
            System.out.println(e.getCode().toString() + " was pressed");
            if(!input.contains(command)) {
                try {
                    input.add(command);
                    dos.writeUTF(command);
                } catch (IOException i) {
                    i.printStackTrace();
                }
            }
        });

        scene.setOnKeyReleased(e -> {
            String command = e.getCode().toString();
            if(input.contains(command)) {
                try {
                    input.remove(command);
                    dos.writeUTF("R" + e.getCode().toString());
                } catch (IOException i) {
                    i.printStackTrace();
                }
            }
        });

    }
}
