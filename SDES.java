package project1;

import java.util.*;

public class SDES {
	// definitions of S-boxes that we will use in the program
	public static int[][] S_0 = { { 1, 3, 0, 2 }, { 3, 2, 1, 0 }, { 0, 2, 1, 3 }, { 3, 1, 3, 2 } };
	public static int[][] S_1 = { { 0, 1, 2, 3 }, { 2, 0, 1, 3 }, { 3, 0, 1, 0 }, { 2, 1, 0, 3 } };

	// arrays to define our steps of our permutations (1 less to avoid out of bounds
	// exception)
	static int[] indicesForInitPerm = { 1, 5, 2, 0, 3, 7, 4, 6 }; // 2,6,3,1,4,8,5,7
	static int[] indicesForExtendedPerm = { 3, 0, 1, 2, 1, 2, 3, 0 }; // 4,1,2,3,2,3,4,1
	static int[] indicesForInverseInitPerm = { 3, 0, 2, 4, 6, 1, 7, 5 }; // 4,1,3,5,7,2,8,6

	static int[] indicesFor4BitPerm = { 1, 3, 2, 0 }; // 2,4,3,1
	static int[] indicesFor10BitPerm = { 2, 4, 1, 6, 3, 9, 0, 8, 7, 5 }; // 3,5,2,7,4,10,1,9,8,6
	static int[] indicesFor8BitPerm = { 5, 2, 6, 3, 7, 4, 9, 8 }; // 6,3,7,4,8,5,x,9

	// instantiate 2 keys to be created from given key
	static int[] key1 = new int[8];
	static int[] key2 = new int[8];

	// main function to execute our program
	public static void main(String[] args) {
		// first 4 values are from provided examples, second 4 are for unknown values
		// given the cipherText
		// control the plain text here (must be 8 bits)
		String[] plainText = { "00000000", "11111111", "00000000","11111111" };

		// control the key below (must be 10 bits)
		String[] rawKey = { "0000000000", "1111111111","0000011111", "0000011111" };

		System.out.println(
				"          Raw Key           |          Plain Text           |           Cipher Text         ");
		System.out.println(
				"--------------------------------------------------------------------------------------------");
		for (int i = 0; i < plainText.length; i++) {
			// print out the output of the sdes function given a plaintext
			System.out.println("        " + rawKey[i] + "          |           " + plainText[i] + "            |       "
					+ Arrays.toString(runSDESEncrypt(plainText[i], rawKey[i])));
			System.out.println("                            |                               |                        ");
		}

		/*
		 * This is for decryption
		 */
		String[] cipherText = { "00011100", "11000010", "10011101", "10010000" };
		String[] key = { "1000101110", "1000101110", "0010011111", "0010011111" };

		// second part of SDES text, we are given the ciphertext and key
		for (int i = 0; i < cipherText.length; i++) {
			System.out.println("        " + rawKey[i] + "          |   "
					+ Arrays.toString(runSDESDecrypt(cipherText[i], key[i])) + "    |                " + cipherText[i]);
			System.out.println("                            |                               |                        ");
		}

		System.out.println("\n");
		System.out.println(
				"          Raw Key           |          Plain Text           |           Cipher Text         ");
		System.out.println(
				"--------------------------------------------------------------------------------------------");
		System.out.println("        0111001101          |         CRYPTOGRAPHY          |       "
				+ SDES.casciiStringEncypt("CRYPTOGRAPHY", "0111001101"));
		
		String msg1 = "1011011001111001001011101111110000111110100000000001110111010001111011111101101100010011000000101101011010101000101111100011101011010111100011101001010111101100101110000010010101110001110111011111010101010100001100011000011010101111011111010011110111001001011100101101001000011011111011000010010001011101100011011110000000110010111111010000011100011111111000010111010100001100001010011001010101010000110101101111111010010110001001000001111000000011110000011110110010010101010100001000011010000100011010101100000010111000000010101110100001000111010010010101110111010010111100011111010101111011101111000101001010001101100101100111001110111001100101100011111001100000110100001001100010000100011100000000001001010011101011100101000111011100010001111101011111100000010111110101010000000100110110111111000000111110111010100110000010110000111010001111000101011111101011101101010010100010111100011100000001010101110111111101101100101010011100111011110101011011";
		
		HashMap<String, String> stringOutputs = SDES.bruteDecryptString(msg1);
	}

	/*
	 * Each SDES execution needs 4 methods: Initial permutation (IP) Complex
	 * function - Expanded Permutation (EP) (takes 4 bit input and converts to 8 bit
	 * output) - S Boxes (performs substitution) - Permutation (P4) Switch (SW)
	 * Reverse of initial permutation
	 */
	// helper method to run all commands needed for sdes
	public static int[] runSDESEncrypt(String plainText, String rawKey) {
		int[] solution = new int[8];

		// generate the 2 keys to use in the program
		generateKeys(rawKey);

		solution = encryptText(plainText);

		return solution;
	}

