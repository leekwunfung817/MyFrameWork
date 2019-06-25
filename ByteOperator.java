/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Reinfo
 */
public class ByteOperator {

    public static void main(String[] args) throws Exception {
        TreeMap<String, String> tm = new TreeMap<String, String>();
        tm.put("{bb b}", ",baby,");
        tm.put("{d   d}", ",dady,");
        byte[] original_text = "aa{b bb}c{d d}e".getBytes("UTF-8");
        byte[] merged_text = claim_merge(tm, original_text);
        println(merged_text);
    }

    public static byte[] claim_merge(TreeMap<String, String> tm, byte[] original_text) throws Exception {
        TreeMap<String, String> ptm = claim_merge_data_preprocess(tm);
        TreeMap<String, String> mtm = claim_merge_tree(original_text);
        for (Map.Entry<String, String> entrySet : mtm.entrySet()) {
            String key = entrySet.getKey();
            String value = entrySet.getValue();
            String bv = ptm.get(value);
            println(key + "=" + value + "={" + bv + "}");
            original_text = replace(original_text, utf(key), utf(bv));
        }
        return original_text;
    }

    private static TreeMap<String, String> claim_merge_data_preprocess(TreeMap<String, String> tm) throws Exception {

        TreeMap<String, String> ptm = new TreeMap<String, String>();
        for (Map.Entry<String, String> entrySet : tm.entrySet()) {
            String key = entrySet.getKey();
            String value = entrySet.getValue();
            ptm.put(filter(key), value);
        }

//        for (Map.Entry<String, String> entrySet : ptm.entrySet()) {
//            String key = entrySet.getKey();
//            String value = entrySet.getValue();
//            println(key + "=" + value);
//        }
        return ptm;
    }

    private static TreeMap<String, String> claim_merge_tree(byte[] original_text) throws Exception {

        byte[] divide_byte = "{".getBytes("UTF-8");
//        println(replace(original_text, divide_byte, utf("<")));
//        println(original_text);

        TreeMap<String, String> key_value = new TreeMap<String, String>();
        ArrayList<byte[]> arr = spilt(original_text, divide_byte);
        for (byte[] arr1 : arr) {
            ArrayList<byte[]> barr = spilt(arr1, "}".getBytes("UTF-8"));
            if (barr.size() > 0) {
                byte[] ele = barr.get(0);
                String merge_key = new String(ele);
                String merge_whole_key = "{" + merge_key + "}";
                merge_key = "{" + filter(merge_key) + "}";
//                println(merge_key);
                key_value.put(merge_whole_key, merge_key);
                original_text = replace(original_text, utf(merge_whole_key), utf("Ivan"));
            }
        }

//        println(original_text);
//        for (Map.Entry<String, String> entrySet : key_value.entrySet()) {
//            String key = entrySet.getKey();
//            String value = entrySet.getValue();
////            println(key + "=" + value);
//        }
        return key_value;
    }

    private static String filter(String text) throws Exception {
        text = text.replace("(", "");
        text = text.replace(")", "");
        text = text.replace("[", "");
        text = text.replace("]", "");
        return text.replace(" ", "").replace(".", "").toUpperCase();
    }

    private static byte[] utf(String text) throws Exception {
        return text.getBytes("UTF-8");
    }

    private static void println(byte[] original_text) throws Exception {
        println(new String(original_text));
    }

    private static void println(String original_text) throws Exception {
        System.out.println(original_text);
    }

    private static ArrayList<byte[]> spilt(byte[] original_text, byte[] divide_byte) throws Exception {
        ArrayList<byte[]> arr = new ArrayList<byte[]>();
        int index;
        byte[] result_after = null;
        while (strpos(original_text, divide_byte) != -1) {
            index = strpos(original_text, divide_byte);
//            System.out.println(index);

            byte[] result = Arrays.copyOfRange(original_text, 0, index);
            result_after = Arrays.copyOfRange(original_text, index + divide_byte.length, original_text.length);

//            System.out.println(new String(result));
//            System.out.println(new String(result_after));
            arr.add(result);
            original_text = result_after;
            index = strpos(original_text, divide_byte);
//            System.out.println(index);
//            System.out.println(new String(original_text));
        }
        if (result_after != null) {
            arr.add(result_after);
        }
        return arr;
    }

    private static int strpos(byte[] text, byte[] divide_bytes) {
        for (int i = 0; i < text.length; i++) {
            boolean have_divide = true;
            for (int j = 0; j < divide_bytes.length; j++) {
                byte db = divide_bytes[j];
                byte b = text[i + j];
                if (db != b) {
                    have_divide = false;
                    break;
                }
            }
            if (have_divide && divide_bytes.length != 0) {
                return i;
            }
        }
        return -1;
    }

    private static byte[] replace(byte[] bytes, byte[] search, byte[] replacement) {
        try {
            return ReplacingInputStream.replaceByteArray(bytes, search, replacement);
        } catch (Exception ex) {
            Logger.getLogger(ByteOperator.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
//
////        Arrays.copyOfRange(text, 1, 10);
//    public static byte[][] spilt(byte[] text, byte[] divide_bytes) {
//
//        int index = strpos(text, divide_bytes);
//        try {
//            strpos("".getBytes("UTF-8"), "".getBytes("UTF-8"));
//        } catch (UnsupportedEncodingException ex) {
//            Logger.getLogger(ByteOPerator.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        return null;
//    }
}

class ReplacingInputStream extends FilterInputStream {

    LinkedList<Integer> inQueue = new LinkedList<Integer>();
    LinkedList<Integer> outQueue = new LinkedList<Integer>();
    final byte[] search, replacement;

    protected ReplacingInputStream(InputStream in, byte[] search, byte[] replacement) {
        super(in);
        this.search = search;
        this.replacement = replacement;
    }

    private boolean isMatchFound() {
        Iterator<Integer> inIter = inQueue.iterator();
        for (int i = 0; i < search.length; i++) {
            if (!inIter.hasNext() || search[i] != inIter.next()) {
                return false;
            }
        }
        return true;
    }

    private void readAhead() throws IOException {
        // Work up some look-ahead.
        while (inQueue.size() < search.length) {
            int next = super.read();
            inQueue.offer(next);
            if (next == -1) {
                break;
            }
        }
    }

    @Override
    public int read() throws IOException {
        // Next byte already determined.
        if (outQueue.isEmpty()) {
            readAhead();

            if (isMatchFound()) {
                for (int i = 0; i < search.length; i++) {
                    inQueue.remove();
                }

                for (byte b : replacement) {
                    outQueue.offer((int) b);
                }
            } else {
                outQueue.add(inQueue.remove());
            }
        }

        return outQueue.remove();
    }

    // TODO: Override the other read methods.
    public static byte[] replaceByteArray(byte[] bytes, byte[] search, byte[] replacement) throws Exception {

        // byte[] bytes = "hello xyz world.".getBytes("UTF-8");
        // byte[] search = "xyz".getBytes("UTF-8");
        // byte[] replacement = "abc".getBytes("UTF-8");
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        InputStream ris = new ReplacingInputStream(bis, search, replacement);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        int b;
        while (-1 != (b = ris.read())) {
            bos.write(b);
        }
//        System.out.println(new String(bos.toByteArray()));
        return bos.toByteArray();

    }
}
