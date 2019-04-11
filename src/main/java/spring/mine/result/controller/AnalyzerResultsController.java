package spring.mine.result.controller;

import java.lang.reflect.InvocationTargetException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.validator.GenericValidator;
import org.hibernate.Transaction;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import spring.mine.common.controller.BaseController;
import spring.mine.common.validator.BaseErrors;
import spring.mine.internationalization.MessageUtil;
import spring.mine.result.form.AnalyzerResultsForm;
import us.mn.state.health.lims.analysis.dao.AnalysisDAO;
import us.mn.state.health.lims.analysis.daoimpl.AnalysisDAOImpl;
import us.mn.state.health.lims.analysis.valueholder.Analysis;
import us.mn.state.health.lims.analyzerimport.util.AnalyzerTestNameCache;
import us.mn.state.health.lims.analyzerimport.util.MappedTestName;
import us.mn.state.health.lims.analyzerresults.action.AnalyzerResultsPaging;
import us.mn.state.health.lims.analyzerresults.action.beanitems.AnalyzerResultItem;
import us.mn.state.health.lims.analyzerresults.dao.AnalyzerResultsDAO;
import us.mn.state.health.lims.analyzerresults.daoimpl.AnalyzerResultsDAOImpl;
import us.mn.state.health.lims.analyzerresults.valueholder.AnalyzerResults;
import us.mn.state.health.lims.common.exception.LIMSRuntimeException;
import us.mn.state.health.lims.common.formfields.FormFields;
import us.mn.state.health.lims.common.formfields.FormFields.Field;
import us.mn.state.health.lims.common.services.LocalizationService;
import us.mn.state.health.lims.common.services.NoteService;
import us.mn.state.health.lims.common.services.PluginMenuService;
import us.mn.state.health.lims.common.services.QAService;
import us.mn.state.health.lims.common.services.QAService.QAObservationType;
import us.mn.state.health.lims.common.services.ResultLimitService;
import us.mn.state.health.lims.common.services.StatusService;
import us.mn.state.health.lims.common.services.StatusService.AnalysisStatus;
import us.mn.state.health.lims.common.services.StatusService.OrderStatus;
import us.mn.state.health.lims.common.services.StatusService.RecordStatus;
import us.mn.state.health.lims.common.services.StatusService.SampleStatus;
import us.mn.state.health.lims.common.services.StatusSet;
import us.mn.state.health.lims.common.services.TypeOfSampleService;
import us.mn.state.health.lims.common.services.TypeOfTestResultService;
import us.mn.state.health.lims.common.util.ConfigurationProperties;
import us.mn.state.health.lims.common.util.DateUtil;
import us.mn.state.health.lims.dictionary.dao.DictionaryDAO;
import us.mn.state.health.lims.dictionary.daoimpl.DictionaryDAOImpl;
import us.mn.state.health.lims.dictionary.valueholder.Dictionary;
import us.mn.state.health.lims.hibernate.HibernateUtil;
import us.mn.state.health.lims.note.dao.NoteDAO;
import us.mn.state.health.lims.note.daoimpl.NoteDAOImpl;
import us.mn.state.health.lims.note.valueholder.Note;
import us.mn.state.health.lims.patient.util.PatientUtil;
import us.mn.state.health.lims.patient.valueholder.Patient;
import us.mn.state.health.lims.result.action.util.ResultUtil;
import us.mn.state.health.lims.result.dao.ResultDAO;
import us.mn.state.health.lims.result.daoimpl.ResultDAOImpl;
import us.mn.state.health.lims.result.valueholder.Result;
import us.mn.state.health.lims.resultlimits.valueholder.ResultLimit;
import us.mn.state.health.lims.sample.dao.SampleDAO;
import us.mn.state.health.lims.sample.daoimpl.SampleDAOImpl;
import us.mn.state.health.lims.sample.valueholder.Sample;
import us.mn.state.health.lims.samplehuman.dao.SampleHumanDAO;
import us.mn.state.health.lims.samplehuman.daoimpl.SampleHumanDAOImpl;
import us.mn.state.health.lims.samplehuman.valueholder.SampleHuman;
import us.mn.state.health.lims.sampleitem.dao.SampleItemDAO;
import us.mn.state.health.lims.sampleitem.daoimpl.SampleItemDAOImpl;
import us.mn.state.health.lims.sampleitem.valueholder.SampleItem;
import us.mn.state.health.lims.sampleqaevent.dao.SampleQaEventDAO;
import us.mn.state.health.lims.sampleqaevent.daoimpl.SampleQaEventDAOImpl;
import us.mn.state.health.lims.sampleqaevent.valueholder.SampleQaEvent;
import us.mn.state.health.lims.test.dao.TestDAO;
import us.mn.state.health.lims.test.daoimpl.TestDAOImpl;
import us.mn.state.health.lims.test.valueholder.Test;
import us.mn.state.health.lims.testanalyte.valueholder.TestAnalyte;
import us.mn.state.health.lims.testreflex.action.util.TestReflexBean;
import us.mn.state.health.lims.testreflex.action.util.TestReflexUtil;
import us.mn.state.health.lims.testreflex.dao.TestReflexDAO;
import us.mn.state.health.lims.testreflex.daoimpl.TestReflexDAOImpl;
import us.mn.state.health.lims.testreflex.valueholder.TestReflex;
import us.mn.state.health.lims.testresult.dao.TestResultDAO;
import us.mn.state.health.lims.testresult.daoimpl.TestResultDAOImpl;
import us.mn.state.health.lims.testresult.valueholder.TestResult;
import us.mn.state.health.lims.typeofsample.dao.TypeOfSampleDAO;
import us.mn.state.health.lims.typeofsample.dao.TypeOfSampleTestDAO;
import us.mn.state.health.lims.typeofsample.daoimpl.TypeOfSampleDAOImpl;
import us.mn.state.health.lims.typeofsample.daoimpl.TypeOfSampleTestDAOImpl;
import us.mn.state.health.lims.typeofsample.valueholder.TypeOfSample;
import us.mn.state.health.lims.typeofsample.valueholder.TypeOfSampleTest;

@Controller
public class AnalyzerResultsController extends BaseController {

	private static final boolean IS_RETROCI = ConfigurationProperties.getInstance()
			.isPropertyValueEqual(ConfigurationProperties.Property.configurationName, "CI_GENERAL");
	private static final String REJECT_VALUE = "XXXX";
	private NoteDAO noteDAO = new NoteDAOImpl();
	private SampleHumanDAO sampleHumanDAO = new SampleHumanDAOImpl();
	private SampleItemDAO sampleItemDAO = new SampleItemDAOImpl();
	private TestDAO testDAO = new TestDAOImpl();
	private TypeOfSampleTestDAO typeOfSampleTestDAO = new TypeOfSampleTestDAOImpl();
	private TypeOfSampleDAO typeOfSampleDAO = new TypeOfSampleDAOImpl();

	private static final String RESULT_SUBJECT = "Analyzer Result Note";
	private static final String DBS_SAMPLE_TYPE_ID;

	static {
		if (IS_RETROCI) {
			TypeOfSample typeOfSample = new TypeOfSample();
			typeOfSample.setDescription("DBS");
			typeOfSample.setDomain("H");
			typeOfSample = new TypeOfSampleDAOImpl().getTypeOfSampleByDescriptionAndDomain(typeOfSample, false);
			DBS_SAMPLE_TYPE_ID = typeOfSample.getId();
		} else {
			DBS_SAMPLE_TYPE_ID = null;
		}
	}

	private AnalyzerResultsDAO analyzerResultsDAO = new AnalyzerResultsDAOImpl();
	private DictionaryDAO dictionaryDAO = new DictionaryDAOImpl();
	private TestResultDAO testResultDAO = new TestResultDAOImpl();
	private SampleDAO sampleDAO = new SampleDAOImpl();
	private TypeOfSampleTestDAO sampleTypeTestDAO = new TypeOfSampleTestDAOImpl();
	private AnalysisDAO analysisDAO = new AnalysisDAOImpl();
	private TestReflexUtil reflexUtil = new TestReflexUtil();
	private TestReflexDAO testReflexDAO = new TestReflexDAOImpl();
	private ResultDAO resultDAO = new ResultDAOImpl();

