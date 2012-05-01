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
 */
package net.dc.lib;

import android.util.Log;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;

import net.dc.lib.EventListenerList;
import net.dc.lib.MerkleTree.Tree;
import net.dc.lib.event.FileListCreatorEvent;
import net.dc.lib.event.FileListCreatorListener;

public class FileListCreator {
    
    private static final class Holder {
         private final static FileListCreator INSTANCE = new FileListCreator();
    }
        
    private final XMLEventFactory ef = XMLEventFactory.newInstance();
    private long sharesize = 0;
    private int hashed = 0;
    
    private EventListenerList evtl;
    
    
    private FileListCreator() {
        evtl = new EventListenerList();
    }
    
    private void fireStateChanged(int state,Object value) {
        for(FileListCreatorListener lis:evtl.getListeners(FileListCreatorListener.class)) {
            lis.stateChanged(new FileListCreatorEvent(this,state,value));
        }
    }
    
    public void addFileListCreatorListener(FileListCreatorListener lis) {
        evtl.add(FileListCreatorListener.class, lis);
    }

    synchronized public void createHashIndex() {
        int maxfiles = 0;
        sharesize = 0;
        hashed = 0;
        Log.d("NET", "createHash");
        String[] folders = SettingsManager.getInstance().getSharedFolder().split(";");
        XMLEventWriter hashwrite = null;
        File hashindex = new File(SettingsManager.getInstance().getHashIndex());
        XMLEventWriter listwrite = null;
        File filelist = new File(SettingsManager.getInstance().getFileList());
        XMLEventWriter hashdatawrite = null;
        File hashdata = new File(SettingsManager.getInstance().getHashData());
        try {
            for(String f:folders) {
                if (f.contains(":")) {
                    String folder = f.split(":")[0];
                    maxfiles += getFileCount(new File(folder));
                    Log.d("NET", f + " "+ maxfiles);
                }
            }
            
            fireStateChanged(FileListCreatorEvent.MAX_FILES, maxfiles);
            hashwrite = startWriteHashIndex(hashindex);
            listwrite = startWriteFileList(filelist);
            hashdatawrite = startWriteHashData(hashdata);
            for (String f : folders) {
                if (f.contains(":")) {
                    String folder = f.split(":")[0];
                    File root = new File(folder);
                    hashFile(root, hashwrite, listwrite, hashdatawrite);
                }
            }
            stopWriteHashData(hashdatawrite, hashdata);
            stopWriteHashIndex(hashwrite, hashindex);
            stopWriteFileList(listwrite, filelist);
            SettingsManager.getInstance().saveFile();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(FileListCreator.class.getName()).log(Level.SEVERE, null, ex);
        } catch (XMLStreamException ex) {
            Logger.getLogger(FileListCreator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void hashFile(File root, XMLEventWriter hashwrite, XMLEventWriter listwrite, XMLEventWriter hashdatawrite) throws XMLStreamException {
        if (root.isDirectory()) {
            listwrite.add(ef.createStartElement("", "", "Directory"));
            listwrite.add(ef.createAttribute("Name", root.getName()));
            for (File f : root.listFiles()) {
                hashFile(f, hashwrite, listwrite, hashdatawrite);
            }
            listwrite.add(ef.createEndElement("", "", "Directory"));
        } else {
            if(root.length() == 0) {
                hashed++;
                fireStateChanged(FileListCreatorEvent.HASHED_FILES ,hashed);
                return;
            }
            fireStateChanged(FileListCreatorEvent.HASHING, root.getName());
            String tth = FileListReader.getHash(root);
            String nodes = null;
            if (tth == null) {
                Tree tmp = MerkleTree.getTree(root);
                tth = Base32.encode(tmp.getRoot());
                nodes = Base32.encode(tmp.getNodes());
            }
            if (nodes == null) {
                nodes = FileListReader.getTigerTreeNodesAsString(tth);
                if (nodes == null) {
                    nodes = Base32.encode(MerkleTree.getTree(root).getNodes());
                }
            }
            hashed++;
            sharesize += root.length();
            fireStateChanged(FileListCreatorEvent.HASHED_FILES ,hashed);
            fireStateChanged(FileListCreatorEvent.SHARESIZE ,sharesize);
            SettingsManager.getInstance().setSharesize(String.valueOf(sharesize));
            hashdatawrite.add(ef.createStartElement("", "", "File"));
            hashdatawrite.add(ef.createAttribute("Root", tth));
            hashdatawrite.add(ef.createAttribute("Size", String.valueOf(root.length())));
            hashdatawrite.add(ef.createAttribute("Nodes", nodes));
            hashdatawrite.add(ef.createEndElement("", "", "File"));
            hashwrite.add(ef.createStartElement("", "", "File"));
            hashwrite.add(ef.createAttribute("Name", root.getAbsolutePath()));
            hashwrite.add(ef.createAttribute("Root", tth));
            hashwrite.add(ef.createAttribute("Size", String.valueOf(root.length())));
            hashwrite.add(ef.createEndElement("", "", "File"));
            listwrite.add(ef.createStartElement("", "", "File"));
            listwrite.add(ef.createAttribute("Name", root.getName()));
            listwrite.add(ef.createAttribute("Size", String.valueOf(root.length())));
            listwrite.add(ef.createAttribute("TTH", tth));
            listwrite.add(ef.createEndElement("", "", "File"));
        }
    }

    private XMLEventWriter startWriteFileList(File filelist) throws FileNotFoundException, XMLStreamException {

        XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
        XMLEventWriter listwrite = outputFactory.createXMLEventWriter(new FileOutputStream(filelist.getAbsoluteFile() + "tmp"));
        listwrite.add(ef.createStartDocument("utf-8", "1.0", true));
        listwrite.add(ef.createStartElement("", "", "FileListing"));
        listwrite.add(ef.createAttribute("Version", "1"));
        listwrite.add(ef.createAttribute("CID", "QXN6KESZL7UFDTIQ6JKYI5ZFYNDFGJRR5UA6OSY"));
        listwrite.add(ef.createAttribute("Base", "/"));
        listwrite.add(ef.createAttribute("Generator", "CalyriumDC"));
        return listwrite;
    }

    private void stopWriteFileList(XMLEventWriter listwrite, File filelist) throws XMLStreamException {

        listwrite.add(ef.createEndElement("", "", "FileListing"));
        listwrite.add(ef.createEndDocument());
        listwrite.flush();
        listwrite.close();

        filelist.delete();
        new File(filelist.getAbsolutePath() + "tmp").renameTo(filelist);
        Crypto.compressFile(filelist);
    }

    private XMLEventWriter startWriteHashData(File hashdata) throws FileNotFoundException, XMLStreamException {

        XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
        XMLEventWriter hashdatawrite = outputFactory.createXMLEventWriter(new FileOutputStream(hashdata.getAbsolutePath() + "tmp"));
        hashdatawrite.add(ef.createStartDocument());
        hashdatawrite.add(ef.createStartElement("", "", "HashData"));
        return hashdatawrite;
    }

    private void stopWriteHashData(XMLEventWriter hashdatawrite, File hashdata) throws XMLStreamException {

        hashdatawrite.add(ef.createEndElement("", "", "HashData"));
        hashdatawrite.add(ef.createEndDocument());
        hashdatawrite.flush();
        hashdatawrite.close();

        hashdata.delete();
        new File(hashdata.getAbsolutePath() + "tmp").renameTo(hashdata);
    }

    private XMLEventWriter startWriteHashIndex(File hashindex) throws FileNotFoundException, XMLStreamException {

        XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
        XMLEventWriter hashwrite = outputFactory.createXMLEventWriter(new FileOutputStream(hashindex.getAbsolutePath() + "tmp"));
        hashwrite.add(ef.createStartDocument());
        hashwrite.add(ef.createStartElement("", "", "HashStore"));
        return hashwrite;

    }

    private void stopWriteHashIndex(XMLEventWriter hashwrite, File hashindex) throws XMLStreamException {

        hashwrite.add(ef.createEndElement("", "", "HashStore"));
        hashwrite.add(ef.createEndDocument());
        hashwrite.flush();
        hashwrite.close();

        hashindex.delete();
        new File(hashindex.getAbsolutePath() + "tmp").renameTo(hashindex);
    }
    
    private int getFileCount(File f) {
        if(f.isFile()) return 1;
        int erg = 0;
        for(File file:f.listFiles()) {
            erg += getFileCount(file);
        }
        return erg;
    }
    
    public static FileListCreator getInstance() {
        return Holder.INSTANCE;
    }
}
