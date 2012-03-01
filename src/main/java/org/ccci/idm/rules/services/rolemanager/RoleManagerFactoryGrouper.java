package org.ccci.idm.rules.services.rolemanager;

import org.ccci.idm.rules.services.RoleManagerFactory;
import org.ccci.idm.rules.services.RoleManagerService;
import org.ccci.idm.rules.services.RoleManagerServiceGrouper;

public class RoleManagerFactoryGrouper implements RoleManagerFactory
{
    @Override
    public RoleManagerService construct(String attestorId, String roleBasePath, boolean convertRoleNames)
    {
        return new RoleManagerServiceGrouper(attestorId, roleBasePath, convertRoleNames);
    }

}
