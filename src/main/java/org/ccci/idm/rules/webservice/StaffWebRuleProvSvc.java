package org.ccci.idm.rules.webservice;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.Properties;

import javax.ejb.Stateless;
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
@Stateless()
public class StaffWebRuleProvSvc
{
    private AuthenticationManager authenticationManager;
    private Properties properties;
    
	private RuleBasedRoleProvisioningProcess ruleBasedStaffWebProvisioningService;
	private StaffService service;
	private String serviceServerId;
	private String serviceServerSecret;
	

	public StaffWebRuleProvSvc() throws Exception
	{
		super();
		setupProperties();
        setupAuthenticationManager();
        setupRules();
        setupStaffServiceClient();
	}

	public StaffWebRuleProvSvc(RuleBasedRoleProvisioningProcess ruleBasedStaffWebProvisioningService, StaffService service)
	{
		super();
		this.ruleBasedStaffWebProvisioningService = ruleBasedStaffWebProvisioningService;
		this.service = service;
	}

    @WebMethod(operationName = "provisionSiebelResponsibilityAccessForEmployee")
    public String provisionStaffWebConsumerForEmployee(@WebParam(name = "serverId") String serverId, @WebParam(name = "serverSecret") String serverSecret, @WebParam(name = "ssoGuid") String ssoGuid, @WebParam(name = "emplid") String emplid) throws Exception
    {
        authenticationManager.authenticate(new UsernamePasswordCredentials(serverId, serverSecret));
        UsStaffMember staff = service.getStaff(serviceServerId, serviceServerSecret, emplid);
        if(staff==null) throw new RuntimeException("Emplid is not valid.");
        ruleBasedStaffWebProvisioningService.computeAndApplyRolesForUser(ssoGuid, new Date(), staff.getEmploymentInfo());
        return null;
    }
    
    
    
    
    
    private void setupProperties()
    {
        PropertyEncryptionSetup encryptionSetup = new PropertyEncryptionSetup("lco97gf5t7D%Y4bh89%U34IF&l*()$Hg6wRD^j4");
        properties = new PropertiesWithFallback(encryptionSetup, false, "/ora/config/staffWebRuleProvSvc.properties","/default.properties");
    }

    private void setupRules()
    {
        ruleBasedStaffWebProvisioningService = new RuleBasedRoleProvisioningProcess(new RoleManagerServiceGrouper("staffweb.responsibility.rules@ccci.org", "", false));
        ruleBasedStaffWebProvisioningService.addDrlRuleset("classpath:StaffWebAccess.drl");
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
