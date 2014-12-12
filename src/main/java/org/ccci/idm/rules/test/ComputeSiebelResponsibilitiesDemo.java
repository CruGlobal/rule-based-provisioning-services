package org.ccci.idm.rules.test;

import java.util.Date;

import org.ccci.idm.rules.services.RoleManagerServiceUserManager;
import org.ccci.idm.rules.services.RuleBasedRoleProvisioningService;
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
      RuleBasedRoleProvisioningService svc = new RuleBasedRoleProvisioningService(new RoleManagerServiceUserManager("siebel.responsibility.rules@ccci.org", "ccci:itroles:uscore:siebel:resp"));
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
