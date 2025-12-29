package org.forpdi.core.storage.file;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.forpdi.core.common.AbstractController;
import org.forpdi.core.company.Company;
import org.forpdi.core.company.CompanyBS;
import org.forpdi.core.company.Company;
import org.forpdi.core.company.CompanyBS;
import org.forpdi.core.company.CompanyDomainContext;
import org.forpdi.core.storage.StorageService;
import org.forpdi.core.storage.dto.DownloadFileRequestDto;
import org.forpdi.core.utils.Consts;
import org.forpdi.security.auth.UserSession;
import org.forpdi.security.authz.AccessLevels;
import org.forpdi.security.auth.UserSession;
import org.forpdi.security.authz.AccessLevels;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Erick Alves
 *
 */
@RestController
public class FileController extends AbstractController {

    private static final long MAX_SIZE_BYTES = 2 * 1024 * 1024;

    private static final Set<String> ALLOWED_EXT = Set.of(
            // Imagens
            "gif", "jpg", "jpeg", "jpg2", "jp2", "bmp", "tiff", "png", "ai", "psd", "svg", "svgz",
            // Documentos
            "pdf", "doc", "docx", "odt", "rtf", "txt", "xml", "xlsx", "xls", "ods", "csv", "ppt", "pptx", "ppsx", "odp",
            // Áudio
            "mp3", "wav", "wma", "ogg", "aac",
            // Vídeos
            "avi", "mov", "wmv", "mp4", "flv", "mkv",
            // Arquivos compactados
            "zip", "rar", "7z", "tar", "gz", "bz2"
    );

    @Autowired
    private CompanyDomainContext domain;
    @Autowired
    private ArchiveBS archiveBS;
    @Autowired
    private StorageService storageService;
    @Autowired
    private CompanyBS companyBS;

    /**
	 * Upload de arquivo
	 * 
	 * @param void.
	 * @return String Url que representa a foto do usuário.
	 */

    @PostMapping(Consts.FILES_ENDPOINT_BASE_URL + "/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file, UserSession userSession) {
        try {
            if (userSession == null || !userSession.isLogged()) {
                return this.fail("Sessão inválida ou não autenticada.");
            }

            boolean isSystemAdmin = userSession.getAccessLevel() >= AccessLevels.SYSTEM_ADMIN.getLevel();
            Company targetCompany = isSystemAdmin
                    ? domain.get().getCompany()
                    : companyBS.findCompanyByUserId(userSession.getAuthData().getId());

            if (targetCompany == null) {
                return this.fail("Acesso negado. Empresa não identificada para o upload.");
            }
            if (file.isEmpty()) {
                LOGGER.error("Upload rejeitado: arquivo vazio.");
                return this.fail("Arquivo vazio.");
            }
            if (file.getSize() > MAX_SIZE_BYTES) {
                LOGGER.error("Upload rejeitado: tamanho excedido ({} bytes).", file.getSize());
                return this.fail("Arquivo excede o tamanho máximo de 2MB.");
            }

            String originalName = file.getOriginalFilename();
            if (originalName == null) {
                originalName = "arquivo";
            }
            originalName = originalName.replaceAll("[^a-zA-Z0-9._-]", "_");

            String ext = FilenameUtils.getExtension(originalName).toLowerCase();
            if (!ALLOWED_EXT.contains(ext)) {
                LOGGER.error("Upload bloqueado: extensão '{}' não permitida (nome original='{}').", ext, originalName);
                return this.fail("Tipo de arquivo não permitido.");
            }

            if (List.of("png", "jpg", "jpeg", "gif", "svg").contains(ext)) {
                String realMime = detectImageMime(file);
                boolean ok = (ext.equals("png") && "image/png".equals(realMime))
                        || ((ext.equals("jpg") || ext.equals("jpeg")) && "image/jpeg".equals(realMime))
                        || (ext.equals("gif") && "image/gif".equals(realMime))
                        || (ext.equals("svg") && "image/svg+xml".equals(realMime));
                if (!ok) {
                    LOGGER.error("Upload de imagem inválido: nome='{}', ext='{}', mimeDetect='{}'.", originalName, ext, realMime);
                    return this.fail("Conteúdo do arquivo de imagem inválido.");
                }
            }

            String baseName = FilenameUtils.getBaseName(originalName);
            String finalName = baseName + "." + ext;

            Archive archive = new Archive();
            archive.setCompany(targetCompany);
            archive.setName(finalName);
            Archive saved = archiveBS.save(archive);

            String storageFileName = saved.getId() + "." + ext;
            String contentType = file.getContentType() != null ? file.getContentType() : "application/octet-stream";

            try (InputStream in = file.getInputStream()) {
                storageService.uploadFile(in, contentType, storageFileName, file.getSize());
            }

            return this.success(storageFileName);
        } catch (Exception ex) {
            LOGGER.error("Erro ao fazer upload do arquivo.", ex);
            return this.fail("Falha ao fazer upload do arquivo: " + ex.getMessage());
        }
    }

