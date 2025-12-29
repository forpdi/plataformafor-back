package org.forpdi.planning.structure;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.forpdi.core.common.PaginatedList;
import org.forpdi.planning.attribute.Attribute;
import org.forpdi.planning.attribute.StaticAttributeLabels;
import org.forpdi.planning.fields.FieldsBS;
import org.forpdi.planning.fields.OptionsField;
import org.forpdi.planning.structure.xml.StructureBeans;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;

class DuplicatedStructureCheckHelperTest {

    @InjectMocks
    private DuplicatedStructureCheckHelper helper;

    @Mock
    private StructureBS structureBS;

    @Mock
    private FieldsBS fieldsBS;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    private static StructureBeans.Structure beanStructure(String name, List<StructureBeans.StructureLevel> levels) {
        StructureBeans.Structure s = new StructureBeans.Structure();
        s.name = name;
        s.levels = new ArrayList<>(levels);
        return s;
    }

    private static StructureBeans.StructureLevel beanLevel(String name, String description,
            List<StructureBeans.Attribute> attributes) {
        StructureBeans.StructureLevel l = new StructureBeans.StructureLevel();
        l.name = name;
        l.description = description;
        l.attributes = new ArrayList<>(attributes);
        return l;
    }

    private static StructureBeans.Attribute beanAttr(String label, String desc, boolean required,
            List<StructureBeans.OptionsField> opts) {
        StructureBeans.Attribute a = new StructureBeans.Attribute();
        a.label = label;
        a.description = desc;
        a.required = required;
        a.optionsField = (opts != null) ? new ArrayList<>(opts) : new ArrayList<>();
        return a;
    }

    private static StructureBeans.OptionsField beanOpt(String label) {
        StructureBeans.OptionsField o = new StructureBeans.OptionsField();
        o.label = label;
        return o;
    }

    private static PaginatedList<Attribute> paginated(List<Attribute> list) {
        PaginatedList<Attribute> p = new PaginatedList<>();
        p.setList(list);
        p.setTotal((long) list.size());
        return p;
    }

    private static PaginatedList<OptionsField> paginatedOptions(List<OptionsField> list) {
        PaginatedList<OptionsField> p = new PaginatedList<>();
        p.setList(list);
        p.setTotal((long) list.size());
        return p;
    }

    @Test
    @DisplayName("retrieveDuplicatedStructure: retorna estrutura quando nome já existe")
    void duplicatedByName() {
        StructureBeans.Structure readed = beanStructure("Estrutura X", List.of());

        Structure s1 = mock(Structure.class);
        when(s1.getName()).thenReturn("Estrutura X");

        PaginatedList<Structure> list = new PaginatedList<>();
        list.setList(List.of(s1));
        when(structureBS.list()).thenReturn(list);

        Structure dup = helper.retrieveDuplicatedStructure(readed);
        assertSame(s1, dup);
        verify(structureBS, never()).listStructureLevels(any());
    }

    @Test
    @DisplayName("retrieveDuplicatedStructure: detecta duplicidade por níveis/atributos/opções mesmo com nomes diferentes")
    void duplicatedByLevelsAttributesAndOptions() {
        var readAttr = beanAttr("Texto A", "Desc A", true, List.of(beanOpt("Op1"), beanOpt("Op2")));
        var readLevel = beanLevel("Meta", "Meta Desc", List.of(readAttr));
        StructureBeans.Structure readed = beanStructure("Nome_Qualquer", List.of(readLevel));

        Structure existing = mock(Structure.class);
        when(existing.getName()).thenReturn("Outro Nome");

        PaginatedList<Structure> list = new PaginatedList<>();
        list.setList(List.of(existing));
        when(structureBS.list()).thenReturn(list);

        StructureLevel levelEntity = mock(StructureLevel.class);
        when(levelEntity.getName()).thenReturn("Meta");
        when(levelEntity.getDescription()).thenReturn("Meta Desc");
        when(structureBS.listStructureLevels(existing))
                .thenReturn(new ArrayList<>(List.of(levelEntity)));
        Attribute attrEntity = mock(Attribute.class);
        when(attrEntity.getLabel()).thenReturn("Texto A");
        when(attrEntity.getDescription()).thenReturn("Desc A");
        when(attrEntity.isRequired()).thenReturn(true);
        when(attrEntity.getId()).thenReturn(123L);

        StructureLevel attrLevel = mock(StructureLevel.class);
        when(attrLevel.isGoal()).thenReturn(false);
        when(attrLevel.isIndicator()).thenReturn(false);
        when(attrEntity.getLevel()).thenReturn(attrLevel);

        when(structureBS.listAttributes(levelEntity)).thenReturn(paginated(List.of(attrEntity)));

        OptionsField of1 = mock(OptionsField.class);
        when(of1.getLabel()).thenReturn("Op1");
        when(of1.isDocument()).thenReturn(false);

        OptionsField of2 = mock(OptionsField.class);
        when(of2.getLabel()).thenReturn("Op2");
        when(of2.isDocument()).thenReturn(false);

        OptionsField doc = mock(OptionsField.class);
        when(doc.isDocument()).thenReturn(true);

        when(fieldsBS.getOptionsField(eq(123L), eq(false)))
                .thenReturn(paginatedOptions(List.of(of2, of1, doc)));

        Structure dup = helper.retrieveDuplicatedStructure(readed);

        assertSame(existing, dup, "Deveria apontar a estrutura existente como duplicada por nível/atributos/opções.");
    }

