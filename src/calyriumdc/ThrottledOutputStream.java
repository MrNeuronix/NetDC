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

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;


public class ThrottledOutputStream extends FilterOutputStream {
    
    private long maxbps;
    private long bytes;
    private long start;
   
    public ThrottledOutputStream(OutputStream out) {
        this(out,0L);
    }
    
    public ThrottledOutputStream(OutputStream out,long bps) {
        super(out);
        this.maxbps = bps;
        bytes = 0;
        start = System.currentTimeMillis();
    }
        
    @Override
    public void write( int b ) throws IOException {
	write( new byte[]{(byte)b}, 0, 1 );
    }
    
    @Override
    public void write(byte[] b) throws IOException {
        write(b,0,b.length);
    }
    
    @Override
    public void write( byte b[], int off, int len ) throws IOException {
	if(maxbps == 0) {
            out.write(b, off, len);
            return;
        }
        bytes += len;
	long elapsed = (System.currentTimeMillis() - start)+1;
	long bps = bytes * 1000L / elapsed;
	if ( bps > maxbps ) {
	    long wakeElapsed = bytes * 1000L / maxbps;
	    try {
		Thread.sleep( wakeElapsed - elapsed );
            }
	    catch ( InterruptedException e ) {}
        }
	out.write( b, off, len );
    }
    
    public void setMaxbps(long bps) {
        maxbps = bps;
    }
    
    public long getMaxbps() {
        return maxbps;
    }
}
