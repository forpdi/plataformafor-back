package org.forrisco.risk.contingency.history;

import org.forpdi.core.user.User;
import org.forrisco.risk.contingency.Contingency;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.lang.annotation.Annotation;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ContingencyHistoryTest {

    @Test
    @DisplayName("Getters e setters devem armazenar os valores corretamente")
    void gettersAndSetters_work() {
        ContingencyHistory h = new ContingencyHistory();

        Contingency contingency = new Contingency();
        User user = new User();
        String changedField = "campo";
        String oldValue = "antigo";
        String newValue = "novo";
        LocalDateTime now = LocalDateTime.now();

        h.setContingency(contingency);
        h.setResponsibleUser(user);
        h.setChangedField(changedField);
        h.setOldValue(oldValue);
        h.setNewValue(newValue);
        h.setChangeDate(now);

        assertSame(contingency, h.getContingency());
        assertSame(user, h.getResponsibleUser());
        assertEquals(changedField, h.getChangedField());
        assertEquals(oldValue, h.getOldValue());
        assertEquals(newValue, h.getNewValue());
        assertEquals(now, h.getChangeDate());
    }

    @Test
    @DisplayName("Classe deve estar anotada como @Entity e @Table")
    void annotations_present() {
        Annotation entityAnn = ContingencyHistory.class.getAnnotation(Entity.class);
        assertNotNull(entityAnn, "Deve ter @Entity");

        Table tableAnn = ContingencyHistory.class.getAnnotation(Table.class);
        assertNotNull(tableAnn, "Deve ter @Table");
        assertEquals("frisco_contingency_history", tableAnn.name());
    }
}
