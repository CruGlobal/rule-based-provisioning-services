package org.ccci.idm.rules.services;

import com.google.common.collect.ImmutableMap;
import org.ccci.idm.obj.RoleAssignment;
import org.ccci.idm.user.Group;
import org.ccci.idm.user.User;
import org.ccci.idm.user.UserManager;
import org.ccci.idm.user.ldaptive.dao.io.GroupValueTranscoder;
import org.ccci.util.strings.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

public class RoleManagerServiceUserManager implements RoleManagerService
{
    private Logger logger = LoggerFactory.getLogger(getClass());

    private String attestorId;
    private String roleBasePath;

    private UserManager userManager;

    private GroupValueTranscoder groupValueTranscoder;

    private static final Map<String, String> RoleMap = ImmutableMap.of(
            "roles:StaffOnlyConsumer", "cn=StaffOnlyConsumer,ou=Roles"
    );

    public RoleManagerServiceUserManager(String attestorId, String roleBasePath)
    {
        super();

        this.attestorId = attestorId;

        this.roleBasePath = roleBasePath;

        this.userManager = UserManagerService.getUserManager();

        this.groupValueTranscoder = UserManagerService.getGroupValueTranscoder();
    }

    @Override
    public Collection<RoleAssignment> findExistingAssignedRoles(String globalId) throws Exception
    {
        Collection<RoleAssignment> assignments = new ArrayList<RoleAssignment>();

        logger.info("find existing global id " + globalId);

        User user = userManager.findUserByRelayGuid(globalId);

        logger.debug("find existing user " + user);

        if(user == null)
        {
            throw new NoSuchUserException("No such user for global id " + globalId);
        }

        logger.debug("user " + user.getEmail());

        if(user.getGroups() != null)
        {
            logger.debug("user groups size " + user.getGroups().size());

            for(Group group : user.getGroups())
            {
                logger.debug("user group " + group.getName());

                RoleAssignment r = new RoleAssignment();

                r.setAssigneeId(globalId);
                r.setExisting(true);
                r.setRoleId(groupValueTranscoder.encodeStringValue(group));
                r.setExpiration(new Date());

                setAttestor(r);

                logger.debug("role assignment for user " + user.getEmail() + r.toString());

                assignments.add(r);
            }
        }

        return assignments;
    }

    private void setAttestor(RoleAssignment r)
    {
        r.setAttestorId("externalAttestor");

        // set ourselves as attestor if the role is one we manage
        for (Map.Entry<String, String> entry : RoleMap.entrySet())
        {
            if(r.getRoleId().startsWith(entry.getValue()))
            {
                r.setAttestorId(attestorId);
                break;
            }
        }
    }

    @Override
    public void assignRoleToPerson(RoleAssignment r) throws Exception
    {
        logger.debug("role assignment " + r.toString());

        User user = userManager.findUserByRelayGuid(r.getAssigneeId());

        if(user == null)
        {
            throw new NoSuchUserException("No such user for assignee id " + r.getAssigneeId());
        }

        logger.debug("role assignment " + r.toString() + " for user " + user.getEmail());

        Group group = groupValueTranscoder.decodeStringValue(r.getRoleId());

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
        User user = userManager.findUserByRelayGuid(r.getAssigneeId());

        Group group = groupValueTranscoder.decodeStringValue(r.getRoleId());

        logger.info("Removing role from user " + user.getEmail() + " for group " + group.getName());

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
    public String convertRoleNameToFullPath(String roleName)
    {
        return Strings.isEmpty(roleBasePath) ? RoleMap.get(roleName) : RoleMap.get(roleName) + "," + roleBasePath;
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
