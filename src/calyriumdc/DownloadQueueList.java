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

import java.io.FileNotFoundException;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

class DownloadQueueList  {
    
    private Vector<DownloadQueueEntry> vec = new Vector<DownloadQueueEntry>();
    
    protected void addDownload(boolean filelist,String filename,String nick,int hubid) {
        try {
            for (DownloadQueueEntry entry : vec) {
                if (entry.getAimfile().getName().equals(filename)) {
                    return;
                }
            }
            vec.add(new DownloadQueueEntry(filelist, filename, nick, hubid));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(DownloadQueueList.class.getName()).log(Level.SEVERE, "DownloadNotAdded", ex);
        }
    }
    
    protected void addDownload(String nick, String filename,String size, String tth) {
        try {
            for (DownloadQueueEntry entry : vec) {
                if (entry.getTth().equals(tth)) {
                    if (entry.isNickAdded(nick)) {
                        return;
                    } else {
                        entry.addNick(nick);
                        return;
                    }
                }
            }
            vec.add(new DownloadQueueEntry(nick, filename, size, tth));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(DownloadQueueList.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    protected void addDownload(String filename,String size, String tth) {
        try {
            for (DownloadQueueEntry entry : vec) {
                if (entry.getTth().equals(tth)) {
                    return;
                }
            }
            vec.add(new DownloadQueueEntry(filename, size, tth));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(DownloadQueueList.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    protected void removeDownloadQueueEntry(DownloadQueueEntry entry) {
        vec.remove(entry);
    }
    
    protected boolean hasNextDownloadForName(String Nick) {
        for(DownloadQueueEntry entry:vec) {
            if(entry.isNickAdded(Nick)) {
                    return true;
            }
        }
        return false;
    }
    
    protected DownloadQueueEntry getNextDownloadForName(String nick) {
        for(DownloadQueueEntry entry:vec) {
            if(entry.isNickAdded(nick)) {
                    return entry;
            }
        }
        return null;
    }
    
    /**
     * 
     * @param Nick
     * @return
     * @deprecated Use hasNextDownloadForName(String Nick) and getNextDownloadForName(String nick) instead
     */
    @Deprecated
    protected Vector<DownloadQueueEntry> getDownloadsForNick(String Nick) {
        Vector<DownloadQueueEntry> tmp = new Vector<DownloadQueueEntry>();
        for(DownloadQueueEntry entry:vec) {
            if(entry.isNickAdded(Nick)) {
                tmp.add(entry);
            }
        }
        return tmp;
    }
      
}


