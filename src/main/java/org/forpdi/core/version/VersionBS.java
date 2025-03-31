package org.forpdi.core.version;

import java.util.List;

import org.forpdi.core.common.Criteria;
import org.forpdi.core.common.HibernateBusiness;
import org.forpdi.core.common.PaginatedList;
import org.forpdi.core.common.Restrictions;
import org.hibernate.criterion.Order;
import org.springframework.stereotype.Service;

/**
 * 
 */

@Service
public class VersionBS extends HibernateBusiness {

	/**
	 * Salvar uma nova versão.
	 * 
	 * @param versionHistory
	 *            versão que será salva.
	 * 
	 * @return versionHistory Versão que foi salva.
	 */
	public VersionHistory saveNewVersion(VersionHistory versionHistory) {
		versionHistory.setDeleted(false);
		this.persist(versionHistory);
		
		return versionHistory;
	}
	
	/**
	 * Recupera uma instância do objeto VersionHistory utilizando um id
	 * 
	 * @param id
	 *             à ser utilizado na query
	 * @return Versão que utiliza o id epecificado
	 */
	public VersionHistory retrieveVersionById (Long id) {
		Criteria criteria  = this.dao.newCriteria(VersionHistory.class)
			.add(Restrictions.eq("id",id))
			.add(Restrictions.eq("deleted", false));
		return (VersionHistory) criteria.uniqueResult();
	}
	
	/**
	 * Retorna riscos de uma unidade
	 * 
	 * @param Unit,
	 *            instância da unidade
	 * 
	 */
	public PaginatedList<VersionHistory> listVersions() {
		PaginatedList<VersionHistory> results = new PaginatedList<VersionHistory>();

		Criteria criteria = this.dao.newCriteria(VersionHistory.class)
				.add(Restrictions.eq("deleted", false))
				.addOrder(Order.desc("releaseDate"));

		List<VersionHistory> versions = this.dao.findByCriteria(criteria, VersionHistory.class);
				
		results.setList(versions);
		results.setTotal((long) versions.size());

		return results;
	}


}
