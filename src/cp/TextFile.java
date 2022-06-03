/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cp;

import java.nio.file.Path;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author sebastian
 */
public class TextFile implements  Result{
    private final Path path;
    private int number;
    
    public Path path(){
        return(path);
    }
    
    TextFile(Path dir){
        path=dir;
        number=0;
        try {
            BufferedReader reader = Files.newBufferedReader( dir );

            String[] numbers = reader.readLine().split(",");
            for(String num: numbers){
                if (number<Integer.parseInt(num)){
                    number=Integer.parseInt(num);
                }
            }

        } catch (IOException ex) {
            System.out.println("mig");
        }
       }
    
    public int number(){
        return(number);
    }


}
