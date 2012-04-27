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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MerkleTree {
    
    public class Tree {
        private byte[] root;
        private byte[] nodes;
        private int nodesize;
        
        private Tree() {
            
        }

        private Tree(byte[] root, byte[] nodes, int nodesize) {
            this.root = root;
            this.nodes = nodes;
            this.nodesize = nodesize;
        }

        public byte[] getNodes() {
            return nodes;
        }

        public int getNodesize() {
            return nodesize;
        }

        public byte[] getRoot() {
            return root;
        }

        private void setNodes(byte[] nodes) {
            this.nodes = nodes;
        }

        private void setNodesize(int nodesize) {
            this.nodesize = nodesize;
        }

        private void setRoot(byte[] root) {
            this.root = root;
        }
        
    }
    
    private static final int BLOCKSIZE = 1024;
    
    private int nodesize = 1024;
    private Vector<byte[]> nodes = new Vector<byte[]>();
    private MessageDigest tiger;
    
    public MerkleTree() {
        tiger = new Tiger();
    }
    
    public MerkleTree(int nodesize) {
        this.nodesize = nodesize;
        tiger = new Tiger();
    }
    
    public void createLeaves(byte[] data) {
        byte[] tmp = new byte[BLOCKSIZE];
        int index = 0;
        while((data.length - index) >= BLOCKSIZE) {
            System.arraycopy(data, index, tmp, 0, BLOCKSIZE);
            tiger.reset();
            tiger.update((byte)0);
            tiger.update(tmp);
            nodes.add(tiger.digest());
            index +=1024;
        }
        if(data.length -index > 0) {
            tmp = new byte[data.length -index];
            System.arraycopy(data, index, tmp, 0, data.length -index);
            tiger.reset();
            tiger.update((byte)0);
            tiger.update(tmp);
            nodes.add(tiger.digest());
        }
    }
    
    public void createLeaves(File f) throws FileNotFoundException {
        System.out.println(f.length());
        BufferedInputStream fin = null;
        byte[] data = new byte[BLOCKSIZE];
        int len = 0;
        try {
            fin = new BufferedInputStream(new FileInputStream(f));
            while (-1 !=(len = fin.read(data,0,BLOCKSIZE))) {            
                if(len == 1024) {
                    tiger.reset();
                    tiger.update((byte) 0);
                    tiger.update(data);
                    nodes.add(tiger.digest());
                } else {
                    tiger.reset();
                    tiger.update((byte) 0);
                    tiger.update(data,0,len);
                    nodes.add(tiger.digest());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(fin != null) {
               try { 
                   fin.close();
               } catch(IOException e) {
                   
               }
            }
        }
        
    }
    
    public Tree calculateTree() {
        int pos;
        int size = 1024;
        Tree tree = new Tree();
        tree.setNodesize(nodesize);
        while(nodes.size() > 1) {
            if(size == nodesize) {
                byte[] tmp = new byte[24*nodes.size()];
                int index = 0;
                for(byte[] bytes:nodes) {
                    System.arraycopy(bytes, 0, tmp, index, bytes.length);
                    index +=bytes.length;
                }
                tree.setNodes(tmp);
            }
            pos = 0;
            while(pos < nodes.size()) {
                byte[] one = nodes.elementAt(pos);
                nodes.remove(pos);
                if(pos < nodes.size()) {
                    byte[] two = nodes.elementAt(pos);
                    nodes.remove(pos);
                    tiger.reset();
                    tiger.update((byte)1);
                    tiger.update(one);
                    tiger.update(two);  
                    nodes.insertElementAt(tiger.digest(),pos);
                } else {
                    nodes.insertElementAt(one, pos);
                }
                pos++;
            }
            size *= 2;
        }
        if(tree.getNodes() == null) tree.setNodes(nodes.elementAt(0));
        tree.setRoot(nodes.elementAt(0));
        return tree;
    }
    
    public static Tree getTree(File file) {
        try {
            MerkleTree mt = new MerkleTree(65536);
            mt.createLeaves(file);
            return mt.calculateTree();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MerkleTree.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public static Tree getTree(byte[] data,int partsize) {
        MerkleTree mt = new MerkleTree(partsize);
        mt.createLeaves(data);
        return mt.calculateTree();
    }
}
