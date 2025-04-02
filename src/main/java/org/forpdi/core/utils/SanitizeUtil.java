package org.forpdi.core.utils;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

import java.util.List;

public class SanitizeUtil {

    /**
     * Sanitiza uma string, permitindo apenas as tags definidas em `allowedTags`.
     *
     * @param string      A string a ser sanitizada.
     * @param allowedTags Lista de tags permitidas.
     * @return String sanitizada com apenas as tags permitidas.
     */
    public static String sanitize(String string, List<String> allowedTags) {
        if (string != null && allowedTags != null) {
            Safelist safelist = createCustomSafelist(allowedTags);
            return Jsoup.clean(string, "", safelist, new org.jsoup.nodes.Document.OutputSettings().prettyPrint(false));
        }
        return string;
    }    

    /**
     * Sobrecarga do método sanitize que utiliza uma lista padrão de tags HTML
     * permitidas.
     *
     * @param string A string a ser sanitizada.
     * @return String sanitizada com as tags padrão permitidas.
     */
    public static String sanitize(String string) {
        List<String> defaultTags = List.of("div", "p", "br", "strong", "em", "ul", "li", "a", "b", "i", "u", "ol", "span",
                "img", "blockquote", "sup");
        return sanitize(string, defaultTags);
    }

    /**
     * Cria uma lista branca personalizada para tags permitidas e adiciona atributos
     * específicos para <img>.
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
}
