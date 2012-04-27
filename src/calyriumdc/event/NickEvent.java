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
package calyriumdc.event;

import java.util.EventObject;

public class NickEvent extends EventObject{
    
    private String nick;
    private String share;
    private String description;
    private String tag;
    private String speed;
    private String email;
    private String ip;
    
    public NickEvent(Object src, String nick, String share, String description, String tag, String speed, String email, String ip) {
        super(src);
        this.nick = nick;
        this.share = share;
        this.description = description;
        this.tag = tag;
        this.speed = speed;
        this.email = email;
        this.ip = ip;
    }

    public String getDescription() {
        return description;
    }

    public String getEmail() {
        return email;
    }

    public String getNick() {
        return nick;
    }

    public String getShare() {
        return share;
    }

    public String getSpeed() {
        return speed;
    }

    public String getTag() {
        return tag;
    }

    public String getIp() {
        return ip;
    }
    
    @Override
    public String toString() {
        return nick+share+description+tag+speed+email+ip;
    }
}
