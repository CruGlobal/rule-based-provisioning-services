package org.ccci.idm.rules.webservice;

import java.util.Properties;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import org.ccci.idm.authentication.credentials.impl.UsernamePasswordCredentials;
import org.ccci.idm.authentication.handler.impl.PropertyBasedUsernamePasswordAuthHandler;
import org.ccci.idm.authentication.manager.AuthenticationManager;
import org.ccci.idm.authentication.manager.impl.AuthenticationManagerImpl;
import org.ccci.idm.dao.IdentityDAO;
import org.ccci.idm.rules.services.IdentityDaoFactory;
import org.ccci.idm.rules.services.MetaRuleService;
import org.ccci.idm.rules.services.RuleFilter;
import org.ccci.idm.rules.services.rolemanager.RoleManagerFactoryGrouper;
import org.ccci.util.mail.ErrorEmailer;
import org.ccci.util.properties.CcciProperties.PropertyEncryptionSetup;
import org.ccci.util.properties.PropertiesWithFallback;

@WebService()
public class RuleProvSvc
{
    private AuthenticationManager authenticationManager;
    private Properties properties;
    private MetaRuleService service;
	

	public RuleProvSvc() throws Exception
	{
		loadProperties();
        setupAuthenticationManager();
        service = new MetaRuleService(properties, new RoleManagerFactoryGrouper());
	}
	
    @WebMethod(operationName = "runRules")
    public String runRules(@WebParam(name = "serverId") String serverId, @WebParam(name = "serverSecret") String serverSecret, @WebParam(name = "ssoGuid") String ssoGuid, @WebParam(name = "ruleFilter") RuleFilter ruleFilter) throws Throwable
    {
        authenticationManager.authenticate(new UsernamePasswordCredentials(serverId, serverSecret));

        service.setupDefaultIfNecessary();

        IdentityDAO identityDao = IdentityDaoFactory.getInstance();

        try
        {
            service.runRules(identityDao, ssoGuid, ruleFilter);
            return "done";
        }
        catch(Throwable e)
        {
            ErrorEmailer.sendErrorToAdmin(properties, e);
            e.printStackTrace();
            throw e;
        }
        finally
        {
            identityDao.close();
        }
    }
    
    
    private void loadProperties()
    {
        PropertyEncryptionSetup encryptionSetup = new PropertyEncryptionSetup("lco97gf5t7D%Y4bh89%U34IF&l*()$Hg6wRD^j4");
        properties = new PropertiesWithFallback(encryptionSetup, false, "/apps/apps-config/rules.properties", "/ora/apps-config/rules.properties","/rulesDefault.properties");
    }

    private void setupAuthenticationManager()
    {
        authenticationManager = new AuthenticationManagerImpl();
        authenticationManager.addAuthenticationHandler(new PropertyBasedUsernamePasswordAuthHandler(properties, "user."));
    }


}
