package org.ccci.idm.rules.services.rolemanager;

import java.util.ArrayList;
import java.util.Collection;

import org.ccci.idm.obj.RoleAssignment;
import org.ccci.idm.rules.services.RoleManagerFactory;
import org.ccci.idm.rules.services.RoleManagerService;
import org.ccci.idm.rules.services.RoleManagerServiceMock;

public class RoleManagerFactoryMock implements RoleManagerFactory
{
    Collection<RoleAssignment> addedRoles = new ArrayList<RoleAssignment>();
    Collection<RoleAssignment> removedRoles = new ArrayList<RoleAssignment>();

    @Override
    public RoleManagerService construct(String attestorId, String roleBasePath, boolean convertRoleNames)
    {
        return new RoleManagerServiceMock(attestorId, roleBasePath, convertRoleNames, this);
    }
    
    public void reset()
    {
        addedRoles.clear();
        removedRoles.clear();
    }


    public Collection<RoleAssignment> getAddedRoles()
    {
        return addedRoles;
    }


    public void setAddedRoles(Collection<RoleAssignment> addedRoles)
    {
        this.addedRoles = addedRoles;
    }


    public Collection<RoleAssignment> getRemovedRoles()
    {
        return removedRoles;
    }


    public void setRemovedRoles(Collection<RoleAssignment> removedRoles)
    {
        this.removedRoles = removedRoles;
    }

}