	// method to run sdes decrypt given ciphertext and key
	public static int[] runSDESDecrypt(String cipherText, String rawKey) {
		int[] solution = new int[8];

		// generate the 2 keys to use in the program
		generateKeys(rawKey);

		solution = decryptText(cipherText);

		return solution;
	}

	/*
	 * The below functions are used to encrypt a plain text to cipher text
	 */
	public static int[] encryptText(String plainText) {
		int[] solution = new int[8];

		// convert the raw key from string to an array
		List<Integer> textArr = new ArrayList<>();

		// convert string value to int and add to the array
		for (int i = 0; i < plainText.length(); i++) {
			textArr.add(Integer.parseInt(Character.toString(plainText.charAt(i))));
		}

		// array that holds 8 bit values to work with
		int[] currentText = new int[8];

		// perform the initial permutation on plainText
		for (int i = 0; i < currentText.length; i++) {
			currentText[i] = textArr.get(indicesForInitPerm[i]);
		}

		// split into left and right arrays (4 bits each)
		int[] leftAfterPermutation = new int[4];
		System.arraycopy(currentText, 0, leftAfterPermutation, 0, 4);
		int[] rightAfterPermutation = new int[4];
		System.arraycopy(currentText, 4, rightAfterPermutation, 0, 4);

		// run steps in between and return array with 2 arrays
		int[][] returnedValues = intermidiateSteps(leftAfterPermutation, rightAfterPermutation, key1);

		// make leftAfterPermutation equal rightAfterPermutation and make
		// rightAfterPermutation equal to second xor output
		System.arraycopy(returnedValues[1], 0, leftAfterPermutation, 0, 4); // set the new left
		System.arraycopy(returnedValues[0], 0, rightAfterPermutation, 0, 4); // set the new right

		// run steps in between and return array with 2 arrays
		returnedValues = intermidiateSteps(leftAfterPermutation, rightAfterPermutation, key2);

		// assign values to use below
		int[] secondXorOutput = returnedValues[0];
		rightAfterPermutation = returnedValues[1];

		// merge the current rightAfterPermutation with the secondXorOutput
		int[] preliminarySolution = new int[8];

		int innerIndex = 0;
		for (int i = 0; i < 4; i++) {
			preliminarySolution[i] = secondXorOutput[innerIndex];
			preliminarySolution[i + 4] = rightAfterPermutation[innerIndex];
			innerIndex++;
		}

		// perform inverse of initial permutation of preliminary solution
		for (int i = 0; i < solution.length; i++) {
			solution[i] = preliminarySolution[indicesForInverseInitPerm[i]];
		}

		return solution;
	}

	/*
	 * The below functions are used to encrypt a plain text to cipher text Literally
	 * the exact same but we use key2 first followed by key1 and we take in a
	 * ciphertext instead
	 */
	public static int[] decryptText(String cipherText) {
		int[] solution = new int[8];

		// convert the raw key from string to an array
		List<Integer> textArr = new ArrayList<>();

		// convert string value to int and add to the array
		for (int i = 0; i < cipherText.length(); i++) {
			textArr.add(Integer.parseInt(Character.toString(cipherText.charAt(i))));
		}

		// array that holds 8 bit values to work with
		int[] currentText = new int[8];

		// perform the initial permutation on cipherText
		for (int i = 0; i < currentText.length; i++) {
			currentText[i] = textArr.get(indicesForInitPerm[i]);
		}

		// split into left and right arrays (4 bits each)
		int[] leftAfterPermutation = new int[4];
		System.arraycopy(currentText, 0, leftAfterPermutation, 0, 4);
		int[] rightAfterPermutation = new int[4];
		System.arraycopy(currentText, 4, rightAfterPermutation, 0, 4);

		// run steps in between and return array with 2 arrays
		int[][] returnedValues = intermidiateSteps(leftAfterPermutation, rightAfterPermutation, key2);

		// make leftAfterPermutation equal rightAfterPermutation and make
		// rightAfterPermutation equal to second xor output
		System.arraycopy(returnedValues[1], 0, leftAfterPermutation, 0, 4); // set the new left
		System.arraycopy(returnedValues[0], 0, rightAfterPermutation, 0, 4); // set the new right

		// run steps in between and return array with 2 arrays
		returnedValues = intermidiateSteps(leftAfterPermutation, rightAfterPermutation, key1);

		// assign values to use below
		int[] secondXorOutput = returnedValues[0];
		rightAfterPermutation = returnedValues[1];

		// merge the current rightAfterPermutation with the secondXorOutput
		int[] preliminarySolution = new int[8];

		int innerIndex = 0;
		for (int i = 0; i < 4; i++) {
			preliminarySolution[i] = secondXorOutput[innerIndex];
			preliminarySolution[i + 4] = rightAfterPermutation[innerIndex];
			innerIndex++;
		}

		// perform inverse of initial permutation of preliminary solution
		for (int i = 0; i < solution.length; i++) {
			solution[i] = preliminarySolution[indicesForInverseInitPerm[i]];
		}

		return solution;
	}

