package chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client implements Runnable{
    private Socket client;
    private BufferedReader in;
    private PrintWriter out;
    private boolean done;

    public static void main(String[] args) {
        Client client = new Client();
        client.run();
    }

    @Override
    public void run() {
        try {
            client = new Socket("127.0.0.1", 9999);
            out = new PrintWriter(client.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            InputHandler inputHandler = new InputHandler();
            Thread t1 = new Thread(inputHandler);
            t1.start();
            String inMessage;
            while ((inMessage = in.readLine()) != null) System.out.println(inMessage);
        } catch (IOException e){ shutdown(); }
    }

    public void shutdown(){
        done = true;
        try {
            in.close();
            out.close();
            if(!client.isClosed()) client.close();
        } catch (IOException e){ /*ignore*/ }
    }

    class InputHandler implements Runnable{

        @Override
        public void run() {
            try {
                BufferedReader inReader = new BufferedReader(new InputStreamReader(System.in));
                while (!done){
                    String message = inReader.readLine();
                    if (message.equals("/exit")){
                        out.println(message);
                        inReader.close();
                        shutdown();
                    } out.println(message);
                }
            } catch (IOException e){ shutdown(); }
        }
    }
}
