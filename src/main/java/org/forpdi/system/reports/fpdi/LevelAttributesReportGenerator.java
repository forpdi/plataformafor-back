package org.forpdi.system.reports.fpdi;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.imageio.ImageIO;

import org.forpdi.core.common.PaginatedList;
import org.forpdi.core.company.CompanyDomainContext;
import org.forpdi.core.user.User;
import org.forpdi.core.user.UserBS;
import org.forpdi.core.utils.Util;
import org.forpdi.planning.attribute.AggregateIndicator;
import org.forpdi.planning.attribute.Attribute;
import org.forpdi.planning.attribute.AttributeHelper;
import org.forpdi.planning.attribute.AttributeInstance;
import org.forpdi.planning.attribute.types.ActionPlanField;
import org.forpdi.planning.attribute.types.BudgetField;
import org.forpdi.planning.attribute.types.ResponsibleField;
import org.forpdi.planning.attribute.types.enums.FormatValue;
import org.forpdi.planning.document.DocumentBS;
import org.forpdi.planning.fields.FieldsBS;
import org.forpdi.planning.fields.actionplan.ActionPlan;
import org.forpdi.planning.fields.budget.BudgetDTO;
import org.forpdi.planning.structure.StructureBS;
import org.forpdi.planning.structure.StructureHelper;
import org.forpdi.planning.structure.StructureLevelInstance;
import org.forpdi.system.reports.ReportGenerator;
import org.forpdi.system.reports.ReportGeneratorParams;
import org.forpdi.system.reports.pdf.PDFSettings;
import org.forpdi.system.reports.pdf.PdfWriterFactory;
import org.forpdi.system.reports.pdf.table.TempFilesManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.itextpdf.text.Chunk;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;


@Component
public class LevelAttributesReportGenerator implements ReportGenerator {

	@Autowired
	private CompanyDomainContext domain;
	@Autowired
	private UserBS userBS;
	@Autowired
	private DocumentBS docBS;

	@Autowired
	private StructureBS structureBS;
	@Autowired
	StructureHelper structHelper;
	@Autowired
	private AttributeHelper attrHelper;
	@Autowired
	private FieldsBS fieldsBS;

