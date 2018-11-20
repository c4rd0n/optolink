package de.myandres.optolink.socket;

import de.myandres.optolink.config.Config;
import de.myandres.optolink.viessmann.ViessmannHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

public class SocketSession extends Thread {
    private static Logger log = LoggerFactory.getLogger(SocketHandler.class);
    private Socket socket;
    private Config config;
    private ViessmannHandler viessmannHandler;

    SocketSession(Socket socket, Config config, ViessmannHandler viessmannHandler) {
        super("SocketSession");
        this.socket = socket;
        this.config = config;
        this.viessmannHandler = viessmannHandler;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            PrintStream out = new PrintStream(socket.getOutputStream());
            out.println("<!-- #Helo from viessmann -->");
            out.println("<optolink>");

            String inStr;

            while ((inStr = in.readLine()) != null) {
                if (inStr.toLowerCase().startsWith("exit"))
                    break;
                // Start a thread for each call command: No blocking of caller
                new Thread(new CommandExec(inStr, out, config, viessmannHandler)).start();
            }
            out.println("<!-- #Bye from viessmann -->");
            socket.close();
        } catch (IOException e) {
            log.info("Connection on Socket {} rejected or closed by client", config.getPort());
        }
    }
}

