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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author kart0ffelsack
 */
class DownloadQueueEntry {

    private String[] nicks;
    private File aimfile;
    private long size;
    private String tth;
    private byte[] nodes;
    private int hubid;
    private FileOutputStream fin;
    private boolean filelist = false;
    
    private int checkleavepos = 0;
    private long partsize = 65536;
    private long minpartsize = 65536;

    protected DownloadQueueEntry(boolean filelist, String filename, String nick, int hubid) throws FileNotFoundException {
        this.filelist = filelist;
        aimfile = new File(SettingsManager.getInstance().getFilelistPath() + filename);
        aimfile.deleteOnExit();
        fin = new FileOutputStream(aimfile, true);
        this.nicks = new String[]{nick};
        size = -1;
        this.hubid = hubid;
    }

    protected DownloadQueueEntry(String nick, String filename, String size, String tth) throws FileNotFoundException {
        this.nicks = new String[]{nick};
        aimfile = new File(SettingsManager.getInstance().getIncomplete() + "/" + filename);
        fin = new FileOutputStream(aimfile, true);
        this.size = Long.parseLong(size);
        this.tth = tth;
    }

    protected DownloadQueueEntry(String filename, String size, String tth) throws FileNotFoundException {
        this.nicks = new String[0];
        this.aimfile = new File(SettingsManager.getInstance().getIncomplete() + "/" + filename);
        fin = new FileOutputStream(aimfile, true);
        this.size = Long.parseLong(size);
        this.tth = tth;
    }

    protected void addNick(String nick) {
        this.nicks = Arrays.copyOf(nicks, nicks.length + 1);
        nicks[nicks.length - 1] = nick;
        Arrays.sort(nicks);
    }

    protected boolean isNickAdded(String nick) {
        return 0 <= Arrays.binarySearch(nicks, nick);
    }

    protected String getFirstNick() {
        return nicks[0];
    }

    protected File getAimfile() {
        return aimfile;
    }

    protected long getSize() {
        return size;
    }

    protected String getTth() {
        return tth;
    }

    protected boolean isFinished() {
        return aimfile.length() == size;
    }

    protected long getOffset() {
        return aimfile.length();
    }

    protected boolean isFilelist() {
        return filelist;
    }

    protected byte[] getPartSize() {
        if(filelist) return new byte[(int)size];
        if (size - aimfile.length() < partsize) {
            return new byte[(int) (size - aimfile.length())];
        }
        return new byte[(int)partsize];
    }
    
    protected boolean checkPart(byte[] part) {
        if(nodes == null) return true;
        byte[] tth = MerkleTree.getTree(part,(int)minpartsize).getNodes();
        byte[] tmp = Arrays.copyOfRange(nodes,checkleavepos, tth.length+checkleavepos);
        if(Arrays.equals(tth, tmp)) {
            checkleavepos += tth.length;
            return true;
        } else {
            return false;
        }
    }

    protected void writePart(byte[] part) {
        try {
            fin = new FileOutputStream(aimfile,true);
            fin.write(part, 0, part.length);
        } catch (IOException ex) {
            Logger.getLogger(DownloadQueueEntry.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                fin.close();
            } catch (IOException ex) {
                Logger.getLogger(DownloadQueueEntry.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    protected void setSize(long size) {
        this.size = size;
    }

    protected int getHubid() {
        return hubid;
    }

    protected void finish() {
        if (!filelist) {
            File f = new File(SettingsManager.getInstance().getDownloadPath() + "/" + aimfile.getName());
            aimfile.renameTo(f);
        }
    }

    public byte[] getLeaves() {
        return nodes;
    }
    
    public void doublePartsize() {
        if(partsize >= SettingsManager.getInstance().getMaxPartsize()) return;
        partsize *=2;
    }
    
    public void halfPartSize() {
        if(partsize > minpartsize) {
            partsize /=2;
        }
    }

    public void setNodes(byte[] leaves) {
        this.nodes = leaves;
        int chunks = leaves.length / 24;
        long tmpsize = size / chunks;
        if(0 < tmpsize  && tmpsize <= 98304) {
            minpartsize = 65536;
            partsize = 65536;
        }
        if(98304 < tmpsize  && tmpsize <= 196608) {
            minpartsize = 131072;
            partsize = 131072;
        }
        if(196608 < tmpsize  && tmpsize <= 327680) {
            minpartsize = 262144;
            partsize = 262144;
        }
        if(327680 < tmpsize  && tmpsize <= 786432) {
            minpartsize = 524288;
            partsize = 524288;
        }
        if(786432 < tmpsize  && tmpsize <= 1572864) {
            minpartsize = 1048576;
            partsize = 1048576;
        }
        if(1572864 < tmpsize  && tmpsize <= 3145728) {
            minpartsize = 2097152;
            partsize = 2097152;
        }
        if(3145728 < tmpsize  && tmpsize <= 6291456) {
            minpartsize = 4194304;
            partsize = 4194304;
        }
        if(6291456 < tmpsize  && tmpsize <= 12582912) {
            minpartsize = 8388608;
            partsize = 8388608;
        }
        if(12582912 < tmpsize  && tmpsize <= 25165824) {
            minpartsize = 16777216;
            partsize = 16777216;
        }
        if(25165824 < tmpsize  && tmpsize <= 50331648) {
            minpartsize = 33554432;
            partsize = 33554432;
        }
        if(50331648 < tmpsize) {
            minpartsize = 67108864;
            partsize = 67108864;
        }
    }
}
