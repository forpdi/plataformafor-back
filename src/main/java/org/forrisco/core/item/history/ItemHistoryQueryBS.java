package org.forrisco.core.item.history;

import java.util.List;

import org.forpdi.core.bean.DefaultParams;
import org.forpdi.core.common.Criteria;
import org.forpdi.core.common.HibernateBusiness;
import org.forpdi.core.common.Order;
import org.forpdi.core.common.PaginatedList;
import org.forpdi.core.common.Projections;
import org.forpdi.core.common.Restrictions;
import org.springframework.stereotype.Service;

@Service
public class ItemHistoryQueryBS extends HibernateBusiness {

  public PaginatedList<ItemHistory> find(String scope, Long entityId, DefaultParams params) {
    PaginatedList<ItemHistory> results = new PaginatedList<>();

    Criteria base = this.dao.newCriteria(ItemHistory.class)
      .add(Restrictions.eq("deleted", false))
      .add(Restrictions.eq("entityType", scope))
      .add(Restrictions.eq("entityId", entityId));

    Criteria count = this.dao.newCriteria(ItemHistory.class)
      .add(Restrictions.eq("deleted", false))
      .add(Restrictions.eq("entityType", scope))
      .add(Restrictions.eq("entityId", entityId))
      .setProjection(Projections.countDistinct("id"));

    if (params.hasTerm()) {
      String term = "%" + params.getTerm() + "%";
      base.add(Restrictions.or(
        Restrictions.like("changedField", term),
        Restrictions.like("responsibleUserName", term)
      ));
      count.add(Restrictions.or(
        Restrictions.like("changedField", term),
        Restrictions.like("responsibleUserName", term)
      ));
    }

    if (params.isSorting()) {
      base.addOrder(params.getSortOrder());
    } else {
      base.addOrder(Order.desc("changeDate"));
    }

    // Paginação
    int page = params.getPage();
    int size = params.getPageSize();
    base.setFirstResult((page-1) * size).setMaxResults(size);

    List<ItemHistory> list = this.dao.findByCriteria(base, ItemHistory.class);
    results.setList(list);
    results.setTotal((Long) count.uniqueResult());
    return results;
  }
}
