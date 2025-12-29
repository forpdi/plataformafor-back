package org.forrisco.risk;

public enum ExecutionControllerEnum {
    NOT_APPLICABLE(1, "Não se aplica"),
    NOT_EXECUTED(2, "Os controles existentes não são executados"),
    PARTIALLY_EXECUTED(3, "Os controles existentes estão sendo parcialmente executados (alguns sim, outros não)"),
    EXECUTED_WITHOUT_EVIDENCE(4,
            "Todos os controles existentes são executados, mas não há evidência (registros) de sua realização"),
    EXECUTED_WITH_EVIDENCE(5,
            "Todos os controles existentes são executados e têm evidência (registros) de sua realização");

    private final int id;
    private final String label;

    private ExecutionControllerEnum(int id, String label) {
        this.id = id;
        this.label = label;
    }

    public int getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public static ExecutionControllerEnum GetById(int id) {
        for (ExecutionControllerEnum e : values()) {
            if (e.id == id) {
                return e;
            }
        }
        throw new IllegalArgumentException("Id inválido para ExecutionControllerEnum: " + id);
    }

    public static ExecutionControllerEnum GetByLabel(String label) {
        for (ExecutionControllerEnum e : values()) {
            if (e.label.equals(label)) {
                return e;
            }
        }
        throw new IllegalArgumentException("Label inválido para ExecutionControllerEnum: " + label);
    }
}
