package org.ccci.idm.rules.webservice;

import java.util.ArrayList;
import java.util.Collection;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import org.ccci.idm.rules.services.RuleBasedProvisioningService;
import org.ccci.idm.rules.services.RuleBasedProvisioningServiceGrouper;
import org.ccci.soa.obj.USEmployment;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.DecisionTableConfiguration;
import org.drools.builder.DecisionTableInputType;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.definition.KnowledgePackage;
import org.drools.io.Resource;
import org.drools.io.ResourceFactory;
import org.drools.runtime.ObjectFilter;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.rule.FactHandle;
import org.drools.runtime.rule.QueryResults;
import org.drools.runtime.rule.QueryResultsRow;


@WebService()
@Stateless()
public class RuleBasedProvisioning
{
    RuleBasedProvisioningService svc;
    
    public RuleBasedProvisioning()
    {
        super();
        svc = new RuleBasedProvisioningServiceGrouper("siebel.responsibility.rules@ccci.org", "ccci:itroles:uscore:siebel_resp", true);
        svc.addExcelRuleset("classpath:SiebelProvisioningRules.xls", "Sheet1");
        svc.addDrlRuleset("classpath:RemoveAllRoles.drl");
    }

    public RuleBasedProvisioning(RuleBasedProvisioningService svc)
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
