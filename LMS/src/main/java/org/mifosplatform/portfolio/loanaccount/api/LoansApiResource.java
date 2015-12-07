/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.api;

import java.io.File;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.StringUtils;
import org.mifosplatform.commands.domain.CommandWrapper;
import org.mifosplatform.commands.service.CommandWrapperBuilder;
import org.mifosplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.mifosplatform.infrastructure.codes.data.CodeValueData;
import org.mifosplatform.infrastructure.codes.service.CodeValueReadPlatformService;
import org.mifosplatform.infrastructure.core.api.ApiParameterHelper;
import org.mifosplatform.infrastructure.core.api.ApiRequestParameterHelper;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.api.JsonQuery;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.infrastructure.core.exception.UnrecognizedQueryParamException;
import org.mifosplatform.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.mifosplatform.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.infrastructure.core.service.Page;
import org.mifosplatform.infrastructure.documentmanagement.contentrepository.FileSystemContentRepository;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.organisation.feemaster.data.FeeMasterData;
import org.mifosplatform.organisation.feemaster.service.FeeMasterReadplatformService;
import org.mifosplatform.organisation.monetary.data.CurrencyData;
import org.mifosplatform.organisation.staff.data.StaffData;
import org.mifosplatform.organisation.taxmapping.data.LoanTaxMapData;
import org.mifosplatform.organisation.taxmapping.data.TaxMapData;
import org.mifosplatform.organisation.taxmapping.service.TaxMapReadPlatformService;
import org.mifosplatform.portfolio.account.PortfolioAccountType;
import org.mifosplatform.portfolio.account.data.PortfolioAccountData;
import org.mifosplatform.portfolio.account.service.AccountAssociationsReadPlatformService;
import org.mifosplatform.portfolio.account.service.PortfolioAccountReadPlatformService;
import org.mifosplatform.portfolio.calendar.data.CalendarData;
import org.mifosplatform.portfolio.calendar.service.CalendarReadPlatformService;
import org.mifosplatform.portfolio.charge.data.ChargeData;
import org.mifosplatform.portfolio.charge.service.ChargeReadPlatformService;
import org.mifosplatform.portfolio.collateral.data.CollateralData;
import org.mifosplatform.portfolio.collateral.service.CollateralReadPlatformService;
import org.mifosplatform.portfolio.fund.data.FundData;
import org.mifosplatform.portfolio.fund.service.FundReadPlatformService;
import org.mifosplatform.portfolio.group.data.GroupGeneralData;
import org.mifosplatform.portfolio.group.service.GroupReadPlatformService;
import org.mifosplatform.portfolio.group.service.SearchParameters;
import org.mifosplatform.portfolio.insurance.data.InsuranceData;
import org.mifosplatform.portfolio.insurance.service.InsuranceReadPlatformService;
import org.mifosplatform.portfolio.loanaccount.data.LoanAccountData;
import org.mifosplatform.portfolio.loanaccount.data.LoanChargeData;
import org.mifosplatform.portfolio.loanaccount.data.LoanFeeMasterData;
import org.mifosplatform.portfolio.loanaccount.data.LoanTaxData;
import org.mifosplatform.portfolio.loanaccount.data.LoanTransactionData;
import org.mifosplatform.portfolio.loanaccount.data.RepaymentScheduleRelatedLoanData;
import org.mifosplatform.portfolio.loanaccount.exception.LeaseScreenReportFileNameNotNullException;
import org.mifosplatform.portfolio.loanaccount.exception.LeaseScreenReportFileNotFoundException;
import org.mifosplatform.portfolio.loanaccount.exception.LoanTemplateTypeRequiredException;
import org.mifosplatform.portfolio.loanaccount.exception.NotSupportedLoanTemplateTypeException;
import org.mifosplatform.portfolio.loanaccount.guarantor.data.GuarantorData;
import org.mifosplatform.portfolio.loanaccount.guarantor.service.GuarantorReadPlatformService;
import org.mifosplatform.portfolio.loanaccount.loanschedule.data.LoanScheduleData;
import org.mifosplatform.portfolio.loanaccount.loanschedule.domain.LoanScheduleModel;
import org.mifosplatform.portfolio.loanaccount.loanschedule.service.LoanScheduleCalculationPlatformService;
import org.mifosplatform.portfolio.loanaccount.service.LoanCalculatorWritePlatformService;
import org.mifosplatform.portfolio.loanaccount.service.LoanChargeReadPlatformService;
import org.mifosplatform.portfolio.loanaccount.service.LoanFeeMasterDataReadPlatformService;
import org.mifosplatform.portfolio.loanaccount.service.LoanReadPlatformService;
import org.mifosplatform.portfolio.loanaccount.service.LoanTaxReadPlatformService;
import org.mifosplatform.portfolio.loanproduct.data.LoanProductData;
import org.mifosplatform.portfolio.loanproduct.data.TransactionProcessingStrategyData;
import org.mifosplatform.portfolio.loanproduct.service.LoanDropdownReadPlatformService;
import org.mifosplatform.portfolio.loanproduct.service.LoanProductReadPlatformService;
import org.mifosplatform.portfolio.note.data.NoteData;
import org.mifosplatform.portfolio.note.domain.NoteType;
import org.mifosplatform.portfolio.note.service.NoteReadPlatformServiceImpl;
import org.mifosplatform.portfolio.savings.domain.SavingsAccountStatusType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

