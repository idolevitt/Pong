package ido.pong;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

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
    public void start(Stage primaryStage) {


        scn = new Scanner(System.in);
        //establishing connection to the server:
        while(true) {
            try {
                socket = new Socket(IP, PORT);
                dos = new DataOutputStream(socket.getOutputStream());
                break;
            } catch (IOException i) {
            }
        }

        input = new ArrayList<>();

        Scene scene = new Scene(new Group(new Canvas(200,0)));
        primaryStage.setScene(scene);
        primaryStage.show();

        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent windowEvent) {
                try {
                    System.out.println("Closing connection");
                    dos.writeUTF("Close");
                    dos.close();
                    socket.close();
                    Platform.exit();
                    System.exit(0);
                } catch (IOException e) {}
            }
        });
        //Player pressed a key
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
        //Player released a key
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
