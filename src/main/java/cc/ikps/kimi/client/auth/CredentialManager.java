package cc.ikps.kimi.client.auth;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Gestiona credenciales encriptadas con AES-256
 * Las contraseñas se guardan encriptadas en config/kimiclient/auth.enc
 */
public class CredentialManager {
    private static final String ALGORITHM = "AES";
    private static final int KEY_SIZE = 256;
    private static final Path AUTH_FILE = Paths.get(".", "config", "kimiclient", "auth.enc");
    private static final String MASTER_KEY = "kimi_client_secure_key_2024"; // Base para generar la clave maestra

    private static SecretKey masterKey;
    private static String storedPasswordHash;
    private static String storedSalt;

    static {
        initializeMasterKey();
        loadStoredCredentials();
    }

    /**
     * Inicializa la clave maestra derivada
     */
    private static void initializeMasterKey() {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(MASTER_KEY.getBytes(StandardCharsets.UTF_8));
            masterKey = new SecretKeySpec(hash, 0, 32, ALGORITHM);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Carga las credenciales almacenadas del archivo
     */
    private static void loadStoredCredentials() {
        try {
            if (Files.exists(AUTH_FILE)) {
                String content = new String(Files.readAllBytes(AUTH_FILE), StandardCharsets.UTF_8);
                String[] parts = content.split("\\|");
                if (parts.length == 2) {
                    storedSalt = parts[0];
                    storedPasswordHash = parts[1];
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Establece una nueva contraseña (primera vez o cambio)
     */
    public static boolean setPassword(String password) {
        try {
            Files.createDirectories(AUTH_FILE.getParent());

            // Generar salt aleatorio
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[16];
            random.nextBytes(salt);
            String saltString = Base64.getEncoder().encodeToString(salt);

            // Encriptar contraseña
            String encryptedPassword = encryptPassword(password, salt);

            // Guardar en archivo
            String fileContent = saltString + "|" + encryptedPassword;
            Files.write(AUTH_FILE, fileContent.getBytes(StandardCharsets.UTF_8));

            // Actualizar variables estáticas
            storedSalt = saltString;
            storedPasswordHash = encryptedPassword;

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Autentica una contraseña
     */
    public static boolean authenticate(String password) {
        try {
            // Si no hay contraseña guardada, cualquier intento falla
            if (storedPasswordHash == null || storedPasswordHash.isEmpty()) {
                return false;
            }

            // Descodificar salt
            byte[] decodedSalt = Base64.getDecoder().decode(storedSalt);

            // Encriptar el intento de contraseña
            String encryptedAttempt = encryptPassword(password, decodedSalt);

            // Comparar con la guardada
            return encryptedAttempt.equals(storedPasswordHash);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Verifica si existe una contraseña establecida
     */
    public static boolean hasPassword() {
        return storedPasswordHash != null && !storedPasswordHash.isEmpty();
    }

    /**
     * Cambia la contraseña existente
     */
    public static boolean changePassword(String oldPassword, String newPassword) {
        if (!authenticate(oldPassword)) {
            return false;
        }
        return setPassword(newPassword);
    }

    /**
     * Elimina la contraseña
     */
    public static boolean removePassword(String currentPassword) {
        if (!authenticate(currentPassword)) {
            return false;
        }
        try {
            Files.deleteIfExists(AUTH_FILE);
            storedPasswordHash = null;
            storedSalt = null;
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Encripta una contraseña con salt
     */
    private static String encryptPassword(String password, byte[] salt) throws Exception {
        // Derivar clave del salt
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        digest.update(salt);
        byte[] derivedKey = digest.digest(password.getBytes(StandardCharsets.UTF_8));

        // Encriptar con AES
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        SecretKeySpec keySpec = new SecretKeySpec(derivedKey, 0, 32, ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);
        byte[] encrypted = cipher.doFinal((MASTER_KEY + System.currentTimeMillis()).getBytes(StandardCharsets.UTF_8));

        return Base64.getEncoder().encodeToString(encrypted);
    }

    /**
     * Genera una contraseña aleatoria fuerte
     */
    public static String generateStrongPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
        StringBuilder password = new StringBuilder();
        SecureRandom random = new SecureRandom();
        for (int i = 0; i < 16; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
        return password.toString();
    }
}