	// intermediateSteps eliminates repetitive code
	public static int[][] intermidiateSteps(int[] leftSide, int[] rightSide, int[] keyToUse) {
		// array that will hold right side expansion and permutation
		int[] rightExpansion = new int[8];

		// expand and permutate the right side
		for (int i = 0; i < rightExpansion.length; i++) {
			rightExpansion[i] = rightSide[indicesForExtendedPerm[i]];
		}

		// array to store the value of the xor operation
		int[] xorOutput = new int[8];

		// perform xor operation with expanded permutation of right side and keyToUse
		for (int i = 0; i < xorOutput.length; i++) {
			xorOutput[i] = (rightExpansion[i] ^ keyToUse[i]);
		}

		// split the xor output into 2 4-bit arrays
		int[] left = new int[4];
		System.arraycopy(xorOutput, 0, left, 0, 4);
		int[] right = new int[4];
		System.arraycopy(xorOutput, 4, right, 0, 4);

		// take 1st and 4th bits for row, 2nd and 3rd for col
		// for left side
		String rowBinary = Integer.toString(left[0]) + Integer.toString(left[3]);
		int leftRow = Integer.parseInt(rowBinary, 2);
		String colBinary = Integer.toString(left[1]) + Integer.toString(left[2]);
		int leftCol = Integer.parseInt(colBinary, 2);

		// for right side
		rowBinary = Integer.toString(right[0]) + Integer.toString(right[3]);
		int rightRow = Integer.parseInt(rowBinary, 2);
		colBinary = Integer.toString(right[1]) + Integer.toString(right[2]);
		int rightCol = Integer.parseInt(colBinary, 2);

		// get the (row,col) value from the corresponding S Box
		int leftSBoxValue = S_0[leftRow][leftCol]; // get S Box value for the left from S_0
		String leftBinaryValue = Integer.toBinaryString(leftSBoxValue);
		leftBinaryValue = String.format("%2s", leftBinaryValue).replaceAll(" ", "0"); // adds 0's to make sure we have 2
																						// bit number

		int rightSBoxValue = S_1[rightRow][rightCol]; // get S Box value for the right from S_1
		String rightBinaryValue = Integer.toBinaryString(rightSBoxValue);
		rightBinaryValue = String.format("%2s", rightBinaryValue).replaceAll(" ", "0"); // adds 0's to make sure we have
																						// 2 bit number

		// array to combine the binary values above
		int[] combinationOfBits = new int[4];

		// merge bits above to combinationOfBits
		int firstHalf = 0;
		int secondHalf = 2;
		for (int i = 0; i < leftBinaryValue.length(); i++) {
			combinationOfBits[firstHalf] = Integer.parseInt(Character.toString(leftBinaryValue.charAt(i)));
			combinationOfBits[secondHalf] = Integer.parseInt(Character.toString(rightBinaryValue.charAt(i)));
			firstHalf++;
			secondHalf++;
		}

		// permutate combinationOfBits (P4)
		int[] abovePermutation = new int[4];
		// perform the initial permutation on plainText
		for (int i = 0; i < abovePermutation.length; i++) {
			abovePermutation[i] = combinationOfBits[indicesFor4BitPerm[i]];
		}

		// array to store the second xor output
		int[] secondXorOutput = new int[4];

		// xor abovePermuation with leftAfterPermutation
		for (int i = 0; i < secondXorOutput.length; i++) {
			secondXorOutput[i] = (abovePermutation[i] ^ leftSide[i]);
		}

		// return the secondXorOutput and rightSide (in that order)
		int[][] returnValue = { secondXorOutput, rightSide };
		return returnValue;
	}

