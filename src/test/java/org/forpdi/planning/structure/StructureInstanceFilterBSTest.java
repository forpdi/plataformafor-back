package org.forpdi.planning.structure;

import java.util.ArrayList;
import java.util.List;

import org.forpdi.core.common.Criteria;
import org.forpdi.core.common.Criterion;
import org.forpdi.core.common.HibernateDAO;
import org.forpdi.core.common.JoinType;
import org.forpdi.core.common.PaginatedList;
import org.forpdi.core.common.Projection;
import org.forpdi.system.CriteriaCompanyFilter;
import org.forrisco.core.unit.Unit;
import org.forrisco.risk.links.RiskAxis;
import org.forrisco.risk.links.RiskGoal;
import org.forrisco.risk.links.RiskIndicator;
import org.forrisco.risk.links.RiskStrategy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StructureInstanceFilterBSTest {
        @InjectMocks
        StructureInstanceFilterBS bs;

        @Mock
        HibernateDAO dao;

        @Mock
        Criteria criteria;

        @Mock
        StructureBS structureBS;

        @Mock
        CriteriaCompanyFilter filter;

        @DisplayName("Deve retornar uma lista com os Objetivos de acordo com os filtros presentes.")
        @Test
        void testFilterObjectivesWithAllFilters() {
                String levelName = "objective";
                List<Long> excludedIds = List.of(4L, 5L, 7L);
                String term = "Term";

                List<StructureLevelInstance> expectedList = new ArrayList<>();

                when(structureBS.createLevelInstanceFilterCriteria(levelName)).thenReturn(criteria);
                when(criteria.add(any(Criterion.class))).thenReturn(criteria);
                when(criteria.setFirstResult(anyInt())).thenReturn(criteria);
                when(criteria.setMaxResults(anyInt())).thenReturn(criteria);
                when(criteria.setProjection(any(Projection.class))).thenReturn(criteria);
                when(filter.filterAndList(criteria, StructureLevelInstance.class, "macro.company"))
                                .thenReturn(expectedList);

                PaginatedList<StructureLevelInstance> returnedList = bs.filterObjectives(excludedIds, 1, 5, term);

                assertEquals(expectedList, returnedList.getList(),
                                "A lista retornada deveria corresponder à esperada.");
                verify(criteria, times(4)).add(any(Criterion.class));
        }

        @DisplayName("Deve retornar uma lista com os Objetivos de acordo com os filtros presentes. Caso sem termo e ids excluídos.")
        @Test
        void testFilterObjectivesWithoutExcludedIdsAndTerm() {
                String levelName = "objective";
                List<Long> excludedIds = null;
                String term = null;

                List<StructureLevelInstance> expectedList = new ArrayList<>();

                when(structureBS.createLevelInstanceFilterCriteria(levelName)).thenReturn(criteria);
                when(criteria.setFirstResult(anyInt())).thenReturn(criteria);
                when(criteria.setMaxResults(anyInt())).thenReturn(criteria);
                when(criteria.setProjection(any(Projection.class))).thenReturn(criteria);
                when(filter.filterAndList(criteria, StructureLevelInstance.class, "macro.company"))
                                .thenReturn(expectedList);

                PaginatedList<StructureLevelInstance> returnedList = bs.filterObjectives(excludedIds, 1, 5, term);

                assertEquals(expectedList, returnedList.getList(),
                                "A lista retornada deveria corresponder à esperada.");
                verify(criteria, times(0)).add(any(Criterion.class));
        }

        @DisplayName("Deve retornar uma lista com os Indicadores de acordo com os filtros presentes.")
        @Test
        void testFilterIndicatorsWithAllFilters() {
                String levelName = "indicator";
                List<Long> excludedIds = List.of(1L, 9L, 13L);
                String term = "Term";

                List<StructureLevelInstance> expectedList = new ArrayList<>();

                when(structureBS.createLevelInstanceFilterCriteria(levelName)).thenReturn(criteria);
                when(criteria.add(any(Criterion.class))).thenReturn(criteria);
                when(criteria.setFirstResult(anyInt())).thenReturn(criteria);
                when(criteria.setMaxResults(anyInt())).thenReturn(criteria);
                when(criteria.setProjection(any(Projection.class))).thenReturn(criteria);
                when(filter.filterAndList(criteria, StructureLevelInstance.class, "macro.company"))
                                .thenReturn(expectedList);

                PaginatedList<StructureLevelInstance> returnedList = bs.filterIndicators(excludedIds, 1, 5, term);

                assertEquals(expectedList, returnedList.getList(),
                                "A lista retornada deveria corresponder à esperada.");
                verify(criteria, times(4)).add(any(Criterion.class));
        }

        @DisplayName("Deve retornar uma lista com os Indicadores de acordo com os filtros presentes. Caso sem termo e ids excluídos.")
        @Test
        void testFilterIndicatorsWithoutExcludedIdsAndTerm() {
                String levelName = "indicator";
                List<Long> excludedIds = null;
                String term = null;

                List<StructureLevelInstance> expectedList = new ArrayList<>();

                when(structureBS.createLevelInstanceFilterCriteria(levelName)).thenReturn(criteria);
                when(criteria.setFirstResult(anyInt())).thenReturn(criteria);
                when(criteria.setMaxResults(anyInt())).thenReturn(criteria);
                when(criteria.setProjection(any(Projection.class))).thenReturn(criteria);
                when(filter.filterAndList(criteria, StructureLevelInstance.class, "macro.company"))
                                .thenReturn(expectedList);

                PaginatedList<StructureLevelInstance> returnedList = bs.filterIndicators(excludedIds, 1, 5, term);

                assertEquals(expectedList, returnedList.getList(),
                                "A lista retornada deveria corresponder à esperada.");
                verify(criteria, times(0)).add(any(Criterion.class));
        }

        @DisplayName("Deve retornar uma lista com as metas de acordo com os filtros presentes.")
        @Test
        void testFilterGoalsWithAllFilters() {
                String levelName = "goal";
                List<Long> excludedIds = List.of(1L, 9L, 13L);
                String term = "Term";

                List<StructureLevelInstance> expectedList = new ArrayList<>();

                when(structureBS.createLevelInstanceFilterCriteria(levelName)).thenReturn(criteria);
                when(criteria.add(any(Criterion.class))).thenReturn(criteria);
                when(criteria.setFirstResult(anyInt())).thenReturn(criteria);
                when(criteria.setMaxResults(anyInt())).thenReturn(criteria);
                when(criteria.setProjection(any(Projection.class))).thenReturn(criteria);
                when(filter.filterAndList(criteria, StructureLevelInstance.class, "macro.company"))
                                .thenReturn(expectedList);

                PaginatedList<StructureLevelInstance> returnedList = bs.filterGoals(excludedIds, 1, 5, term);

                assertEquals(expectedList, returnedList.getList(),
                                "A lista retornada deveria corresponder à esperada.");
                verify(criteria, times(4)).add(any(Criterion.class));
        }

        @DisplayName("Deve retornar uma lista com as metas de acordo com os filtros presentes. Caso sem termo e ids excluídos.")
        @Test
        void testFilterGoalsWithoutExcludedIdsAndTerm() {
                String levelName = "goal";
                List<Long> excludedIds = null;
                String term = null;

                List<StructureLevelInstance> expectedList = new ArrayList<>();

                when(structureBS.createLevelInstanceFilterCriteria(levelName)).thenReturn(criteria);
                when(criteria.setFirstResult(anyInt())).thenReturn(criteria);
                when(criteria.setMaxResults(anyInt())).thenReturn(criteria);
                when(criteria.setProjection(any(Projection.class))).thenReturn(criteria);
                when(filter.filterAndList(criteria, StructureLevelInstance.class, "macro.company"))
                                .thenReturn(expectedList);

                PaginatedList<StructureLevelInstance> returnedList = bs.filterGoals(excludedIds, 1, 5, term);

                assertEquals(expectedList, returnedList.getList(),
                                "A lista retornada deveria corresponder à esperada.");
                verify(criteria, times(0)).add(any(Criterion.class));
        }

        @DisplayName("Deve retornar uma PaginatedList com os eixos filtrados de acordo com todos os filtros passados.")
        @Test
        void testFilterAxesWithAllFilters() {
                List<Long> excludedIds = List.of(4L, 22L);
                String term = "Axes One";

                List<StructureLevel> expectedStructureLevel = new ArrayList<>();
                List<StructureLevelInstance> expectedStructureLevelInstanceList = new ArrayList<>();

                when(dao.newCriteria(StructureLevel.class)).thenReturn(criteria);
                when(criteria.add(any(Criterion.class))).thenReturn(criteria);
                when(dao.findByCriteria(criteria, StructureLevel.class)).thenReturn(expectedStructureLevel);

                Criteria newCriteria = mock(Criteria.class);
                when(dao.newCriteria(StructureLevelInstance.class)).thenReturn(newCriteria);
                when(newCriteria.createAlias(anyString(), anyString(), any(JoinType.class))).thenReturn(newCriteria);
                when(newCriteria.add(any(Criterion.class))).thenReturn(newCriteria);
                when(newCriteria.setProjection(any(Projection.class))).thenReturn(newCriteria);
                when(newCriteria.setFirstResult(anyInt())).thenReturn(newCriteria);
                when(newCriteria.setMaxResults(anyInt())).thenReturn(newCriteria);
                when(filter.filterAndList(newCriteria, StructureLevelInstance.class, "macro.company"))
                                .thenReturn(expectedStructureLevelInstanceList);

                PaginatedList<StructureLevelInstance> returnedAxes = bs.filterAxes(excludedIds, 1, 5, term);

                assertEquals(expectedStructureLevelInstanceList, returnedAxes.getList(),
                                "A lista retornada não corresponde à esperada.");
                verify(newCriteria, times(12)).add(any(Criterion.class));
        }

        @DisplayName("Deve retornar uma PaginatedList com os eixos filtrados de acordo com todos os filtros passados. Caso com filtros omissos.")
        @Test
        void testFilterAxesWithoutExcludedIdsAndTerm() {
                List<Long> excludedIds = null;
                String term = null;

                List<StructureLevel> expectedStructureLevel = new ArrayList<>();
                List<StructureLevelInstance> expectedStructureLevelInstanceList = new ArrayList<>();

                when(dao.newCriteria(StructureLevel.class)).thenReturn(criteria);
                when(criteria.add(any(Criterion.class))).thenReturn(criteria);
                when(dao.findByCriteria(criteria, StructureLevel.class)).thenReturn(expectedStructureLevel);

                Criteria newCriteria = mock(Criteria.class);
                when(dao.newCriteria(StructureLevelInstance.class)).thenReturn(newCriteria);
                when(newCriteria.createAlias(anyString(), anyString(), any(JoinType.class))).thenReturn(newCriteria);
                when(newCriteria.add(any(Criterion.class))).thenReturn(newCriteria);
                when(newCriteria.setProjection(any(Projection.class))).thenReturn(newCriteria);
                when(newCriteria.setFirstResult(anyInt())).thenReturn(newCriteria);
                when(newCriteria.setMaxResults(anyInt())).thenReturn(newCriteria);
                when(filter.filterAndList(newCriteria, StructureLevelInstance.class, "macro.company"))
                                .thenReturn(expectedStructureLevelInstanceList);

                PaginatedList<StructureLevelInstance> returnedAxes = bs.filterAxes(excludedIds, null, null, term);

                assertEquals(expectedStructureLevelInstanceList, returnedAxes.getList(),
                                "A lista retornada não corresponde à esperada.");
                verify(newCriteria, times(8)).add(any(Criterion.class));
        }

        @DisplayName("Listar as instâncias de níveis vinculadas a riscos.")
        @Test
        void testListPdiLinkedToRisks() {
                Unit unitToVerify = new Unit();
                unitToVerify.setId(1L);

                List<StructureLevelInstance> expectedList = new ArrayList<>();

                List<Long> listRiskAxis = List.of(1L);
                List<Long> listRiskStrategys = List.of(6L, 7L);
                List<Long> listRiskIndicator = List.of(97L);
                List<Long> listRiskGoal = List.of(23L);

                Criteria criteriaRiskAxis = mock(Criteria.class);
                Criteria criteriaRiskStrategy = mock(Criteria.class);
                Criteria criteriaRiskIndicator = mock(Criteria.class);
                Criteria criteriaRiskGoal = mock(Criteria.class);

                when(dao.newCriteria(RiskAxis.class)).thenReturn(criteriaRiskAxis);
                when(dao.newCriteria(RiskStrategy.class)).thenReturn(criteriaRiskStrategy);
                when(dao.newCriteria(RiskIndicator.class)).thenReturn(criteriaRiskIndicator);
                when(dao.newCriteria(RiskGoal.class)).thenReturn(criteriaRiskGoal);

                when(criteriaRiskAxis.add(any(Criterion.class))).thenReturn(criteriaRiskAxis);
                when(criteriaRiskAxis.createAlias(anyString(), anyString(), any(JoinType.class)))
                                .thenReturn(criteriaRiskAxis);
                when(dao.findByCriteria(criteriaRiskAxis, Long.class)).thenReturn(listRiskAxis);

                when(criteriaRiskStrategy.add(any(Criterion.class))).thenReturn(criteriaRiskStrategy);
                when(criteriaRiskStrategy.createAlias(anyString(), anyString(), any(JoinType.class)))
                                .thenReturn(criteriaRiskStrategy);
                when(dao.findByCriteria(criteriaRiskStrategy, Long.class)).thenReturn(listRiskStrategys);

                when(criteriaRiskIndicator.add(any(Criterion.class))).thenReturn(criteriaRiskIndicator);
                when(criteriaRiskIndicator.createAlias(anyString(), anyString(), any(JoinType.class)))
                                .thenReturn(criteriaRiskIndicator);
                when(dao.findByCriteria(criteriaRiskIndicator, Long.class)).thenReturn(listRiskIndicator);

                when(criteriaRiskGoal.add(any(Criterion.class))).thenReturn(criteriaRiskGoal);
                when(criteriaRiskGoal.createAlias(anyString(), anyString(), any(JoinType.class)))
                                .thenReturn(criteriaRiskGoal);
                when(dao.findByCriteria(criteriaRiskGoal, Long.class)).thenReturn(listRiskGoal);

                when(dao.newCriteria(StructureLevelInstance.class)).thenReturn(criteria);
                when(criteria.add(any(Criterion.class))).thenReturn(criteria);
                when(dao.findByCriteria(criteria, StructureLevelInstance.class)).thenReturn(expectedList);

                List<StructureLevelInstance> returnedList = bs.listPdiLinkedToRisks(unitToVerify);

                assertEquals(expectedList, returnedList, "A lista retornada não é a esperada.");
        }

    @DisplayName("Listar as instâncias de níveis vinculadas a riscos. Caso onde não há vínculos, gera lista vazia.")
    @Test
    void testListPdiLinkedToRisksShouldReturnEmptyList() {
        Unit unitToVerify = new Unit();
        unitToVerify.setId(1L);

        List<Long> listRiskAxis = List.of(); 
        List<Long> listRiskStrategys = List.of();
        List<Long> listRiskIndicator = List.of();
        List<Long> listRiskGoal = List.of();

        Criteria criteriaRiskAxis = mock(Criteria.class);
        Criteria criteriaRiskStrategy = mock(Criteria.class);
        Criteria criteriaRiskIndicator = mock(Criteria.class);
        Criteria criteriaRiskGoal = mock(Criteria.class);


        when(dao.newCriteria(RiskAxis.class)).thenReturn(criteriaRiskAxis);
        when(dao.newCriteria(RiskStrategy.class)).thenReturn(criteriaRiskStrategy);
        when(dao.newCriteria(RiskIndicator.class)).thenReturn(criteriaRiskIndicator);
        when(dao.newCriteria(RiskGoal.class)).thenReturn(criteriaRiskGoal);


        lenient().when(criteriaRiskAxis.add(any(Criterion.class))).thenReturn(criteriaRiskAxis);
        lenient().when(criteriaRiskAxis.createAlias(anyString(), anyString(), any(org.forpdi.core.common.JoinType.class))) 
                 .thenReturn(criteriaRiskAxis);
        lenient().when(criteriaRiskStrategy.add(any(Criterion.class))).thenReturn(criteriaRiskStrategy);
        lenient().when(criteriaRiskStrategy.createAlias(anyString(), anyString(), any(org.forpdi.core.common.JoinType.class))) 
                 .thenReturn(criteriaRiskStrategy);
        lenient().when(criteriaRiskIndicator.add(any(Criterion.class))).thenReturn(criteriaRiskIndicator);
        lenient().when(criteriaRiskIndicator.createAlias(anyString(), anyString(), any(org.forpdi.core.common.JoinType.class))) 
                 .thenReturn(criteriaRiskIndicator);
        lenient().when(criteriaRiskGoal.add(any(Criterion.class))).thenReturn(criteriaRiskGoal);
        lenient().when(criteriaRiskGoal.createAlias(anyString(), anyString(), any(org.forpdi.core.common.JoinType.class))) 
                 .thenReturn(criteriaRiskGoal);

        lenient().when(dao.findByCriteria(criteriaRiskAxis, Long.class)).thenReturn(listRiskAxis); 
        lenient().when(dao.findByCriteria(criteriaRiskStrategy, Long.class)).thenReturn(listRiskStrategys);
        lenient().when(dao.findByCriteria(criteriaRiskIndicator, Long.class)).thenReturn(listRiskIndicator); 
        lenient().when(dao.findByCriteria(criteriaRiskGoal, Long.class)).thenReturn(listRiskGoal); 
        lenient().when(dao.newCriteria(StructureLevelInstance.class)).thenReturn(criteria); 
        lenient().when(criteria.add(any(Criterion.class))).thenReturn(criteria);
        lenient().when(criteria.createAlias(anyString(), anyString(), any(org.forpdi.core.common.JoinType.class))).thenReturn(criteria); 
        lenient().when(dao.findByCriteria(criteria, StructureLevelInstance.class)).thenReturn(new ArrayList<>()); 

        List<StructureLevelInstance> returnedList = bs.listPdiLinkedToRisks(unitToVerify);

        assertTrue(returnedList.isEmpty(), "O retorno esperado seria uma lista vazia.");
    }
}

