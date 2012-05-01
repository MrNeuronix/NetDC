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
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.dc.lib.Client2Client.ConnectionData;
import net.dc.lib.event.ChatListener;
import net.dc.lib.event.NickListener;

public class ConnectionManager {

    private static final class Holder {
        private static final ConnectionManager INSTANCE = new ConnectionManager();
    }
        
    private Thread server;
    private Vector<Client2Client> c2cs = new Vector<Client2Client>();
    private Map<Integer, Client2Server> c2ss = Collections.synchronizedMap(new HashMap<Integer, Client2Server>());

    private ConnectionManager() {
        try {
            server = new Thread(new Server());
            server.start();
        } catch (IOException ex) {
            Logger.getLogger(ConnectionManager.class.getName()).log(Level.SEVERE, "Can't start server");
        }
    }
    
    public void addChatListenerToClient2Server(int id, ChatListener lis) {
        c2ss.get(id).addChatListener(lis);
    }

    public void addNickListenerToClient2Server(int id, NickListener lis) {
        c2ss.get(id).addNickListener(lis);
    }
    
    public void addClient2Client(Socket sock) {
        try {
            Client2Client c2c = new Client2Client(sock);
            new Thread(c2c).start();
            c2cs.add(c2c);
        } catch (IOException ex) {
            Logger.getLogger(ConnectionManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void createClient2Client(String ip, int port, String ownnick, boolean download) {
        try {
            Client2Client c2c = new Client2Client(ip, port);
            c2c.startConnection(ownnick, download);
            c2cs.add(c2c);
        } catch (UnknownHostException ex) {
            Logger.getLogger(ConnectionManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ConnectionManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Creates a connection with a hub and returns a unique id. Usually it is the hashcode of the Client2Server instance
     * 
     * @param ip IP of the hub
     * @param port Port of the hub
     * @param nick Nick u want to use on the hub
     * @param password Password for the hub
     * @return A unique id
     */
    public int createClient2Server(String ip, int port, String nick, String password) {
        Client2Server c2s = new Client2Server(ip, port, nick, password);
        int id = c2s.hashCode();
        c2ss.put(id, c2s);
        return id;
    }

    public String getHubIp(int id) {
        return c2ss.get(id).getIp();
    }
    
    public Vector<Tupel<String,Integer>> getHubs() {
        Vector<Tupel<String,Integer>> vec = new Vector<Tupel<String,Integer>>();
        Set<Entry<Integer,Client2Server>> hubs = c2ss.entrySet();
        for(Entry<Integer,Client2Server> e:hubs) {
            vec.add(new Tupel<String,Integer>(e.getValue().getHubName(),e.getKey()));
        }
        return vec;
    }
    
    public void startConnection(int id) {
        new Thread(c2ss.get(id)).start();
    }
    
    public void startSearch(int id,String text) {
        
    }

    public void sendText(int id, String text) {
        c2ss.get(id).sendText(text);
    }

    public void sendPrivateText(int id, String othernick, String text) {
        c2ss.get(id).sendPrivateMessage(othernick, text);
    }

    public void connectToMe(int id, String othernick) {
        c2ss.get(id).sendConnectToMe(othernick);
    }

    public int reconnect(int id, NickListener nickl, ChatListener chatl) {
        Client2Server old = c2ss.get(id);
        old.removeAllListener();
        old.close();
        c2ss.remove(id);
        Client2Server c2s = new Client2Server(old.getIp(), old.getPort(), old.getNick(), old.getPw());
        c2s.addChatListener(chatl);
        c2s.addNickListener(nickl);
        int key = c2s.hashCode();
        c2ss.put(key, c2s);
        new Thread(c2s).start();
        return key;
    }

    protected void removeClient2Client(Client2Client c2c) {
        c2cs.remove(c2c);
    }

    public static ConnectionManager getInstance() {
        return Holder.INSTANCE;
    }
    
    public Vector<ConnectionData> getConnectionData() {
        Vector<ConnectionData> vec = new Vector<ConnectionData>();
        for(Client2Client c2c:c2cs) {
            vec.add(c2c.getConnectionData());
        }
        return vec;
    }
}