    @Test
    @DisplayName("retrieveDuplicatedStructure: retorna null quando níveis diferem (tamanho)")
    void notDuplicatedWhenLevelsSizeDiffers() {
        var lvl1 = beanLevel("Meta", "d1", List.of());
        var lvl2 = beanLevel("Ind", "d2", List.of());
        StructureBeans.Structure readed = beanStructure("Estrutura Z", List.of(lvl1, lvl2));

        Structure existing = mock(Structure.class);
        when(existing.getName()).thenReturn("Estrutura Outra");

        PaginatedList<Structure> list = new PaginatedList<>();
        list.setList(List.of(existing));
        when(structureBS.list()).thenReturn(list);

        StructureLevel levelEntity = mock(StructureLevel.class);
        when(levelEntity.getName()).thenReturn("Meta");
        when(levelEntity.getDescription()).thenReturn("d1");
        when(structureBS.listStructureLevels(existing))
                .thenReturn(new ArrayList<>(List.of(levelEntity)));

        Structure dup = helper.retrieveDuplicatedStructure(readed);
        assertNull(dup, "Níveis em quantidade diferente → não é duplicado.");
    }

    @Test
    @DisplayName("hasDifferentAttributes: atributos estáticos são ignorados antes da comparação")
    void staticAttributesAreIgnored() {
        var readAttr = beanAttr("Campo Livre", "desc", false, List.of());
        var readLevel = beanLevel("Meta", "d", List.of(readAttr));
        StructureBeans.Structure readed = beanStructure("S1", List.of(readLevel));

        Structure existing = mock(Structure.class);
        when(existing.getName()).thenReturn("S2");

        PaginatedList<Structure> list = new PaginatedList<>();
        list.setList(List.of(existing));
        when(structureBS.list()).thenReturn(list);

        StructureLevel levelEntity = mock(StructureLevel.class);
        when(levelEntity.getName()).thenReturn("Meta");
        when(levelEntity.getDescription()).thenReturn("d");
        when(structureBS.listStructureLevels(existing))
                .thenReturn(new ArrayList<>(List.of(levelEntity)));
        Attribute staticGoalManager = mock(Attribute.class);
        StructureLevel goalLevel = mock(StructureLevel.class);
        when(goalLevel.isGoal()).thenReturn(true);
        when(goalLevel.isIndicator()).thenReturn(false);
        when(staticGoalManager.getLevel()).thenReturn(goalLevel);
        when(staticGoalManager.getLabel()).thenReturn(StaticAttributeLabels.GOAL_MANAGER_LABEL);

        Attribute nonStatic = mock(Attribute.class);
        when(nonStatic.getLevel()).thenReturn(goalLevel);
        when(nonStatic.getLabel()).thenReturn("Campo Livre");
        when(nonStatic.getDescription()).thenReturn("desc");
        when(nonStatic.isRequired()).thenReturn(false);
        when(nonStatic.getId()).thenReturn(1L);

        when(structureBS.listAttributes(levelEntity)).thenReturn(paginated(List.of(staticGoalManager, nonStatic)));
        when(fieldsBS.getOptionsField(eq(1L), eq(false))).thenReturn(paginatedOptions(List.of()));

        Structure dup = helper.retrieveDuplicatedStructure(readed);
        assertSame(existing, dup, "Com o estático filtrado, resta apenas o atributo comparável (igual) → duplicado.");
    }
}
