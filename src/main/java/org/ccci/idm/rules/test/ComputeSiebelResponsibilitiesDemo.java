package org.ccci.idm.rules.test;

import org.ccci.idm.rules.services.RuleBasedResponsibilityProvisioningService;
import org.ccci.idm.rules.services.RuleBasedResponsibilityProvisioningServiceGrouper;
import org.ccci.soa.obj.USEmployment;
import org.junit.Test;

public class ComputeSiebelResponsibilitiesDemo
{
    public ComputeSiebelResponsibilitiesDemo()
    {
        super();
    }
    
    @Test
    public void basicDemo() throws Exception
    {
      RuleBasedResponsibilityProvisioningService svc = new RuleBasedResponsibilityProvisioningServiceGrouper("siebel.responsibility.rules@ccci.org", "ccci:itroles:uscore:siebel_resp", true);
      svc.addExcelRuleset("SiebelResponsibilitiesProvisioningRules.xls", "Sheet1");
      USEmployment e = new USEmployment();
      
      e.setCompany("CCC");
      e.setMinistryCode("HQ");
      e.setSubministryCode("WCS");
      e.setDeptCode("ADMT");
      e.setStatusCode("HCF");
      e.setEmplStatus("A");
      e.setJobCode("CT2");
      
      svc.computeAndApplyRolesForEmployee("nathan.kopp@ccci.org",e);
    }
}
