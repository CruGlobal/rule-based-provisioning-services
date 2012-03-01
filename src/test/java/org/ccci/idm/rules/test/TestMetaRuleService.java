package org.ccci.idm.rules.test;

import java.util.Properties;

import junit.framework.Assert;

import org.ccci.idm.dao.IdentityDAO;
import org.ccci.idm.rules.services.MetaRuleService;
import org.ccci.idm.rules.services.factprovider.EmployeeInfoProvider;
import org.ccci.idm.rules.services.rolemanager.RoleManagerFactoryMock;
import org.junit.Test;

public class TestMetaRuleService
{
    @Test
    public void firstTest() throws Exception
    {
        Properties props = new Properties();
        props.setProperty("rulesets", "stellent");
        props.setProperty("stellent.attestationUser", "stellent.rules@ccci.org");
        props.setProperty("stellent.grouperBase", "ccci:itroles:uscore:stellent");
        props.setProperty("stellent.rulefiles", "classpath:StaffWebAccess.drl");
        props.setProperty("stellent.facts", "EmployeeInfo");
        
        RoleManagerFactoryMock factory = new RoleManagerFactoryMock();
        
        MetaRuleService service = new MetaRuleService(props, factory);
        service.setupRuleServices();
        service.addFactProvider(new EmployeeInfoProvider(new MockStaffService(), props));
        
        IdentityDAO identityDao = new MockIdentityDAO();
        try
        {
            service.runRules(identityDao, "myssoguid");
            Assert.assertEquals(1, factory.getAddedRoles().size());
        }
        finally
        {
            identityDao.close();
        }
    }
}
