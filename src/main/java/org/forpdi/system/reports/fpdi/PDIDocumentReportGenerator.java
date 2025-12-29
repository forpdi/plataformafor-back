package org.forpdi.system.reports.fpdi;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.forpdi.core.common.PaginatedList;
import org.forpdi.core.user.User;
import org.forpdi.core.user.UserBS;
import org.forpdi.core.utils.Util;
import org.forpdi.planning.attribute.Attribute;
import org.forpdi.planning.attribute.AttributeHelper;
import org.forpdi.planning.attribute.AttributeInstance;
import org.forpdi.planning.attribute.types.Currency;
import org.forpdi.planning.attribute.types.NumberField;
import org.forpdi.planning.attribute.types.Percentage;
import org.forpdi.planning.attribute.types.ResponsibleField;
import org.forpdi.planning.attribute.types.ScheduleField;
import org.forpdi.planning.attribute.types.SelectPlan;
import org.forpdi.planning.attribute.types.TableField;
import org.forpdi.planning.attribute.types.TextArea;
import org.forpdi.planning.attribute.types.enums.FormatValue;
import org.forpdi.planning.document.DocumentAttribute;
import org.forpdi.planning.document.DocumentBS;
import org.forpdi.planning.document.DocumentSection;
import org.forpdi.planning.fields.FieldsBS;
import org.forpdi.planning.fields.budget.BudgetDTO;
import org.forpdi.planning.fields.schedule.ScheduleInstance;
import org.forpdi.planning.fields.table.TableFields;
import org.forpdi.planning.fields.table.TableInstance;
import org.forpdi.planning.fields.table.TableStructure;
import org.forpdi.planning.fields.table.TableValues;
import org.forpdi.planning.plan.Plan;
import org.forpdi.planning.plan.PlanBS;
import org.forpdi.planning.structure.StructureBS;
import org.forpdi.planning.structure.StructureHelper;
import org.forpdi.planning.structure.StructureLevelInstance;
import org.forpdi.system.reports.ReportGenerator;
import org.forpdi.system.reports.ReportGeneratorHelper;
import org.forpdi.system.reports.ReportGeneratorParams;
import org.forpdi.system.reports.pdf.PDFSettings;
import org.forpdi.system.reports.pdf.PdfWriterFactory;
import org.forpdi.system.reports.pdf.TOCEvent;
import org.forpdi.system.reports.pdf.htmlparser.HtmlToPdfParser;
import org.forpdi.system.reports.pdf.table.TempFilesManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.itextpdf.text.BadElementException;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BadPdfFormatException;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.CMYKColor;
import com.itextpdf.text.pdf.PdfAction;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.draw.DottedLineSeparator;


@Component
public class PDIDocumentReportGenerator implements ReportGenerator {
	
	@Autowired
	private PlanBS planBS;
	@Autowired
	private DocumentBS docBS;
	@Autowired
	StructureHelper structHelper;
	@Autowired
	private FieldsBS fieldsBS;
	@Autowired
	private UserBS userBS;
	@Autowired
	private StructureBS structureBS;
	@Autowired
	private AttributeHelper attrHelper;
	@Autowired
	private ReportGeneratorHelper reportGeneratorHelper;

