package server;

import io.javalin.Javalin;
import models.Song;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    static ConcurrentHashMap<String, Room> rooms = new ConcurrentHashMap<>();
    static ConcurrentHashMap<String, ClientHandler> clients = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        // Initialize Javalin
        Javalin app = Javalin.create(config -> {
            config.staticFiles.add("/public");
        }).start(7000);
        
        app.get("/", ctx -> ctx.redirect("/index.html"));

        app.get("/joinRoom", ctx -> {
            String roomName = ctx.queryParam("room");
            if (roomName != null) {
                Room room = rooms.computeIfAbsent(roomName, Room::new);
                ctx.result("Joined room: " + roomName);
                room.broadcast("A new user has joined the room.");
            } else {
                ctx.status(400).result("Room name is required.");
            }
        });

        app.post("/addSong", ctx -> {
            String songName = ctx.queryParam("song");
            if (songName != null) {
                Song song = new Song(songName);
                Room defaultRoom = rooms.get("default");
                if (defaultRoom != null) {
                    defaultRoom.addSong(song);
                    ctx.result("Added song: " + songName);
                } else {
                    ctx.status(404).result("Default room not found.");
                }
            } else {
                ctx.status(400).result("Song name is required.");
            }
        });

        app.ws("/ws", ws -> {
            ws.onConnect(ctx -> {
                System.out.println("WebSocket connected: " + ctx.getSessionId());
            });
            ws.onMessage(ctx -> {
                System.out.println("Received message: " + ctx.message());
                ctx.send("Echo: " + ctx.message());
            });
            ws.onClose(ctx -> {
                System.out.println("WebSocket closed: " + ctx.getSessionId());
            });
            ws.onError(ctx -> {
                System.out.println("WebSocket error: " + ctx.getSessionId());
            });
        });

        // Start socket server in a new thread
        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(12345)) {
                System.out.println("Socket server is listening on port 12345");
                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("New socket client connected: " + clientSocket.getInetAddress());

                    // Create and start a new ClientHandler thread
                    ClientHandler clientHandler = new ClientHandler(clientSocket, rooms);
                    clients.put(clientSocket.getInetAddress().toString(), clientHandler);
                    new Thread(clientHandler).start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
}