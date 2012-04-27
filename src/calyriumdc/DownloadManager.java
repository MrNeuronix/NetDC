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
import java.util.Vector;

public class DownloadManager {
    
    private static final class Holder {
        private static final DownloadManager INSTANCE = new DownloadManager();
    }
    
    private DownloadQueueList dlq = new DownloadQueueList();
        
    private DownloadManager() {
        checkDirectories();
    }
    
    private void checkDirectories() {
        File f = new File(SettingsManager.getInstance().getDownloadPath());
        if(!f.exists()) f.mkdirs();
        f = new File(SettingsManager.getInstance().getIncomplete());
        if(!f.exists()) f.mkdirs();
    }
    
    public void addDownload(boolean filelist,String filename,String nick,int hubid) {
        if(dlq.hasNextDownloadForName(nick)) {
            dlq.addDownload(filelist, filename,nick,hubid);
        } else {
            dlq.addDownload(filelist, filename,nick,hubid);
            ConnectionManager.getInstance().connectToMe(hubid, nick);
        }
    }
    
    public void addDownload(String nick, String filename,String size, String tth,int hubid) {
        if(dlq.hasNextDownloadForName(nick)) {
            dlq.addDownload(nick, filename, size, tth);
        } else {
            dlq.addDownload(nick, filename, size, tth);
            ConnectionManager.getInstance().connectToMe(hubid, nick);
        }
    }
    
    public void addDownload(String filename,String size, String tth) {
        dlq.addDownload(filename, size, tth);
    }
    
    @Deprecated
    public Vector<DownloadQueueEntry> getDownloadsForName(String Nick) {
        return dlq.getDownloadsForNick(Nick);
    }
    
    public boolean hasMoreDownloadsForName(String name) {
        return dlq.hasNextDownloadForName(name);
    }
    
    public DownloadQueueEntry getNextDownloadForName(String name) {
        return dlq.getNextDownloadForName(name);
    }
    
    public void removeDownloadQueueEntry(DownloadQueueEntry entry) {
        if(entry.isFilelist()) {
       //     CalyriumDC.getApplication().showFileList(entry.getFirstNick(), entry.getAimfile(),entry.getHubid());
        }
        dlq.removeDownloadQueueEntry(entry);
    }
    
    public static DownloadManager getInstance() {
        return Holder.INSTANCE;
    }
}