	private static Map<String, String> analyzerNameToSubtitleKey = new HashMap<>();
	static {
		analyzerNameToSubtitleKey.put(AnalyzerTestNameCache.COBAS_INTEGRA400_NAME, "banner.menu.results.cobas.integra");
		analyzerNameToSubtitleKey.put(AnalyzerTestNameCache.SYSMEX_XT2000_NAME, "banner.menu.results.sysmex");
		analyzerNameToSubtitleKey.put(AnalyzerTestNameCache.FACSCALIBUR, "banner.menu.results.facscalibur");
		analyzerNameToSubtitleKey.put(AnalyzerTestNameCache.FACSCANTO, "banner.menu.results.facscanto");
		analyzerNameToSubtitleKey.put(AnalyzerTestNameCache.EVOLIS, "banner.menu.results.evolis");
		analyzerNameToSubtitleKey.put(AnalyzerTestNameCache.COBAS_TAQMAN, "banner.menu.results.cobas.taqman");
		analyzerNameToSubtitleKey.put(AnalyzerTestNameCache.COBAS_DBS, "banner.menu.results.cobasDBS");
		analyzerNameToSubtitleKey.put(AnalyzerTestNameCache.COBAS_C311, "banner.menu.results.cobasc311");
	}

	@RequestMapping(value = "/AnalyzerResults", method = RequestMethod.GET)
	public ModelAndView showAnalyzerResults(HttpServletRequest request)
			throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		AnalyzerResultsForm form = new AnalyzerResultsForm();

		request.getSession().setAttribute(SAVE_DISABLED, TRUE);

		String page = request.getParameter("page");
		String requestAnalyzerType = request.getParameter("type");

		PropertyUtils.setProperty(form, "analyzerType", requestAnalyzerType);

		AnalyzerResultsPaging paging = new AnalyzerResultsPaging();
		if (GenericValidator.isBlankOrNull(page)) {
			// get list of AnalyzerData from table based on analyzer type
			List<AnalyzerResults> analyzerResultsList = getAnalyzerResults();

			if (analyzerResultsList.isEmpty()) {
				PropertyUtils.setProperty(form, "resultList", new ArrayList<AnalyzerResultItem>());
				PropertyUtils.setProperty(form, "displayNotFoundMsg", true);
				paging.setEmptyPageBean(request, form);

			} else {

				/*
				 * The problem we are solving is that the accession numbers may not be
				 * consecutive but we still want to maintain the order So we will form the
				 * groups (by analyzer runs) by going in order but if the accession number is in
				 * another group it will be boosted to the first group
				 */
				boolean missingTest = false;

				resolveMissingTests(analyzerResultsList);

				List<List<AnalyzerResultItem>> accessionGroupedResultsList = groupAnalyzerResults(analyzerResultsList);

				List<AnalyzerResultItem> analyzerResultItemList = new ArrayList<>();

				int sampleGroupingNumber = 0;
				for (List<AnalyzerResultItem> group : accessionGroupedResultsList) {
					sampleGroupingNumber++;
					AnalyzerResultItem groupHeader = null;
					for (AnalyzerResultItem resultItem : group) {
						if (groupHeader == null) {
							groupHeader = resultItem;
							setNonConformityStateForResultItem(resultItem);
							if (FormFields.getInstance().useField(Field.QaEventsBySection)) {
								resultItem.setNonconforming(getQaEventByTestSection(
										analysisDAO.getAnalysisById(resultItem.getAnalysisId())));
							}

						}
						resultItem.setSampleGroupingNumber(sampleGroupingNumber);

						// There are two reasons there may not be a test id,
						// 1. it could not be found due to missing mapping
						// 2. it may not be looked for if the results are read
						// only
						// we only want to capture 1.
						if (GenericValidator.isBlankOrNull(resultItem.getTestId()) && !resultItem.isReadOnly()) {
							groupHeader.setGroupIsReadOnly(true);
							missingTest = true;
						} else if (resultItem.getIsControl()) {
							groupHeader.setGroupIsReadOnly(true);
						}

						analyzerResultItemList.add(resultItem);
					}
				}

				PropertyUtils.setProperty(form, "displayMissingTestMsg", new Boolean(missingTest));

				paging.setDatabaseResults(request, form, analyzerResultItemList);
			}
		} else {
			paging.page(request, form, page);
		}