@Path("/loans")
@Component
@Scope("singleton")
public class LoansApiResource {

    private final Set<String> LOAN_DATA_PARAMETERS = new HashSet<String>(Arrays.asList("id", "accountNo", "status", "externalId",
            "clientId", "group", "loanProductId", "loanProductName", "loanProductDescription", "fundId", "fundName", "loanPurposeId",
            "loanPurposeName", "loanOfficerId", "loanOfficerName", "currency", "principal", "totalOverpaid", "inArrearsTolerance",
            "termFrequency", "termPeriodFrequencyType", "numberOfRepayments", "repaymentEvery", "interestRatePerPeriod",
            "annualInterestRate", "repaymentFrequencyType", "transactionProcessingStrategyId", "transactionProcessingStrategyName",
            "interestRateFrequencyType", "amortizationType", "interestType", "interestCalculationPeriodType",
            "expectedFirstRepaymentOnDate", "graceOnPrincipalPayment", "graceOnInterestPayment", "graceOnInterestCharged",
            "interestChargedFromDate", "timeline", "totalFeeChargesAtDisbursement", "summary", "repaymentSchedule", "transactions",
            "charges", "collateral", "guarantors", "meeting", "productOptions", "amortizationTypeOptions", "interestTypeOptions",
            "interestCalculationPeriodTypeOptions", "repaymentFrequencyTypeOptions", "termFrequencyTypeOptions",
            "interestRateFrequencyTypeOptions", "fundOptions", "repaymentStrategyOptions", "chargeOptions", "loanOfficerOptions",
            "loanPurposeOptions", "loanCollateralOptions", "chargeTemplate", "calendarOptions", "syncDisbursementWithMeeting",
            "loanCounter", "loanProductCounter", "notes", "accountLinkingOptions", "linkedAccount"));

    private final String resourceNameForPermissions = "LOAN";
    private static final String LEASE = "LeaseCalculator";
    private static final String PROSPECT = "Prospect";

    private final PlatformSecurityContext context;
    private final LoanReadPlatformService loanReadPlatformService;
    private final LoanProductReadPlatformService loanProductReadPlatformService;
    private final LoanDropdownReadPlatformService dropdownReadPlatformService;
    private final FundReadPlatformService fundReadPlatformService;
    private final ChargeReadPlatformService chargeReadPlatformService;
    private final LoanChargeReadPlatformService loanChargeReadPlatformService;
    private final CollateralReadPlatformService loanCollateralReadPlatformService;
    private final LoanScheduleCalculationPlatformService calculationPlatformService;
    private final GuarantorReadPlatformService guarantorReadPlatformService;
    private final CodeValueReadPlatformService codeValueReadPlatformService;
    private final GroupReadPlatformService groupReadPlatformService;
    private final DefaultToApiJsonSerializer<LoanAccountData> toApiJsonSerializer;
    private final DefaultToApiJsonSerializer<LoanScheduleData> loanScheduleToApiJsonSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final FromJsonHelper fromJsonHelper;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final CalendarReadPlatformService calendarReadPlatformService;
    private final NoteReadPlatformServiceImpl noteReadPlatformService;
    private final PortfolioAccountReadPlatformService portfolioAccountReadPlatformService;
    private final AccountAssociationsReadPlatformService accountAssociationsReadPlatformService;
    private final InsuranceReadPlatformService insuranceReadPlatformService;
    private final LoanTaxReadPlatformService loanTaxReadPlatformService;
    private final TaxMapReadPlatformService taxMapReadPlatformService;
    private final DefaultToApiJsonSerializer<LoanTaxMapData> loanTaxMappingApiJsonSerializer;
    private final LoanFeeMasterDataReadPlatformService loanFeeMasterDataReadPlatformService;
    private final FeeMasterReadplatformService feeMasterReadPlatformService;
    private final LoanCalculatorWritePlatformService loanCalculatorWritePlatformService;
    

