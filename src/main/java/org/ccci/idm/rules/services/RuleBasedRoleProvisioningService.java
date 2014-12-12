package org.ccci.idm.rules.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ccci.idm.obj.RoleAssignment;
import org.ccci.util.NkUtil;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.DecisionTableConfiguration;
import org.drools.builder.DecisionTableInputType;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.io.Resource;
import org.drools.io.ResourceFactory;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.rule.FactHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This process runs a set of JBoss Drools rules and modifies the roles for a person based on
 * the results of the ruleset.
 * 
 * Usage:
 * 1) construct, giving it a RoleManagerService corresponding to the system storing the roles
 * 2) use the various add*Ruleset() functions to add rules defined in DRL, Excel, Changeset, and Package files
 * 3) call computeAndApplyRolesForUser() to compute the roles for a person and apply them
 *    (pass the facts required by the ruleset)
 * 
 * All rules will have access to a global variable named "now" which contains the current date/time as
 * a java.util.Date.
 * 
 * =====================
 * 
 * See: http://http://itwiki.ccci.org/confluence/display/ITG/JBoss+Rules+Engine+and+Web+Services
 * 
 * When making a new ruleset, it is important to understand these assumptions.
 * 
 * Terminology: assert=insert=assign, revoke=retract=unassign, role=group=access-level
 * 
 * Whenever the ruleset is run...
 * 
 *     If a role/group/access-level was last asserted by this ruleset...
 *         then the rulset must re-assert that same role (using "insert") or it will be removed.
 *     If a role was last asserted by someone or something other than this ruleset...
 *         and if the ruleset does NOT assert it or revoke it...
 *             then it will be left unchanged.
 *         and if the ruleset DOES assert it...
 *             then this ruleset will "take over" attestation (i.e. the next time you look at this assignment, it will look like the ruleset is responsible for the assignment).  This is done by removing and re-adding the person to the group.
 *         and if the ruleset DOES revoke it...
 *             then the assignment will be removed (i.e. the person will be removed from the group).
 * 
 * In other words,
 * 
 *     If you want a user to keep an automatically-assigned role (that is managed by this ruleset), then the ruleset must "insert" it every time the rule is run.
 *     If you want a user to lose an automatically-assigned role, then don't re-insert it.
 *     If you want a user to keep a manually-assigned role, then don't do anything to it.
 *     If you want a user to lose a manually-assigned role, then you need to "retract" it explicitly in your ruleset.
 *         Alternatively, if your ruleset inserts it in one run of the ruleset and then fails to re-insert it in a later run of the ruleset, the role will be removed from the user.
 * 
 * @author Nathan.Kopp
 *
 */
public class RuleBasedRoleProvisioningService
{
	private Logger logger = LoggerFactory.getLogger(getClass());

    protected KnowledgeBase kbase;
    protected RoleManagerService roleManager;
    protected String name;

    protected Set<String> requiredFacts;

    public RuleBasedRoleProvisioningService(RoleManagerService roleManager)
    {
    	this(null, roleManager);
    }
    
    public RuleBasedRoleProvisioningService(String name, RoleManagerService roleManager)
    {
        super();
        this.name = name;
        this.roleManager = roleManager;
        kbase = KnowledgeBaseFactory.newKnowledgeBase();
        // if these are started, tomcat won't shut down cleanly.  if we start them, we need to also be sure to stop them somehow
//        ResourceFactory.getResourceChangeNotifierService().start();
//        ResourceFactory.getResourceChangeScannerService().start();
    }
    
    public synchronized void addRequiredFact(String factName)
    {
        if(requiredFacts==null) requiredFacts = new HashSet<String>();
        requiredFacts.add(factName);
    }
    
    /**
     * This is the primary entry point into the service.  This runs the rules engine and then
     * applies the results to the user through the RoleManager passed into the constructor.
     * 
     * @param ssoGuid - the single sign-on identifier of the user for whom we are computing roles.
     * @param now - the current date
     * @param facts - all of the facts (other than existing assignments) that should be inserted
     *                into the knowledge base before running the rules.
     * @return
     * @throws Exception
     */
    public String computeAndApplyRolesForUser(String ssoGuid, Date now, Object... facts) throws Exception
    {
        logger.debug("compute and apply roles for user " + ssoGuid);

        Collection<RoleAssignment> allExistingAssignments = roleManager.findExistingAssignedRoles(ssoGuid);
        Collection<RoleAssignment> externalExistingAssignments = filterExistingRolesKeepExternal(allExistingAssignments);
        Collection<RoleAssignment> newAssignments = computeNewRoleAssignments(ssoGuid, externalExistingAssignments, now, facts);

        for(RoleAssignment roleAssignment : allExistingAssignments)
            logger.debug("all existing assignments for ssoguid " + ssoGuid + roleAssignment.toString());
        for(RoleAssignment roleAssignment : externalExistingAssignments)
            logger.debug("external existing assignments for ssoguid " + ssoGuid + roleAssignment.toString());
        for(RoleAssignment roleAssignment : newAssignments)
            logger.debug("new assignments for ssoguid " + ssoGuid + roleAssignment.toString());

        applyRoleAssignments(newAssignments, allExistingAssignments);

        return null;
    }

