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

import calyriumdc.event.FileListParserEvent;
import calyriumdc.event.FileListParserListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import calyriumdc.EventListenerList;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.apache.tools.bzip2.CBZip2InputStream;

public class FileListParser {

    private File filelist;
    private EventListenerList evtl;

    public FileListParser(String filepath) {
        filelist = new File(filepath);
        evtl = new EventListenerList();
    }

    public void addFileListParserListener(FileListParserListener lis) {
        evtl.add(FileListParserListener.class, lis);
    }

    public void fireNewDirectory(String name) {
        for (FileListParserListener lis : evtl.getListeners(FileListParserListener.class)) {
            lis.newElement(new FileListParserEvent(this, FileListParserEvent.DIRECTORY, name));
        }
    }

    public void fireEndDirectory() {
        for (FileListParserListener lis : evtl.getListeners(FileListParserListener.class)) {
            lis.newElement(new FileListParserEvent(this, FileListParserEvent.DIRECTORY_CLOSED));
        }
    }

    public void fireFile(String name, String tth, String size) {
        for (FileListParserListener lis : evtl.getListeners(FileListParserListener.class)) {
            lis.newElement(new FileListParserEvent(this, FileListParserEvent.FILE, name, tth, size));
        }
    }

    public void parseDirectories() throws FileNotFoundException, IOException, XMLStreamException {
        XMLEvent evt;
        InputStream in;
        FileInputStream filein = new FileInputStream(filelist);
        if (filelist.getName().contains(".bz2")) {
            filein.skip(2);
            in = new CBZip2InputStream(filein);
        } else {
            in = filein;
        }

        XMLInputFactory factory = XMLInputFactory.newInstance();
        XMLEventReader parser = factory.createXMLEventReader(in);
        while (parser.hasNext()) {
            evt = parser.nextEvent();
            if (evt.isStartElement()) {
                StartElement elem = evt.asStartElement();
                if (elem.getName().toString().equals("Directory")) {
                    fireNewDirectory(elem.getAttributeByName(new QName("Name")).getValue());
                }
            }
            if (evt.isEndElement()) {
                EndElement elem = evt.asEndElement();
                if (elem.getName().toString().equals("Directory")) {
                    fireEndDirectory();
                }
            }
        }
        parser.close();
        in.close();
    }

    public void parseFiles(Object[] path) throws FileNotFoundException, IOException, XMLStreamException {
        XMLEvent evt;
        InputStream in;
        FileInputStream filein = new FileInputStream(filelist);
        if (filelist.getName().contains(".bz2")) {
            filein.skip(2);
            in = new CBZip2InputStream(filein);
        } else {
            in = filein;
        }

        XMLInputFactory factory = XMLInputFactory.newInstance();
        XMLEventReader parser = factory.createXMLEventReader(in);
        for (int i = 1; i < path.length; i++) {
            while (parser.hasNext()) {
                evt = parser.nextEvent();
                if (evt.isStartElement()) {
                    StartElement elem = evt.asStartElement();
                    if (elem.getName().toString().equals("Directory")) {
                        if (elem.getAttributeByName(new QName("Name")).getValue().equals(path[i].toString())) {
                            if (i == (path.length - 1)) {
                                int subdir = 0;
                                while (parser.hasNext()) {
                                    evt = parser.nextEvent();
                                    if (evt.isStartElement()) {
                                        elem = evt.asStartElement();
                                        if (elem.getName().toString().equals("File")) {
                                            if (subdir == 0) {
                                                String name = elem.getAttributeByName(new QName("Name")).getValue();
                                                String size = elem.getAttributeByName(new QName("Size")).getValue();
                                                String tth = elem.getAttributeByName(new QName("TTH")).getValue();
                                                fireFile(name, tth, size);
                                            }
                                        }
                                        if (elem.getName().toString().equals("Directory")) {
                                            subdir++;
                                        }
                                    }
                                    if (evt.isEndElement()) {
                                        EndElement end = evt.asEndElement();
                                        if (end.getName().toString().equals("Directory")) {
                                            if (subdir <= 0) {
                                                break;
                                            } else {
                                                subdir--;
                                            }
                                        }
                                    }
                                }
                                parser.close();
                                in.close();
                            }
                            break;
                        }
                    }
                }
            }
        }
    }
}
