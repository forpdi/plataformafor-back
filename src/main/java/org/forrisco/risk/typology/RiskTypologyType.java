package org.forrisco.risk.typology;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public enum RiskTypologyType implements Comparator<RiskTypologyType> {
    OPERATIONAL(1, "Risco Operacional", "#ed3737"),
    IMAGE_REPUTATION(2, "Risco de imagem/reputação de órgão", "#ef8a49"),
    LEGAL(3, "Risco legal", "#f6cd2b"),
    BUDGET_FINANCIAL(4, "Risco financeiro/orçamentário", "#cce655"),
    INTEGRITY(5, "Risco de Integridade", "#79cbc1"),
    OTHER(6, "Outras", "#9ec3f4");
    
    RiskTypologyType(Integer id, String value, String colorHex) {
        this.id = id;
        this.value = value;
        this.colorHex = colorHex;
    }
    
    private Integer id;
    private String value;
    private String colorHex;
    
    public String getValue() {
        return value;
    }

    public String getColorHex() {
        return colorHex;
    }
    
    public Integer getId() {
        return id;
    }
    
    public static List<RiskTypologyType> getRiskTypologyTypesByValue(String value) {
        if (value == null) {
            return new ArrayList<>();
        }
        
        String typologiesDelimiter = ";";
        String[] tipologies = value.split(typologiesDelimiter, 0);
        
        return Arrays.stream(tipologies)
            .map(typology -> getRiskTypologyTypeByValue(typology))
            .collect(Collectors.toList());
    }
    
    public static RiskTypologyType getRiskTypologyTypeByValue(String value) {
        return Arrays.stream(RiskTypologyType.values())
            .filter(type -> type.value.equals(value))
            .findFirst()
            .orElse(null);
    }

    @Override
    public int compare(RiskTypologyType riskTypology, RiskTypologyType otherRiskTypology) {
        return riskTypology.id - otherRiskTypology.id;
    }
}
