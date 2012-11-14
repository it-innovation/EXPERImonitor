/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.open.kmi.analysis.core;

import java.util.Locale;

/**
 *
 * @author miriamfernandez
 */
public class Language {
    
    public static String ENGLISH = "en";
    public static String GERMAN  = "de";
    
    public static boolean isEnglish(String language){
        if(language.toLowerCase().equals(ENGLISH)){
            return true;
        }
        return false;
    }
    
     public static boolean isGerman(String language){
        if(language.toLowerCase().equals(GERMAN)){
            return true;
        }
        return false;
    }   
    
    
}