    @PostMapping(Consts.FILES_ENDPOINT_BASE_URL)
    public ResponseEntity<?> downloadFile(@RequestBody DownloadFileRequestDto request, UserSession userSession) {
		String file = request.file();
		try {
            Long archiveId = file.contains(".")
                    ? Long.parseLong(FilenameUtils.getBaseName(file))
                    : Long.parseLong(file);

            Archive archive = archiveBS.exists(archiveId, Archive.class);
            if (archive == null) {
                LOGGER.warn("Arquivo com ID {} não encontrado.", archiveId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Arquivo não encontrado.");
            }

            if (userSession.isLogged()) {
                boolean isSystemAdmin = (userSession.getAccessLevel() >= AccessLevels.SYSTEM_ADMIN.getLevel());
                if (!isSystemAdmin) {
                    Long loggedInUserId = userSession.getAuthData().getId();
                    Company userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);

                    if (userCompany == null || !archive.getCompany().getId().equals(userCompany.getId())) {
                        LOGGER.warn(
                            "Tentativa de acesso negado ao arquivo ID {}. Usuário ID {} não pertence à empresa do arquivo.",
                            archiveId, loggedInUserId);
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acesso não autorizado ao arquivo.");
                    }
                }
            } else {
                if (!archive.getCompany().getId().equals(this.domain.get().getCompany().getId())) {
                    LOGGER.warn("Tentativa de acesso anônimo não autorizado ao arquivo ID {} pela empresa ID {}.",
                                archiveId, this.domain.get().getCompany().getId());
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acesso não autorizado ao arquivo.");
                }
            }

            InputStream fileStream = storageService.retrieveFile(archive.getFileLink());
            InputStreamResource resource = new InputStreamResource(fileStream);

            String ext = archive.getExtension().toLowerCase();
            String contentType = switch (ext) {
                case "gif" ->
                    "image/gif";
                case "jpg", "jpeg" ->
                    "image/jpeg";
                case "png" ->
                    "image/png";
                case "svg" ->
                    "image/svg+xml";
                default ->
                    "application/octet-stream";
            };

            String disposition = ContentDisposition.builder("inline")
                    .filename(archive.getName(), StandardCharsets.UTF_8)
                    .build()
                    .toString();

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, disposition)
                    .body(resource);

        } catch (NumberFormatException e) {
            LOGGER.error("Formato inválido para o parâmetro 'file': {}", file, e);
            return ResponseEntity.badRequest().body("ID de arquivo inválido.");
        } catch (IOException e) {
            LOGGER.error("Erro ao recuperar o arquivo: {}", file, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro interno ao tentar recuperar o arquivo.");
        } catch (Exception e) {
            LOGGER.error("Erro inesperado ao baixar o arquivo: {}", file, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro inesperado ao processar o download.");
        }
    }

    private String detectImageMime(MultipartFile file) throws IOException {
        byte[] header;
        try (InputStream in = file.getInputStream()) {
            header = in.readNBytes(512);
        }
        if (header.length >= 8
                && header[0] == (byte) 0x89 && header[1] == 0x50 && header[2] == 0x4E && header[3] == 0x47) {
            return "image/png";
        }
        if (header.length >= 2
                && header[0] == (byte) 0xFF && header[1] == (byte) 0xD8) {
            return "image/jpeg";
        }
        if (header.length >= 6) {
            String sig = new String(header, 0, 6, java.nio.charset.StandardCharsets.US_ASCII);
            if ("GIF87a".equals(sig) || "GIF89a".equals(sig)) {
                return "image/gif";
            }
        }
        String text = new String(header, java.nio.charset.StandardCharsets.UTF_8).toLowerCase();
        if (text.contains("<svg")) {
            return "image/svg+xml";
        }
        return null;
    }
}
