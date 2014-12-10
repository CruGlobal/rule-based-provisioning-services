package org.ccci.idm.rules.services.rolemanager;

import org.ccci.idm.rules.services.RoleManagerFactory;
import org.ccci.idm.rules.services.RoleManagerService;
import org.ccci.idm.rules.services.RoleManagerServiceUserManager;

public class RoleManagerFactoryUserManager implements RoleManagerFactory
{
    @Override
    public RoleManagerService construct(String attestorId, String roleBasePath, boolean convertRoleNames)
    {
        return new RoleManagerServiceUserManager(attestorId);
    }

}
