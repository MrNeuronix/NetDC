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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import android.os.Environment;

/**
 * Saves and manages all Settings.
 * @author Sebastian K&ouml;hler
 */
public class SettingsManager {

    /**
     * Singleton holder
     */
    private static final class Holder {

        private static final SettingsManager INSTANCE = new SettingsManager();
    }
    private static final String SETTINGS_PATH = Environment.getDataDirectory() + "/data/net.dc.core";
    private Properties prop;

    private SettingsManager() {
        prop = new Properties();
        loadFile();
    }

    public static SettingsManager getInstance() {
        return Holder.INSTANCE;
    }

    synchronized public void saveFile() {
        FileOutputStream out = null;
        try {
            File f = new File(SETTINGS_PATH);
            f.mkdirs();
            f = new File(SETTINGS_PATH + "/settings.xml");
            out = new FileOutputStream(f);
            prop.storeToXML(out, "CalyriumDC-Settings");
        } catch (FileNotFoundException ex) {
            Logger.getLogger(SettingsManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(SettingsManager.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                out.close();
            } catch (IOException ex) {
                Logger.getLogger(SettingsManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void loadFile() {
        loadDefault();
        File f = new File(SETTINGS_PATH + "/settings.xml");
        try {
            prop.loadFromXML(new FileInputStream(f));
        } catch (InvalidPropertiesFormatException e) {
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        }
    }

    synchronized public void loadDefault() {
        prop.setProperty("nick", "netDC-User");
        prop.setProperty("e-mail", "");
        prop.setProperty("description", "netDC Android client");
        prop.setProperty("uploadspeed", "0.005");
        prop.setProperty("sharesize", "0");
        prop.setProperty("password", "");
        prop.setProperty("sharedfolder", Environment.getExternalStorageDirectory().toString());
        prop.setProperty("filelist", SETTINGS_PATH + "files.xml");
        prop.setProperty("bzfilelist", SETTINGS_PATH + "files.xml.bz2");
        prop.setProperty("hashindex", SETTINGS_PATH + "HashIndex.xml");
        prop.setProperty("hashdata", SETTINGS_PATH + "HashData.xml");
        prop.setProperty("favhubs", SETTINGS_PATH + "favoritehubs.xml");
        prop.setProperty("favusers", SETTINGS_PATH + "favoriteusers.xml");
        prop.setProperty("hublist", SETTINGS_PATH + "hublist.xml.bz2");
        prop.setProperty("hublisturl", "http://dchublist.com/hublist.xml.bz2");
        prop.setProperty("filelistpath", SETTINGS_PATH);
        prop.setProperty("ip", "active");
        prop.setProperty("tcpport", "23344");
        prop.setProperty("udpport", "23344");
        prop.setProperty("maxpartsize", "8388608");
        prop.setProperty("downloadpath", Environment.getDataDirectory() + "/data/net.dc.core");
        prop.setProperty("incomplete", Environment.getDataDirectory() + "/data/net.dc.core" + "/incomplete/");
        prop.setProperty("dllimit", "1");
        prop.setProperty("uplimit", "1");
        prop.setProperty("maxuploadbps", "0");
        prop.setProperty("maxdownloadbps", "0");
        prop.setProperty("hubport", "411");
    }

    /*
     * Getter and Setter for Settings
     */
    
    public String getFavUsers() {
        return prop.getProperty("favusers");
    }
    
    public void setFavUsers(String path) {
        prop.setProperty("favusers",path);
    }
    
    public void setMaxUploadBps(long bps) {
        prop.setProperty("maxuploadbps", String.valueOf(bps));
    }
    
    public long getMaxUploadBps() {
        return Long.parseLong(prop.getProperty("maxuploadbps"));
    }
    
    public void setMaxDownloadBps(long bps) {
        prop.setProperty("maxdownloadbps", String.valueOf(bps));
    }
    
    public long getMaxDownloadBps() {
        return Long.parseLong(prop.getProperty("maxdownloadbps"));
    }
    
    public long getMaxPartsize() {
        return Long.parseLong(prop.getProperty("maxpartsize"));
    }
    
    public void setMaxPartsize(long value) {
        prop.setProperty("maxpartsize", String.valueOf(value));
    }
    
    public void setHublistURL(String data) {
        prop.setProperty("hublisturl", data);
    }

    public String getHublistURL() {
        return prop.getProperty("hublisturl");
    }

    public void setHublist(String data) {
        prop.setProperty("hublist", data);
    }

    public String getHublist() {
        return prop.getProperty("hublist");
    }

    public String getHashData() {
        return prop.getProperty("hashdata");
    }

    public void setHashData(String path) {
        prop.setProperty("hashdata", path);
    }

    public boolean isActive() {
        return getIP().equals("active");
    }

    public String getFilelistPath() {
        return prop.getProperty("filelistpath");
    }

    public String getUpLimit() {
        return prop.getProperty("uplimit");
    }

    public void setUpLimit(String value) {
        prop.setProperty("uplimit", value);
    }

    public String getBZFileList() {
        return prop.getProperty("bzfilelist");
    }

    public void setBZFileList(String value) {
        prop.setProperty("bzfilelist", value);
    }

    public String getFavoriteHubs() {
        return prop.getProperty("favhubs");
    }

    public void setFavoriteHubs(String value) {
        prop.setProperty("favhubs", value);
    }

    public String getDLLimit() {
        return prop.getProperty("dllimit");
    }

    public void setDLLimit(String value) {
        prop.setProperty("dllimit", value);
    }

    public String getIncomplete() {
        return prop.getProperty("incomplete");
    }

    public void setIncomplete(String value) {
        prop.setProperty("incomplete", value);
    }

    public String getDescription() {
        return prop.getProperty("description");
    }

    public String getEMail() {
        return prop.getProperty("email");
    }

    public String getNick() {
        return prop.getProperty("nick");
    }

    public String getPassword() {
        return prop.getProperty("password");
    }

    public String getSharesize() {
        return prop.getProperty("sharesize");
    }

    public String getUploadSpeed() {
        return prop.getProperty("uploadspeed");
    }

    public String getUDPPort() {
        return prop.getProperty("udpport");
    }

    public String getTCPPort() {
        return prop.getProperty("tcpport");
    }

    public String getIP() {
        return prop.getProperty("ip");
    }

    public String getHashIndex() {
        return prop.getProperty("hashindex");
    }

    public String getFileList() {
        return prop.getProperty("filelist");
    }

    public String getSharedFolder() {
        return prop.getProperty("sharedfolder");
    }

    public String getDownloadPath() {
        return prop.getProperty("downloadpath");
    }

    public void setDescription(String value) {
        prop.setProperty("description", value);
    }

    public void setEMail(String value) {
        prop.setProperty("email", value);
    }

    public void setNick(String value) {
        prop.setProperty("nick", value);
    }

    public void setPassword(String value) {
        prop.setProperty("password", value);
    }

    public void setSharesize(String value) {
        prop.setProperty("sharesize", value);
    }

    public void setUploadSpeed(String value) {
        prop.setProperty("uploadspeed", value);
    }

    public void setUDPPort(String value) {
        prop.setProperty("udpport", value);
    }

    public void setTCPPort(String value) {
        prop.setProperty("tcpport", value);
    }

    public void setIP(String value) {
        prop.setProperty("ip", value);
    }

    public void setHashIndex(String value) {
        prop.setProperty("hashindex", value);
    }

    public void setFileList(String value) {
        prop.setProperty("filelist", value);
    }

    public void setSharedFolder(String value) {
        prop.setProperty("sharedfolder", value);
    }

    public void setDownloadPath(String value) {
        prop.setProperty("downloadpath", value);
    }
    
    public void setHubPort(String value) {
        prop.setProperty("hubport", value);
    }

    public String getHubPort() {
        return prop.getProperty("hubport");
    }
}
