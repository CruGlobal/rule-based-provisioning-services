package org.ccci.idm.rules.test;

import org.ccci.idm.rules.services.RuleBasedProvisioningService;
import org.ccci.idm.rules.services.RuleBasedProvisioningServiceGrouper;
import org.ccci.soa.obj.USEmployment;
import org.junit.Test;

public class ComputeSiebelAccessGroupsDemo
{
    public ComputeSiebelAccessGroupsDemo()
    {
        super();
    }
    
    @Test
    public void basicDemo() throws Exception
    {
      RuleBasedProvisioningService svc = new RuleBasedProvisioningServiceGrouper("siebel.accessgroup.rules@ccci.org", "ccci:itroles:uscore:siebel:access_groups", true);
      svc.addExcelRuleset("SiebelAccessGroupProvisioningRules.xls", "Sheet1");
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
