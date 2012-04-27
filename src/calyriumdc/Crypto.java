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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;

import org.apache.tools.bzip2.CBZip2InputStream;
import org.apache.tools.bzip2.CBZip2OutputStream;

public class Crypto {
    
    public static String getKey(String l) {
	String lock = l;
	//System.out.println(l);
	String key = "";
	
	byte[] charlock;
	int[] intlock;
	int[] intkey;
	
	charlock = lock.getBytes();
	intlock = new int[charlock.length];
	intkey = new int[charlock.length];
	
	for(int i = 0; i < charlock.length;i++) {
		intlock[i] = charlock[i];
	}
	
	for (int i = 1; i  < intlock.length; i++) {
		intkey[i] = intlock[i] ^intlock[i-1];
	}
	intkey[0] = intlock[0] ^ intlock[intlock.length -1] ^ intlock[intlock.length - 2] ^ 5;
	
	for (int i = 0; i < intkey.length; i++) {
		intkey[i] = ((intkey[i] << 4) & 240) | ((intkey[i] >> 4) & 15);
	}
	
	for (int i = 0; i < intkey.length;i++) {
		if(intkey[i] == 0) {
			key = key + "/%DCN000%/";
		} else {
			if(intkey[i] == 5) {
				key = key + "/%DCN005%/";
			} else {
				if(intkey[i] == 36) {
					key = key + "/%DCN036%/";
				} else {
					if(intkey[i] == 96) {
						key = key + "/%DCN096%/";
					} else {
						if(intkey[i] == 124) {
							key = key + "/%DCN124%/";
						} else {
							if(intkey[i] == 126) {
								key = key + "DCN126%/";
							} else {
								key = key + (char)intkey[i];
							}
						}
					}
				}
			}
		}
	}
	return key;
    }
    
    public static String getBytes(long bytes) {
	DecimalFormat df = new DecimalFormat("#.00");
	double size = bytes;
	if(size > Double.valueOf("1099511627776")) {
	    return df.format(size/Long.valueOf("1099511627776")) + " TB";
	} else if(size > 1073741824) {
	    return df.format(size/1073741824) + " GB";
	} else if(size > 1048576) {
	    return df.format(size/1048576) + " MB";
	} else if(size > 1024) {
	    return df.format(size/1024) + " KB";
	} else {
	    return df.format(size) + " B";
	}
    }
    
    public static void compressFile(File src) {
	byte[] mark = new byte[]{'B','Z'};
	File tmp = new File(src.getAbsolutePath()+".tmp");
	File zip = new File(src.getAbsolutePath()+".bz2");
	
	OutputStream os = null; 
	InputStream  is = null;
	int size;

	try {
	    os = new CBZip2OutputStream( new FileOutputStream( tmp ) ); 
	    is  = new FileInputStream( src );
	    byte[] buffer = new byte[ 1024 ];
	    while((size = is.read(buffer)) != -1) {
		os.write(buffer, 0, size);
	    }
	} catch ( IOException e ) {
	} finally { 
	    if ( is != null ) try { is.close(); } catch ( IOException e ) { } 
	    if ( os != null ) try { os.close(); } catch ( IOException e ) { } 
	}
	
	try {
	    os = new FileOutputStream(zip);
	    is = new FileInputStream(tmp);
	    byte[] buffer = new byte[1024];
	    os.write(mark);
	    while((size = is.read(buffer)) != -1) {
		os.write(buffer, 0, size);
	    }
	} catch (FileNotFoundException e) {
	} catch (IOException e) {
	} finally { 
	    if ( is != null ) try { is.close(); } catch ( IOException e ) { } 
	    if ( os != null ) try { os.close(); } catch ( IOException e ) { } 
	}
	tmp.delete();
    }
    
    public static void decompressFile(File src) {
	File unzip = new File(src.getAbsolutePath().substring(0, src.getAbsolutePath().lastIndexOf(".")));
	System.out.println("decomp");
	OutputStream os = null; 
	InputStream  is = null;
	int size;

	try {
	    os = new FileOutputStream( unzip ) ;
	    FileInputStream filein = new FileInputStream(src);
	    filein.skip(2);
	    is  = new CBZip2InputStream(filein);
	    byte[] buffer = new byte[ 1024 ];
	    while((size = is.read(buffer)) != -1) {
		os.write(buffer, 0, size);
	    }
	} catch ( IOException e ) {
	    e.printStackTrace();
	} finally { 
	    if ( is != null ) try { is.close(); } catch ( IOException e ) { } 
	    if ( os != null ) try { os.close(); } catch ( IOException e ) { } 
	} 
    }
}