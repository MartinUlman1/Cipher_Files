package cz.ntt.factory;

import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.X509EncodedKeySpec;


@Slf4j
@Component
public class Encrypt {

    private static void saveToFile(byte[] data, String filename) throws Exception {
        FileOutputStream fos = new FileOutputStream(filename);
        fos.write(data);
        fos.close();
    }

    /**
     *
     * @param inputPath The path in which the files we want to encrypt are located
     * @param outputPath The path where the encrypted files will be sent
     * @param publicKey The path to the public key
     * @param ivFile The path where the encrypted IV vector will be stored
     * @param aesKeyFile Path where the encrypted AES key to the cipher is stored
     * @throws Exception
     */
    public void encryptOAEP(String inputPath, String outputPath, String publicKey, String ivFile, String aesKeyFile) throws Exception {
        try {
            // Random AES key generation
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(256);
            SecretKey aesKey = keyGenerator.generateKey();

            // Random initialization vector generation (IV)
            byte[] iv = new byte[16]; // 16 bytů pro AES CBC
            SecureRandom secureRandom = new SecureRandom();
            secureRandom.nextBytes(iv);
            IvParameterSpec vector = new IvParameterSpec(iv);

            FileReader keyReader = new FileReader(publicKey);
            PemReader pemReader = new PemReader(keyReader);
            PemObject pemObject = pemReader.readPemObject();
            byte[] content = pemObject.getContent();
            X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(content);
            PublicKey pubKey = KeyFactory.getInstance("RSA").generatePublic(pubKeySpec);

            Cipher aesCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            Cipher rsaCipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-1AndMGF1Padding");

            rsaCipher.init(Cipher.ENCRYPT_MODE, pubKey);
            aesCipher.init(Cipher.ENCRYPT_MODE, aesKey, vector);

            byte[] encryptedAESKey = rsaCipher.doFinal(aesKey.getEncoded());
            byte[] encryptedVector = rsaCipher.doFinal(vector.getIV());

            File input = new File(inputPath);
            File outputFile = new File(outputPath);

            saveToFile(encryptedAESKey, aesKeyFile);
            saveToFile(encryptedVector, ivFile);


            for (File file : input.listFiles()) {
                if (file.isFile()) {
                    // Input and output stream preparation for each file
                    FileInputStream inputStream = new FileInputStream(file);
                    File output = new File(outputFile, file.getName());
                    FileOutputStream outputStream = new FileOutputStream(output);
                    CipherOutputStream cipherOutputStream = new CipherOutputStream(outputStream, aesCipher);

                    // File encryption
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        cipherOutputStream.write(buffer, 0, bytesRead);
                    }

                    // Closure of streams
                    cipherOutputStream.close();
                    inputStream.close();

                    log.info("Encrypt was successful.");
                }
            }
        } catch (Exception e) {
            log.info("Encrypt was unsuccessful. {} {}", e.getMessage(), e.getStackTrace());
        }
    }
}