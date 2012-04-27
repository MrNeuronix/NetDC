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

import calyriumdc.event.*;
import java.io.File;

/**
 * The main class of the application.
 */
public class CalyriumDC {
   
    /**
     * Main method launching the application.
     */
    public static void main(String[] args) {
        
        System.out.println("START!");
        
        String ip = "109.120.128.21";
        int port = 411;
        String nick = "Neuronix555";
        String password = "";
        
        System.out.println("Connecting to "+ip+":"+port);
        
        int id = ConnectionManager.getInstance().createClient2Server(ip, port, nick, password);
        
        System.out.println("Connect ID: "+id);

        ConnectionManager.getInstance().addChatListenerToClient2Server(id, getChatListener());
        ConnectionManager.getInstance().addNickListenerToClient2Server(id, getNickListener());
        
        ConnectionManager.getInstance().startConnection(id);
        System.out.println("Connect ID "+id+" started!");
    }
    
    protected static NickListener getNickListener() {
        
        System.out.println("AT getNickListener!");
        
        return new NickListener() {

            public void nickChanged(final NickEvent evt) {
                new Runnable() {
                    public void run() {

                            System.out.println("IN Runnable!");
                      
                            String[] row = new String[7];
                            row[0] = evt.getNick();
                            row[1] = Crypto.getBytes(Long.parseLong(evt.getShare()));
                            row[2] = evt.getDescription();
                            row[3] = evt.getTag();
                            row[4] = evt.getSpeed();
                            row[5] = evt.getEmail();
                            row[6] = evt.getIp();
      
                               System.out.println("User: "+row);

                    }
                };
            }

            public void removeNick(final NickEvent evt) {
                new Runnable() {

                    public void run() {
                        System.out.println("User: "+evt.getNick());
                        
                    }
         };
        }
      };
    }
    
    
    protected static ChatListener getChatListener() {
        return new ChatListener() {

            public void chatEvent(final ChatEvent evt) {
                new Runnable() {

                    public void run() {
                        System.out.println(evt.getData());
                    }
                };
            }

            public void privateMessage(final PrivateMessageEvent evt) {
                new Runnable() {
                    public void run() {
                        System.out.println("Private: " + evt.getData());
                    }
                };
            }
        };
    }
    
}

