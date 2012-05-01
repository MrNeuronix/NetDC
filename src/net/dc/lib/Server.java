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
package net.dc.lib;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import java.util.logging.Level;
import java.util.logging.Logger;

class Server implements Runnable {
    
    private ServerSocket serversocket;
        
    protected Server() throws IOException {
	serversocket = new ServerSocket(Integer.parseInt(SettingsManager.getInstance().getTCPPort()));
    }
    
    public void run() {
        while(true) {
            try {
                Socket socket;
                socket = serversocket.accept();
                ConnectionManager.getInstance().addClient2Client(socket);
            } catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, "A problem occured in Server.java", ex);
            }
	}
    }

}

