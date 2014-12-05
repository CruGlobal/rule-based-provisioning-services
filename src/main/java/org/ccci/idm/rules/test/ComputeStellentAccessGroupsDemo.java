package org.ccci.idm.rules.test;

import org.ccci.idm.rules.services.RoleManagerService;
import org.ccci.idm.rules.services.RoleManagerServiceUserManager;
import org.ccci.idm.rules.services.RuleBasedRoleProvisioningService;
import org.ccci.soa.obj.USEmployment;
import org.junit.Test;

import java.util.Date;

public class ComputeStellentAccessGroupsDemo
{
    public ComputeStellentAccessGroupsDemo()
    {
        super();
    }

    @Test
    public void basicDemo() throws Exception
    {
        RoleManagerService roleManagerService =
                new RoleManagerServiceUserManager("stellent.accessgroup.rules@ccci.org");

        RuleBasedRoleProvisioningService svc =
                new RuleBasedRoleProvisioningService(roleManagerService);

        svc.addExcelRuleset("classpath:StellentRules.xls", "Sheet1");

        USEmployment e = new USEmployment();

        e.setCompany("CCC");
        e.setMinistryCode("HQ");
        e.setSubministryCode("WCS");
        e.setDeptCode("ADMT");
        e.setStatusCode("HCF");
        e.setEmplStatus("A");
        e.setJobCode("CT2");

        String ssoguid = "479A6FA2-A217-2111-0CA8-B4860716B964";

        svc.computeAndApplyRolesForUser(ssoguid, new Date(), e);
    }
}
