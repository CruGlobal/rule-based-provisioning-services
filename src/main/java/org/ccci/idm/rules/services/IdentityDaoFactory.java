package org.ccci.idm.rules.services;

import org.ccci.idm.dao.IdentityDAO;
import org.ccci.idm.dao.impl.IdentityDaoUserManagerImpl;
import org.ccci.idm.user.UserManager;

public class IdentityDaoFactory
{
    public static IdentityDAO getInstance()
    {
        IdentityDaoUserManagerImpl identityDaoUserManager = new IdentityDaoUserManagerImpl();

        UserManager userManager = UserManagerService.getUserManager();

        identityDaoUserManager.setUserManager(userManager);

        return identityDaoUserManager;
    }
}
