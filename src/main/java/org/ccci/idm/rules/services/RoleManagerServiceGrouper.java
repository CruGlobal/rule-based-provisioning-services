package org.ccci.idm.rules.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import org.ccci.idm.grouper.dao.GrouperDao;
import org.ccci.idm.grouper.dao.GrouperDaoImpl;
import org.ccci.idm.grouper.obj.GrouperMembership;
import org.ccci.idm.obj.RoleAssignment;
import org.ccci.util.NkUtil;

public class RoleManagerServiceGrouper implements RoleManagerService
{
    private String attestorId;
    private String roleBasePath;
    protected boolean convertRoleNames = true;
    
    public RoleManagerServiceGrouper(String attestorId, String roleBasePath, boolean convertRoleNames)
    {
        super();
        this.attestorId = attestorId;
        this.roleBasePath = roleBasePath;
        this.convertRoleNames = convertRoleNames;
    }

    @Override
    public Collection<RoleAssignment> findExistingAssignedRoles(String globalId) throws Exception
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
            r.setExpiration(m.getExpiration());
            assignments.add(r);
        }
        return assignments;
    }

    @Override
    public void assignRoleToPerson(RoleAssignment r) throws Exception
    {
        GrouperDao dao = new GrouperDaoImpl(r.getAttestorId());
        try
        {
            GrouperMembership existing = dao.getMembership(r.getAssigneeId(), r.getRoleId());
            if(existing!=null)
            {
                dao.addMember(r.getAssigneeId(), r.getRoleId());
                dao.setExpiration(r.getAssigneeId(), r.getRoleId(), r.getExpiration());
            }
        }
        finally
        {
            dao.close();
        }
    }

    @Override
    public void removeRoleFromPerson(RoleAssignment r) throws Exception
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
    public String convertRoleNameToId(String string)
    {
        return string.replace(" ", "_").toLowerCase();
    }

    @Override
    public void updateRoleExpiration(RoleAssignment r) throws Exception
    {
        GrouperDao dao = new GrouperDaoImpl(r.getAttestorId());
        try
        {
            dao.setExpiration(r.getAssigneeId(), r.getRoleId(), r.getExpiration());
        }
        finally
        {
            dao.close();
        }
    }

    @Override
    public String convertRoleNameToFullPath(String name)
    {
        if(NkUtil.isBlank(roleBasePath)) return convertRoleNames?convertRoleNameToId(name):name;
        else return roleBasePath+":"+(convertRoleNames?convertRoleNameToId(name):name);
    }

    public String getAttestorId()
    {
        return attestorId;
    }
}
