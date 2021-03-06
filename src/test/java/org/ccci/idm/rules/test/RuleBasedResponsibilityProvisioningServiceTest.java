package org.ccci.idm.rules.test;

import java.util.Collection;
import java.util.Date;

import org.ccci.idm.obj.RoleAssignment;
import org.ccci.idm.rules.services.RoleManagerServiceMock;
import org.ccci.idm.rules.services.RuleBasedRoleProvisioningService;
import org.ccci.soa.obj.USEmployment;
import org.junit.Assert;
import org.junit.Test;

public class RuleBasedResponsibilityProvisioningServiceTest
{
    private Date now = new Date();
    
    public RuleBasedResponsibilityProvisioningServiceTest()
    {
        super();
    }

    @Test
    public void basicDemo() throws Exception
    {
        RoleManagerServiceMock svc = new RoleManagerServiceMock("siebel.responsibility.rules@ccci.org");
        RuleBasedRoleProvisioningService proc = new RuleBasedRoleProvisioningService(svc);
        proc.addExcelRuleset("classpath:Test1Rules.xls", "Sheet1");
        proc.addDrlRuleset("classpath:RemoveAllRoles.drl");
        
        
        
        // ==============================================================
        // initial setup
        USEmployment e = new USEmployment();
        e.setCompany("CCC");
        e.setMinistryCode("HQ");
        e.setSubministryCode("WCS");
        e.setDeptCode("ADMT");
        e.setStatusCode("HCF");
        e.setEmplStatus("A");
        e.setJobCode("CT2");

        proc.computeAndApplyRolesForUser("nathan.kopp@ccci.org", now, e);
        
        Assert.assertEquals(2, svc.getCurrentRoles().size());
        Assert.assertEquals(2, svc.getAddedRoles().size());
        Assert.assertEquals(0, svc.getRemovedRoles().size());
        
        Assert.assertTrue(containsRole(svc.getCurrentRoles(), new RoleAssignment("ccci:itroles:uscore:siebel:resp:ccci_base_user","nathan.kopp@ccci.org","siebel.responsibility.rules@ccci.org",false)));
        Assert.assertTrue(containsRole(svc.getCurrentRoles(), new RoleAssignment("ccci:itroles:uscore:siebel:resp:ccci_call_center_agent","nathan.kopp@ccci.org","siebel.responsibility.rules@ccci.org",false)));
        
        // ==============================================================
        // change job code: should result in one role removed and one added
        svc.reset(false);
        
        e.setJobCode("CT3");
        
        proc.computeAndApplyRolesForUser("nathan.kopp@ccci.org", now, e);
      
        Assert.assertEquals(2, svc.getCurrentRoles().size());
        Assert.assertEquals(1, svc.getAddedRoles().size());
        Assert.assertEquals(1, svc.getRemovedRoles().size());
        
        Assert.assertTrue(containsRole(svc.getCurrentRoles(), new RoleAssignment("ccci:itroles:uscore:siebel:resp:ccci_base_user","nathan.kopp@ccci.org","siebel.responsibility.rules@ccci.org",false)));
        Assert.assertTrue(containsRole(svc.getCurrentRoles(), new RoleAssignment("ccci:itroles:uscore:siebel:resp:ccci_call_center_manager","nathan.kopp@ccci.org","siebel.responsibility.rules@ccci.org",false)));
        
        // ==============================================================
        // The rules sould NOT remove a role attested by a different user
        svc.reset(false);
        
        svc.getCurrentRoles().add(new RoleAssignment("ccci:itroles:uscore:siebel:resp:another","nathan.kopp@ccci.org","another.user@ccci.org",true));
        
        proc.computeAndApplyRolesForUser("nathan.kopp@ccci.org", now, e);
        
        Assert.assertEquals(3, svc.getCurrentRoles().size());
        Assert.assertEquals(0, svc.getAddedRoles().size());
        Assert.assertEquals(0, svc.getRemovedRoles().size());
      
        Assert.assertTrue(containsRole(svc.getCurrentRoles(), new RoleAssignment("ccci:itroles:uscore:siebel:resp:ccci_base_user","nathan.kopp@ccci.org","siebel.responsibility.rules@ccci.org",false)));
        Assert.assertTrue(containsRole(svc.getCurrentRoles(), new RoleAssignment("ccci:itroles:uscore:siebel:resp:ccci_call_center_manager","nathan.kopp@ccci.org","siebel.responsibility.rules@ccci.org",false)));
        Assert.assertTrue(containsRole(svc.getCurrentRoles(), new RoleAssignment("ccci:itroles:uscore:siebel:resp:another","nathan.kopp@ccci.org","another.user@ccci.org",false)));
    }

    private boolean containsRole(Collection<RoleAssignment> roles, RoleAssignment roleAssignment)
    {
        for(RoleAssignment r : roles)
        {
            if(r.equals(roleAssignment)) return true;
        }
        return false;
    }
}
