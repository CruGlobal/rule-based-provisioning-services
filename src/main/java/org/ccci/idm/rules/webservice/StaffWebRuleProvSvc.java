package org.ccci.idm.rules.webservice;

import java.util.Date;
import java.util.Properties;

import javax.ejb.Stateless;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import org.ccci.idm.authentication.credentials.impl.UsernamePasswordCredentials;
import org.ccci.idm.authentication.handler.impl.PropertyBasedUsernamePasswordAuthHandler;
import org.ccci.idm.authentication.manager.AuthenticationManager;
import org.ccci.idm.authentication.manager.impl.AuthenticationManagerImpl;
import org.ccci.idm.rules.processes.RuleBasedProvisioningProcess;
import org.ccci.idm.rules.processes.RuleBasedProvisioningProcessGrouper;
import org.ccci.idm.util.PropertiesWithFallback;
import org.ccci.soa.obj.USEmployment;

@WebService()
@Stateless()
public class StaffWebRuleProvSvc
{
    private AuthenticationManager authenticationManager;
    private Properties properties;
    
	RuleBasedProvisioningProcess ruleBasedStaffWebProvisioningService;

	public StaffWebRuleProvSvc()
	{
		super();
		properties = new PropertiesWithFallback(false, "/ora/config/staffWebRuleProvSvc.properties","/default.properties");
        authenticationManager = new AuthenticationManagerImpl();
        authenticationManager.addAuthenticationHandler(new PropertyBasedUsernamePasswordAuthHandler(properties, "user."));

        ruleBasedStaffWebProvisioningService = new RuleBasedProvisioningProcessGrouper("staffweb.responsibility.rules@ccci.org", "", false);
        ruleBasedStaffWebProvisioningService.addDrlRuleset("classpath:StaffWebAccess.drl");
	}

	public StaffWebRuleProvSvc(RuleBasedProvisioningProcess ruleBasedStaffWebProvisioningService)
	{
		super();

		this.ruleBasedStaffWebProvisioningService = ruleBasedStaffWebProvisioningService;
	}

	@WebMethod(operationName = "provisionSiebelResponsibilityAccessForEmployee")
	public String provisionStaffWebConsumerForEmployee(@WebParam(name = "serverId") String serverId, @WebParam(name = "serverSecret") String serverSecret, @WebParam(name = "globalId") String globalId, @WebParam(name = "employment") USEmployment employment) throws Exception
	{
	    authenticationManager.authenticate(new UsernamePasswordCredentials(serverId, serverSecret));
	    
	    ruleBasedStaffWebProvisioningService.computeAndApplyRolesForEmployee(globalId, new Date(), employment);

		return null;
	}
}
