package org.forpdi.security;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class CryptManagerTest {


    @Test
    void test_encrypt_plain_text_returns_base64_encoded_result() {
        CryptManager cryptManager = new CryptManager();
		
        cryptManager.setKey("g#8K2@j!x5Qr$VmZ");
    
        String plainText = "Hello World";
    
        String encryptedText = cryptManager.encrypt(plainText);
    
        assertNotNull(encryptedText);
    
        assertTrue(Base64.isBase64(encryptedText));
    
        String decryptedText = cryptManager.decrypt(encryptedText);
    
        assertEquals(plainText, decryptedText);
    }

    @Test
    void test_initialize_with_empty_key_throws_exception() {
        CryptManager cryptManager = new CryptManager();
    
        assertThrows(IllegalArgumentException.class, () -> {
            cryptManager.setKey("");
        });
    
        assertThrows(IllegalArgumentException.class, () -> {
            cryptManager.setKey(null);
        });
    }

    @Test
    void test_decrypt_base64_encoded_ciphertext_returns_original_plain_text() {
        CryptManager cryptManager = new CryptManager();

        cryptManager.setKey("g#8K2@j!x5Qr$VmZ");

        String plainText = "Sample Text";

        String encryptedText = cryptManager.encrypt(plainText);

        String decryptedText = cryptManager.decrypt(encryptedText);

        assertEquals(plainText, decryptedText);
    }

    @Test
    void test_digest_generates_sha256_hex() {
        String input = "testInput";
        String expectedDigest = DigestUtils.sha256Hex(input);
    
        String actualDigest = CryptManager.digest(input);
    
        assertEquals(expectedDigest, actualDigest);
    }

    @Test
    void test_decrypt_malformed_base64_input_throws_exception() {
        CryptManager cryptManager = new CryptManager();

        cryptManager.setKey("g#8K2@j!x5Qr$VmZ");

        String malformedBase64 = "ThisIsNotBase64!";

        assertThrows(RuntimeException.class, () -> {
            cryptManager.decrypt(malformedBase64);
        });
    }

    @Test
    void test_decrypt_with_different_key_fails() {
        CryptManager cryptManager1 = new CryptManager();
        cryptManager1.setKey("g#8K2@j!x5Qr$VmZ");
    
        String plainText = "Hello World";
        String encryptedText = cryptManager1.encrypt(plainText);
    
        CryptManager cryptManager2 = new CryptManager();
        cryptManager2.setKey("&p9X#Lm4$Wq@2ZvR");
    
        Exception exception = assertThrows(RuntimeException.class, () -> {
            cryptManager2.decrypt(encryptedText);
        });
    
        String expectedMessage = "Erro ao decriptografar.";
        String actualMessage = exception.getMessage();
    
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void test_general_security_exception_handling() {
        CryptManager cryptManager = new CryptManager();
    
        String invalidKey = "short";
        assertThrows(RuntimeException.class, () -> {
            cryptManager.setKey(invalidKey);
        });
    
        cryptManager.setKey("&p9X#Lm4$Wq@2ZvR");
    
        String plainText = "Test";
        String encryptedText = cryptManager.encrypt(plainText);
    
        String tamperedEncryptedText = encryptedText.substring(1);
        assertThrows(RuntimeException.class, () -> {
            cryptManager.decrypt(tamperedEncryptedText);
        });
    }

    @Test
    void test_encryption_and_decryption_inverse_operations() {
        CryptManager cryptManager = new CryptManager();

        cryptManager.setKey("g#8K2@j!x5Qr$VmZ");

        String originalText = "SampleText";

        String encryptedText = cryptManager.encrypt(originalText);

        assertNotNull(encryptedText);

        String decryptedText = cryptManager.decrypt(encryptedText);

        assertEquals(originalText, decryptedText);
    }

	@Test
	void test_mixed_byte_array_conversion() {
		byte[] input = new byte[]{(byte)0xAB, (byte)0x12, (byte)0x00, (byte)0xFF};

		String result = CryptManager.toHexString(input);

		assertEquals("ab1200ff", result.toLowerCase());
	}

	@Test
	void test_token_with_nonempty_string_returns_sha256_hash() {
		String testValue = "test123";

		String token = CryptManager.token(testValue);

		assertNotNull(token);
		assertEquals(64, token.length());
		assertTrue(token.matches("[a-f0-9]{64}"));
	}


}