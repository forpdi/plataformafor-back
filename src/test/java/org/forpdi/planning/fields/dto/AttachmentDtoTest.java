package org.forpdi.planning.fields.dto;

import org.forpdi.planning.fields.attachment.Attachment;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class AttachmentDtoTest {

    @Test
    @DisplayName("Criação do DTO de anexo")
    public void testAttachmentDtoCreation() {

        Attachment attachment = new Attachment();

        AttachmentDto dto = new AttachmentDto(attachment);

        assertEquals(attachment, dto.attachment(), "O attachment no DTO não corresponde ao esperado.");
    }
} 

