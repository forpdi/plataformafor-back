package org.forpdi.planning.document.bootstrap;

import org.forpdi.core.common.Criteria;
import org.forpdi.core.common.HibernateDAO;
import org.forpdi.core.common.HibernateDAO.TransactionalOperation;
import org.forpdi.core.common.Restrictions;
import org.forpdi.planning.attribute.types.DateField;
import org.forpdi.planning.attribute.types.NumberField;
import org.forpdi.planning.attribute.types.ScheduleField;
import org.forpdi.planning.attribute.types.SelectField;
import org.forpdi.planning.attribute.types.SelectPlan;
import org.forpdi.planning.attribute.types.StrategicObjective;
import org.forpdi.planning.attribute.types.TableField;
import org.forpdi.planning.attribute.types.TextArea;
import org.forpdi.planning.attribute.types.TextField;
import org.forpdi.planning.attribute.types.TotalField;
import org.forpdi.planning.document.Document;
import org.forpdi.planning.document.DocumentAttribute;
import org.forpdi.planning.document.DocumentSection;
import org.forpdi.planning.fields.OptionsField;
import org.forpdi.planning.fields.schedule.Schedule;
import org.forpdi.planning.fields.table.TableFields;
import org.forpdi.planning.fields.table.TableStructure;
import org.forpdi.planning.plan.PlanMacro;
import org.hibernate.HibernateException;

/**
 * Helper que cria o documento estipulado pelo documento de referência do
 * FORPLAD. Ele cria o documento dentro de um plano macro.
 * 
 * @see https://docs.google.com/document/d/1IJjCoGLbR4bsVM_o-
 *      tlyf_5H1efAF24WcaIvoaEouq0/edit#heading=h.fr3x6z3vdjm
 * @author Renato Oliveira
 *
 */
public class ForpladHelper {

	private final HibernateDAO dao;

	public ForpladHelper(HibernateDAO dao) {
		this.dao = dao;
	}

	public Document initializeDocument(PlanMacro plan) {
		if (plan == null) {
			throw new IllegalArgumentException("You must provide a PlanMacro to initialize a Forplad Document.");
		}

		Document document = this.retrieveByPlan(plan);
		if (document != null) {
			throw new IllegalArgumentException("The provided PlanMacro already has a Document.");
		}

		DocumentCreator creator = new DocumentCreator(plan);
		this.dao.execute(creator);

		return creator.document;
	}

	public Document retrieveByPlan(PlanMacro plan) {
		Criteria criteria = this.dao.newCriteria(Document.class).add(Restrictions.eq("plan", plan));
		return (Document) criteria.uniqueResult();
	}

	private DocumentSection createSection(HibernateDAO dao, Document doc, DocumentSection parent,
			String name, int sequence, boolean leaf, boolean preText) {
		DocumentSection s = new DocumentSection();
		s.setDocument(doc);
		s.setParent(parent);
		s.setName(name);
		s.setSequence(sequence);
		s.setLeaf(leaf);
		s.setPreTextSection(preText);
		dao.persist(s);
		return s;
	}

	private DocumentAttribute addAttr(HibernateDAO dao, DocumentSection section, String name, String type, int seq,
			boolean required) {
		DocumentAttribute a = new DocumentAttribute();
		a.setName(name);
		a.setSection(section);
		a.setType(type);
		a.setSequence(seq);
		a.setRequired(required);
		dao.persist(a);
		return a;
	}

	private void addTextArea(HibernateDAO dao, DocumentSection section, String label, int seq) {
		addAttr(dao, section, label, TextArea.generateId(TextArea.class), seq, false);
	}

	private TableFields createTable(HibernateDAO dao, DocumentAttribute tableAttr) {
		TableFields tf = new TableFields();
		tf.setAttributeId(tableAttr.getId());
		tf.setIsDocument(true);
		dao.persist(tf);
		return tf;
	}

	private TableStructure addColumn(HibernateDAO dao, TableFields table, String label, String type, boolean inTotal) {
		TableStructure ts = new TableStructure();
		ts.setLabel(label);
		ts.setType(type);
		ts.setTableFields(table);
		ts.setInTotal(inTotal);
		dao.persist(ts);
		return ts;
	}

