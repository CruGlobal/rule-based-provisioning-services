package org.ccci.idm.rules.webservice;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.Properties;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.xml.namespace.QName;

import org.ccci.idm.authentication.credentials.impl.UsernamePasswordCredentials;
import org.ccci.idm.authentication.handler.impl.PropertyBasedUsernamePasswordAuthHandler;
import org.ccci.idm.authentication.manager.AuthenticationManager;
import org.ccci.idm.authentication.manager.impl.AuthenticationManagerImpl;
import org.ccci.idm.rules.processes.RuleBasedRoleProvisioningProcess;
import org.ccci.idm.rules.services.RoleManagerServiceGrouper;
import org.ccci.soa.pshr.client.StaffService;
import org.ccci.soa.pshr.client.StaffServiceService;
import org.ccci.soa.pshr.client.UsStaffMember;
import org.ccci.util.properties.CcciProperties.PropertyEncryptionSetup;
import org.ccci.util.properties.PropertiesWithFallback;

@WebService()
public class SiebelRuleProvSvc
{
    private AuthenticationManager authenticationManager;
    private Properties properties;
    
    private RuleBasedRoleProvisioningProcess ruleBasedResponsibilityProvisioningService;
	private RuleBasedRoleProvisioningProcess ruleBasedAccessGroupProvisioningService;
	private StaffService service;
    private String serviceServerId;
    private String serviceServerSecret;

	public SiebelRuleProvSvc() throws Exception
	{
		super();
		System.out.println("SiebelRuleProvSvc.setupProperties");
		setupProperties();
		System.out.println("SiebelRuleProvSvc.setupAuthenticationManager");
		setupAuthenticationManager();
		System.out.println("SiebelRuleProvSvc.setupRules");
		setupRules();
		System.out.println("SiebelRuleProvSvc.setupStaffServiceClient");
		setupStaffServiceClient();
		System.out.println("SiebelRuleProvSvc done");
	}

	public SiebelRuleProvSvc(RuleBasedRoleProvisioningProcess ruleBasedResponsibilityProvisioningService, RuleBasedRoleProvisioningProcess ruleBasedAccessGroupProvisioningService)
	{
		super();

		this.ruleBasedResponsibilityProvisioningService = ruleBasedResponsibilityProvisioningService;
		this.ruleBasedAccessGroupProvisioningService = ruleBasedAccessGroupProvisioningService;
	}

	@WebMethod(operationName = "provisionSiebelResponsibilityAccessForEmployee")
	public String provisionSiebelResponsibilityAccessForEmployee(@WebParam(name = "serverId") String serverId, @WebParam(name = "serverSecret") String serverSecret, @WebParam(name = "ssoGuid") String ssoGuid, @WebParam(name = "emplid") String emplid) throws Exception
	{
	    authenticationManager.authenticate(new UsernamePasswordCredentials(serverId, serverSecret));
	    UsStaffMember staff = service.getStaff(serviceServerId, serviceServerSecret, emplid);
	    if(staff==null) throw new RuntimeException("Emplid is not valid.");
		ruleBasedResponsibilityProvisioningService.computeAndApplyRolesForUser(ssoGuid, new Date(), staff.getEmploymentInfo());
		return null;
	}

	@WebMethod(operationName = "provisionSiebelAccessGroupAccessForEmployee")
	public String provisionSiebelAccessGroupAccessForEmployee(@WebParam(name = "serverId") String serverId, @WebParam(name = "serverSecret") String serverSecret, @WebParam(name = "ssoGuid") String ssoGuid, @WebParam(name = "emplid") String emplid) throws Exception
	{
	    authenticationManager.authenticate(new UsernamePasswordCredentials(serverId, serverSecret));
	    UsStaffMember staff = service.getStaff(serviceServerId, serviceServerSecret, emplid);
	    if(staff==null) throw new RuntimeException("Emplid is not valid.");
		ruleBasedAccessGroupProvisioningService.computeAndApplyRolesForUser(ssoGuid, new Date(), staff.getEmploymentInfo());
		return null;
	}
	
	
	private void setupProperties()
    {
        PropertyEncryptionSetup encryptionSetup = new PropertyEncryptionSetup("lco97gf5t7D%Y4bh89%U34IF&l*()$Hg6wRD^j4");
        properties = new PropertiesWithFallback(encryptionSetup, false, "/ora/config/siebelRuleProvSvc.properties","/default.properties");
    }

    private void setupRules()
    {
        ruleBasedResponsibilityProvisioningService = new RuleBasedRoleProvisioningProcess(new RoleManagerServiceGrouper("siebel.responsibility.rules@ccci.org", "ccci:itroles:uscore:siebel:resp", true));
        ruleBasedResponsibilityProvisioningService.addExcelRuleset("classpath:SiebelResponsibilityProvisioningRules.xls", "Sheet1");
        ruleBasedResponsibilityProvisioningService.addDrlRuleset("classpath:RemoveAllRoles.drl");

        ruleBasedAccessGroupProvisioningService = new RuleBasedRoleProvisioningProcess(new RoleManagerServiceGrouper("siebel.accessgroup.rules@ccci.org", "ccci:itroles:uscore:siebel:access_groups", true));
        ruleBasedAccessGroupProvisioningService.addExcelRuleset("classpath:SiebelAccessGroupProvisioningRules.xls", "Sheet1");
        ruleBasedAccessGroupProvisioningService.addDrlRuleset("classpath:RemoveAllRoles.drl");
    }

    private void setupAuthenticationManager()
    {
        authenticationManager = new AuthenticationManagerImpl();
        authenticationManager.addAuthenticationHandler(new PropertyBasedUsernamePasswordAuthHandler(properties, "user."));
    }

    private void setupStaffServiceClient() throws MalformedURLException
    {
        String wsdlUrl = properties.getProperty("staffService.wsdlUrl");
        String namespace = properties.getProperty("staffService.namespace");
        String serviceName = properties.getProperty("staffService.serviceName");
        URL wsdl = new URL(wsdlUrl);
        QName serviceQname = new QName(namespace, serviceName);
        
        StaffServiceService locator = new StaffServiceService(wsdl, serviceQname);
        service = locator.getStaffServicePort();
        
        serviceServerId = properties.getProperty("staffService.serverId");
        serviceServerSecret = properties.getProperty("staffService.serverSecret");
    }
}
