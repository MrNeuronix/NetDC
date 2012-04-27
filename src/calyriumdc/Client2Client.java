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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages a Connection with another client. 
 * 
 * @author Sebastian K&ouml;hler
 */
public class Client2Client extends Connection implements Runnable {
    
    public class ConnectionData {
        //Public readable access with getters
        private String nick = "";
        private String filename = "";
        private long size = 0;
        private long transfered = 0;
        
        //Only access within Client2Client
        private String lock;
        private boolean download = true;
        private boolean MiniSlots = false;
        private boolean XmlBZList = false;
        private boolean ADCGet = false;
        private boolean tthl = false;
        private boolean tthf = false;
        private boolean zlig = false;
                       
        private ConnectionData() {}
        
        private ConnectionData(boolean download) {
            this.download = download;
        }

        private void setFilename(String filename) {
            this.filename = filename;
        }

        private void setNick(String nick) {
            this.nick = nick;
        }

        private void setSize(long size) {
            this.size = size;
        }

        private void setTransfered(long transfered) {
            this.transfered = transfered;
        }
                
        public String getDirection() {
            if(download) {
                return "download";
            } else {
                return "upload";
            }
        }

        public String getFilename() {
            return filename;
        }

        public String getNick() {
            return nick;
        }

        public long getSize() {
            return size;
        }

        public long getTransfered() {
            return transfered;
        }
    }
    
    private static final DownloadManager DOWNLOAD_MANAGER = DownloadManager.getInstance();
       
    private ConnectionData condata;
    private DownloadQueueEntry downentry;
    private int updownnum = 8608;
    /**
     * Creates a Client to Client connection to the given ip and port.
     * 
     * @param ip remote ip adress
     * @param port port of the remote adress
     * @throws java.net.UnknownHostException
     * @throws java.io.IOException
     */
    protected Client2Client(String ip, int port) throws UnknownHostException, IOException {
        connect(ip, port);
        condata = new ConnectionData();
    }

    protected Client2Client(Socket sock) throws IOException {
        connect(sock);
        condata = new ConnectionData();
    }

    protected void startConnection(String nick, boolean download) {
        condata.download = download;
        write("$MyNick " + nick + "|");
        write("$Lock EXTENDEDPROTOCOLABCABCABCABCABCABC Pk=DCPLUSPLUS0.706ABCABC|");
        new Thread(this).start();
    }