	private void addOptions(HibernateDAO dao, Long attributeId, Long columnId, String... labels) {
		for (String l : labels) {
			OptionsField op = new OptionsField();
			op.setAttributeId(attributeId);
			op.setColumnId(columnId);
			op.setDocument(true);
			op.setLabel(l);
			dao.persist(op);
		}
	}

	private void addSchedule(HibernateDAO dao, DocumentAttribute attr, boolean periodicity) {
		Schedule schedule = new Schedule();
		schedule.setPeriodicityEnable(periodicity);
		schedule.setAttributeId(attr.getId());
		schedule.setIsDocument(true);
		dao.persist(schedule);
	}

	private void addSingleTextAreaSubsection(HibernateDAO dao, Document doc, DocumentSection parent,
			String subsectionName, int seq, String textAreaLabel) {
		DocumentSection sub = createSection(dao, doc, parent, subsectionName, seq, true, false);
		addTextArea(dao, sub, textAreaLabel, 1);
	}

	/**
	 * Classe privada que insere a estrutura do documento FORPLAD em uma única
	 * transação do banco de dados.
	 * 
	 * @author Renato Oliveira
	 *
	 */
	protected class DocumentCreator implements TransactionalOperation {
		public final Document document;

		private static final String[] HABILITACOES = {
				"Tecnólogo", "Bacharelado", "Licenciatura", "Especialização", "MBA", "Mestrado", "Doutorado"
		};
		private static final String[] MODALIDADES = {
				"Presencial", "À distância", "Semipresencial"
		};
		private static final String[] TURNOS = {
				"Integral", "Diurno", "Noturno", "Vespertino"
		};

		public DocumentCreator(PlanMacro plan) {
			this.document = new Document();
			this.document.setPlan(plan);
			this.document.setDescription(
					"O PDI, elaborado para um período de cinco anos, é o documento" +
							" que identifica a IES no que diz respeito à sua filosofia de trabalho, à missão a que se propõe, "
							+
							"às diretrizes pedagógicas que orientam suas ações, à sua estrutura organizacional " +
							"e às atividades acadêmicas que desenvolve e/ou que pretende desenvolver " +
							"Atenção: Evite colar textos formatados nas seções do Documento do PDI.");
			this.document.setTitle("Documento - " + plan.getName());
		}

