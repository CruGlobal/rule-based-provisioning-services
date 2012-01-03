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
		System.out.println("StaffWebRuleProvSvc.setupProperties");
		setupProperties();
        System.out.println("StaffWebRuleProvSvc.setupAuthenticationManager");
        setupAuthenticationManager();
        System.out.println("StaffWebRuleProvSvc.setupRules");
        setupRules();
        // WARNING: we can't call setupStaffServiceClient() in the constructor if this is deployed in the same
        // container as the StaffServiceClient!  If so, it will create a deadlock, since tomcat doesn't start
        // listening on the port until all web apps have loaded, and this function will block until tomcat starts
        // processing requests for the WSDL
        //System.out.println("StaffWebRuleProvSvc.setupStaffServiceClient");
        //setupStaffServiceClient();
        System.out.println("StaffWebRuleProvSvc done");
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
        setupStaffServiceClientIfNecessary();
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

    private synchronized void setupStaffServiceClientIfNecessary() throws MalformedURLException
    {
        if(service!=null) return;
        System.out.println("a");
        String wsdlUrl = properties.getProperty("staffService.wsdlUrl");
        System.out.println("b");
        String namespace = properties.getProperty("staffService.namespace");
        System.out.println("c");
        String serviceName = properties.getProperty("staffService.serviceName");
        URL wsdl = new URL(wsdlUrl);
        System.out.println("d");
        QName serviceQname = new QName(namespace, serviceName);
        System.out.println("e");
        
        System.out.println("1");
        
        StaffServiceService locator = new StaffServiceService(wsdl, serviceQname);
        System.out.println("2");
        service = locator.getStaffServicePort();
        
        System.out.println("3");
        
        serviceServerId = properties.getProperty("staffService.serverId");
        System.out.println("4");
        serviceServerSecret = properties.getProperty("staffService.serverSecret");
        
        System.out.println("5");
    }


}
