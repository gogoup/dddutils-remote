package org.gogoup.dddutils.misc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

public class Helper {
    
    /**
     * Read json message from file.
     * 
     * NOTE: This method is not prefered to read large size files, it would cause out of memory exception.
     * 
     * @param path String
     * @return String json message.
     */
    public static String readFileAsJson(String path) {
        
        return readFileAsJson(new File(path));
        
    }
    
    public static String readFileAsJson(File jsonFile) {
        
        if (!jsonFile.exists())
            throw new RuntimeException(jsonFile.getAbsolutePath() + " does not exist!");
        
        StringBuilder json = new StringBuilder();
        BufferedReader fileReader = null;
        
        try {
            fileReader = new BufferedReader(new InputStreamReader(new FileInputStream(jsonFile), "UTF-8"));
            
            String line = null;
            while ((line = fileReader.readLine()) != null) {
                json.append(line);
            }
            
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            
            try {
                if (null != fileReader)
                    fileReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        return json.toString();
    }
    
}
