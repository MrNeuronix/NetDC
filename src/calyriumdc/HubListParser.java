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

import calyriumdc.event.HubListParserEvent;
import calyriumdc.event.HubListParserListener;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import calyriumdc.EventListenerList;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import org.apache.tools.bzip2.CBZip2InputStream;

public class HubListParser {
    
    private EventListenerList evtl;
    
    public HubListParser() {
        evtl = new EventListenerList();
    }

    public void downloadHublist() {

        InputStream in = null;
        OutputStream out = null;
        byte[] buffer = new byte[1024];
        int len = 0;
        try {
            URL url = new URL(SettingsManager.getInstance().getHublistURL());
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.connect();
            in = con.getInputStream();
            out = new FileOutputStream(SettingsManager.getInstance().getHublist());
            while (-1 != (len = in.read(buffer))) {
                out.write(buffer, 0, len);
            }
        } catch (MalformedURLException ex) {
            Logger.getLogger(HubListParser.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(HubListParser.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                }
            }
        }
    }

    public void parseHublist() {
        FileInputStream filein = null;
        try {
            XMLEvent evt;
            InputStream in = null;
            filein = new FileInputStream(SettingsManager.getInstance().getHublist());
            filein.skip(2);
            in = new CBZip2InputStream(filein);
            XMLInputFactory factory = XMLInputFactory.newInstance();
            XMLEventReader parser = factory.createXMLEventReader(in);

            while (parser.hasNext()) {
                evt = parser.nextEvent();
                if (evt.isStartElement()) {
                    StartElement elem = evt.asStartElement();
                    if (elem.getName().getLocalPart().equals("Hub")) {
                        String name = elem.getAttributeByName(new QName("Name")).getValue();
                        String address = elem.getAttributeByName(new QName("Address")).getValue();
                        String description = elem.getAttributeByName(new QName("Description")).getValue();
                        String country = elem.getAttributeByName(new QName("Country")).getValue();
                        String users = elem.getAttributeByName(new QName("Users")).getValue();
                        String shared = elem.getAttributeByName(new QName("Shared")).getValue();
                        String status = elem.getAttributeByName(new QName("Status")).getValue();
                        String minshare = elem.getAttributeByName(new QName("Minshare")).getValue();
                        String minslots = elem.getAttributeByName(new QName("Minslots")).getValue();
                        String maxhubs = elem.getAttributeByName(new QName("Maxhubs")).getValue();
                        String maxusers = elem.getAttributeByName(new QName("Maxusers")).getValue();
                        String reliability = elem.getAttributeByName(new QName("Reliability")).getValue();
                        String rating = elem.getAttributeByName(new QName("Rating")).getValue();
                        fireHubFound(name, address, description, country, users, shared, status, minshare, minslots, maxhubs, maxusers, reliability, rating);
                    }
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(HubListParser.class.getName()).log(Level.SEVERE, null, ex);
        } catch (XMLStreamException ex) {
            Logger.getLogger(HubListParser.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(HubListParser.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                filein.close();
            } catch (IOException ex) {
                Logger.getLogger(HubListParser.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void addHubListParserListener(HubListParserListener lis) {
        evtl.add(HubListParserListener.class, lis);
    }
    
    private void fireHubFound(String name, String address, String description, String country, String users, String shared, String status, String minshare, String minslots, String maxhubs, String maxusers, String reliability, String rating) {
       for(HubListParserListener lis:evtl.getListeners(HubListParserListener.class)) {
           lis.hubFound(new HubListParserEvent(this,name,address,description,country,users,shared,status,minshare,minslots,maxhubs,maxusers,reliability,rating));
       } 
    }
}
