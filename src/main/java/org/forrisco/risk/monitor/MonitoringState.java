package org.forrisco.risk.monitor;

import java.util.Arrays;
import java.util.Comparator;

public enum MonitoringState implements Comparator<MonitoringState> {
    UP_TO_DATE("em dia", "#83cab1", "Em dia", 1),
    CLOSE_TO_DUE("próximo a vencer", "#e2e470", "Próximos à Vencer", 2),
    LATE("atrasado", "#e97c66", "Atrasado", 3);

    private String value;
    private String colorHex;
    private String reportLabel;
    private Integer order;

    MonitoringState(String value, String colorHex, String reportLabel, Integer order) {
        this.value = value;
        this.colorHex = colorHex;
        this.reportLabel = reportLabel;
        this.order = order;
    }

    public String getColorHex() {
        return colorHex;
    }

    public String getReportLabel() {
        return reportLabel;
    }

    public Integer getOrder() {
        return order;
    }

    public static MonitoringState getMonitoringStateByValue(String value) {
        return Arrays.stream(MonitoringState.values())
                .filter(state -> state.value.equals(value))
                .findFirst()
                .orElse(null);
    }

    @Override
    public int compare(MonitoringState state1, MonitoringState state2) {
        return state1.order - state2.order;
    }
}