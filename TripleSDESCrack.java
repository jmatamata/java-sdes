package project1;

import java.util.*;
import java.io.*;

public class TripleSDESCrack {

	final static String outputFilePath = "../java-sdes/output.txt";
	public static void main(String[] args) {
		String msg2 = "00011111100111111110011111101100111000000011001011110010101010110001011101001101000000110011010111111110000000001010111111000001010010111001111001010101100000110111100011111101011100100100010101000011001100101000000101111011000010011010111100010001001000100001111100100000001000000001101101000000001010111010000001000010011100101111001101111011001001010001100010100000";
        
        HashMap<ArrayList<String>, String> stringOutputs = TripleSDESCrack.bruteDecryptString(msg2);

		// write the hashmap output to a file
		File file = new File(outputFilePath);
  
        BufferedWriter bf = null;

		try {
  
            // create new BufferedWriter for the output file
            bf = new BufferedWriter(new FileWriter(file));
  
            // iterate map entries
            for (Map.Entry<ArrayList<String>, String> entry : stringOutputs.entrySet()) {
  
                // put key and value separated by a colon
                bf.write(entry.getKey() + ":" + entry.getValue());
  
                // new line
                bf.newLine();
            }
  
            bf.flush();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
  
            try {
  
                // always close the writer
                bf.close();
            }
            catch (Exception e) {
            }
        }
		// delete above if no longer necessary
	}
	
    public static HashMap<ArrayList<String>, String> bruteDecryptString(String userCipherData) {
    	// Converts large String to ArrayList full of Strings of length 8
		//ArrayList<String> cipherTextList = SDESCrack.toCipherTextArrayList(userCipherData);
		ArrayList<String> cipherTextList = SDESCrack.toCipherTextArrayList(userCipherData);
		HashMap<ArrayList<String>, String> plainTextOutputs = new HashMap<>();

		// Runs for all possible keys
		for (int i = 0; i < 1024; i++) {
			for(int j = 0; j < 1024; j++) {
				
				// ArrayList containing all decrypted bits
				ArrayList<Byte> decryptedBits = new ArrayList<>();

				// Makes new rawKey for each i iteration
				String rawKey = SDESCrack.keyFromBinaryString(Integer.toBinaryString(i));
				
				// Makes new rawKey for each j iteration
				String rawKey2 = SDESCrack.keyFromBinaryString(Integer.toBinaryString(j));

				// Decrypts all bits in cipherTextList
				for (String s : cipherTextList) {
					// Decrypts length of 8 String cipher text
					Byte[] tempDecrypt = TripleSDES.tripleDesDecryption(s, rawKey, rawKey2);

					// adds decrypted integers to Integer ArrayList
					for (Byte b : tempDecrypt)
						decryptedBits.add(b);
				}

				// Converts all the bits in decryptedBits to a String
				String text = SDESCrack.casciiDecode(decryptedBits);
				
				ArrayList<String> keysAtValue = new ArrayList<>();
				keysAtValue.add(rawKey);
				keysAtValue.add(rawKey2);

				// Adds key and output CASCII Decoded String to HashMap
				plainTextOutputs.put(keysAtValue, text);
			}
		}
		
		return plainTextOutputs;
    }

	public static ArrayList<Byte> toByteArray(String userCipherData){
		ArrayList<Byte> vals = new ArrayList<>();
		for(int i = 0; i < userCipherData.length(); i++){
			vals.add((byte) Integer.parseInt(Character.toString(userCipherData.charAt(i))));
		}

		return vals;
	}

}