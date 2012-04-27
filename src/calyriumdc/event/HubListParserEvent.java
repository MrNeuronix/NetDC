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
package calyriumdc.event;

import java.util.EventObject;

public class HubListParserEvent extends EventObject {

    private String name;
    private String address;
    private String description;
    private String country;
    private String users;
    private String shared;
    private String status;
    private String minshare;
    private String minslots;
    private String maxhubs;
    private String maxusers;
    private String reliability;
    private String rating;

    public HubListParserEvent(Object arg0, String name, String address, String description, String country, String users, String shared, String status, String minshare, String minslots, String maxhubs, String maxusers, String reliability, String rating) {
        super(arg0);
        this.name = name;
        this.address = address;
        this.description = description;
        this.country = country;
        this.users = users;
        this.shared = shared;
        this.status = status;
        this.minshare = minshare;
        this.minslots = minslots;
        this.maxhubs = maxhubs;
        this.maxusers = maxusers;
        this.reliability = reliability;
        this.rating = rating;
    }
    
    public String[] getData() {
        return new String[]{name,address,description,country,users,shared,status,minshare,minslots,maxhubs,maxusers,reliability,rating};
    }
    
    public String getAddress() {
        return address;
    }

    public String getCountry() {
        return country;
    }

    public String getDescription() {
        return description;
    }

    public String getMaxhubs() {
        return maxhubs;
    }

    public String getMaxusers() {
        return maxusers;
    }

    public String getMinshare() {
        return minshare;
    }

    public String getMinslots() {
        return minslots;
    }

    public String getName() {
        return name;
    }

    public String getRating() {
        return rating;
    }

    public String getReliability() {
        return reliability;
    }

    public String getShared() {
        return shared;
    }

    public String getStatus() {
        return status;
    }

    public String getUsers() {
        return users;
    }
}
