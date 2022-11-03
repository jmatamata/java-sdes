package project1;

import java.util.*;

/*
NOTE: You must have this file and SDES.java under a folder named project1 to run the project!

this program implements SDES but 3 times with 2 different keys

The order of key usage to encrypt is: 
    DES cipher with key 1
    DES reverse cipher with key 2
    DES cipher with key 1

The order of key usage to decrypt is: 
    DES reverse cipher with key 1
    DES cipher with key 2
    DES reverse cipher with key 1
*/

public class TripleSDES {
    //public static SDES sdes = new SDES();
    public static void main(String[] args){
        // control the plain text here (must be 8 bits)
        String[] plainText = {"00000000", "11010111", "10101010", "10101010"};

        // control the keys below (must be 10 bits)
        String[] rawKey1 = {"0000000000", "1000101110", "1000101110","1111111111", "1000101110", "1011101111", "0000000000", "1111111111"};
        String[] rawKey2 = {"0000000000", "0110101110", "0110101110", "1111111111", "0110101110", "0110101110", "0000000000", "1111111111"}; // aka "raw key" under assignment description

        // variable to track which key index we want
        int currentKeyIndex = 0;

        System.out.println("          Raw Key 1          |          Raw Key 2          |          Plain Text           |           Cipher Text         ");
        System.out.println("--------------------------------------------------------------------------------------------------------------------------");
        for(int i = 0; i<plainText.length; i++){
            // print out the output of the sdes function given a plaintext
            System.out.println("         "+rawKey1[currentKeyIndex]+"          |         "+rawKey2[currentKeyIndex]+"          |           "+plainText[i]+"            |       "+Arrays.toString( tripleDesEncryption(plainText[i],rawKey1[currentKeyIndex],rawKey2[currentKeyIndex]) ));
            System.out.println("                             |                             |                               |                        ");
            currentKeyIndex++;
        }

        /*
            This is for decryption
        */
        String[] cipherText = {"11100110", "01010000", "10000000", "10010010"};

        // second part of SDES text, we are given the ciphertext and key
        for(int i = 0; i<cipherText.length; i++){
            System.out.println("         "+rawKey1[currentKeyIndex]+"          |         "+rawKey2[currentKeyIndex]+"          |   "+Arrays.toString( tripleDesDecryption(cipherText[i],rawKey1[currentKeyIndex],rawKey2[currentKeyIndex]) )+"    |                "+cipherText[i]);
            System.out.println("                             |                             |                               |                        ");   
            currentKeyIndex++;
        }
        
    }

    // method to run sdes 
    public static int[] tripleDesEncryption(String plainText, String rawKey1, String rawKey2){
        // use a string to hold int[] values from helper method
        String solutionString = "";

        int[] solution = new int[8];

        //DES cipher with key 1 and plain text provided
        solution = SDES.runSDESEncrypt(plainText, rawKey1);
        solutionString = getArrayValue(solution);
        
        // DES reverse-cipher (DES decrypt) with key 2 and encrypted text above
        solution = SDES.runSDESDecrypt(solutionString, rawKey2);
        solutionString = getArrayValue(solution);

        // DES cipher with key 1 and encrypted text above
        solution = SDES.runSDESEncrypt(solutionString, rawKey1);

        // return the array after last step of triple SDES
        return solution;
    }

    public static int[] tripleDesDecryption(String plainText, String rawKey1, String rawKey2){
        // use a string to hold int[] values from helper method
        String solutionString = "";

        int[] solution = new int[8];

        // DES reverse-cipher (DES decrypt) with key 1 and plain text provided
        solution = SDES.runSDESDecrypt(plainText, rawKey1);
        solutionString = getArrayValue(solution);

        // DES cipher with key 2 and encrypted text above
        solution = SDES.runSDESEncrypt(solutionString, rawKey2);
        solutionString = getArrayValue(solution);

        // DES reverse-cipher (DES decrypt) with key 1 and encrypted text above
        solution = SDES.runSDESDecrypt(solutionString, rawKey1);

        // return the array after last step of triple SDES
        return solution;
    }

    // helper method that adds all int values in an array of ints to a string
    public static String getArrayValue(int[] arr){
        String solution = "";

        // loop through all values and add to string solution
        for(Integer curr:arr){
            solution+=Integer.toString(curr);
        }

        return solution;
    }
    
}