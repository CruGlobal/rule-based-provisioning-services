package org.ccci.idm.rules.services;

import java.util.ArrayList;
import java.util.Collection;

import org.ccci.idm.obj.RoleAssignment;
import org.ccci.idm.rules.services.rolemanager.RoleManagerFactoryMock;
import org.ccci.util.NkUtil;

public class RoleManagerServiceMock implements RoleManagerService
{
    private String attestorId;
    Collection<RoleAssignment> addedRoles = new ArrayList<RoleAssignment>();
    Collection<RoleAssignment> removedRoles = new ArrayList<RoleAssignment>();
    Collection<RoleAssignment> currentRoles = new ArrayList<RoleAssignment>();
    private boolean convertRoleNames;
    private String roleBasePath;
    
    private RoleManagerFactoryMock factory;
    
    
    public RoleManagerServiceMock(String attestorId)
    {
        this(attestorId, "", false);
    }
    
    public RoleManagerServiceMock(String attestorId, String roleBasePath, boolean convertRoleNames)
    {
        super();
        this.attestorId = attestorId;
        this.roleBasePath = roleBasePath;
        this.convertRoleNames = convertRoleNames;
    }

    public RoleManagerServiceMock(String attestorId, String roleBasePath, boolean convertRoleNames,
                                  RoleManagerFactoryMock factory)
    {
        this(attestorId,roleBasePath,convertRoleNames);
        this.factory = factory;
    }

    

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
    
    @Override
    public Collection<RoleAssignment> findExistingAssignedRoles(String globalId) throws Exception
    {
        Collection<RoleAssignment> retVal = new ArrayList<RoleAssignment>(currentRoles.size());
        for(RoleAssignment r : currentRoles) retVal.add(r);
        return retVal;
    }

    @Override
    public void assignRoleToPerson(RoleAssignment r) throws Exception
    {
        addedRoles.add(r);
        if(factory!=null) factory.getAddedRoles().add(r);
        currentRoles.add(r);
    }

    @Override
    public void removeRoleFromPerson(RoleAssignment r) throws Exception
    {
        removedRoles.add(r);
        if(factory!=null) factory.getRemovedRoles().add(r);
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
    public String convertRoleNameToId(String string)
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

    @Override
    public void updateRoleExpiration(RoleAssignment r) throws Exception
    {
        removeRoleFromPerson(r);
        assignRoleToPerson(r);
    }
    @Override
    public String convertRoleNameToFullPath(String name)
    {
        if(NkUtil.isBlank(roleBasePath)) return convertRoleNames?convertRoleNameToId(name):name;
        else return roleBasePath +":"+(convertRoleNames?convertRoleNameToId(name):name);
    }

    public String getAttestorId()
    {
        return attestorId;
    }

}
