package org.ccci.idm.rules.test;

import java.util.Date;

import org.ccci.idm.rules.processes.RuleBasedRoleProvisioningProcess;
import org.ccci.idm.rules.services.RoleManagerServiceGrouper;
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
      RuleBasedRoleProvisioningProcess svc = new RuleBasedRoleProvisioningProcess(new RoleManagerServiceGrouper("siebel.responsibility.rules@ccci.org", "ccci:itroles:uscore:siebel:resp", true));
      svc.addExcelRuleset("SiebelResponsibilityProvisioningRules.xls", "Sheet1");
      USEmployment e = new USEmployment();
      
      e.setCompany("CCC");
      e.setMinistryCode("HQ");
      e.setSubministryCode("WCS");
      e.setDeptCode("ADMT");
      e.setStatusCode("HCF");
      e.setEmplStatus("A");
      e.setJobCode("CT2");
      
      svc.computeAndApplyRolesForUser("nathan.kopp@ccci.org", new Date(),e);
    }
}
