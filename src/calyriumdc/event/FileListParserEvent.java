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

public class FileListParserEvent extends EventObject {
    
    public static final int DIRECTORY = 1;
    public static final int FILE = 2;
    public static final int DIRECTORY_CLOSED = 3;
    
    private int state;
    private String name;
    private String tth;
    private String size;

    public FileListParserEvent(Object arg0, int state) {
        super(arg0);
        this.state = state;
    }

    public FileListParserEvent(Object arg0, int state, String name) {
        super(arg0);
        this.state = state;
        this.name = name;
    }

    public FileListParserEvent(Object arg0, int state, String name, String tth, String size) {
        super(arg0);
        this.state = state;
        this.name = name;
        this.tth = tth;
        this.size = size;
    }

    public String getName() {
        return name;
    }

    public String getSize() {
        return size;
    }

    public int getState() {
        return state;
    }

    public String getTth() {
        return tth;
    }

    
}
