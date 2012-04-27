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

package calyriumdc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

public class FileListReader {
    
    protected static String getHash(File file) {
        try {
            XMLEvent evt;
            InputStream in = new FileInputStream(SettingsManager.getInstance().getHashIndex());
            XMLInputFactory factory = XMLInputFactory.newInstance();
            XMLEventReader parser = factory.createXMLEventReader(in);
            while (parser.hasNext()) {
                evt = parser.nextEvent();
                if (evt.isStartElement()) {
                    StartElement elem = evt.asStartElement();
                    if (elem.getName().toString().equals("File") && elem.getAttributeByName(new QName("Name")).getValue().equals(file.getAbsolutePath())) {
                        return elem.getAttributeByName(new QName("Root")).getValue();
                    }
                }
            }
        } catch (FileNotFoundException e) {
        } catch (XMLStreamException e) {
        }
        return null;
    }
    
    public static String getTigerTreeNodesAsString(String root) {
        InputStream in = null;
        try {
            XMLEvent evt;
            in = new FileInputStream(SettingsManager.getInstance().getHashData());
            XMLInputFactory factory = XMLInputFactory.newInstance();
            XMLEventReader parser = factory.createXMLEventReader(in);
            while (parser.hasNext()) {
                evt = parser.nextEvent();
                if (evt.isStartElement()) {
                    StartElement elem = evt.asStartElement();
                    if (elem.getName().toString().equals("File") && elem.getAttributeByName(new QName("Root")).getValue().equals(root)) {
                        return elem.getAttributeByName(new QName("Nodes")).getValue();
                    }
                }
            }
        } catch (XMLStreamException ex) {
            Logger.getLogger(FileListReader.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(FileListReader.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                in.close();
            } catch (IOException ex) {
                Logger.getLogger(FileListReader.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }
    
    public static byte[] getTigerTreeNodes(String root) {
        InputStream in = null;
        try {
            XMLEvent evt;
            in = new FileInputStream(SettingsManager.getInstance().getHashData());
            XMLInputFactory factory = XMLInputFactory.newInstance();
            XMLEventReader parser = factory.createXMLEventReader(in);
            while (parser.hasNext()) {
                evt = parser.nextEvent();
                if (evt.isStartElement()) {
                    StartElement elem = evt.asStartElement();
                    if (elem.getName().toString().equals("File") && elem.getAttributeByName(new QName("Root")).getValue().equals(root)) {
                        return Base32.decode(elem.getAttributeByName(new QName("Nodes")).getValue());
                    }
                }
            }
        } catch (XMLStreamException ex) {
            Logger.getLogger(FileListReader.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(FileListReader.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                in.close();
            } catch (IOException ex) {
                Logger.getLogger(FileListReader.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }
    
    public static String getPath(String root) {
        try {
            XMLEvent evt;
            InputStream in = new FileInputStream(SettingsManager.getInstance().getHashIndex());
            XMLInputFactory factory = XMLInputFactory.newInstance();
            XMLEventReader parser = factory.createXMLEventReader(in);
            while (parser.hasNext()) {
                evt = parser.nextEvent();
                if (evt.isStartElement()) {
                    StartElement elem = evt.asStartElement();
                    if (elem.getName().toString().equals("File") && elem.getAttributeByName(new QName("Root")).getValue().equals(root)) {
                        return elem.getAttributeByName(new QName("Name")).getValue();
                    }
                }
            }
        } catch (FileNotFoundException e) {
        } catch (XMLStreamException e) {
        }
        return "";
    }
}
