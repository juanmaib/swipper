/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.globant.labs.swipper.utils;

import java.util.Random;

/**
 *
 * @author justomiguel
 */
public class Generador {
    
    static Random rnd = new Random();
    
    public static int generarNumeroAleatorio(int desde, int hasta){
        return rnd.nextInt(hasta-desde+1)+desde;
    }
    
    public static int generarDNIAleatorio(){
        return generarNumeroAleatorio(30100500, 40200323);
    }
    
}
