package utils;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * EncrpytThenHash Class
 * This class takes a file input, converts to bytes and encrypts the file using AES.
 * A hash is then created which can be used to verify the integrity of the file.
 */
public class EncryptThenHash {

    private static final String AES = "AES";
    private static final String HMAC_SHA256 = "HmacSHA256";
    private static final int AES_KEY_SIZE = 256;

    /**
     * Reads files and converts to bytes.
     * @param file  File to be read and converted to bytes.
     * @return returns the a byte array representation of the file.
     * @throws IOException
     */
    public static byte[] readFile(File file) throws IOException {
        byte[] fileBytes;
        try (FileInputStream inputStream = new FileInputStream(file)) {
            fileBytes = new byte[(int) file.length()];
            inputStream.read(fileBytes);
        }
        return fileBytes;
    }

    /**
     * Encrypts the contents of the input file using the AES encryption algorithm with the provided SecretKey.
     *
     * @param inputFile The input file to be encrypted.
     * @param key The SecretKey used for encryption.
     * @return A byte array containing the encrypted data.
     * @throws IOException If an I/O error occurs while reading or writing the file.
     * @throws NoSuchAlgorithmException If the requested cryptographic algorithm is not available.
     * @throws NoSuchPaddingException If the padding scheme is not available for the cipher.
     * @throws InvalidKeyException If the key is invalid for encryption.
     * @throws IllegalBlockSizeException If the block size of the input data is incorrect.
     * @throws BadPaddingException If the padding of the input data is incorrect.
     */
    public static byte[] encryptFile(File inputFile, SecretKey key) throws IOException, NoSuchAlgorithmException,
            NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        byte[] fileBytes = readFile(inputFile);
        byte[] encryptedBytes = encrypt(fileBytes, key);

        try (FileOutputStream outputStream = new FileOutputStream("Encrypted" +inputFile )) {
            outputStream.write(encryptedBytes);
        }

        return encryptedBytes;
    }

    /**
     * Encrypts the provided byte array using the AES encryption algorithm with the provided SecretKey.
     *
     * @param data The byte array to be encrypted.
     * @param key The SecretKey used for encryption.
     * @return A byte array containing the encrypted data.
     * @throws NoSuchAlgorithmException If the requested cryptographic algorithm is not available.
     * @throws NoSuchPaddingException If the padding scheme is not available for the cipher.
     * @throws InvalidKeyException If the key is invalid for encryption.
     * @throws IllegalBlockSizeException If the block size of the input data is incorrect.
     * @throws BadPaddingException If the padding of the input data is incorrect.
     */
    public static byte[] encrypt(byte[] data, SecretKey key) throws NoSuchAlgorithmException,
            NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance("AES");
        SecretKey secretKey = new SecretKeySpec(key.getEncoded(), "AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        return cipher.doFinal(data);
    }

    /**
     * Decrypts the provided byte array using the AES decryption algorithm with the provided SecretKeySpec key.
     *
     * @param data The byte array to be decrypted.
     * @param key The SecretKeySpec key used for decryption.
     * @return A byte array containing the decrypted data.
     * @throws NoSuchAlgorithmException If the requested cryptographic algorithm is not available.
     * @throws NoSuchPaddingException If the padding scheme is not available for the cipher.
     * @throws InvalidKeyException If the key is invalid for decryption.
     * @throws IllegalBlockSizeException If the block size of the input data is incorrect.
     * @throws BadPaddingException If the padding of the input data is incorrect.
     */
    public static byte[] decrypt(byte[] data, SecretKeySpec key) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance(AES);
        cipher.init(Cipher.DECRYPT_MODE, key);
        return cipher.doFinal(data);
    }

    /**
     * Computes the HMAC-SHA256 hash of the provided byte array using the specified SecretKeySpec key.
     *
     * @param data The byte array to be hashed.
     * @param key The SecretKeySpec key used for HMAC-SHA256 hashing.
     * @return A byte array containing the HMAC-SHA256 hash of the input data.
     * @throws NoSuchAlgorithmException If the requested cryptographic algorithm is not available.
     * @throws InvalidKeyException If the key is invalid for HMAC-SHA256 hashing.
     */
    public static byte[] hashify(byte[] data, SecretKeySpec key) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac hmac = Mac.getInstance(HMAC_SHA256);
        hmac.init(key);
        return hmac.doFinal(data);
    }

    /**
     * Verifies whether the provided byte array matches the HMAC-SHA256 hash using the specified SecretKeySpec key.
     *
     * @param data The byte array to be verified against the hash.
     * @param hash The expected HMAC-SHA256 hash.
     * @param key The SecretKeySpec key used for HMAC-SHA256 hashing.
     * @return true if the computed hash matches the expected hash, false otherwise.
     * @throws NoSuchAlgorithmException If the requested cryptographic algorithm is not available.
     * @throws InvalidKeyException If the key is invalid for HMAC-SHA256 hashing.
     */
    public static boolean verifyHash(byte[] data, byte[] hash, SecretKeySpec key) throws NoSuchAlgorithmException, InvalidKeyException {
        byte[] computedHash = hashify(data, key);
        return MessageDigest.isEqual(computedHash, hash);
    }

    public static void main(String[] args) {
        File filename = new File("motivation.txt");

        try {
            // generate AES key
            KeyGenerator keyGen = KeyGenerator.getInstance(AES);
            keyGen.init(AES_KEY_SIZE);
            SecretKey aesKey = keyGen.generateKey();
            SecretKeySpec aesKeySpec = new SecretKeySpec(aesKey.getEncoded(), AES);

            byte[] encryptedPayload = encryptFile(filename, aesKeySpec);

            byte[] hash = hashify(encryptedPayload, aesKeySpec);

            System.out.println("Encrypted payload: " + Base64.getEncoder().encodeToString(encryptedPayload));
            System.out.println("Hash: " + Base64.getEncoder().encodeToString(hash));

            System.out.println("Hash verification: " + verifyHash(encryptedPayload, hash, aesKeySpec));

            // Tamper with the payload
            encryptedPayload[0] ^= 0x01;  // Flip the first bit of the first byte

            System.out.println("Tampered payload: " + Base64.getEncoder().encodeToString(encryptedPayload));

            // Verify the hash again
            System.out.println("Hash verification after tampering: " + verifyHash(encryptedPayload, hash, aesKeySpec));

        } catch (NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException |
                 InvalidKeyException | IOException e) {
            throw new RuntimeException(e);
        }
    }


}

