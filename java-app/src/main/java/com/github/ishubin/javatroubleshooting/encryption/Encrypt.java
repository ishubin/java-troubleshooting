package com.github.ishubin.javatroubleshooting.encryption;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

public class Encrypt {
    public static SecretKey generateSecretKey(String password, byte [] iv) throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeySpec spec = new PBEKeySpec(password.toCharArray(), iv, 65536, 128); // AES-128
        SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        byte[] key = secretKeyFactory.generateSecret(spec).getEncoded();

        return new SecretKeySpec(key, "AES");
    }

    public static byte [] encryptData(String key, byte [] data) throws NoSuchPaddingException,
            NoSuchAlgorithmException,
            InvalidAlgorithmParameterException,
            InvalidKeyException,
            BadPaddingException,
            IllegalBlockSizeException, InvalidKeySpecException {

        //Prepare the nonce
        SecureRandom secureRandom = new SecureRandom();

        //Noonce should be 12 bytes
        byte[] iv = new byte[12];
        secureRandom.nextBytes(iv);

        //Prepare your key/password
        SecretKey secretKey = generateSecretKey(key, iv);


        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec parameterSpec = new GCMParameterSpec(128, iv);

        //Encryption mode on!
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);

        //Encrypt the data
        byte [] encryptedData = cipher.doFinal(data);

        //Concatenate everything and return the final data
        ByteBuffer byteBuffer = ByteBuffer.allocate(4 + iv.length + encryptedData.length);
        byteBuffer.putInt(iv.length);
        byteBuffer.put(iv);
        byteBuffer.put(encryptedData);
        return byteBuffer.array();
    }


    public static byte [] decryptData(String key, byte [] encryptedData)
            throws NoSuchPaddingException,
            NoSuchAlgorithmException,
            InvalidAlgorithmParameterException,
            InvalidKeyException,
            BadPaddingException,
            IllegalBlockSizeException,
            InvalidKeySpecException {


        //Wrap the data into a byte buffer to ease the reading process
        ByteBuffer byteBuffer = ByteBuffer.wrap(encryptedData);

        int noonceSize = byteBuffer.getInt();

        //Make sure that the file was encrypted properly
        if(noonceSize < 12 || noonceSize >= 16) {
            throw new IllegalArgumentException("Nonce size is incorrect. Make sure that the incoming data is an AES encrypted file.");
        }
        byte[] iv = new byte[noonceSize];
        byteBuffer.get(iv);

        //Prepare your key/password
        SecretKey secretKey = generateSecretKey(key, iv);

        //get the rest of encrypted data
        byte[] cipherBytes = new byte[byteBuffer.remaining()];
        byteBuffer.get(cipherBytes);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec parameterSpec = new GCMParameterSpec(128, iv);

        //Encryption mode on!
        cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);

        //Encrypt the data
        return cipher.doFinal(cipherBytes);

    }

}
