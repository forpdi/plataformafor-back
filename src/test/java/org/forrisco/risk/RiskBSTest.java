package org.forrisco.risk;

import org.forpdi.core.common.HibernateDAO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class RiskBSTest {

	@Mock
	HibernateDAO hibernateDAO;

	@InjectMocks
	private RiskBS riskBS;

	@DisplayName("RiskBs Salvar risco.")
	@Test
	public void testSaveRisk() {

		Risk validRisk = new Risk();
		validRisk.setDeleted(false);
		doNothing().when(hibernateDAO).persist(validRisk);

		riskBS.saveRisk(validRisk);

		try {
			verify(hibernateDAO).persist(validRisk);
		}catch (Throwable ex){
			fail("O método persist não foi chamado para o risco válido: " + ex.getMessage());
		}
		assertFalse(validRisk.isDeleted(),"Risk.isDeleted() é verdadeiro.");
	}
}