    @Autowired
    public LoansApiResource(final PlatformSecurityContext context, final LoanReadPlatformService loanReadPlatformService,
            final LoanProductReadPlatformService loanProductReadPlatformService,
            final LoanDropdownReadPlatformService dropdownReadPlatformService, final FundReadPlatformService fundReadPlatformService,
            final ChargeReadPlatformService chargeReadPlatformService, final LoanChargeReadPlatformService loanChargeReadPlatformService,
            final CollateralReadPlatformService loanCollateralReadPlatformService,
            final LoanScheduleCalculationPlatformService calculationPlatformService,
            final GuarantorReadPlatformService guarantorReadPlatformService,
            final CodeValueReadPlatformService codeValueReadPlatformService, final GroupReadPlatformService groupReadPlatformService,
            final DefaultToApiJsonSerializer<LoanAccountData> toApiJsonSerializer,
            final DefaultToApiJsonSerializer<LoanScheduleData> loanScheduleToApiJsonSerializer,
            final ApiRequestParameterHelper apiRequestParameterHelper, final FromJsonHelper fromJsonHelper,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
            final CalendarReadPlatformService calendarReadPlatformService, final NoteReadPlatformServiceImpl noteReadPlatformService,
            final PortfolioAccountReadPlatformService portfolioAccountReadPlatformServiceImpl,
            final AccountAssociationsReadPlatformService accountAssociationsReadPlatformService,
            final InsuranceReadPlatformService insuranceReadPlatformService,final LoanTaxReadPlatformService loanTaxReadPlatformService,
            final TaxMapReadPlatformService taxMapReadPlatformService, 
            final DefaultToApiJsonSerializer<LoanTaxMapData> loanTaxMappingApiJsonSerializer,final LoanFeeMasterDataReadPlatformService loanFeeMasterDataReadPlatformService,
            final FeeMasterReadplatformService feeMasterReadPlatformService,final LoanCalculatorWritePlatformService loanCalculatorWritePlatformService) {
        this.context = context;
        this.loanReadPlatformService = loanReadPlatformService;
        this.loanProductReadPlatformService = loanProductReadPlatformService;
        this.dropdownReadPlatformService = dropdownReadPlatformService;
        this.fundReadPlatformService = fundReadPlatformService;
        this.chargeReadPlatformService = chargeReadPlatformService;
        this.loanChargeReadPlatformService = loanChargeReadPlatformService;
        this.loanCollateralReadPlatformService = loanCollateralReadPlatformService;
        this.calculationPlatformService = calculationPlatformService;
        this.guarantorReadPlatformService = guarantorReadPlatformService;
        this.codeValueReadPlatformService = codeValueReadPlatformService;
        this.groupReadPlatformService = groupReadPlatformService;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.loanScheduleToApiJsonSerializer = loanScheduleToApiJsonSerializer;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.fromJsonHelper = fromJsonHelper;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
        this.calendarReadPlatformService = calendarReadPlatformService;
        this.noteReadPlatformService = noteReadPlatformService;
        this.portfolioAccountReadPlatformService = portfolioAccountReadPlatformServiceImpl;
        this.accountAssociationsReadPlatformService = accountAssociationsReadPlatformService;
        this.insuranceReadPlatformService=insuranceReadPlatformService;
        this.loanTaxReadPlatformService=loanTaxReadPlatformService;
        this.taxMapReadPlatformService=taxMapReadPlatformService;
        this.loanTaxMappingApiJsonSerializer = loanTaxMappingApiJsonSerializer;
        this.loanFeeMasterDataReadPlatformService = loanFeeMasterDataReadPlatformService;
        this.feeMasterReadPlatformService = feeMasterReadPlatformService;
        this.loanCalculatorWritePlatformService = loanCalculatorWritePlatformService;
    }
    
    public LoanAccountData getTemplate(Long clientId, Long groupId, Long productId, 
    		String templateType, boolean staffInSelectedOfficeOnly) {
    	
    	this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);
        // template
        final Collection<LoanProductData> productOptions = this.loanProductReadPlatformService.retrieveAllLoanProductsForLookup();
        // options
        Collection<StaffData> allowedLoanOfficers = null;
        Collection<CodeValueData> loanCollateralOptions = null;
        Collection<CalendarData> calendarOptions = null;
        LoanAccountData newLoanAccount = null;
        Long officeId = null;

        if (productId != null) {
            newLoanAccount = this.loanReadPlatformService.retrieveLoanProductDetailsTemplate(productId, clientId, groupId);
        }