    private void applyRoleAssignments(Collection<RoleAssignment> newAssignments, Collection<RoleAssignment> allExistingAssignments) throws Exception
    {
        // add any new assignments
        for (RoleAssignment newOne : newAssignments)
        {
            RoleAssignment found = null;
            for (RoleAssignment r2 : allExistingAssignments)
            {
                if (r2.matches(newOne))
                {
                    found = r2;
                    break;
                }
            }
            if (found==null)
            {
                logger.debug("role assignment : role not found");
                roleManager.assignRoleToPerson(newOne);
            }
            else if(!thisIsAttestor(found) && thisIsAttestor(newOne))
            {
                // it now matches the rules, so let's replace to claim attestation
                logger.debug("role assignment : role found but replacing with new attestation");
                roleManager.removeRoleFromPerson(newOne);
                // need to make sure that timestamps show a difference between removal and addition, so changelog sorting happens properly!!!
                synchronized(this) {this.wait(1100);}
                roleManager.assignRoleToPerson(newOne);
            }
            else if(!NkUtil.equal(found.getExpiration(),newOne.getExpiration()))
            {
                logger.debug("role assignment : role found but updating role expiration");
                roleManager.updateRoleExpiration(newOne);
            }
            else
            {
                logger.debug("role assignment : role found but not doing anything");
            }
        }

        // remove any assignments that don't apply any more
        for (RoleAssignment r2 : allExistingAssignments)
        {
            boolean found = false;
            for (RoleAssignment r : newAssignments)
            {
                if (r2.matches(r))
                {
                    found = true;
                    break;
                }
            }
            if (!found)
            {
                logger.debug("role removal : role not found so removing");
                roleManager.removeRoleFromPerson(r2);
            }
        }
    }


    private List<RoleAssignment> computeNewRoleAssignments(String ssoGuid,  Collection<RoleAssignment> externalExistingAssignments, Date now, Object... facts)
    {
        StatefulKnowledgeSession ksession = setupRulesSession(kbase, externalExistingAssignments, now, facts);
        
        ksession.fireAllRules();
        
        List<RoleAssignment> newAssignments = extractRoleAssignmentsFromSession(ksession);

        massageDataForNewRoles(newAssignments, ssoGuid);
        newAssignments = removeDuplicateNewRoles(newAssignments, ssoGuid);

        return newAssignments;
    }

    private List<RoleAssignment> removeDuplicateNewRoles(List<RoleAssignment> newAssignments, String ssoGuid)
    {
        List<RoleAssignment> output = new ArrayList<RoleAssignment>();
        
        for(int i=0; i<newAssignments.size(); i++)
        {
            RoleAssignment newOne = (RoleAssignment)newAssignments.get(i);
            
            boolean shouldAdd = true;
            for(RoleAssignment alreadySelected : output)
            {
                if(alreadySelected.matches(newOne))
                {
                    // keep the one attested by us
                    if(!thisIsAttestor(alreadySelected) && thisIsAttestor(newOne))
                    {
                        // swap - remove the old one and we'll add the new one
                        output.remove(alreadySelected);
                        break;
                    }
                    else
                    {
                        // we want to keep the one we found, so break out now and don't add the new one
                        shouldAdd = false;
                        break;
                    }
                }
            }
            if(shouldAdd) output.add(newOne);
        }
        return output;
    }

    private List<RoleAssignment> extractRoleAssignmentsFromSession(StatefulKnowledgeSession ksession)
    {
        List<RoleAssignment> assignments = new ArrayList<RoleAssignment>();
        for(Object fact : ksession.getObjects())
        {
            if(fact instanceof RoleAssignment) assignments.add((RoleAssignment)fact);
        }
        return assignments;
    }

    private void massageDataForNewRoles(Collection<RoleAssignment> newAssignments, String ssoGuid)
    {
        for(RoleAssignment r : newAssignments)
        {
            if(!r.isExisting() && NkUtil.isBlank(r.getAttestorId()))
            {
                r.setAttestorId(roleManager.getAttestorId());
                r.setAssigneeId(ssoGuid);
                r.setRoleId(roleManager.convertRoleNameToFullPath(r.getRoleId()));
            }
        }
    }