	protected final Font mainFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16.0f, BaseColor.BLACK);
    protected final Font tableMainFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12.0f, BaseColor.BLACK);
    protected final Font tableSecondaryFont = FontFactory.getFont(FontFactory.HELVETICA, 12.0f, BaseColor.BLACK);
	
	@Override
	public InputStream generateReport(ReportGeneratorParams params) {
		try {
			Params parsedParams = ReportGeneratorParams.extractParams(params, Params.class);
			return onGenerate(parsedParams.title, parsedParams.lista);
		} catch (DocumentException | IOException e) {
			throw new RuntimeException(e);
		}
	}


	private InputStream onGenerate(String title, String lista) throws DocumentException, FileNotFoundException, BadElementException,
			MalformedURLException, IOException, BadPdfFormatException {
		Document document = new Document();
		Document coverDocument = new Document();
		Document preTextDocument = new Document();
		Document summaryDocument = new Document();

		File outputDir = TempFilesManager.getTempDir();

		final String prefix = String.format("fpdi-doc-export-%d", System.currentTimeMillis());

		File coverPdfFile = new File(outputDir, String.format("%s-cover.pdf", prefix));
		File preTextPdfFile = new File(outputDir, String.format("%s-pre-text.pdf", prefix));
		File summaryPdfFile = new File(outputDir, String.format("%s-summary.pdf", prefix));
		File finalSummaryPdfFile = new File(outputDir, String.format("%s-final-summary.pdf", prefix));
		File contentFile = new File(outputDir, String.format("%s-content.pdf", prefix));
		File destinationFile = new File(outputDir, String.format("%s-mounted.pdf", prefix));
		File finalPdfFile = new File(outputDir, String.format("%s-final.pdf", prefix));

		TOCEvent event = new TOCEvent();
		PdfWriterFactory.create(document, contentFile, event);
		PdfWriterFactory.create(preTextDocument, preTextPdfFile);
		PdfWriterFactory.create(summaryDocument, summaryPdfFile);

        Map<String, String> coverTextContent = new HashMap<>();
        coverTextContent.put("Documento de PDI", "");
		reportGeneratorHelper.generateCover(coverPdfFile, title, coverTextContent);

        String[] sections = lista != null ? lista.split(",") : new String[0];
		int secIndex = 0, subSecIndex = 0;

		boolean lastAttWasPlan = false;

		reportGeneratorHelper.setDimensions(document);
		document.open();
		document.add(new Chunk(""));

        reportGeneratorHelper.setDimensions(preTextDocument);
		preTextDocument.open();

		boolean havePreText = false;
		boolean haveContent = false;
		
		FileWriter fw = null;
		BufferedWriter conexao = null;

		try {
			for (int i = 0; i < sections.length; i++) {
				DocumentSection ds = this.docBS.retrieveSectionById(Long.parseLong(sections[i]));
				ds.setDocumentAttributes(this.docBS.listAttributesBySection(ds, ds.getDocument().getPlan().getId()));
	
				subSecIndex = 0;
				String secName = ds.getName();
	
				if (ds.isPreTextSection()) { // SEÇÕES PRÉ TEXTUAIS
	
					for (DocumentAttribute a : ds.getDocumentAttributes()) {
						if (a.getType().equals(TableField.class.getCanonicalName())) {
							havePreText = true;
							TableFields tf = fieldsBS.tableFieldsByAttribute(a.getId(), true);
							List<TableStructure> tabStructList = fieldsBS.listTableStructureByFields(tf);
							List<TableInstance> tabInstList = fieldsBS.listTableInstanceByFields(tf);
							if (!tabInstList.isEmpty()) {
								// String attName = a.getName();
								// if (!attName.equals(secName)) {
	
								Chunk c = new Chunk(secName, PDFSettings.TITLE_FONT);
								c.setGenericTag(secName);
	
								Paragraph attTitle = new Paragraph(c);
								attTitle.setAlignment(Element.ALIGN_CENTER);
								attTitle.setLeading(PDFSettings.INTER_LINE_SPACING);
								attTitle.setSpacingAfter(PDFSettings.PARAGRAPH_SPACING);
								attTitle.setSpacingBefore(PDFSettings.PARAGRAPH_SPACING);
								preTextDocument.add(attTitle);
								// }
								PdfPTable table;
								if (tabStructList.size() == 4) /**
																 * SOLUÇÃO DE CONTORNO PARA TRATAR O CASO ESPECIAL DO
																 * HISTÓRICO DE VERSÕES QUE É UMA SEÇÃO PRÉ-TEXTUAL MAS DEVE
																 * EXIBIR BORDA E CABEÇALHO, NECESSITA DE MELHORIA
																 */
									table = returnPdfPTable(tabStructList, tabInstList, false, false);
								else
									table = returnPdfPTable(tabStructList, tabInstList, true, true);
								preTextDocument.add(table);
								preTextDocument.newPage();
								lastAttWasPlan = false;
							}
						}
					}
	
				} else {
					haveContent = true;
					// SEÇÕES NUMERADAS
					/*
					 * if (lastSecWasPreText) { lastSecWasPreText = false;
					 * 
					 * Paragraph secTitle = new Paragraph("Sumário", titulo);
					 * secTitle.setLeading(interLineSpacing);
					 * secTitle.setSpacingAfter(paragraphSpacing);
					 * secTitle.setSpacingBefore(paragraphSpacing);
					 * secTitle.setAlignment(Element.ALIGN_CENTER); document.add(secTitle); int
					 * summaryIndex = 0; int summarySubSecIndex = 0; for (String secaoId : sections)
					 * { summarySection = this.retrieveSectionById(Long.parseLong(secaoId)); if
					 * (!summarySection.isPreTextSection()) {
					 * 
					 * summaryIndex++; secTitle = new Paragraph(summaryIndex + ". " +
					 * summarySection.getName(), titulo); secTitle.setLeading(interLineSpacing);
					 * document.add(secTitle);
					 * 
					 * List<DocumentSection> dsList = this.listSectionsSons(summarySection);
					 * this.setSectionsFilled(dsList,
					 * summarySection.getDocument().getPlan().getId()); summarySubSecIndex = 0; for
					 * (DocumentSection sec : dsList) {
					 * 
					 * if (sec.isFilled()) { summarySubSecIndex++; secTitle = new Paragraph(
					 * summaryIndex + "." + summarySubSecIndex + ". " + sec.getName(), texto);
					 * secTitle.setLeading(interLineSpacing);
					 * secTitle.setFirstLineIndent(firstLineIndent / 2); document.add(secTitle); } }
					 * 
					 * } } document.newPage(); }
					 */
	
					secIndex++;
	
					// LOGGER.info(ds.getId() + ". " + ds.getName() + " - Size:
					// " +
					// ds.getDocumentAttributes().size());
	
					boolean secTitlePrinted = false;
	
					if (ds.getDocumentAttributes().size() == 0) {
						if (lastAttWasPlan) {
							document.setPageSize(PageSize.A4);
							document.newPage();
						}
	
						Chunk c = new Chunk(secIndex + ". " + secName, PDFSettings.TITLE_FONT);
						c.setGenericTag(secIndex + ". " + secName);
						Paragraph secTitle = new Paragraph(c);
						secTitle.setLeading(PDFSettings.INTER_LINE_SPACING);
						secTitle.setSpacingAfter(PDFSettings.PARAGRAPH_SPACING);
						secTitle.setSpacingBefore(PDFSettings.PARAGRAPH_SPACING);
						document.add(secTitle);
						secTitlePrinted = true;
						lastAttWasPlan = false;
					}
	
					for (DocumentAttribute a : ds.getDocumentAttributes()) {
						if (a.getType().equals(TextArea.class.getCanonicalName())) {
							if (a.getValue() != null && !a.getValue().equals("")) {
								if (lastAttWasPlan) {
									document.setPageSize(PageSize.A4);
									document.newPage();
								}
								if (!secTitlePrinted) {
									Chunk c = new Chunk(secIndex + ". " + secName, PDFSettings.TITLE_FONT);
									c.setGenericTag(secIndex + ". " + secName);
									Paragraph secTitle = new Paragraph(c);
									secTitle.setLeading(PDFSettings.INTER_LINE_SPACING);
									secTitle.setSpacingAfter(PDFSettings.PARAGRAPH_SPACING);
									secTitle.setSpacingBefore(PDFSettings.PARAGRAPH_SPACING);
									document.add(secTitle);
									secTitlePrinted = true;
								}
								String attName = a.getName();
								if (!attName.equals(secName)) {
									Paragraph attTitle = new Paragraph(attName, PDFSettings.TITLE_FONT);
									attTitle.setLeading(PDFSettings.INTER_LINE_SPACING);
									attTitle.setSpacingAfter(PDFSettings.PARAGRAPH_SPACING);
									attTitle.setSpacingBefore(PDFSettings.PARAGRAPH_SPACING);
									document.add(attTitle);
								}
								HtmlToPdfParser.addElementsParsedToDocument(a.getValue(), document);
								lastAttWasPlan = false;
							}
						} else if (a.getType().equals(TableField.class.getCanonicalName())) {
	
							TableFields tf = fieldsBS.tableFieldsByAttribute(a.getId(), true);
							List<TableStructure> tabStructList = fieldsBS.listTableStructureByFields(tf);
							List<TableInstance> tabInstList = fieldsBS.listTableInstanceByFields(tf);
							if (!tabInstList.isEmpty()) {
								if (lastAttWasPlan) {
									document.setPageSize(PageSize.A4);
									document.newPage();
								}
								if (!secTitlePrinted) {
									Chunk c = new Chunk(secIndex + ". " + secName, PDFSettings.TITLE_FONT);
									c.setGenericTag(secIndex + ". " + secName);
									Paragraph secTitle = new Paragraph(c);
									secTitle.setLeading(PDFSettings.INTER_LINE_SPACING);
									secTitle.setSpacingAfter(PDFSettings.PARAGRAPH_SPACING);
									secTitle.setSpacingBefore(PDFSettings.PARAGRAPH_SPACING);
									document.add(secTitle);
									secTitlePrinted = true;
								}
								String attName = a.getName();
								if (!attName.equals(secName)) {
									Paragraph attTitle = new Paragraph(attName, PDFSettings.TEXT_FONT);
									attTitle.setAlignment(Element.ALIGN_CENTER);
									attTitle.setLeading(PDFSettings.INTER_LINE_SPACING);
									attTitle.setSpacingAfter(PDFSettings.PARAGRAPH_SPACING);
									attTitle.setSpacingBefore(PDFSettings.PARAGRAPH_SPACING);
									document.add(attTitle);
								}
								PdfPTable table = returnPdfPTable(tabStructList, tabInstList, false, false);
								document.add(table);
								lastAttWasPlan = false;
							}
	
						} else if (a.getType().equals(ScheduleField.class.getCanonicalName())) {
							List<ScheduleInstance> schInstList = this.fieldsBS
									.retrieveScheduleInstanceByAttribute(a.getId(), true);
							if (!schInstList.isEmpty()) {
								if (lastAttWasPlan) {
									document.setPageSize(PageSize.A4);
									document.newPage();
								}
								if (!secTitlePrinted) {
									Chunk c = new Chunk(secIndex + ". " + secName, PDFSettings.TITLE_FONT);
									c.setGenericTag(secIndex + ". " + secName);
									Paragraph secTitle = new Paragraph(c);
									secTitle.setLeading(PDFSettings.INTER_LINE_SPACING);
									secTitle.setSpacingAfter(PDFSettings.PARAGRAPH_SPACING);
									secTitle.setSpacingBefore(PDFSettings.PARAGRAPH_SPACING);
									document.add(secTitle);
									secTitlePrinted = true;
								}
								String attName = a.getName();
								if (!attName.equals(secName)) {
									Paragraph attTitle = new Paragraph(attName, PDFSettings.TEXT_FONT);
									attTitle.setAlignment(Element.ALIGN_CENTER);
									attTitle.setLeading(PDFSettings.INTER_LINE_SPACING);
									attTitle.setSpacingAfter(PDFSettings.PARAGRAPH_SPACING);
									attTitle.setSpacingBefore(PDFSettings.PARAGRAPH_SPACING);
									document.add(attTitle);
								}
								PdfPTable table = new PdfPTable(3);
								table.getDefaultCell();
								PdfPCell c = new PdfPCell(new Paragraph("Atividade", PDFSettings.TEXT_FONT));
								c.setHorizontalAlignment(Element.ALIGN_CENTER);
								c.setBackgroundColor(PDFSettings.HEADER_BG_COLOR);
								table.addCell(c);
								c = new PdfPCell(new Paragraph("Início", PDFSettings.TEXT_FONT));
								c.setHorizontalAlignment(Element.ALIGN_CENTER);
								c.setBackgroundColor(PDFSettings.HEADER_BG_COLOR);
	
								table.addCell(c);
								c = new PdfPCell(new Paragraph("Fim", PDFSettings.TEXT_FONT));
								c.setHorizontalAlignment(Element.ALIGN_CENTER);
								c.setBackgroundColor(PDFSettings.HEADER_BG_COLOR);
								table.addCell(c);
	
								SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
								for (ScheduleInstance sch : schInstList) {
									table.addCell(new Paragraph(sch.getDescription(), PDFSettings.TEXT_FONT));
									table.addCell(new Paragraph(sdf.format(sch.getBegin()), PDFSettings.TEXT_FONT));
									table.addCell(new Paragraph(sdf.format(sch.getEnd()), PDFSettings.TEXT_FONT));
								}
								document.add(table);
								lastAttWasPlan = false;
							}
						} else if (a.getType().equals(SelectPlan.class.getCanonicalName())) {
							if (a.getValue() != null) {
								Plan plan = planBS.retrieveById(Long.parseLong(a.getValue()));
								List<PdfPTable> planTableList = this.generatePDFplanTable(plan);
								boolean first = true;
								// LOGGER.info("2 - " + plan.getName());
								// LOGGER.info(secName + " " + secTitlePrinted);
								for (PdfPTable planTable : planTableList) {
									if (!lastAttWasPlan) {
										document.setPageSize(PageSize.A4.rotate());
									}
									document.newPage();
									if (first) {
	
										if (!secTitlePrinted) {
											Chunk c = new Chunk(secIndex + ". " + secName, PDFSettings.TITLE_FONT);
											c.setGenericTag(secIndex + ". " + secName);
											Paragraph secTitle = new Paragraph(c);
											document.add(secTitle);
											secTitlePrinted = true;
										}
										Paragraph attTitle = new Paragraph(plan.getName(), PDFSettings.TEXT_FONT);
										attTitle.setAlignment(Element.ALIGN_CENTER);
										document.add(attTitle);
										first = false;
										lastAttWasPlan = true;
									}
									document.add(planTable);
								}
								// document.setPageSize(PageSize.A4);
								// document.newPage();
							}
						}
					}
	
					List<DocumentSection> dsList = this.docBS.listSectionsSons(ds);
					this.docBS.setSectionsFilled(dsList, ds.getDocument().getPlan().getId());
	
					// subitens de um item
					for (DocumentSection d : dsList) {
						if (d.isFilled()) {
							// LOGGER.info("filled: " + d.getName());
							String subSecName = d.getName();
							boolean subSecTitlePrinted = false;
							// if (!secName.equals(subSecName)) {
							/*
							 * subSecIndex++; Paragraph subSecTitle = new Paragraph(secIndex + "." +
							 * subSecIndex + ". " + subSecName, titulo);
							 * subSecTitle.setLeading(interLineSpacing);
							 * subSecTitle.setSpacingAfter(paragraphSpacing);
							 * subSecTitle.setSpacingBefore(paragraphSpacing); document.add(subSecTitle);
							 * subSecTitlePrinted = true;
							 */
							// }
	
							List<DocumentAttribute> attList = this.docBS.listAttributesBySection(d,
									d.getDocument().getPlan().getId());
							// LOGGER.info("attList.size: " + attList.size());
							for (DocumentAttribute a : attList) {
								if (a.getType().equals(TextArea.class.getCanonicalName())) {
									if (a.getValue() != null && !a.getValue().equals("")) {
										String attName = a.getName();
										if (lastAttWasPlan) {
											document.setPageSize(PageSize.A4);
											document.newPage();
										}
										if (!subSecTitlePrinted) {
											subSecIndex++;
											Chunk c = new Chunk(secIndex + "." + subSecIndex + ". " + subSecName, PDFSettings.TITLE_FONT);
											c.setGenericTag(secIndex + "." + subSecIndex + ". " + subSecName);
											Paragraph subSecTitle = new Paragraph(c);
											subSecTitle.setLeading(PDFSettings.INTER_LINE_SPACING);
											subSecTitle.setSpacingAfter(PDFSettings.PARAGRAPH_SPACING);
											subSecTitle.setSpacingBefore(PDFSettings.PARAGRAPH_SPACING);
											document.add(subSecTitle);
											subSecTitlePrinted = true;
										}
										if (!attName.equals(subSecName)) {
											Paragraph attTitle = new Paragraph(attName, PDFSettings.TITLE_FONT);
											attTitle.setLeading(PDFSettings.INTER_LINE_SPACING);
											attTitle.setSpacingAfter(PDFSettings.PARAGRAPH_SPACING);
											attTitle.setSpacingBefore(PDFSettings.PARAGRAPH_SPACING);
											document.add(attTitle);
										}
										HtmlToPdfParser.addElementsParsedToDocument(a.getValue(), document);
										lastAttWasPlan = false;
									}
								} else if (a.getType().equals(TableField.class.getCanonicalName())) {
	
									TableFields tf = fieldsBS.tableFieldsByAttribute(a.getId(), true);
									List<TableStructure> tabStructList = fieldsBS.listTableStructureByFields(tf);
									List<TableInstance> tabInstList = fieldsBS.listTableInstanceByFields(tf);
									if (!tabInstList.isEmpty()) {
										if (lastAttWasPlan) {
											document.setPageSize(PageSize.A4);
											document.newPage();
										}
										String attName = a.getName();
										if (!subSecTitlePrinted) {
											subSecIndex++;
											Chunk c = new Chunk(secIndex + "." + subSecIndex + ". " + subSecName, PDFSettings.TITLE_FONT);
											c.setGenericTag(secIndex + "." + subSecIndex + ". " + subSecName);
											Paragraph subSecTitle = new Paragraph(c);
											subSecTitle.setLeading(PDFSettings.INTER_LINE_SPACING);
											subSecTitle.setSpacingAfter(PDFSettings.PARAGRAPH_SPACING);
											subSecTitle.setSpacingBefore(PDFSettings.PARAGRAPH_SPACING);
											document.add(subSecTitle);
											subSecTitlePrinted = true;
										}
										if (!attName.equals(subSecName)) {
											Paragraph attTitle = new Paragraph(attName, PDFSettings.TEXT_FONT);
											attTitle.setAlignment(Element.ALIGN_CENTER);
											attTitle.setLeading(PDFSettings.INTER_LINE_SPACING);
											attTitle.setSpacingAfter(PDFSettings.PARAGRAPH_SPACING);
											attTitle.setSpacingBefore(PDFSettings.PARAGRAPH_SPACING);
											document.add(attTitle);
										}
										PdfPTable table = returnPdfPTable(tabStructList, tabInstList, false, false);
										document.add(table);
										lastAttWasPlan = false;
									}
								} else if (a.getType().equals(ScheduleField.class.getCanonicalName())) {
									List<ScheduleInstance> schInstList = this.fieldsBS
											.retrieveScheduleInstanceByAttribute(a.getId(), true);
	
									if (!schInstList.isEmpty()) {
										if (lastAttWasPlan) {
											document.setPageSize(PageSize.A4);
											document.newPage();
										}
										if (!subSecTitlePrinted) {
											subSecIndex++;
											Chunk c = new Chunk(secIndex + "." + subSecIndex + ". " + subSecName, PDFSettings.TITLE_FONT);
											c.setGenericTag(secIndex + "." + subSecIndex + ". " + subSecName);
											Paragraph subSecTitle = new Paragraph(c);
											subSecTitle.setLeading(PDFSettings.INTER_LINE_SPACING);
											subSecTitle.setSpacingAfter(PDFSettings.PARAGRAPH_SPACING);
											subSecTitle.setSpacingBefore(PDFSettings.PARAGRAPH_SPACING);
											document.add(subSecTitle);
											subSecTitlePrinted = true;
										}
										String attName = a.getName();
										if (!attName.equals(subSecName)) {
											Paragraph attTitle = new Paragraph(attName, PDFSettings.TEXT_FONT);
											attTitle.setAlignment(Element.ALIGN_CENTER);
											attTitle.setLeading(PDFSettings.INTER_LINE_SPACING);
											attTitle.setSpacingAfter(PDFSettings.PARAGRAPH_SPACING);
											attTitle.setSpacingBefore(PDFSettings.PARAGRAPH_SPACING);
											document.add(attTitle);
										}
										PdfPTable table = new PdfPTable(3);
										table.getDefaultCell();
										PdfPCell c = new PdfPCell(new Paragraph("Atividade", PDFSettings.TEXT_FONT));
										c.setHorizontalAlignment(Element.ALIGN_CENTER);
										c.setBackgroundColor(PDFSettings.HEADER_BG_COLOR);
										table.addCell(c);
										c = new PdfPCell(new Paragraph("Início", PDFSettings.TEXT_FONT));
										c.setHorizontalAlignment(Element.ALIGN_CENTER);
										c.setBackgroundColor(PDFSettings.HEADER_BG_COLOR);
	
										table.addCell(c);
										c = new PdfPCell(new Paragraph("Fim", PDFSettings.TEXT_FONT));
										c.setHorizontalAlignment(Element.ALIGN_CENTER);
										c.setBackgroundColor(PDFSettings.HEADER_BG_COLOR);
										table.addCell(c);
	
										SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
										for (ScheduleInstance sch : schInstList) {
											table.addCell(new Paragraph(sch.getDescription(), PDFSettings.TEXT_FONT));
											table.addCell(new Paragraph(sdf.format(sch.getBegin()), PDFSettings.TEXT_FONT));
											table.addCell(new Paragraph(sdf.format(sch.getEnd()), PDFSettings.TEXT_FONT));
										}
										document.add(table);
										lastAttWasPlan = false;
									}
								} else if (a.getType().equals(SelectPlan.class.getCanonicalName())) {
									if (a.getValue() != null) {
										Plan plan = planBS.retrieveById(Long.parseLong(a.getValue()));
										// LOGGER.info("2 - " + plan.getName());
										List<PdfPTable> planTableList = this.generatePDFplanTable(plan);
										boolean first = true;
										for (PdfPTable planTable : planTableList) {
											if (!lastAttWasPlan) {
												document.setPageSize(PageSize.A4.rotate());
											}
											document.newPage();
											if (first) {
												if (!subSecTitlePrinted) {
													subSecIndex++;
													Chunk c = new Chunk(secIndex + "." + subSecIndex + ". " + subSecName,
															PDFSettings.TITLE_FONT);
													c.setGenericTag(secIndex + "." + subSecIndex + ". " + subSecName);
													Paragraph subSecTitle = new Paragraph(c);
													document.add(subSecTitle);
													subSecTitlePrinted = true;
												}
												Paragraph attTitle = new Paragraph(plan.getName(), PDFSettings.TEXT_FONT);
												attTitle.setAlignment(Element.ALIGN_CENTER);
												document.add(attTitle);
												first = false;
												lastAttWasPlan = true;
											}
											document.add(planTable);
										}
										// document.setPageSize(PageSize.A4);
										// document.newPage();
									}
								}
							}
						}
					}
				}
			}
		} finally {
			Util.closeFile(fw);
			Util.closeFile(conexao);
			if (havePreText)
				preTextDocument.close();
			document.close();
		}

		reportGeneratorHelper.setDimensions(summaryDocument);
		summaryDocument.open();

		Paragraph summaryTitle = new Paragraph("Sumário", PDFSettings.TITLE_FONT);
		summaryTitle.setLeading(PDFSettings.INTER_LINE_SPACING);
		summaryTitle.setSpacingAfter(PDFSettings.PARAGRAPH_SPACING);
		summaryTitle.setSpacingBefore(PDFSettings.PARAGRAPH_SPACING);
		summaryDocument.add(summaryTitle);

		Chunk dottedLine = new Chunk(new DottedLineSeparator());
		List<SimpleEntry<String, SimpleEntry<String, Integer>>> entries = event.getTOC();
		Paragraph p;
		int summaryCountPages = 0;
		for (SimpleEntry<String, SimpleEntry<String, Integer>> entry : entries) {
			// LOGGER.info(entry.getKey());
			Chunk chunk = new Chunk(entry.getKey(), PDFSettings.TITLE_FONT);
			SimpleEntry<String, Integer> value = entry.getValue();
			chunk.setAction(PdfAction.gotoLocalPage(value.getKey(), false));
			p = new Paragraph(chunk);
			p.add(dottedLine);
			chunk = new Chunk(String.valueOf(value.getValue()), PDFSettings.TITLE_FONT);
			chunk.setAction(PdfAction.gotoLocalPage(value.getKey(), false));
			p.add(chunk);
			summaryDocument.add(p);
		}
		summaryDocument.close();

		PdfReader summaryAux = new PdfReader(summaryPdfFile.getPath());
		PdfReader cover = new PdfReader(coverPdfFile.getPath());
		summaryCountPages = summaryAux.getNumberOfPages() + cover.getNumberOfPages();
		PdfReader preText = null;
		if (havePreText) {
			preText = new PdfReader(preTextPdfFile.getPath());
			summaryCountPages += preText.getNumberOfPages();
		}

		Document finalSummaryDocument = new Document();
		PdfWriterFactory.create(finalSummaryDocument, finalSummaryPdfFile);

		reportGeneratorHelper.setDimensions(finalSummaryDocument);
		finalSummaryDocument.open();

		finalSummaryDocument.add(summaryTitle);
		for (SimpleEntry<String, SimpleEntry<String, Integer>> entry : entries) {
			Chunk chunk = new Chunk(entry.getKey(), PDFSettings.TITLE_FONT);
			SimpleEntry<String, Integer> value = entry.getValue();
			chunk.setAction(PdfAction.gotoLocalPage(value.getKey(), false));
			p = new Paragraph(chunk);
			p.add(dottedLine);
			chunk = new Chunk(String.valueOf(value.getValue() + summaryCountPages), PDFSettings.TITLE_FONT);
			chunk.setAction(PdfAction.gotoLocalPage(value.getKey(), false));
			p.add(chunk);
			finalSummaryDocument.add(p);
		}
		finalSummaryDocument.close();

		Document newDocument = new Document();

		PdfImportedPage page;
		int n;
		PdfCopy copy = new PdfCopy(newDocument, new FileOutputStream(destinationFile.getPath()));
		newDocument.open();
		newDocument.addTitle(title);

		PdfReader summary = new PdfReader(finalSummaryPdfFile.getPath());
		PdfReader content = null;
		// int unnumberedPgsCount = summaryCountPages;
		// CAPA
		n = cover.getNumberOfPages();
		// unnumberedPgsCount += n;
		for (int i = 0; i < n;) {
			page = copy.getImportedPage(cover, ++i);
			copy.addPage(page);
		}
		if (havePreText) {
			preText = new PdfReader(preTextPdfFile.getPath());
			// SEÇÕES PRE TEXTUAIS
			n = preText.getNumberOfPages();
			// unnumberedPgsCount += n;
			for (int i = 0; i < n;) {
				page = copy.getImportedPage(preText, ++i);
				copy.addPage(page);
			}
		}
		
		Set<Integer> rotatedPages = new HashSet<>();
		if (haveContent) {
			// SUMÁRIO
			n = summary.getNumberOfPages();
			for (int i = 0; i < n;) {
				page = copy.getImportedPage(summary, ++i);
				copy.addPage(page);
			}
			content = new PdfReader(contentFile.getPath());
			// CONTEÚDO
			n = content.getNumberOfPages();
			for (int i = 0; i < n;) {
				page = copy.getImportedPage(content, ++i);
				if (page.getRotation() == 90) {
					rotatedPages.add(copy.getCurrentPageNumber());
				}

				copy.addPage(page);
			}
		}
		reportGeneratorHelper.closePdfReader(cover);
		reportGeneratorHelper.closePdfReader(summaryAux);
		reportGeneratorHelper.closePdfReader(preText);
		reportGeneratorHelper.closePdfReader(summary);
		reportGeneratorHelper.closePdfReader(content);
		
		newDocument.close();

		this.reportGeneratorHelper.manipulatePdf(
				destinationFile.getPath(), finalPdfFile.getPath(), summaryCountPages, "forpdi", rotatedPages, true);

		InputStream inpStr = new FileInputStream(finalPdfFile);

		TempFilesManager.cleanTempDir(outputDir, prefix);

		return inpStr;
	}
	
	/**
	 * Gera tabela PDF de atributo do tipo "TableField".
	 * 
	 * @param tabStructList Lista de estruturas da tabela (Cabeçalhos).
	 * @param tabInstList   Lista de instâncias de tabela (Valores).
	 * @return PdfPTable Tabela do atributo em PDF.
	 * @throws DocumentException
	 */
	private PdfPTable returnPdfPTable(List<TableStructure> tabStructList, List<TableInstance> tabInstList,
			boolean hideHeaders, boolean hideBorders) throws DocumentException {
		PdfPTable table = new PdfPTable(tabStructList.size());
		table.setHorizontalAlignment(Element.ALIGN_CENTER);

		if (hideBorders) {
			table.getDefaultCell().setBorder(PdfPCell.NO_BORDER);
		}
		table.setWidthPercentage(100);
		// int[] sizes = new int[tabStructList.size()];
		// int i = 0;

		// ajuste de widths
		/*
		 * for (TableStructure ts : tabStructList) { if (ts.getLabel().length() < 4) {
		 * sizes[i] = 6; } else { String[] split = ts.getLabel().split( " "); String
		 * maior = split[0]; for (int j = 0; j < split.length; j++) { if
		 * (split[j].length() > maior.length()) { maior = split[j]; } } if (split.length
		 * <= 3 && maior.length() < 6) { sizes[i] = ts.getLabel().length(); } else {
		 * sizes[i] = maior.length(); } } i++; }
		 */
		// table.setWidths(sizes);
		if (!hideHeaders) {
			for (TableStructure ts : tabStructList) {
				PdfPCell c = new PdfPCell(new Paragraph(ts.getLabel(), PDFSettings.TABLE_FONT));
				c.setBackgroundColor(PDFSettings.HEADER_BG_COLOR);
				c.setHorizontalAlignment(Element.ALIGN_CENTER);
				table.addCell(c);
			}
			for (TableInstance ti : tabInstList) {
				List<TableValues> tabValuesList = fieldsBS.listTableValuesByInstance(ti);
				for (TableValues tv : tabValuesList) {
					if(tv.getValue().equals("") || tv.getValue() == null) {
						table.addCell(new Paragraph(" ", PDFSettings.TABLE_FONT));
						continue;
					}

					if (tv.getTableStructure().getType().equals(Currency.class.getCanonicalName())) {
						table.addCell(new Paragraph(FormatValue.MONETARY.format(tv.getValue()), PDFSettings.TABLE_FONT));
					} else if (tv.getTableStructure().getType().equals(Percentage.class.getCanonicalName())) {
						table.addCell(new Paragraph(FormatValue.PERCENTAGE.format(tv.getValue()), PDFSettings.TABLE_FONT));
					} else if (tv.getTableStructure().getType().equals(NumberField.class.getCanonicalName())) {
						double integerTest = Double.valueOf(tv.getValue());
						if (integerTest == (int) integerTest) {
							table.addCell(new Paragraph(tv.getValue(), PDFSettings.TABLE_FONT));
						} else {
							table.addCell(new Paragraph(FormatValue.NUMERIC.format(tv.getValue()), PDFSettings.TABLE_FONT));
						}
					} else if (tv.getTableStructure().getType().equals(ResponsibleField.class.getCanonicalName())) {
						table.addCell(new Paragraph(this.userBS.existsByUser(Long.valueOf(tv.getValue())).getName(),
								PDFSettings.TABLE_FONT));
					} else {
						table.addCell(new Paragraph(tv.getValue(), PDFSettings.TABLE_FONT));
					}
				}
			}
		} else {
			if (tabStructList.size() == 2) {
				table.setWidths(new float[] { 1, 1 });
			}
			for (TableInstance ti : tabInstList) {
				List<TableValues> tabValuesList = fieldsBS.listTableValuesByInstance(ti);
				for (TableValues tv : tabValuesList) {
					PdfPCell c = new PdfPCell();
					c.setBorder(Rectangle.NO_BORDER);
					// c.setHorizontalAlignment(Element.ALIGN_CENTER);
					Paragraph cellContent = new Paragraph();
					if (tv.getTableStructure().getType().equals(Currency.class.getCanonicalName())) {
						cellContent = new Paragraph(FormatValue.MONETARY.format(tv.getValue()), PDFSettings.TABLE_FONT);

					} else if (tv.getTableStructure().getType().equals(Percentage.class.getCanonicalName())) {
						cellContent = new Paragraph(FormatValue.PERCENTAGE.format(tv.getValue()), PDFSettings.TABLE_FONT);
					} else if (tv.getTableStructure().getType().equals(ResponsibleField.class.getCanonicalName())) {
						cellContent = new Paragraph(this.userBS.existsByUser(Long.valueOf(tv.getValue())).getName(),
								PDFSettings.TABLE_FONT);
					} else {
						cellContent = new Paragraph(tv.getValue(), PDFSettings.TABLE_FONT);
					}
					// cellContent.setAlignment(Element.ALIGN_CENTER);
					c.addElement(cellContent);
					table.addCell(c);
				}
			}
		}
		return table;
	}
	
	/**
	 * Gera tabelas PDF do plano de meta passado por parâmetro.
	 * 
	 * @param plan Plano de metas para geração de PDF.
	 * @return List<PdfPTable> Lista de tabelas em PDF.
	 */
	private List<PdfPTable> generatePDFplanTable(Plan plan) {

		CMYKColor eixoHeaderBgColor = new CMYKColor(0, 0, 0, 70);
		// Cor cinza - cabeçalho objetivo do plano de metas
		CMYKColor objetivoHeaderBgColor = new CMYKColor(0, 0, 0, 50);
		// Cor cinza - conteudo objetivo do plano de metas
		CMYKColor objetivoRowBgColor = new CMYKColor(0, 0, 0, 20);
		// Cor branco - borda tabela plano de metas
		CMYKColor borderPlanColor = new CMYKColor(0, 0, 0, 0);
		// Cor roxo - cabeçalho indicador do plano de metas
		CMYKColor indicadorHeaderBgColor = new CMYKColor(62, 30, 0, 18);
		// Cor roxo - conteudo indicador do plano de metas
		CMYKColor indicadorRowBgColor = new CMYKColor(28, 14, 0, 9);
		PdfPTable table = new PdfPTable(5);
		// Plan plan = planBS.retrieveById(planId);
		List<StructureLevelInstance> list = structureBS.listRootLevelInstanceByPlan(plan);
		// ArrayList<Long> attInstList = new ArrayList<Long>();
		ArrayList<PdfPTable> tableList = new ArrayList<PdfPTable>();
		for (StructureLevelInstance s : list) {

			PaginatedList<StructureLevelInstance> structureLevelSons = new PaginatedList<>();
			structureLevelSons.setList(structureBS.retrieveLevelInstanceSons(s.getId()));
			s.setSons(structureLevelSons);

			List<Attribute> attributeList = structureBS.retrieveLevelAttributes(s.getLevel());
			attributeList = structureBS.setAttributesInstances(s, attributeList);

			if (!s.getSons().getList().isEmpty()) {
				for (StructureLevelInstance son : s.getSons().getList()) {
					if (son.getLevel().isObjective()) {// Objetivo
						String eixoLabel = s.getLevel().getName() + ": ";
						String eixoName = s.getName();
						table = new PdfPTable(5);
						table.setSpacingBefore(PDFSettings.PARAGRAPH_SPACING);
						table.setHorizontalAlignment(Element.ALIGN_CENTER);
						table.setWidthPercentage(100.0f);

						Phrase phraseEixoLabel = new Phrase(eixoLabel, PDFSettings.TITLE_FONT);
						Phrase phraseEixoName = new Phrase(eixoName, PDFSettings.TEXT_FONT);
						phraseEixoLabel.add(phraseEixoName);

						PdfPCell cell = new PdfPCell(phraseEixoLabel);

						cell.setHorizontalAlignment(Element.ALIGN_CENTER);

						// centraliza verticalmente
						Float fontSize = PDFSettings.TITLE_FONT.getSize();
						Float capHeight = PDFSettings.TITLE_FONT.getBaseFont().getFontDescriptor(BaseFont.CAPHEIGHT, fontSize);
						Float padding = 5f;
						cell.setPadding(padding);
						cell.setPaddingTop(capHeight - fontSize + padding);

						cell.setBackgroundColor(eixoHeaderBgColor);
						cell.setBorderColor(borderPlanColor);
						cell.setColspan(5);
						table.addCell(cell);

						String objetivoLabel = son.getLevel().getName() + ": ";
						String objetivoName = son.getName();
						Phrase phraseObjetivoLabel = new Phrase(objetivoLabel, PDFSettings.TITLE_FONT);
						Phrase phraseObjetivoName = new Phrase(objetivoName, PDFSettings.TEXT_FONT);
						phraseObjetivoLabel.add(phraseObjetivoName);

						cell = new PdfPCell(phraseObjetivoLabel);

						cell.setHorizontalAlignment(Element.ALIGN_CENTER);

						// centraliza verticalmente
						cell.setPadding(padding);
						cell.setPaddingTop(capHeight - fontSize + padding);

						cell.setBackgroundColor(objetivoHeaderBgColor);
						cell.setBorderColor(borderPlanColor);
						cell.setColspan(5);
						table.addCell(cell);

						List<BudgetDTO> budgetList = new ArrayList<BudgetDTO>();
						int budgetListSize = 0;

						List<Attribute> sonAttributeList = structureBS.retrieveLevelAttributes(son.getLevel());
						sonAttributeList = structureBS.setAttributesInstances(son, sonAttributeList);
						if (!sonAttributeList.isEmpty()) {
							String bsc = "-";
							for (Attribute sonAttribute : sonAttributeList) {

								if (sonAttribute.isBscField()) { // Perspectiva
									AttributeInstance attInst = attrHelper.retrieveAttributeInstance(son, sonAttribute);
									if (attInst != null) {
										bsc = attInst.getValue();
									} else {
										bsc = "-";
									}
								}
								if (sonAttribute.getBudgets() != null && !sonAttribute.getBudgets().isEmpty()) { // Orçamento
									budgetList = sonAttribute.getBudgets();
									budgetListSize = budgetList.size();
								}
							}
							if (budgetListSize > 0) {
								// Perspectiva BSC
								cell = new PdfPCell(new Phrase("Perspectiva do BSC", PDFSettings.TITLE_FONT));
								cell.setHorizontalAlignment(Element.ALIGN_CENTER);
								// centraliza verticalmente
								cell.setPadding(padding);
								cell.setPaddingTop(capHeight - fontSize + padding);
								cell.setBackgroundColor(objetivoHeaderBgColor);
								cell.setBorderColor(borderPlanColor);
								cell.setColspan(1);
								table.addCell(cell);

								// Orçamento
								cell = new PdfPCell(new Phrase("Orçamento", PDFSettings.TITLE_FONT));
								cell.setHorizontalAlignment(Element.ALIGN_CENTER);
								// centraliza verticalmente
								cell.setPadding(padding);
								cell.setPaddingTop(capHeight - fontSize + padding);
								cell.setBackgroundColor(objetivoHeaderBgColor);
								cell.setBorderColor(borderPlanColor);
								cell.setColspan(4);
								table.addCell(cell);

								// Perspectiva BSC - valor
								cell = new PdfPCell(new Phrase(bsc, PDFSettings.TEXT_FONT));
								cell.setHorizontalAlignment(Element.ALIGN_CENTER);
								// centraliza verticalmente
								cell.setPadding(padding);
								cell.setPaddingTop(capHeight - fontSize + padding);
								cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
								cell.setBackgroundColor(objetivoRowBgColor);
								cell.setBorderColor(borderPlanColor);
								cell.setColspan(1);
								cell.setRowspan(budgetListSize + 1);
								table.addCell(cell);

								// Subação
								cell = new PdfPCell(new Phrase("Subação", PDFSettings.TITLE_FONT));
								cell.setHorizontalAlignment(Element.ALIGN_CENTER);
								// centraliza verticalmente
								cell.setPadding(padding);
								cell.setPaddingTop(capHeight - fontSize + padding);
								cell.setBackgroundColor(objetivoRowBgColor);
								cell.setBorderColor(borderPlanColor);
								cell.setColspan(1);
								table.addCell(cell);

								// Planejado
								cell = new PdfPCell(new Phrase("Planejado", PDFSettings.TITLE_FONT));
								cell.setHorizontalAlignment(Element.ALIGN_CENTER);
								// centraliza verticalmente
								cell.setPadding(padding);
								cell.setPaddingTop(capHeight - fontSize + padding);
								cell.setBackgroundColor(objetivoRowBgColor);
								cell.setBorderColor(borderPlanColor);
								cell.setColspan(1);
								table.addCell(cell);

								// Empenhado
								cell = new PdfPCell(new Phrase("Empenhado", PDFSettings.TITLE_FONT));
								cell.setHorizontalAlignment(Element.ALIGN_CENTER);
								// centraliza verticalmente
								cell.setPadding(padding);
								cell.setPaddingTop(capHeight - fontSize + padding);
								cell.setBackgroundColor(objetivoRowBgColor);
								cell.setBorderColor(borderPlanColor);
								cell.setColspan(1);
								table.addCell(cell);

								// Realizado
								cell = new PdfPCell(new Phrase("Realizado", PDFSettings.TITLE_FONT));
								cell.setHorizontalAlignment(Element.ALIGN_CENTER);
								// centraliza verticalmente
								cell.setPadding(padding);
								cell.setPaddingTop(capHeight - fontSize + padding);
								cell.setBackgroundColor(objetivoRowBgColor);
								cell.setBorderColor(borderPlanColor);
								cell.setColspan(1);
								table.addCell(cell);

								for (BudgetDTO b : budgetList) {
									// Subação - valor
									cell = new PdfPCell(new Phrase(b.getBudget().getSubAction(), PDFSettings.TEXT_FONT));
									cell.setHorizontalAlignment(Element.ALIGN_CENTER);
									// centraliza verticalmente
									cell.setPadding(padding);
									cell.setPaddingTop(capHeight - fontSize + padding);
									cell.setBackgroundColor(objetivoRowBgColor);
									cell.setBorderColor(borderPlanColor);
									cell.setColspan(1);
									table.addCell(cell);

									// Para formatar valores em R$
									Locale ptBr = new Locale("pt", "BR"); // Locale
																			// para
																			// o
																			// Brasil
									NumberFormat moedaFormat = NumberFormat.getCurrencyInstance(ptBr);

									// Planejado - valor
									cell = new PdfPCell(new Phrase(moedaFormat.format(b.getBudgetLoa()), PDFSettings.TEXT_FONT));
									cell.setHorizontalAlignment(Element.ALIGN_CENTER);
									// centraliza verticalmente
									cell.setPadding(padding);
									cell.setPaddingTop(capHeight - fontSize + padding);
									cell.setBackgroundColor(objetivoRowBgColor);
									cell.setBorderColor(borderPlanColor);
									cell.setColspan(1);
									table.addCell(cell);

									// Empenhado - valor
									cell = new PdfPCell(
											new Phrase(moedaFormat.format(b.getBudget().getCommitted()), PDFSettings.TEXT_FONT));
									cell.setHorizontalAlignment(Element.ALIGN_CENTER);
									// centraliza verticalmente
									cell.setPadding(padding);
									cell.setPaddingTop(capHeight - fontSize + padding);
									cell.setBackgroundColor(objetivoRowBgColor);
									cell.setBorderColor(borderPlanColor);
									cell.setColspan(1);
									table.addCell(cell);

									// Realizado - valor
									cell = new PdfPCell(
											new Phrase(moedaFormat.format(b.getBudget().getRealized()), PDFSettings.TEXT_FONT));
									cell.setHorizontalAlignment(Element.ALIGN_CENTER);
									// centraliza verticalmente
									cell.setPadding(padding);
									cell.setPaddingTop(capHeight - fontSize + padding);
									cell.setBackgroundColor(objetivoRowBgColor);
									cell.setBorderColor(borderPlanColor);
									cell.setColspan(1);
									table.addCell(cell);
								}
							}
						}

						StructureLevelInstance sonAux = son;
						PaginatedList<StructureLevelInstance> objSonsList = new PaginatedList<>();
						objSonsList.setList(structureBS.retrieveLevelInstanceSons(sonAux.getId()));
						sonAux.setSons(objSonsList);
						objSonsList = sonAux.getSons();

						if (objSonsList.getList().size() > 0) {
							// Indicadores
							cell = new PdfPCell(new Phrase("Indicadores", PDFSettings.TITLE_FONT));
							cell.setHorizontalAlignment(Element.ALIGN_CENTER);
							// centraliza verticalmente
							cell.setPadding(padding);
							cell.setPaddingTop(capHeight - fontSize + padding);
							cell.setBackgroundColor(indicadorHeaderBgColor);
							cell.setBorderColor(borderPlanColor);
							cell.setColspan(1);
							table.addCell(cell);

							// Metas
							cell = new PdfPCell(new Phrase("Metas", PDFSettings.TITLE_FONT));
							cell.setHorizontalAlignment(Element.ALIGN_CENTER);
							// centraliza verticalmente
							cell.setPadding(padding);
							cell.setPaddingTop(capHeight - fontSize + padding);
							cell.setBackgroundColor(objetivoHeaderBgColor);
							cell.setBorderColor(borderPlanColor);
							cell.setColspan(2);
							table.addCell(cell);

							// Esperado
							cell = new PdfPCell(new Phrase("Esperado", PDFSettings.TITLE_FONT));
							cell.setHorizontalAlignment(Element.ALIGN_CENTER);
							// centraliza verticalmente
							cell.setPadding(padding);
							cell.setPaddingTop(capHeight - fontSize + padding);
							cell.setBackgroundColor(objetivoHeaderBgColor);
							cell.setBorderColor(borderPlanColor);
							cell.setColspan(1);
							table.addCell(cell);

							// Alcançado
							cell = new PdfPCell(new Phrase("Alcançado", PDFSettings.TITLE_FONT));
							cell.setHorizontalAlignment(Element.ALIGN_CENTER);
							// centraliza verticalmente
							cell.setPadding(padding);
							cell.setPaddingTop(capHeight - fontSize + padding);
							cell.setBackgroundColor(objetivoHeaderBgColor);
							cell.setBorderColor(borderPlanColor);
							cell.setColspan(1);
							table.addCell(cell);

							// String indicadorName = "";
							String calculo = "";
							Long responsavel = (long) -1;

							for (StructureLevelInstance indicatorSon : objSonsList.getList()) { // Indicadores
								if (indicatorSon.getLevel().isIndicator()) {
									List<Attribute> indicatorSonAttributeList = structureBS
											.retrieveLevelAttributes(indicatorSon.getLevel());
									indicatorSonAttributeList = structureBS.setAttributesInstances(indicatorSon,
											indicatorSonAttributeList);
									indicatorSon.getLevel().setAttributes(indicatorSonAttributeList);
									for (Attribute indicatorSonAttribute : indicatorSonAttributeList) {
										if (indicatorSonAttribute.getId() == 14) { // Cálculo
											AttributeInstance attInst = attrHelper.retrieveAttributeInstance(
													indicatorSon, structureBS.retrieveAttribute((long) 14));
											if (attInst != null) {
												calculo = indicatorSonAttribute.getAttributeInstance().getValue();
											} else {
												calculo = "-";
											}
										}
										if (indicatorSonAttribute.getId() == 7) { // responsável
											AttributeInstance attInst = attrHelper.retrieveAttributeInstance(
													indicatorSon, structureBS.retrieveAttribute((long) 7));
											if (attInst != null) {
												responsavel = Long.parseLong(
														indicatorSonAttribute.getAttributeInstance().getValue());
											} else {
												responsavel = (long) -1;
											}

										}
									}
									// LOGGER.info(calculo);
									// LOGGER.info(responsavel);
									PaginatedList<StructureLevelInstance> levelInstances = new PaginatedList<>();
									levelInstances
											.setList(this.structureBS.retrieveLevelInstanceSons(indicatorSon.getId()));

									// Indicador - valores

									User responsible = userBS.existsByUser(responsavel);
									// LOGGER.info(responsible.toString());
									Phrase indicador = new Phrase(indicatorSon.getName(), PDFSettings.TEXT_FONT);
									indicador.add(new Phrase("\n\nCálculo: ", PDFSettings.TITLE_FONT));
									indicador.add(new Phrase(calculo, PDFSettings.TEXT_FONT));
									indicador.add(new Phrase("\nResponsável: ", PDFSettings.TITLE_FONT));
									if (responsible != null) {
										indicador.add(new Phrase(responsible.getName(), PDFSettings.TEXT_FONT));
									} else {
										indicador.add(new Phrase("-", PDFSettings.TEXT_FONT));
									}

									cell = new PdfPCell(indicador);
									cell.setHorizontalAlignment(Element.ALIGN_CENTER);
									// centraliza verticalmente
									cell.setPadding(padding);
									cell.setPaddingTop(capHeight - fontSize + padding);
									cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
									cell.setBackgroundColor(indicadorRowBgColor);
									cell.setBorderColor(borderPlanColor);
									cell.setColspan(1);
									cell.setMinimumHeight(4 * fontSize);
									if (levelInstances.getList().size() > 0) {
										cell.setRowspan(levelInstances.getList().size());
									}
									table.addCell(cell);

									for (int index = 0; index < levelInstances.getList().size(); index++) {

										levelInstances.getList().get(index).getLevel().setAttributes(this.structureBS
												.retrieveLevelSonsAttributes(levelInstances.getList().get(index)));
									}

									StructureLevelInstance levelInstanceAux = new StructureLevelInstance();
									levelInstanceAux.setId(indicatorSon.getId());
									levelInstanceAux.setSons(levelInstances);

									// CABEÇALHO
									// METAS
									if (levelInstanceAux.getSons().getList().size() != 0) {
										ArrayList<String> expected = new ArrayList<String>();
										ArrayList<String> reached = new ArrayList<String>();

										HashMap<Long, ArrayList<String>> meta = new HashMap<Long, ArrayList<String>>();
										AttributeInstance formatAttr = this.attrHelper
												.retrieveFormatAttributeInstance(levelInstanceAux);
										FormatValue formatValue = FormatValue.forAttributeInstance(formatAttr);
										for (int goalIndex = 0; goalIndex < levelInstanceAux.getSons().getList()
												.size(); goalIndex++) {

											ArrayList<String> values = new ArrayList<String>();
											// LOGGER.info(levelInstanceAux.getSons().get(goalIndex).toString());

											List<AttributeInstance> attInst = structureBS.listAttributeInstanceByLevel(
													levelInstanceAux.getSons().getList().get(goalIndex), false);
											List<Attribute> attList = structureBS.listAttributesPDF(
													levelInstanceAux.getSons().getList().get(goalIndex).getLevel());

											for (Attribute a : attList) {
												if (a.isExpectedField()) { // esperado
													for (AttributeInstance at : attInst) {
														if (at.getAttribute().getId().equals(a.getId())) {
															at.setFormattedValue(formatValue
																	.format(at.getValue().replace(',', '.')));
															expected.add(at.getFormattedValue());
															values.add(at.getFormattedValue());
														}
													}
												} else if (a.isReachedField()) { // realizado
													for (AttributeInstance at : attInst) {
														if (at.getAttribute().getId().equals(a.getId())) {
															at.setFormattedValue(formatValue.format(at.getValue()));
															reached.add(at.getFormattedValue());
															values.add(at.getFormattedValue());
														}
													}
												}
											}

											meta.put(levelInstanceAux.getSons().getList().get(goalIndex).getId(),
													values);
										}

										List<Long> keys = new ArrayList<Long>(meta.keySet());
										Collections.sort(keys);
										// LOGGER.info(keys);
										int i = 0;
										for (Long x : keys) {
											// LOGGER.info(meta.get(x));
											cell = new PdfPCell(
													new Phrase(levelInstances.getList().get(i).getName(), PDFSettings.TEXT_FONT));
											i++;
											cell.setHorizontalAlignment(Element.ALIGN_CENTER);
											cell.setPadding(padding);
											cell.setPaddingTop(capHeight - fontSize + padding);
											cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
											cell.setBackgroundColor(objetivoRowBgColor);
											cell.setBorderColor(borderPlanColor);
											cell.setColspan(2);
											table.addCell(cell);

											if (meta.get(x).size() > 1) {
												cell = new PdfPCell(new Phrase(meta.get(x).get(1), PDFSettings.TEXT_FONT));
												cell.setHorizontalAlignment(Element.ALIGN_CENTER);
												cell.setPadding(padding);
												cell.setPaddingTop(capHeight - fontSize + padding);
												cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
												cell.setBackgroundColor(objetivoRowBgColor);
												cell.setBorderColor(borderPlanColor);
												table.addCell(cell);

												cell = new PdfPCell(new Phrase(meta.get(x).get(0), PDFSettings.TEXT_FONT));
												cell.setHorizontalAlignment(Element.ALIGN_CENTER);
												cell.setPadding(padding);
												cell.setPaddingTop(capHeight - fontSize + padding);
												cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
												cell.setBackgroundColor(objetivoRowBgColor);
												cell.setBorderColor(borderPlanColor);
												table.addCell(cell);
											} else if (meta.get(x).size() == 0) {
												cell = new PdfPCell(new Phrase("-", PDFSettings.TEXT_FONT));
												cell.setHorizontalAlignment(Element.ALIGN_CENTER);
												cell.setPadding(padding);
												cell.setPaddingTop(capHeight - fontSize + padding);
												cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
												cell.setBackgroundColor(objetivoRowBgColor);
												cell.setBorderColor(borderPlanColor);
												table.addCell(cell);

												cell = new PdfPCell(new Phrase("-", PDFSettings.TEXT_FONT));
												cell.setHorizontalAlignment(Element.ALIGN_CENTER);
												cell.setPadding(padding);
												cell.setPaddingTop(capHeight - fontSize + padding);
												cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
												cell.setBackgroundColor(objetivoRowBgColor);
												cell.setBorderColor(borderPlanColor);
												table.addCell(cell);

											} else {
												cell = new PdfPCell(new Phrase(meta.get(x).get(0), PDFSettings.TEXT_FONT));
												cell.setHorizontalAlignment(Element.ALIGN_CENTER);
												cell.setPadding(padding);
												cell.setPaddingTop(capHeight - fontSize + padding);
												cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
												cell.setBackgroundColor(objetivoRowBgColor);
												cell.setBorderColor(borderPlanColor);
												table.addCell(cell);

												cell = new PdfPCell(new Phrase("-", PDFSettings.TEXT_FONT));
												cell.setHorizontalAlignment(Element.ALIGN_CENTER);
												cell.setPadding(padding);
												cell.setPaddingTop(capHeight - fontSize + padding);
												cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
												cell.setBackgroundColor(objetivoRowBgColor);
												cell.setBorderColor(borderPlanColor);
												table.addCell(cell);
											}
										}
									} else {
										cell = new PdfPCell(new Phrase("-", PDFSettings.TEXT_FONT));
										cell.setHorizontalAlignment(Element.ALIGN_CENTER);
										cell.setPadding(padding);
										cell.setPaddingTop(capHeight - fontSize + padding);
										cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
										cell.setBackgroundColor(objetivoRowBgColor);
										cell.setBorderColor(borderPlanColor);
										cell.setColspan(2);
										table.addCell(cell);
										cell.setColspan(1);
										table.addCell(cell);
										table.addCell(cell);

									}
								}
							}
						}
						tableList.add(table);
					}
				}
			}
		}
		return tableList;
	}
	
	public static class Params implements ReportGeneratorParams {
		public String title;
		public String lista;

		public Params(String title, String lista) {
			this.title = title;
			this.lista = lista;
		}
	}
}
