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
import org.ccci.idm.rules.processes.RuleBasedRoleProvisioningProcess;
import org.ccci.idm.rules.services.RoleManagerServiceGrouper;
import org.ccci.idm.util.PropertiesWithFallback;
import org.ccci.soa.obj.USEmployment;

@WebService()
@Stateless()
public class SiebelRuleProvSvc
{
    private AuthenticationManager authenticationManager;
    private Properties properties;
    
	RuleBasedRoleProvisioningProcess ruleBasedResponsibilityProvisioningService;
	RuleBasedRoleProvisioningProcess ruleBasedAccessGroupProvisioningService;

	public SiebelRuleProvSvc()
	{
		super();
		properties = new PropertiesWithFallback(false, "/ora/config/siebelRuleProvSvc.properties","/default.properties");
        authenticationManager = new AuthenticationManagerImpl();
        authenticationManager.addAuthenticationHandler(new PropertyBasedUsernamePasswordAuthHandler(properties, "user."));

		ruleBasedResponsibilityProvisioningService = new RuleBasedRoleProvisioningProcess(new RoleManagerServiceGrouper("siebel.responsibility.rules@ccci.org", "ccci:itroles:uscore:siebel:resp", true));
		ruleBasedResponsibilityProvisioningService.addExcelRuleset("classpath:SiebelResponsibilityProvisioningRules.xls", "Sheet1");
		ruleBasedResponsibilityProvisioningService.addDrlRuleset("classpath:RemoveAllRoles.drl");

		ruleBasedAccessGroupProvisioningService = new RuleBasedRoleProvisioningProcess(new RoleManagerServiceGrouper("siebel.accessgroup.rules@ccci.org", "ccci:itroles:uscore:siebel:access_groups", true));
		ruleBasedAccessGroupProvisioningService.addExcelRuleset("classpath:SiebelAccessGroupProvisioningRules.xls", "Sheet1");
		ruleBasedAccessGroupProvisioningService.addDrlRuleset("classpath:RemoveAllRoles.drl");
	}

	public SiebelRuleProvSvc(RuleBasedRoleProvisioningProcess ruleBasedResponsibilityProvisioningService, RuleBasedRoleProvisioningProcess ruleBasedAccessGroupProvisioningService)
	{
		super();

		this.ruleBasedResponsibilityProvisioningService = ruleBasedResponsibilityProvisioningService;
		this.ruleBasedAccessGroupProvisioningService = ruleBasedAccessGroupProvisioningService;
	}

	@WebMethod(operationName = "provisionSiebelResponsibilityAccessForEmployee")
	public String provisionSiebelResponsibilityAccessForEmployee(@WebParam(name = "serverId") String serverId, @WebParam(name = "serverSecret") String serverSecret, @WebParam(name = "globalId") String globalId, @WebParam(name = "employment") USEmployment employment) throws Exception
	{
	    authenticationManager.authenticate(new UsernamePasswordCredentials(serverId, serverSecret));
	    
		ruleBasedResponsibilityProvisioningService.computeAndApplyRolesForUser(globalId, new Date(), employment);

		return null;
	}

	@WebMethod(operationName = "provisionSiebelAccessGroupAccessForEmployee")
	public String provisionSiebelAccessGroupAccessForEmployee(@WebParam(name = "serverId") String serverId, @WebParam(name = "serverSecret") String serverSecret, @WebParam(name = "globalId") String globalId, @WebParam(name = "employment") USEmployment employment) throws Exception
	{
	    authenticationManager.authenticate(new UsernamePasswordCredentials(serverId, serverSecret));
	    
		ruleBasedAccessGroupProvisioningService.computeAndApplyRolesForUser(globalId, new Date(), employment);

		return null;
	}
}
