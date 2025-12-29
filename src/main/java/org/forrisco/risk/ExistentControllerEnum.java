package org.forrisco.risk;

public enum ExistentControllerEnum {

    NO_CONTROLS(1, "Não há procedimentos de controle"),
    INSUFFICIENT_CONTROLS(2, "Há procedimentos de controles, mas são insuficientes"),
    FORMALIZED_BUT_INSUFFICIENT(3,
            "Todos os controles existentes estão formalizados, mas são insuficientes para o nível de risco"),
    ADEQUATE_NOT_FORMALIZED(4,
            "Os controles existentes são adequados (suficientes) ao nível de risco, mas nem todos (ou nenhum) estão formalizados"),
    ADEQUATE_AND_FORMALIZED(5, "Há procedimentos de controle adequados (suficientes) e todos estão formalizados");

    private final int id;
    private final String label;

    private ExistentControllerEnum(int id, String label) {
        this.id = id;
        this.label = label;
    }

    public int getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public static ExistentControllerEnum GetById(int id) {
        for (ExistentControllerEnum existentControllerEnum : values()) {
            if (existentControllerEnum.id == id) {
                return existentControllerEnum;
            }
        }

        throw new IllegalArgumentException("Unrecognized ExistentController id: " + id);
    }

    public static ExistentControllerEnum GetByLabel(String label) {
        for (ExistentControllerEnum existentControllerEnum : values()) {
            if (existentControllerEnum.label.equals(label)) {
                return existentControllerEnum;
            }
        }

        throw new IllegalArgumentException("Unrecognized ExistentController label: " + label);
    }
}
