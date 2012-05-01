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

package net.dc.lib.event;

import java.util.EventObject;

public class FileListCreatorEvent extends EventObject {
    
    public static final int SHARESIZE = 1;
    public static final int MAX_FILES = 2;
    public static final int HASHED_FILES = 3;
    public static final int HASHING = 4;
    
    private int state;
    private Object value;

    public FileListCreatorEvent(Object src, int state, Object value) {
        super(src);
        this.state = state;
        this.value = value;
    }

    public int getState() {
        return state;
    }

    public Object getValue() {
        return value;
    }    
}
