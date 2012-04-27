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

public class UploadManager {
    
    private static final class Holder {
        private static final UploadManager INSTANCE = new UploadManager();
    }
    
    private int usedslots = 0;
    
    private UploadManager(){}
        
    public void addSlot() {
        usedslots++;
    }
    
    public void removeSlots() {
        usedslots--;
    }
    
    public boolean isSlotAvailable() {
        return usedslots < Integer.parseInt(SettingsManager.getInstance().getUpLimit());
    }
    
    public static UploadManager getInstance() {
        return Holder.INSTANCE;
    }

}
