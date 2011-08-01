package org.ccci.idm.rules.webservice;

import javax.ejb.Stateless;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import org.ccci.idm.rules.services.RuleBasedProvisioningService;
import org.ccci.idm.rules.services.RuleBasedProvisioningServiceGrouper;
import org.ccci.soa.obj.USEmployment;

@WebService()
@Stateless()
public class RuleBasedProvisioning
{
	RuleBasedProvisioningService ruleBasedResponsibilityProvisioningService;
	RuleBasedProvisioningService ruleBasedAccessGroupProvisioningService;

	public RuleBasedProvisioning()
	{
		super();

		ruleBasedResponsibilityProvisioningService = new RuleBasedProvisioningServiceGrouper("siebel.responsibility.rules@ccci.org", "ccci:itroles:uscore:siebel_resp", true);
		ruleBasedResponsibilityProvisioningService.addExcelRuleset("classpath:SiebelResponsibilityProvisioningRules.xls", "Sheet1");
		ruleBasedResponsibilityProvisioningService.addDrlRuleset("classpath:RemoveAllRoles.drl");

		ruleBasedAccessGroupProvisioningService = new RuleBasedProvisioningServiceGrouper("siebel.accessgroup.rules@ccci.org", "ccci:itroles:uscore:siebel_access_group", true);
		ruleBasedAccessGroupProvisioningService.addExcelRuleset("classpath:SiebelAccessGroupProvisioningRules.xls", "Sheet1");
		ruleBasedAccessGroupProvisioningService.addDrlRuleset("classpath:RemoveAllRoles.drl");
	}

	public RuleBasedProvisioning(RuleBasedProvisioningService ruleBasedResponsibilityProvisioningService, RuleBasedProvisioningService ruleBasedAccessGroupProvisioningService)
	{
		super();

		this.ruleBasedResponsibilityProvisioningService = ruleBasedResponsibilityProvisioningService;
		this.ruleBasedAccessGroupProvisioningService = ruleBasedAccessGroupProvisioningService;
	}

	@WebMethod(operationName = "provisionSiebelResponsibilityAccessForEmployee")
	public String provisionSiebelResponsibilityAccessForEmployee(@WebParam(name = "serverId") String serverId, @WebParam(name = "serverSecret") String serverSecret, @WebParam(name = "globalId") String globalId, @WebParam(name = "employment") USEmployment employment) throws Exception
	{
		ruleBasedResponsibilityProvisioningService.computeAndApplyRolesForEmployee(globalId, employment);

		return null;
	}

	@WebMethod(operationName = "provisionSiebelAccessGroupAccessForEmployee")
	public String provisionSiebelAccessGroupAccessForEmployee(@WebParam(name = "serverId") String serverId, @WebParam(name = "serverSecret") String serverSecret, @WebParam(name = "globalId") String globalId, @WebParam(name = "employment") USEmployment employment) throws Exception
	{
		ruleBasedAccessGroupProvisioningService.computeAndApplyRolesForEmployee(globalId, employment);

		return null;
	}
}