		addFlashMsgsToRequest(request);
		return findForward(FWD_SUCCESS, form);
	}

	private void setNonConformityStateForResultItem(AnalyzerResultItem resultItem) {
		boolean nonconforming = false;

		Sample sample = sampleDAO.getSampleByAccessionNumber(resultItem.getAccessionNumber());
		if (sample != null) {
			nonconforming = QAService.isOrderNonConforming(sample);
			// The sample is nonconforming, now we have to check if any sample items are
			// non_conforming and
			// if they are are they for this test
			// Note we only have to check one test since the sample item is the same for all
			// the tests

			if (nonconforming) {
				List<SampleItem> nonConformingSampleItems = QAService.getNonConformingSampleItems(sample);
				// If there is a nonconforming sample item then we need to check if it is the
				// one for this
				// test if it is then it is nonconforming if not then it is not nonconforming
				if (!nonConformingSampleItems.isEmpty()) {
					TypeOfSampleTest typeOfSample = sampleTypeTestDAO
							.getTypeOfSampleTestForTest(resultItem.getTestId());
					if (typeOfSample != null) {
						String sampleTypeId = typeOfSample.getTypeOfSampleId();
						nonconforming = false;
						for (SampleItem sampleItem : nonConformingSampleItems) {
							if (sampleTypeId.equals(sampleItem.getTypeOfSample().getId())) {
								nonconforming = true;
								break;
							}
						}

					}
				}

			}

		}

		resultItem.setNonconforming(nonconforming);

	}

	private List<List<AnalyzerResultItem>> groupAnalyzerResults(List<AnalyzerResults> analyzerResultsList) {
		Map<String, Integer> accessionToAccessionGroupMap = new HashMap<>();
		List<List<AnalyzerResultItem>> accessionGroupedResultsList = new ArrayList<>();

		for (AnalyzerResults analyzerResult : analyzerResultsList) {
			AnalyzerResultItem resultItem = analyzerResultsToAnalyzerResultItem(analyzerResult);
			Integer groupIndex = accessionToAccessionGroupMap.get(resultItem.getAccessionNumber());
			List<AnalyzerResultItem> group;
			if (groupIndex == null) {
				group = new ArrayList<>();
				accessionGroupedResultsList.add(group);
				accessionToAccessionGroupMap.put(resultItem.getAccessionNumber(),
						accessionGroupedResultsList.size() - 1);
			} else {
				group = accessionGroupedResultsList.get(groupIndex.intValue());
			}

			group.add(resultItem);
		}
		return accessionGroupedResultsList;
	}

	private void resolveMissingTests(List<AnalyzerResults> analyzerResultsList) {
		boolean reloadCache = true;
		List<AnalyzerResults> resolvedResults = new ArrayList<>();

		for (AnalyzerResults analyzerResult : analyzerResultsList) {
			if (GenericValidator.isBlankOrNull(analyzerResult.getTestId())) {
				if (reloadCache) {
					AnalyzerTestNameCache.instance().reloadCache();
					reloadCache = false;
				}
			}

			String analyzerTestName = analyzerResult.getTestName();
			MappedTestName mappedTestName = AnalyzerTestNameCache.instance().getMappedTest(getAnalyzerNameFromRequest(),
					analyzerTestName);
			if (mappedTestName != null) {
				analyzerResult.setTestName(mappedTestName.getOpenElisTestName());
				analyzerResult.setTestId(mappedTestName.getTestId());
				resolvedResults.add(analyzerResult);
			}
		}

		if (resolvedResults.size() > 0) {
			Transaction tx = HibernateUtil.getSession().beginTransaction();

			try {
				for (AnalyzerResults analyzerResult : resolvedResults) {
					analyzerResult.setSysUserId(getSysUserId(request));
					analyzerResultsDAO.updateData(analyzerResult);
				}

				tx.commit();
			} catch (LIMSRuntimeException lre) {
				tx.rollback();
			} finally {
				HibernateUtil.closeSession();
			}

		}

	}

	private List<AnalyzerResults> getAnalyzerResults() {
		return analyzerResultsDAO.getResultsbyAnalyzer(
				AnalyzerTestNameCache.instance().getAnalyzerIdForName(getAnalyzerNameFromRequest()));
	}

	protected AnalyzerResultItem analyzerResultsToAnalyzerResultItem(AnalyzerResults result) {

		AnalyzerResultItem resultItem = new AnalyzerResultItem();
		resultItem.setAccessionNumber(result.getAccessionNumber());
		resultItem.setAnalyzerId(result.getAnalyzerId());
		resultItem.setIsControl(result.getIsControl());
		resultItem.setTestName(result.getTestName());
		resultItem.setUnits(getUnits(result.getUnits()));
		resultItem.setId(result.getId());
		resultItem.setTestId(result.getTestId());
		resultItem.setCompleteDate(result.getCompleteDateForDisplay());
		resultItem.setLastUpdated(result.getLastupdated());
		resultItem.setReadOnly((result.isReadOnly() || result.getTestId() == null));
		resultItem.setResult(getResultForItem(result));
		resultItem.setSignificantDigits(getSignificantDigitsFromAnalyzerResults(result));
		resultItem.setTestResultType(result.getResultType());
		resultItem.setDictionaryResultList(getDictionaryResultList(result));
		resultItem.setIsHighlighted(!GenericValidator.isBlankOrNull(result.getDuplicateAnalyzerResultId())
				|| GenericValidator.isBlankOrNull(result.getTestId()));
		resultItem.setUserChoiceReflex(giveUserChoice(result));
		resultItem.setUserChoicePending(false);

		if (resultItem.isUserChoiceReflex()) {
			setChoiceForCurrentValue(resultItem, result);
			resultItem.setUserChoicePending(!GenericValidator.isBlankOrNull(resultItem.getSelectionOneText()));
		}
		return resultItem;
	}

	private boolean giveUserChoice(AnalyzerResults result) {
		/*
		 * This is how we figure out if the user will be able to select 1. Is the test
		 * involved with triggering a user selection reflex 2. If the reflex has sibs
		 * has the sample been entered yet 3. If the sample has been entered have all of
		 * the sibling tests been ordered
		 */
		if (!TestReflexUtil.isTriggeringUserChoiceReflexTestId(result.getTestId())) {
			return false;
		}

		if (!TestReflexUtil.testIsTriggeringReflexWithSibs(result.getTestId())) {
			return false;
		}

		Sample sample = getSampleForAnalyzerResult(result);
		if (sample == null) {
			return false;
		}

		List<TestReflex> reflexes = reflexUtil.getPossibleUserChoiceTestReflexsForTest(result.getTestId());

		List<Analysis> analysisList = analysisDAO.getAnalysesBySampleId(sample.getId());
		Set<String> analysisTestIds = new HashSet<>();

		for (Analysis analysis : analysisList) {
			analysisTestIds.add(analysis.getTest().getId());
		}

		for (TestReflex reflex : reflexes) {
			if (!analysisTestIds.contains(reflex.getTest().getId())) {
				return false;
			}
		}
		return true;
	}

	private Sample getSampleForAnalyzerResult(AnalyzerResults result) {
		return sampleDAO.getSampleByAccessionNumber(result.getAccessionNumber());
	}

	private void setChoiceForCurrentValue(AnalyzerResultItem resultItem, AnalyzerResults analyzerResult) {
		/*
		 * If there are no siblings for the reflex then we just need to find if there
		 * are choices for the current value
		 *
		 * If there are siblings then we need to find if they are currently satisfied
		 */
		TestReflex selectionOne = null;
		TestReflex selectionTwo = null;

		if (!TestReflexUtil.testIsTriggeringReflexWithSibs(analyzerResult.getTestId())) {
			List<TestReflex> reflexes = reflexUtil.getTestReflexsForDictioanryResultTestId(analyzerResult.getResult(),
					analyzerResult.getTestId(), true);
			resultItem.setReflexSelectionId(null);
			for (TestReflex reflex : reflexes) {
				if (selectionOne == null) {
					selectionOne = reflex;
				} else {
					selectionTwo = reflex;
				}
			}

		} else {

			Sample sample = getSampleForAnalyzerResult(analyzerResult);

			List<Analysis> analysisList = analysisDAO.getAnalysesBySampleId(sample.getId());

			List<TestReflex> reflexesForDisplayedTest = reflexUtil.getTestReflexsForDictioanryResultTestId(
					analyzerResult.getResult(), analyzerResult.getTestId(), true);

			for (TestReflex possibleTestReflex : reflexesForDisplayedTest) {
				if (TestReflexUtil.isUserChoiceReflex(possibleTestReflex)) {
					if (GenericValidator.isBlankOrNull(possibleTestReflex.getSiblingReflexId())) {
						if (possibleTestReflex.getActionScriptlet() != null) {
							selectionOne = possibleTestReflex;
							break;
						} else if (selectionOne == null) {
							selectionOne = possibleTestReflex;
						} else {
							selectionTwo = possibleTestReflex;
							break;
						}
					} else {
						// find if the sibling reflex is satisfied
						TestReflex sibTestReflex = new TestReflex();
						sibTestReflex.setId(possibleTestReflex.getSiblingReflexId());

						testReflexDAO.getData(sibTestReflex);

						TestResult sibTestResult = new TestResult();
						sibTestResult.setId(sibTestReflex.getTestResultId());
						testResultDAO.getData(sibTestResult);

						for (Analysis analysis : analysisList) {
							List<Result> resultList = resultDAO.getResultsByAnalysis(analysis);
							Test test = analysis.getTest();

							for (Result result : resultList) {
								TestResult testResult = testResultDAO
										.getTestResultsByTestAndDictonaryResult(test.getId(), result.getValue());
								if (testResult != null && testResult.getId().equals(sibTestReflex.getTestResultId())) {
									if (possibleTestReflex.getActionScriptlet() != null) {
										selectionOne = possibleTestReflex;
										break;
									} else if (selectionOne == null) {
										selectionOne = possibleTestReflex;
									} else {
										selectionTwo = possibleTestReflex;
										break;
									}
								}
							}
						}
					}
				}
			}
		}
		populateAnalyzerResultItemWithReflexes(resultItem, selectionOne, selectionTwo);
	}

	private void populateAnalyzerResultItemWithReflexes(AnalyzerResultItem resultItem, TestReflex selectionOne,
			TestReflex selectionTwo) {
		if (selectionOne != null) {
			if (selectionTwo == null && !GenericValidator.isBlankOrNull(selectionOne.getActionScriptletId())
					&& !GenericValidator.isBlankOrNull(selectionOne.getTestId())) {

				resultItem.setSelectionOneText(TestReflexUtil.makeReflexTestName(selectionOne));
				resultItem.setSelectionOneValue(TestReflexUtil.makeReflexTestValue(selectionOne));
				resultItem.setSelectionTwoText(TestReflexUtil.makeReflexScriptName(selectionTwo));
				resultItem.setSelectionTwoValue(TestReflexUtil.makeReflexScriptValue(selectionOne));
			} else if (selectionTwo != null) {
				if (selectionOne.getTest() != null) {
					resultItem.setSelectionOneText(TestReflexUtil.makeReflexTestName(selectionOne));
					resultItem.setSelectionOneValue(TestReflexUtil.makeReflexTestValue(selectionOne));
				} else {
					resultItem.setSelectionOneText(TestReflexUtil.makeReflexScriptName(selectionOne));
					resultItem.setSelectionOneValue(TestReflexUtil.makeReflexScriptValue(selectionOne));
				}

				if (selectionTwo.getTest() != null) {
					resultItem.setSelectionTwoText(TestReflexUtil.makeReflexTestName(selectionTwo));
					resultItem.setSelectionTwoValue(TestReflexUtil.makeReflexTestValue(selectionOne));
				} else {
					resultItem.setSelectionTwoText(TestReflexUtil.makeReflexScriptName(selectionTwo));
					resultItem.setSelectionTwoValue(TestReflexUtil.makeReflexScriptValue(selectionOne));
				}
			}
		}
	}

	private String getResultForItem(AnalyzerResults result) {
		if (TypeOfTestResultService.ResultType.NUMERIC.matches(result.getResultType())) {
			return getRoundedToSignificantDigits(result);
		}

		if (TypeOfTestResultService.ResultType.isTextOnlyVariant(result.getResultType())
				|| GenericValidator.isBlankOrNull(result.getResultType())
				|| GenericValidator.isBlankOrNull(result.getResult())) {

			return result.getResult();
		}

		// If it's readonly or the selectlist can not be gotten then we want the result
		// otherwise we want the id so the correct selection will be choosen
		if (result.isReadOnly() || result.getTestId() == null || result.getIsControl()) {
			return dictionaryDAO.getDictionaryById(result.getResult()).getDictEntry();
		} else {
			return result.getResult();
		}
	}

	private String getSignificantDigitsFromAnalyzerResults(AnalyzerResults result) {

		List<TestResult> testResults = testResultDAO.getActiveTestResultsByTest(result.getTestId());

		if (GenericValidator.isBlankOrNull(result.getResult()) || testResults.isEmpty()) {
			return result.getResult();
		}

		TestResult testResult = testResults.get(0);

		return testResult.getSignificantDigits();

	}

	private String getRoundedToSignificantDigits(AnalyzerResults result) {
		if (result.getTestId() != null) {

			Double results;
			try {
				results = Double.valueOf(result.getResult());
			} catch (NumberFormatException e) {
				return result.getResult();
			}

			String significantDigitsAsString = getSignificantDigitsFromAnalyzerResults(result);
			if (GenericValidator.isBlankOrNull(significantDigitsAsString) || "-1".equals(significantDigitsAsString)) {
				return result.getResult();
			}

			Integer significantDigits;
			try {
				significantDigits = Integer.parseInt(significantDigitsAsString);
			} catch (NumberFormatException e) {
				return result.getResult();
			}

			if (significantDigits == 0) {
				return String.valueOf(Math.round(results));
			}

			double power = Math.pow(10, significantDigits);
			return String.valueOf(Math.round(results * power) / power);
		} else {
			return result.getResult();
		}
	}

	private String getUnits(String units) {
		if (GenericValidator.isBlankOrNull(units) || "null".equals(units)) {
			return "";
		}
		return units;
	}

	private List<Dictionary> getDictionaryResultList(AnalyzerResults result) {
		if ("N".equals(result.getResultType()) || "A".equals(result.getResultType())
				|| "R".equals(result.getResultType()) || GenericValidator.isBlankOrNull(result.getResultType())
				|| result.getTestId() == null) {
			return null;
		}

		List<Dictionary> dictionaryList = new ArrayList<>();

		List<TestResult> testResults = testResultDAO.getActiveTestResultsByTest(result.getTestId());

		for (TestResult testResult : testResults) {
			dictionaryList.add(dictionaryDAO.getDictionaryById(testResult.getValue()));
		}

		return dictionaryList;
	}

	@Override
	protected String getActualMessage(String messageKey) {
		String actualMessage = null;
		if (messageKey != null) {
			actualMessage = PluginMenuService.getInstance().getMenuLabel(LocalizationService.getCurrentLocale(),
					messageKey);
		}
		return actualMessage == null ? getAnalyzerNameFromRequest() : actualMessage;
	}

	protected String getAnalyzerNameFromRequest() {
		String analyzer = null;
		String requestType = request.getParameter("type");
		if (!GenericValidator.isBlankOrNull(requestType)) {
			analyzer = AnalyzerTestNameCache.instance().getDBNameForActionName(requestType);
		}
		return analyzer;
	}

	private boolean getQaEventByTestSection(Analysis analysis) {
		if (analysis == null) {
			return false;
		}
		if (analysis.getTestSection() != null && analysis.getSampleItem().getSample() != null) {
			Sample sample = analysis.getSampleItem().getSample();
			List<SampleQaEvent> sampleQaEventsList = getSampleQaEvents(sample);
			for (SampleQaEvent event : sampleQaEventsList) {
				QAService qa = new QAService(event);
				if (!GenericValidator.isBlankOrNull(qa.getObservationValue(QAObservationType.SECTION))
						&& qa.getObservationValue(QAObservationType.SECTION)
								.equals(analysis.getTestSection().getNameKey())) {
					return true;
				}
			}
		}
		return false;
	}

	public List<SampleQaEvent> getSampleQaEvents(Sample sample) {
		SampleQaEventDAO sampleQaEventDAO = new SampleQaEventDAOImpl();
		return sampleQaEventDAO.getSampleQaEventsBySample(sample);
	}

	@RequestMapping(value = "/AnalyzerResults", method = RequestMethod.POST)
	public ModelAndView showAnalyzerResultsSave(HttpServletRequest request,
			@ModelAttribute("form") @Valid AnalyzerResultsForm form, BindingResult result,
			RedirectAttributes redirectAttibutes) {
		if (result.hasErrors()) {
			saveErrors(result);
			return findForward(FWD_FAIL_INSERT, form);
		}

		AnalyzerResultsPaging paging = new AnalyzerResultsPaging();
		paging.updatePagedResults(request, form);
		List<AnalyzerResultItem> resultItemList = paging.getResults(request);

		List<AnalyzerResultItem> actionableResults = extractActionableResult(resultItemList);

		if (actionableResults.isEmpty()) {
			return findForward(FWD_SUCCESS_INSERT, form);
		}

		validateSavableItems(actionableResults, result);

		if (result.hasErrors()) {
			saveErrors(result);

			return findForward(FWD_VALIDATION_ERROR, form);
		}

		List<SampleGrouping> sampleGroupList = new ArrayList<>();

		resultItemList.removeAll(actionableResults);
		List<AnalyzerResultItem> childlessControls = extractChildlessControls(resultItemList);
		List<AnalyzerResults> deletableAnalyzerResults = getRemovableAnalyzerResults(actionableResults,
				childlessControls);

		createResultsFromItems(actionableResults, sampleGroupList);

		Transaction tx = HibernateUtil.getSession().beginTransaction();

		try {
			removeHandledResultsFromAnalyzerResults(deletableAnalyzerResults);

			boolean saveSuccess = insertResults(request, sampleGroupList);

			if (saveSuccess) {
				tx.commit();
			} else {
				tx.rollback();
				return findForward(FWD_VALIDATION_ERROR, form);
			}
		} catch (LIMSRuntimeException lre) {
			tx.rollback();
			String errorMsg = "errors.UpdateException";
			result.reject(errorMsg);
			saveErrors(result);

			return findForward(FWD_VALIDATION_ERROR, form);
		}

		redirectAttibutes.addFlashAttribute(FWD_SUCCESS, true);
		if (GenericValidator.isBlankOrNull(form.getString("analyzerType"))) {
			return findForward(FWD_SUCCESS_INSERT, form);
		} else {
			Map<String, String> params = new HashMap<>();
			params.put("type", form.getString("analyzerType"));
			// params.put("forward", FWD_SUCCESS_INSERT);
			return getForwardWithParameters(findForward(FWD_SUCCESS_INSERT, form), params);
		}
	}

	private Errors validateSavableItems(List<AnalyzerResultItem> savableResults, Errors errors) {
		for (AnalyzerResultItem item : savableResults) {
			if (item.getIsAccepted() && item.isUserChoicePending()) {
				StringBuilder augmentedAccession = new StringBuilder(item.getAccessionNumber());
				augmentedAccession.append(" : ");
				augmentedAccession.append(item.getTestName());
				augmentedAccession.append(" - ");
				augmentedAccession.append(MessageUtil.getMessage("error.reflexStep.notChosen"));
				String errorMsg = "errors.followingAccession";
				errors.reject(errorMsg, new String[] { augmentedAccession.toString() }, errorMsg);
			}
		}

		return errors;
	}

	private boolean insertResults(HttpServletRequest request, List<SampleGrouping> sampleGroupList) {
		for (SampleGrouping grouping : sampleGroupList) {
			if (grouping.addSample) {
				try {
					sampleDAO.insertDataWithAccessionNumber(grouping.sample);
				} catch (LIMSRuntimeException lre) {
					Errors errors = new BaseErrors();
					String errorMsg = "warning.duplicate.accession";
					errors.reject(errorMsg, new String[] { grouping.sample.getAccessionNumber() }, errorMsg);
					saveErrors(errors);
					return false;
				}
			} else if (grouping.updateSample) {
				sampleDAO.updateData(grouping.sample);
			}

			String sampleId = grouping.sample.getId();

			if (grouping.addSample) {
				grouping.sampleHuman.setSampleId(sampleId);
				sampleHumanDAO.insertData(grouping.sampleHuman);

				RecordStatus patientStatus = grouping.statusSet.getPatientRecordStatus() == null
						? RecordStatus.NotRegistered
						: null;
				RecordStatus sampleStatus = grouping.statusSet.getSampleRecordStatus() == null
						? RecordStatus.NotRegistered
						: null;
				StatusService.getInstance().persistRecordStatusForSample(grouping.sample, sampleStatus,
						grouping.patient, patientStatus, getSysUserId(request));
			}

			if (grouping.addSampleItem) {
				grouping.sampleItem.setSample(grouping.sample);
				sampleItemDAO.insertData(grouping.sampleItem);
			}

			for (int i = 0; i < grouping.analysisList.size(); i++) {

				Analysis analysis = grouping.analysisList.get(i);
				if (GenericValidator.isBlankOrNull(analysis.getId())) {
					analysis.setSampleItem(grouping.sampleItem);
					analysisDAO.insertData(analysis, false);
				} else {
					analysisDAO.updateData(analysis);
				}

				Result result = grouping.resultList.get(i);
				if (GenericValidator.isBlankOrNull(result.getId())) {
					result.setAnalysis(analysis);
					setAnalyte(result);
					resultDAO.insertData(result);
				} else {
					resultDAO.updateData(result);
				}

				Note note = grouping.noteList.get(i);

				if (note != null) {
					note.setReferenceId(result.getId());
					noteDAO.insertData(note);
				}
			}
		}

		TestReflexUtil testReflexUtil = new TestReflexUtil();
		testReflexUtil.setCurrentUserId(getSysUserId(request));
		testReflexUtil.addNewTestsToDBForReflexTests(convertGroupListToTestReflexBeans(sampleGroupList));

		return true;
	}

	private List<TestReflexBean> convertGroupListToTestReflexBeans(List<SampleGrouping> sampleGroupList) {
		List<TestReflexBean> reflexBeanList = new ArrayList<>();

		for (SampleGrouping sampleGroup : sampleGroupList) {
			if (sampleGroup.accepted) {
				for (Result result : sampleGroup.resultList) {
					TestReflexBean reflex = new TestReflexBean();
					reflex.setPatient(sampleGroup.patient);
					reflex.setTriggersToSelectedReflexesMap(sampleGroup.triggersToSelectedReflexesMap);
					reflex.setResult(result);
					reflex.setSample(sampleGroup.sample);
					reflexBeanList.add(reflex);
				}
			}
		}

		return reflexBeanList;
	}

	private void createResultsFromItems(List<AnalyzerResultItem> actionableResults,
			List<SampleGrouping> sampleGroupList) {
		int groupingNumber = -1;
		List<AnalyzerResultItem> groupedResultList = null;

		/*
		 * Basic idea is that analyzerResultItems are put into a groupedResultList if
		 * they have the same grouping number. When the grouping number changes then the
		 * list is converted to a sampleGrouping. Note that the first time through the
		 * groupedResultList is empty so the sampleGrouping is null
		 */
		for (AnalyzerResultItem analyzerResultItem : actionableResults) {
			if (analyzerResultItem.getIsDeleted()) {
				continue;
			}

			if (analyzerResultItem.getSampleGroupingNumber() != groupingNumber) {
				groupingNumber = analyzerResultItem.getSampleGroupingNumber();

				SampleGrouping sampleGrouping = createRecordsForNewResult(groupedResultList);

				if (sampleGrouping != null) {
					sampleGrouping.triggersToSelectedReflexesMap = new HashMap<>();
					sampleGroupList.add(sampleGrouping);
				}

				groupedResultList = new ArrayList<>();
			}

			if (!analyzerResultItem.isReadOnly()) {
				groupedResultList.add(analyzerResultItem);
			}

		}

		// for the last set of results the grouping number will not change
		SampleGrouping sampleGrouping = createRecordsForNewResult(groupedResultList);
		// TODO currently there are no user selections of reflexes on the analyzer
		// result page so for now this is ok
		sampleGrouping.triggersToSelectedReflexesMap = new HashMap<>();

		sampleGroupList.add(sampleGrouping);

	}

	private SampleGrouping createRecordsForNewResult(List<AnalyzerResultItem> groupedAnalyzerResultItems) {

		if (groupedAnalyzerResultItems != null && !groupedAnalyzerResultItems.isEmpty()) {
			String accessionNumber = groupedAnalyzerResultItems.get(0).getAccessionNumber();
			StatusSet statusSet = StatusService.getInstance().getStatusSetForAccessionNumber(accessionNumber);

			// If neither the test request or demographics has been entered then
			// both a skeleton set of entries should be made
			// If either one of them has been done the sketched entries have
			// been done and we only care if the
			// sample is a skeleton. Otherwise we just enter the results.
			// One corner cases includes the results from one analyzer have been
			// done and this is a different
			// analyzer, it may or may not be from the same sample
			if (noEntryDone(statusSet, accessionNumber)) {
				return createGroupForNoSampleEntryDone(groupedAnalyzerResultItems, statusSet);
			} else if (statusSet.getSampleRecordStatus() == RecordStatus.NotRegistered
					&& statusSet.getPatientRecordStatus() == RecordStatus.NotRegistered) {
				return createGroupForPreviousAnalyzerDone(groupedAnalyzerResultItems, statusSet);
			} else if (statusSet.getSampleRecordStatus() == RecordStatus.NotRegistered) {
				return createGroupForDemographicsEntered(groupedAnalyzerResultItems, statusSet);
			} else {
				// this is called when just sample entry has been done/ fix
				return createGroupForSampleAndDemographicsEntered(groupedAnalyzerResultItems, statusSet);
			}
		}

		return null;
	}

	private boolean noEntryDone(StatusSet statusSet, String accessionNumber) {
		boolean sampleOrPatientEntryDone = statusSet.getPatientRecordStatus() != null
				|| statusSet.getSampleRecordStatus() != null;

		if (sampleOrPatientEntryDone) {
			return false;
		}

		// This last case is that non-conformity may have been done
		return sampleDAO.getSampleByAccessionNumber(accessionNumber) == null;

	}

	/*
	 * Demographics and sample are stubbed out but we may need to add a new
	 * sample_item, if the sample type is different then the current one.
	 */
	private SampleGrouping createGroupForPreviousAnalyzerDone(List<AnalyzerResultItem> groupedAnalyzerResultItems,
			StatusSet statusSet) {
		SampleGrouping sampleGrouping = new SampleGrouping();
		Sample sample = sampleDAO.getSampleByAccessionNumber(groupedAnalyzerResultItems.get(0).getAccessionNumber());

		List<Analysis> analysisList = new ArrayList<>();
		List<Result> resultList = new ArrayList<>();
		Map<Result, String> resultToUserSelectionMap = new HashMap<>();
		List<Note> noteList = new ArrayList<>();

		// we're not setting the sample status because this doesn't change it.
		sample.setEnteredDate(new Date(new java.util.Date().getTime()));
		sample.setSysUserId(getSysUserId(request));

		Patient patient = sampleHumanDAO.getPatientForSample(sample);
		createAndAddItems_Analysis_Results(groupedAnalyzerResultItems, analysisList, resultList,
				resultToUserSelectionMap, noteList, patient);

		// We either have to find an existing sample item or create a new one
		SampleItem sampleItem = getOrCreateSampleItem(groupedAnalyzerResultItems, sample);

		sampleGrouping.sample = sample;
		sampleGrouping.sampleItem = sampleItem;
		sampleGrouping.analysisList = analysisList;
		sampleGrouping.resultList = resultList;
		sampleGrouping.noteList = noteList;
		sampleGrouping.addSample = false;
		sampleGrouping.addSampleItem = sampleItem.getId() == null;
		sampleGrouping.statusSet = statusSet;
		sampleGrouping.accepted = groupedAnalyzerResultItems.get(0).getIsAccepted();
		sampleGrouping.patient = patient;
		sampleGrouping.resultToUserserSelectionMap = resultToUserSelectionMap;

		return sampleGrouping;
	}

	protected SampleItem getOrCreateSampleItem(List<AnalyzerResultItem> groupedAnalyzerResultItems, Sample sample) {
		List<Analysis> dBAnalysisList = analysisDAO.getAnalysesBySampleId(sample.getId());

		TypeOfSampleTest typeOfSampleForNewTest = typeOfSampleTestDAO
				.getTypeOfSampleTestForTest(groupedAnalyzerResultItems.get(0).getTestId());
		String typeOfSampleId = typeOfSampleForNewTest.getTypeOfSampleId();

		SampleItem sampleItem = null;
		int maxSampleItemSortOrder = 0;

		for (Analysis dbAnalysis : dBAnalysisList) {
			if (GenericValidator.isBlankOrNull(dbAnalysis.getSampleItem().getSortOrder())) {
				maxSampleItemSortOrder = Math.max(maxSampleItemSortOrder,
						Integer.parseInt(dbAnalysis.getSampleItem().getSortOrder()));
			}
			if (typeOfSampleId.equals(dbAnalysis.getSampleItem().getTypeOfSampleId())) {
				sampleItem = dbAnalysis.getSampleItem();
				break;
			}
		}

		boolean newSampleItem = sampleItem == null;

		if (newSampleItem) {
			sampleItem = new SampleItem();
			sampleItem.setSysUserId(getSysUserId(request));
			sampleItem.setSortOrder(Integer.toString(maxSampleItemSortOrder + 1));
			sampleItem.setStatusId(StatusService.getInstance().getStatusID(SampleStatus.Entered));
			TypeOfSample typeOfSample = new TypeOfSample();
			typeOfSample.setId(typeOfSampleId);
			typeOfSampleDAO.getData(typeOfSample);
			sampleItem.setTypeOfSample(typeOfSample);
		}
		return sampleItem;
	}

	private SampleGrouping createGroupForDemographicsEntered(List<AnalyzerResultItem> groupedAnalyzerResultItems,
			StatusSet statusSet) {
		SampleGrouping sampleGrouping = new SampleGrouping();
		Sample sample = sampleDAO.getSampleByAccessionNumber(groupedAnalyzerResultItems.get(0).getAccessionNumber());

		// A previous sample item may exist if there was a previous import and
		// patient demographics was entered
		SampleItem sampleItem = getOrCreateSampleItem(groupedAnalyzerResultItems, sample);

		List<Analysis> analysisList = new ArrayList<>();
		List<Result> resultList = new ArrayList<>();
		Map<Result, String> resultToUserSelectionMap = new HashMap<>();
		List<Note> noteList = new ArrayList<>();

		if (StatusService.getInstance().getStatusID(OrderStatus.Entered).equals(sample.getStatusId())) {
			sample.setStatusId(StatusService.getInstance().getStatusID(OrderStatus.Started));
		}
		sample.setEnteredDate(new Date(new java.util.Date().getTime()));
		sample.setSysUserId(getSysUserId(request));

		Patient patient = sampleHumanDAO.getPatientForSample(sample);
		createAndAddItems_Analysis_Results(groupedAnalyzerResultItems, analysisList, resultList,
				resultToUserSelectionMap, noteList, patient);

		sampleGrouping.sample = sample;
		sampleGrouping.sampleItem = sampleItem;
		sampleGrouping.analysisList = analysisList;
		sampleGrouping.resultList = resultList;
		sampleGrouping.noteList = noteList;
		sampleGrouping.addSample = false;
		sampleGrouping.updateSample = true;
		sampleGrouping.statusSet = statusSet;
		sampleGrouping.addSampleItem = sampleItem.getId() == null;
		sampleGrouping.accepted = groupedAnalyzerResultItems.get(0).getIsAccepted();
		sampleGrouping.patient = patient;
		sampleGrouping.resultToUserserSelectionMap = resultToUserSelectionMap;

		return sampleGrouping;
	}

	private SampleGrouping createGroupForSampleAndDemographicsEntered(
			List<AnalyzerResultItem> groupedAnalyzerResultItems, StatusSet statusSet) {
		SampleGrouping sampleGrouping = new SampleGrouping();
		Sample sample = sampleDAO.getSampleByAccessionNumber(groupedAnalyzerResultItems.get(0).getAccessionNumber());

		List<Analysis> analysisList = new ArrayList<>();
		List<Result> resultList = new ArrayList<>();
		Map<Result, String> resultToUserSelectionMap = new HashMap<>();
		List<Note> noteList = new ArrayList<>();

		if (StatusService.getInstance().getStatusID(OrderStatus.Entered).equals(sample.getStatusId())) {
			sample.setStatusId(StatusService.getInstance().getStatusID(OrderStatus.Started));
		}
		sample.setEnteredDate(new Date(new java.util.Date().getTime()));
		sample.setSysUserId(getSysUserId(request));

		SampleItem sampleItem = null;
		/*****
		 * this is causing the status id for the sample in the DB to be updated
		 *********/
		List<Analysis> dBAnalysisList = analysisDAO.getAnalysesBySampleId(sample.getId());
		Patient patient = sampleHumanDAO.getPatientForSample(sample);

		for (AnalyzerResultItem resultItem : groupedAnalyzerResultItems) {
			Analysis analysis = null;

			for (Analysis dbAnalysis : dBAnalysisList) {
				if (dbAnalysis.getTest().getId().equals(resultItem.getTestId())) {
					analysis = dbAnalysis;
					break;
				}
			}

			if (analysis == null) {
				// This is an analysis which is not in the ordered tests but
				// should be tracked anyway
				analysis = new Analysis();
				Test test = new Test();
				test.setId(resultItem.getTestId());
				test = testDAO.getTestById(test);
				analysis.setTest(test);
				// A new sampleItem may be needed
				TypeOfSample typeOfSample = TypeOfSampleService.getTypeOfSampleForTest(test.getId());
				List<SampleItem> sampleItemsForSample = sampleItemDAO.getSampleItemsBySampleId(sample.getId());

				// if the type of sample is found then assign to analysis
				// otherwise create it and assign
				for (SampleItem item : sampleItemsForSample) {
					if (item.getTypeOfSample().getId().equals(typeOfSample.getId())) {
						sampleItem = item;
						analysis.setSampleItem(sampleItem);
					}
				}
				if (sampleItem == null) {
					sampleItem = new SampleItem();
					sampleItem.setSysUserId(getSysUserId(request));
					sampleItem.setSortOrder("1");
					sampleItem.setStatusId(StatusService.getInstance().getStatusID(SampleStatus.Entered));
					sampleItem.setCollectionDate(DateUtil.getNowAsTimestamp());
					sampleItem.setTypeOfSample(typeOfSample);
					analysis.setSampleItem(sampleItem);
				}
			} else {
				dBAnalysisList.remove(analysis);
			}
			// Since this is for a single analyzer we are assuming a single
			// sample and sample type so a single SampleItem
			if (sampleItem == null) {
				sampleItem = analysis.getSampleItem();
				sampleItem.setSysUserId(getSysUserId(request));
			}

			populateAnalysis(resultItem, analysis, analysis.getTest());
			analysis.setSysUserId(getSysUserId(request));
			analysisList.add(analysis);

			Result result = getResult(analysis, patient, resultItem);
			resultToUserSelectionMap.put(result, resultItem.getReflexSelectionId());

			resultList.add(result);

			if (GenericValidator.isBlankOrNull(resultItem.getNote())) {
				noteList.add(null);
			} else {
				Note note = new NoteService(analysis).createSavableNote(NoteService.NoteType.INTERNAL,
						resultItem.getNote(), RESULT_SUBJECT, getSysUserId(request));
				noteList.add(note);
			}
		}

		sampleGrouping.sample = sample;
		sampleGrouping.sampleItem = sampleItem;
		sampleGrouping.analysisList = analysisList;
		sampleGrouping.resultList = resultList;
		sampleGrouping.noteList = noteList;
		sampleGrouping.addSample = false;
		sampleGrouping.updateSample = true;
		sampleGrouping.statusSet = statusSet;
		sampleGrouping.addSampleItem = sampleItem.getId() == null;
		sampleGrouping.accepted = groupedAnalyzerResultItems.get(0).getIsAccepted();
		sampleGrouping.patient = patient;
		sampleGrouping.resultToUserserSelectionMap = resultToUserSelectionMap;

		return sampleGrouping;
	}

	private SampleGrouping createGroupForNoSampleEntryDone(List<AnalyzerResultItem> groupedAnalyzerResultItems,
			StatusSet statusSet) {
		SampleGrouping sampleGrouping = new SampleGrouping();
		Sample sample = new Sample();
		SampleHuman sampleHuman = new SampleHuman();
		SampleItem sampleItem = new SampleItem();
		sampleItem.setSysUserId(getSysUserId(request));
		sampleItem.setSortOrder("1");
		sampleItem.setStatusId(StatusService.getInstance().getStatusID(SampleStatus.Entered));

		List<Analysis> analysisList = new ArrayList<>();
		List<Result> resultList = new ArrayList<>();
		Map<Result, String> resultToUserSelectionMap = new HashMap<>();
		List<Note> noteList = new ArrayList<>();

		sample.setAccessionNumber(groupedAnalyzerResultItems.get(0).getAccessionNumber());
		sample.setDomain("H");
		sample.setStatusId(StatusService.getInstance().getStatusID(OrderStatus.Started));
		sample.setEnteredDate(new Date(new java.util.Date().getTime()));
		sample.setReceivedDate(new Date(new java.util.Date().getTime()));
		sample.setSysUserId(getSysUserId(request));

		sampleHuman.setPatientId(PatientUtil.getUnknownPatient().getId());
		sampleHuman.setSysUserId(getSysUserId(request));

		Patient patient = PatientUtil.getUnknownPatient();
		createAndAddItems_Analysis_Results(groupedAnalyzerResultItems, analysisList, resultList,
				resultToUserSelectionMap, noteList, patient);

		addSampleTypeToSampleItem(sampleItem, analysisList, sample.getAccessionNumber());

		sampleGrouping.sample = sample;
		sampleGrouping.sampleHuman = sampleHuman;
		sampleGrouping.sampleItem = sampleItem;
		sampleGrouping.patient = patient;
		sampleGrouping.analysisList = analysisList;
		sampleGrouping.resultList = resultList;
		sampleGrouping.noteList = noteList;
		sampleGrouping.addSample = true;
		sampleGrouping.addSampleItem = true;
		sampleGrouping.statusSet = statusSet;
		sampleGrouping.accepted = groupedAnalyzerResultItems.get(0).getIsAccepted();
		sampleGrouping.resultToUserserSelectionMap = resultToUserSelectionMap;

		return sampleGrouping;
	}

	private void addSampleTypeToSampleItem(SampleItem sampleItem, List<Analysis> analysisList, String accessionNumber) {
		if (analysisList.size() > 0) {
			String typeOfSampleId = getTypeOfSampleId(analysisList, accessionNumber);
			sampleItem.setTypeOfSample(typeOfSampleDAO.getTypeOfSampleById(typeOfSampleId));
		}
	}

	private String getTypeOfSampleId(List<Analysis> analysisList, String accessionNumber) {
		if (IS_RETROCI && accessionNumber.startsWith("LDBS")) {
			List<TypeOfSampleTest> typeOfSmapleTestList = typeOfSampleTestDAO
					.getTypeOfSampleTestsForTest(analysisList.get(0).getTest().getId());

			for (TypeOfSampleTest typeOfSampleTest : typeOfSmapleTestList) {
				if (DBS_SAMPLE_TYPE_ID.equals(typeOfSampleTest.getTypeOfSampleId())) {
					return DBS_SAMPLE_TYPE_ID;
				}
			}

		}

		return typeOfSampleTestDAO.getTypeOfSampleTestsForTest(analysisList.get(0).getTest().getId()).get(0)
				.getTypeOfSampleId();

	}

	private void createAndAddItems_Analysis_Results(List<AnalyzerResultItem> groupedAnalyzerResultItems,
			List<Analysis> analysisList, List<Result> resultList, Map<Result, String> resultToUserSelectionMap,
			List<Note> noteList, Patient patient) {

		for (AnalyzerResultItem resultItem : groupedAnalyzerResultItems) {
			Analysis analysis = getExistingAnalysis(resultItem);

			if (analysis == null) {
				analysis = new Analysis();
				Test test = new Test();
				test.setId(resultItem.getTestId());
				test = testDAO.getTestById(test);
				populateAnalysis(resultItem, analysis, test);
			} else {
				String statusId = StatusService.getInstance()
						.getStatusID(resultItem.getIsAccepted() ? AnalysisStatus.TechnicalAcceptance
								: AnalysisStatus.TechnicalRejected);
				analysis.setStatusId(statusId);
			}

			analysis.setSysUserId(getSysUserId(request));
			analysisList.add(analysis);

			Result result = getResult(analysis, patient, resultItem);
			resultList.add(result);
			resultToUserSelectionMap.put(result, resultItem.getReflexSelectionId());
			if (GenericValidator.isBlankOrNull(resultItem.getNote())) {
				noteList.add(null);
			} else {
				Note note = new NoteService(analysis).createSavableNote(NoteService.NoteType.INTERNAL,
						resultItem.getNote(), RESULT_SUBJECT, getSysUserId(request));
				noteList.add(note);
			}
		}
	}

	private Analysis getExistingAnalysis(AnalyzerResultItem resultItem) {
		List<Analysis> analysisList = analysisDAO.getAnalysisByAccessionAndTestId(resultItem.getAccessionNumber(),
				resultItem.getTestId());

		return analysisList.isEmpty() ? null : analysisList.get(0);
	}

	private Result getResult(Analysis analysis, Patient patient, AnalyzerResultItem resultItem) {

		Result result = null;

		if (analysis.getId() != null) {
			List<Result> resultList = resultDAO.getResultsByAnalysis(analysis);

			if (!resultList.isEmpty()) {
				result = resultList.get(resultList.size() - 1);
				// this should be refactored -- it's very close to createNewResult
				String resultValue = resultItem.getIsRejected() ? REJECT_VALUE : resultItem.getResult();
				result.setValue(resultValue);
				result.setTestResult(getTestResultForResult(resultItem));
				result.setSysUserId(getSysUserId(request));

				setAnalyte(result);
			}
		}

		if (result == null) {
			result = createNewResult(resultItem, patient);
		}

		return result;
	}

	private void setAnalyte(Result result) {
		TestAnalyte testAnalyte = ResultUtil.getTestAnalyteForResult(result);

		if (testAnalyte != null) {
			result.setAnalyte(testAnalyte.getAnalyte());
		}
	}

	private Result createNewResult(AnalyzerResultItem resultItem, Patient patient) {
		Result result = new Result();
		String resultValue = resultItem.getIsRejected() ? REJECT_VALUE : resultItem.getResult();
		result.setValue(resultValue);
		result.setTestResult(getTestResultForResult(resultItem));
		result.setResultType(resultItem.getTestResultType());
		// the results table is not autmatically updated with the significant digits
		// from TestResult so we must do this
		if (!GenericValidator.isBlankOrNull(resultItem.getSignificantDigits())) {
			result.setSignificantDigits(Integer.parseInt(resultItem.getSignificantDigits()));
		}

		addMinMaxNormal(result, resultItem, patient);
		result.setSysUserId(getSysUserId(request));

		return result;
	}

	private void addMinMaxNormal(Result result, AnalyzerResultItem resultItem, Patient patient) {
		boolean limitsFound = false;

		if (resultItem != null) {
			ResultLimit resultLimit = new ResultLimitService().getResultLimitForTestAndPatient(resultItem.getTestId(),
					patient);
			if (resultLimit != null) {
				result.setMinNormal(resultLimit.getLowNormal());
				result.setMaxNormal(resultLimit.getHighNormal());
				limitsFound = true;
			}
		}

		if (!limitsFound) {
			result.setMinNormal(Double.NEGATIVE_INFINITY);
			result.setMaxNormal(Double.POSITIVE_INFINITY);
		}
	}

	private TestResult getTestResultForResult(AnalyzerResultItem resultItem) {
		if ("D".equals(resultItem.getTestResultType())) {
			TestResult testResult;
			testResult = testResultDAO.getTestResultsByTestAndDictonaryResult(resultItem.getTestId(),
					resultItem.getResult());
			return testResult;
		} else {
			List<TestResult> testResultList = testResultDAO.getActiveTestResultsByTest(resultItem.getTestId());
			// we are assuming there is only one testResult for a numeric
			// type result
			if (!testResultList.isEmpty()) {
				return testResultList.get(0);
			}
		}

		return null;
	}

	private void populateAnalysis(AnalyzerResultItem resultItem, Analysis analysis, Test test) {
		if (!StatusService.getInstance().getStatusID(AnalysisStatus.Canceled).equals(analysis.getStatusId())) {
			String statusId = StatusService.getInstance().getStatusID(
					resultItem.getIsAccepted() ? AnalysisStatus.TechnicalAcceptance : AnalysisStatus.TechnicalRejected);
			analysis.setStatusId(statusId);
			analysis.setAnalysisType(resultItem.getManual() ? ANALYSIS_TYPE_MANUAL : ANALYSIS_TYPE_AUTO);
			analysis.setCompletedDateForDisplay(resultItem.getCompleteDate());
			analysis.setTest(test);
			analysis.setTestSection(test.getTestSection());
			analysis.setIsReportable(test.getIsReportable());
			analysis.setRevision("0");
		}

	}

	private void removeHandledResultsFromAnalyzerResults(List<AnalyzerResults> deletableAnalyzerResults) {

		AnalyzerResultsDAO analyzerResultsDAO = new AnalyzerResultsDAOImpl();
		analyzerResultsDAO.delete(deletableAnalyzerResults);

	}

	private List<AnalyzerResults> getRemovableAnalyzerResults(List<AnalyzerResultItem> actionableResults,
			List<AnalyzerResultItem> childlessControls) {

		Set<AnalyzerResults> deletableAnalyzerResults = new HashSet<>();

		for (AnalyzerResultItem resultItem : actionableResults) {
			AnalyzerResults result = new AnalyzerResults();
			result.setId(resultItem.getId());
			deletableAnalyzerResults.add(result);
		}

		for (AnalyzerResultItem resultItem : childlessControls) {
			AnalyzerResults result = new AnalyzerResults();
			result.setId(resultItem.getId());
			deletableAnalyzerResults.add(result);
		}

		List<AnalyzerResults> resultList = new ArrayList<>();
		resultList.addAll(deletableAnalyzerResults);
		return resultList;
	}

	private List<AnalyzerResultItem> extractActionableResult(List<AnalyzerResultItem> resultItemList) {
		List<AnalyzerResultItem> actionableResultList = new ArrayList<>();

		int currentSampleGrouping = 0;
		boolean acceptResult = false;
		boolean rejectResult = false;
		boolean deleteResult = false;
		String accessionNumber = null;

		for (AnalyzerResultItem resultItem : resultItemList) {

			if (currentSampleGrouping != resultItem.getSampleGroupingNumber()) {
				currentSampleGrouping = resultItem.getSampleGroupingNumber();
				acceptResult = resultItem.getIsAccepted();
				rejectResult = resultItem.getIsRejected();
				deleteResult = resultItem.getIsDeleted();
				// this clears the selection in case of failure
				// Note it also screwed up acception and rejection. This is why we should follow
				// the struts pattern
				// resultItem.setIsAccepted(false);
				// resultItem.setIsRejected(false);
				// resultItem.setIsDeleted(false);
				accessionNumber = resultItem.getAccessionNumber();
			} else {
				resultItem.setAccessionNumber(accessionNumber);
				resultItem.setIsAccepted(acceptResult);
				resultItem.setIsRejected(rejectResult);
				resultItem.setIsDeleted(deleteResult);
			}

			if (acceptResult || rejectResult || deleteResult) {
				actionableResultList.add(resultItem);
			}
		}

		return actionableResultList;
	}

	private List<AnalyzerResultItem> extractChildlessControls(List<AnalyzerResultItem> resultItemList) {
		/*
		 * A childless control is a control which is adjacent to another control. It is
		 * the first set of controls which will be removed. For that reason we're going
		 * through the list backwards.
		 */

		List<AnalyzerResultItem> childLessControlList = new ArrayList<>();
		int sampleGroupingNumber = 0;
		boolean lastGroupIsControl = false;
		boolean inControlGroup = true;// covers the bottom control has no
		// children

		for (int i = resultItemList.size() - 1; i >= 0; i--) {
			AnalyzerResultItem resultItem = resultItemList.get(i);

			if (sampleGroupingNumber != resultItem.getSampleGroupingNumber()) {
				lastGroupIsControl = inControlGroup;
				inControlGroup = resultItem.getIsControl();
				sampleGroupingNumber = resultItem.getSampleGroupingNumber();
			}

			if (lastGroupIsControl && resultItem.getIsControl()) {
				childLessControlList.add(resultItem);
			}
		}

		return childLessControlList;
	}

	@Override
	protected String findLocalForward(String forward) {
		if (FWD_SUCCESS.equals(forward)) {
			return "analyzerResultsDefinition";
		} else if (FWD_FAIL.equals(forward)) {
			return "homePageDefinition";
		} else if (FWD_SUCCESS_INSERT.equals(forward)) {
			return "redirect:/AnalyzerResults.do";
		} else if (FWD_FAIL_INSERT.equals(forward)) {
			return "analyzerResultsDefinition";
		} else if (FWD_VALIDATION_ERROR.equals(forward)) {
			return "analyzerResultsDefinition";
		} else {
			return "PageNotFound";
		}
	}

	@Override
	protected String getPageTitleKey() {
		return "banner.menu.results.analyzer";
	}

	@Override
	protected String getPageSubtitleKey() {
		String key = analyzerNameToSubtitleKey.get(getAnalyzerNameFromRequest());
		if (key == null) {
			key = PluginMenuService.getInstance()
					.getKeyForAction("/AnalyzerResults.do?type=" + request.getParameter("type"));
		}
		return key;

	}

	public class SampleGrouping {
		public boolean accepted = true;
		public Sample sample;
		private SampleHuman sampleHuman;
		public Patient patient;
		private List<Note> noteList;
		private SampleItem sampleItem;
		private List<Analysis> analysisList;
		public List<Result> resultList;
		public Map<String, List<String>> triggersToSelectedReflexesMap;
		private StatusSet statusSet;
		private boolean addSample = false; // implies adding patient
		private boolean updateSample = false;
		private boolean addSampleItem = false;
		private Map<Result, String> resultToUserserSelectionMap;

	}
}