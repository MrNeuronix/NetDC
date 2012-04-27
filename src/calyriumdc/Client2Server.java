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

import calyriumdc.event.ChatEvent;
import calyriumdc.event.ChatListener;
import calyriumdc.event.NickEvent;
import calyriumdc.event.NickListener;
import calyriumdc.event.PrivateMessageEvent;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import calyriumdc.EventListenerList;

class Client2Server extends Connection implements Runnable {
    
    private class ConnectionData {
        
        private String nick;
        private String pw;
        private String ip;
        private int port;
        private String hubname;
        private Vector<String> protext;
        
        /*private boolean NoGetINFO = false, NoHello = false, UserIP2 = false,
                UserCommand = false,TTHSearch = false,OpPlus = false,
                Feed = false, MCTo = false, HubTopic = false, ZPipe0 = false,
                ZLine = false, BotList = false, QuickList = false,
                ClientID = false;
         */

        public ConnectionData(String nick, String pw, String ip, int port) {
            this.nick = nick;
            this.pw = pw;
            this.ip = ip;
            this.hubname = ip;
            this.port = port;
            protext = new Vector<String>();
        }
        
        private void setSupportedExtensions(String[] ext) {
            protext.addAll(Arrays.asList(ext));
        }
    }
    
    private ConnectionData condata;
    private EventListenerList eventlistenerlist;
        
    protected Client2Server(String ip, int port,String nick,String password) {
	condata = new ConnectionData(nick,password,ip,port);
        eventlistenerlist = new EventListenerList();
        timeout = 0;
    }
    
    @Override
    protected void react(String command, String data) {
        
        if(command.equals("$Lock")) {
            write("$Supports XmlBZList NoGetINFO NoHello UserIP2 TTHSearch|");
            write("$Key " + Crypto.getKey(data.split(" ")[0])+"|");
            write("$ValidateNick " + condata.nick +"|");
	}
        if(command.equals("$HubIsFull")) {
            fireChatEvent("Hub is Full");
            close();
        }
        if(command.equals("$ValidateDenide")) {
            fireChatEvent("Nick "+data+ " already in usage!");
            close();
        }
        if(command.equals("$Supports")) {
            condata.setSupportedExtensions(data.split(" "));
        }
	if(command.equals("$GetPass")) {
            write("$MyPass "+ condata.pw +"|");
	}
	if(command.equals("$BadPass")) {
            fireChatEvent("Wrong Password");
            close();
	}
	if(command.equals("$HubName")) {
            condata.hubname = data;
        }
        if(command.equals("$HubTopic")) {
        }
	if(command.equals("$Hello")) {
            write("$Version 1,0091|");
            write("$GetNickList|");
            write("$MyINFO $ALL " + condata.nick + " " + SettingsManager.getInstance().getDescription()+"<netDC client v0.1>$ $"+SettingsManager.getInstance().getUploadSpeed() +" $$"+SettingsManager.getInstance().getSharesize() +"$|");
	}
	if(command.equals("$MyINFO")) {
            String dataar[] = data.split("\\$");
            //$MyINFO $ALL Basement [VIP] [2][L:15KB]Forelle<ApexDC++ V:1.1.0,M:A,H:0/2/0,S:2,L:15>$ $0.005$$333857606187$
            //$MyINFO $ALL <nick> <description>$ $<connection><flag>$<e-mail>$<sharesize>$|
            String name = dataar[1].split(" ")[1];
            String desc = dataar[1].substring(dataar[1].lastIndexOf(name));
            String connection = dataar[3];
            String email = dataar[4];
            String sharesize = dataar[5];
            fireNickAdded(name, sharesize, desc, "", connection, email,"");
        }
	if(command.equals("$OpList")) {
	}
	if(command.equals("$UserIP")) {
            for(String s:data.split("\\$\\$")) {
                String name = s.split(" ")[0];
		String ip = s.split(" ")[1];
                fireNickAdded(name, "", "", "", "", "", ip);
            }
	}
	if(command.equals("$Quit")) {
            fireNickRemoved(data, "", "", "", "", "", "");
	}
	if(command.equals("$To:")) {
            //$To: <othernick> From: <nick> $<<nick>> <message>|
            String othernick = data.split(" ")[2];
            String text = data.split("\\$")[1];
            firePrivateChatEvent(othernick, text);
	}
	if(command.equals("$ConnectToMe")) {
            String ipandport = data.split(" ")[1];
            String ip = ipandport.split(":")[0];
            int port = Integer.parseInt(ipandport.split(":")[1]);
            ConnectionManager.getInstance().createClient2Client(condata.ip, condata.port, condata.nick, false);
	}
	if(!command.contains("$")) {
            fireChatEvent(command+": "+ data + "\n");
	}
    }
                
