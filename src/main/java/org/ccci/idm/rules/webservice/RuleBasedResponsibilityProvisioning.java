package org.ccci.idm.rules.webservice;

import javax.ejb.Stateless;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import org.ccci.idm.rules.services.RuleBasedResponsibilityProvisioningService;
import org.ccci.idm.rules.services.RuleBasedResponsibilityProvisioningServiceGrouper;
import org.ccci.soa.obj.USEmployment;


@WebService()
@Stateless()
public class RuleBasedResponsibilityProvisioning
{
    RuleBasedResponsibilityProvisioningService svc;
    
    public RuleBasedResponsibilityProvisioning()
    {
        super();
        svc = new RuleBasedResponsibilityProvisioningServiceGrouper("siebel.responsibility.rules@ccci.org", "ccci:itroles:uscore:siebel_resp", true);
        svc.addExcelRuleset("classpath:SiebelProvisioningRules.xls", "Sheet1");
        svc.addDrlRuleset("classpath:RemoveAllRoles.drl");
    }

    public RuleBasedResponsibilityProvisioning(RuleBasedResponsibilityProvisioningService svc)
    {
        super();
        this.svc = svc;
    }

    @WebMethod(operationName = "provisionSiebelAccessForEmployee")
    public String provisionSiebelAccessForEmployee(
        @WebParam(name = "serverId") String serverId,
        @WebParam(name = "serverSecret") String serverSecret,
        @WebParam(name = "globalId") String globalId,
        @WebParam(name = "employment") USEmployment employment) throws Exception
    {
        
        svc.computeAndApplyRolesForEmployee(globalId, employment);
        
        return null;
    }


}
