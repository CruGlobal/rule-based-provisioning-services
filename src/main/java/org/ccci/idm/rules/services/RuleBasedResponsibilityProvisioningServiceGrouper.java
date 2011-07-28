package org.ccci.idm.rules.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import org.ccci.idm.grouper.dao.GrouperDao;
import org.ccci.idm.grouper.dao.GrouperDaoImpl;
import org.ccci.idm.grouper.obj.GrouperMembership;
import org.ccci.idm.obj.RoleAssignment;

public class RuleBasedResponsibilityProvisioningServiceGrouper extends RuleBasedResponsibilityProvisioningService
{
    public RuleBasedResponsibilityProvisioningServiceGrouper(String attestorId, String roleBasePath, boolean convertRoleNames)
    {
        super(attestorId, roleBasePath, convertRoleNames);
    }

    @Override
    protected Collection<RoleAssignment> findExistingAssignedRoles(String globalId) throws Exception
    {
        GrouperDao dao = new GrouperDaoImpl(attestorId);
        Set<GrouperMembership> memberships = dao.getMemberships(globalId, roleBasePath);
        Collection<RoleAssignment> assignments = new ArrayList<RoleAssignment>();
        for (GrouperMembership m : memberships)
        {
            RoleAssignment r = new RoleAssignment();
            r.setAssigneeId(m.getMember());
            r.setAttestorId(m.getAttester());
            r.setExisting(true);
            r.setRoleId(m.getGroup());
            assignments.add(r);
        }
        return assignments;
    }

    @Override
    protected void assignRoleToPerson(RoleAssignment r) throws Exception
    {
        GrouperDao dao = new GrouperDaoImpl(r.getAttestorId());
        try
        {
            dao.addMember(r.getAssigneeId(), r.getRoleId());
        }
        finally
        {
            dao.close();
        }
    }

    @Override
    protected void removeRoleFromPerson(RoleAssignment r) throws Exception
    {
        GrouperDao dao = new GrouperDaoImpl(r.getAttestorId());
        try
        {
            dao.deleteMember(r.getAssigneeId(), r.getRoleId());
        }
        finally
        {
            dao.close();
        }
    }

    @Override
    protected String convertRoleNameToId(String string)
    {
        return string.replace(" ", "_").toLowerCase();
    }
}