    protected void sendText(String text) {
	if(text != null || !text.equals("")) {
	    if(isConnected()) {
		write("<"+condata.nick+"> "+text+"|");
	    }
	}
    }
    
    protected void sendPrivateMessage(String user,String text) {
        //$To: <othernick> From: <nick> $<<nick>> <message>|
        if(text != null || !text.equals("")) {
	    if(isConnected()) {
		write("$To: "+user+ " From: "+condata.nick+" $<"+condata.nick+"> "+text+"|");
	    }
	}
    }
    
    protected void sendConnectToMe(String othernick) {
        String ip;
        if(SettingsManager.getInstance().isActive()) {
            ip = getLocalIp();
        } else {
            ip = SettingsManager.getInstance().getIP();
        }
        write("$ConnectToMe "+othernick+" "+ip+":"+SettingsManager.getInstance().getTCPPort()+"|");
    }
    
    protected void addChatListener(ChatListener lis) {
        eventlistenerlist.add(ChatListener.class, lis);
    }
    
    protected void removeChatListener(ChatListener lis) {
        eventlistenerlist.remove(ChatListener.class, lis);
    }
    
    protected void addNickListener(NickListener lis) {
        eventlistenerlist.add(NickListener.class, lis);
    }
    
    protected void removeNickListener(NickListener lis) {
        eventlistenerlist.remove(NickListener.class, lis);
    }
    
    protected void removeAllListener() {
        eventlistenerlist = null;
        eventlistenerlist = new EventListenerList();
    }
    
    protected void fireChatEvent(String data) {
        for(ChatListener lis:eventlistenerlist.getListeners(ChatListener.class)) {
            lis.chatEvent(new ChatEvent(this,data));
        }
    }
    
    protected void firePrivateChatEvent(String user,String data) {
        for(ChatListener lis:eventlistenerlist.getListeners(ChatListener.class)) {
            lis.privateMessage(new PrivateMessageEvent(this,user,data));
        }
    }
    
    protected void fireNickAdded(String nick, String share, String description, String tag, String speed, String email,String ip) {
        for(NickListener lis:eventlistenerlist.getListeners(NickListener.class)) {
            lis.nickChanged(new NickEvent(this,nick,share,description,tag,speed,email,ip));
        }
    }
    
    protected void fireNickRemoved(String nick, String share, String description, String tag, String speed, String email,String ip) {
        for(NickListener lis:eventlistenerlist.getListeners(NickListener.class)) {
            lis.removeNick(new NickEvent(this,nick,share,description,tag,speed,email,ip));
        }
    }
    
    public void run() {
        try {
            connect(condata.ip, condata.port);
            listen();
        } catch (UnknownHostException ex) {
            Logger.getLogger(Client2Server.class.getName()).log(Level.SEVERE, "Unknown Host");
        } catch (IOException ex) {
            Logger.getLogger(Client2Server.class.getName()).log(Level.SEVERE, "Closed");
        }
    }

    public String getIp() {
        return condata.ip;
    }

    public String getNick() {
        return condata.nick;
    }

    public int getPort() {
        return condata.port;
    }

    public String getPw() {
        return condata.pw;
    }
    
    public String getHubName() {
        return condata.hubname;
    }
    
    @Override
    protected void connectionClosed() {
        fireChatEvent("Connection closed");
    }    
}