		@Override
		public void execute(HibernateDAO dao) throws HibernateException {
			DocumentSection section, subsection;
			DocumentAttribute attr;
			Schedule schedule;
			TableFields tableFields;
			TableStructure tableStructure;
			OptionsField optionsField;

			dao.persist(this.document);

			// SEÇÕES NÃO NUMERADAS - PRÉ TEXTUAIS

			// Seção 1 - Ficha técnica
			DocumentSection s1 = createSection(dao, document, null, "Ficha técnica", 1, true, true);
			var tf1 = createTable(dao,
					addAttr(dao, s1, "Ficha Técnica", TableField.class.getCanonicalName(), 1, false));
			addColumn(dao, tf1, "Nome", TextField.generateId(TextField.class), false);
			addColumn(dao, tf1, "Cargo", TextField.generateId(TextField.class), false);

			// Seção 2 - Histórico de versões
			DocumentSection s2 = createSection(dao, document, null, "Histórico de versões", 2, true, true);
			var tf2 = createTable(dao,
					addAttr(dao, s2, "Histórico de versões", TableField.class.getCanonicalName(), 1, false));
			addColumn(dao, tf2, "Data", DateField.generateId(DateField.class), false);
			addColumn(dao, tf2, "Versão", TextField.generateId(TextField.class), false);
			addColumn(dao, tf2, "Descrição", TextField.generateId(TextField.class), false);
			addColumn(dao, tf2, "Autor", TextField.generateId(TextField.class), false);

			// Seção 3 - Lista de abreviações
			DocumentSection s3 = createSection(dao, document, null, "Lista de abreviações", 3, true, true);
			var tf3 = createTable(dao,
					addAttr(dao, s3, "Lista de abreviações", TableField.class.getCanonicalName(), 1, false));
			addColumn(dao, tf3, "Sigla", TextField.generateId(TextField.class), false);
			addColumn(dao, tf3, "Descrição", TextField.generateId(TextField.class), false);

			// SEÇÕES NUMERADAS

			// Seção 1 - Apresentação
			DocumentSection n1 = createSection(dao, document, null, "Apresentação", 1, true, false);
			addTextArea(dao, n1, "Apresentação do PDI", 1);

			// Seção 2 - Método
			DocumentSection n2 = createSection(dao, document, null, "Método", 2, true, false);
			addTextArea(dao, n2, "Descrição do método utilizado para a elaboração do PDI", 1);

			// Seção 3 - Documentos de referência
			DocumentSection n3 = createSection(dao, document, null, "Documentos de referência", 3, true, false);
			addTextArea(dao, n3, "Documentos de referência mais utilizados para a elaboração do PDI", 1);

			// Seção 4 - Resultados do PDI anterior
			DocumentSection n4 = createSection(dao, document, null, "Resultados do PDI anterior", 4, true, false);
			addTextArea(dao, n4, "Resultados do PDI anterior", 1);

			// Seção 5 - Perfil institucional
			DocumentSection n5 = createSection(dao, document, null, "Perfil institucional", 5, false, false);

			// Subseção 5.1 - Histórico da IES
			DocumentSection n5_1 = createSection(dao, document, n5, "Histórico da IES", 1, true, false);
			addTextArea(dao, n5_1, "Análise do histórico de todos os campus da IES", 1);

			// Subseção 5.2 - Finalidade
			DocumentSection n5_2 = createSection(dao, document, n5, "Finalidade", 2, true, false);
			addTextArea(dao, n5_2, "Finalidade da IES", 1);

			// Subseção 5.3 - Missão
			DocumentSection n5_3 = createSection(dao, document, n5, "Missão, visão e valores", 3, true, false);
			addTextArea(dao, n5_3, "Missão", 1);
			addTextArea(dao, n5_3, "Visão", 2);
			addTextArea(dao, n5_3, "Valores", 3);
			addAttr(dao, n5_3, "Objetivos estratégicos", StrategicObjective.generateId(StrategicObjective.class), 4,
					false);

			// Subseção 5.4 - Área(s) de atuação acadêmica
			DocumentSection n5_4 = createSection(dao, document, n5, "Área(s) de atuação acadêmica", 4, true, false);
			addTextArea(dao, n5_4, "Áreas de atuação acadêmica", 1);

			// Seção 6 - Projeto Pedagógico Institucional (PPI)
			DocumentSection n6 = createSection(dao, document, null, "Projeto Pedagógico Institucional (PPI)", 6, false,
					false);
			addSingleTextAreaSubsection(dao, document, n6, "Inserção regional", 1,
					"Descrição");
			addSingleTextAreaSubsection(dao, document, n6, "Princípios filosóficos e técnico-metodológicos gerais", 2,
					"Descrição dos princípios que norteiam as práticas acadêmicas da IES");
			addSingleTextAreaSubsection(dao, document, n6, "Organização didático-pedagógica da instituição", 3,
					"Organização didático-pedagógica da instituição");
			addSingleTextAreaSubsection(dao, document, n6, "Políticas de ensino", 4,
					"Descrição das políticas de ensino");
			addSingleTextAreaSubsection(dao, document, n6, "Políticas de extensão", 5,
					"Descrição das políticas de extensão");
			addSingleTextAreaSubsection(dao, document, n6, "Políticas de pesquisa", 6,
					"Descrição das políticas de pesquisa adotadas pela IES");
			addSingleTextAreaSubsection(dao, document, n6, "Políticas de gestão", 7,
					"Descrição das políticas de gestão adotadas pela IES");
			addSingleTextAreaSubsection(dao, document, n6, "Responsabilidade social da IES", 8,
					"Descrição da contribuição à inclusão social e ao desenvolvimento econômico e social da região");

			// Seção 7 - Cronograma de implantação e desenvolvimento da
			// instituição e dos cursos
			DocumentSection n7 = createSection(dao, document, null,
					"Cronograma de implantação e desenvolvimento da instituição e dos cursos", 7, false, false);

			// Subseção 7.1 - Descrição da situação atual
			DocumentSection n7_1 = createSection(dao, document, n7, "Descrição da situação atual", 1, true, false);
			addTextArea(dao, n7_1, "Descrição da situação atual", 1);

			// Tabela: Cursos ofertados...
			var tf7_1a = createTable(dao,
					addAttr(dao, n7_1, "Cursos ofertados pela IES", TableField.class.getCanonicalName(), 2, false));
			addColumn(dao, tf7_1a, "Nome do Curso", TextField.generateId(TextField.class), false);
			var colHab1 = addColumn(dao, tf7_1a, "Habilitação", SelectField.class.getCanonicalName(), false);
			addOptions(dao, tf7_1a.getAttributeId(), colHab1.getId(), HABILITACOES);
			var colModal1 = addColumn(dao, tf7_1a, "Modalidade", SelectField.class.getCanonicalName(), false);
			addOptions(dao, tf7_1a.getAttributeId(), colModal1.getId(), MODALIDADES);
			var colRegMat1 = addColumn(dao, tf7_1a, "Regime de matrícula", SelectField.class.getCanonicalName(), false);
			addOptions(dao, tf7_1a.getAttributeId(), colRegMat1.getId(), "Anual", "Semestral", "Bienal");
			addColumn(dao, tf7_1a, "Nº de turmas", NumberField.generateId(NumberField.class), false);
			addColumn(dao, tf7_1a, "Nº de Alunos por turma", NumberField.generateId(NumberField.class), false);
			var colTurno1 = addColumn(dao, tf7_1a, "Turno de funcionamento", SelectField.class.getCanonicalName(),
					false);
			addOptions(dao, tf7_1a.getAttributeId(), colTurno1.getId(), TURNOS);
			addColumn(dao, tf7_1a, "Situação Atual", TextField.generateId(TextField.class), false);

			// Tabela: Cursos que serão expandidos/abertos
			var tf7_1b = createTable(dao, addAttr(dao, n7_1, "Cursos que serão expandidos ou abertos no futuro",
					TableField.class.getCanonicalName(), 3, false));
			addColumn(dao, tf7_1b, "Nome do Curso", TextField.generateId(TextField.class), false);
			var colHab2 = addColumn(dao, tf7_1b, "Habilitação", SelectField.class.getCanonicalName(), false);
			addOptions(dao, tf7_1b.getAttributeId(), colHab2.getId(), HABILITACOES);
			var colModal2 = addColumn(dao, tf7_1b, "Modalidade", SelectField.class.getCanonicalName(), false);
			addOptions(dao, tf7_1b.getAttributeId(), colModal2.getId(), MODALIDADES);
			var colTurno2 = addColumn(dao, tf7_1b, "Turno de funcionamento", SelectField.class.getCanonicalName(),
					false);
			addOptions(dao, tf7_1b.getAttributeId(), colTurno2.getId(), TURNOS);
			addColumn(dao, tf7_1b, "Nº de vagas autorizadas", NumberField.generateId(NumberField.class), false);
			addColumn(dao, tf7_1b, "Nº de vagas a solicitar", NumberField.generateId(NumberField.class), false);
			addColumn(dao, tf7_1b, "Ano previsto para solicitação", TextField.generateId(NumberField.class), false);

			// Cronograma de expansão (schedule)
			var attrCron = addAttr(dao, n7_1, "Cronograma de expansão", ScheduleField.class.getCanonicalName(), 4,
					false);
			addSchedule(dao, attrCron, false);

			// 7.2 Objetivos e metas
			DocumentSection n7_2 = createSection(dao, document, n7, "Objetivos e metas", 2, true, false);
			addTextArea(dao, n7_2, "Descrição do plano de implantação e desenvolvimento da instituição e dos cursos",
					1);
			addAttr(dao, n7_2, "Selecione o plano de metas correspondente", SelectPlan.class.getCanonicalName(), 2,
					false);

			// Seção 8 - Perfil do corpo docente
			DocumentSection sec8 = createSection(dao, document, null, "Perfil do corpo docente", 8, false, false);

			// Subseção 8.1 - Composição
			DocumentSection s81 = createSection(dao, document, sec8, "Composição", 1, true, false);
			DocumentAttribute compClasse = addAttr(dao, s81, "Evolução no quadro permanente de docentes por classe/ano",
					TableField.class.getCanonicalName(), 1, false);
			TableFields tfClasse = createTable(dao, compClasse);
			addColumn(dao, tfClasse, "Ano", NumberField.generateId(NumberField.class), false);
			addColumn(dao, tfClasse, "Auxiliar", NumberField.generateId(NumberField.class), true);
			addColumn(dao, tfClasse, "Assistente", NumberField.generateId(NumberField.class), true);
			addColumn(dao, tfClasse, "Adjunto", NumberField.generateId(NumberField.class), true);
			addColumn(dao, tfClasse, "Associado", NumberField.generateId(NumberField.class), true);
			addColumn(dao, tfClasse, "Titular", NumberField.generateId(NumberField.class), true);
			addColumn(dao, tfClasse, "Total", TotalField.generateId(TotalField.class), false);

			DocumentAttribute compTitul = addAttr(dao, s81, "Evolução no quadro permanente de docentes por titulação",
					TableField.class.getCanonicalName(), 2, false);
			TableFields tfTitul = createTable(dao, compTitul);
			addColumn(dao, tfTitul, "Ano", NumberField.generateId(NumberField.class), false);
			addColumn(dao, tfTitul, "Graduação", NumberField.generateId(NumberField.class), true);
			addColumn(dao, tfTitul, "Especialização", NumberField.generateId(NumberField.class), true);
			addColumn(dao, tfTitul, "Mestrado", NumberField.generateId(NumberField.class), true);
			addColumn(dao, tfTitul, "Doutorado", NumberField.generateId(NumberField.class), true);
			addColumn(dao, tfTitul, "Pós-doutorado", NumberField.generateId(NumberField.class), true);
			addColumn(dao, tfTitul, "Total", TotalField.generateId(TotalField.class), false);

			DocumentAttribute compRegime = addAttr(dao, s81,
					"Evolução no quadro permanente de docentes por regime de trabalho",
					TableField.class.getCanonicalName(), 3, false);
			TableFields tfRegime = createTable(dao, compRegime);
			addColumn(dao, tfRegime, "Ano", NumberField.generateId(NumberField.class), false);
			addColumn(dao, tfRegime, "Dedicação exclusiva", NumberField.generateId(NumberField.class), true);
			addColumn(dao, tfRegime, "40h", NumberField.generateId(NumberField.class), true);
			addColumn(dao, tfRegime, "20h", NumberField.generateId(NumberField.class), true);
			addColumn(dao, tfRegime, "Total", TotalField.generateId(TotalField.class), false);

			// Subseção 8.2 - Plano de carreira
			addSingleTextAreaSubsection(dao, document, sec8, "Plano de carreira", 2, "Plano de carreira");

			// Subseção 8.3 - Critérios de seleção e contratação
			addSingleTextAreaSubsection(dao, document, sec8, "Critérios de seleção e contratação", 3,
					"Critérios de seleção e contratação");

			// Subseção 8.4 - Procedimentos para substituição (definitiva e
			// eventual) dos professores do quadro
			addSingleTextAreaSubsection(dao, document, sec8,
					"Procedimentos para substituição (definitiva e eventual) dos professores do quadro", 4,
					"Procedimentos adotados pela IES, visando recomposição e substituição de professores do quadro");

			// Subseção 8.5 - Cronograma
			DocumentSection s85 = createSection(dao, document, sec8, "Cronograma e plano de expansão do corpo docente",
					5, true, false);
			DocumentAttribute cronDoc = addAttr(dao, s85, "Cronograma e plano de expansão do corpo docente",
					ScheduleField.class.getCanonicalName(), 1, false);
			addSchedule(dao, cronDoc, false);

			// Subseção 8.6 - Objetivos e metas
			DocumentSection s86 = createSection(dao, document, sec8, "Objetivos e metas", 6, true, false);
			addTextArea(dao, s86, "Descrição do plano de expansão do corpo docente", 1);
			section = createSection(dao, document, null,
					"Perfil do corpo técnico administrativo", 9, false, false);

			// Subseção 9.1 - Composição
			subsection = createSection(dao, document, section, "Composição", 1, true, false);
			addTextArea(dao, subsection, "Composição", 1);

			// Subseção 9.2 - Plano de carreira
			addSingleTextAreaSubsection(dao, document, section, "Plano de carreira", 2, "Plano de carreira");

			// Subseção 9.3 - Critérios de seleção e contratação
			addSingleTextAreaSubsection(dao, document, section,
					"Critérios de seleção e contratação", 3,
					"Apresentar os critérios de seleção e contratação de técnicos utilizados pela IES");

			// Subseção 9.4 - Procedimentos para substituição
			addSingleTextAreaSubsection(dao, document, section,
					"Procedimentos para substituição (definitiva e eventual) dos técnicos do quadro", 4,
					"Descrição dos procedimentos adotados pela IES, visando recomposição e substituição de técnicos do quadro");

			// Subseção 9.5 - Cronograma e plano de expansão
			subsection = createSection(dao, document, section,
					"Cronograma e plano de expansão do corpo técnico administrativo", 5, true, false);
			attr = addAttr(dao, subsection, "Cronograma", ScheduleField.class.getCanonicalName(), 1, false);
			addSchedule(dao, attr, false);

			// Subseção 9.6 - Objetivos e metas
			subsection = createSection(dao, document, section, "Objetivos e metas", 6, true, false);
			addTextArea(dao, subsection, "Descrição do plano de expansão do corpo técnico administrativo", 1);
			addAttr(dao, subsection, "Selecione o plano de metas correspondente", SelectPlan.class.getCanonicalName(),
					2, false);

			// =====================
			// Seção 10 - Organização administrativa da IES
			// =====================
			section = createSection(dao, document, null,
					"Organização administrativa da IES", 10, /* leaf */ false, /* preText */ false);

			// 10.1 - Estrutura organizacional, instâncias de decisão e organograma
			// institucional e acadêmico
			addSingleTextAreaSubsection(
					dao, document, section,
					"Estrutura organizacional, instâncias de decisão e organograma institucional e acadêmico",
					1,
					"Descrever a estrutura organizacional da IES");

			// 10.2 - Órgãos Colegiados
			addSingleTextAreaSubsection(
					dao, document, section,
					"Órgãos Colegiados",
					2,
					"Descrever as competências e composição de cada órgão colegiado");

			// 10.3 - Órgãos de apoio às atividades acadêmicas
			addSingleTextAreaSubsection(
					dao, document, section,
					"Órgãos de apoio às atividades acadêmicas",
					3,
					"Descrever as competências e composição de cada órgão de apoio");

			// =====================
			// Seção 11 - Políticas de atendimento aos discentes
			// =====================
			section = createSection(dao, document, null,
					"Políticas de atendimento aos discentes", 11, /* leaf */ false, /* preText */ false);

			// 11.1 - Programas de apoio pedagógico e financeiro (bolsas)
			addSingleTextAreaSubsection(
					dao, document, section,
					"Programas de apoio pedagógico e financeiro (bolsas)",
					1,
					"Descrição de todos os programas de apoio pedagógico e financeiro");

			// 11.2 - Estímulos à permanência
			addSingleTextAreaSubsection(
					dao, document, section,
					"Estímulos à permanência",
					2,
					"Estímulos à permanência");

			// 11.3 - Organização estudantil
			addSingleTextAreaSubsection(
					dao, document, section,
					"Organização estudantil",
					3,
					"Descrição dos espaços para participação e convivência estudantil, bem como dos órgãos de representatividade discente");

			// 11.4 - Acompanhamento dos egressos
			addSingleTextAreaSubsection(
					dao, document, section,
					"Acompanhamento dos egressos",
					4,
					"Descrição das formas de relação com os egressos");

			// 11.5 - Objetivos e metas
			subsection = createSection(dao, document, section, "Objetivos e metas", 5, true, false);
			addTextArea(dao, subsection, "Descrição do plano de atendimento aos discentes", 1);
			addAttr(dao, subsection, "Selecione o plano de metas correspondente", SelectPlan.class.getCanonicalName(),
					2, false);

			// =====================
			// Seção 12 - Infraestrutura
			// =====================
			section = createSection(dao, document, null, "Infraestrutura", 12, /* leaf */ false, /* preText */ false);

			// 12.1 - Infraestrutura física
			addSingleTextAreaSubsection(
					dao, document, section,
					"Infraestrutura física",
					1,
					"Detalhar infraestrutura física");

			// 12.2 - Biblioteca (texto + cronograma)
			subsection = createSection(dao, document, section, "Biblioteca", 2, true, false);
			addTextArea(dao, subsection, "Informações sobre a biblioteca", 1);
			attr = addAttr(dao, subsection, "Cronograma de expansão do acervo", ScheduleField.class.getCanonicalName(),
					2, false);
			addSchedule(dao, attr, /* periodicity */ false);

			// 12.3 - Laboratórios
			addSingleTextAreaSubsection(
					dao, document, section,
					"Laboratórios",
					3,
					"Informações sobre os laboratórios");

			// 12.4 - Recursos tecnológicos e de áudio visual
			addSingleTextAreaSubsection(
					dao, document, section,
					"Recursos tecnológicos e de áudio visual",
					4,
					"Descrever os recursos presentes na IES, e o planejamento de melhorias nessa área");

			// 12.5 - Plano de promoção de acessibilidade...
			addSingleTextAreaSubsection(
					dao, document, section,
					"Plano de promoção de acessibilidade e de atendimento diferenciado a portadores de necessidades especiais",
					5,
					"Ações planejadas para promover acessibilidade aos portadores de necessidades especiais");

			// 12.6 - Cronograma de expansão da infraestrutura (cronograma)
			subsection = createSection(dao, document, section,
					"Cronograma de expansão da infraestrutura para o período de vigência do PDI",
					6, true, false);
			attr = addAttr(dao, subsection,
					"Cronograma de implementação das ações voltadas para a infraestrutura",
					ScheduleField.class.getCanonicalName(), 1, false);
			addSchedule(dao, attr, /* periodicity */ false);

			// 12.7 - Objetivos e metas (texto + SelectPlan)
			subsection = createSection(dao, document, section, "Objetivos e metas", 7, true, false);
			addTextArea(dao, subsection, "Descrição do plano de expansão da infraestrutura", 1);
			addAttr(dao, subsection, "Selecione o plano de metas correspondente", SelectPlan.class.getCanonicalName(),
					2, false);

			// =====================
			// Seção 13 - Avaliação e acompanhamento do desenvolvimento institucional
			// =====================
			section = createSection(dao, document, null,
					"Avaliação e acompanhamento do desenvolvimento institucional",
					13, /* leaf */ true, /* preText */ false);

			addTextArea(dao, section,
					"Descrever os procedimentos utilizados para a realização da autoavaliação da IES",
					1);


			// =====================
			// Seção 14 - Gestão financeira e orçamentária
			// =====================
			section = createSection(dao, document, null, "Gestão financeira e orçamentária", 14, /* leaf */ true,
					/* preText */ false);
			addTextArea(dao, section,
					"Apresentar ações relacionadas à melhoria da gestão financeira da IES, bem como demonstrar a sustentabilidade financeira",
					1);

			// =====================
			// Seção 15 - Processo de monitoramento, controle e revisão do PDI
			// =====================
			section = createSection(dao, document, null,
					"Processo de monitoramento, controle e revisão do PDI",
					15, /* leaf */ true, /* preText */ false);
			addTextArea(dao, section,
					"Apresentar o planejamento das atividades de controle que ocorrerão durante a vigência do PDI",
					1);
			attr = addAttr(dao, section, "Cronograma de execução", ScheduleField.class.getCanonicalName(), 2, false);
			addSchedule(dao, attr, /* periodicity */ false);

			// =====================
			// Seção 16 - Plano para gestão de riscos
			// =====================
			section = createSection(dao, document, null, "Plano para gestão de riscos", 16, /* leaf */ true,
					/* preText */ false);
			addTextArea(dao, section, "Análise de riscos e o plano em caso de ocorrência", 1);

			// =====================
			// Seção 17 - Conclusão
			// =====================
			section = createSection(dao, document, null, "Conclusão", 17, /* leaf */ true, /* preText */ false);
			addTextArea(dao, section, "Conclusão do documento de PDI", 1);

			// =====================
			// Seção 18 - Anexos
			// =====================
			section = createSection(dao, document, null, "Anexos", 18, /* leaf */ true, /* preText */ false);
			addTextArea(dao, section, "Descrição dos anexos", 1);

			// =====================
			// Seção 19 - Apêndice
			// =====================
			section = createSection(dao, document, null, "Apêndice", 19, /* leaf */ true, /* preText */ false);
			addTextArea(dao, section, "Descrição dos apêndices", 1);

		}

	}
}
