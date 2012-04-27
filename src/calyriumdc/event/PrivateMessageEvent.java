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

public class PrivateMessageEvent extends EventObject {
    
    private String user;
    private String data;
    
    public PrivateMessageEvent(Object src, String user, String data) {
        super(src);
        this.user = user;
        this.data = data;
    }
    
    public String getUser() {
        return user;
    }
    
    public String getData() {
        return data;
    }
}