    @Override
    protected void react(String command, String data) {
        if (command.equals("$MyNick")) {
            condata.setNick(data);
        }
        if (command.equals("$Lock")) {
            condata.lock = data.split(" ")[0];
            write("$MyNick " + SettingsManager.getInstance().getNick() + "|");
            write("$Lock EXTENDEDPROTOCOLABCABCABCABCABCABC Pk=DCPLUSPLUS0.706ABCABC|");
        }
        if (command.equals("$Direction")) {
            int num = Integer.parseInt(data.split(" ")[1]);
            updownnum = num;
        }
        if (command.equals("$Supports")) {
            //Supports MiniSlots XmlBZList ADCGet TTHL TTHF ZLIG |
            String[] tmp = data.split(" ");
            for (String s : tmp) {
                if (s.equals("MiniSlots")) {
                    condata.MiniSlots = true;
                }
                if (s.equals("XmlBZList")) {
                    condata.XmlBZList = true;
                }
                if (s.equals("ADCGet")) {
                    condata.ADCGet = true;
                }
                if (s.equals("TTHL")) {
                    condata.tthl = true;
                }
                if (s.equals("TTHF")) {
                    condata.tthf = true;
                }
                if (s.equals("ZLIG")) {
                    condata.zlig = true;
                }
            }
        }
        if (command.equals("$Key")) {
            write("$Supports MiniSlots XmlBZList ADCGet TTHF TTHL|");
            if (condata.download) {
                write("$Direction Download " + String.valueOf(updownnum * 2) + "|");
            } else {
                write("$Direction Upload " + String.valueOf(updownnum - 2) + "|");
            }
            write("$Key " + Crypto.getKey(condata.lock) + "|");
            if (condata.download) {
                if (!DOWNLOAD_MANAGER.hasMoreDownloadsForName(condata.getNick())) {
                    close();
                }
                downentry = DOWNLOAD_MANAGER.getNextDownloadForName(condata.getNick());
                if (downentry.isFilelist()) {
                    write("$ADCGET file files.xml.bz2 0 -1|");
                } else {
                    if (condata.tthl) {
                        write("$ADCGET tthl TTH/" + downentry.getTth() + " 0 -1|");
                    } else {
                        write("$ADCGET file TTH/" + downentry.getTth() + " " + downentry.getOffset() + " " + downentry.getPartSize().length + "|");
                    }
                    condata.setFilename(downentry.getAimfile().getName());
                    condata.setSize(downentry.getSize());
                }
            }
        }
        if (command.equals("$ADCSND")) {
            //$ADCSND tthl TTH/PPUROLR2WSYTGPLCM3KV4V6LJC36SCTFQJFDJKA 0 24
            //$ADCSND file TTH/PPUROLR2WSYTGPLCM3KV4V6LJC36SCTFQJFDJKA 0 1154
            this.stoplistening = true;
            System.out.println(data);
            if (data.split(" ")[0].equals("file")) {
                long offset = Long.parseLong(data.split(" ")[2]);
                long size = Long.parseLong(data.split(" ")[3]);
                if (downentry.isFilelist()) {
                    downentry.setSize(size);
                    condata.setSize(size);
                }
                saveFile(offset, downentry.getPartSize().length);
            } else if (data.split(" ")[0].equals("tthl")) {
                long size = Long.parseLong(data.split(" ")[3]);
                saveTTHL(size);
            } else {
                write("$Error Clienterror|");
                close();
            }
        }
        if (command.equals("$ADCGET")) {
            if (data.split(" ")[0].equals("file")) {
                String path = "";
                String tosend = "";
                long offset = 0;
                long size = -1;

                if (data.split(" ")[1].equals("files.xml.bz2")) {
                    path = SettingsManager.getInstance().getBZFileList();
                    tosend = data.split(" ")[0] + " " + data.split(" ")[1];
                    offset = Long.parseLong(data.split(" ")[2]);
                    size = Long.parseLong(data.split(" ")[3]);
                }
                if (data.split(" ")[1].equals("files.xml")) {
                    path = SettingsManager.getInstance().getFileList();
                    tosend = data.split(" ")[0] + " " + data.split(" ")[1];
                    offset = Long.parseLong(data.split(" ")[2]);
                    size = Long.parseLong(data.split(" ")[3]);
                }
                if (data.contains("TTH/")) {
                    if (UploadManager.getInstance().isSlotAvailable()) {
                        String tth = data.split(" ")[1].split("/")[1];
                        path = FileListReader.getPath(tth);
                        tosend = data.split(" ")[0] + " " + data.split(" ")[1];
                        offset = Long.parseLong(data.split(" ")[2]);
                        size = Long.parseLong(data.split(" ")[3]);
                    } else {
                        write("$MaxedOut|");
                        return;
                    }
                }
                File fl = new File(path);
                if (size == -1) {
                    size = fl.length();
                }
                //System.out.println("$ADCSND "+tosend+" "+offset+" "+size+"|");
                condata.setFilename(fl.getName());
                condata.setSize(size);
                write("$ADCSND " + tosend + " " + offset + " " + size + "|");
                uploadFile(fl, offset, size);
            }
            if(data.split(" ")[0].equals("tthl")) {
                String tth = data.split(" ")[1].split("/")[1];
                uploadTTHL(tth);
            }
        }
    }
    
