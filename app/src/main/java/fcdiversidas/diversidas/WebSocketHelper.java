package fcdiversidas.diversidas;

import android.graphics.Bitmap;
import android.util.Log;

import io.socket.client.IO;
import io.socket.client.Socket;

/**
 * Created by Zachary Bys on 2018-01-20.
 */

public class WebSocketHelper {
    public Socket socket;

    WebSocketHelper(){
       try {
           socket = IO.socket("http://ec2-54-236-246-164.compute-1.amazonaws.com:3000/");
           socket.connect();
       }
       catch (Exception e) {
           Log.e("ERROR IN SOCKET CREATOR", e.getMessage());
       }

    }
}
