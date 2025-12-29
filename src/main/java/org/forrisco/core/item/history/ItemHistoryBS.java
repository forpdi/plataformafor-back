package org.forrisco.core.item.history;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.forpdi.core.common.HibernateBusiness;
import org.forpdi.core.user.User;
import org.forrisco.core.item.FieldItem;
import org.forrisco.core.item.FieldSubItem;
import org.forrisco.core.item.Item;
import org.forrisco.core.item.PlanRiskItem;
import org.forrisco.core.item.PlanRiskItemField;
import org.forrisco.core.item.PlanRiskSubItem;
import org.forrisco.core.item.PlanRiskSubItemField;
import org.forrisco.core.item.SubItem;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;

@Service
public class ItemHistoryBS extends HibernateBusiness {

  public void saveItemChanges(Item oldItem, Item newItem, List<FieldItem> oldFields, List<FieldItem> newFields, User responsible) {
    Date now = new Date();
    Long entityId = oldItem.getId();
    addIfChanged(ItemHistoryScope.ITEM, entityId, "Título", oldItem.getName(), newItem.getName(), responsible, now);
    addIfChanged(ItemHistoryScope.ITEM, entityId, "Descrição", nz(oldItem.getDescription()), nz(newItem.getDescription()), responsible, now);

    diffFields(
      ItemHistoryScope.ITEM, entityId,
      oldFields, newFields,
      FieldItem::getName,
      ItemHistoryBS::fieldKind,
      ItemHistoryBS::fieldOldValue,
      ItemHistoryBS::fieldNewValue,
      responsible, now
    );
  }

  public void saveSubItemChanges(SubItem oldSub, SubItem newSub, List<FieldSubItem> oldFields, List<FieldSubItem> newFields, User responsible) {
    Date now = new Date();
    Long entityId = oldSub.getId();
    addIfChanged(ItemHistoryScope.SUBITEM, entityId, "Título", oldSub.getName(), newSub.getName(), responsible, now);
    addIfChanged(ItemHistoryScope.SUBITEM, entityId, "Descrição", nz(oldSub.getDescription()), nz(newSub.getDescription()), responsible, now);

    diffFields(
      ItemHistoryScope.SUBITEM, entityId,
      oldFields, newFields,
      FieldSubItem::getName,
      ItemHistoryBS::subFieldKind,
      ItemHistoryBS::subFieldOldValue,
      ItemHistoryBS::subFieldNewValue,
      responsible, now
    );
  }

  public void savePlanRiskItemChanges(PlanRiskItem oldItem, PlanRiskItem newItem, List<PlanRiskItemField> oldFields, List<PlanRiskItemField> newFields, User responsible) {
    Date now = new Date();
    Long entityId = oldItem.getId();
    addIfChanged(ItemHistoryScope.PLAN_RISK_ITEM, entityId, "Título", oldItem.getName(), newItem.getName(), responsible, now);
    addIfChanged(ItemHistoryScope.PLAN_RISK_ITEM, entityId, "Descrição", nz(oldItem.getDescription()), nz(newItem.getDescription()), responsible, now);

    diffFields(
      ItemHistoryScope.PLAN_RISK_ITEM, entityId,
      oldFields, newFields,
      PlanRiskItemField::getName,
      ItemHistoryBS::prFieldKind,
      ItemHistoryBS::prFieldOldValue,
      ItemHistoryBS::prFieldNewValue,
      responsible, now
    );
  }

  public void savePlanRiskSubItemChanges(PlanRiskSubItem oldSub, PlanRiskSubItem newSub, List<PlanRiskSubItemField> oldFields, List<PlanRiskSubItemField> newFields, User responsible) {
    Date now = new Date();
    Long entityId = oldSub.getId();
    addIfChanged(ItemHistoryScope.PLAN_RISK_SUBITEM, entityId, "Título", oldSub.getName(), newSub.getName(), responsible, now);
    addIfChanged(ItemHistoryScope.PLAN_RISK_SUBITEM, entityId, "Descrição", nz(oldSub.getDescription()), nz(newSub.getDescription()), responsible, now);

    diffFields(
      ItemHistoryScope.PLAN_RISK_SUBITEM, entityId,
      oldFields, newFields,
      PlanRiskSubItemField::getName,
      ItemHistoryBS::prsFieldKind,
      ItemHistoryBS::prsFieldOldValue,
      ItemHistoryBS::prsFieldNewValue,
      responsible, now
    );
  }

