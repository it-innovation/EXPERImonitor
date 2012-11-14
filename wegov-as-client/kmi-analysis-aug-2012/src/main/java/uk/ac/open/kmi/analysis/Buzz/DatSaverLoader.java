package uk.ac.open.kmi.analysis.Buzz;

import java.io.*;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import weka.classifiers.Classifier;

/**
 * User: Matt Date: Jul 27, 2010
 */
public class DatSaverLoader {

    public static void saveArray(String filename, HashMap<Integer, String> results) {
        try {
            FileOutputStream fos = new FileOutputStream(filename);
            GZIPOutputStream gzos = new GZIPOutputStream(fos);
            ObjectOutputStream out = new ObjectOutputStream(gzos);
            out.writeObject(results);
            out.flush();
            out.close();
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    public static HashMap<Integer, String> loadArray(String filename) {
        try {
            FileInputStream fis = new FileInputStream(filename);
            GZIPInputStream gzis = new GZIPInputStream(fis);
            ObjectInputStream in = new ObjectInputStream(gzis);
            HashMap<Integer, String> results = (HashMap<Integer, String>) in.readObject();
            in.close();
            return results;
        } catch (Exception e) {
            System.out.println(e);
        }
        return null;
    }

    public static void saveClassifier(String filename, Classifier results) {
        try {
            FileOutputStream fos = new FileOutputStream(filename);
            GZIPOutputStream gzos = new GZIPOutputStream(fos);
            ObjectOutputStream out = new ObjectOutputStream(gzos);
            out.writeObject(results);
            out.flush();
            out.close();
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    public static Classifier loadClassifier(String filename) {
        try {
            FileInputStream fis = new FileInputStream(filename);
            GZIPInputStream gzis = new GZIPInputStream(fis);
            ObjectInputStream in = new ObjectInputStream(gzis);
            Classifier results = (Classifier) in.readObject();
            in.close();
            return results;
        } catch (Exception e) {
            System.out.println(e);
        }
        return null;
    }
}