    private void uploadTTHL(String tth) {
        try {
            //$ADCSND tthl TTH/YLKFPDS5ZVXAAUYD3M2KCFOI64WQXYTWTEYUE2A 0 6744|
            byte[] tthl = FileListReader.getTigerTreeNodes(tth);
            write("$ADCSND tthl TTH/" + tth + " 0 " + tthl.length + "|");
            getOut().write(tthl);
            getOut().flush();
        } catch (IOException ex) {
            Logger.getLogger(Client2Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void saveTTHL(long size) {
        InputStream in = null;
        byte[] buffer = new byte[1024];
        byte[] tthl = new byte[(int) size];
        int downloaded = 0;
        int len = 0;

        in = getIn();
        setTimeout(5000);
        try {
            while (downloaded < size) {
                try {
                    len = in.read(buffer);
                } catch (SocketTimeoutException e) {
                    e.printStackTrace();
                    break;
                }
                for (int i = 0; i < len; i++) {
                    tthl[downloaded + i] = buffer[i];
                }
                downloaded += len;
            }
            downentry.setNodes(tthl);
            write("$ADCGET file TTH/" + downentry.getTth() + " " + downentry.getOffset() + " " + downentry.getPartSize().length + "|");
            startListening();
        } catch (IOException ex) {
            Logger.getLogger(Client2Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void uploadFile(File file, long offset, long size) {

        UploadManager.getInstance().addSlot();
        long uploaded = 0;
        byte[] buffer = new byte[1024];
        int len = 0;
        OutputStream out = null;
        InputStream in = null;
        try {
            in = new FileInputStream(file);
            in.skip(offset);
            out = new ThrottledOutputStream(getOut(),SettingsManager.getInstance().getMaxUploadBps());
            while (uploaded < size && len != -1) {
                len = in.read(buffer);
                out.write(buffer, 0, len);
                uploaded += len;
                condata.setTransfered(condata.getTransfered()+uploaded);
            }
            out.flush();
        } catch (FileNotFoundException e) {
            close();
            e.printStackTrace();
            removeThisFromConnectionManager();
        } catch (IOException e) {
            close();
            e.printStackTrace();
            removeThisFromConnectionManager();
        } finally {
            UploadManager.getInstance().removeSlots();
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
        }
        if (!isClosed()) {
            startListening();
        }

    }

    private void saveFile(long offset, long size) {
        InputStream in = null;
        byte[] buffer = new byte[1024];
        byte[] filepart = downentry.getPartSize();
        int downloaded = 0;
        int len = 0;
        try {
            in = getIn();
            setTimeout(10000);
            while (downloaded < size) {
                try {
                    len = in.read(buffer);
                } catch (SocketTimeoutException e) {
                    e.printStackTrace();
                    break;
                }
                for (int i = 0; i < len; i++) {
                    filepart[downloaded + i] = buffer[i];
                }
                downloaded += len;
                condata.setTransfered(condata.getTransfered()+len);
            }
        } catch (FileNotFoundException e) {
            close();
            e.printStackTrace();
            removeThisFromConnectionManager();
        } catch (IOException e) {
            close();
            e.printStackTrace();
            removeThisFromConnectionManager();
        }
        if (downentry.checkPart(filepart)) {
            downentry.writePart(filepart);
            downentry.doublePartsize();
        } else {
            downentry.halfPartSize();
        }
        if (!isClosed()) {
            System.out.println(downentry.getAimfile().length() + ">=" + downentry.getSize());
            if (downentry.getAimfile().length() >= downentry.getSize()) {
                downentry.finish();
                DOWNLOAD_MANAGER.removeDownloadQueueEntry(downentry);
                //System.out.println("more: "+ dlmanage.hasMoreDownloadsForName(othernick));
                if (DOWNLOAD_MANAGER.hasMoreDownloadsForName(condata.getNick())) {

                    downentry = DOWNLOAD_MANAGER.getNextDownloadForName(condata.getNick());

                    if (downentry.isFilelist()) {
                        write("$ADCGET file files.xml.bz2 0 -1 |");
                    } else {
                        write("$ADCGET file TTH/" + downentry.getTth() + " " + downentry.getOffset() + " " + downentry.getPartSize().length + "|");
                        condata.setFilename(downentry.getAimfile().getName());
                        condata.setSize(downentry.getSize());
                    }
                    startListening();

                } else {
                    close();
                    removeThisFromConnectionManager();
                }
            } else {
                if (downentry.isFilelist()) {
                    write("$ADCGET file files.xml.bz2 " + downentry.getOffset() + " " + downentry.getPartSize().length + "|");
                } else {
                    write("$ADCGET file TTH/" + downentry.getTth() + " " + downentry.getOffset() + " " + downentry.getPartSize().length + "|");
                }
                startListening();
            }
        }
    }

    private void removeThisFromConnectionManager() {
        ConnectionManager.getInstance().removeClient2Client(this);
    }
    
    public void run() {
        try {
            stoplistening = false;
            listen();
        } catch (SocketTimeoutException ex) {
            Logger.getLogger(Client2Client.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Client2Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void startListening() {
        try {
            stoplistening = false;
            listen();
        } catch (SocketTimeoutException ex) {
            Logger.getLogger(Client2Client.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Client2Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
        
    public ConnectionData getConnectionData() {
        return condata;
    }
    
    @Override
    protected void connectionClosed() {
    }

    
}

