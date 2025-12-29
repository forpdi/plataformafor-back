package org.forpdi.planning.fields.dto;

import org.forpdi.core.common.PaginatedList;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;
class AttachmentListDtoTest {

    @Test
    void testCreateAttachmentListDto_WithNullPaginatedList() {
        PaginatedList<Double> paginatedList = null;

        AttachmentListDto dto = new AttachmentListDto(paginatedList);

        assertNull(dto.attachmentList(), "A 'attachmentList' deveria ser nula.");
    }
}

