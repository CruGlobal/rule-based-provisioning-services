package org.ccci.idm.rules.test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

import org.ccci.idm.obj.IdentityUser;
import org.ccci.idm.obj.RoleAssignment;
import org.ccci.idm.rules.obj.EmployeeInfo;
import org.ccci.idm.rules.services.RoleManagerServiceMock;
import org.ccci.idm.rules.services.RuleBasedRoleProvisioningService;
import org.ccci.soa.pshr.client.UsEmployeeInfo;
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
        RoleManagerServiceMock svc = new RoleManagerServiceMock("stellent.rules@ccci.org");
        RuleBasedRoleProvisioningService proc = new RuleBasedRoleProvisioningService(svc);
        proc.addDrlRuleset("classpath:StaffWebAccess.drl");
        
        // ==============================================================
        // initial setup
        EmployeeInfo e = new EmployeeInfo(new UsEmployeeInfo());
        e.setTermDate(null);
        e.setPaygroup("USS");
        e.setEmplStatus("A");

        Date now = df.parse("1/1/2000");
        proc.computeAndApplyRolesForUser("nathan.kopp@ccci.org", now, e);
        
        Assert.assertEquals(1, svc.getCurrentRoles().size());
        Assert.assertEquals(1, svc.getAddedRoles().size());
        Assert.assertEquals(0, svc.getRemovedRoles().size());
        
        RoleAssignment r = getRole(svc.getCurrentRoles(), "roles:StaffOnlyConsumer");
        Assert.assertNotNull(r);
        Assert.assertEquals("stellent.rules@ccci.org", r.getAttestorId().toLowerCase());
        Assert.assertEquals("nathan.kopp@ccci.org", r.getAssigneeId().toLowerCase());
        Assert.assertFalse(r.getExisting());
        Assert.assertNull(r.getExpiration());
    }

    @Test
    public void paidLeaveStaff() throws Exception
    {
        RoleManagerServiceMock svc = new RoleManagerServiceMock("stellent.rules@ccci.org");
        RuleBasedRoleProvisioningService proc = new RuleBasedRoleProvisioningService(svc);
        proc.addDrlRuleset("classpath:StaffWebAccess.drl");
        
        // ==============================================================
        // initial setup
        EmployeeInfo e = new EmployeeInfo(new UsEmployeeInfo());
        e.setTermDate(null);
        e.setPaygroup("USS");
        e.setEmplStatus("P");

        Date now = df.parse("1/1/2000");
        proc.computeAndApplyRolesForUser("nathan.kopp@ccci.org", now, e);
        
        Assert.assertEquals(1, svc.getCurrentRoles().size());
        Assert.assertEquals(1, svc.getAddedRoles().size());
        Assert.assertEquals(0, svc.getRemovedRoles().size());
        
        RoleAssignment r = getRole(svc.getCurrentRoles(), "roles:StaffOnlyConsumer");
        Assert.assertNotNull(r);
        Assert.assertEquals("stellent.rules@ccci.org", r.getAttestorId().toLowerCase());
        Assert.assertEquals("nathan.kopp@ccci.org", r.getAssigneeId().toLowerCase());
        Assert.assertFalse(r.getExisting());
        Assert.assertNull(r.getExpiration());
    }

    @Test
    public void unpaidLeaveStaff() throws Exception
    {
        RoleManagerServiceMock svc = new RoleManagerServiceMock("stellent.rules@ccci.org");
        RuleBasedRoleProvisioningService proc = new RuleBasedRoleProvisioningService(svc);
        proc.addDrlRuleset("classpath:StaffWebAccess.drl");
        
        // ==============================================================
        // initial setup
        EmployeeInfo e = new EmployeeInfo(new UsEmployeeInfo());
        e.setTermDate(null);
        e.setPaygroup("USS");
        e.setEmplStatus("L");

        Date now = df.parse("1/1/2000");
        proc.computeAndApplyRolesForUser("nathan.kopp@ccci.org", now, e);
        
        Assert.assertEquals(1, svc.getCurrentRoles().size());
        Assert.assertEquals(1, svc.getAddedRoles().size());
        Assert.assertEquals(0, svc.getRemovedRoles().size());
        
        RoleAssignment r = getRole(svc.getCurrentRoles(), "roles:StaffOnlyConsumer");
        Assert.assertNotNull(r);
        Assert.assertEquals("stellent.rules@ccci.org", r.getAttestorId().toLowerCase());
        Assert.assertEquals("nathan.kopp@ccci.org", r.getAssigneeId().toLowerCase());
        Assert.assertFalse(r.getExisting());
        Assert.assertNull(r.getExpiration());
    }

    @Test
    public void terminatedWithinGracePeriod() throws Exception
    {
        RoleManagerServiceMock svc = new RoleManagerServiceMock("stellent.rules@ccci.org");
        RuleBasedRoleProvisioningService proc = new RuleBasedRoleProvisioningService(svc);
        proc.addDrlRuleset("classpath:StaffWebAccess.drl");
        
        // ==============================================================
        // initial setup
        EmployeeInfo e = new EmployeeInfo(new UsEmployeeInfo());
        e.setTermDate(null);
        e.setPaygroup("USS");
        e.setEmplStatus("T");
        e.setTermDate("1/1/2000");

        Date now = df.parse("1/1/2000");
        proc.computeAndApplyRolesForUser("nathan.kopp@ccci.org", now, e);
        
        Assert.assertEquals(1, svc.getCurrentRoles().size());
        Assert.assertEquals(1, svc.getAddedRoles().size());
        Assert.assertEquals(0, svc.getRemovedRoles().size());
        
        RoleAssignment r = getRole(svc.getCurrentRoles(), "roles:StaffOnlyConsumer");
        Assert.assertEquals("stellent.rules@ccci.org", r.getAttestorId().toLowerCase());
        Assert.assertEquals("nathan.kopp@ccci.org", r.getAssigneeId().toLowerCase());
        Assert.assertFalse(r.getExisting());
        Assert.assertEquals("3/31/00",df.format(r.getExpiration()));
        
        svc.reset(true);
        now = df.parse("3/31/2000");
        proc.computeAndApplyRolesForUser("nathan.kopp@ccci.org", now, e);
        
        Assert.assertEquals(1, svc.getCurrentRoles().size());
        Assert.assertEquals(1, svc.getAddedRoles().size());
        Assert.assertEquals(0, svc.getRemovedRoles().size());
        
        r = getRole(svc.getCurrentRoles(), "roles:StaffOnlyConsumer");
        Assert.assertEquals("stellent.rules@ccci.org", r.getAttestorId().toLowerCase());
        Assert.assertEquals("nathan.kopp@ccci.org", r.getAssigneeId().toLowerCase());
        Assert.assertFalse(r.getExisting());
        Assert.assertEquals("3/31/00",df.format(r.getExpiration()));
    }

    @Test
    public void terminatedPastGracePeriod() throws Exception
    {
        RoleManagerServiceMock svc = new RoleManagerServiceMock("stellent.rules@ccci.org");
        RuleBasedRoleProvisioningService proc = new RuleBasedRoleProvisioningService(svc);
        proc.addDrlRuleset("classpath:StaffWebAccess.drl");
        
        // ==============================================================
        // initial setup
        EmployeeInfo e = new EmployeeInfo(new UsEmployeeInfo());
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
    
    @Test
    public void nationalStaff() throws Exception
    {
        RoleManagerServiceMock svc = new RoleManagerServiceMock("stellent.rules@ccci.org");
        RuleBasedRoleProvisioningService proc = new RuleBasedRoleProvisioningService(svc);
        proc.addDrlRuleset("classpath:StaffWebAccess.drl");
        
        // ==============================================================
        // initial setup
        IdentityUser user = new IdentityUser();
        user.getDesignation().setDesignationId("0123456");

        Date now = df.parse("1/1/2000");
        proc.computeAndApplyRolesForUser("nathan.kopp@ccci.org", now, user);
        
        Assert.assertEquals(1, svc.getCurrentRoles().size());
        Assert.assertEquals(1, svc.getAddedRoles().size());
        Assert.assertEquals(0, svc.getRemovedRoles().size());
        
        RoleAssignment r = getRole(svc.getCurrentRoles(), "roles:StaffOnlyConsumer");
        Assert.assertNotNull(r);
        Assert.assertEquals("stellent.rules@ccci.org", r.getAttestorId().toLowerCase());
        Assert.assertEquals("nathan.kopp@ccci.org", r.getAssigneeId().toLowerCase());
        Assert.assertFalse(r.getExisting());
        Assert.assertNull(r.getExpiration());

        // ==============================================================
        // remove the designation

        // reset the service (leave current roles in place)
        svc.getAddedRoles().clear();
        svc.getRemovedRoles().clear();

        user.getDesignation().setDesignationId(".");

        proc.addDrlRuleset("classpath:StaffWebAccess.drl");
        proc.computeAndApplyRolesForUser("nathan.kopp@ccci.org", now, user);
        
        Assert.assertEquals(0, svc.getCurrentRoles().size());
        Assert.assertEquals(0, svc.getAddedRoles().size());
        Assert.assertEquals(1, svc.getRemovedRoles().size());
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
