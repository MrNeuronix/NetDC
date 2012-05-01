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

public class FavoriteUsers {

    private static final class Holder {

        private final static FavoriteUsers INSTANCE = new FavoriteUsers();
    }
    private Vector<Object[]> users;

    private FavoriteUsers() {
        users = loadFromXml();
    }

    private Vector<Object[]> loadFromXml() {
        Vector<Object[]> users = new Vector<Object[]>();
        InputStream in = null;
        XMLEvent evt;
        try {
            in = new FileInputStream(SettingsManager.getInstance().getFavUsers());
            XMLInputFactory factory = XMLInputFactory.newInstance();
            XMLEventReader parser = factory.createXMLEventReader(in);
            while (parser.hasNext()) {
                evt = parser.nextEvent();
                if (evt.isStartElement()) {
                    StartElement elem = evt.asStartElement();
                    if (elem.getName().getLocalPart().equals("User")) {
                        Object[] entry = new Object[4];
                        entry[0] = elem.getAttributeByName(new QName("Nick")).getValue();
                        entry[1] = elem.getAttributeByName(new QName("Hub")).getValue();
                        entry[2] = new Boolean(elem.getAttributeByName(new QName("Slot")).getValue());
                        entry[3] = new Boolean(elem.getAttributeByName(new QName("Bypass")).getValue());
                        users.add(entry);
                    }
                }
            }
        } catch (XMLStreamException ex) {
            Logger.getLogger(FavoriteUsers.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(FavoriteUsers.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                in.close();
            } catch (IOException ex) {
            }
        }
        return users;
    }

    synchronized private void saveToXml() {
        new Thread(new Runnable() {
            public void run() {
                OutputStream out = null;
                XMLEventWriter write = null;
                try {
                    out = new FileOutputStream(SettingsManager.getInstance().getFavUsers());
                    XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
                    write = outputFactory.createXMLEventWriter(out);
                    XMLEventFactory ef = XMLEventFactory.newInstance();
                    write.add(ef.createStartDocument("utf-8", "1.0", true));
                    write.add(ef.createStartElement("", "", "Favorite-Users"));
                    for (Object[] entry : users) {
                        write.add(ef.createStartElement("", "", "User"));
                        write.add(ef.createAttribute("Nick", entry[0].toString()));
                        write.add(ef.createAttribute("Hub", entry[1].toString()));
                        write.add(ef.createAttribute("Slot", entry[2].toString()));
                        write.add(ef.createAttribute("Bypass", entry[3].toString()));
                        write.add(ef.createEndElement("", "", "User"));
                    }
                    write.add(ef.createEndElement("", "", "Favorite-Users"));
                    write.add(ef.createEndDocument());
                    write.flush();
                } catch (XMLStreamException ex) {
                    Logger.getLogger(FavoriteUsers.class.getName()).log(Level.SEVERE, null, ex);
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(FavoriteUsers.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    try {
                        out.close();
                    } catch (IOException ex) {
                    }
                }
            }
        }).start();
    }

    public void addUser(String nick, String hub, boolean slot, boolean bypass) {
        if (containsUser(nick, hub)) {
            return;
        }
        users.add(new Object[]{nick, hub, slot, bypass});
        saveToXml();
    }

    public void removeUser(String nick, String hub) {
        if (!containsUser(nick, hub)) {
            return;
        }
    //TODO: Remove fertig machen
    }

    public boolean containsUser(String nick, String hub) {
        for (Object[] entry : users) {
            if (entry[0].equals(nick) && entry[1].equals(hub)) {
                return true;
            }
        }
        return false;
    }

    public static FavoriteUsers getInstance() {
        return Holder.INSTANCE;
    }
}
