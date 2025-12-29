package org.forpdi.core.company;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.forpdi.core.common.Criteria;
import org.forpdi.core.common.Criterion;
import org.forpdi.core.common.HibernateBusiness;
import org.forpdi.core.common.HibernateDAO;
import org.forpdi.core.properties.SystemConfigs;
import org.forpdi.core.storage.file.Archive;
import org.forpdi.core.user.UserBS;
import org.forpdi.planning.document.DocumentBS;
import org.forpdi.planning.fields.FieldsBS;
import org.forpdi.planning.fields.budget.BudgetBS;
import org.forpdi.planning.fields.budget.BudgetElement;
import org.forpdi.planning.plan.PlanBS;
import org.forpdi.planning.structure.StructureBS;
import org.junit.jupiter.api.AfterAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BackupAndRestoreHelperTest {

    @InjectMocks
    private BackupAndRestoreHelper helper;

    @Mock private HibernateDAO dao;
    @Mock private Criteria criteria;

    @Mock private PlanBS planBS;
    @Mock private DocumentBS docBS;
    @Mock private StructureBS structureBS;
    @Mock private BudgetBS budgetBS;
    @Mock private CompanyBS companyBS;
    @Mock private UserBS userBS;
    @Mock private FieldsBS fieldsBS;
    @Mock private CompanyDomainContext domain;

    private Company company;

    // ------ suporte para TempFilesManager/SystemConfigs ------
    private static File testTempDir;
    private static String originalStorePdfs;

    @BeforeAll
    static void beforeAll() throws Exception {
        // Cria diretório temporário dedicado para testes que usam TempFilesManager
        testTempDir = new File(System.getProperty("java.io.tmpdir"), "forpdi-tests-backuprestore");
        if (!testTempDir.exists()) {
            assertTrue(testTempDir.mkdirs(), "Não foi possível criar diretório temporário de teste.");
        }
        // Guarda valor original e seta STORE_PDFS para o diretório válido
        originalStorePdfs = getStaticField(SystemConfigs.class, "STORE_PDFS");
        setStaticField(SystemConfigs.class, "STORE_PDFS", testTempDir.getAbsolutePath());
    }

    @AfterAll
    static void afterAll() throws Exception {
        // Restaura STORE_PDFS original
        setStaticField(SystemConfigs.class, "STORE_PDFS", originalStorePdfs);
        // Não removo a pasta para não interferir em testes paralelos; é /tmp
    }

    @BeforeEach
    void setUp() throws Exception {
        company = new Company();
        company.setId(123L);

        // Injeta o 'dao' no *superclass* (HibernateBusiness) via reflexão
        setFieldInSuperclass(helper, HibernateBusiness.class, "dao", dao);

        // Quando restore() chamar this.dao.execute(op), executar a operação transacional
        doAnswer(invocation -> {
            HibernateDAO.TransactionalOperation op = invocation.getArgument(0);
            op.execute(dao);
            return null;
        }).when(dao).execute(any());
    }



    @Test
    @DisplayName("zipAdd (privado): adiciona entrada com nome e conteúdo corretos (UTF-8)")
    void zipAdd_private_writesEntryWithNameAndContent() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            var method = BackupAndRestoreHelper.class.getDeclaredMethod(
                    "zipAdd", ZipOutputStream.class, String.class, String.class);
            method.setAccessible(true);
            method.invoke(helper, zos, "Foo", "{\"x\":1}");
        }
        Map<String,String> zip = unzip(baos.toByteArray());
        assertEquals(1, zip.size());
        assertEquals("{\"x\":1}", zip.get("Foo.json"));
    }

    // ---------------------- restore ----------------------

    @Test
    @DisplayName("restore: lança IllegalArgumentException quando empresa do contexto é nula")
    void restore_throwsWhenNoCompany() throws Exception {
        CompanyDomain cd = new CompanyDomain(); // sem company
        when(domain.get()).thenReturn(cd);

        MultipartFile file = mock(MultipartFile.class);
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream(emptyZip()));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> helper.restore(file));
        assertTrue(ex.getMessage().contains("criar uma instituição"), "Mensagem de erro inesperada: " + ex.getMessage());
    }

    @Test
    @DisplayName("restore: com ZIP vazio não persiste nenhuma entidade")
    void restore_withEmptyZip_doesNothing() throws Exception {
        CompanyDomain cd = new CompanyDomain();
        cd.setCompany(company);
        when(domain.get()).thenReturn(cd);

        MultipartFile file = mock(MultipartFile.class);
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream(emptyZip()));

        helper.restore(file);

        verify(dao, never()).persist(any(Serializable.class));
        verify(companyBS, never()).updateMessageOverlay(any(), anyString(), anyString());
    }

    @Test
    @DisplayName("restore: importa CompanyMessage chamando updateMessageOverlay para cada item")
    void restore_importsCompanyMessages() throws Exception {
        CompanyDomain cd = new CompanyDomain();
        cd.setCompany(company);
        when(domain.get()).thenReturn(cd);

        String msgsJson = "[{\"messageKey\":\"a\",\"messageValue\":\"1\"},{\"messageKey\":\"b\",\"messageValue\":\"2\"}]";
        Map<String,String> entries = Map.of("CompanyMessage.json", msgsJson);

        MultipartFile file = mock(MultipartFile.class);
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream(zip(entries)));

        helper.restore(file);

        verify(companyBS, times(1)).updateMessageOverlay(eq(company), eq("a"), eq("1"));
        verify(companyBS, times(1)).updateMessageOverlay(eq(company), eq("b"), eq("2"));
        verify(dao, never()).persist(any(Serializable.class));
    }

    @Test
    @DisplayName("restore: importa BudgetElement definindo company e limpando id")
    void restore_importsBudgetElements() throws Exception {
        CompanyDomain cd = new CompanyDomain();
        cd.setCompany(company);
        when(domain.get()).thenReturn(cd);

        String json = "[{\"id\":101}]";
        Map<String,String> entries = Map.of("BudgetElement.json", json);

        MultipartFile file = mock(MultipartFile.class);
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream(zip(entries)));

        helper.restore(file);

        ArgumentCaptor<Serializable> captor = ArgumentCaptor.forClass(Serializable.class);
        verify(dao, atLeastOnce()).persist(captor.capture());

        boolean found = false;
        for (Serializable obj : captor.getAllValues()) {
            if (obj instanceof BudgetElement) {
                BudgetElement be = (BudgetElement) obj;
                assertNull(be.getId(), "id deve ser nulo antes do persist.");
                assertEquals(company, be.getCompany(), "Company do BudgetElement deve ser a do contexto.");
                found = true;
            }
        }
        assertTrue(found, "Nenhum BudgetElement foi persistido.");
    }

    // ---------------------- infra privada ----------------------

    @Test
    @DisplayName("readFromFile: retorna \"[]\" quando arquivo não existe no mapa")
    void readFromFile_returnsEmptyArrayWhenMissing() throws Exception {
        var method = BackupAndRestoreHelper.class.getDeclaredMethod("readFromFile", Map.class, Class.class);
        method.setAccessible(true);
        String content = (String) method.invoke(helper, Collections.emptyMap(), CompanyMessage.class);
        assertEquals("[]", content);
    }

    @Test
    @DisplayName("readFromFile: retorna conteúdo do arquivo quando presente")
    void readFromFile_returnsContentWhenPresent() throws Exception {
        File tmp = File.createTempFile("CompanyMessage", ".json", testTempDir);
        try (FileOutputStream fos = new FileOutputStream(tmp)) {
            fos.write("[{\"messageKey\":\"k\",\"messageValue\":\"v\"}]".getBytes(StandardCharsets.UTF_8));
        }
        Map<String, File> files = Map.of("CompanyMessage", tmp);

        var method = BackupAndRestoreHelper.class.getDeclaredMethod("readFromFile", Map.class, Class.class);
        method.setAccessible(true);
        String content = (String) method.invoke(helper, files, CompanyMessage.class);
        assertTrue(content.contains("\"messageKey\""));
    }

    @Test
    @DisplayName("uncompress: extrai entradas do ZIP para arquivos temporários com chave pelo nome simples da classe")
    void uncompress_extractsZipEntries() throws Exception {
        Map<String,String> entries = Map.of(
            "Document.json", "[{\"id\":1}]",
            "PlanMacro.json", "[{\"id\":10}]"
        );
        byte[] data = zip(entries);

        var method = BackupAndRestoreHelper.class.getDeclaredMethod("uncompress", InputStream.class);
        method.setAccessible(true);

        @SuppressWarnings("unchecked")
        Map<String, File> files = (Map<String, File>) method.invoke(helper, new ByteArrayInputStream(data));

        assertTrue(files.containsKey("Document"));
        assertTrue(files.containsKey("PlanMacro"));
        assertTrue(files.get("Document").exists());

        var del = BackupAndRestoreHelper.class.getDeclaredMethod("deleteTempFiles", Iterable.class);
        del.setAccessible(true);
        del.invoke(helper, files.values());
    }

    // ---------------------- listFilebyName ----------------------

    @Test
    @DisplayName("listFilebyName: retorna Archive quando encontrado")
    void listFilebyName_returnsArchive() {
        Archive archive = new Archive();
        when(dao.newCriteria(Archive.class)).thenReturn(criteria);
        when(criteria.add(any(Criterion.class))).thenReturn(criteria);
        when(criteria.uniqueResult()).thenReturn(archive);

        Archive result = helper.listFilebyName("relatorio.pdf");
        assertSame(archive, result);
        verify(criteria, times(2)).add(any(Criterion.class));
        verify(criteria, times(1)).setMaxResults(1);
    }

    @Test
    @DisplayName("listFilebyName: retorna null quando não encontrado")
    void listFilebyName_returnsNull() {
        when(dao.newCriteria(Archive.class)).thenReturn(criteria);
        when(criteria.add(any(Criterion.class))).thenReturn(criteria);
        when(criteria.uniqueResult()).thenReturn(null);

        Archive result = helper.listFilebyName("inexistente.pdf");
        assertNull(result);
    }

    // ---------------------- util ----------------------

    /** Cria um ZIP em memória com entradas (nome → conteúdo). */
    private static byte[] zip(Map<String,String> entries) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            for (Map.Entry<String,String> e : entries.entrySet()) {
                ZipEntry ze = new ZipEntry(e.getKey());
                zos.putNextEntry(ze);
                zos.write(e.getValue().getBytes(StandardCharsets.UTF_8));
                zos.closeEntry();
            }
        }
        return baos.toByteArray();
    }

    /** ZIP vazio (sem entradas). */
    private static byte[] emptyZip() throws IOException {
        return zip(Collections.emptyMap());
    }

    /** Lê o ZIP em memória e devolve mapa de (nome → conteúdo). */
    private static Map<String,String> unzip(byte[] zipBytes) throws IOException {
        Map<String,String> out = new LinkedHashMap<>();
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipBytes))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buf = new byte[512];
                int r;
                while ((r = zis.read(buf)) > 0) {
                    baos.write(buf, 0, r);
                }
                out.put(entry.getName(), baos.toString(StandardCharsets.UTF_8));
                zis.closeEntry();
            }
        }
        return out;
    }

    // ------------ helpers de reflexão para campos estáticos e de superclasse ------------

    private static void setFieldInSuperclass(Object target, Class<?> superclass, String fieldName, Object value) throws Exception {
        Field f = superclass.getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }

    private static void setStaticField(Class<?> clazz, String fieldName, String value) throws Exception {
        Field f = clazz.getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(null, value);
    }

    private static String getStaticField(Class<?> clazz, String fieldName) throws Exception {
        Field f = clazz.getDeclaredField(fieldName);
        f.setAccessible(true);
        Object v = f.get(null);
        return (v == null) ? null : v.toString();
    }
}
