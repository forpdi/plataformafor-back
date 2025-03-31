package org.forpdi.core.utils;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class RegexUtilTest {


    @Test
    @DisplayName("Deve retornar o padrão regex correto para tag HTML div")
    public void test_html_open_tag_regex_for_div() {
        String tag = "div";
    
        String regex = RegexUtil.getHTMLOpenTagRegex(tag);
    
        assertEquals("<div[^>]*>", regex, "O padrão regex deve corresponder a uma tag div de abertura");
    
        assertTrue("<div>".matches(regex), "Deve corresponder a tag div simples");
        assertTrue("<div class=\"test\">".matches(regex), "Deve corresponder a tag div com atributos");
    }

    @Test
    @DisplayName("Deve retornar um padrão regex válido para tag HTML básica com conteúdo")
    void test_basic_html_tag_regex_pattern() {
        String tag = "div";
    
        String regex = RegexUtil.getHTMLElementTagRegex(tag);
    
        String htmlContent = "<div class='test'>Content</div>";
        assertTrue(htmlContent.matches(regex), "O padrão regex deve corresponder a uma tag HTML básica com conteúdo");
    
        String invalidContent = "<span>Wrong tag</span>";
        assertFalse(invalidContent.matches(regex), "O padrão regex não deve corresponder a uma tag HTML diferente");
    }
}