	@Override
	public InputStream generateReport(ReportGeneratorParams params) {
		try {
			Params parsedParams = ReportGeneratorParams.extractParams(params, Params.class);
			return exportLevelAttributes(parsedParams.levelId);
		} catch (IOException | DocumentException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Exportar para pdf atributos de um level
	 * 
	 * @param levelId Id do level
	 * @return
	 * @throws MalformedURLException
	 * @throws IOException
	 * @throws DocumentException
	 */
	public InputStream exportLevelAttributes(Long levelId)
			throws MalformedURLException, IOException, DocumentException {

		com.itextpdf.text.Document document = new com.itextpdf.text.Document();

		ClassLoader classLoader = getClass().getClassLoader();

		File outputDir = TempFilesManager.getTempDir();

		final String prefix = String.valueOf(System.currentTimeMillis());
		File pdfFile = new File(outputDir, String.format("%s-pdi-levelattribute-output.pdf", prefix));
		PdfWriterFactory.create(document, pdfFile);

		// Parágrafo com 1,25 cm na primeira linha
		// float firstLineIndent = 35.43307f;
		// 1,5 entrelinhas
		// float interLineSpacing = texto.getCalculatedLeading(1.5f);
		// Formato A4 do documento
		document.setPageSize(PageSize.A4);
		// Margens Superior e esquerda: 3 cm Inferior e direita: 2 cm
		document.setMargins(PDFSettings.HORIZONTAL_MARGIN, PDFSettings.VERTICAL_MARGIN, PDFSettings.HORIZONTAL_MARGIN, PDFSettings.VERTICAL_MARGIN);

		document.open();
		document.add(new Chunk(""));

		// CABEÇALHO
		String companyLogoUrl = domain.get().getCompany().getLogo();
		// String fpdiLogoUrl2 = "http://cloud.progolden.com.br/file/8345";// new
		// String fpdiLogoUrl =new
		// File(classLoader.getResource("logo.png").getFile()).getPath();

		// carregamento de resource da forma correta
		BufferedImage buff = ImageIO.read(classLoader.getResourceAsStream("logo.png"));
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ImageIO.write(buff, "png", bos);

		if (!companyLogoUrl.trim().isEmpty()) {
			Image companyLogo = Image.getInstance(new URL(companyLogoUrl));
			Image fpdiLogo = Image.getInstance(bos.toByteArray());
			// image.scaleAbsolute(150f, 150f);
			float companyLogoScaler = ((document.getPageSize().getWidth() - document.leftMargin()
					- document.rightMargin()) / companyLogo.getWidth()) * 100;
			float fpdiLogoScaler = ((document.getPageSize().getWidth() - document.leftMargin() - document.rightMargin())
					/ fpdiLogo.getWidth()) * 100;
			companyLogo.scalePercent(companyLogoScaler * 0.25f);
			companyLogo.setAlignment(Element.ALIGN_LEFT);
			fpdiLogo.scalePercent(fpdiLogoScaler * 0.15f);
			fpdiLogo.setAlignment(Element.ALIGN_RIGHT);
			PdfPTable table = new PdfPTable(2);
			table.setWidthPercentage(100);
			table.setWidths(new int[] { 1, 1 });
			table.setSpacingAfter(PDFSettings.PARAGRAPH_SPACING);

			PdfPCell companyCell = new PdfPCell(companyLogo);
			PdfPCell fpdiCell = new PdfPCell(fpdiLogo);

			companyCell.setBorder(Rectangle.NO_BORDER);
			fpdiCell.setBorder(Rectangle.NO_BORDER);
			companyCell.setHorizontalAlignment(Element.ALIGN_LEFT);
			fpdiCell.setHorizontalAlignment(Element.ALIGN_RIGHT);

			table.addCell(companyCell);
			table.addCell(fpdiCell);
			document.add(table);
		}

		StructureLevelInstance levelInstance = this.structHelper.retrieveLevelInstance(levelId);
		String levelInstanceName = levelInstance.getName();
		document.addTitle(levelInstanceName);
		String levelInstanceType;
		if (levelInstance.getLevel().isIndicator()) {
			if (levelInstance.isAggregate()) {
				levelInstanceType = levelInstance.getLevel().getName() + " agregado";
			} else {
				levelInstanceType = levelInstance.getLevel().getName() + " simples";
			}
		} else {
			levelInstanceType = levelInstance.getLevel().getName();
		}
		String planName = levelInstance.getPlan().getName();
		String planMacroName = levelInstance.getPlan().getParent().getName();

		// PLANO MACRO
		Paragraph planMacroParagraph = new Paragraph(planMacroName, PDFSettings.TITLE_FONT);
		document.add(planMacroParagraph);

		// PLANO DE METAS
		Paragraph planParagraph = new Paragraph(planName, PDFSettings.TITLE_FONT);
		document.add(planParagraph);

		// DATA EXPORTAÇÃO
		SimpleDateFormat brDateFormat = new SimpleDateFormat("dd/MM/yyyy");
		Calendar cal = Calendar.getInstance();
		// Paragraph dataExportacaoLabel = new Paragraph("Data da exportação: ",
		// titulo);
		Paragraph dataExportacaoValue = new Paragraph(brDateFormat.format(cal.getTime()), PDFSettings.TEXT_FONT);
		dataExportacaoValue.setSpacingAfter(PDFSettings.PARAGRAPH_SPACING);
		document.add(dataExportacaoValue);

		// NOME DO NIVEL
		Paragraph levelInstanceNameParagraph = new Paragraph(levelInstanceName, PDFSettings.TITLE_FONT);
		document.add(levelInstanceNameParagraph);

		// TIPO DO NIVEL
		Paragraph levelInstanceTypeParagraph = new Paragraph(levelInstanceType, PDFSettings.ITALIC_FONT);
		document.add(levelInstanceTypeParagraph);

		// RENDIMENTO DO NIVEL
		DecimalFormat decimalFormatDbl = new DecimalFormat("#,##0.00");
		Paragraph proceedsParagraph = new Paragraph();
		Phrase proceedsValue;
		Phrase proceedsLabel = new Phrase("Rendimento atual do nível: ", PDFSettings.TITLE_FONT);
		if (levelInstance.getLevelValue() == null) {
			proceedsValue = new Phrase(Util.PERCENTAGE_PATTERN, PDFSettings.TEXT_FONT);
		} else {

			proceedsValue = new Phrase(decimalFormatDbl.format(levelInstance.getLevelValue()) + "%",
					PDFSettings.TEXT_FONT);
		}
		proceedsParagraph.add(proceedsLabel);
		proceedsParagraph.add(proceedsValue);
		proceedsParagraph.setSpacingAfter(PDFSettings.PARAGRAPH_SPACING);
		document.add(proceedsParagraph);

		List<Attribute> attrList = this.structureBS.retrieveLevelSonsAttributes(levelInstance);

		for (Attribute attribute : attrList) {
			// LOGGER.info(attribute.toString());
			if (attribute.isRequired() || attribute.getType().equals(BudgetField.class.getCanonicalName())
					|| attribute.isReachedField()) {
				// LOGGER.info(attribute.toString());

				Paragraph attributeParagraph = new Paragraph();
				Phrase attributeValue = new Phrase();
				Phrase attributeLabel = new Phrase(attribute.getLabel() + ": ", PDFSettings.TITLE_FONT);

				if (attribute.getType().equals(ResponsibleField.class.getCanonicalName())) {
					if (attribute.getAttributeInstances().get(0) != null) {

						User responsible = this.userBS
								.existsByUser(Long.parseLong(attribute.getAttributeInstances().get(0).getValue()));
						if (responsible != null) {
							attributeValue = new Phrase(responsible.getName(), PDFSettings.TEXT_FONT);
							attributeParagraph.add(attributeLabel);
							attributeParagraph.add(attributeValue);
							document.add(attributeParagraph);
						}
					}
				} else if (attribute.getType().equals(ActionPlanField.class.getCanonicalName())) {

					PaginatedList<ActionPlan> actionPlanList = this.fieldsBS.listActionPlansByInstance(levelInstance);
					if (actionPlanList.getList() != null && !actionPlanList.getList().isEmpty()) {
						attributeParagraph.setSpacingBefore(PDFSettings.PARAGRAPH_SPACING);
						attributeLabel = new Phrase(attribute.getLabel(), PDFSettings.TITLE_FONT);
						attributeParagraph.add(attributeLabel);
						document.add(attributeParagraph);

						PdfPTable table = new PdfPTable(4);
						table.setSpacingBefore(PDFSettings.PARAGRAPH_SPACING / 2);
						table.setWidthPercentage(100);
						table.getDefaultCell();
						PdfPCell c = new PdfPCell(new Paragraph("Ação", PDFSettings.TEXT_FONT));
						c.setHorizontalAlignment(Element.ALIGN_CENTER);
						// c.setBackgroundColor(headerBgColor);
						table.addCell(c);
						c = new PdfPCell(new Paragraph("Responsável", PDFSettings.TEXT_FONT));
						c.setHorizontalAlignment(Element.ALIGN_CENTER);
						// c.setBackgroundColor(headerBgColor);
						table.addCell(c);

						c = new PdfPCell(new Paragraph("Início", PDFSettings.TEXT_FONT));
						c.setHorizontalAlignment(Element.ALIGN_CENTER);
						// c.setBackgroundColor(headerBgColor);
						table.addCell(c);

						c = new PdfPCell(new Paragraph("Fim", PDFSettings.TEXT_FONT));
						c.setHorizontalAlignment(Element.ALIGN_CENTER);
						// c.setBackgroundColor(headerBgColor);
						table.addCell(c);

						SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
						for (ActionPlan acp : actionPlanList.getList()) {
							table.addCell(new Paragraph(acp.getDescription(), PDFSettings.TEXT_FONT));
							table.addCell(new Paragraph(acp.getUserResponsibleName(), PDFSettings.TEXT_FONT));
							table.addCell(new Paragraph(sdf.format(acp.getBegin()), PDFSettings.TEXT_FONT));
							table.addCell(new Paragraph(sdf.format(acp.getEnd()), PDFSettings.TEXT_FONT));
						}
						document.add(table);
					}
				} else if (attribute.getType().equals(BudgetField.class.getCanonicalName())) {
					List<BudgetDTO> budgetList = new ArrayList<BudgetDTO>();
					budgetList = this.fieldsBS.getBudgets(levelInstance);
					if (!budgetList.isEmpty()) {
						attributeParagraph.setSpacingBefore(PDFSettings.PARAGRAPH_SPACING);
						attributeLabel = new Phrase(attribute.getLabel(), PDFSettings.TITLE_FONT);
						attributeParagraph.add(attributeLabel);
						document.add(attributeParagraph);

						// Orçamento
						PdfPTable table = new PdfPTable(4);
						table.setSpacingBefore(PDFSettings.PARAGRAPH_SPACING / 2);
						table.setWidthPercentage(100);

						// Subação
						PdfPCell cell = new PdfPCell(new Phrase("Subação", PDFSettings.TEXT_FONT));
						cell.setHorizontalAlignment(Element.ALIGN_CENTER);
						table.addCell(cell);

						// Planejado
						cell = new PdfPCell(new Phrase("Planejado", PDFSettings.TEXT_FONT));
						cell.setHorizontalAlignment(Element.ALIGN_CENTER);
						table.addCell(cell);

						// Empenhado
						cell = new PdfPCell(new Phrase("Empenhado", PDFSettings.TEXT_FONT));
						cell.setHorizontalAlignment(Element.ALIGN_CENTER);
						table.addCell(cell);

						// Realizado
						cell = new PdfPCell(new Phrase("Realizado", PDFSettings.TEXT_FONT));
						cell.setHorizontalAlignment(Element.ALIGN_CENTER);
						table.addCell(cell);

						for (BudgetDTO b : budgetList) {
							// Subação - valor
							cell = new PdfPCell(new Phrase(b.getBudget().getSubAction(), PDFSettings.TEXT_FONT));
							table.addCell(cell);

							// Para formatar valores em R$
							Locale ptBr = new Locale("pt", "BR"); // Locale
																	// para
																	// o
																	// Brasil
							NumberFormat moedaFormat = NumberFormat.getCurrencyInstance(ptBr);

							// Planejado - valor
							cell = new PdfPCell(new Phrase(moedaFormat.format(b.getBudgetLoa()), PDFSettings.TEXT_FONT));
							table.addCell(cell);

							// Empenhado - valor
							cell = new PdfPCell(new Phrase(moedaFormat.format(b.getBudget().getCommitted()), PDFSettings.TEXT_FONT));
							table.addCell(cell);

							// Realizado - valor
							cell = new PdfPCell(new Phrase(moedaFormat.format(b.getBudget().getRealized()), PDFSettings.TEXT_FONT));
							table.addCell(cell);
						}
						document.add(table);
					}

				} else {
					if (levelInstance.getLevel().isGoal()) {
						AttributeInstance attinst = attribute.getAttributeInstances().get(0);
						if (attribute.isMaximumField() || attribute.isMinimumField() || attribute.isExpectedField()
								|| attribute.isReachedField()) {

							FormatValue formatValue = FormatValue.forAttributeInstance(
									this.attrHelper.retrieveFormatAttributeInstance(levelInstance.getParent()));
							if (attinst != null) {
								if (attinst.getValue() != null)
									attributeValue = new Phrase(formatValue.format(attinst.getValue()), PDFSettings.TEXT_FONT);
								else
									attributeValue = new Phrase("-", PDFSettings.TEXT_FONT);
							} else {
								attributeValue = new Phrase("-", PDFSettings.TEXT_FONT);
							}
						} else {
							if (attinst != null) {
								attributeValue = new Phrase(attinst.getValue(), PDFSettings.TEXT_FONT);
							}
						}
						if (attinst != null) {
							attributeParagraph.add(attributeLabel);
							attributeParagraph.add(attributeValue);
							document.add(attributeParagraph);
						}
					} else {
						if (attribute.getAttributeInstances().get(0) != null) {
							attributeValue = new Phrase(attribute.getAttributeInstances().get(0).getValue(), PDFSettings.TEXT_FONT);
							attributeParagraph.add(attributeLabel);
							attributeParagraph.add(attributeValue);
							document.add(attributeParagraph);
						}
					}
				}

			}
		}

		if (levelInstance.isAggregate()) {

			List<AggregateIndicator> levelList = this.docBS.listAggIndbyLevelInstance(levelInstance);

			if (!levelList.isEmpty()) {
				Paragraph aggParagraphLabel = new Paragraph();
				aggParagraphLabel.setSpacingBefore(PDFSettings.PARAGRAPH_SPACING);
				aggParagraphLabel.add(new Phrase("Indicadores", PDFSettings.TITLE_FONT));
				document.add(aggParagraphLabel);

				PdfPTable table = new PdfPTable(5);
				table.setSpacingBefore(PDFSettings.PARAGRAPH_SPACING / 2);
				table.setWidthPercentage(100);

				PdfPCell cell = new PdfPCell(new Phrase("Nome", PDFSettings.TEXT_FONT));
				cell.setHorizontalAlignment(Element.ALIGN_CENTER);
				table.addCell(cell);

				cell = new PdfPCell(new Phrase("Responsável", PDFSettings.TEXT_FONT));
				cell.setHorizontalAlignment(Element.ALIGN_CENTER);
				table.addCell(cell);

				cell = new PdfPCell(new Phrase("Início", PDFSettings.TEXT_FONT));
				cell.setHorizontalAlignment(Element.ALIGN_CENTER);
				table.addCell(cell);

				cell = new PdfPCell(new Phrase("Fim", PDFSettings.TEXT_FONT));
				cell.setHorizontalAlignment(Element.ALIGN_CENTER);
				table.addCell(cell);

				cell = new PdfPCell(new Phrase("Rendimento", PDFSettings.TEXT_FONT));
				cell.setHorizontalAlignment(Element.ALIGN_CENTER);
				table.addCell(cell);

				for (AggregateIndicator son : levelList) {

					cell = new PdfPCell(new Phrase(son.getAggregate().getName(), PDFSettings.TEXT_FONT));
					table.addCell(cell);

					List<Attribute> sonAttrList = this.structureBS.retrieveLevelSonsAttributes(son.getAggregate());
					User responsible = new User();
					Date beginDate = new Date();
					Date endDate = new Date();
					for (Attribute attribute : sonAttrList) {
						if (attribute.getType().equals(ResponsibleField.class.getCanonicalName())) {
							responsible = this.userBS
									.existsByUser(Long.parseLong(attribute.getAttributeInstances().get(0).getValue()));
						} else if (attribute.isBeginField()) {
							beginDate = attribute.getAttributeInstances().get(0).getValueAsDate();
						} else if (attribute.isEndField()) {
							endDate = attribute.getAttributeInstances().get(0).getValueAsDate();
						}
					}

					if (responsible != null)
						cell = new PdfPCell(new Phrase(responsible.getName(), PDFSettings.TEXT_FONT));
					else
						cell = new PdfPCell(new Phrase("-", PDFSettings.TEXT_FONT));
					table.addCell(cell);

					cell = new PdfPCell(new Phrase(brDateFormat.format(beginDate), PDFSettings.TEXT_FONT));
					table.addCell(cell);

					cell = new PdfPCell(new Phrase(brDateFormat.format(endDate), PDFSettings.TEXT_FONT));
					table.addCell(cell);

					if (son.getAggregate().getLevelValue() != null)
						cell = new PdfPCell(new Phrase(
								decimalFormatDbl.format(son.getAggregate().getLevelValue()) + "%",
								PDFSettings.TEXT_FONT));
					else
						cell = new PdfPCell(new Phrase(Util.PERCENTAGE_PATTERN, PDFSettings.TEXT_FONT));
					table.addCell(cell);
				}
				document.add(table);

			}

		}

		PaginatedList<StructureLevelInstance> sonsList = new PaginatedList<>();
		sonsList.setList(structureBS.retrieveLevelInstanceSons(levelInstance.getId()));
		levelInstance.setSons(sonsList);
		sonsList = levelInstance.getSons();
		Paragraph sonParagraphLabel = new Paragraph();
		// LOGGER.info(sonsList.getList().toString());
		if (!sonsList.getList().isEmpty()) {
			if (sonsList.getList().get(0).getLevel().isObjective()) {
				sonParagraphLabel.setSpacingBefore(PDFSettings.PARAGRAPH_SPACING);
				sonParagraphLabel.add(new Phrase("Objetivos", PDFSettings.TITLE_FONT));
				document.add(sonParagraphLabel);

				PdfPTable table = new PdfPTable(3);
				table.setSpacingBefore(PDFSettings.PARAGRAPH_SPACING / 2);
				table.setWidthPercentage(100);

				PdfPCell cell = new PdfPCell(new Phrase("Nome", PDFSettings.TEXT_FONT));
				cell.setHorizontalAlignment(Element.ALIGN_CENTER);
				table.addCell(cell);

				cell = new PdfPCell(new Phrase("Responsável", PDFSettings.TEXT_FONT));
				cell.setHorizontalAlignment(Element.ALIGN_CENTER);
				table.addCell(cell);

				cell = new PdfPCell(new Phrase("Rendimento", PDFSettings.TEXT_FONT));
				cell.setHorizontalAlignment(Element.ALIGN_CENTER);
				table.addCell(cell);
				int indexObjectivesSon = 0;
				for (StructureLevelInstance son : sonsList.getList()) {
					cell = new PdfPCell(new Phrase(son.getName(), PDFSettings.TEXT_FONT));
					table.addCell(cell);

					List<Attribute> sonAttrList = this.structureBS.retrieveLevelSonsAttributes(son);
					User responsible = new User();
					for (Attribute attribute : sonAttrList) {
						if (attribute.getType().equals(ResponsibleField.class.getCanonicalName())) {
							if (attribute.getAttributeInstances().get(indexObjectivesSon) != null) {
								responsible = this.userBS.existsByUser(
										Long.parseLong(attribute.getAttributeInstances().get(indexObjectivesSon).getValue()));
							} else {
								responsible = null;
							}
							break;
						}
					}
					if (responsible != null)
						cell = new PdfPCell(new Phrase(responsible.getName(), PDFSettings.TEXT_FONT));
					else
						cell = new PdfPCell(new Phrase("-", PDFSettings.TEXT_FONT));

					table.addCell(cell);

					if (son.getLevelValue() != null)
						cell = new PdfPCell(
								new Phrase(decimalFormatDbl.format(son.getLevelValue()) + "%", PDFSettings.TEXT_FONT));
					else
						cell = new PdfPCell(new Phrase(Util.PERCENTAGE_PATTERN, PDFSettings.TEXT_FONT));
					table.addCell(cell);
					indexObjectivesSon++;
				}
				document.add(table);
			} else if (sonsList.getList().get(0).getLevel().isIndicator()) {
				sonParagraphLabel.setSpacingBefore(PDFSettings.PARAGRAPH_SPACING);
				sonParagraphLabel.add(new Phrase("Indicadores", PDFSettings.TITLE_FONT));
				document.add(sonParagraphLabel);

				PdfPTable table = new PdfPTable(5);
				table.setSpacingBefore(PDFSettings.PARAGRAPH_SPACING / 2);
				table.setWidthPercentage(100);

				PdfPCell cell = new PdfPCell(new Phrase("Nome", PDFSettings.TEXT_FONT));
				cell.setHorizontalAlignment(Element.ALIGN_CENTER);
				table.addCell(cell);

				cell = new PdfPCell(new Phrase("Responsável", PDFSettings.TEXT_FONT));
				cell.setHorizontalAlignment(Element.ALIGN_CENTER);
				table.addCell(cell);

				cell = new PdfPCell(new Phrase("Início", PDFSettings.TEXT_FONT));
				cell.setHorizontalAlignment(Element.ALIGN_CENTER);
				table.addCell(cell);

				cell = new PdfPCell(new Phrase("Fim", PDFSettings.TEXT_FONT));
				cell.setHorizontalAlignment(Element.ALIGN_CENTER);
				table.addCell(cell);

				cell = new PdfPCell(new Phrase("Rendimento", PDFSettings.TEXT_FONT));
				cell.setHorizontalAlignment(Element.ALIGN_CENTER);
				table.addCell(cell);
				int indexIndicatorsSon = 0;
				for (StructureLevelInstance son : sonsList.getList()) {
					cell = new PdfPCell(new Phrase(son.getName(), PDFSettings.TEXT_FONT));
					table.addCell(cell);

					List<Attribute> sonAttrList = this.structureBS.retrieveLevelSonsAttributes(son);
					User responsible = new User();
					Date beginDate = new Date();
					Date endDate = new Date();
					for (Attribute attribute : sonAttrList) {
						if (attribute.getType().equals(ResponsibleField.class.getCanonicalName())) {
							if (attribute.getAttributeInstances().get(indexIndicatorsSon) != null) {
								responsible = this.userBS.existsByUser(
										Long.parseLong(attribute.getAttributeInstances().get(indexIndicatorsSon).getValue()));
							} else {
								responsible = null;
							}
						} else if (attribute.isBeginField()) {
							if (attribute.getAttributeInstances().get(indexIndicatorsSon) != null)
								beginDate = attribute.getAttributeInstances().get(indexIndicatorsSon).getValueAsDate();
							else
								beginDate = null;
						} else if (attribute.isEndField()) {
							if (attribute.getAttributeInstances().get(indexIndicatorsSon) != null)
								endDate = attribute.getAttributeInstances().get(indexIndicatorsSon).getValueAsDate();
							else
								endDate = null;
						}
					}

					if (responsible != null)
						cell = new PdfPCell(new Phrase(responsible.getName(), PDFSettings.TEXT_FONT));
					else
						cell = new PdfPCell(new Phrase("-", PDFSettings.TEXT_FONT));
					table.addCell(cell);
					if (beginDate != null) {
						cell = new PdfPCell(new Phrase(brDateFormat.format(beginDate), PDFSettings.TEXT_FONT));
					} else {
						cell = new PdfPCell(new Phrase("-", PDFSettings.TEXT_FONT));
					}
					table.addCell(cell);

					if (endDate != null) {
						cell = new PdfPCell(new Phrase(brDateFormat.format(endDate), PDFSettings.TEXT_FONT));
					} else {
						cell = new PdfPCell(new Phrase("-", PDFSettings.TEXT_FONT));
					}
					table.addCell(cell);

					if (son.getLevelValue() != null)
						cell = new PdfPCell(
								new Phrase(decimalFormatDbl.format(son.getLevelValue()) + "%", PDFSettings.TEXT_FONT));
					else
						cell = new PdfPCell(new Phrase(Util.PERCENTAGE_PATTERN, PDFSettings.TEXT_FONT));
					table.addCell(cell);
					indexIndicatorsSon++;
				}
				document.add(table);
			} else if (sonsList.getList().get(0).getLevel().isGoal()) {
				sonParagraphLabel.setSpacingBefore(PDFSettings.PARAGRAPH_SPACING);
				sonParagraphLabel.add(new Phrase("Metas", PDFSettings.TITLE_FONT));
				document.add(sonParagraphLabel);

				PdfPTable table = new PdfPTable(3);
				table.setSpacingBefore(PDFSettings.PARAGRAPH_SPACING / 2);
				table.setWidthPercentage(100);

				PdfPCell cell = new PdfPCell(new Phrase("Nome", PDFSettings.TEXT_FONT));
				cell.setHorizontalAlignment(Element.ALIGN_CENTER);
				table.addCell(cell);

				cell = new PdfPCell(new Phrase("Responsável", PDFSettings.TEXT_FONT));
				cell.setHorizontalAlignment(Element.ALIGN_CENTER);
				table.addCell(cell);

				cell = new PdfPCell(new Phrase("Desempenho", PDFSettings.TEXT_FONT));
				cell.setHorizontalAlignment(Element.ALIGN_CENTER);
				table.addCell(cell);

				int indexGoalsSon = 0;
				for (StructureLevelInstance son : sonsList.getList()) {
					cell = new PdfPCell(new Phrase(son.getName(), PDFSettings.TEXT_FONT));
					table.addCell(cell);
					
					List<Attribute> sonAttrList = this.structureBS.retrieveLevelSonsAttributes(son);
					User responsible = new User();
					for (Attribute attribute : sonAttrList) {
						if (attribute.getType().equals(ResponsibleField.class.getCanonicalName())) {
							if (attribute.getAttributeInstances().get(indexGoalsSon) != null) {
								responsible = this.userBS.existsByUser(
										Long.parseLong(attribute.getAttributeInstances().get(indexGoalsSon).getValue()));
							} else {
								responsible = null;
							}
							break;
						}
					}

					if (responsible != null)
						cell = new PdfPCell(new Phrase(responsible.getName(), PDFSettings.TEXT_FONT));
					else
						cell = new PdfPCell(new Phrase("-", PDFSettings.TEXT_FONT));

					table.addCell(cell);

					if (son.getLevelValue() != null)
						cell = new PdfPCell(
								new Phrase(decimalFormatDbl.format(son.getLevelValue()) + "%", PDFSettings.TEXT_FONT));
					else
						cell = new PdfPCell(new Phrase(Util.PERCENTAGE_PATTERN, PDFSettings.TEXT_FONT));
					table.addCell(cell);
					indexGoalsSon++;
				}
				document.add(table);
			}
		}

		document.close();

		InputStream in = new FileInputStream(pdfFile);
		
		TempFilesManager.cleanTempDir(outputDir, prefix);

		return in;
	}

	public static class Params implements ReportGeneratorParams {
		public long levelId;

		public Params(long levelId) {
			this.levelId = levelId;
		}
	}
}
