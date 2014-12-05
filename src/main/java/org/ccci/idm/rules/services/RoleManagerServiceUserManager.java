package org.ccci.idm.rules.services;

import org.ccci.idm.obj.RoleAssignment;
import org.ccci.idm.user.Group;
import org.ccci.idm.user.User;
import org.ccci.idm.user.UserManager;
import org.ccci.idm.user.ldaptive.dao.mapper.GroupDnResolver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

public class RoleManagerServiceUserManager implements RoleManagerService
{
    private String attestorId;

    private UserManager userManager;

    private GroupDnResolver groupDnResolver;

    public RoleManagerServiceUserManager(String attestorId)
    {
        super();

        this.attestorId = attestorId;

        this.userManager = UserManagerService.getUserManager();

        this.groupDnResolver = UserManagerService.getGroupDnResolver();
    }

    @Override
    public Collection<RoleAssignment> findExistingAssignedRoles(String globalId) throws Exception
    {
        Collection<RoleAssignment> assignments = new ArrayList<RoleAssignment>();

        User user = userManager.findUserByRelayGuid(globalId);

        if(user == null)
        {
            throw new NoSuchUserException("No such user for global id " + globalId);
        }

        if(user.getGroups() != null)
        {
            for(Group group : user.getGroups())
            {
                RoleAssignment r = new RoleAssignment();

                r.setAssigneeId(globalId);
                r.setAttestorId(attestorId);
                r.setExisting(true);
                r.setRoleId(groupDnResolver.resolve(group));
                r.setExpiration(new Date());

                assignments.add(r);
            }
        }

        return assignments;
    }

    @Override
    public void assignRoleToPerson(RoleAssignment r) throws Exception
    {
        User user = userManager.findUserByGuid(r.getAssigneeId());

        if(user == null)
        {
            throw new NoSuchUserException("No such user for assignee id " + r.getAssigneeId());
        }

        Group group = groupDnResolver.resolve(r.getRoleId());

        if(group == null)
        {
            throw new NoSuchGroupException("No such group for assignee id " + r.getAssigneeId() + " and role id "
                    + r.getRoleId());
        }

        userManager.addToGroup(user, group);
    }

    @Override
    public void removeRoleFromPerson(RoleAssignment r) throws Exception
    {
        User user = userManager.findUserByGuid(r.getAssigneeId());

        Group group = groupDnResolver.resolve(r.getRoleId());

        userManager.removeFromGroup(user, group);
    }

    @Override
    public String convertRoleNameToId(String string)
    {
        return string;
    }

    @Override
    public void updateRoleExpiration(RoleAssignment r) throws Exception
    {
        // not supported
    }

    @Override
    public String convertRoleNameToFullPath(String name)
    {
        return name;
    }

    @Override
    public String getAttestorId()
    {
        return attestorId;
    }

    public class NoSuchUserException extends Exception
    {
        public NoSuchUserException(String message)
        {
            super(message);
        }
    }

    public class NoSuchGroupException extends Exception
    {
        public NoSuchGroupException(String message)
        {
            super(message);
        }
    }
}
