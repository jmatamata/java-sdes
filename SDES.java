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
	static Byte[] key1 = new Byte[8];
	static Byte[] key2 = new Byte[8];

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

	}

	/*
	 * Each SDES execution needs 4 methods: 
	 * Initial permutation (IP) 
	 * Complex function 
	 * 	- Expanded Permutation (EP) (takes 4 bit input and converts to 8 bit output) 
	 * 	- S Boxes (performs substitution) 
	 * 	- Permutation (P4) 
	 * Switch (SW) Reverse of initial permutation
	 */
	// helper method to run all commands needed for sdes
	public static Byte[] runSDESEncrypt(String plainText, String rawKey) {
		Byte[] solution = new Byte[8];

		// generate the 2 keys to use in the program
		generateKeys(rawKey);

		solution = encryptText(plainText);

		return solution;
	}

	// method to run sdes decrypt given ciphertext and key
	public static Byte[] runSDESDecrypt(String cipherText, String rawKey) {
		Byte[] solution = new Byte[8];

		// generate the 2 keys to use in the program
		generateKeys(rawKey);

		solution = decryptText(cipherText);

		return solution;
	}

	/*
	 * The below functions are used to encrypt a plain text to cipher text
	 */
	public static Byte[] encryptText(String plainText) {
		Byte[] solution = new Byte[8];

		// convert the raw key from string to an array
		List<Byte> textArr = new ArrayList<>();

		// convert string value to int and add to the array
		for (int i = 0; i < plainText.length(); i++) {
			textArr.add( (byte) Integer.parseInt(Character.toString(plainText.charAt(i))));
		}

		// array that holds 8 bit values to work with
		Byte[] currentText = new Byte[8];

		// perform the initial permutation on plainText
		for (int i = 0; i < currentText.length; i++) {
			currentText[i] = textArr.get(indicesForInitPerm[i]);
		}

		// split into left and right arrays (4 bits each)
		Byte[] leftAfterPermutation = new Byte[4];
		System.arraycopy(currentText, 0, leftAfterPermutation, 0, 4);
		Byte[] rightAfterPermutation = new Byte[4];
		System.arraycopy(currentText, 4, rightAfterPermutation, 0, 4);

		// run steps in between and return array with 2 arrays
		Byte[][] returnedValues = intermidiateSteps(leftAfterPermutation, rightAfterPermutation, key1);

		// make leftAfterPermutation equal rightAfterPermutation and make
		// rightAfterPermutation equal to second xor output
		System.arraycopy(returnedValues[1], 0, leftAfterPermutation, 0, 4); // set the new left
		System.arraycopy(returnedValues[0], 0, rightAfterPermutation, 0, 4); // set the new right

		// run steps in between and return array with 2 arrays
		returnedValues = intermidiateSteps(leftAfterPermutation, rightAfterPermutation, key2);

		// assign values to use below
		Byte[] secondXorOutput = returnedValues[0];
		rightAfterPermutation = returnedValues[1];

		// merge the current rightAfterPermutation with the secondXorOutput
		Byte[] preliminarySolution = new Byte[8];

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
	public static Byte[] decryptText(String cipherText) {
		Byte[] solution = new Byte[8];

		// convert the raw key from string to an array
		List<Byte> textArr = new ArrayList<>();

		// convert string value to int and add to the array
		for (int i = 0; i < cipherText.length(); i++) {
			textArr.add((byte) Integer.parseInt(Character.toString(cipherText.charAt(i))));
		}

		// array that holds 8 bit values to work with
		Byte[] currentText = new Byte[8];

		// perform the initial permutation on cipherText
		for (int i = 0; i < currentText.length; i++) {
			currentText[i] = textArr.get(indicesForInitPerm[i]);
		}

		// split into left and right arrays (4 bits each)
		Byte[] leftAfterPermutation = new Byte[4];
		System.arraycopy(currentText, 0, leftAfterPermutation, 0, 4);
		Byte[] rightAfterPermutation = new Byte[4];
		System.arraycopy(currentText, 4, rightAfterPermutation, 0, 4);

		// run steps in between and return array with 2 arrays
		Byte[][] returnedValues = intermidiateSteps(leftAfterPermutation, rightAfterPermutation, key2);

		// make leftAfterPermutation equal rightAfterPermutation and make
		// rightAfterPermutation equal to second xor output
		System.arraycopy(returnedValues[1], 0, leftAfterPermutation, 0, 4); // set the new left
		System.arraycopy(returnedValues[0], 0, rightAfterPermutation, 0, 4); // set the new right

		// run steps in between and return array with 2 arrays
		returnedValues = intermidiateSteps(leftAfterPermutation, rightAfterPermutation, key1);

		// assign values to use below
		Byte[] secondXorOutput = returnedValues[0];
		rightAfterPermutation = returnedValues[1];

		// merge the current rightAfterPermutation with the secondXorOutput
		Byte[] preliminarySolution = new Byte[8];

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
	public static Byte[][] intermidiateSteps(Byte[] leftSide, Byte[] rightSide, Byte[] keyToUse) {
		// array that will hold right side expansion and permutation
		Byte[] rightExpansion = new Byte[8];

		// expand and permutate the right side
		for (int i = 0; i < rightExpansion.length; i++) {
			rightExpansion[i] = rightSide[indicesForExtendedPerm[i]];
		}

		// array to store the value of the xor operation
		Byte[] xorOutput = new Byte[8];

		// perform xor operation with expanded permutation of right side and keyToUse
		for (int i = 0; i < xorOutput.length; i++) {
			xorOutput[i] = (byte) (rightExpansion[i] ^ keyToUse[i]);
		}

		// split the xor output into 2 4-bit arrays
		Byte[] left = new Byte[4];
		System.arraycopy(xorOutput, 0, left, 0, 4);
		Byte[] right = new Byte[4];
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
		Byte[] combinationOfBits = new Byte[4];

		// merge bits above to combinationOfBits
		int firstHalf = 0;
		int secondHalf = 2;
		for (int i = 0; i < leftBinaryValue.length(); i++) {
			combinationOfBits[firstHalf] = (byte) Integer.parseInt(Character.toString(leftBinaryValue.charAt(i)));
			combinationOfBits[secondHalf] = (byte) Integer.parseInt(Character.toString(rightBinaryValue.charAt(i)));
			firstHalf++;
			secondHalf++;
		}

		// permutate combinationOfBits (P4)
		Byte[] abovePermutation = new Byte[4];
		// perform the initial permutation on plainText
		for (int i = 0; i < abovePermutation.length; i++) {
			abovePermutation[i] = combinationOfBits[indicesFor4BitPerm[i]];
		}

		// array to store the second xor output
		Byte[] secondXorOutput = new Byte[4];

		// xor abovePermuation with leftAfterPermutation
		for (int i = 0; i < secondXorOutput.length; i++) {
			secondXorOutput[i] = (byte) (abovePermutation[i] ^ leftSide[i]);
		}

		// return the secondXorOutput and rightSide (in that order)
		Byte[][] returnValue = { secondXorOutput, rightSide };
		return returnValue;
	}

	/*
	 * The below functions are used to help generate key 1 and key 2 for SDES
	 * algorithm
	 */
	// helper method to generate 2 keys from raw key
	public static void generateKeys(String rawKey) {
		// convert the raw key from string to an array
		List<Byte> keyArr = new ArrayList<>();

		// convert string value to int and add to the array
		for (int i = 0; i < rawKey.length(); i++) {
			keyArr.add( (byte) Integer.parseInt(Character.toString(rawKey.charAt(i))));
		}

		// permutate the current raw key with 10 bit permutation
		Byte[] currentKey = new Byte[10];
		for (int i = 0; i < currentKey.length; i++) {
			currentKey[i] = keyArr.get(indicesFor10BitPerm[i]);
		}

		// copy the values of the current key and split into 2 5 bit arrays
		Byte[] left = new Byte[5];
		System.arraycopy(currentKey, 0, left, 0, 5);
		Byte[] right = new Byte[5];
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
		left = new Byte[5];
		System.arraycopy(currentKey, 0, left, 0, 5); // left side
		right = new Byte[5];
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
	public static void moveLeftByNBits(Byte[] arr, int bits) {
		// create an array of the number of bits that we want to shift
		Byte[] remainder = new Byte[bits];

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

}