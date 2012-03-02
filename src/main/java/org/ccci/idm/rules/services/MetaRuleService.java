package org.ccci.idm.rules.services;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.ccci.idm.dao.IdentityDAO;
import org.ccci.idm.obj.IdentityUser;
import org.ccci.idm.rules.services.factprovider.EmployeeInfoProvider;

public class MetaRuleService
{
    private Properties properties;
    private RoleManagerFactory roleManagerFactory;
    private List<RuleBasedRoleProvisioningService> ruleServices;
	private List<FactProvider> factProviders;
	boolean setupComplete = false;
	

	public MetaRuleService(Properties properties, RoleManagerFactory roleManagerFactory) throws Exception
	{
		super();
		this.properties = properties;
		this.roleManagerFactory = roleManagerFactory;
	}
	
	public void setupDefaultIfNecessary() throws Exception
	{
	    if(setupComplete) return;
	    setupDefaultFactProviders();
        setupRuleServices();
	}
	
    public void setupDefaultFactProviders() throws Exception
    {
        factProviders = new ArrayList<FactProvider>();
        factProviders.add(new EmployeeInfoProvider(properties));
    }
    
    public void addFactProvider(FactProvider provider)
    {
        if(factProviders==null) factProviders = new ArrayList<FactProvider>();
        factProviders.add(provider);
    }
    
    public void setupRuleServices()
    {
        ruleServices = new ArrayList<RuleBasedRoleProvisioningService>();
        
        String[] rulesets = commaListToArray(properties.getProperty("rulesets"));
        for(String ruleset : rulesets)
        {
            RuleBasedRoleProvisioningService svc = loadRuleFromProperties(ruleset);
            ruleServices.add(svc);
        }
        
        
    }

    private RuleBasedRoleProvisioningService loadRuleFromProperties(String ruleset)
    {
        RoleManagerService roleManager = roleManagerFactory.construct(properties.getProperty(ruleset+".attestationUser"),
                                                                      properties.getProperty(ruleset+".grouperBase"),
                                                                      Boolean.valueOf(properties.getProperty(ruleset+".convertRoleNames")));
        RuleBasedRoleProvisioningService svc = new RuleBasedRoleProvisioningService(roleManager);
        String[] ruleFiles = commaListToArray(properties.getProperty(ruleset+".rulefiles"));
        for(String ruleFile : ruleFiles)
        {
            String ruleFileLC = ruleFile.toLowerCase();
            if(ruleFileLC.endsWith(".drl"))
            {
                svc.addDrlRuleset(ruleFile);
            }
            else if(ruleFileLC.endsWith(".changeset"))
            {
                svc.addChangeSetRuleset(ruleFile);
            }
            else if(ruleFileLC.endsWith(".pkg"))
            {
                svc.addPackageRuleset(ruleFile);
            }
            else if(ruleFileLC.endsWith(".xls"))
            {
                String[] sheets = commaListToArray(properties.getProperty(ruleset+"."+ruleFile.replace(':','_')+".sheets"));
                svc.addExcelRuleset(ruleFile, sheets);
            }
        }
        String[] facts = commaListToArray(properties.getProperty(ruleset+".facts"));
        for(String factName : facts)
        {
            svc.addRequiredFact(factName);
        }
        return svc;
    }

    public void runRules(IdentityDAO identityDao, String ssoGuid, RuleFilter ruleFilter) throws Exception
    {
        IdentityUser identityUser = identityDao.loadBySsoGuid(ssoGuid);
        
        for(RuleBasedRoleProvisioningService svc : ruleServices)
        {
            if(ruleFilter==null || ruleFilter.serviceMatches(svc))
            {
                List<Object> facts = loadFactsForRuleset(identityUser, svc);
                
                svc.computeAndApplyRolesForUser(ssoGuid, new Date(), facts.toArray());
            }
        }
    }

    private List<Object> loadFactsForRuleset(IdentityUser identityUser, RuleBasedRoleProvisioningService svc)
    {
        List<Object> facts = new ArrayList<Object>();
        facts.add(identityUser);
        
        for(String fact : svc.getRequiredFacts())
        {
            if(fact.equals("IdentityUser")) continue;
            
            for(FactProvider prov : factProviders)
            {
                if(prov.getFactName().equals(fact))
                {
                    facts.add(prov.getFact(identityUser));
                    break;
                }
            }
        }
        return facts;
    }

    private String[] commaListToArray(String str)
    {
        if(str==null) return null;
        String[] vals = str.split(",");
        for(int i=0; i<vals.length; i++) vals[i] = vals[i].trim();
        return vals;
    }


}