    private StatefulKnowledgeSession setupRulesSession(KnowledgeBase kbase, Collection<RoleAssignment> externalExistingAssignments, Date now, Object... facts)
    {
        StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();
        
        setGlobals(now, ksession);

        for(Object fact : facts)
        {
            FactHandle employmentHandle = ksession.insert(fact);
        }
        
        for (RoleAssignment role : externalExistingAssignments)
        {
            FactHandle roleHandle = ksession.insert(role);
        }
        return ksession;
    }

    private void setGlobals(Date now, StatefulKnowledgeSession ksession)
    {
        ksession.getGlobals().set("now", now);
//        ksession.getGlobals().get(identifier);
//        try
//        {
//            ksession.setGlobal("now", now);
//        }
//        catch(RuntimeException e)
//        {
//            // if the ruleset doesn't declare the global, we're not allowed to set it and we'll get a null pointer exception
//            if(!e.getMessage().startsWith("Unexpected global")) throw e;
//        }
    }


    private Collection<RoleAssignment> filterExistingRolesKeepExternal(Collection<RoleAssignment> allExistingAssignments)
    {
        Collection<RoleAssignment> externalAssignments = new ArrayList<RoleAssignment>();
        for (RoleAssignment assignment : allExistingAssignments)
        {
            if (!thisIsAttestor(assignment))
            {
                externalAssignments.add(assignment);
                logger.debug("detected external assignment: " + assignment.getRoleId() + " assigned by " + assignment
                        .getAttestorId());
            }
        }
        return externalAssignments;
    }

    private boolean thisIsAttestor(RoleAssignment assignment)
    {
      return roleManager.getAttestorId().equalsIgnoreCase(assignment.getAttestorId());
    }


    /**
     * Example of a changeset:
     * <change-set xmlns='http://drools.org/drools-5.0/change-set' xmlns:xs='http://www.w3.org/2001/XMLSchema-instance' xs:schemaLocation='http://drools.org/drools-5.0/change-set change-set-5.0.xsd' >
     *   <add>
     *       <resource source='classpath:org/domain/someRules.drl' type='DRL' />
     *       <resource source='classpath:org/domain/aFlow.drf' type='DRF' />
     *   </add>
     * </change-set>
     * @param changeSetFileName
     */
    public void addChangeSetRuleset(String changeSetFileName)
    {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        
        Resource res = loadResource(changeSetFileName);
        
        if (res == null) throw new RuntimeException("file not found");
        
        kbuilder.add(res, ResourceType.CHANGE_SET);
    
        if (kbuilder.hasErrors())
        {
            throw new RuntimeException("Ruleset "+changeSetFileName+" has errors:\n" + kbuilder.getErrors());
        }
    
        kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());
    }

    private Resource loadResource(String changeSetFileName)
    {
        Resource res;
        if(changeSetFileName.startsWith("http:"))
            res = ResourceFactory.newUrlResource(changeSetFileName);
        else if(changeSetFileName.startsWith("classpath:"))
            res = ResourceFactory.newClassPathResource(changeSetFileName.substring(10));
        else
            res = ResourceFactory.newFileResource(changeSetFileName);
        return res;
    }
    
    public void addDrlRuleset(String drlFileName)
    {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        
        Resource res = loadResource(drlFileName);
        if (res == null) throw new RuntimeException("file not found");
        
        kbuilder.add(res, ResourceType.DRL);
    
        if (kbuilder.hasErrors())
        {
            throw new RuntimeException("Ruleset "+drlFileName+" has errors:\n" + kbuilder.getErrors());
        }
    
        kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());
    }
    
    public void addPackageRuleset(String pkgFileName)
    {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        
        Resource res = loadResource(pkgFileName);
        if (res == null) throw new RuntimeException("file not found");
        
        kbuilder.add(res, ResourceType.PKG);
    
        if (kbuilder.hasErrors())
        {
            throw new RuntimeException("Ruleset "+pkgFileName+" has errors:\n" + kbuilder.getErrors());
        }
    
        kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());
    }
    
    public void addExcelRuleset(String excelFileName, String... excelWorksheetNames)
    {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        
        Resource res = loadResource(excelFileName);
        if (res == null) throw new RuntimeException("file not found");
  
        for(String worksheetName : excelWorksheetNames)
        {
            DecisionTableConfiguration dtconf = KnowledgeBuilderFactory.newDecisionTableConfiguration();
            dtconf.setInputType(DecisionTableInputType.XLS);
            dtconf.setWorksheetName(worksheetName);
            kbuilder.add(res, ResourceType.DTABLE, dtconf);
        }
  
  
        if (kbuilder.hasErrors())
        {
            throw new RuntimeException("Ruleset "+excelFileName+" has errors:\n" + kbuilder.getErrors());
        }
  
        kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());
    }

    public Set<String> getRequiredFacts()
    {
        return requiredFacts;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }


    

}