        if (templateType == null) {
            final String errorMsg = "Loan template type must be provided";
            throw new LoanTemplateTypeRequiredException(errorMsg);
        } else if (templateType.equals("collateral")) {
            loanCollateralOptions = this.codeValueReadPlatformService.retrieveCodeValuesByCode("LeaseCollateral");
            newLoanAccount = LoanAccountData.collateralTemplate(loanCollateralOptions);
        } else {
            // for JLG loan both client and group details are required
            if (templateType.equals("individual") || templateType.equals("jlg")) {

                final LoanAccountData loanAccountClientDetails = this.loanReadPlatformService.retrieveClientDetailsTemplate(clientId);

                officeId = loanAccountClientDetails.officeId();

                newLoanAccount = newLoanAccount == null ? loanAccountClientDetails : LoanAccountData.populateClientDefaults(
                        newLoanAccount, loanAccountClientDetails);

                // if it's JLG loan add group details
                if (templateType.equals("jlg")) {
                    final GroupGeneralData group = this.groupReadPlatformService.retrieveOne(groupId);
                    newLoanAccount = LoanAccountData.associateGroup(newLoanAccount, group);
                    calendarOptions = this.loanReadPlatformService.retrieveCalendars(groupId);
                }

            } else if (templateType.equals("group")) {

                final LoanAccountData loanAccountGroupData = this.loanReadPlatformService.retrieveGroupDetailsTemplate(groupId);
                officeId = loanAccountGroupData.groupOfficeId();
                calendarOptions = this.loanReadPlatformService.retrieveCalendars(groupId);
                newLoanAccount = newLoanAccount == null ? loanAccountGroupData : LoanAccountData.populateGroupDefaults(newLoanAccount,
                        loanAccountGroupData);

            } else if (templateType.equals("jlgbulk")) {
                // get group details along with members in that group
                final LoanAccountData loanAccountGroupData = this.loanReadPlatformService.retrieveGroupAndMembersDetailsTemplate(groupId);
                officeId = loanAccountGroupData.groupOfficeId();
                calendarOptions = this.loanReadPlatformService.retrieveCalendars(groupId);
                newLoanAccount = newLoanAccount == null ? loanAccountGroupData : LoanAccountData.populateGroupDefaults(newLoanAccount,
                        loanAccountGroupData);
            } else {
                final String errorMsg = "Loan template type '" + templateType + "' is not supported";
                throw new NotSupportedLoanTemplateTypeException(errorMsg, templateType);
            }

            allowedLoanOfficers = this.loanReadPlatformService.retrieveAllowedLoanOfficers(officeId, staffInSelectedOfficeOnly);

            Collection<PortfolioAccountData> accountLinkingOptions = null;
            if (clientId != null) {
                final CurrencyData currencyData = newLoanAccount.currency();
                String currencyCode = null;
                if (currencyData != null) {
                    currencyCode = currencyData.code();
                }
                final long[] accountStatus = { SavingsAccountStatusType.ACTIVE.getValue() };
                accountLinkingOptions = this.portfolioAccountReadPlatformService.retrieveAllForLookup(
                        PortfolioAccountType.SAVINGS.getValue(), clientId, currencyCode, accountStatus);

            }

            // add product options, allowed loan officers and calendar options
            // (calendar options will be null in individual loan)
            newLoanAccount = LoanAccountData.associationsAndTemplate(newLoanAccount, productOptions, allowedLoanOfficers, calendarOptions,
                    accountLinkingOptions);
        }
        
