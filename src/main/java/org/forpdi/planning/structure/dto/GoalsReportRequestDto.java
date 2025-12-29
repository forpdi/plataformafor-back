package org.forpdi.planning.structure.dto;

import org.forpdi.planning.plan.PDIFilterParams;

public record GoalsReportRequestDto(
        Long macroId,
        Long planId,
        Long levelInstanceId,
        Long goalId,
        String startDate,
        String endDate,
        Boolean goalStatus,
        Integer progressStatus,
        Long companyId
) {
    /**
     * Constrói o DTO a partir do PDIFilterParams e do companyId.
     */
    public static GoalsReportRequestDto from(PDIFilterParams f, Long companyId) {
        return new GoalsReportRequestDto(
                f.getMacro(),
                f.getPlan(),
                f.getLevelInstance(),
                f.getGoalId(),
                f.getStartDate(),
                f.getEndDate(),
                f.getGoalStatus(),
                f.getProgressStatus(),
                companyId
        );
    }
}
