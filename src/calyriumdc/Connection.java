/*
 * Copyright 2008 Sebastian Köhler
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package calyriumdc;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;

import android.util.Log;

public abstract class Connection {

    protected int timeout = 30000;
    protected boolean stoplistening = false;
    private Socket socket;
    private BufferedInputStream in;
    private BufferedOutputStream out;

    protected void connect(String ip, int port) throws UnknownHostException, IOException {
        socket = new Socket(ip, port);
        socket.setSoTimeout(timeout);
        in = new BufferedInputStream(socket.getInputStream());
        out = new BufferedOutputStream(socket.getOutputStream());
    }

    protected void connect(Socket socket) throws IOException {
        this.socket = socket;
        socket.setSoTimeout(timeout);
        in = new BufferedInputStream(socket.getInputStream());
        out = new BufferedOutputStream(socket.getOutputStream());
    }

    protected void listen() throws SocketTimeoutException, IOException {
        char c;
        String s = "";
        socket.setSoTimeout(timeout);
        while (socket.isConnected() && !socket.isClosed()) {
            if (stoplistening) {
                break;
            }
            int i = in.read();
            if (i == -1) {
                connectionClosed();
                socket.close();
                break;
            }
            c = (char) i;
            if (c == '|') {
                String command;
                String data;
                if (s.contains(" ")) {
                    command = s.substring(0, s.indexOf(" "));
                    data = s.substring(s.indexOf(" ") + 1);
                } else {
                    command = s;
                    data = "";
                }
                
                byte[] temp = data.getBytes("ISO-8859-1");
                data = new String(temp, "WINDOWS-1251");
                            
                Log.d("NET", "Command: "+command+" "+data);
                
                react(command, data);
                s = "";
            } else {
                s = s + c;
            }
        }
    }

    abstract protected void react(String command, String data);

    abstract protected void connectionClosed();

    protected void write(String data) {
        try {
            
        	Log.d("NET", "Write: "+data);
            
            out.write(data.getBytes("WINDOWS-1251"));
            out.flush();
        } catch (IOException ex) {
            Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    protected void close() {
        try {
            socket.close();
        } catch (IOException ex) {
            Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    protected boolean isConnected() {
        return socket.isConnected();
    }

    protected boolean isClosed() {
        return socket.isClosed();
    }

    protected OutputStream getOut() {
        return out;
    }

    protected InputStream getIn() {
        return in;
    }

    protected String getLocalIp() {
        return socket.getLocalAddress().getHostAddress();
    }
    
    protected void setTimeout(int millisec) {
        try {
            socket.setSoTimeout(millisec);
        } catch (SocketException ex) {
            Logger.getLogger(Connection.class.getName()).log(Level.INFO, "Download might need longer", ex);
        }
    }
}
