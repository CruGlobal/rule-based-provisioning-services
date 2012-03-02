package org.ccci.idm.rules.test;

import java.util.Properties;

import junit.framework.Assert;

import org.ccci.idm.dao.IdentityDAO;
import org.ccci.idm.rules.services.MetaRuleService;
import org.ccci.idm.rules.services.RuleFilter;
import org.ccci.idm.rules.services.factprovider.EmployeeInfoProvider;
import org.ccci.idm.rules.services.rolemanager.RoleManagerFactoryMock;
import org.junit.Test;

public class TestMetaRuleService
{
    @Test
    public void firstTest() throws Exception
    {
        Properties props = new Properties();
        props.setProperty("rulesets", "stellent, siebel-resp, siebel-access");
        props.setProperty("stellent.attestationUser", "stellent.rules@ccci.org");
        props.setProperty("stellent.grouperBase", "ccci:itroles:uscore:stellent");
        props.setProperty("stellent.rulefiles", "classpath:StaffWebAccess.drl, classpath:StellentRules.xls");
        props.setProperty("stellent.classpath_StellentRules.xls.sheets", "Sheet1");
        props.setProperty("stellent.facts", "EmployeeInfo, IdentityUser");
        
        props.setProperty("siebel-resp.attestationUser", "siebel.rules@ccci.org");
        props.setProperty("siebel-resp.grouperBase", "ccci:itroles:uscore:siebel:resp");
        props.setProperty("siebel-resp.rulefiles", "classpath:SiebelResponsibilityProvisioningRules.xls, classpath:RemoveAllRoles.drl");
        props.setProperty("siebel-resp.classpath_SiebelResponsibilityProvisioningRules.xls.sheets", "Sheet1");
        props.setProperty("siebel-resp.facts", "EmployeeInfo, IdentityUser");
        
        props.setProperty("siebel-access.attestationUser", "siebel.rules@ccci.org");
        props.setProperty("siebel-access.grouperBase", "ccci:itroles:uscore:siebel:resp");
        props.setProperty("siebel-access.rulefiles", "classpath:SiebelAccessGroupProvisioningRules.xls, classpath:RemoveAllRoles.drl");
        props.setProperty("siebel-access.classpath_SiebelAccessGroupProvisioningRules.xls.sheets", "Sheet1");
        props.setProperty("siebel-access.facts", "EmployeeInfo, IdentityUser");

        
        RoleManagerFactoryMock factory = new RoleManagerFactoryMock();
        
        MetaRuleService service = new MetaRuleService(props, factory);
        service.setupRuleServices();
        service.addFactProvider(new EmployeeInfoProvider(new MockStaffService(), props));
        
        IdentityDAO identityDao = new MockIdentityDAO();
        try
        {
            RuleFilter ruleFilter =new RuleFilter();
            ruleFilter.getChangedFacts().add("EmployeeInfo");
            service.runRules(identityDao, "myssoguid", ruleFilter);
            Assert.assertEquals(1, factory.getAddedRoles().size());
        }
        finally
        {
            identityDao.close();
        }
    }
}
