package org.forpdi.planning.jobs;

import org.forpdi.core.common.PaginatedList;
import org.forpdi.dashboard.DashboardBS;
import org.forpdi.planning.attribute.types.enums.Periodicity;
import org.forpdi.planning.structure.StructureBS;
import org.forpdi.planning.structure.StructureLevelInstance;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScheduledDashboardTasksTest {

	@Mock
	private StructureBS structureBS;

	@Mock
	private DashboardBS dashboardBS;

	@InjectMocks
	private ScheduledDasboardTasks scheduledTasks;

	@Test
	@DisplayName("Deve salvar histórico de indicadores corretamente")
	void testSaveIndicatorHistory() {
		List<StructureLevelInstance> indicators = new ArrayList<>();
		StructureLevelInstance indicator1 = new StructureLevelInstance();
		StructureLevelInstance indicator2 = new StructureLevelInstance();
		indicator1.setId(2L);
		indicator2.setId(3L);

		indicators.add(indicator1);
		indicators.add(indicator2);

		PaginatedList<StructureLevelInstance> paginatedIndicators = new PaginatedList<>();
		paginatedIndicators.setList(indicators);
		paginatedIndicators.setTotal(2L);

		Periodicity periodicity = Periodicity.MONTHLY;
		Calendar calendar = Calendar.getInstance();
		calendar.set(2024, Calendar.DECEMBER, 7);
		Date nextSaveDate = calendar.getTime();
		indicator1.setNextSave(nextSaveDate);
		indicator2.setNextSave(nextSaveDate);

		when(structureBS.listAllIndicators()).thenReturn(paginatedIndicators);
		when(structureBS.getPeriodicityByInstance(indicator1)).thenReturn(periodicity);
		when(structureBS.getPeriodicityByInstance(indicator2)).thenReturn(periodicity);
		doNothing().when(dashboardBS).saveIndicatorHistory(indicator1, periodicity);
		doNothing().when(dashboardBS).saveIndicatorHistory(indicator2, periodicity);
		scheduledTasks.saveIndicatorHistory();

		verify(dashboardBS, times(1)).saveIndicatorHistory(indicator1, periodicity);
		verify(dashboardBS, times(1)).saveIndicatorHistory(indicator2, periodicity);
	}

	@Test
	@DisplayName("Não deve salvar histórico quando periodicidade é nula")
	void testSaveIndicatorHistoryWithNullPeriodicity() {
		List<StructureLevelInstance> indicators = new ArrayList<>();
		StructureLevelInstance indicator = mock(StructureLevelInstance.class);

		indicators.add(indicator);

		PaginatedList<StructureLevelInstance> paginatedIndicators = new PaginatedList<>();
		paginatedIndicators.setList(indicators);

		when(structureBS.listAllIndicators()).thenReturn(paginatedIndicators);
		when(indicator.getNextSave()).thenReturn(new Date());
		when(structureBS.getPeriodicityByInstance(indicator)).thenReturn(null);

		scheduledTasks.saveIndicatorHistory();

		verify(dashboardBS, never()).saveIndicatorHistory(any(), any());
	}

	@Test
	@DisplayName("Não deve salvar histórico para indicadores com data futura de salvamento")
	void testSaveIndicatorHistoryWithFutureDate() {
		List<StructureLevelInstance> indicators = new ArrayList<>();
		StructureLevelInstance indicator = mock(StructureLevelInstance.class);

		indicators.add(indicator);

		PaginatedList<StructureLevelInstance> paginatedIndicators = new PaginatedList<>();
		paginatedIndicators.setList(indicators);

		Calendar futureDate = Calendar.getInstance();
		futureDate.add(Calendar.DAY_OF_MONTH, 1);

		when(structureBS.listAllIndicators()).thenReturn(paginatedIndicators);
		when(indicator.getNextSave()).thenReturn(futureDate.getTime());

		scheduledTasks.saveIndicatorHistory();

		verify(dashboardBS, never()).saveIndicatorHistory(any(), any());
	}
}
