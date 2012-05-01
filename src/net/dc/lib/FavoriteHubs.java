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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

public class FavoriteHubs {

    public static Vector<Vector<String>> getAutoConnectHubs() {
        Vector<Vector<String>> vec = new Vector<Vector<String>>();
        InputStream in = null;
        try {
            XMLEvent evt;
            in = new FileInputStream(SettingsManager.getInstance().getFavoriteHubs());
            XMLInputFactory factory = XMLInputFactory.newInstance();
            XMLEventReader parser = factory.createXMLEventReader(in);
            while (parser.hasNext()) {
                evt = parser.nextEvent();
                if (evt.isStartElement()) {
                    StartElement elem = evt.asStartElement();
                    if (elem.getName().getLocalPart().equals("Hub")) {
                        if (new Boolean(elem.getAttributeByName(new QName("Connect")).getValue())) {
                            Vector<String> hub = new Vector<String>();
                            hub.add(elem.getAttributeByName(new QName("IP")).getValue());
                            hub.add(elem.getAttributeByName(new QName("Port")).getValue());
                            hub.add(elem.getAttributeByName(new QName("Nick")).getValue());
                            hub.add(elem.getAttributeByName(new QName("Password")).getValue());
                            vec.add(hub);
                        }
                    }
                }
            }
        } catch (XMLStreamException ex) {
            Logger.getLogger(FavoriteHubs.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(FavoriteHubs.class.getName()).log(Level.INFO, "favoritehubs.xml missing");
        } finally {
          //  try {
              //  in.close();
          //  } catch (IOException ex) {
          //      Logger.getLogger(FavoriteHubs.class.getName()).log(Level.SEVERE, null, ex);
          //  }
        }
        return vec;
    }

    public static Vector<Vector<Object>> getFavoriteHubs() {
        System.out.println(SettingsManager.getInstance().getFavoriteHubs());
        Vector<Vector<Object>> data = new Vector<Vector<Object>>();
        InputStream in = null;
        XMLEvent evt;
        try {
            in = new FileInputStream(SettingsManager.getInstance().getFavoriteHubs());
            XMLInputFactory factory = XMLInputFactory.newInstance();
            XMLEventReader parser = factory.createXMLEventReader(in);
            while (parser.hasNext()) {
                evt = parser.nextEvent();
                if (evt.isStartElement()) {
                    StartElement elem = evt.asStartElement();
                    if (elem.getName().getLocalPart().equals("Hub")) {
                        Vector<Object> vec = new Vector<Object>();
                        vec.add(new Boolean(elem.getAttributeByName(new QName("Connect")).getValue()));
                        vec.add(elem.getAttributeByName(new QName("Nick")).getValue());
                        vec.add(elem.getAttributeByName(new QName("Password")).getValue());
                        vec.add(elem.getAttributeByName(new QName("IP")).getValue());
                        vec.add(elem.getAttributeByName(new QName("Port")).getValue());
                        vec.add(elem.getAttributeByName(new QName("Description")).getValue());
                        data.add(vec);
                    }
                }
            }
        } catch (XMLStreamException ex) {
            Logger.getLogger(FavoriteHubs.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(FavoriteHubs.class.getName()).log(Level.INFO, "favoritehubs.xml missing");
        } finally {
            try {
                in.close();
            } catch (IOException ex) {
                Logger.getLogger(FavoriteHubs.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return data;
    }

    synchronized public static void saveFavoriteHubs(Vector<Vector> hubs) {
        System.out.println(SettingsManager.getInstance().getFavoriteHubs());
        OutputStream out = null;
        XMLEventWriter write = null;
        try {
            out = new FileOutputStream(SettingsManager.getInstance().getFavoriteHubs());
            XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
            write = outputFactory.createXMLEventWriter(out);
            XMLEventFactory ef = XMLEventFactory.newInstance();
            write.add(ef.createStartDocument("utf-8", "1.0", true));
            write.add(ef.createStartElement("", "", "Favorite-Hubs"));
            for (Vector oba : hubs) {
                write.add(ef.createStartElement("", "", "Hub"));
                write.add(ef.createAttribute("Connect", oba.elementAt(0).toString()));
                write.add(ef.createAttribute("Nick", oba.elementAt(1).toString()));
                write.add(ef.createAttribute("Password", oba.elementAt(2).toString()));
                write.add(ef.createAttribute("IP", oba.elementAt(3).toString()));
                write.add(ef.createAttribute("Port", oba.elementAt(4).toString()));
                write.add(ef.createAttribute("Description", oba.elementAt(5).toString()));
                write.add(ef.createEndElement("", "", "Hub"));
            }

            write.add(ef.createEndElement("", "", "Favorite-Hubs"));
            write.add(ef.createEndDocument());
            write.flush();
        } catch (XMLStreamException ex) {
            Logger.getLogger(FavoriteHubs.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(FavoriteHubs.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try { out.close(); } catch (IOException ex) {} 
        }
    }
}
