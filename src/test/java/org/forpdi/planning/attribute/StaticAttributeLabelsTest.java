package org.forpdi.planning.attribute;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class StaticAttributeLabelsTest {


    @Test
    @DisplayName("Deve retornar os valores corretos para as constantes estáticas")
    public void test_static_labels_return_expected_values() {
        String responsibleLabel = StaticAttributeLabels.GOAL_RESPONSIBLE_LABEL;
        String managerLabel = StaticAttributeLabels.GOAL_MANAGER_LABEL;
        String attachmentLabel = StaticAttributeLabels.GOAL_ATTACHMENT_LABEL;
        String justificationLabel = StaticAttributeLabels.INDICATOR_JUSTIFICATION_LABEL;

        assertEquals("Responsável Técnico", responsibleLabel, "GOAL_RESPONSIBLE_LABEL deve retornar o valor correto");
        assertEquals("Gestor", managerLabel, "GOAL_MANAGER_LABEL deve retornar o valor correto");
        assertEquals("Anexar arquivos", attachmentLabel, "GOAL_ATTACHMENT_LABEL deve retornar o valor correto");
        assertEquals("Justificativa do Formato", justificationLabel, "INDICATOR_JUSTIFICATION_LABEL deve retornar o valor correto");
    }
}
