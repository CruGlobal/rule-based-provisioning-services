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
import org.ccci.idm.dao.IdentityDAO;
import org.ccci.idm.dao.impl.IdentityDAOLDAPImpl;
import org.ccci.idm.obj.IdentityUser;
import org.ccci.idm.rules.obj.EmployeeInfo;
import org.ccci.idm.rules.services.RoleManagerServiceGrouper;
import org.ccci.idm.rules.services.RuleBasedRoleProvisioningService;
import org.ccci.soa.pshr.client.StaffService;
import org.ccci.soa.pshr.client.StaffServiceService;
import org.ccci.soa.pshr.client.UsStaffMember;
import org.ccci.util.properties.CcciProperties.PropertyEncryptionSetup;
import org.ccci.util.properties.PropertiesWithFallback;

@WebService()
public class StellentRuleProvSvc
{
    private AuthenticationManager authenticationManager;
    private Properties properties;
    
	private RuleBasedRoleProvisioningService stellentRuleProc;
	private StaffService service;
	private String serviceServerId;
	private String serviceServerSecret;
	

	public StellentRuleProvSvc() throws Exception
	{
		super();
		setupProperties();
        setupAuthenticationManager();
        setupRules();
        // WARNING: we can't call setupStaffServiceClient() in the constructor if this is deployed in the same
        // container as the StaffServiceClient!  If so, it will create a deadlock, since tomcat doesn't start
        // listening on the port until all web apps have loaded, and this function will block until tomcat starts
        // processing requests for the WSDL
        //setupStaffServiceClient();
	}

	public StellentRuleProvSvc(RuleBasedRoleProvisioningService stellentRuleProc, StaffService service)
	{
		super();
		this.stellentRuleProc = stellentRuleProc;
		this.service = service;
	}

    @WebMethod(operationName = "provisionStellentAccess")
    public String provisionStellentAccess(@WebParam(name = "serverId") String serverId, @WebParam(name = "serverSecret") String serverSecret, @WebParam(name = "ssoGuid") String ssoGuid) throws Exception
    {
        authenticationManager.authenticate(new UsernamePasswordCredentials(serverId, serverSecret));
        
        setupStaffServiceClientIfNecessary();
        
        IdentityDAO identityDao = new IdentityDAOLDAPImpl(properties.getProperty("ldapUrl"));
        try
        {
            IdentityUser identityUser = new IdentityUser();
            identityUser.getAccount().setSsoguid(ssoGuid);
            identityUser = identityDao.load(identityUser);
            
            String emplid = identityUser.getEmployee()==null?null:identityUser.getEmployee().getEmployeeId();
            UsStaffMember staff = emplid==null?null: service.getStaff(serviceServerId, serviceServerSecret, emplid);
            
            if(staff==null) throw new RuntimeException("Emplid is not valid.");
            stellentRuleProc.computeAndApplyRolesForUser(ssoGuid, new Date(), new EmployeeInfo(staff.getEmploymentInfo()), identityUser);
    
            return "done";
        }
        finally
        {
            identityDao.close();
        }
        
    }
    
    
    private void setupProperties()
    {
        PropertyEncryptionSetup encryptionSetup = new PropertyEncryptionSetup("lco97gf5t7D%Y4bh89%U34IF&l*()$Hg6wRD^j4");
        properties = new PropertiesWithFallback(encryptionSetup, false, "/apps/apps-config/stellentRules.properties", "/ora/apps-config/stellentRules.properties","/stellentRulesDefault.properties");
    }

    private void setupRules()
    {
        stellentRuleProc = new RuleBasedRoleProvisioningService(new RoleManagerServiceGrouper(properties.getProperty("attestationUser"), properties.getProperty("grouperBase"), false));
        stellentRuleProc.addDrlRuleset("classpath:StaffWebAccess.drl");
    }

    private void setupAuthenticationManager()
    {
        authenticationManager = new AuthenticationManagerImpl();
        authenticationManager.addAuthenticationHandler(new PropertyBasedUsernamePasswordAuthHandler(properties, "user."));
    }

    private synchronized void setupStaffServiceClientIfNecessary() throws MalformedURLException
    {
        if(service!=null) return;
        String wsdlUrl = properties.getProperty("pshrService.wsdlUrl");
        String namespace = properties.getProperty("pshrService.namespace");
        String serviceName = properties.getProperty("pshrService.serviceName");
        URL wsdl = new URL(wsdlUrl);
        QName serviceQname = new QName(namespace, serviceName);
        
        StaffServiceService locator = new StaffServiceService(wsdl, serviceQname);
        service = locator.getStaffServicePort();
        
        serviceServerId = properties.getProperty("pshrService.serverId");
        serviceServerSecret = properties.getProperty("pshrService.serverSecret");
    }


}
