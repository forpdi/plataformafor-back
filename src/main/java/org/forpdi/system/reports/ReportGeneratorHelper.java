package org.forpdi.system.reports;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.AbstractMap.SimpleEntry;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.forpdi.core.company.CompanyDomainContext;
import org.forpdi.core.storage.StorageService;
import org.forpdi.core.storage.file.Archive;
import org.forpdi.core.storage.file.ArchiveBS;
import org.forpdi.core.utils.Util;
import org.forpdi.system.reports.pdf.PDFSettings;
import org.forpdi.system.reports.pdf.PdfWriterFactory;
import org.forpdi.system.reports.pdf.TOCEvent;
import org.forpdi.system.reports.pdf.htmlparser.HtmlToPdfParser;
import org.forpdi.system.reports.pdf.table.TempFilesManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import com.google.common.io.ByteStreams;
import com.itextpdf.text.BadElementException;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Font.FontFamily;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.BadPdfFormatException;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfAction;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.DottedLineSeparator;

@Component
public class ReportGeneratorHelper {
    @Autowired
    private CompanyDomainContext domain;
	@Autowired
	private ArchiveBS archiveBS;
	@Autowired
	private StorageService storageService;
	@Autowired
	private ResourceLoader resourceLoader;

    private final int COVER_PAGE_INDEX = 1;

    public void closePdfReader(PdfReader pdfReader) {
        if (pdfReader != null) {
            pdfReader.close();
        }
    }

    public void manipulatePdf(String src, String dest, int unnumbered, String platform)
    		throws IOException, DocumentException {
    	manipulatePdf(src, dest, unnumbered, platform, Collections.emptySet(), true);
    }
    
    public void manipulatePdf(String src, String dest, int unnumbered, String platform, boolean hasCoverPage)
    		throws IOException, DocumentException {
    	manipulatePdf(src, dest, unnumbered, platform, Collections.emptySet(), hasCoverPage);
    }

    public void manipulatePdf(String src, String dest, int unnumbered, String platform, Set<Integer> rotatedPages, boolean hasCoverPage)
            throws IOException, DocumentException {
        PdfReader reader = new PdfReader(src);
        int numberOfPages = reader.getNumberOfPages();
        PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(dest));

    	Image logoImage = null;
        Image pageBackground = null;
        Image pageBackgroundCover = null;

        if (platform != null) {
        	logoImage = getPageLogo(platform);
            pageBackground = getPageBackground("/img/" + platform + "-page-background.png");
            pageBackgroundCover = getPageBackground("/img/" + platform + "-cover.png");
        }
    	
