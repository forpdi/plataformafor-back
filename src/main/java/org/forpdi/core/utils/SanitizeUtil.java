package org.forpdi.core.utils;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Utilitário central de sanitização da plataforma.
 * <p>
 * 1. Sanitiza HTML (mantendo apenas as tags permitidas) – métodos
 * {@code sanitize}.
 * 2. Sanitiza nomes de arquivo para garantir compatibilidade com o charset
 * legádo do banco de dados – método {@code sanitizeFilename}.
 * </p>
 */
public class SanitizeUtil {

    /*
     * =============================================================
     * HTML – mantém apenas as tags e atributos permitidos
     * =============================================================
     */

    /**
     * Sanitiza uma string HTML, permitindo apenas as tags definidas em
     * {@code allowedTags}.
     *
     * @param string      A string a ser sanitizada.
     * @param allowedTags Lista de tags permitidas.
     * @return String sanitizada com apenas as tags permitidas.
     */
    public static String sanitize(String string, List<String> allowedTags) {
        if (string != null && allowedTags != null) {
            Safelist safelist = createCustomSafelist(allowedTags);
            return Jsoup.clean(string, "", safelist,
                    new org.jsoup.nodes.Document.OutputSettings().prettyPrint(false));
        }
        return string;
    }

    /**
     * Sanitiza utilizando uma lista padrão de tags HTML permitidas.
     *
     * @param string A string a ser sanitizada.
     * @return String sanitizada.
     */
    public static String sanitize(String string) {
        List<String> defaultTags = List.of(
                "div", "p", "br", "strong", "em", "ul", "li", "a", "b", "i", "u", "ol", "span",
                "img", "blockquote", "sup");
        return sanitize(string, defaultTags);
    }

    /**
     * Cria uma {@link Safelist} customizada com base nas tags permitidas.
     */
    private static Safelist createCustomSafelist(List<String> allowedTags) {
        Safelist safelist = new Safelist();

        allowedTags.forEach(tag -> {
            safelist.addTags(tag);
            safelist.addAttributes(tag, "class", "style");

            if ("a".equals(tag)) {
                safelist.addAttributes("a", "href", "rel", "target");
                safelist.addProtocols("a", "href", "https", "http");
            }
            if ("img".equals(tag)) {
                safelist.addAttributes("img", "src", "alt", "width", "height");
            }
        });

        return safelist;
    }

    private static final Charset DB_CHARSET = StandardCharsets.ISO_8859_1;
    private static final Pattern FORBIDDEN = Pattern.compile("[\\p{Cntrl}&&[^\\r\\n\\t]]|[\\uD800-\\uDFFF]");

    /**
     * Sanitiza nomes de arquivo para gravação no banco/S3.
     * <ul>
     * <li>Converte para NFC (forma pré-composta) – "o\u0301" → "ó".</li>
     * <li>Elimina caracteres de controle e emojis.</li>
     * <li>Substitui bytes que o charset do banco não suporta por "_".</li>
     * <li>Limita a 250 caracteres para caber em VARCHAR(255) indexado.</li>
     * </ul>
     *
     * @param original nome original enviado pelo usuário.
     * @return versão segura para persistir.
     */
    public static String sanitizeFilename(String original) {
        if (original == null)
            return null;
        String nfc = Normalizer.normalize(original, Normalizer.Form.NFC);
        String cleaned = FORBIDDEN.matcher(nfc).replaceAll("");

        byte[] bytes = cleaned.getBytes(DB_CHARSET);
        char[] chars = cleaned.toCharArray();
        StringBuilder safe = new StringBuilder(chars.length);
        int i = 0;
        for (byte b : bytes) {
            safe.append(b == '?' ? '_' : chars[i]);
            i++;
        }

        return safe.length() > 250 ? safe.substring(0, 250) : safe.toString();
    }
}
