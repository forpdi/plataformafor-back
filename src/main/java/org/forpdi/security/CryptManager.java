package org.forpdi.security;

import java.security.GeneralSecurityException;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.forpdi.core.common.GeneralUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Utility class to generate encrypted content.
 * @author Renato R. R. de Oliveira
 *
 */
@Component
public class CryptManager {

	private static final String ALGORITHM = "AES";
	private static final String TRANSFORMATION = ALGORITHM + "/GCM/NoPadding";
	
	private static final Logger LOG = LoggerFactory.getLogger(CryptManager.class);
	
	/** Cipher key. */
	private byte[] key;
	/** Cipher secret key spec. */
	private SecretKeySpec keySpec;
	/** Cipher to encrypt */
	private Cipher encrypter;
	/** Cipher to decrypt */
	private Cipher decrypter;
	/** Base64 Encoder/Decoder */
	private Base64 base64;
	
	@Value("${crypt.key}")
	public void setKey(String key) {
		if (GeneralUtils.isEmpty(key)) {
			throw new IllegalArgumentException("You must provide a key or set the default key.");
		}
		LOG.debug("Instantiating crypt manager.");
		this.key = key.getBytes();
		try {
			this.encrypter = Cipher.getInstance(TRANSFORMATION);
			this.decrypter = Cipher.getInstance(TRANSFORMATION);
			this.keySpec = new SecretKeySpec(this.key, ALGORITHM);
			
			GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(128, this.key);
			
			this.encrypter.init(Cipher.ENCRYPT_MODE, this.keySpec, gcmParameterSpec);
			this.decrypter.init(Cipher.DECRYPT_MODE, this.keySpec, gcmParameterSpec);
		} catch (GeneralSecurityException ex) {
			LOG.error("Erro ao inicializar o sistema de criptografia.", ex);
			throw new RuntimeException("Erro ao inicializar o sistema de criptografia.", ex);
		}
		this.base64 = new Base64(true);
	}
	
	public String encrypt(String plain) {
		LOG.debug("Starting encryption...");
		try {
			byte[] encrypted = this.encrypter.doFinal(plain.getBytes());
			return this.base64.encodeToString(encrypted).trim();
		} catch (GeneralSecurityException ex) {
			LOG.error("Erro ao criptografar: "+plain, ex);
			throw new RuntimeException("Erro ao criptografar.", ex);
		}
	}
	

	public String decrypt(String encrypted) {
		LOG.debug("Starting encryption...");
		try {
			byte[] plain = this.base64.decode(encrypted);
			return new String(this.decrypter.doFinal(plain));
		} catch (GeneralSecurityException ex) {
			LOG.error("Erro ao decriptografar: "+encrypted, ex);
			throw new RuntimeException("Erro ao decriptografar.", ex);
		}
	}
	
	public static String toHexString(byte[] bytes) {
		StringBuilder s = new StringBuilder();
		for (int i = 0; i < bytes.length; i++) {
			int parteAlta = ((bytes[i] >> 4) & 0xf) << 4;
			int parteBaixa = bytes[i] & 0xf;
			if (parteAlta == 0)
				s.append('0');
			s.append(Integer.toHexString(parteAlta | parteBaixa));
		}
		return s.toString();
	}
	
	public static String digest(String raw) {
		return DigestUtils.sha256Hex(raw);
	}
	
	public static String token(String value) {
		return digest(value+String.valueOf(System.nanoTime()));
	}
}