        for (int page = 1; page <= numberOfPages; page += 1) {
        	PdfContentByte pdfContent = stamper.getOverContent(page);
        	Document document = pdfContent.getPdfDocument();

            boolean isRotatedPage = rotatedPages.contains(page);
            boolean isCoverPage = page == COVER_PAGE_INDEX;
            
            if (!isRotatedPage && platform != null) {
                if (isCoverPage && hasCoverPage) {
            		setImageLogoSize(logoImage, 70);
                	setCoverPageBackground(pdfContent, logoImage, pageBackgroundCover);
                } else {
            		setImageLogoSize(logoImage, 40);
                	setContentPageBackground(pdfContent, logoImage, pageBackground);
                }
            }

            float pageNumberPositionX;
            Font pageNumberFont;
            
            if (isRotatedPage) {
                pageNumberPositionX = PDFSettings.ROTATED_A4.getRight() - 28;
                pageNumberFont = PDFSettings.PAGE_NUMBER_FONT_DARK;
            } else {
        		pageNumberPositionX = document.right() - 28;
        		pageNumberFont = PDFSettings.PAGE_NUMBER_FONT_LIGHT;
            }
            
            if (page > unnumbered)
                ColumnText.showTextAligned(
                        pdfContent,
                        Element.ALIGN_CENTER,
                        new Phrase(String.format("%s", page), pageNumberFont),
                        pageNumberPositionX,
                        document.bottom(),
                        0);
        }
        stamper.close();
        reader.close();
    }

    public int getPageCount(File pdfFile) throws IOException {
        PdfReader reader = null;
        try {
            reader = new PdfReader(pdfFile.getPath());
            return reader.getNumberOfPages();
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    private void setCoverPageBackground(PdfContentByte pdfContent, Image logoImage, Image pageBackground)
            throws IOException, DocumentException {
    	Document document = pdfContent.getPdfDocument();
    	final float logoPositionX = 54;
    	final float logoPositionY = document.getPageSize().getHeight() - logoImage.getScaledHeight() - 103;
    	
		logoImage.setAbsolutePosition(logoPositionX, logoPositionY);

		pdfContent.addImage(logoImage);
    	        
        pdfContent.addImage(pageBackground);
    }

    private void setContentPageBackground(PdfContentByte pdfContent, Image logoImage, Image pageBackground)
            throws IOException, DocumentException {
    	Document document = pdfContent.getPdfDocument();
    	final float logoPositionX = document.getPageSize().getWidth() - logoImage.getScaledWidth() - 18;
    	final float logoPositionY = document.getPageSize().getHeight() - logoImage.getScaledHeight() - 12;
    	
		logoImage.setAbsolutePosition(logoPositionX, logoPositionY);
		
		pdfContent.addImage(logoImage);
        
        pdfContent.addImage(pageBackground);
    }
    
    private Image getPageLogo(String platform) throws BadElementException, IOException {
        Image image;
    
        if ("platfor".equalsIgnoreCase(platform)) {
            InputStream is = getClass().getResourceAsStream("/img/" + platform + "-logo.png");
            if (is == null) {
                throw new FileNotFoundException("Logo file not found for platform: " + platform);
            }
            image = Image.getInstance(ByteStreams.toByteArray(is));
        } else if (domain.get().getCompany().hasLogo()) {
            Archive logoArchive = domain.get().getCompany().getLogoArchive();
    		InputStream is = storageService.retrieveFile(logoArchive.getFileLink());
    		image = Image.getInstance(ByteStreams.toByteArray(is));
    	} else {
    		InputStream is = getClass().getResourceAsStream("/img/" + platform + "-logo.png");
    		image = Image.getInstance(ByteStreams.toByteArray(is));
    	}

		return image;
    }
    
    private void setImageLogoSize(Image logoImage, float height) {
		float ratio = logoImage.getScaledWidth() / logoImage.getScaledHeight();
		logoImage.scaleAbsoluteHeight(height);
		logoImage.scaleAbsoluteWidth(ratio * logoImage.getScaledHeight());
    }

    private Image getPageBackground(String imgPath)
            throws MalformedURLException, IOException, DocumentException {
    	Resource e = resourceLoader.getResource("classpath:" + imgPath);
        InputStream is = e.getInputStream();
        Image pageBackground = Image.getInstance(ByteStreams.toByteArray(is));

        pageBackground.scaleToFit(PageSize.A4);
        pageBackground.setAbsolutePosition(0, 0);
        return pageBackground;
    }

    public Paragraph paragraphSpacing() {
        Paragraph spacing = new Paragraph("");
        spacing.setSpacingBefore(PDFSettings.PARAGRAPH_SPACING);
        return spacing;
    }

    public void copyDocumentContent(PdfCopy combinedContent, PdfReader document)
            throws BadPdfFormatException, IOException {
        PdfImportedPage page;
        int n = document.getNumberOfPages();

        for (int i = 0; i < n;) {
            page = combinedContent.getImportedPage(document, ++i);
            combinedContent.addPage(page);
        }
        document.close();
    }

    public Paragraph newSectionTitle(String title) {
        Paragraph sectionTitle = new Paragraph(title, PDFSettings.TITLE_FONT);

        sectionTitle.setSpacingBefore(PDFSettings.PARAGRAPH_SPACING);
        sectionTitle.setSpacingAfter(PDFSettings.PARAGRAPH_SPACING);
        return sectionTitle;
    }

    public Paragraph newSummarySection(String sectionName) {
        Chunk sectionChunk = new Chunk(sectionName, PDFSettings.TITLE_FONT);
        sectionChunk.setGenericTag(sectionName);

        Paragraph section = new Paragraph(sectionChunk);
        section.setLeading(PDFSettings.INTER_LINE_SPACING);
        section.setSpacingAfter(PDFSettings.PARAGRAPH_SPACING);

        return section;
    }

    public Paragraph listItem(String itemName) {
        Chunk bulletPointSymbol = new Chunk(String.valueOf((char) 108), new Font(FontFamily.ZAPFDINGBATS, 7));
        Paragraph listItem = new Paragraph("", PDFSettings.TEXT_FONT);

        listItem.setIndentationLeft(PDFSettings.INTER_LINE_SPACING);
        listItem.add(bulletPointSymbol);
        listItem.add("  " + itemName);

        return listItem;
    }
    
    public Paragraph listItemBold(String itemName) {
        Chunk bulletPointSymbol = new Chunk(String.valueOf((char) 108), new Font(FontFamily.ZAPFDINGBATS, 7));
        Paragraph listItem = new Paragraph("", PDFSettings.TITLE_FONT);

        listItem.setIndentationLeft(PDFSettings.INTER_LINE_SPACING);
        listItem.add(bulletPointSymbol);
        listItem.add("  " + itemName);

        return listItem;
    }
    
    public Paragraph listItemWithoutBullet(String itemName) {
        Paragraph listItem = new Paragraph("", PDFSettings.TEXT_FONT);

        listItem.setIndentationLeft(PDFSettings.INTER_LINE_SPACING);
        listItem.add("  " + itemName);

        return listItem;
    }

    public Paragraph attributeDisplay(String attributeDescription, String attributeValue) {
        Phrase displayPhrase = new Phrase();
        Paragraph displayItem = new Paragraph();
        
        displayPhrase.add(listItem(attributeDescription, PDFSettings.TEXT_FONT_BOLD));
        displayPhrase.add(new Phrase(attributeValue, PDFSettings.TEXT_FONT));
        
        displayItem.add(displayPhrase);
        displayItem.setIndentationLeft(PDFSettings.INTER_LINE_SPACING);
        displayItem.setSpacingBefore(PDFSettings.PARAGRAPH_SPACING_SM/2);
        
        return displayItem;
    }
    
    public Paragraph richTextAttributeDisplay(String fieldName, String fieldValue, Document document, Font listItemfont)
            throws FileNotFoundException, IOException, DocumentException {
        Paragraph displayParagraph = listItem(fieldName, listItemfont);
        displayParagraph.add(generateRichTextParagraph(fieldValue, document));
        return displayParagraph;
    }
    
    public Paragraph listItem(String itemName, Font itemFont) {
        Chunk bulletPointSymbol = new Chunk(String.valueOf((char) 108), new Font(FontFamily.ZAPFDINGBATS, 7));
        Paragraph listItem = new Paragraph("", itemFont);

        listItem.setIndentationLeft(PDFSettings.INTER_LINE_SPACING);
        listItem.add(bulletPointSymbol);
        listItem.add("  " + itemName + ": ");

        return listItem;
    }

    private Paragraph generateRichTextParagraph(String fieldValue, Document document)
            throws FileNotFoundException, IOException, DocumentException {
        List<Element> textElements = new HtmlToPdfParser(fieldValue, document).parse();
        Paragraph text = new Paragraph();
        text.setFont(PDFSettings.TEXT_FONT);
        text.setIndentationLeft(PDFSettings.FIRST_LINE_IDENT);
        for (Element textElement : textElements) {
            text.add(textElement);
        }
        return text;
    }
        
    public Paragraph richTextAttributeDisplay(String fieldName, String fieldValue, Document document)
            throws FileNotFoundException, IOException, DocumentException {
        return richTextAttributeDisplay(fieldName, fieldValue, document, PDFSettings.TEXT_FONT);
    }

    public void setDimensions(Document document) {
        document.setPageSize(PageSize.A4);
        document.setMargins(PDFSettings.HORIZONTAL_MARGIN, PDFSettings.HORIZONTAL_MARGIN, PDFSettings.VERTICAL_MARGIN,
                PDFSettings.VERTICAL_MARGIN);
    }
    
    public void generateCover(File coverPdfFile, String title, Map<String, String> coverTextContent)
            throws DocumentException, IOException, MalformedURLException {
        Document coverDocument = new Document();
        PdfWriter coverWriter = PdfWriterFactory.create(coverDocument, coverPdfFile);

        setDimensions(coverDocument);
        coverDocument.open();

        // CABEÇALHO
        Paragraph coverTitle = new Paragraph(title, PDFSettings.COVER_PAGE_TITLE_FONT);
        coverTitle.setAlignment(Element.ALIGN_CENTER);
        coverTitle.setSpacingBefore(5 * PDFSettings.PARAGRAPH_SPACING);
        
        Paragraph localizationPhrase = new Paragraph(this.domain.get().getCompany().getLocalization(),
                PDFSettings.FOOTER_FONT);

        localizationPhrase.setAlignment(Element.ALIGN_CENTER);

        Paragraph preCoverTitleSpace = new Paragraph(" ");
        preCoverTitleSpace.setLeading(coverDocument.getPageSize().getHeight() / 4);
        coverDocument.add(preCoverTitleSpace);
        coverDocument.add(coverTitle);

        coverDocument.newPage();

        // FOLHA DE ROSTO
        PdfContentByte cb = coverWriter.getDirectContent();

        generateSecondaryCoverContent(coverDocument, coverTextContent);

        Calendar cal = Calendar.getInstance();

        Phrase periodPhrase = new Phrase(
                String.valueOf(cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault())
                        + " de " + String.valueOf(cal.get(Calendar.YEAR))),
                PDFSettings.FOOTER_FONT);

        ColumnText.showTextAligned(cb, Element.ALIGN_CENTER, periodPhrase,
                (coverDocument.right() - coverDocument.left()) / 2 + coverDocument.left(),
                coverDocument.bottom() + 15, 0);
        ColumnText.showTextAligned(cb, Element.ALIGN_CENTER, localizationPhrase,
                (coverDocument.right() - coverDocument.left()) / 2 + coverDocument.left(),
                coverDocument.bottom() + 30, 0);
        coverDocument.newPage();
        coverDocument.close();
    }

    private void generateSecondaryCoverContent(Document document, Map<String, String> coverTextContent) throws DocumentException {
        Paragraph company = new Paragraph(domain.get().getCompany().getName(), PDFSettings.MAIN_TITLE_FONT);
        company.setAlignment(Element.ALIGN_CENTER);
        company.setSpacingBefore(PDFSettings.PARAGRAPH_SPACING);
        document.add(company);
        boolean firstLabel = true;

		for (Entry<String, String> entry : coverTextContent.entrySet()) {
	        Paragraph labelTitle = new Paragraph(entry.getKey(), PDFSettings.TITLE_FONT);
	        labelTitle.setAlignment(Element.ALIGN_CENTER);
	        labelTitle.setSpacingBefore(firstLabel ? 12 * PDFSettings.PARAGRAPH_SPACING : PDFSettings.PARAGRAPH_SPACING_SM);	        
	        document.add(labelTitle);
	        Paragraph content = new Paragraph(entry.getValue(), PDFSettings.SUB_TITLE_FONT);
	        content.setAlignment(Element.ALIGN_CENTER);
	        document.add(content);
	        firstLabel = false;
		}
    }

    public int generateSummary(File finalSummaryPdfFile, TOCEvent event, int Npages)
            throws DocumentException, IOException {

        File outputDir;

        outputDir = TempFilesManager.getTempDir();

        final String prefix = String.format("frisco-report-export-%d", System.currentTimeMillis());
        File summaryPdfFile = new File(outputDir, String.format("%s-summary.pdf", prefix));

        Document summaryDocument = new Document();
        PdfWriterFactory.create(summaryDocument, summaryPdfFile);

        setDimensions(summaryDocument);
        summaryDocument.open();

        Paragraph summaryTitle = new Paragraph("Sumário", PDFSettings.MAIN_TITLE_FONT);
        summaryTitle.setAlignment(Element.ALIGN_CENTER);
        summaryTitle.setSpacingAfter(PDFSettings.PARAGRAPH_SPACING);
        summaryDocument.add(summaryTitle);

        Chunk dottedLine = new Chunk(new DottedLineSeparator());
        List<SimpleEntry<String, SimpleEntry<String, Integer>>> entries = event.getTOC();
        Paragraph p;
        int summaryCountPages = 0;
        for (SimpleEntry<String, SimpleEntry<String, Integer>> entry : entries) {
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

        summaryCountPages = summaryAux.getNumberOfPages() + Npages;

        summaryAux.close();

        Document finalSummaryDocument = new Document();
        PdfWriterFactory.create(finalSummaryDocument, finalSummaryPdfFile);

        setDimensions(finalSummaryDocument);
        finalSummaryDocument.open();

        finalSummaryDocument.add(summaryTitle);
        for (SimpleEntry<String, SimpleEntry<String, Integer>> entry : entries) {
        	int counter = entry.getKey().length() - entry.getKey().replaceAll(String.valueOf("\\d+(?=\\.)"), "").length();
            Chunk chunk = new Chunk(entry.getKey(), PDFSettings.SUMMARY_FONT);
            SimpleEntry<String, Integer> value = entry.getValue();
            chunk.setAction(PdfAction.gotoLocalPage(value.getKey(), false));
            p = new Paragraph(chunk);
            p.add(dottedLine);
            chunk = new Chunk(String.valueOf(value.getValue() + summaryCountPages), PDFSettings.SUMMARY_FONT);
            chunk.setAction(PdfAction.gotoLocalPage(value.getKey(), false));
            p.add(chunk);
            p.setIndentationLeft((counter - 1) * PDFSettings.PARAGRAPH_SPACING);
            finalSummaryDocument.add(p);
        }
        finalSummaryDocument.close();

        TempFilesManager.deleteTempFile(summaryPdfFile);

        return summaryCountPages;
    }
    
	public Element generateItemFieldElement(String fileLink)
			throws IOException, BadElementException {
		if (fileLink == null || fileLink.trim().isEmpty()) {
			throw new IllegalArgumentException("File link is invalid.");
		}

		String fileExtension = FilenameUtils.getExtension(fileLink).toLowerCase();
		if (!fileExtension.matches("gif|jpeg|jpg|bmp|tiff|png|ai|psd|svg|svgz|pdf")) {
			throw new IllegalArgumentException("File type not supported.");
		}

		Long archiveId;
		try {
			archiveId = Long.parseLong(FilenameUtils.getBaseName(fileLink));
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Invalid file identifier.");
		}

		Archive archive = archiveBS.exists(archiveId, Archive.class);
		if (archive == null) {
			throw new IllegalArgumentException("File not found.");
		}
		if (!archive.getCompany().getId().equals(this.domain.get().getCompany().getId())) {
			throw new RuntimeException("Unauthorized access.");
		}

		if (fileExtension.matches("gif|jpeg|jpg|bmp|tiff|png|ai|psd|svg|svgz")) {
			try (InputStream is = storageService.retrieveFile(fileLink)) {
				byte[] imageData = ByteStreams.toByteArray(is);
				Image image = Image.getInstance(imageData);
				image.scaleToFit(400, 350);
				return image;
			} catch (IOException ex) {
				throw new RuntimeException("Error processing image file", ex);
			}
		} else if (fileExtension.equals("pdf")) {
			Chunk linkToFile = new Chunk(archive.getName());
			Font fontBlue = new Font(linkToFile.getFont().getBaseFont(), linkToFile.getFont().getSize(),
					Font.NORMAL, BaseColor.BLUE);
			linkToFile.setFont(fontBlue);
			linkToFile.setAnchor(Util.getDownloadFilesURL(fileLink, domain.get().getBaseUrl()));
			linkToFile.setUnderline(0.1F, -2F);
			Paragraph attLinkField = new Paragraph();
			attLinkField.add(linkToFile);
			attLinkField.setFirstLineIndent(38f);
			return attLinkField;
		} else {
			throw new IllegalArgumentException("File type not supported.");
		}
	}
}
