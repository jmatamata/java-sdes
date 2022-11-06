package project1;

import java.util.*;
import java.io.*;

public class SDESCrack {
	final static String outputFilePath = "../java-sdes/SDESCrackoutput.txt";


	public static void main(String[] args) {
		System.out.println(
				"          Raw Key           |          Plain Text           |           Cipher Text         ");
		System.out.println(
				"--------------------------------------------------------------------------------------------");
		System.out.println("        0111001101          |         CRYPTOGRAPHY          |       "
				+ SDESCrack.casciiStringEncypt("CRYPTOGRAPHY", "0111001101"));
		
		String msg1 = "1011011001111001001011101111110000111110100000000001110111010001111011111101101100010011000000101101011010101000101111100011101011010111100011101001010111101100101110000010010101110001110111011111010101010100001100011000011010101111011111010011110111001001011100101101001000011011111011000010010001011101100011011110000000110010111111010000011100011111111000010111010100001100001010011001010101010000110101101111111010010110001001000001111000000011110000011110110010010101010100001000011010000100011010101100000010111000000010101110100001000111010010010101110111010010111100011111010101111011101111000101001010001101100101100111001110111001100101100011111001100000110100001001100010000100011100000000001001010011101011100101000111011100010001111101011111100000010111110101010000000100110110111111000000111110111010100110000010110000111010001111000101011111101011101101010010100010111100011100000001010101110111111101101100101010011100111011110101011011";
		
		// Map containing all of the possible keys and Decrypted Cascii text
		HashMap<String, String> stringOutputs = SDESCrack.bruteDecryptString(msg1);

		// write the hashmap output to a file
		File file = new File(outputFilePath);
  
        BufferedWriter bf = null;

		try {
  
            // create new BufferedWriter for the output file
            bf = new BufferedWriter(new FileWriter(file));
  
            // iterate map entries
            for (Map.Entry<String, String> entry : stringOutputs.entrySet()) {
  
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
	
	// Takes in a cascii String and key to use in SDES encryption 
	public static ArrayList<Byte> casciiStringEncypt(String casciiString, String rawKey) {
		ArrayList<Byte> encryptedBits = new ArrayList<>();
		
		for(int i = 0; i < casciiString.length(); i++) {
			String tempPlainText = SDESCrack.charToPlainText(casciiString.charAt(i));
			Byte[] tempOutput = SDES.runSDESEncrypt(tempPlainText, rawKey);
			
			for(byte b : tempOutput)
				encryptedBits.add(b);
		}
		
		return encryptedBits;
	}
	
	public static HashMap<String, String> bruteDecryptString(String userCipherData) {
		// Converts large String to ArrayList full of Strings of length 8
		ArrayList<String> cipherTextList = SDESCrack.toCipherTextArrayList(userCipherData);
		HashMap<String, String> plainTextOutputs = new HashMap<>(1024);

		// Runs for all possible keys
		for (int i = 0; i < 1024; i++) {

			// ArrayList containing all decrypted bits
			ArrayList<Byte> decryptedBits = new ArrayList<>(userCipherData.length());

			// Makes new rawKey for each iteration
			String rawKey = SDESCrack.keyFromBinaryString(Integer.toBinaryString(i));

			// Decrypts all bits in cipherTextList
			for (String s : cipherTextList) {
				// Decrypts length of 8 String cipher text
				Byte[] tempDecrypt = SDES.runSDESDecrypt(s, rawKey);

				// adds decrypted integers to Integer ArrayList
				for (Byte b : tempDecrypt)
					decryptedBits.add(b);
			}

			// Converts all the bits in decryptedBits to a String
			String text = SDESCrack.casciiDecode(decryptedBits);

			// Adds key and output CASCII Decoded String to HashMap
			plainTextOutputs.put(rawKey, text);
		}

		return plainTextOutputs;
	}

	// Divides large input String into Strings length 8 then stores in ArrayList.
	public static ArrayList<String> toCipherTextArrayList(String s) {
		// ArrayList that will contain cipher text Strings
		// Instantiated with large capacity to avoid constant reallocation.
		ArrayList<String> cipherTextList = new ArrayList<>(2048);

		// Adds Strings of length 8 into ArrayList
		for (int i = 0; i < s.length(); i += 8)
			cipherTextList.add(s.substring(i, i + 8));

		return cipherTextList;
	}

	// Adds 0s to binary String that may be shorter than 10 bits.
	public static String keyFromBinaryString(String userBinaryString) {
		// Calculates how many 0s need to be appended to beginning of string
		int remainingBits = 10 - userBinaryString.length();
		// Returns inputed String if already contains 10 bits
		if (remainingBits <= 0)
			return userBinaryString;
		StringBuilder sb = new StringBuilder();

		// Appends 0s
		for (int i = 0; i < remainingBits; i++)
			sb.append("0");
		// Appends original binary String
		sb.append(userBinaryString);

		return sb.toString();
	}

	public static String casciiDecode(ArrayList<Byte> userDecodedBits) {
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < userDecodedBits.size(); i += 5) {

			if (i + 5 >= userDecodedBits.size())
				break;

			int tempValue = 0;

			for (int j = i; j < i + 5; j++) {
				// each bit over is a raising of the power of two
				// multiply it by the bit value - one or zero
				tempValue += Math.pow(2, j - i) * userDecodedBits.get(j);
			}

			sb.append(SDESCrack.valueToChar(tempValue));
		}
		return sb.toString();
	}

	// Converts an ArrayList of int type bits to a single String
	public static String bitsToString(ArrayList<Byte> bits) {
		StringBuilder sb = new StringBuilder();
		for (int i : bits)
			sb.append(i);
		return sb.toString();
	}
	
	public static String charToPlainText(char userChar) {
		// Make cascii userChar into binary String
		String binaryString = Integer.toBinaryString(SDESCrack.charToValue(userChar));
		
		// Calculates how many 0s need to be appended to beginning of string
		int remainingBits = 8 - binaryString.length();
		// Returns inputed String if already contains 8 bits
		if (remainingBits <= 0)
			return binaryString;
		
		StringBuilder sb = new StringBuilder();

		// Appends 0s
		for (int i = 0; i < remainingBits; i++)
			sb.append("0");
		// Appends original binary String
		sb.append(binaryString);

		return sb.toString();
	}

	public static int charToValue(char userChar) {
		switch (userChar) {
		case ' ':
			return 0;
		case 'A':
			return 1;
		case 'B':
			return 2;
		case 'C':
			return 3;

		case 'D':
			return 4;

		case 'E':
			return 5;

		case 'F':
			return 6;

		case 'G':
			return 6;

		case 'H':
			return 8;

		case 'I':
			return 8;

		case 'J':
			return 10;

		case 'K':
			return 11;

		case 'L':
			return 12;

		case 'M':
			return 13;

		case 'N':
			return 14;

		case 'O':
			return 15;

		case 'P':
			return 16;

		case 'Q':
			return 17;

		case 'R':
			return 18;

		case 'S':
			return 19;

		case 'T':
			return 20;

		case 'U':
			return 21;

		case 'V':
			return 22;

		case 'W':
			return 23;

		case 'X':
			return 24;

		case 'Y':
			return 25;

		case 'Z':
			return 26;
		case ',':
			return 27;
		case '?':
			return 28;
		case ':':
			return 29;
		case '.':
			return 30;
		case '\'':
			return 31;

		default:
			throw new java.lang.IllegalArgumentException("Argument not within Cascii");
		}
	}

	public static char valueToChar(int value) {
		switch (value) {
		case 0:
			return ' ';
		case 1:
			return 'A';
		case 2:
			return 'B';
		case 3:
			return 'C';
		case 4:
			return 'D';
		case 5:
			return 'E';
		case 6:
			return 'F';
		case 7:
			return 'G';
		case 8:
			return 'H';
		case 9:
			return 'I';
		case 10:
			return 'J';
		case 11:
			return 'K';
		case 12:
			return 'L';
		case 13:
			return 'M';
		case 14:
			return 'N';
		case 15:
			return 'O';
		case 16:
			return 'P';
		case 17:
			return 'Q';
		case 18:
			return 'R';
		case 19:
			return 'S';
		case 20:
			return 'T';
		case 21:
			return 'U';
		case 22:
			return 'V';
		case 23:
			return 'W';
		case 24:
			return 'X';
		case 25:
			return 'Y';
		case 26:
			return 'Z';
		case 27:
			return ',';
		case 28:
			return '?';
		case 29:
			return ':';
		case 30:
			return '.';
		case 31:
			return '\'';

		default:
			throw new java.lang.IllegalArgumentException("Argument must be be on interval [0, 31]");
		}
	}

}