        return newLoanAccount;
    }

    @GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String template(@QueryParam("clientId") final Long clientId, @QueryParam("groupId") final Long groupId,
            @QueryParam("productId") final Long productId, @QueryParam("templateType") final String templateType,
            @DefaultValue("false") @QueryParam("staffInSelectedOfficeOnly") final boolean staffInSelectedOfficeOnly,
            @Context final UriInfo uriInfo) {
    	LoanAccountData newLoanAccount = getTemplate(clientId, groupId, productId, templateType, staffInSelectedOfficeOnly);
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, newLoanAccount, this.LOAN_DATA_PARAMETERS);
    }

    @GET
    @Path("/printleasedoc/{loanId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public void printlease(@PathParam("loanId") final Long loanId) {
        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);
        loanReadPlatformService.printLeaseDetails(loanId);
    }
    
    @GET
    @Path("/printlsrdoc/{loanId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public Response printleaseScreenReport(@PathParam("loanId") final Long loanId) {
    	
        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);
        String fileName = loanReadPlatformService.printLeaseScreenReportDoc(loanId);
        
        if(null == fileName) {
        	throw new LeaseScreenReportFileNameNotNullException();
        }

        File file = new File(fileName);
        
        if(!file.exists()) {
        	String fileNameData = fileName.replace("\\\\", "/");
        	file = new File(fileNameData);
        	if(!file.exists()) {
        		throw new LeaseScreenReportFileNotFoundException(fileNameData);
        	}    	
        }
        
        final ResponseBuilder response = Response.ok(file);
        response.header("Content-Disposition", "attachment; filename=" + file.getName().replace(" ", "_"));
        response.header("Content-Type", "application/pdf");
        
        return response.build();
    }
    
    @GET
    @Path("{loanId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveLoan(@PathParam("loanId") final Long loanId,
            @DefaultValue("false") @QueryParam("staffInSelectedOfficeOnly") final boolean staffInSelectedOfficeOnly,
            @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

        final LoanAccountData loanBasicDetails = this.loanReadPlatformService.retrieveOne(loanId);

        Collection<LoanTransactionData> loanRepayments = null;
        LoanScheduleData repaymentSchedule = null;
        Collection<LoanChargeData> charges = null;
        Collection<GuarantorData> guarantors = null;
        Collection<InsuranceData> insurances=null;
        Collection<CollateralData> collateral = null;
        CalendarData meeting = null;
        Collection<NoteData> notes = null;
        PortfolioAccountData linkedAccount = null;
        Collection<LoanTaxData> taxes = null;
        Collection<LoanFeeMasterData> deposits = null;

        final Set<String> mandatoryResponseParameters = new HashSet<String>();
        final Set<String> associationParameters = ApiParameterHelper.extractAssociationsForResponseIfProvided(uriInfo.getQueryParameters());
        if (!associationParameters.isEmpty()) {

        	 if (associationParameters.contains("all")) {
                 associationParameters.addAll(Arrays.asList("repaymentSchedule", "transactions", "charges", "guarantors", "collateral",
                         "notes", "linkedAccount", "insurances","taxes","deposits"));
             }
            if (associationParameters.contains("guarantors")) {
                mandatoryResponseParameters.add("guarantors");
                guarantors = this.guarantorReadPlatformService.retrieveGuarantorsForLoan(loanId);
                if (CollectionUtils.isEmpty(guarantors)) {
                    guarantors = null;
                }
            }
            if (associationParameters.contains("insurances")) {
                mandatoryResponseParameters.add("insurances");
                insurances= this.insuranceReadPlatformService.retrieveInsuracetData(loanId);
                if (CollectionUtils.isEmpty(insurances)) {
                	insurances = null;
                }
            }

            if (associationParameters.contains("transactions")) {
                mandatoryResponseParameters.add("transactions");
                final Collection<LoanTransactionData> currentLoanRepayments = this.loanReadPlatformService.retrieveLoanTransactions(loanId);
                if (!CollectionUtils.isEmpty(currentLoanRepayments)) {
                    loanRepayments = currentLoanRepayments;
                }
            }

            if (associationParameters.contains("repaymentSchedule")) {
                mandatoryResponseParameters.add("repaymentSchedule");

                final RepaymentScheduleRelatedLoanData repaymentScheduleRelatedData = loanBasicDetails.repaymentScheduleRelatedData();
                repaymentSchedule = this.loanReadPlatformService.retrieveRepaymentSchedule(loanId, repaymentScheduleRelatedData);
            }

            if (associationParameters.contains("charges")) {
                mandatoryResponseParameters.add("charges");
                charges = this.loanChargeReadPlatformService.retrieveLoanCharges(loanId);
                if (CollectionUtils.isEmpty(charges)) {
                    charges = null;
                }
            }
            
            if (associationParameters.contains("taxes")) {
            	mandatoryResponseParameters.add("taxes");
            	taxes = this.loanTaxReadPlatformService.retrieveLoanTaxes(loanId);
            	if (CollectionUtils.isEmpty(taxes)) {
            		taxes = null;
            	}
            }
            
            if (associationParameters.contains("deposits")) {
            	mandatoryResponseParameters.add("deposits");
            	deposits = this.loanFeeMasterDataReadPlatformService.retrieveLoanFeeMasterData(loanId);
            	if (CollectionUtils.isEmpty(deposits)) {
            		deposits = null;
            	}
            }

            if (associationParameters.contains("collateral")) {
                mandatoryResponseParameters.add("collateral");
                collateral = this.loanCollateralReadPlatformService.retrieveCollaterals(loanId);
                if (CollectionUtils.isEmpty(collateral)) {
                    collateral = null;
                }
            }

            if (associationParameters.contains("meeting")) {
                mandatoryResponseParameters.add("meeting");
                meeting = this.calendarReadPlatformService.retrieveLoanCalendar(loanId);
            }

            if (associationParameters.contains("notes")) {
                mandatoryResponseParameters.add("notes");
                notes = this.noteReadPlatformService.retrieveNotesByResource(loanId, NoteType.LOAN.getValue());
                if (CollectionUtils.isEmpty(notes)) {
                    notes = null;
                }
            }

            if (associationParameters.contains("linkedAccount")) {
                mandatoryResponseParameters.add("linkedAccount");
                linkedAccount = this.accountAssociationsReadPlatformService.retriveLoanAssociation(loanId);
            }
        }

        Collection<LoanProductData> productOptions = null;
        Collection<EnumOptionData> loanTermFrequencyTypeOptions = null;
        Collection<EnumOptionData> repaymentFrequencyTypeOptions = null;
        Collection<TransactionProcessingStrategyData> repaymentStrategyOptions = null;
        Collection<EnumOptionData> interestRateFrequencyTypeOptions = null;
        Collection<EnumOptionData> amortizationTypeOptions = null;
        Collection<EnumOptionData> interestTypeOptions = null;
        Collection<EnumOptionData> interestCalculationPeriodTypeOptions = null;
        Collection<FundData> fundOptions = null;
        Collection<StaffData> allowedLoanOfficers = null;
        Collection<ChargeData> chargeOptions = null;
        ChargeData chargeTemplate = null;
        Collection<CodeValueData> loanPurposeOptions = null;
        Collection<CodeValueData> loanCollateralOptions = null;
        Collection<CalendarData> calendarOptions = null;
        Collection<PortfolioAccountData> accountLinkingOptions = null;
        Collection<TaxMapData> taxMapData = null;
        Collection<FeeMasterData> feeMasterDataOptions = null;

        final boolean template = ApiParameterHelper.template(uriInfo.getQueryParameters());
        if (template) {
            productOptions = this.loanProductReadPlatformService.retrieveAllLoanProductsForLookup();
            loanTermFrequencyTypeOptions = this.dropdownReadPlatformService.retrieveLoanTermFrequencyTypeOptions();
            repaymentFrequencyTypeOptions = this.dropdownReadPlatformService.retrieveRepaymentFrequencyTypeOptions();
            interestRateFrequencyTypeOptions = this.dropdownReadPlatformService.retrieveInterestRateFrequencyTypeOptions();

            amortizationTypeOptions = this.dropdownReadPlatformService.retrieveLoanAmortizationTypeOptions();
            interestTypeOptions = this.dropdownReadPlatformService.retrieveLoanInterestTypeOptions();
            interestCalculationPeriodTypeOptions = this.dropdownReadPlatformService.retrieveLoanInterestRateCalculatedInPeriodOptions();

            fundOptions = this.fundReadPlatformService.retrieveAllFunds();
            repaymentStrategyOptions = this.dropdownReadPlatformService.retreiveTransactionProcessingStrategies();
            final boolean feeChargesOnly = false;
            chargeOptions = this.chargeReadPlatformService.retrieveLoanApplicableCharges(feeChargesOnly);
            taxMapData = this.taxMapReadPlatformService.retriveTaxMapData(true);
            feeMasterDataOptions = this.feeMasterReadPlatformService.retrieveAllData("Deposit");
            chargeTemplate = this.loanChargeReadPlatformService.retrieveLoanChargeTemplate();

            allowedLoanOfficers = this.loanReadPlatformService.retrieveAllowedLoanOfficers(loanBasicDetails.officeId(),
                    staffInSelectedOfficeOnly);

            loanPurposeOptions = this.codeValueReadPlatformService.retrieveCodeValuesByCode("LeasePurpose");
            loanCollateralOptions = this.codeValueReadPlatformService.retrieveCodeValuesByCode("LeaseCollateral");
            final CurrencyData currencyData = loanBasicDetails.currency();
            String currencyCode = null;
            if (currencyData != null) {
                currencyCode = currencyData.code();
            }
            final long[] accountStatus = { SavingsAccountStatusType.ACTIVE.getValue() };
            accountLinkingOptions = this.portfolioAccountReadPlatformService.retrieveAllForLookup(PortfolioAccountType.SAVINGS.getValue(),
                    loanBasicDetails.clientId(), currencyCode, accountStatus);

            if (!associationParameters.contains("linkedAccount")) {
                mandatoryResponseParameters.add("linkedAccount");
                linkedAccount = this.accountAssociationsReadPlatformService.retriveLoanAssociation(loanId);
            }
            if (loanBasicDetails.groupId() != null) {
                calendarOptions = this.loanReadPlatformService.retrieveCalendars(loanBasicDetails.groupId());
            }

        }
        //madhav   used for the s
      BigDecimal principaPlusInterest=loanReadPlatformService.retrivePrincipalForReSchedule(loanId);
                       if(principaPlusInterest.intValue()>0)
                       loanBasicDetails.setPrincipal(principaPlusInterest);   
        
        final LoanAccountData loanAccount = LoanAccountData.associationsAndTemplate(loanBasicDetails, repaymentSchedule, loanRepayments,
                charges, collateral, guarantors, meeting, productOptions, loanTermFrequencyTypeOptions, repaymentFrequencyTypeOptions,
                repaymentStrategyOptions, interestRateFrequencyTypeOptions, amortizationTypeOptions, interestTypeOptions,
                interestCalculationPeriodTypeOptions, fundOptions, chargeOptions, chargeTemplate, allowedLoanOfficers, loanPurposeOptions,
                loanCollateralOptions, calendarOptions, notes,insurances, accountLinkingOptions, linkedAccount,taxMapData, taxes,feeMasterDataOptions,deposits );

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters(),
                mandatoryResponseParameters);
        return this.toApiJsonSerializer.serialize(settings, loanAccount, this.LOAN_DATA_PARAMETERS);
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveAll(@Context final UriInfo uriInfo, @QueryParam("sqlSearch") final String sqlSearch,
            @QueryParam("externalId") final String externalId,
//            @QueryParam("underHierarchy") final String hierarchy,
            @QueryParam("offset") final Integer offset, @QueryParam("limit") final Integer limit,
            @QueryParam("orderBy") final String orderBy, @QueryParam("sortOrder") final String sortOrder) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

        final SearchParameters searchParameters = SearchParameters.forLoans(sqlSearch, externalId, offset, limit, orderBy, sortOrder);

        final Page<LoanAccountData> loanBasicDetails = this.loanReadPlatformService.retrieveAll(searchParameters);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, loanBasicDetails, this.LOAN_DATA_PARAMETERS);
    }
    
    @GET
    @Path("tax/{loanId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveLoanTaxMappingDetails(@Context final UriInfo uriInfo, @PathParam("loanId") final Long loanId) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

        final List<LoanTaxMapData> loanTaxMapData = this.loanReadPlatformService.retrieveLoanTaxMappingData(loanId);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.loanTaxMappingApiJsonSerializer.serialize(settings, loanTaxMapData, this.LOAN_DATA_PARAMETERS);
    }
    
    @POST
    @Path("tax")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String calculateTaxForLoanApplication(@QueryParam("flag") final Long flag, final String apiRequestBodyAsJson) {

		Long value = 0L;
		
		if (null != flag) {
			value = flag;
		}

		final CommandWrapper commandRequest = new CommandWrapperBuilder().createLoanTaxMap(value).withJson(apiRequestBodyAsJson).build();
        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        return this.toApiJsonSerializer.serialize(result);
	}
    
    @PUT
    @Path("tax")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String updateLoanTaxMapping(final String apiRequestBodyAsJson) {

		final CommandWrapper commandRequest = new CommandWrapperBuilder().updateLoanTaxMap().withJson(apiRequestBodyAsJson).build();
        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        return this.toApiJsonSerializer.serialize(result);
	}
    
    @DELETE
    @Path("tax/{loanId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String deleteLoanTaxMapping(@PathParam("loanId") final Long loanId) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteLoanTaxMap(loanId).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String calculateLoanScheduleOrSubmitLoanApplication(@QueryParam("command") final String commandParam,
			@Context final UriInfo uriInfo, final String apiRequestBodyAsJson) {

		String testApiRequestBodyAsJson = calculateTax(apiRequestBodyAsJson, commandParam);

		final JsonElement parsedQuery = this.fromJsonHelper.parse(testApiRequestBodyAsJson);

		JsonArray taxArray = this.fromJsonHelper.extractJsonArrayNamed("taxArray", parsedQuery);

		if (is(commandParam, "calculateLoanSchedule")) {

			final JsonQuery query = JsonQuery.from(testApiRequestBodyAsJson, parsedQuery, this.fromJsonHelper);

			final LoanScheduleModel loanSchedule = this.calculationPlatformService.calculateLoanSchedule(query);

			loanSchedule.setTaxesAsString(taxArray.toString());

			final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

			return this.loanScheduleToApiJsonSerializer.serialize(settings, loanSchedule.toData(), new HashSet<String>());
		}

		final CommandWrapper commandRequest = new CommandWrapperBuilder().createLoanApplication().withJson(testApiRequestBodyAsJson).build();

		final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

		if (null != result && result.getLoanId() > 0) {

			JsonObject object = new JsonObject();
			object.addProperty("loanId", result.getLoanId());
			object.add("taxArray", taxArray);
			updateLoanTaxMapping(object.toString());
		}
		
		return this.toApiJsonSerializer.serialize(result);
	}

    private String calculateTax(String apiRequestBodyAsJson, String commandParam) {
		
    	 Long flag = new Long(1);
    	 
    	 if (is(commandParam, "calculateLoanSchedule")) {
    		 flag = new Long(0);
    	 }
    	 
    	JsonElement element = this.fromJsonHelper.parse(apiRequestBodyAsJson);
    	
    	BigDecimal principal = this.fromJsonHelper.extractBigDecimalWithLocaleNamed("principal", element);
    	String locale = this.fromJsonHelper.extractStringNamed("locale", element);
    	JsonArray taxes = this.fromJsonHelper.extractJsonArrayNamed("taxes", element);
    
    	JsonObject object = new JsonObject();
    	
    	object.addProperty("principal", principal);
    	object.addProperty("locale", locale);
    	object.add("taxes", taxes);
    	
    	String returnOutput = calculateTaxForLoanApplication(flag, object.toString());
    	
    	object = element.getAsJsonObject();
    	
    	element = this.fromJsonHelper.parse(returnOutput);
    	
    	returnOutput = this.fromJsonHelper.extractStringNamed("resourceIdentifier", element);
    	
    	element = this.fromJsonHelper.parse(returnOutput);
    	
    	JsonArray taxArray = this.fromJsonHelper.extractJsonArrayNamed("taxArray", element);
    	BigDecimal finalAmount = this.fromJsonHelper.extractBigDecimalWithLocaleNamed("finalAmount", element);
    	 	
    	object.addProperty("principal", finalAmount);
    	object.add("taxArray", taxArray);
    	
    	object.remove("taxes");
    	
		return object.toString();
	}

	@PUT
    @Path("{loanId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String modifyLoanApplication(@PathParam("loanId") final Long loanId, final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateLoanApplication(loanId).withJson(apiRequestBodyAsJson)
                .build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @DELETE
    @Path("{loanId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String deleteLoanApplication(@PathParam("loanId") final Long loanId) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteLoanApplication(loanId).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }
    
    @DELETE
    @Path("tax")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String deleteAllLoanTaxMapping(final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteAllLoanTaxMap().withJson(apiRequestBodyAsJson).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @POST
    @Path("{loanId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String stateTransitions(@PathParam("loanId") final Long loanId, @QueryParam("command") final String commandParam,
            final String apiRequestBodyAsJson) {

        final CommandWrapperBuilder builder = new CommandWrapperBuilder().withJson(apiRequestBodyAsJson);

        CommandProcessingResult result = null;

        if (is(commandParam, "reject")) {
            final CommandWrapper commandRequest = builder.rejectLoanApplication(loanId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, "withdrawnByApplicant")) {
            final CommandWrapper commandRequest = builder.withdrawLoanApplication(loanId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, "approve")) {
            final CommandWrapper commandRequest = builder.approveLoanApplication(loanId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, "disburse")) {
            final CommandWrapper commandRequest = builder.disburseLoanApplication(loanId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        }

        if (is(commandParam, "undoapproval")) {
            final CommandWrapper commandRequest = builder.undoLoanApplicationApproval(loanId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, "undodisbursal")) {
            final CommandWrapper commandRequest = builder.undoLoanApplicationDisbursal(loanId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        }

        if (is(commandParam, "assignloanofficer")) {
            final CommandWrapper commandRequest = builder.assignLoanOfficer(loanId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, "unassignloanofficer")) {
            final CommandWrapper commandRequest = builder.unassignLoanOfficer(loanId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        }

        if (result == null) { throw new UnrecognizedQueryParamException("command", commandParam); }

        return this.toApiJsonSerializer.serialize(result);
    }

    private boolean is(final String commandParam, final String commandValue) {
        return StringUtils.isNotBlank(commandParam) && commandParam.trim().equalsIgnoreCase(commandValue);
    }
    
    @POST
    @Path("calculator")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String loanCalculator(final String apiRequestBodyAsJson, @QueryParam("command") String commandParam) {
       
    	Long entityId = new Long(0);
    	
    	if (null != commandParam && commandParam.equalsIgnoreCase(PROSPECT)) {
    		entityId = new Long(1);
		}
    	
    	final CommandWrapper commandRequest = new CommandWrapperBuilder().createLoanCalculator(entityId).withJson(apiRequestBodyAsJson).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        
        if(null == result) {
        	return null;
        }
		entityId = result.resourceId() == null ? 0 : result.resourceId();
        
        Map<String, Object> changes = result.getChanges();
        
        if(null != changes && !changes.isEmpty()) {
        
        	JsonObject object = this.fromJsonHelper.parse(changes.get("data").toString()).getAsJsonObject();
        	object.addProperty("prospectLoanCalculatorId", entityId);
        	
        	return object.toString();
        }
        return null;
    }
    
    @POST
    @Path("calculator/export")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
	public String exportToXlsx(final String apiRequestBodyAsJson,
			@QueryParam("command") final String commandParam, @QueryParam("type") final String type) {

		this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

		String jsonObjectInString = this.loanCalculatorWritePlatformService.getExportJsonString(apiRequestBodyAsJson, commandParam);

		String fileName = this.loanReadPlatformService.export(jsonObjectInString, type);

		File file = new File(fileName);

		if (!file.exists()) {
			throw new LeaseScreenReportFileNotFoundException(fileName);
		}

		JsonElement element = this.fromJsonHelper.parse(jsonObjectInString);

		Long prospectLoanCalculatorId = this.fromJsonHelper.extractLongNamed("prospectLoanCalculatorId", element);

		JsonObject jsonObject = new JsonObject();

		jsonObject.addProperty("fileName", fileName.replace(FileSystemContentRepository.MIFOSX_BASE_DIR 
				+ File.separator + LEASE + File.separator, "").trim());
		
		jsonObject.addProperty("prospectLoanCalculatorId", prospectLoanCalculatorId);

		return jsonObject.toString();
	}
    
    @GET
    @Path("calculator/export")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public Response testing(@QueryParam("file") final String fileName) {
    	
    	String fileLocation = FileSystemContentRepository.MIFOSX_BASE_DIR + File.separator + LEASE + File.separator + fileName;
    	
        if(null == fileName) {
        	throw new LeaseScreenReportFileNameNotNullException();
        }
        
        File file = new File(fileLocation);
        
        if(!file.exists()) {
        	String fileNameData = fileName.replace("\\\\", "/");
        	file = new File(fileNameData);
        	if(!file.exists()) {
        		throw new LeaseScreenReportFileNotFoundException(fileNameData);
        	}    	
        }
        
        final ResponseBuilder response = Response.ok(file);
        response.header("Content-Disposition", "attachment; filename=" + file.getName().replace(" ", "_"));
        response.header("Content-Type", "application/vnd.ms-excel");
        
        return response.build();
    }
    
    
}