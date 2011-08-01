package org.ccci.idm.rules.services;

import java.util.ArrayList;
import java.util.Collection;

import org.ccci.idm.obj.RoleAssignment;

public class RuleBasedProvisioningServiceForTest extends RuleBasedProvisioningService
{
    Collection<RoleAssignment> addedRoles = new ArrayList<RoleAssignment>();
    Collection<RoleAssignment> removedRoles = new ArrayList<RoleAssignment>();
    Collection<RoleAssignment> currentRoles = new ArrayList<RoleAssignment>();
    
    public void reset(boolean resetCurrent)
    {
        addedRoles.clear();
        removedRoles.clear();
        if(resetCurrent)
        {
            currentRoles.clear();
        }
        else
        {
            for(RoleAssignment r : currentRoles) r.setExisting(true);
        }
    }
    
    public RuleBasedProvisioningServiceForTest(String attestorId, String roleBasePath, boolean convertRoleNames)
    {
        super(attestorId, roleBasePath, convertRoleNames);
    }

    @Override
    protected Collection<RoleAssignment> findExistingAssignedRoles(String globalId) throws Exception
    {
        Collection<RoleAssignment> retVal = new ArrayList<RoleAssignment>(currentRoles.size());
        for(RoleAssignment r : currentRoles) retVal.add(r);
        return retVal;
    }

    @Override
    protected void assignRoleToPerson(RoleAssignment r) throws Exception
    {
        addedRoles.add(r);
        currentRoles.add(r);
    }

    @Override
    protected void removeRoleFromPerson(RoleAssignment r) throws Exception
    {
        removedRoles.add(r);
        for(RoleAssignment r2 : currentRoles)
        {
            if(r2.matches(r))
            {
                currentRoles.remove(r2);
                break;
            }
        }
    }

    @Override
    protected String convertRoleNameToId(String string)
    {
        return string.replace(" ", "_").toLowerCase();
    }

    
    public void setAddedRoles(Collection<RoleAssignment> addedRoles)
    {
        this.addedRoles = addedRoles;
    }

    public Collection<RoleAssignment> getAddedRoles()
    {
        return addedRoles;
    }

    public void setRemovedRoles(Collection<RoleAssignment> removedRoles)
    {
        this.removedRoles = removedRoles;
    }

    public Collection<RoleAssignment> getRemovedRoles()
    {
        return removedRoles;
    }

    public void setCurrentRoles(Collection<RoleAssignment> currentRoles)
    {
        this.currentRoles = currentRoles;
    }

    public Collection<RoleAssignment> getCurrentRoles()
    {
        return currentRoles;
    }
}