	/*
	 * The below functions are used to help generate key 1 and key 2 for SDES
	 * algorithm
	 */
	// helper method to generate 2 keys from raw key
	public static void generateKeys(String rawKey) {
		// convert the raw key from string to an array
		List<Integer> keyArr = new ArrayList<>();

		// convert string value to int and add to the array
		for (int i = 0; i < rawKey.length(); i++) {
			keyArr.add(Integer.parseInt(Character.toString(rawKey.charAt(i))));
		}

		// permutate the current raw key with 10 bit permutation
		int[] currentKey = new int[10];
		for (int i = 0; i < currentKey.length; i++) {
			currentKey[i] = keyArr.get(indicesFor10BitPerm[i]);
		}

		// copy the values of the current key and split into 2 5 bit arrays
		int[] left = new int[5];
		System.arraycopy(currentKey, 0, left, 0, 5);
		int[] right = new int[5];
		System.arraycopy(currentKey, 5, right, 0, 5);

		// run left circular shift by 1 bit on left and right side
		moveLeftByNBits(left, 1);
		moveLeftByNBits(right, 1);

		// pointers to track the left index and right index to add values to
		int leftIndex = 0;
		int rightIndex = 5;

		// merge the two halfs and set to 'currentKey'
		for (int i = 0; i < 5; i++) {
			currentKey[leftIndex] = left[i];
			currentKey[rightIndex] = right[i];

			// move index by 1 space
			leftIndex++;
			rightIndex++;
		}

		// permutate 'currentKey' to get the 8-bit key 1
		for (int i = 0; i < key1.length; i++) {
			key1[i] = currentKey[indicesFor8BitPerm[i]];
		}

		// get 'currentKey' split into two halves
		left = new int[5];
		System.arraycopy(currentKey, 0, left, 0, 5); // left side
		right = new int[5];
		System.arraycopy(currentKey, 5, right, 0, 5); // right side

		// do a left circular shift by 2 bits on both halves
		moveLeftByNBits(left, 2);
		moveLeftByNBits(right, 2);

		// pointers to track the left index and right index to add values to
		leftIndex = 0;
		rightIndex = 5;

		// merge the two halfs and set to 'currentKey'
		for (int i = 0; i < 5; i++) {
			currentKey[leftIndex] = left[i];
			currentKey[rightIndex] = right[i];

			// move index by 1 space
			leftIndex++;
			rightIndex++;
		}

		// permutate 'currentKey' to get the 8-bit key 2
		for (int i = 0; i < key1.length; i++) {
			key2[i] = currentKey[indicesFor8BitPerm[i]];
		}
		// At this point, both keys have been created for the SDES algorithm to work
	}

	// method to move values in array by n-bits
	public static void moveLeftByNBits(int[] arr, int bits) {
		// create an array of the number of bits that we want to shift
		int[] remainder = new int[bits];

		// loop until we have n bits we want to shift
		for (int i = 0; i < bits; i++) {
			remainder[i] = arr[i];
		}

		// shift all values to the left in parameter array
		for (int i = 0; i < arr.length - bits; i++) {
			arr[i] = arr[i + bits];
		}

		// add the remaining elements to the end of the parameter array
		int lastIndex = arr.length - 1;
		for (int i = remainder.length - 1; i >= 0; i--) {
			arr[lastIndex] = remainder[i];
			lastIndex--;
		}
	}

	public static ArrayList<Integer> casciiStringEncypt(String casciiString, String rawKey) {
		ArrayList<Integer> encryptedBits = new ArrayList<>();
		
		for(int i = 0; i < casciiString.length(); i++) {
			String tempPlainText = SDES.charToPlainText(casciiString.charAt(i));
			int[] tempOutput = SDES.runSDESEncrypt(tempPlainText, rawKey);
			
			for(int b : tempOutput)
				encryptedBits.add(b);
		}
		
		return encryptedBits;
	}
	
	public static HashMap<String, String> bruteDecryptString(String userCipherData) {
		// Converts large String to ArrayList full of Strings of length 8
		ArrayList<String> cipherTextList = SDES.toCipherTextArrayList(userCipherData);
		HashMap<String, String> plainTextOutputs = new HashMap<>(1024);

		// Runs for all possible keys
		for (int i = 0; i < 1024; i++) {

			// ArrayList containing all decrypted bits
			ArrayList<Integer> decryptedBits = new ArrayList<>(userCipherData.length());

			// Makes new rawKey for each iteration
			String rawKey = SDES.keyFromBinaryString(Integer.toBinaryString(i));

			// Decrypts all bits in cipherTextList
			for (String s : cipherTextList) {
				// Decrypts length of 8 String cipher text
				int[] tempDecrypt = SDES.runSDESDecrypt(s, rawKey);

				// adds decrypted integers to Integer ArrayList
				for (int b : tempDecrypt)
					decryptedBits.add(b);
			}

			// Converts all the bits in decryptedBits to a String
			String text = SDES.casciiDecode(decryptedBits);

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

	public static String casciiDecode(ArrayList<Integer> userDecodedBits) {
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

			sb.append(SDES.valueToChar(tempValue));
		}
		return sb.toString();
	}

	// Converts an ArrayList of int type bits to a single String
	public static String bitsToString(ArrayList<Integer> bits) {
		StringBuilder sb = new StringBuilder();
		for (int i : bits)
			sb.append(i);
		return sb.toString();
	}
	
	public static String charToPlainText(char userChar) {
		// Make cascii userChar into binary String
		String binaryString = Integer.toBinaryString(SDES.charToValue(userChar));
		
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