  private <F> void diffFields(
      ItemHistoryScope scope, Long entityId,
      List<F> oldFields, List<F> newFields,
      Function<F, String> getName,
      Function<F, String> kindLabel,
      Function<F, String> getOldValue,
      Function<F, String> getNewValue,
      User responsible, Date when) {

    Map<String, F> oldByName = mapByName(oldFields, getName);
    Map<String, F> curByName = mapByName(newFields, getName);

    for (String name : oldByName.keySet()) {
      if (!curByName.containsKey(name)) {
        F of = oldByName.get(name);
        add(scope, entityId,
          "Campo removido: " + name + " (" + kindLabel.apply(of) + ")",
          getOldValue.apply(of), null, responsible, when);
      }
    }

    for (String name : curByName.keySet()) {
      F nf = curByName.get(name);
      if (!oldByName.containsKey(name)) {
        add(scope, entityId,
          "Campo adicionado: " + name + " (" + kindLabel.apply(nf) + ")",
          null, getNewValue.apply(nf), responsible, when);
      } else {
        F of = oldByName.get(name);
        String oldKind = kindLabel.apply(of);
        String newKind = kindLabel.apply(nf);
        if (!Objects.equals(oldKind, newKind)) {
          add(scope, entityId,
            "Tipo do campo: " + name,
            oldKind, newKind, responsible, when);
        }
        String ov = getOldValue.apply(of);
        String nv = getNewValue.apply(nf);
        if (!Objects.equals(nz(ov), nz(nv))) {
          add(scope, entityId,
            "Valor do campo: " + name,
            ov, nv, responsible, when);
        }
      }
    }
  }

  private <F> Map<String, F> mapByName(List<F> list, Function<F, String> getName) {
    if (list == null) return Collections.emptyMap();
    return list.stream().filter(Objects::nonNull).collect(Collectors.toMap(
      f -> nz(getName.apply(f)), Function.identity(), (a, b) -> a));
  }

  private static String nz(String s) { return s == null ? "" : s; }

  private static String fieldKind(FieldItem f) { return f.isText() ? "Texto" : "Arquivo"; }
  private static String fieldOldValue(FieldItem f) { return f.isText() ? txt(f.getValue(), f.getDescription()) : file(f.getFileLink()); }
  private static String fieldNewValue(FieldItem f) { return fieldOldValue(f); }

  private static String subFieldKind(FieldSubItem f) { return f.isText() ? "Texto" : "Arquivo"; }
  private static String subFieldOldValue(FieldSubItem f) { return f.isText() ? txt(f.getValue(), f.getDescription()) : file(f.getFileLink()); }
  private static String subFieldNewValue(FieldSubItem f) { return subFieldOldValue(f); }

  private static String prFieldKind(PlanRiskItemField f) { return f.isText() ? "Texto" : "Arquivo"; }
  private static String prFieldOldValue(PlanRiskItemField f) { return f.isText() ? txt(f.getValue(), f.getDescription()) : file(f.getFileLink()); }
  private static String prFieldNewValue(PlanRiskItemField f) { return prFieldOldValue(f); }

  private static String prsFieldKind(PlanRiskSubItemField f) { return f.isText() ? "Texto" : "Arquivo"; }
  private static String prsFieldOldValue(PlanRiskSubItemField f) { return f.isText() ? txt(f.getValue(), f.getDescription()) : file(f.getFileLink()); }
  private static String prsFieldNewValue(PlanRiskSubItemField f) { return prsFieldOldValue(f); }

  private static String txt(String value, String descriptionFallback) {
    String v = (value != null && !value.isBlank()) ? value : descriptionFallback;
    return v == null ? "" : v;
  }
  private static String file(String fileLink) {
    return (fileLink == null || fileLink.isBlank()) ? "" : "Arquivo: " + fileLink;
  }

  private static String clean(String s) {
    if (s == null) return "";
    return Jsoup.parse(s).text().trim();
  }

  private void addIfChanged(ItemHistoryScope scope, Long entityId, String label, String oldV, String newV, User user, Date when) {
    String ov = clean(oldV);
    String nv = clean(newV);
    if (!Objects.equals(ov, nv)) {
      add(scope, entityId, label, ov, nv, user, when);
    }
  }

  private void add(ItemHistoryScope scope, Long entityId, String field, String oldV, String newV, User user, Date when) {
    ItemHistory h = new ItemHistory();
    h.setEntityType(scope.name());
    h.setEntityId(entityId);
    h.setChangedField(field);
    h.setOldValue(clean(oldV));
    h.setNewValue(clean(newV));
    h.setResponsibleUser(user);
    h.setResponsibleUserName(user != null ? user.getName() : null);
    h.setChangeDate(when);
    h.setDeleted(false);
    this.persist(h);
  }
}
