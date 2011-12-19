package org.ccci.idm.rules.test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

import org.ccci.idm.obj.RoleAssignment;
import org.ccci.idm.rules.processes.RuleBasedRoleProvisioningProcess;
import org.ccci.idm.rules.services.RoleManagerServiceMock;
import org.ccci.soa.obj.USEmployment;
import org.junit.Assert;
import org.junit.Test;

public class StaffWebRulesTest
{
    DateFormat df = new SimpleDateFormat("M/d/yy");    
    
    public StaffWebRulesTest()
    {
        super();
    }

    @Test
    public void activeStaff() throws Exception
    {
        RoleManagerServiceMock svc = new RoleManagerServiceMock("staffweb.responsibility.rules@ccci.org");
        RuleBasedRoleProvisioningProcess proc = new RuleBasedRoleProvisioningProcess(svc);
        proc.addDrlRuleset("classpath:StaffWebAccess.drl");
        
        // ==============================================================
        // initial setup
        USEmployment e = new USEmployment();
        e.setTermDate(null);
        e.setPaygroup("USS");
        e.setEmplStatus("A");

        Date now = df.parse("1/1/2000");
        proc.computeAndApplyRolesForUser("nathan.kopp@ccci.org", now, e);
        
        Assert.assertEquals(1, svc.getCurrentRoles().size());
        Assert.assertEquals(1, svc.getAddedRoles().size());
        Assert.assertEquals(0, svc.getRemovedRoles().size());
        
        RoleAssignment r = getRole(svc.getCurrentRoles(), "ccci:itroles:uscore:stellent:roles:StaffOnlyConsumer");
        Assert.assertNotNull(r);
        Assert.assertEquals("staffweb.responsibility.rules@ccci.org", r.getAttestorId().toLowerCase());
        Assert.assertEquals("nathan.kopp@ccci.org", r.getAssigneeId().toLowerCase());
        Assert.assertFalse(r.getExisting());
        Assert.assertNull(r.getExpiration());
    }

    @Test
    public void terminatedWithinGracePeriod() throws Exception
    {
        RoleManagerServiceMock svc = new RoleManagerServiceMock("staffweb.responsibility.rules@ccci.org");
        RuleBasedRoleProvisioningProcess proc = new RuleBasedRoleProvisioningProcess(svc);
        proc.addDrlRuleset("classpath:StaffWebAccess.drl");
        
        // ==============================================================
        // initial setup
        USEmployment e = new USEmployment();
        e.setTermDate(null);
        e.setPaygroup("USS");
        e.setEmplStatus("T");
        e.setTermDate("1/1/2000");

        Date now = df.parse("1/1/2000");
        proc.computeAndApplyRolesForUser("nathan.kopp@ccci.org", now, e);
        
        Assert.assertEquals(1, svc.getCurrentRoles().size());
        Assert.assertEquals(1, svc.getAddedRoles().size());
        Assert.assertEquals(0, svc.getRemovedRoles().size());
        
        RoleAssignment r = getRole(svc.getCurrentRoles(), "ccci:itroles:uscore:stellent:roles:StaffOnlyConsumer");
        Assert.assertEquals("staffweb.responsibility.rules@ccci.org", r.getAttestorId().toLowerCase());
        Assert.assertEquals("nathan.kopp@ccci.org", r.getAssigneeId().toLowerCase());
        Assert.assertFalse(r.getExisting());
        Assert.assertEquals("3/31/00",df.format(r.getExpiration()));
        
        svc.reset(true);
        now = df.parse("3/31/2000");
        proc.computeAndApplyRolesForUser("nathan.kopp@ccci.org", now, e);
        
        Assert.assertEquals(1, svc.getCurrentRoles().size());
        Assert.assertEquals(1, svc.getAddedRoles().size());
        Assert.assertEquals(0, svc.getRemovedRoles().size());
        
        r = getRole(svc.getCurrentRoles(), "ccci:itroles:uscore:stellent:roles:StaffOnlyConsumer");
        Assert.assertEquals("staffweb.responsibility.rules@ccci.org", r.getAttestorId().toLowerCase());
        Assert.assertEquals("nathan.kopp@ccci.org", r.getAssigneeId().toLowerCase());
        Assert.assertFalse(r.getExisting());
        Assert.assertEquals("3/31/00",df.format(r.getExpiration()));
    }

    @Test
    public void terminatedPastGracePeriod() throws Exception
    {
        RoleManagerServiceMock svc = new RoleManagerServiceMock("staffweb.responsibility.rules@ccci.org");
        RuleBasedRoleProvisioningProcess proc = new RuleBasedRoleProvisioningProcess(svc);
        proc.addDrlRuleset("classpath:StaffWebAccess.drl");
        
        // ==============================================================
        // initial setup
        USEmployment e = new USEmployment();
        e.setTermDate(null);
        e.setPaygroup("USS");
        e.setEmplStatus("T");
        e.setTermDate("1/1/2000");

        Date now = df.parse("4/1/2000");
        proc.computeAndApplyRolesForUser("nathan.kopp@ccci.org", now, e);
        
        Assert.assertEquals(0, svc.getCurrentRoles().size());
        Assert.assertEquals(0, svc.getAddedRoles().size());
        Assert.assertEquals(0, svc.getRemovedRoles().size());
        
        svc.reset(true);
        
        now = df.parse("4/2/2000");
        proc.computeAndApplyRolesForUser("nathan.kopp@ccci.org", now, e);
        
        Assert.assertEquals(0, svc.getCurrentRoles().size());
        Assert.assertEquals(0, svc.getAddedRoles().size());
        Assert.assertEquals(0, svc.getRemovedRoles().size());
    }

    private boolean containsRole(Collection<RoleAssignment> roles, RoleAssignment roleAssignment)
    {
        for(RoleAssignment r : roles)
        {
            if(r.equals(roleAssignment)) return true;
        }
        return false;
    }
    
    private RoleAssignment getRole(Collection<RoleAssignment> roles, String id)
    {
        for(RoleAssignment r : roles)
        {
            if(r.getRoleId().equals(id)) return r;
        }
        return null;
    }
}
