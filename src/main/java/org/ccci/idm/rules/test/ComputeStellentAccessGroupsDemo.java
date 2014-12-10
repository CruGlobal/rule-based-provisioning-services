package org.ccci.idm.rules.test;

import org.ccci.idm.rules.services.RoleManagerService;
import org.ccci.idm.rules.services.RoleManagerServiceUserManager;
import org.ccci.idm.rules.services.RuleBasedRoleProvisioningService;
import org.ccci.soa.obj.USEmployment;
import org.ccci.util.properties.CcciProperties;
import org.ccci.util.properties.PropertiesWithFallback;
import org.junit.Test;

import java.util.Date;
import java.util.Properties;

public class ComputeStellentAccessGroupsDemo
{
    public ComputeStellentAccessGroupsDemo()
    {
        super();
    }

    private Properties properties;

    @Test
    public void basicDemo() throws Exception
    {
        CcciProperties.PropertyEncryptionSetup encryptionSetup = new CcciProperties.PropertyEncryptionSetup("lco97gf5t7D%Y4bh89%U34IF&l*()$Hg6wRD^j4");
        properties = new PropertiesWithFallback(encryptionSetup, false, "/apps/apps-config/rules.properties", "/ora/apps-config/rules.properties","/rulesDefault.properties");

        RoleManagerService roleManagerService =
                new RoleManagerServiceUserManager(properties.getProperty("stellent.attestationUser"), properties
                        .getProperty("stellent.base"));

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
