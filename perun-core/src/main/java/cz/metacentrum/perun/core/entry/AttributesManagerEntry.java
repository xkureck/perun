package cz.metacentrum.perun.core.entry;

import cz.metacentrum.perun.core.api.ActionType;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributeRights;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.AuthzResolver;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Host;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.PerunBean;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.Role;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.AttributeAlreadyMarkedUniqueException;
import cz.metacentrum.perun.core.api.exceptions.AttributeDefinitionExistsException;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.FacilityNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.GroupResourceMismatchException;
import cz.metacentrum.perun.core.api.exceptions.HostNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.IllegalArgumentException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.MemberGroupMismatchException;
import cz.metacentrum.perun.core.api.exceptions.MemberNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.MemberResourceMismatchException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.RelationExistsException;
import cz.metacentrum.perun.core.api.exceptions.ResourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ServiceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.UserExtSourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.bl.AttributesManagerBl;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.impl.Utils;
import cz.metacentrum.perun.utils.graphs.GraphTextFormat;
import guru.nidi.graphviz.engine.Graphviz;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * AttributesManager entry logic.
 *
 * @author Slavek Licehammer glory@ics.muni.cz
 */
public class AttributesManagerEntry implements AttributesManager {

	private PerunBl perunBl;
	private AttributesManagerBl attributesManagerBl;

	/**
	 * Constructor.
	 */
	public AttributesManagerEntry(PerunBl perunBl) {
		this.perunBl = perunBl;
		this.attributesManagerBl = perunBl.getAttributesManagerBl();
	}

	public AttributesManagerEntry() {
	}

	@Override
	public List<Attribute> getAttributes(PerunSession sess, Facility facility) throws PrivilegeException, FacilityNotExistsException, InternalErrorException {
		Utils.checkPerunSession(sess);

		getPerunBl().getFacilitiesManagerBl().checkFacilityExists(sess, facility);
		List<Attribute> attributes = getAttributesManagerBl().getAttributes(sess, facility);
		Iterator<Attribute> attrIter = attributes.iterator();
		//Choose to which attributes has the principal access
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, new AttributeDefinition(attrNext), facility)) attrIter.remove();
			else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, facility));
		}

		return attributes;
	}

	@Override
	public List<Attribute> getAttributes(PerunSession sess, Facility facility, List<String> attrNames) throws PrivilegeException, InternalErrorException, FacilityNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getFacilitiesManagerBl().checkFacilityExists(sess, facility);
		List<Attribute> attributes = getAttributesManagerBl().getAttributes(sess, facility, attrNames);
		Iterator<Attribute> attrIter = attributes.iterator();
		//Choose to which attributes has the principal access
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, new AttributeDefinition(attrNext), facility)) attrIter.remove();
			else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, facility));
		}
		return attributes;
	}

	@Override
	public List<Attribute> getAttributes(PerunSession sess, Vo vo) throws PrivilegeException, VoNotExistsException, InternalErrorException {
		Utils.checkPerunSession(sess);
		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);
		List<Attribute> attributes = getAttributesManagerBl().getAttributes(sess, vo);
		Iterator<Attribute> attrIter = attributes.iterator();
		//Choose to which attributes has the principal access
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, new AttributeDefinition(attrNext), vo)) attrIter.remove();
			else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, vo));
		}

		return attributes;
	}

	@Override
	public List<Attribute> getAttributes(PerunSession sess, Group group) throws PrivilegeException, GroupNotExistsException, InternalErrorException {
		Utils.checkPerunSession(sess);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);
		List<Attribute> attributes = getAttributesManagerBl().getAttributes(sess, group);
		Iterator<Attribute> attrIter = attributes.iterator();
		//Choose to which attributes has the principal access
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, new AttributeDefinition(attrNext), group)) attrIter.remove();
			else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, group));
		}

		return attributes;
	}

	@Override
	public List<Attribute> getAttributes(PerunSession sess, Resource resource) throws PrivilegeException, ResourceNotExistsException, InternalErrorException {
		Utils.checkPerunSession(sess);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		List<Attribute> attributes = getAttributesManagerBl().getAttributes(sess, resource);
		Iterator<Attribute> attrIter = attributes.iterator();
		//Choose to which attributes has the principal access
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, new AttributeDefinition(attrNext), resource)) attrIter.remove();
			else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, resource));
		}

		return attributes;
	}

	@Override
	public List<Attribute> getAttributes(PerunSession sess, Member member, Resource resource) throws PrivilegeException, ResourceNotExistsException, InternalErrorException, MemberNotExistsException, MemberResourceMismatchException {
		List<Attribute> attributes = getAttributesManagerBl().getAttributes(sess, member, resource);
		Iterator<Attribute> attrIter = attributes.iterator();
		//Choose to which attributes has the principal access
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, new AttributeDefinition(attrNext), member, resource)) attrIter.remove();
			else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, member, resource));
		}

		return attributes;
	}

	@Override
	public List<Attribute> getAttributes(PerunSession sess, Member member, Resource resource, boolean workWithUserAttributes) throws PrivilegeException, ResourceNotExistsException, InternalErrorException, MemberNotExistsException, MemberResourceMismatchException {
		Utils.checkPerunSession(sess);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);

		List<Attribute> attributes = getAttributesManagerBl().getAttributes(sess, member, resource, workWithUserAttributes);
		Iterator<Attribute> attrIter = attributes.iterator();
		//Choose to which attributes has the principal access
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_MEMBER_ATTR)) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, member)) attrIter.remove();
				else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, member));
			} else if(getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_USER_ATTR)) {
				User user = getPerunBl().getUsersManagerBl().getUserByMember(sess, member);
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, user)) attrIter.remove();
				else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, user));
			} else if(getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_USER_FACILITY_ATTR)) {
				User user = getPerunBl().getUsersManagerBl().getUserByMember(sess, member);
				Facility facility = getPerunBl().getResourcesManagerBl().getFacility(sess, resource);
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, user, facility)) attrIter.remove();
				else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, user, facility));
			} else if(getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_MEMBER_RESOURCE_ATTR)) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, member, resource)) attrIter.remove();
				else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, member, resource));
			} else {
				throw new ConsistencyErrorException("One of getting attributes is not correct type : " + attrNext);
			}
		}
		return attributes;
	}

	@Override
	public List<Attribute> getAttributes(PerunSession sess, Member member, Resource resource, List<String> attrNames, boolean workWithUserAttributes) throws PrivilegeException, ResourceNotExistsException, InternalErrorException, MemberNotExistsException, MemberResourceMismatchException {
		Utils.checkPerunSession(sess);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);

		List<Attribute> attributes = getAttributesManagerBl().getAttributes(sess, member, resource, attrNames, workWithUserAttributes);
		Iterator<Attribute> attrIter = attributes.iterator();
		//Choose to which attributes has the principal access
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_MEMBER_ATTR)) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, member)) attrIter.remove();
				else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, member));
			} else if(getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_USER_ATTR)) {
				User user = getPerunBl().getUsersManagerBl().getUserByMember(sess, member);
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, user)) attrIter.remove();
				else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, user));
			} else if(getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_USER_FACILITY_ATTR)) {
				User user = getPerunBl().getUsersManagerBl().getUserByMember(sess, member);
				Facility facility = getPerunBl().getResourcesManagerBl().getFacility(sess, resource);
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, user, facility)) attrIter.remove();
				else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, user, facility));
			} else if(getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_MEMBER_RESOURCE_ATTR)) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, member, resource)) attrIter.remove();
				else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, member, resource));
			} else {
				throw new ConsistencyErrorException("One of getting attributes is not correct type : " + attrNext);
			}
		}
		return attributes;
	}

	@Override
	public List<Attribute> getAttributes(PerunSession sess, Member member, Group group) throws PrivilegeException, GroupNotExistsException, InternalErrorException, MemberNotExistsException, WrongAttributeAssignmentException {
		Utils.checkPerunSession(sess);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);

		List<Attribute> attributes = getAttributesManagerBl().getAttributes(sess, member, group);
		Iterator<Attribute> attrIter = attributes.iterator();
		//Choose to which attributes has the principal access
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, new AttributeDefinition(attrNext), member, group)) attrIter.remove();
			else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, member, group));
		}

		return attributes;
	}

	@Override
	public List<Attribute> getAttributes(PerunSession sess, Member member, Group group, List<String> attrNames) throws PrivilegeException, GroupNotExistsException, InternalErrorException, MemberNotExistsException, WrongAttributeAssignmentException {
		Utils.checkPerunSession(sess);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);

		List<Attribute> attributes = getAttributesManagerBl().getAttributes(sess, member, group, attrNames);
		Iterator<Attribute> attrIter = attributes.iterator();
		// Choose to which attributes has the principal access
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if (!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, new AttributeDefinition(attrNext), member, group)) attrIter.remove();
			else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, member, group));
		}

		return attributes;
	}

	@Override
	public List<Attribute> getAttributes(PerunSession sess, Member member, Group group, List<String> attrNames, boolean workWithUserAttributes) throws PrivilegeException, GroupNotExistsException, InternalErrorException, MemberNotExistsException, WrongAttributeAssignmentException {
		Utils.checkPerunSession(sess);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);

		List<Attribute> attributes = getAttributesManagerBl().getAttributes(sess, member, group, attrNames, workWithUserAttributes);
		Iterator<Attribute> attrIter = attributes.iterator();
		// Choose to which attributes has the principal access
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_MEMBER_GROUP_ATTR)) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, member, group)) attrIter.remove();
				else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, member, group));
			} else if(getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_USER_ATTR)) {
				User user = getPerunBl().getUsersManagerBl().getUserByMember(sess, member);
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, user)) attrIter.remove();
				else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, user));
			} else if(getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_MEMBER_ATTR)) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, member)) attrIter.remove();
				else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, member));
			} else {
				throw new ConsistencyErrorException("One of getting attributes is not correct type : " + attrNext);
			}
		}
		return attributes;
	}

	@Override
	public List<Attribute> getAttributes(PerunSession sess, Facility facility, Resource resource, User user, Member member) throws PrivilegeException, ResourceNotExistsException, InternalErrorException, MemberNotExistsException, FacilityNotExistsException, UserNotExistsException, MemberResourceMismatchException {
		Utils.checkPerunSession(sess);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		getPerunBl().getFacilitiesManagerBl().checkFacilityExists(sess, facility);
		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);

		List<Attribute> attributes = getAttributesManagerBl().getAttributes(sess, facility, resource, user, member);
		Iterator<Attribute> attrIter = attributes.iterator();
		//Choose to which attributes has the principal access
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_MEMBER_ATTR)) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, member)) attrIter.remove();
				else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, member));
			} else if(getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_USER_ATTR)) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, user)) attrIter.remove();
				else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, user));
			} else if(getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_USER_FACILITY_ATTR)) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, user, facility)) attrIter.remove();
				else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, user, facility));
			} else if(getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_MEMBER_RESOURCE_ATTR)) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, member, resource)) attrIter.remove();
				else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, member, resource));
			} else {
				throw new ConsistencyErrorException("One of getting attributes is not correct type_: " + attrNext);
			}
		}
		return attributes;
	}

	@Override
	public List<Attribute> getAttributes(PerunSession sess, Member member) throws PrivilegeException, InternalErrorException, MemberNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		List<Attribute> attributes = getAttributesManagerBl().getAttributes(sess, member);
		Iterator<Attribute> attrIter = attributes.iterator();
		//Choose to which attributes has the principal access
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, new AttributeDefinition(attrNext), member)) attrIter.remove();
			else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, member));
		}
		return attributes;
	}

	@Override
	public List<Attribute> getAttributes(PerunSession sess, Vo vo, List<String> attrNames) throws PrivilegeException, InternalErrorException, VoNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);
		List<Attribute> attributes = getAttributesManagerBl().getAttributes(sess, vo, attrNames);
		Iterator<Attribute> attrIter = attributes.iterator();
		//Choose to which attributes has the principal access
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, new AttributeDefinition(attrNext), vo)) attrIter.remove();
			else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, vo));
		}
		return attributes;
	}

	@Override
	public List<Attribute> getAllAttributesStartWithNameWithoutNullValue(PerunSession sess, Group group, String startPartOfName) throws PrivilegeException, GroupNotExistsException, InternalErrorException {
		Utils.checkPerunSession(sess);
		Utils.notNull(startPartOfName, "startPartOfName");
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);
		List<Attribute> attributes = getAttributesManagerBl().getAllAttributesStartWithNameWithoutNullValue(sess, group, startPartOfName);
		Iterator<Attribute> attrIter = attributes.iterator();
		//Choose to which attributes has the principal access
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, new AttributeDefinition(attrNext), group)) attrIter.remove();
			else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, group));
		}
		return attributes;
	}

	@Override
	public List<Attribute> getAllAttributesStartWithNameWithoutNullValue(PerunSession sess, Resource resource, String startPartOfName) throws PrivilegeException, ResourceNotExistsException, InternalErrorException {
		Utils.checkPerunSession(sess);
		Utils.notNull(startPartOfName, "startPartOfName");
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		List<Attribute> attributes = getAttributesManagerBl().getAllAttributesStartWithNameWithoutNullValue(sess, resource, startPartOfName);
		Iterator<Attribute> attrIter = attributes.iterator();
		//Choose to which attributes has the principal access
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, new AttributeDefinition(attrNext), resource)) attrIter.remove();
			else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, resource));
		}
		return attributes;

	}

	@Override
	public List<Attribute> getAttributes(PerunSession sess, Member member, List<String> attrNames) throws PrivilegeException, InternalErrorException, MemberNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		List<Attribute> attributes = getAttributesManagerBl().getAttributes(sess, member, attrNames);
		Iterator<Attribute> attrIter = attributes.iterator();
		//Choose to which attributes has the principal access
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, new AttributeDefinition(attrNext), member)) attrIter.remove();
			else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, member));
		}
		return attributes;
	}

	@Override
	public List<Attribute> getAttributes(PerunSession sess, Group group, List<String> attrNames) throws PrivilegeException, InternalErrorException, GroupNotExistsException{
		Utils.checkPerunSession(sess);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);
		List<Attribute> attributes = getAttributesManagerBl().getAttributes(sess, group, attrNames);
		Iterator<Attribute> attrIter = attributes.iterator();
		//Choose to which attributes has the principal access
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, new AttributeDefinition(attrNext), group)) attrIter.remove();
			else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, group));
		}
		return attributes;
	}

	@Override
	public List<Attribute> getAttributes(PerunSession sess, Resource resource, List<String> attrNames) throws PrivilegeException, InternalErrorException, ResourceNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		List<Attribute> attributes = getAttributesManagerBl().getAttributes(sess, resource, attrNames);
		Iterator<Attribute> attrIter = attributes.iterator();
		//Choose to which attributes has the principal access
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, new AttributeDefinition(attrNext), resource)) attrIter.remove();
			else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, resource));
		}
		return attributes;
	}

	@Override
	public List<Attribute> getAttributes(PerunSession sess, Member member, List<String> attrNames, boolean workWithUserAttributes) throws PrivilegeException, InternalErrorException, MemberNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		List<Attribute> attributes = getAttributesManagerBl().getAttributes(sess, member, attrNames, workWithUserAttributes);
		Iterator<Attribute> attrIter = attributes.iterator();
		//Choose to which attributes has the principal access
		User user = getPerunBl().getUsersManagerBl().getUserByMember(sess, member);
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_MEMBER_ATTR)) {
				if (!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, new AttributeDefinition(attrNext), member)) attrIter.remove();
				else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, member));
			} else if (getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_USER_ATTR)) {
				if (!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, new AttributeDefinition(attrNext), user)) attrIter.remove();
				else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, user));
			} else {
				throw new ConsistencyErrorException("One of getting attributes is not correct type: " + attrNext);
			}
		}
		return attributes;
	}

	@Override
	public List<Attribute> getAttributes(PerunSession sess, User user, List<String> attrNames) throws PrivilegeException, InternalErrorException, UserNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);
		List<Attribute> attributes = getAttributesManagerBl().getAttributes(sess, user, attrNames);
		Iterator<Attribute> attrIter = attributes.iterator();
		//Choose to which attributes has the principal access
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, new AttributeDefinition(attrNext), user)) attrIter.remove();
			else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, user));
		}
		return attributes;
	}

	@Override
	public List<Attribute> getAttributes(PerunSession sess, UserExtSource ues, List<String> attrNames) throws PrivilegeException, InternalErrorException, UserExtSourceNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getUsersManagerBl().checkUserExtSourceExists(sess, ues);
		List<Attribute> attributes = getAttributesManagerBl().getAttributes(sess, ues, attrNames);
		Iterator<Attribute> attrIter = attributes.iterator();
		//Choose to which attributes has the principal access
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, new AttributeDefinition(attrNext), ues)) attrIter.remove();
			else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, ues));
		}
		return attributes;
	}

	@Override
	public List<Attribute> getAttributes(PerunSession sess, Facility facility, User user) throws PrivilegeException, FacilityNotExistsException, InternalErrorException, UserNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getFacilitiesManagerBl().checkFacilityExists(sess, facility);
		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);
		List<Attribute> attributes = getAttributesManagerBl().getAttributes(sess, facility, user);
		Iterator<Attribute> attrIter = attributes.iterator();
		//Choose to which attributes has the principal access
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, new AttributeDefinition(attrNext), user, facility)) attrIter.remove();
			else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, user, facility));
		}
		return attributes;
	}

	@Override
	public List<Attribute> getAttributes(PerunSession sess, User user) throws PrivilegeException, InternalErrorException, UserNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);

		List<Attribute> attributes = getAttributesManagerBl().getAttributes(sess, user);
		Iterator<Attribute> attrIter = attributes.iterator();
		//Choose to which attributes has the principal access
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, new AttributeDefinition(attrNext), user)) attrIter.remove();
			else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, user));
		}
		return attributes;
	}

	@Override
	public List<Attribute> getAttributes(PerunSession sess, Host host) throws PrivilegeException, InternalErrorException, HostNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getFacilitiesManagerBl().checkHostExists(sess, host);
		List<Attribute> attributes = getAttributesManagerBl().getAttributes(sess, host);
		Iterator<Attribute> attrIter = attributes.iterator();
		//Choose to which attributes has the principal access
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, new AttributeDefinition(attrNext), host)) attrIter.remove();
			else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, host));
		}
		return attributes;
	}

	@Override
	public List<Attribute> getAttributes(PerunSession sess, Resource resource, Group group) throws PrivilegeException, InternalErrorException, ResourceNotExistsException, GroupNotExistsException, GroupResourceMismatchException, WrongAttributeAssignmentException {
		Utils.checkPerunSession(sess);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);
		if(!getPerunBl().getGroupsManagerBl().getVo(sess, group).equals(getPerunBl().getResourcesManagerBl().getVo(sess, resource))) {
			throw new GroupResourceMismatchException("group and resource are not in the same VO");
		}
		List<Attribute> attributes = getAttributesManagerBl().getAttributes(sess, resource, group);
		Iterator<Attribute> attrIter = attributes.iterator();
		//Choose to which attributes has the principal access
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, new AttributeDefinition(attrNext), group, resource)) attrIter.remove();
			else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, group, resource));
		}
		return attributes;
	}

	@Override
	public List<Attribute> getAttributes(PerunSession sess, Resource resource, Group group, boolean workWithGroupAttributes) throws PrivilegeException, InternalErrorException, ResourceNotExistsException, GroupNotExistsException, GroupResourceMismatchException, WrongAttributeAssignmentException {
		Utils.checkPerunSession(sess);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);
		if(!getPerunBl().getGroupsManagerBl().getVo(sess, group).equals(getPerunBl().getResourcesManagerBl().getVo(sess, resource))) {
			throw new GroupResourceMismatchException("group and resource are not in the same VO");
		}
		List<Attribute> attributes = getAttributesManagerBl().getAttributes(sess, resource, group, workWithGroupAttributes);
		Iterator<Attribute> attrIter = attributes.iterator();
		//Choose to which attributes has the principal access
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_GROUP_ATTR)) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, group)) attrIter.remove();
				else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, group));
			} else if(getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_GROUP_RESOURCE_ATTR)) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, group, resource)) attrIter.remove();
				else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, group, resource));
			} else {
				throw new ConsistencyErrorException("One of getting attribute is not type of group or group_resource : " + attrNext);
			}
		}

		return attributes;
	}

	@Override
	public List<Attribute> getAttributes(PerunSession sess, Resource resource, Group group, List<String> attrNames, boolean workWithGroupAttributes) throws PrivilegeException, InternalErrorException, ResourceNotExistsException, GroupNotExistsException, GroupResourceMismatchException, WrongAttributeAssignmentException {
		Utils.checkPerunSession(sess);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);
		List<Attribute> attributes = getAttributesManagerBl().getAttributes(sess, resource, group, attrNames, workWithGroupAttributes);
		Iterator<Attribute> attrIter = attributes.iterator();
		//Choose to which attributes has the principal access
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_GROUP_ATTR)) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, group)) attrIter.remove();
				else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, group));
			} else if(getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_GROUP_RESOURCE_ATTR)) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, group, resource)) attrIter.remove();
				else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, group, resource));
			} else {
				throw new ConsistencyErrorException("One of getting attribute is not type of group or group_resource : " + attrNext);
			}
		}

		return attributes;
	}

	@Override
	public List<Attribute> getAttributes(PerunSession sess, Member member, boolean workWithUserAttributes) throws PrivilegeException, InternalErrorException, MemberNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		List<Attribute> attributes = getAttributesManagerBl().getAttributes(sess, member, workWithUserAttributes);
		Iterator<Attribute> attrIter = attributes.iterator();
		//Choose to which attributes has the principal access
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_MEMBER_ATTR)) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, member)) attrIter.remove();
				else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, member));
			} else if(getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_USER_ATTR)) {
				User user = getPerunBl().getUsersManagerBl().getUserByMember(sess, member);
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, user)) attrIter.remove();
				else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, user));
			} else {
				throw new ConsistencyErrorException("One of getting attribute is not type of member or user : " + attrNext);
			}
		}
		return attributes;
	}

	@Override
	public List<Attribute> getAttributes(PerunSession sess, String key) throws PrivilegeException, InternalErrorException{
		Utils.checkPerunSession(sess);
		Utils.notNull(key, "key for entityless attribute");
		if(key.isEmpty()) throw new InternalErrorException("key for entityless attribute can't be empty string");

		if(!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN)) throw new PrivilegeException("For getting entityless attributes principal need to be PerunAdmin.");

		return getAttributesManagerBl().setWritableTrue(sess, getAttributesManagerBl().getAttributes(sess, key));
	}

	@Override
	public List<Attribute> getAttributes(PerunSession sess, UserExtSource ues) throws PrivilegeException, InternalErrorException, UserExtSourceNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getUsersManagerBl().checkUserExtSourceExists(sess, ues);
		List<Attribute> attributes = getAttributesManagerBl().getAttributes(sess, ues);
		Iterator<Attribute> attrIter = attributes.iterator();
		//Choose to which attributes has the principal access
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, new AttributeDefinition(attrNext), ues)) attrIter.remove();
			else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, ues));
		}
		return attributes;
	}

	@Override
	public List<Attribute> getEntitylessAttributes(PerunSession sess, String attrName) throws PrivilegeException, InternalErrorException {
		Utils.checkPerunSession(sess);
		Utils.notNull(attrName, "name of entityless attributes");
		if(attrName.isEmpty()) throw new InternalErrorException("name for entityless attribute can't be empty string");
		if(!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN)) throw new PrivilegeException("For getting entityless attributes principal need to be PerunAdmin.");

		return getAttributesManagerBl().setWritableTrue(sess, getAttributesManagerBl().getEntitylessAttributes(sess, attrName));
	}

	@Override
	public List<String> getEntitylessKeys(PerunSession sess, AttributeDefinition attributeDefinition) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException, PrivilegeException {
		Utils.checkPerunSession(sess);
		if(!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN)) throw new PrivilegeException("For getting entityless attributes principal need to be PerunAdmin.");
		return getAttributesManagerBl().getEntitylessKeys(sess, attributeDefinition);
	}

	@Override
	public List<Attribute> getAttributesByAttributeDefinition(PerunSession sess, AttributeDefinition attributeDefinition) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException, PrivilegeException {
		Utils.checkPerunSession(sess);
		getAttributesManagerBl().checkAttributeExists(sess, attributeDefinition);
		if(!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN)) throw new PrivilegeException("For getting entityless attributes principal need to be PerunAdmin.");
		return getAttributesManagerBl().setWritableTrue(sess, getAttributesManagerBl().getAttributesByAttributeDefinition(sess, attributeDefinition));
	}

	@Override
	public void setAttributes(PerunSession sess, Facility facility, List<Attribute> attributes) throws PrivilegeException, InternalErrorException, FacilityNotExistsException, AttributeNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getPerunBl().getFacilitiesManagerBl().checkFacilityExists(sess, facility);
		getAttributesManagerBl().checkAttributesExists(sess, attributes);

		//Choose to which attributes has the principal access
		for(Attribute attr: attributes) {
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attr), facility )) throw new PrivilegeException("Principal has no access to set attribute = " + new AttributeDefinition(attr));
		}
		getAttributesManagerBl().setAttributes(sess, facility, attributes);
	}

	@Override
	public void setAttributes(PerunSession sess, Vo vo, List<Attribute> attributes) throws PrivilegeException, InternalErrorException, VoNotExistsException, AttributeNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);
		getAttributesManagerBl().checkAttributesExists(sess, attributes);
		//Choose to which attributes has the principal access
		for(Attribute attr: attributes) {
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attr), vo )) throw new PrivilegeException("Principal has no access to set attribute = " + new AttributeDefinition(attr));
		}
		getAttributesManagerBl().setAttributes(sess, vo, attributes);
	}

	@Override
	public void setAttributes(PerunSession sess, Group group, List<Attribute> attributes) throws PrivilegeException, InternalErrorException, GroupNotExistsException, AttributeNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);
		getAttributesManagerBl().checkAttributesExists(sess, attributes);
		//Choose to which attributes has the principal access
		for(Attribute attr: attributes) {
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attr), group )) throw new PrivilegeException("Principal has no access to set attribute = " + new AttributeDefinition(attr));
		}
		getAttributesManagerBl().setAttributes(sess, group, attributes);
	}

	@Override
	public void setAttributes(PerunSession sess, Resource resource, List<Attribute> attributes) throws PrivilegeException, ResourceNotExistsException, InternalErrorException, AttributeNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		getAttributesManagerBl().checkAttributesExists(sess, attributes);
		//Choose to which attributes has the principal access
		for(Attribute attr: attributes) {
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attr), resource )) throw new PrivilegeException("Principal has no access to set attribute = " + new AttributeDefinition(attr));
		}
		getAttributesManagerBl().setAttributes(sess, resource, attributes);
	}

	@Override
	public void setAttributes(PerunSession sess, Member member, Resource resource, List<Attribute> attributes) throws PrivilegeException, ResourceNotExistsException, InternalErrorException, MemberNotExistsException, AttributeNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException, MemberResourceMismatchException {
		Utils.checkPerunSession(sess);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		getAttributesManagerBl().checkAttributesExists(sess, attributes);
		//Choose to which attributes has the principal access
		for(Attribute attr: attributes) {
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attr), member , resource)) throw new PrivilegeException("Principal has no access to set attribute = " + new AttributeDefinition(attr));
		}
		getAttributesManagerBl().setAttributes(sess, member, resource, attributes);
	}

	@Override
	public void setAttributes(PerunSession sess, Member member, Group group, List<Attribute> attributes) throws PrivilegeException, GroupNotExistsException, InternalErrorException, MemberNotExistsException, AttributeNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException, UserNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);

		getAttributesManagerBl().checkAttributesExists(sess, attributes);
		// Choose to which attributes has the principal access
		for(Attribute attribute : attributes) {
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attribute), member, group)) throw new PrivilegeException("Principal has no access to set attribute = " + new AttributeDefinition(attribute));
		}
		getAttributesManagerBl().setAttributes(sess, member, group, attributes);
	}

	@Override
	public void setAttributes(PerunSession sess, Member member, Group group, List<Attribute> attributes, boolean workWithUserAttributes) throws PrivilegeException, GroupNotExistsException, InternalErrorException, MemberNotExistsException, AttributeNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException, UserNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);

		getAttributesManagerBl().checkAttributesExists(sess, attributes);
		//Choose to which attributes has the principal access
		for(Attribute attr: attributes) {
			if(getAttributesManagerBl().isFromNamespace(sess, attr, NS_MEMBER_GROUP_ATTR)) {
				if (!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attr), member, group))
					throw new PrivilegeException("Principal has no access to set attribute = " + new AttributeDefinition(attr));
			} else if(getAttributesManagerBl().isFromNamespace(sess, attr, NS_MEMBER_ATTR_DEF)) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attr), member)) throw new PrivilegeException("Principal has no access to set attribute = " + new AttributeDefinition(attr));
			} else if(getAttributesManagerBl().isFromNamespace(sess, attr, NS_USER_ATTR)) {
				User u = getPerunBl().getUsersManagerBl().getUserByMember(sess, member);
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attr), u)) throw new PrivilegeException("Principal has no access to set attribute = " + new AttributeDefinition(attr));
			} else {
				throw new WrongAttributeAssignmentException("One of setting attribute has not correct type : " + new AttributeDefinition(attr));
			}
		}
		getAttributesManagerBl().setAttributes(sess, member, group, attributes, workWithUserAttributes);
	}

	@Override
	public void setAttributes(PerunSession sess, Member member, Resource resource, List<Attribute> attributes, boolean workWithUserAttributes) throws PrivilegeException, ResourceNotExistsException, InternalErrorException, MemberNotExistsException, AttributeNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException, MemberResourceMismatchException {
		Utils.checkPerunSession(sess);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		getAttributesManagerBl().checkAttributesExists(sess, attributes);
		//Choose to which attributes has the principal access
		for(Attribute attr: attributes) {
			if(getAttributesManagerBl().isFromNamespace(sess, attr, NS_MEMBER_ATTR_DEF)) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attr), member)) throw new PrivilegeException("Principal has no access to set attribute = " + new AttributeDefinition(attr));
			} else if(getAttributesManagerBl().isFromNamespace(sess, attr, NS_MEMBER_RESOURCE_ATTR)) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attr), member , resource)) throw new PrivilegeException("Principal has no access to set attribute = " + new AttributeDefinition(attr));
			} else if(getAttributesManagerBl().isFromNamespace(sess, attr, NS_USER_FACILITY_ATTR)) {
				User u = getPerunBl().getUsersManagerBl().getUserByMember(sess, member);
				Facility f = getPerunBl().getResourcesManagerBl().getFacility(sess, resource);
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attr), u , f)) throw new PrivilegeException("Principal has no access to set attribute = " + new AttributeDefinition(attr));
			} else if(getAttributesManagerBl().isFromNamespace(sess, attr, NS_USER_ATTR)) {
				User u = getPerunBl().getUsersManagerBl().getUserByMember(sess, member);
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attr), u)) throw new PrivilegeException("Principal has no access to set attribute = " + new AttributeDefinition(attr));
			} else {
				throw new WrongAttributeAssignmentException("One of setting attribute has not correct type : " + new AttributeDefinition(attr));
			}
		}
		getAttributesManagerBl().setAttributes(sess, member, resource, attributes, workWithUserAttributes);
	}

	@Override
	public void setAttributes(PerunSession sess, Member member, List<Attribute> attributes, boolean workWithUserAttributes) throws PrivilegeException, MemberNotExistsException, UserNotExistsException, InternalErrorException, AttributeNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException{
		Utils.checkPerunSession(sess);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		if(workWithUserAttributes) getPerunBl().getUsersManagerBl().checkUserExists(sess,getPerunBl().getUsersManagerBl().getUserByMember(sess, member));
		getAttributesManagerBl().checkAttributesExists(sess, attributes);
		//Choose to which attributes has the principal access
		for(Attribute attr: attributes) {
			if(getAttributesManagerBl().isFromNamespace(sess, attr, NS_MEMBER_ATTR)) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attr), member )) throw new PrivilegeException("Principal has no access to set attribute = " + new AttributeDefinition(attr));
			} else if(getAttributesManagerBl().isFromNamespace(sess, attr, NS_USER_ATTR)) {
				User u = getPerunBl().getUsersManagerBl().getUserByMember(sess, member);
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attr), u)) throw new PrivilegeException("Principal has no access to set attribute = " + new AttributeDefinition(attr));
			} else {
				throw new WrongAttributeAssignmentException("One of setting attribute has not correct type : " + new AttributeDefinition(attr));
			}
		}
		getAttributesManagerBl().setAttributes(sess, member, attributes, workWithUserAttributes);
	}


	@Override
	public void setAttributes(PerunSession sess, Facility facility, Resource resource, User user, Member member, List<Attribute> attributes) throws PrivilegeException, ResourceNotExistsException, InternalErrorException, MemberNotExistsException, FacilityNotExistsException, UserNotExistsException, AttributeNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException, MemberResourceMismatchException {
		Utils.checkPerunSession(sess);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		getPerunBl().getFacilitiesManagerBl().checkFacilityExists(sess, facility);
		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);
		getAttributesManagerBl().checkAttributesExists(sess, attributes);
		//Choose to which attributes has the principal access
		for(Attribute attr: attributes) {
			if(getAttributesManagerBl().isFromNamespace(sess, attr, NS_MEMBER_ATTR_DEF)) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attr), member)) throw new PrivilegeException("Principal has no access to set attribute = " + new AttributeDefinition(attr));
			} else if(getAttributesManagerBl().isFromNamespace(sess, attr, NS_MEMBER_RESOURCE_ATTR)) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attr), member , resource)) throw new PrivilegeException("Principal has no access to set attribute = " + new AttributeDefinition(attr));
			} else if(getAttributesManagerBl().isFromNamespace(sess, attr, NS_USER_FACILITY_ATTR)) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attr), user , facility)) throw new PrivilegeException("Principal has no access to set attribute = " + new AttributeDefinition(attr));
			} else if(getAttributesManagerBl().isFromNamespace(sess, attr, NS_USER_ATTR)) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attr), user)) throw new PrivilegeException("Principal has no access to set attribute = " + new AttributeDefinition(attr));
			} else {
				throw new WrongAttributeAssignmentException("One of setting attribute has not correct type : " + new AttributeDefinition(attr));
			}
		}
		getAttributesManagerBl().setAttributes(sess, facility, resource, user, member, attributes);
	}

	@Override
	public void setAttributes(PerunSession sess, Facility facility, Resource resource, Group group, User user, Member member, List<Attribute> attributes) throws PrivilegeException, ResourceNotExistsException, InternalErrorException, MemberNotExistsException, FacilityNotExistsException, UserNotExistsException, AttributeNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException, GroupNotExistsException, GroupResourceMismatchException, MemberResourceMismatchException {
		Utils.checkPerunSession(sess);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		getPerunBl().getFacilitiesManagerBl().checkFacilityExists(sess, facility);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);
		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);
		getAttributesManagerBl().checkAttributesExists(sess, attributes);
		//Choose to which attributes has the principal access
		for(Attribute attr: attributes) {
			if(getAttributesManagerBl().isFromNamespace(sess, attr, NS_MEMBER_ATTR_DEF)) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attr), member)) throw new PrivilegeException("Principal has no access to set attribute = " + new AttributeDefinition(attr));
			} else if(getAttributesManagerBl().isFromNamespace(sess, attr, NS_MEMBER_RESOURCE_ATTR)) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attr), member , resource)) throw new PrivilegeException("Principal has no access to set attribute = " + new AttributeDefinition(attr));
			} else if(getAttributesManagerBl().isFromNamespace(sess, attr, NS_MEMBER_GROUP_ATTR)) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attr), member , group)) throw new PrivilegeException("Principal has no access to set attribute = " + new AttributeDefinition(attr));
			} else if(getAttributesManagerBl().isFromNamespace(sess, attr, NS_USER_FACILITY_ATTR)) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attr), user , facility)) throw new PrivilegeException("Principal has no access to set attribute = " + new AttributeDefinition(attr));
			} else if(getAttributesManagerBl().isFromNamespace(sess, attr, NS_USER_ATTR)) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attr), user)) throw new PrivilegeException("Principal has no access to set attribute = " + new AttributeDefinition(attr));
			} else {
				throw new WrongAttributeAssignmentException("One of setting attribute has not correct type : " + new AttributeDefinition(attr));
			}
		}
		getAttributesManagerBl().setAttributes(sess, facility, resource, group, user, member, attributes);
	}

	@Override
	public void setAttributes(PerunSession sess, Member member, List<Attribute> attributes) throws PrivilegeException, InternalErrorException, MemberNotExistsException, AttributeNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		getAttributesManagerBl().checkAttributesExists(sess, attributes);
		//Choose to which attributes has the principal access
		for(Attribute attr: attributes) {
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attr), member)) throw new PrivilegeException("Principal has no access to set attribute = " + new AttributeDefinition(attr));
		}
		getAttributesManagerBl().setAttributes(sess, member, attributes);
	}

	@Override
	public void setAttributes(PerunSession sess, Facility facility, User user, List<Attribute> attributes) throws PrivilegeException, FacilityNotExistsException, InternalErrorException, UserNotExistsException, AttributeNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getPerunBl().getFacilitiesManagerBl().checkFacilityExists(sess, facility);
		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);
		getAttributesManagerBl().checkAttributesExists(sess, attributes);
		//Choose to which attributes has the principal access
		for(Attribute attr: attributes) {
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attr), user , facility)) throw new PrivilegeException("Principal has no access to set attribute = " + new AttributeDefinition(attr));
		}
		getAttributesManagerBl().setAttributes(sess, facility, user, attributes);
	}

	@Override
	public void setAttributes(PerunSession sess, User user, List<Attribute> attributes) throws PrivilegeException, InternalErrorException, UserNotExistsException, AttributeNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);
		getAttributesManagerBl().checkAttributesExists(sess, attributes);
		//Choose to which attributes has the principal access
		for(Attribute attr: attributes) {
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attr), user)) throw new PrivilegeException("Principal has no access to set attribute = " + new AttributeDefinition(attr));
		}
		getAttributesManagerBl().setAttributes(sess, user, attributes);
	}

	@Override
	public void setAttributes(PerunSession sess, Host host, List<Attribute> attributes) throws PrivilegeException, HostNotExistsException, InternalErrorException,AttributeNotExistsException,WrongAttributeAssignmentException, WrongAttributeValueException {
		Utils.checkPerunSession(sess);
		getPerunBl().getFacilitiesManagerBl().checkHostExists(sess, host);
		getAttributesManagerBl().checkAttributesExists(sess, attributes);
		//Choose to which attributes has the principal access
		for(Attribute attr: attributes) {
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attr), host)) throw new PrivilegeException("Principal has no access to set attribute = " + new AttributeDefinition(attr));
		}
		try {
			getAttributesManagerBl().setAttributes(sess, host, attributes);
		} catch (WrongReferenceAttributeValueException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public void setAttributes(PerunSession sess, Resource resource, Group group, List<Attribute> attributes) throws InternalErrorException, ResourceNotExistsException, GroupNotExistsException, WrongAttributeAssignmentException, WrongAttributeValueException, AttributeNotExistsException, WrongReferenceAttributeValueException, PrivilegeException, GroupResourceMismatchException {
		Utils.checkPerunSession(sess);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);
		getAttributesManagerBl().checkAttributesExists(sess, attributes);
		//Choose to which attributes has the principal access
		for(Attribute attr: attributes) {
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attr), group , resource)) throw new PrivilegeException("Principal has no access to set attribute = " + new AttributeDefinition(attr));
		}
		getAttributesManagerBl().setAttributes(sess, resource, group, attributes);
	}

	@Override
	public void setAttributes(PerunSession sess, Resource resource, Group group, List<Attribute> attributes, boolean workWithGroupAttributes) throws InternalErrorException, ResourceNotExistsException, GroupNotExistsException, WrongAttributeAssignmentException, WrongAttributeValueException, AttributeNotExistsException, WrongReferenceAttributeValueException, PrivilegeException, GroupResourceMismatchException {
		Utils.checkPerunSession(sess);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);
		getAttributesManagerBl().checkAttributesExists(sess, attributes);
		if(!getPerunBl().getGroupsManagerBl().getVo(sess, group).equals(getPerunBl().getResourcesManagerBl().getVo(sess, resource))) {
			throw new GroupResourceMismatchException("group and resource are not in the same VO");
		}

		for(Attribute attr: attributes) {
			if(getAttributesManagerBl().isFromNamespace(sess, attr, NS_GROUP_ATTR)) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attr), group)) throw new PrivilegeException("Principal has no access to set attribute = " + new AttributeDefinition(attr));
			} else if(getAttributesManagerBl().isFromNamespace(sess, attr, NS_GROUP_RESOURCE_ATTR)) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attr), group , resource)) throw new PrivilegeException("Principal has no access to set attribute = " + new AttributeDefinition(attr));
			} else {
				throw new WrongAttributeAssignmentException("One of setting attribute has not correct type : " + new AttributeDefinition(attr));
			}
		}
		getAttributesManagerBl().setAttributes(sess, resource, group, attributes, workWithGroupAttributes);
	}

	@Override
	public void setAttributes(PerunSession sess, UserExtSource ues, List<Attribute> attributes) throws PrivilegeException, InternalErrorException, AttributeNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException, UserExtSourceNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getUsersManagerBl().checkUserExtSourceExists(sess, ues);
		getAttributesManagerBl().checkAttributesExists(sess, attributes);
		//Choose to which attributes has the principal access
		for(Attribute attr: attributes) {
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attr), ues)) throw new PrivilegeException("Principal has no access to set attribute = " + new AttributeDefinition(attr));
		}
		getAttributesManagerBl().setAttributes(sess, ues, attributes);
	}

	@Override
	public Attribute getAttribute(PerunSession sess, Facility facility, String attributeName) throws PrivilegeException, InternalErrorException, FacilityNotExistsException, AttributeNotExistsException, WrongAttributeAssignmentException {
		Utils.checkPerunSession(sess);
		Utils.notNull(attributeName, "attributeName");
		getPerunBl().getFacilitiesManagerBl().checkFacilityExists(sess, facility);
		Attribute attr = getAttributesManagerBl().getAttribute(sess, facility, attributeName);
		//Choose to which attributes has the principal access
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attr, facility)) throw new PrivilegeException("Principal has no access to get attribute = " + new AttributeDefinition(attr));
		else attr.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attr, facility));
		return attr;
	}

	@Override
	public Attribute getAttribute(PerunSession sess, Vo vo, String attributeName) throws PrivilegeException, InternalErrorException, VoNotExistsException, AttributeNotExistsException, WrongAttributeAssignmentException {
		Utils.checkPerunSession(sess);
		Utils.notNull(attributeName, "attributeName");
		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);
		Attribute attr = getAttributesManagerBl().getAttribute(sess, vo, attributeName);
		//Choose to which attributes has the principal access
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attr, vo)) throw new PrivilegeException("Principal has no access to get attribute = " + new AttributeDefinition(attr));
		else attr.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attr, vo));
		return attr;
	}

	@Override
	public Attribute getAttribute(PerunSession sess, Group group, String attributeName) throws PrivilegeException, InternalErrorException, GroupNotExistsException, AttributeNotExistsException, WrongAttributeAssignmentException {
		Utils.checkPerunSession(sess);
		Utils.notNull(attributeName, "attributeName");
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);
		Attribute attr = getAttributesManagerBl().getAttribute(sess, group, attributeName);
		//Choose to which attributes has the principal access
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attr, group)) throw new PrivilegeException("Principal has no access to get attribute = " + new AttributeDefinition(attr));
		else attr.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attr, group));
		return attr;
	}

	@Override
	public Attribute getAttribute(PerunSession sess, Resource resource, String attributeName) throws PrivilegeException, ResourceNotExistsException, InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		Utils.checkPerunSession(sess);
		Utils.notNull(attributeName, "attributeName");
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		Attribute attr = getAttributesManagerBl().getAttribute(sess, resource, attributeName);
		//Choose to which attributes has the principal access
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attr, resource)) throw new PrivilegeException("Principal has no access to get attribute = " + new AttributeDefinition(attr));
		else attr.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attr, resource));
		return attr;
	}

	@Override
	public Attribute getAttribute(PerunSession sess, Member member, Resource resource, String attributeName) throws PrivilegeException, ResourceNotExistsException, InternalErrorException, AttributeNotExistsException, MemberNotExistsException, MemberResourceMismatchException, WrongAttributeAssignmentException {
		Utils.checkPerunSession(sess);
		Utils.notNull(attributeName, "attributeName");
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		Attribute attr = getAttributesManagerBl().getAttribute(sess, member, resource, attributeName);
		//Choose to which attributes has the principal access
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attr, member, resource)) throw new PrivilegeException("Principal has no access to get attribute = " + new AttributeDefinition(attr));
		else attr.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attr, member, resource));
		return attr;
	}

	@Override
	public Attribute getAttribute(PerunSession sess, Member member, Group group, String attributeName) throws PrivilegeException, GroupNotExistsException, InternalErrorException, AttributeNotExistsException, MemberNotExistsException, WrongAttributeAssignmentException {
		Utils.checkPerunSession(sess);
		Utils.notNull(attributeName, "attributeName");
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);
		Attribute attribute = getAttributesManagerBl().getAttribute(sess, member, group, attributeName);
		//Choose to which attributes has the principal access
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attribute, member, group)) throw new PrivilegeException("Principal has no access to get attribute = " + new AttributeDefinition(attribute));
		else attribute.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attribute, member, group));

		return attribute;
	}

	@Override
	public Attribute getAttribute(PerunSession sess, Member member, String attributeName) throws PrivilegeException, InternalErrorException, AttributeNotExistsException, MemberNotExistsException, WrongAttributeAssignmentException, WrongAttributeAssignmentException {
		Utils.checkPerunSession(sess);
		Utils.notNull(attributeName, "attributeName");
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		Attribute attr = getAttributesManagerBl().getAttribute(sess, member, attributeName);
		//Choose to which attributes has the principal access
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attr, member)) throw new PrivilegeException("Principal has no access to get attribute = " + new AttributeDefinition(attr));
		else attr.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attr, member));
		return attr;
	}

	@Override
	public Attribute getAttribute(PerunSession sess, Facility facility, User user, String attributeName) throws PrivilegeException, FacilityNotExistsException, InternalErrorException, AttributeNotExistsException, UserNotExistsException, WrongAttributeAssignmentException {
		Utils.checkPerunSession(sess);
		Utils.notNull(attributeName, "attributeName");
		getPerunBl().getFacilitiesManagerBl().checkFacilityExists(sess, facility);
		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);
		Attribute attr = getAttributesManagerBl().getAttribute(sess, facility, user, attributeName);
		//Choose to which attributes has the principal access
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attr, user, facility)) throw new PrivilegeException("Principal has no access to get attribute = " + new AttributeDefinition(attr));
		else attr.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attr, user, facility));
		return attr;
	}

	@Override
	public Attribute getAttribute(PerunSession sess, User user, String attributeName) throws PrivilegeException, InternalErrorException, AttributeNotExistsException, UserNotExistsException, WrongAttributeAssignmentException {
		Utils.checkPerunSession(sess);
		Utils.notNull(attributeName, "attributeName");
		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);
		Attribute attr = getAttributesManagerBl().getAttribute(sess, user, attributeName);
		//Choose to which attributes has the principal access
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attr, user)) throw new PrivilegeException("Principal has no access to get attribute = " + new AttributeDefinition(attr));
		else attr.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attr, user));
		return attr;
	}

	@Override
	public Attribute getAttribute(PerunSession sess, Host host, String attributeName) throws PrivilegeException, InternalErrorException, AttributeNotExistsException, HostNotExistsException,WrongAttributeAssignmentException {
		Utils.checkPerunSession(sess);
		Utils.notNull(attributeName, "attributeName");
		getPerunBl().getFacilitiesManagerBl().checkHostExists(sess, host);
		Attribute attr = getAttributesManagerBl().getAttribute(sess, host, attributeName);
		//Choose to which attributes has the principal access
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attr, host)) throw new PrivilegeException("Principal has no access to get attribute = " + new AttributeDefinition(attr));
		else attr.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attr, host));
		return attr;
	}

	@Override
	public Attribute getAttribute(PerunSession sess, Resource resource, Group group, String attributeName) throws PrivilegeException, InternalErrorException, AttributeNotExistsException, ResourceNotExistsException, GroupNotExistsException, WrongAttributeAssignmentException, GroupResourceMismatchException {
		Utils.checkPerunSession(sess);
		Utils.notNull(attributeName, "attributeName");
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);
		if(!getPerunBl().getGroupsManagerBl().getVo(sess, group).equals(getPerunBl().getResourcesManagerBl().getVo(sess, resource))) {
			throw new GroupResourceMismatchException("group and resource are not in the same VO");
		}
		Attribute attr = getAttributesManagerBl().getAttribute(sess, resource, group, attributeName);
		//Choose to which attributes has the principal access
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attr, group, resource)) throw new PrivilegeException("Principal has no access to get attribute = " + new AttributeDefinition(attr));
		else attr.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attr, group, resource));
		return attr;
	}

	@Override
	public Attribute getAttribute(PerunSession sess, UserExtSource ues, String attributeName) throws PrivilegeException, InternalErrorException, AttributeNotExistsException, UserExtSourceNotExistsException, WrongAttributeAssignmentException {
		Utils.checkPerunSession(sess);
		Utils.notNull(attributeName, "attributeName");
		getPerunBl().getUsersManagerBl().checkUserExtSourceExists(sess, ues);
		Attribute attr = getAttributesManagerBl().getAttribute(sess, ues, attributeName);
		//Choose to which attributes has the principal access
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attr, ues)) throw new PrivilegeException("Principal has no access to get attribute = " + new AttributeDefinition(attr));
		else attr.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attr, ues));
		return attr;
	}

	@Override
	public Attribute getAttribute(PerunSession sess, String key, String attributeName) throws PrivilegeException, InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		Utils.checkPerunSession(sess);
		Utils.notNull(attributeName, "attributeName");
		Utils.notNull(key, "key");
		if(key.isEmpty()) throw new InternalErrorException("key for entityless attribute can't be empty string");

		return getAttributesManagerBl().getAttribute(sess, key, attributeName);
	}

	@Override
	public AttributeDefinition getAttributeDefinition(PerunSession sess, String attributeName) throws PrivilegeException, InternalErrorException, AttributeNotExistsException {
		Utils.checkPerunSession(sess);
		Utils.notNull(attributeName, "attributeName");
		//Everyone can access to attrDefs
		return getAttributesManagerBl().getAttributeDefinition(sess, attributeName);
	}

	@Override
	public List<AttributeDefinition> getAttributesDefinition(PerunSession sess) throws PrivilegeException, InternalErrorException, AttributeNotExistsException {
		Utils.checkPerunSession(sess);
		//Everyone can access to attrDefs
		return getAttributesManagerBl().getAttributesDefinition(sess);
	}

	@Override
	public List<AttributeDefinition> getAttributesDefinitionWithRights(PerunSession sess, List<PerunBean> entities) throws PrivilegeException, InternalErrorException, AttributeNotExistsException {
		Utils.checkPerunSession(sess);

		//For this method are rights resolved in the Bl(BlImpl)
		return getAttributesManagerBl().getAttributesDefinitionWithRights(sess, entities);
	}

	@Override
	public List<AttributeDefinition> getAttributesDefinition(PerunSession sess, List<String> listOfAttributesNames) throws PrivilegeException, InternalErrorException, AttributeNotExistsException {
		Utils.checkPerunSession(sess);
		Utils.notNull(listOfAttributesNames, "List of attrNames");
		//Everyone can access to attrDefs
		return getAttributesManagerBl().getAttributesDefinition(sess, listOfAttributesNames);
	}

	@Override
	public AttributeDefinition getAttributeDefinitionById(PerunSession sess, int id) throws PrivilegeException, InternalErrorException, AttributeNotExistsException {
		Utils.checkPerunSession(sess);
		//Everyone can access to attrDefs
		return getAttributesManagerBl().getAttributeDefinitionById(sess, id);
	}

	@Override
	public List<AttributeDefinition> getAttributesDefinitionByNamespace(PerunSession sess, String namespace) throws PrivilegeException, InternalErrorException {
		Utils.checkPerunSession(sess);
		Utils.notNull(namespace, "namespace");
		//Everyone can access to attrDefs
		return getAttributesManagerBl().getAttributesDefinitionByNamespace(sess, namespace);
	}

	@Override
	public Attribute getAttributeById(PerunSession sess, Facility facility, int id) throws PrivilegeException, InternalErrorException, FacilityNotExistsException, AttributeNotExistsException, WrongAttributeAssignmentException {
		Utils.checkPerunSession(sess);
		getPerunBl().getFacilitiesManagerBl().checkFacilityExists(sess, facility);
		Attribute attr = getAttributesManagerBl().getAttributeById(sess, facility, id);
		//Choose to which attributes has the principal access
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attr, facility)) throw new PrivilegeException("Principal has no access to get attribute = " + new AttributeDefinition(attr));
		else attr.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attr, facility));
		return attr;
	}

	@Override
	public Attribute getAttributeById(PerunSession sess, Vo vo, int id) throws PrivilegeException, InternalErrorException, VoNotExistsException, AttributeNotExistsException, WrongAttributeAssignmentException {
		Utils.checkPerunSession(sess);
		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);
		Attribute attr = getAttributesManagerBl().getAttributeById(sess, vo, id);
		//Choose to which attributes has the principal access
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attr, vo)) throw new PrivilegeException("Principal has no access to get attribute = " + new AttributeDefinition(attr));
		else attr.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attr, vo));
		return attr;
	}

	@Override
	public Attribute getAttributeById(PerunSession sess, Resource resource, int id) throws PrivilegeException, ResourceNotExistsException, InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		Utils.checkPerunSession(sess);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		Attribute attr = getAttributesManagerBl().getAttributeById(sess, resource, id);
		//Choose to which attributes has the principal access
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attr, resource)) throw new PrivilegeException("Principal has no access to get attribute = " + new AttributeDefinition(attr));
		else attr.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attr, resource));
		return attr;
	}

	@Override
	public Attribute getAttributeById(PerunSession sess, Member member, Resource resource, int id) throws PrivilegeException, ResourceNotExistsException, InternalErrorException, AttributeNotExistsException, MemberNotExistsException, WrongAttributeAssignmentException, MemberResourceMismatchException {
		Utils.checkPerunSession(sess);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		Attribute attr = getAttributesManagerBl().getAttributeById(sess, member, resource, id);
		//Choose to which attributes has the principal access
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attr, member, resource)) throw new PrivilegeException("Principal has no access to get attribute = " + new AttributeDefinition(attr));
		else attr.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attr, member, resource));
		return attr;
	}

	@Override
	public Attribute getAttributeById(PerunSession sess, Member member, Group group, int id) throws PrivilegeException, GroupNotExistsException, InternalErrorException, AttributeNotExistsException, MemberNotExistsException, WrongAttributeAssignmentException {
		Utils.checkPerunSession(sess);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);
		Attribute attribute = getAttributesManagerBl().getAttributeById(sess, member, group, id);
		//Choose to which attributes has the principal access
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attribute, member, group)) throw new PrivilegeException("Principal has no access to get attribute = " + new AttributeDefinition(attribute));
		else attribute.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attribute, member, group));

		return attribute;
	}

	@Override
	public Attribute getAttributeById(PerunSession sess, Member member, int id) throws PrivilegeException, InternalErrorException, AttributeNotExistsException, MemberNotExistsException, WrongAttributeAssignmentException {
		Utils.checkPerunSession(sess);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		Attribute attr = getAttributesManagerBl().getAttributeById(sess, member, id);
		//Choose to which attributes has the principal access
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attr, member)) throw new PrivilegeException("Principal has no access to get attribute = " + new AttributeDefinition(attr));
		else attr.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attr, member));
		return attr;
	}

	@Override
	public Attribute getAttributeById(PerunSession sess, Facility facility, User user, int id) throws PrivilegeException, FacilityNotExistsException, InternalErrorException, AttributeNotExistsException, UserNotExistsException, WrongAttributeAssignmentException {
		Utils.checkPerunSession(sess);
		getPerunBl().getFacilitiesManagerBl().checkFacilityExists(sess, facility);
		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);
		Attribute attr = getAttributesManagerBl().getAttributeById(sess, facility, user, id);
		//Choose to which attributes has the principal access
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attr, user, facility)) throw new PrivilegeException("Principal has no access to get attribute = " + new AttributeDefinition(attr));
		else attr.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attr, user, facility));
		return attr;
	}

	@Override
	public Attribute getAttributeById(PerunSession sess, User user, int id) throws PrivilegeException, InternalErrorException, AttributeNotExistsException, UserNotExistsException, WrongAttributeAssignmentException {
		Utils.checkPerunSession(sess);
		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);
		Attribute attr = getAttributesManagerBl().getAttributeById(sess, user, id);
		//Choose to which attributes has the principal access
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attr, user)) throw new PrivilegeException("Principal has no access to get attribute = " + new AttributeDefinition(attr));
		else attr.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attr, user));
		return attr;
	}

	@Override
	public Attribute getAttributeById(PerunSession sess, Host host, int id) throws PrivilegeException, InternalErrorException, AttributeNotExistsException, HostNotExistsException, WrongAttributeAssignmentException {
		Utils.checkPerunSession(sess);
		getPerunBl().getFacilitiesManagerBl().checkHostExists(sess, host);
		Attribute attr = getAttributesManagerBl().getAttributeById(sess, host, id);
		//Choose to which attributes has the principal access
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attr, host)) throw new PrivilegeException("Principal has no access to get attribute = " + new AttributeDefinition(attr));
		else attr.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attr, host));
		return attr;
	}

	@Override
	public Attribute getAttributeById(PerunSession sess, Resource resource, Group group, int id) throws PrivilegeException, InternalErrorException, AttributeNotExistsException, ResourceNotExistsException, GroupNotExistsException, WrongAttributeAssignmentException, GroupResourceMismatchException {
		Utils.checkPerunSession(sess);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);
		if(!getPerunBl().getGroupsManagerBl().getVo(sess, group).equals(getPerunBl().getResourcesManagerBl().getVo(sess, resource))) {
			throw new GroupResourceMismatchException("group and resource are not in the same VO");
		}
		Attribute attr = getAttributesManagerBl().getAttributeById(sess, resource, group, id);
		//Choose to which attributes has the principal access
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attr, group, resource)) throw new PrivilegeException("Principal has no access to get attribute = " + new AttributeDefinition(attr));
		else attr.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attr, group, resource));
		return attr;
	}

	@Override
	public Attribute getAttributeById(PerunSession sess, Group group, int id) throws PrivilegeException, InternalErrorException, AttributeNotExistsException, GroupNotExistsException, WrongAttributeAssignmentException, GroupResourceMismatchException {
		Utils.checkPerunSession(sess);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);
		Attribute attr = getAttributesManagerBl().getAttributeById(sess, group, id);
		//Choose to which attributes has the principal access
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attr, group)) throw new PrivilegeException("Principal has no access to get attribute = " + new AttributeDefinition(attr));
		else attr.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attr, group));
		return attr;
	}

	@Override
	public Attribute getAttributeById(PerunSession sess, UserExtSource ues, int id) throws PrivilegeException, InternalErrorException, AttributeNotExistsException, UserExtSourceNotExistsException, WrongAttributeAssignmentException {
		Utils.checkPerunSession(sess);
		getPerunBl().getUsersManagerBl().checkUserExtSourceExists(sess, ues);
		Attribute attr = getAttributesManagerBl().getAttributeById(sess, ues, id);
		//Choose to which attributes has the principal access
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attr, ues)) throw new PrivilegeException("Principal has no access to get attribute = " + new AttributeDefinition(attr));
		else attr.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attr, ues));
		return attr;
	}

	@Override
	public void setAttribute(PerunSession sess, Facility facility, Attribute attribute) throws PrivilegeException, InternalErrorException, FacilityNotExistsException, AttributeNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getAttributesManagerBl().checkAttributeExists(sess, attribute);
		getPerunBl().getFacilitiesManagerBl().checkFacilityExists(sess, facility);
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attribute, facility)) throw new PrivilegeException("Principal has no access to set attribute = " + new AttributeDefinition(attribute));
		getAttributesManagerBl().setAttribute(sess, facility, attribute);
	}

	@Override
	public void setAttribute(PerunSession sess, Vo vo, Attribute attribute) throws PrivilegeException, InternalErrorException, VoNotExistsException, AttributeNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);
		getAttributesManagerBl().checkAttributeExists(sess, attribute);
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attribute, vo)) throw new PrivilegeException("Principal has no access to set attribute = " + new AttributeDefinition(attribute));
		getAttributesManagerBl().setAttribute(sess, vo, attribute);
	}

	@Override
	public void setAttribute(PerunSession sess, Group group, Attribute attribute) throws PrivilegeException, InternalErrorException, GroupNotExistsException, AttributeNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);
		getAttributesManagerBl().checkAttributeExists(sess, attribute);
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attribute, group)) throw new PrivilegeException("Principal has no access to set attribute = " + new AttributeDefinition(attribute));
		getAttributesManagerBl().setAttribute(sess, group, attribute);
	}

	@Override
	public void setAttribute(PerunSession sess, Resource resource, Attribute attribute) throws PrivilegeException, ResourceNotExistsException, InternalErrorException, AttributeNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getAttributesManagerBl().checkAttributeExists(sess, attribute);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attribute, resource)) throw new PrivilegeException("Principal has no access to set attribute = " + new AttributeDefinition(attribute));
		getAttributesManagerBl().setAttribute(sess, resource, attribute);
	}

	@Override
	public void setAttribute(PerunSession sess, Member member, Resource resource, Attribute attribute) throws PrivilegeException, ResourceNotExistsException, InternalErrorException, MemberNotExistsException, AttributeNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException, MemberResourceMismatchException {
		Utils.checkPerunSession(sess);
		getAttributesManagerBl().checkAttributeExists(sess, attribute);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attribute, member, resource)) throw new PrivilegeException("Principal has no access to set attribute = " + new AttributeDefinition(attribute));
		getAttributesManagerBl().setAttribute(sess, member, resource, attribute);
	}

	@Override
	public void setAttribute(PerunSession sess, Member member, Group group, Attribute attribute) throws PrivilegeException, GroupNotExistsException, InternalErrorException, MemberNotExistsException, AttributeNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getAttributesManagerBl().checkAttributeExists(sess, attribute);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attribute, member, group)) throw new PrivilegeException("Principal has no access to set attribute = " + new AttributeDefinition(attribute));
		getAttributesManagerBl().setAttribute(sess, member, group, attribute);
	}

	@Override
	public void setAttribute(PerunSession sess, Member member, Attribute attribute) throws PrivilegeException, InternalErrorException, MemberNotExistsException, AttributeNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getAttributesManagerBl().checkAttributeExists(sess, attribute);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attribute, member)) throw new PrivilegeException("Principal has no access to set attribute = " + new AttributeDefinition(attribute));
		getAttributesManagerBl().setAttribute(sess, member, attribute);
	}

	@Override
	public void setAttribute(PerunSession sess, Facility facility, User user, Attribute attribute) throws PrivilegeException, FacilityNotExistsException, InternalErrorException, UserNotExistsException, AttributeNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getAttributesManagerBl().checkAttributeExists(sess, attribute);
		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);
		getPerunBl().getFacilitiesManagerBl().checkFacilityExists(sess, facility);
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attribute, user, facility)) throw new PrivilegeException("Principal has no access to set attribute = " + new AttributeDefinition(attribute));
		getAttributesManagerBl().setAttribute(sess, facility, user, attribute);
	}

	@Override
	public void setAttribute(PerunSession sess, User user, Attribute attribute) throws PrivilegeException, InternalErrorException, UserNotExistsException, AttributeNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getAttributesManagerBl().checkAttributeExists(sess, attribute);
		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attribute, user)) throw new PrivilegeException("Principal has no access to set attribute = " + new AttributeDefinition(attribute));
		getAttributesManagerBl().setAttribute(sess, user, attribute);
	}

	@Override
	public void setAttribute(PerunSession sess, Host host, Attribute attribute) throws PrivilegeException, InternalErrorException, HostNotExistsException,AttributeNotExistsException,WrongAttributeValueException,WrongAttributeAssignmentException {
		Utils.checkPerunSession(sess);
		getAttributesManagerBl().checkAttributeExists(sess, attribute);
		getPerunBl().getFacilitiesManagerBl().checkHostExists(sess, host);
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attribute, host)) throw new PrivilegeException("Principal has no access to set attribute = " + new AttributeDefinition(attribute));
		try {
			getAttributesManagerBl().setAttribute(sess, host, attribute);
		} catch (WrongReferenceAttributeValueException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public void setAttribute(PerunSession sess, Resource resource, Group group, Attribute attribute) throws PrivilegeException, InternalErrorException, ResourceNotExistsException, GroupNotExistsException, AttributeNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException, GroupResourceMismatchException {
		Utils.checkPerunSession(sess);
		getAttributesManagerBl().checkAttributeExists(sess, attribute);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attribute, group, resource)) throw new PrivilegeException("Principal has no access to set attribute = " + new AttributeDefinition(attribute));
		getAttributesManagerBl().setAttribute(sess, resource, group, attribute);

	}

	@Override
	public void setAttribute(PerunSession sess, String key, Attribute attribute) throws PrivilegeException, InternalErrorException, AttributeNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getAttributesManagerBl().checkAttributeExists(sess, attribute);
		Utils.notNull(key, "key");
		if(key.isEmpty()) throw new InternalErrorException("key for entityless attribute can't be empty string");
		if(!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN)) throw new PrivilegeException("Only perunAdmin can set entityless attributes.");
		getAttributesManagerBl().setAttribute(sess, key, attribute);

	}

	@Override
	public void setAttribute(PerunSession sess, UserExtSource ues, Attribute attribute) throws PrivilegeException, InternalErrorException, UserExtSourceNotExistsException, AttributeNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getAttributesManagerBl().checkAttributeExists(sess, attribute);
		getPerunBl().getUsersManagerBl().checkUserExtSourceExists(sess, ues);
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attribute, ues)) throw new PrivilegeException("Principal has no access to set attribute = " + new AttributeDefinition(attribute));
		getAttributesManagerBl().setAttribute(sess, ues, attribute);
	}

	@Override
	public AttributeDefinition createAttribute(PerunSession sess, AttributeDefinition attribute) throws PrivilegeException, InternalErrorException, AttributeDefinitionExistsException {
		Utils.checkPerunSession(sess);
		Utils.notNull(attribute, "attributeDefinition");
		if(!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN)) throw new PrivilegeException("Only perunAdmin can create new Attribute.");
		if (!attribute.getFriendlyName().matches(AttributesManager.ATTRIBUTES_REGEXP)) {
			throw new InternalErrorException(new IllegalArgumentException("Wrong attribute name " + attribute.getFriendlyName() + ", attribute name must match " + AttributesManager.ATTRIBUTES_REGEXP));
		}
		return getAttributesManagerBl().createAttribute(sess, attribute);
	}

	@Override
	public void deleteAttribute(PerunSession sess, AttributeDefinition attribute) throws PrivilegeException, InternalErrorException, AttributeNotExistsException, RelationExistsException {
		Utils.checkPerunSession(sess);
		getAttributesManagerBl().checkAttributeExists(sess, attribute);
		if(!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN)) throw new PrivilegeException("Only perunAdmin can delete existing Attribute.");
		getAttributesManagerBl().deleteAttribute(sess, attribute);
	}

	@Override
	public void deleteAttribute(PerunSession sess, AttributeDefinition attributeDefinition, boolean force) throws PrivilegeException, InternalErrorException, AttributeNotExistsException, RelationExistsException {
		Utils.checkPerunSession(sess);
		getAttributesManagerBl().checkAttributeExists(sess, attributeDefinition);
		if(!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN)) throw new PrivilegeException("Only perunAdmin can delete existing Attribute.");
		getAttributesManagerBl().deleteAttribute(sess, attributeDefinition, force);
	}

	@Override
	public List<Attribute> getRequiredAttributes(PerunSession sess, Resource resource) throws PrivilegeException, InternalErrorException, ResourceNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		List<Attribute> attributes = getAttributesManagerBl().getRequiredAttributes(sess, resource);
		Iterator<Attribute> attrIter = attributes.iterator();
		//Choose to which attributes has the principal access
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, resource)) attrIter.remove();
			else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, resource));
		}
		return attributes;
	}

	@Override
	public List<Attribute> getResourceRequiredAttributes(PerunSession sess, Resource resourceToGetServicesFrom, Member member, Resource resource) throws PrivilegeException, InternalErrorException, ResourceNotExistsException, MemberNotExistsException, WrongAttributeAssignmentException, MemberResourceMismatchException {
		Utils.checkPerunSession(sess);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resourceToGetServicesFrom);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		List<Attribute> attributes = getAttributesManagerBl().getResourceRequiredAttributes(sess, resourceToGetServicesFrom, member, resource);
		Iterator<Attribute> attrIter = attributes.iterator();
		//Choose to which attributes has the principal access
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, member, resource)) attrIter.remove();
			else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, member, resource));
		}
		return attributes;
	}

	@Override
	public List<Attribute> getResourceRequiredAttributes(PerunSession sess, Resource resourceToGetServicesFrom, Member member, Resource resource, boolean workWithUserAttributes) throws PrivilegeException, InternalErrorException, ResourceNotExistsException, MemberNotExistsException, WrongAttributeAssignmentException, MemberResourceMismatchException {
		Utils.checkPerunSession(sess);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resourceToGetServicesFrom);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		List<Attribute> attributes = getAttributesManagerBl().getResourceRequiredAttributes(sess, resourceToGetServicesFrom, member, resource, workWithUserAttributes);
		Iterator<Attribute> attrIter = attributes.iterator();
		//Choose to which attributes has the principal access
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_MEMBER_RESOURCE_ATTR)) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, member, resource)) attrIter.remove();
				else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, member, resource));
			} else if (getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_MEMBER_ATTR)){
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, member)) attrIter.remove();
				else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, member));
			} else if (getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_USER_ATTR)) {
				User u = getPerunBl().getUsersManagerBl().getUserByMember(sess, member);
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, u)) attrIter.remove();
				else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, u));
			} else if (getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_USER_FACILITY_ATTR)){
				User u = getPerunBl().getUsersManagerBl().getUserByMember(sess, member);
				Facility f = getPerunBl().getResourcesManagerBl().getFacility(sess, resource);
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, u, f)) attrIter.remove();
				else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, u, f));
			} else {
				throw new ConsistencyErrorException("There is some attribute which is not type of any possible choice.");
			}
		}
		return attributes;
	}

	@Override
	public List<Attribute> getResourceRequiredAttributes(PerunSession sess, Resource resourceToGetServicesFrom, Member member, Group group) throws PrivilegeException, InternalErrorException, WrongAttributeAssignmentException, ResourceNotExistsException, MemberNotExistsException, GroupNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resourceToGetServicesFrom);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);

		List<Attribute> attributes = getAttributesManagerBl().getResourceRequiredAttributes(sess, resourceToGetServicesFrom, member, group);
		Iterator<Attribute> attrIter = attributes.iterator();
		//Choose to which attributes has the principal access
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, member, group)) attrIter.remove();
			else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, member, group));
		}
		return attributes;
	}

	@Override
	public List<Attribute> getResourceRequiredAttributes(PerunSession sess, Resource resourceToGetServicesFrom, Member member, Group group, boolean workWithUserAttributes) throws PrivilegeException, InternalErrorException, WrongAttributeAssignmentException, ResourceNotExistsException, MemberNotExistsException, GroupNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resourceToGetServicesFrom);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);

		List<Attribute> attributes = getAttributesManagerBl().getResourceRequiredAttributes(sess, resourceToGetServicesFrom, member, group, workWithUserAttributes);
		Iterator<Attribute> attrIter = attributes.iterator();
		//Choose to which attributes has the principal access
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_MEMBER_GROUP_ATTR)) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, member, group)) attrIter.remove();
				else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, member, group));
			} else if (getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_MEMBER_ATTR)){
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, member)) attrIter.remove();
				else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, member));
			} else if (getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_USER_ATTR)) {
				User user = getPerunBl().getUsersManagerBl().getUserByMember(sess, member);
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, user)) attrIter.remove();
				else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, user));
			} else {
				throw new ConsistencyErrorException("There is some attribute which is not type of any possible choice.");
			}
		}
		return attributes;
	}

	@Override
	public List<Attribute> getResourceRequiredAttributes(PerunSession sess, Resource resourceToGetServicesFrom, Facility facility, Resource resource, User user, Member member) throws PrivilegeException, InternalErrorException, ResourceNotExistsException, MemberNotExistsException, FacilityNotExistsException, UserNotExistsException, MemberResourceMismatchException {
		Utils.checkPerunSession(sess);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resourceToGetServicesFrom);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		getPerunBl().getFacilitiesManagerBl().checkFacilityExists(sess, facility);
		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);
		List<Attribute> attributes = getAttributesManagerBl().getResourceRequiredAttributes(sess, resourceToGetServicesFrom, facility, resource, user, member);
		Iterator<Attribute> attrIter = attributes.iterator();
		//Choose to which attributes has the principal access
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_MEMBER_RESOURCE_ATTR)) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, member, resource)) attrIter.remove();
				else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, member, resource));
			} else if (getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_MEMBER_ATTR)){
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, member)) attrIter.remove();
				else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, member));
			} else if (getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_USER_ATTR)) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, user)) attrIter.remove();
				else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, user));
			} else if (getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_USER_FACILITY_ATTR)){
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, user, facility)) attrIter.remove();
				else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, user, facility));
			} else {
				throw new ConsistencyErrorException("There is some attribute which is not type of any possible choice.");
			}
		}
		return attributes;
	}

	@Override
	public List<Attribute> getResourceRequiredAttributes(PerunSession sess, Resource resourceToGetServicesFrom, Facility facility, User user) throws InternalErrorException, PrivilegeException, ResourceNotExistsException, FacilityNotExistsException, UserNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resourceToGetServicesFrom);
		getPerunBl().getFacilitiesManagerBl().checkFacilityExists(sess, facility);
		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);
		List<Attribute> attributes = getAttributesManagerBl().getResourceRequiredAttributes(sess, resourceToGetServicesFrom, facility, user);
		Iterator<Attribute> attrIter = attributes.iterator();
		//Choose to which attributes has the principal access
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, user, facility)) attrIter.remove();
			else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, user, facility));
		}
		return attributes;
	}

	@Override
	public List<Attribute> getResourceRequiredAttributes(PerunSession sess, Resource resourceToGetServicesFrom, Member member) throws PrivilegeException, InternalErrorException, MemberNotExistsException, ResourceNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resourceToGetServicesFrom);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		List<Attribute> attributes = getAttributesManagerBl().getResourceRequiredAttributes(sess, resourceToGetServicesFrom, member);
		Iterator<Attribute> attrIter = attributes.iterator();
		//Choose to which attributes has the principal access
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, member)) attrIter.remove();
			else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, member));
		}
		return attributes;
	}

	@Override
	public List<Attribute> getResourceRequiredAttributes(PerunSession sess, Resource resourceToGetServicesFrom, User user) throws PrivilegeException, InternalErrorException, UserNotExistsException, ResourceNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resourceToGetServicesFrom);
		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);
		List<Attribute> attributes = getAttributesManagerBl().getResourceRequiredAttributes(sess, resourceToGetServicesFrom, user);
		Iterator<Attribute> attrIter = attributes.iterator();
		//Choose to which attributes has the principal access
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, user)) attrIter.remove();
			else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, user));
		}
		return attributes;
	}

	@Override
	public List<Attribute> getResourceRequiredAttributes(PerunSession sess, Resource resourceToGetServicesFrom, Host host) throws PrivilegeException, InternalErrorException, HostNotExistsException, ResourceNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resourceToGetServicesFrom);
		getPerunBl().getFacilitiesManagerBl().checkHostExists(sess, host);
		List<Attribute> attributes = getAttributesManagerBl().getResourceRequiredAttributes(sess, resourceToGetServicesFrom, host);
		Iterator<Attribute> attrIter = attributes.iterator();
		//Choose to which attributes has the principal access
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, host)) attrIter.remove();
			else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, host));
		}
		return attributes;
	}

	@Override
	public List<Attribute> getResourceRequiredAttributes(PerunSession sess, Resource resourceToGetServicesFrom, Resource resource, Group group, boolean workWithGroupAttributes) throws PrivilegeException, InternalErrorException, ResourceNotExistsException, GroupNotExistsException, GroupResourceMismatchException, WrongAttributeAssignmentException {
		Utils.checkPerunSession(sess);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resourceToGetServicesFrom);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);

		if(group.getVoId() != resource.getVoId()) {
			throw new GroupResourceMismatchException("Group and resource are not in the same VO.");
		}

		List<Attribute> attributes = getAttributesManagerBl().getResourceRequiredAttributes(sess, resourceToGetServicesFrom, resource, group, workWithGroupAttributes);
		Iterator<Attribute> attrIter = attributes.iterator();
		//Choose to which attributes has the principal access
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_GROUP_RESOURCE_ATTR)) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, group, resource)) attrIter.remove();
				else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, group, resource));
			} else if (getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_GROUP_ATTR)){
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, group)) attrIter.remove();
				else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, group));
			} else {
				throw new ConsistencyErrorException("There is some attribute which is not of expected type (group or group_resource).");
			}
		}
		return attributes;
	}

	@Override
	public List<Attribute> getResourceRequiredAttributes(PerunSession sess, Resource resourceToGetServicesFrom, Resource resource, Group group, Member member, boolean workWithUserAttributes) throws PrivilegeException, InternalErrorException, ResourceNotExistsException, GroupNotExistsException, GroupResourceMismatchException, WrongAttributeAssignmentException, MemberNotExistsException, MemberGroupMismatchException, UserNotExistsException, FacilityNotExistsException, MemberResourceMismatchException {
		Utils.checkPerunSession(sess);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resourceToGetServicesFrom);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);

		if(group.getVoId() != resource.getVoId()) {
			throw new GroupResourceMismatchException("Group and resource are not in the same VO.");
		}
		if(member.getVoId() != group.getVoId()) {
			throw new MemberGroupMismatchException("Member and Group are not in the same VO.");
		}

		List<Attribute> attributes = getAttributesManagerBl().getResourceRequiredAttributes(sess, resourceToGetServicesFrom, member, resource, workWithUserAttributes);
		attributes.addAll(getAttributesManagerBl().getResourceRequiredAttributes(sess, resourceToGetServicesFrom, member, group));

		User user = getPerunBl().getUsersManagerBl().getUserById(sess, member.getUserId());
		Facility facility = getPerunBl().getFacilitiesManagerBl().getFacilityById(sess, resource.getFacilityId());

		Iterator<Attribute> attrIter = attributes.iterator();
		//Choose to which attributes has the principal access
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_MEMBER_ATTR)) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, member)) attrIter.remove();
				else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, member));
			} else if (getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_USER_ATTR)){
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, user)) attrIter.remove();
				else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, user));
			} else if (getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_MEMBER_GROUP_ATTR)){
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, member, group)) attrIter.remove();
				else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, member, group));
			} else if (getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_MEMBER_RESOURCE_ATTR)){
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, member, resource)) attrIter.remove();
				else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, member, resource));
			} else if (getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_USER_FACILITY_ATTR)){
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, user, facility)) attrIter.remove();
				else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, user, facility));
			} else {
				throw new ConsistencyErrorException("There is some attribute which is not of expected type (member, user, user_facility, member_group, member_resource).");
			}
		}
		return attributes;
	}

	@Override
	public List<Attribute> getResourceRequiredAttributes(PerunSession sess, Resource resourceToGetServicesFrom, Resource resource, Group group) throws PrivilegeException, InternalErrorException, ResourceNotExistsException, GroupNotExistsException, WrongAttributeAssignmentException {
		Utils.checkPerunSession(sess);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resourceToGetServicesFrom);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);
		List<Attribute> attributes = getAttributesManagerBl().getResourceRequiredAttributes(sess, resourceToGetServicesFrom, resource, group);
		Iterator<Attribute> attrIter = attributes.iterator();
		//Choose to which attributes has the principal access
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, group, resource)) attrIter.remove();
			else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, group, resource));
		}
		return attributes;
	}

	@Override
	public List<Attribute> getResourceRequiredAttributes(PerunSession sess, Resource resourceToGetServicesFrom, Group group) throws PrivilegeException, InternalErrorException, ResourceNotExistsException, GroupNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resourceToGetServicesFrom);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);
		List<Attribute> attributes = getAttributesManagerBl().getResourceRequiredAttributes(sess, resourceToGetServicesFrom, group);
		Iterator<Attribute> attrIter = attributes.iterator();
		//Choose to which attributes has the principal access
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, group)) attrIter.remove();
			else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, group));
		}
		return attributes;
	}


	@Override
	public List<Attribute> getRequiredAttributes(PerunSession sess, Member member, Resource resource) throws PrivilegeException, InternalErrorException, ResourceNotExistsException, MemberNotExistsException, MemberResourceMismatchException {
		Utils.checkPerunSession(sess);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		List<Attribute> attributes = getAttributesManagerBl().getRequiredAttributes(sess, member, resource);
		Iterator<Attribute> attrIter = attributes.iterator();
		//Choose to which attributes has the principal access
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, member, resource)) attrIter.remove();
			else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, member, resource));
		}
		return attributes;
	}

	@Override
	public List<Attribute> getRequiredAttributes(PerunSession sess, Member member, Resource resource, boolean workWithUserAttributes) throws PrivilegeException, InternalErrorException, ResourceNotExistsException, MemberNotExistsException, MemberResourceMismatchException {
		Utils.checkPerunSession(sess);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		List<Attribute> attributes = getAttributesManagerBl().getRequiredAttributes(sess, member, resource, workWithUserAttributes);
		Iterator<Attribute> attrIter = attributes.iterator();
		//Choose to which attributes has the principal access
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if (getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_MEMBER_ATTR)){
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, member)) attrIter.remove();
				else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, member));
			} else if (getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_USER_ATTR)) {
				User u = getPerunBl().getUsersManagerBl().getUserByMember(sess, member);
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, u)) attrIter.remove();
				else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, u));
			} else if (getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_MEMBER_RESOURCE_ATTR)) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, member, resource)) attrIter.remove();
				else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, member, resource));
			} else if (getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_USER_FACILITY_ATTR)) {
				User u = getPerunBl().getUsersManagerBl().getUserByMember(sess, member);
				Facility f = getPerunBl().getResourcesManagerBl().getFacility(sess, resource);
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, u, f)) attrIter.remove();
				else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, u, f));
			} else {
				throw new ConsistencyErrorException("There is some attribute which is not type of any possible choice.");
			}
		}
		return attributes;
	}

	@Override
	public List<Attribute> getRequiredAttributes(PerunSession sess, Facility facility, User user) throws InternalErrorException, PrivilegeException, ResourceNotExistsException, FacilityNotExistsException, UserNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getFacilitiesManagerBl().checkFacilityExists(sess, facility);
		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);
		List<Attribute> attributes = getAttributesManagerBl().getRequiredAttributes(sess, facility, user);
		Iterator<Attribute> attrIter = attributes.iterator();
		//Choose to which attributes has the principal access
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, user, facility)) attrIter.remove();
			else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, user, facility));
		}
		return attributes;
	}

	@Override
	public List<Attribute> getRequiredAttributes(PerunSession sess, Member member, boolean workWithUserAttributes) throws PrivilegeException, InternalErrorException, MemberNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		List<Attribute> attributes = getAttributesManagerBl().getRequiredAttributes(sess, member, workWithUserAttributes);
		Iterator<Attribute> attrIter = attributes.iterator();
		User user = null;
		//Choose to which attributes has the principal access
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if (getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_MEMBER_ATTR)){
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, member)) attrIter.remove();
				else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, member));
			} else if (getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_USER_ATTR)) {
				if (user == null) user = getPerunBl().getUsersManagerBl().getUserByMember(sess, member);
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, user)) attrIter.remove();
				else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, user));
			} else {
				throw new ConsistencyErrorException("There is some attribute which is not type of any possible choice.");
			}
		}
		return attributes;
	}

	@Override
	public List<Attribute> getRequiredAttributes(PerunSession sess, Member member, Group group, boolean workWithUserAttributes) throws PrivilegeException, InternalErrorException, MemberNotExistsException, GroupNotExistsException, WrongAttributeAssignmentException {
		Utils.checkPerunSession(sess);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);

		List<Attribute> attributes = getAttributesManagerBl().getRequiredAttributes(sess, member, group, workWithUserAttributes);
		Iterator<Attribute> attrIter = attributes.iterator();
		User user = null;
		//Choose to which attributes has the principal access
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if (getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_MEMBER_ATTR)){
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, member)) attrIter.remove();
				else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, member));
			} else if (getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_USER_ATTR)) {
				if (user == null) user = getPerunBl().getUsersManagerBl().getUserByMember(sess, member);
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, user)) attrIter.remove();
				else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, user));
			} else if (getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_MEMBER_GROUP_ATTR)) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, member, group)) attrIter.remove();
				else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, member, group));
			} else {
				throw new ConsistencyErrorException("There is some attribute which is not type of any possible choice.");
			}
		}
		return attributes;
	}

	@Override
	public List<Attribute> getRequiredAttributes(PerunSession sess, User user) throws PrivilegeException, InternalErrorException, UserNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);
		List<Attribute> attributes = getAttributesManagerBl().getRequiredAttributes(sess, user);
		Iterator<Attribute> attrIter = attributes.iterator();
		//Choose to which attributes has the principal access
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, user)) attrIter.remove();
			else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, user));
		}
		return attributes;
	}

	@Override
	public List<AttributeDefinition> getRequiredAttributesDefinition(PerunSession sess, Service service) throws PrivilegeException, InternalErrorException, ServiceNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getServicesManagerBl().checkServiceExists(sess, service);
		//Everyone can access to attrNexts
		return getAttributesManagerBl().getRequiredAttributesDefinition(sess, service);
	}

	@Override
	public List<Attribute> getRequiredAttributes(PerunSession sess, Facility facility) throws PrivilegeException, InternalErrorException, FacilityNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getFacilitiesManagerBl().checkFacilityExists(sess, facility);
		List<Attribute> attributes = getAttributesManagerBl().getRequiredAttributes(sess, facility);
		Iterator<Attribute> attrIter = attributes.iterator();
		//Choose to which attributes has the principal access
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, facility)) attrIter.remove();
			else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, facility));
		}
		return attributes;
	}

	@Override
	public List<Attribute> getRequiredAttributes(PerunSession sess, Service service, Facility facility) throws PrivilegeException, InternalErrorException, FacilityNotExistsException, ServiceNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getServicesManagerBl().checkServiceExists(sess, service);
		getPerunBl().getFacilitiesManagerBl().checkFacilityExists(sess, facility);
		List<Attribute> attributes = getAttributesManagerBl().getRequiredAttributes(sess, service, facility);
		Iterator<Attribute> attrIter = attributes.iterator();
		//Choose to which attributes has the principal access
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, facility)) attrIter.remove();
			else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, facility));
		}
		return attributes;
	}

	@Override
	public List<Attribute> getRequiredAttributes(PerunSession sess, Service service, Vo vo) throws PrivilegeException, InternalErrorException, VoNotExistsException, ServiceNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getServicesManagerBl().checkServiceExists(sess, service);
		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);
		List<Attribute> attributes = getAttributesManagerBl().getRequiredAttributes(sess, service, vo);
		Iterator<Attribute> attrIter = attributes.iterator();
		//Choose to which attributes has the principal access
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, vo)) attrIter.remove();
			else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, vo));
		}
		return attributes;
	}

	@Override
	public List<Attribute> getRequiredAttributes(PerunSession sess, List<Service> services, Facility facility) throws PrivilegeException, InternalErrorException, FacilityNotExistsException, ServiceNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getFacilitiesManagerBl().checkFacilityExists(sess, facility);
		Set<Attribute> attributes = new HashSet<Attribute>();
		// TODO & FIXME: there should be a proper select in BL & Impl
		for (Service s : services) {
			getPerunBl().getServicesManagerBl().checkServiceExists(sess, s);
			attributes.addAll(getAttributesManagerBl().getRequiredAttributes(sess, s, facility));
		}
		List<Attribute> result = new ArrayList<Attribute>();
		Iterator<Attribute> attrIter = attributes.iterator();
		//Choose to which attributes has the principal access
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, facility)) {
				// if allowed to read, add it to result
				attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, facility));
				result.add(attrNext);
			}
		}
		return result;
	}

	@Override
	public List<Attribute> getRequiredAttributes(PerunSession sess, Service service, Resource resource) throws PrivilegeException, InternalErrorException, ResourceNotExistsException, ServiceNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		getPerunBl().getServicesManagerBl().checkServiceExists(sess, service);
		List<Attribute> attributes = getAttributesManagerBl().getRequiredAttributes(sess, service, resource);
		Iterator<Attribute> attrIter = attributes.iterator();
		//Choose to which attributes has the principal access
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, resource)) attrIter.remove();
			else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, resource));
		}
		return attributes;
	}

	@Override
	public List<Attribute> getRequiredAttributes(PerunSession sess, List<Service> services, Resource resource) throws PrivilegeException, InternalErrorException, ResourceNotExistsException, ServiceNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		for (Service s : services) getPerunBl().getServicesManagerBl().checkServiceExists(sess, s);
		List<Attribute> attributes = attributesManagerBl.getRequiredAttributes(sess, services, resource);
		Iterator<Attribute> attrIter = attributes.iterator();
		//Choose to which attributes has the principal access
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, resource)) attrIter.remove();
			else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, resource));
		}
		return attributes;
	}

	@Override
	public List<Attribute> getRequiredAttributes(PerunSession sess, Service service, Host host) throws PrivilegeException, InternalErrorException, ServiceNotExistsException, HostNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getFacilitiesManagerBl().checkHostExists(sess, host);
		getPerunBl().getServicesManagerBl().checkServiceExists(sess, service);
		List<Attribute> attributes = getAttributesManagerBl().getRequiredAttributes(sess, service, host);
		Iterator<Attribute> attrIter = attributes.iterator();
		//Choose to which attributes has the principal access
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, host)) attrIter.remove();
			else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, host));
		}
		return attributes;
	}
	@Override
	public List<Attribute> getRequiredAttributes(PerunSession sess, Service service, Member member, Resource resource) throws PrivilegeException, InternalErrorException, ResourceNotExistsException, ServiceNotExistsException, MemberNotExistsException, MemberResourceMismatchException {
		Utils.checkPerunSession(sess);
		getPerunBl().getServicesManagerBl().checkServiceExists(sess, service);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		List<Attribute> attributes = getAttributesManagerBl().getRequiredAttributes(sess, service, member, resource);
		Iterator<Attribute> attrIter = attributes.iterator();
		//Choose to which attributes has the principal access
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, member, resource)) attrIter.remove();
			else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, member, resource));
		}
		return attributes;
	}

	@Override
	public List<Attribute> getRequiredAttributes(PerunSession sess, Service service, Member member, Resource resource, boolean workWithUserAttributes) throws PrivilegeException, InternalErrorException, ResourceNotExistsException, ServiceNotExistsException, MemberNotExistsException, MemberResourceMismatchException {
		Utils.checkPerunSession(sess);
		getPerunBl().getServicesManagerBl().checkServiceExists(sess, service);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		List<Attribute> attributes = getAttributesManagerBl().getRequiredAttributes(sess, service, member, resource, workWithUserAttributes);
		Iterator<Attribute> attrIter = attributes.iterator();
		User user = null;
		Facility facility = null;
		//Choose to which attributes has the principal access
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_MEMBER_RESOURCE_ATTR)) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, member, resource)) attrIter.remove();
				else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, member, resource));
			} else if (getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_MEMBER_ATTR)){
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, member)) attrIter.remove();
				else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, member));
			} else if (getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_USER_ATTR)) {
				if (user==null) user = getPerunBl().getUsersManagerBl().getUserByMember(sess, member);
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, user)) attrIter.remove();
				else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, user));
			} else if (getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_USER_FACILITY_ATTR)){
				if (user==null) user = getPerunBl().getUsersManagerBl().getUserByMember(sess, member);
				if (facility==null) facility = getPerunBl().getResourcesManagerBl().getFacility(sess, resource);
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, user, facility)) attrIter.remove();
				else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, user, facility));
			} else {
				throw new ConsistencyErrorException("There is some attribute which is not type of any possible choice.");
			}
		}
		return attributes;
	}

	@Override
	public List<Attribute> getRequiredAttributes(PerunSession sess, Service service, Resource resource, Group group, Member member, boolean workWithUserAttributes) throws PrivilegeException, InternalErrorException, ResourceNotExistsException, ServiceNotExistsException, MemberNotExistsException, WrongAttributeAssignmentException, GroupNotExistsException, GroupResourceMismatchException, MemberResourceMismatchException {
		Utils.checkPerunSession(sess);
		getPerunBl().getServicesManagerBl().checkServiceExists(sess, service);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);

		List<Attribute> attributes = getAttributesManagerBl().getRequiredAttributes(sess, service, resource, group, member, workWithUserAttributes);

		User user = null;
		Facility facility = null;

		Iterator<Attribute> attrIter = attributes.iterator();
		//Choose to which attributes has the principal access
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_MEMBER_RESOURCE_ATTR)) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, member, resource)) attrIter.remove();
				else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, member, resource));
			} else if (getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_MEMBER_ATTR)){
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, member)) attrIter.remove();
				else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, member));
			} else if (getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_MEMBER_GROUP_ATTR)){
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, member, group)) attrIter.remove();
				else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, member, group));
			} else if (getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_USER_ATTR)) {
				if (user==null) user = getPerunBl().getUsersManagerBl().getUserByMember(sess, member);
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, user)) attrIter.remove();
				else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, user));
			} else if (getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_USER_FACILITY_ATTR)){
				if (user==null) user = getPerunBl().getUsersManagerBl().getUserByMember(sess, member);
				if (facility==null) facility = getPerunBl().getResourcesManagerBl().getFacility(sess, resource);
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, user, facility)) attrIter.remove();
				else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, user, facility));
			} else {
				throw new ConsistencyErrorException("There is some attribute which is not type of any possible choice.");
			}
		}
		return attributes;
	}

	@Override
	public HashMap<Member, List<Attribute>> getRequiredAttributes(PerunSession sess, Service service, Resource resource, List<Member> members, boolean workWithUserAttributes) throws InternalErrorException, ServiceNotExistsException, ResourceNotExistsException, MemberNotExistsException, FacilityNotExistsException, MemberResourceMismatchException {
		Utils.checkPerunSession(sess);
		getPerunBl().getServicesManagerBl().checkServiceExists(sess, service);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		Facility facility = getPerunBl().getResourcesManagerBl().getFacility(sess, resource);
		for (Member member : members) {
			getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		}
		HashMap<Member, List<Attribute>> result = getAttributesManagerBl().getRequiredAttributes(sess, service, facility, resource, members, workWithUserAttributes);
		for (Member member : result.keySet()) {
			Iterator<Attribute> attrIter = result.get(member).iterator();
			//Choose to which attributes has the principal access
					User user = getPerunBl().getUsersManagerBl().getUserByMember(sess, member);
			while (attrIter.hasNext()) {
				Attribute attrNext = attrIter.next();
				if (getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_MEMBER_RESOURCE_ATTR)) {
					if (!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, member, resource))
						attrIter.remove();
					else
					attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, member, resource));
					} else if (getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_MEMBER_ATTR)) {
					if (!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, member))
						attrIter.remove();
					else
					attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, member));
					} else if (getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_USER_ATTR)) {
					if (!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, user))
						attrIter.remove();
					else
					attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, user));
					} else if (getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_USER_FACILITY_ATTR)) {
					if (!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, user, facility))
						attrIter.remove();
					else
					attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, user, facility));
					} else {
					throw new ConsistencyErrorException("There is some attribute which is not type of any possible choice.");
					}
				}
			}
		return result;
		}

	@Override
	public HashMap<Member, List<Attribute>> getRequiredAttributes(PerunSession sess, Service service, Resource resource, List<Member> members) throws InternalErrorException, ResourceNotExistsException, ServiceNotExistsException, MemberNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getServicesManagerBl().checkServiceExists(sess, service);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		for (Member member : members) {
			getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		}
		HashMap<Member, List<Attribute>> result = getAttributesManagerBl().getRequiredAttributes(sess, service, resource, members);
		for (Member member : result.keySet()) {
			Iterator<Attribute> attrIter = result.get(member).iterator();
			//Choose to which attributes has the principal access
					while (attrIter.hasNext()) {
				Attribute attrNext = attrIter.next();
				if (getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_MEMBER_RESOURCE_ATTR)) {
					if (!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, member, resource))
						attrIter.remove();
					else
					attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, member, resource));
					} else {
					throw new ConsistencyErrorException("There is some attribute which is not type of any possible choice.");
					}
				}
			}
		return result;
	}

	@Override
	public HashMap<Member, List<Attribute>> getRequiredAttributes(PerunSession sess, Resource resource, Service service, List<Member> members) throws InternalErrorException, ServiceNotExistsException, ResourceNotExistsException, MemberNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getServicesManagerBl().checkServiceExists(sess, service);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		for (Member member : members) {
			getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		}
		HashMap<Member, List<Attribute>> result = getAttributesManagerBl().getRequiredAttributes(sess, resource, service, members);
		for (Member member : result.keySet()) {
			Iterator<Attribute> attrIter = result.get(member).iterator();
			//Choose to which attributes has the principal access
					while (attrIter.hasNext()) {
				Attribute attrNext = attrIter.next();
				if (getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_MEMBER_ATTR)) {
					if (!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, member))
						attrIter.remove();
					else
					attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, member));
					} else {
					throw new ConsistencyErrorException("There is some attribute which is not type of any possible choice.");
					}
				}
			}
		return result;
	}

	@Override
	public HashMap<User, List<Attribute>> getRequiredAttributes(PerunSession sess, Service service, Facility facility, List<User> users) throws InternalErrorException, ServiceNotExistsException, FacilityNotExistsException, UserNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getServicesManagerBl().checkServiceExists(sess, service);
		getPerunBl().getFacilitiesManagerBl().checkFacilityExists(sess, facility);
		for (User user : users) {
			getPerunBl().getUsersManagerBl().checkUserExists(sess, user);
		}
		HashMap<User, List<Attribute>> result = getAttributesManagerBl().getRequiredAttributes(sess, service, facility, users);
		for (User user : result.keySet()) {
			Iterator<Attribute> attrIter = result.get(user).iterator();
			//Choose to which attributes has the principal access
					while (attrIter.hasNext()) {
				Attribute attrNext = attrIter.next();
				if (getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_USER_FACILITY_ATTR)) {
					if (!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, user, facility))
						attrIter.remove();
					else
					attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, user, facility));
					} else {
					throw new ConsistencyErrorException("There is some attribute which is not type of any possible choice.");
					}
				}
			}
		return result;
	}

	@Override
	public HashMap<User, List<Attribute>> getRequiredAttributes(PerunSession sess, Service service, List<User> users) throws InternalErrorException, ServiceNotExistsException, UserNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getServicesManagerBl().checkServiceExists(sess, service);
		for (User user : users) {
			getPerunBl().getUsersManagerBl().checkUserExists(sess, user);
		}
		HashMap<User, List<Attribute>> result = getAttributesManagerBl().getRequiredAttributes(sess, service, users);
		for (User user : result.keySet()) {
			Iterator<Attribute> attrIter = result.get(user).iterator();
			//Choose to which attributes has the principal access
			while (attrIter.hasNext()) {
				Attribute attrNext = attrIter.next();
				if (getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_USER_ATTR)) {
					if (!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, user))
						attrIter.remove();
					else
						attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, user));
				} else {
					throw new ConsistencyErrorException("There is some attribute which is not type of any possible choice.");
				}
			}
		}
		return result;
	}

	@Override
	public List<Attribute> getRequiredAttributes(PerunSession sess, Service service, Member member, Group group) throws PrivilegeException, InternalErrorException, ServiceNotExistsException, MemberNotExistsException, GroupNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getServicesManagerBl().checkServiceExists(sess, service);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);

		List<Attribute> attributes = getAttributesManagerBl().getRequiredAttributes(sess, service, member, group);
		Iterator<Attribute> attrIter = attributes.iterator();
		//Choose to which attributes has the principal access
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, member, group)) attrIter.remove();
			else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, member, group));
		}

		return attributes;
	}

	@Override
	public List<Attribute> getRequiredAttributes(PerunSession sess, Service service, Member member, Group group, boolean workWithUserAttributes) throws PrivilegeException, WrongAttributeAssignmentException, InternalErrorException, ResourceNotExistsException, ServiceNotExistsException, MemberNotExistsException, GroupNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getServicesManagerBl().checkServiceExists(sess, service);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);

		List<Attribute> attributes = getAttributesManagerBl().getRequiredAttributes(sess, service, member, group, workWithUserAttributes);
		Iterator<Attribute> attrIter = attributes.iterator();
		//Choose to which attributes has the principal access
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_MEMBER_GROUP_ATTR)) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, member, group)) attrIter.remove();
				else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, member, group));
			} else if (getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_MEMBER_ATTR)){
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, member)) attrIter.remove();
				else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, member));
			} else if (getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_USER_ATTR)) {
				User user = getPerunBl().getUsersManagerBl().getUserByMember(sess, member);
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, user)) attrIter.remove();
				else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, user));
			} else {
				throw new ConsistencyErrorException("There is some attribute which is not type of any possible choice.");
			}
		}
		return attributes;
	}

	@Override
	public List<Attribute> getRequiredAttributes(PerunSession sess, Service service, Member member) throws PrivilegeException, InternalErrorException, ServiceNotExistsException, MemberNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getServicesManagerBl().checkServiceExists(sess, service);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		List<Attribute> attributes = getAttributesManagerBl().getRequiredAttributes(sess, service, member);
		Iterator<Attribute> attrIter = attributes.iterator();
		//Choose to which attributes has the principal access
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, member)) attrIter.remove();
			else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, member));
		}
		return attributes;
	}

	@Override
	public List<Attribute> getRequiredAttributes(PerunSession sess, Service service, Resource resource, Group group, boolean workWithGroupAttributes) throws PrivilegeException, InternalErrorException, ServiceNotExistsException, ResourceNotExistsException, GroupNotExistsException, GroupResourceMismatchException {
		Utils.checkPerunSession(sess);
		getPerunBl().getServicesManagerBl().checkServiceExists(sess, service);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);
		List<Attribute> attributes = getAttributesManagerBl().getRequiredAttributes(sess, service, resource, group, workWithGroupAttributes);
		Iterator<Attribute> attrIter = attributes.iterator();
		//Choose to which attributes has the principal access
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_GROUP_RESOURCE_ATTR)) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, group, resource)) attrIter.remove();
				else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, group, resource));
			} else if (getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_GROUP_ATTR)){
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, group)) attrIter.remove();
				else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, group));
			} else {
				throw new ConsistencyErrorException("There is some attribute which is not type of any possible choice.");
			}
		}
		return attributes;
	}

	@Override
	public List<Attribute> getRequiredAttributes(PerunSession sess, Service service, Resource resource, Group group) throws PrivilegeException, InternalErrorException, ServiceNotExistsException, ResourceNotExistsException, GroupNotExistsException, GroupResourceMismatchException, WrongAttributeAssignmentException {
		Utils.checkPerunSession(sess);
		getPerunBl().getServicesManagerBl().checkServiceExists(sess, service);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);
		if(!getPerunBl().getGroupsManagerBl().getVo(sess, group).equals(getPerunBl().getResourcesManagerBl().getVo(sess, resource))) {
			throw new GroupResourceMismatchException("group and resource are not in the same VO");
		}
		List<Attribute> attributes = getAttributesManagerBl().getRequiredAttributes(sess, service, resource, group);
		Iterator<Attribute> attrIter = attributes.iterator();
		//Choose to which attributes has the principal access
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, group, resource)) attrIter.remove();
			else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, group, resource));
		}
		return attributes;
	}

	@Override
	public List<Attribute> getRequiredAttributes(PerunSession sess, Service service, Group group) throws PrivilegeException, InternalErrorException, ServiceNotExistsException, GroupNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getServicesManagerBl().checkServiceExists(sess, service);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);
		List<Attribute> attributes = getAttributesManagerBl().getRequiredAttributes(sess, service, group);
		Iterator<Attribute> attrIter = attributes.iterator();
		//Choose to which attributes has the principal access
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, group)) attrIter.remove();
			else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, group));
		}
		return attributes;
	}


	@Override
	public Attribute fillAttribute(PerunSession sess, Resource resource, Attribute attribute) throws PrivilegeException, InternalErrorException, ResourceNotExistsException, AttributeNotExistsException, WrongAttributeAssignmentException {
		Utils.checkPerunSession(sess);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		//Choose to which attributes has the principal access
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attribute), resource )) throw new PrivilegeException("Principal has no access to fill attribute = " + new AttributeDefinition(attribute));

		Attribute attr = getAttributesManagerBl().fillAttribute(sess, resource, attribute);
		attr.setWritable(true);
		return attr;
	}

	@Override
	public List<Attribute> fillAttributes(PerunSession sess, Resource resource, List<Attribute> attributes) throws PrivilegeException, InternalErrorException, ResourceNotExistsException, AttributeNotExistsException, WrongAttributeAssignmentException {
		Utils.checkPerunSession(sess);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		//Choose to which attributes has the principal access

		List<Attribute> listOfAttributes = getAttributesManagerBl().fillAttributes(sess, resource, attributes);
		Iterator<Attribute> attrIter = listOfAttributes.iterator();
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attrNext), resource )) attrIter.remove();
			else attrNext.setWritable(true);
		}

		return listOfAttributes;
	}

	@Override
	public Attribute fillAttribute(PerunSession sess, Member member, Resource resource, Attribute attribute) throws PrivilegeException, InternalErrorException, ResourceNotExistsException, MemberNotExistsException, AttributeNotExistsException, WrongAttributeAssignmentException, MemberResourceMismatchException {
		Utils.checkPerunSession(sess);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		//Choose to which attributes has the principal access
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attribute), member , resource)) throw new PrivilegeException("Principal has no access to fill attribute = " + new AttributeDefinition(attribute));

		Attribute attr = getAttributesManagerBl().fillAttribute(sess, member, resource, attribute);
		attr.setWritable(true);
		return attr;
	}

	@Override
	public List<Attribute> fillAttributes(PerunSession sess, Member member, Resource resource, List<Attribute> attributes) throws PrivilegeException, InternalErrorException, ResourceNotExistsException, MemberNotExistsException, AttributeNotExistsException, WrongAttributeAssignmentException, MemberResourceMismatchException {
		Utils.checkPerunSession(sess);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		getAttributesManagerBl().checkAttributesExists(sess, attributes);
		//Choose to which attributes has the principal access
		List<Attribute> listOfAttributes = getAttributesManagerBl().fillAttributes(sess, member, resource, attributes);
		Iterator<Attribute> attrIter = listOfAttributes.iterator();
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attrNext), member , resource)) attrIter.remove();
			else attrNext.setWritable(true);
		}

		return listOfAttributes;
	}

	@Override
	public List<Attribute> fillAttributes(PerunSession sess, Member member, Resource resource, List<Attribute> attributes, boolean workWithUserAttributes) throws PrivilegeException, InternalErrorException, ResourceNotExistsException, MemberNotExistsException, AttributeNotExistsException, WrongAttributeAssignmentException, MemberResourceMismatchException {
		Utils.checkPerunSession(sess);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		getAttributesManagerBl().checkAttributesExists(sess, attributes);
		//Choose to which attributes has the principal access
		List<Attribute> listOfAttributes = getAttributesManagerBl().fillAttributes(sess, member, resource, attributes, workWithUserAttributes);
		Iterator<Attribute> attrIter = listOfAttributes.iterator();

		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_MEMBER_ATTR)) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attrNext), member)) attrIter.remove();
				else attrNext.setWritable(true);
			} else if(getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_MEMBER_RESOURCE_ATTR)) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attrNext), member , resource)) attrIter.remove();
				else attrNext.setWritable(true);
			} else if(getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_USER_ATTR)) {
				User u = getPerunBl().getUsersManagerBl().getUserByMember(sess, member);
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attrNext), u)) attrIter.remove();
				else attrNext.setWritable(true);
			} else if(getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_USER_FACILITY_ATTR)) {
				User u = getPerunBl().getUsersManagerBl().getUserByMember(sess, member);
				Facility f = getPerunBl().getResourcesManagerBl().getFacility(sess, resource);
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attrNext), u, f)) attrIter.remove();
				else attrNext.setWritable(true);
			} else {
				throw new ConsistencyErrorException("There is some attribute which is not type of any possible choice.");
			}
		}

		return listOfAttributes;
	}

	@Override
	public Attribute fillAttribute(PerunSession sess, Member member, Group group, Attribute attribute) throws PrivilegeException, InternalErrorException, MemberNotExistsException, GroupNotExistsException, AttributeNotExistsException, WrongAttributeAssignmentException {
		Utils.checkPerunSession(sess);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);
		//Choose to which attributes has the principal access
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attribute), member, group)) throw new PrivilegeException("Principal has no access to fill attribute = " + new AttributeDefinition(attribute));

		Attribute attr = getAttributesManagerBl().fillAttribute(sess, member, group, attribute);
		attr.setWritable(true);
		return attr;
	}

	@Override
	public List<Attribute> fillAttributes(PerunSession sess, Member member, Group group, List<Attribute> attributes) throws PrivilegeException, InternalErrorException, MemberNotExistsException, GroupNotExistsException, AttributeNotExistsException, WrongAttributeAssignmentException {
		Utils.checkPerunSession(sess);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);
		getAttributesManagerBl().checkAttributesExists(sess, attributes);
		//Choose to which attributes has the principal access
		List<Attribute> listOfAttributes = getAttributesManagerBl().fillAttributes(sess, member, group, attributes);
		Iterator<Attribute> attrIter = listOfAttributes.iterator();
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attrNext), member, group)) attrIter.remove();
			else attrNext.setWritable(true);
		}

		return listOfAttributes;
	}

	@Override
	public List<Attribute> fillAttributes(PerunSession sess, Member member, Group group, List<Attribute> attributes, boolean workWithUserAttributes) throws PrivilegeException, InternalErrorException, ResourceNotExistsException, MemberNotExistsException, AttributeNotExistsException, WrongAttributeAssignmentException, GroupNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);
		getAttributesManagerBl().checkAttributesExists(sess, attributes);
		// Choose to which attributes has the principal access
		List<Attribute> listOfAttributes = getAttributesManagerBl().fillAttributes(sess, member, group, attributes, workWithUserAttributes);
		Iterator<Attribute> attrIter = listOfAttributes.iterator();

		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_MEMBER_GROUP_ATTR)) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attrNext), member, group)) attrIter.remove();
				else attrNext.setWritable(true);
			} else if(getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_USER_ATTR)) {
				User u = getPerunBl().getUsersManagerBl().getUserByMember(sess, member);
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attrNext), u)) attrIter.remove();
				else attrNext.setWritable(true);
			} else if(getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_MEMBER_ATTR)) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attrNext), member)) attrIter.remove();
				else attrNext.setWritable(true);
			} else {
				throw new ConsistencyErrorException("There is some attribute which is not type of any possible choice.");
			}
		}

		return listOfAttributes;
	}

	@Override
	public List<Attribute> fillAttributes(PerunSession sess, Facility facility, Resource resource, User user, Member member, List<Attribute> attributes) throws PrivilegeException, InternalErrorException, ResourceNotExistsException, MemberNotExistsException, FacilityNotExistsException, UserNotExistsException, AttributeNotExistsException, WrongAttributeAssignmentException, MemberResourceMismatchException {
		Utils.checkPerunSession(sess);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		getPerunBl().getFacilitiesManagerBl().checkFacilityExists(sess, facility);
		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);
		getAttributesManagerBl().checkAttributesExists(sess, attributes);
		//Choose to which attributes has the principal access
		List<Attribute> listOfAttributes = getAttributesManagerBl().fillAttributes(sess, facility, resource, user, member, attributes);
		Iterator<Attribute> attrIter = listOfAttributes.iterator();

		while (attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if (getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_MEMBER_ATTR)) {
				if (!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attrNext), member)) {
					attrIter.remove();
				} else {
					attrNext.setWritable(true);
				}
			} else if (getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_MEMBER_RESOURCE_ATTR)) {
				if (!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attrNext), member, resource)) {
					attrIter.remove();
				} else {
					attrNext.setWritable(true);
				}
			} else if (getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_USER_ATTR)) {
				if (!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attrNext), user)) {
					attrIter.remove();
				} else {
					attrNext.setWritable(true);
				}
			} else if (getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_USER_FACILITY_ATTR)) {
				if (!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attrNext), user, facility)) {
					attrIter.remove();
				} else {
					attrNext.setWritable(true);
				}
			} else {
				throw new ConsistencyErrorException("There is some attribute which is not type of any possible choice.");
			}
		}

		return listOfAttributes;
	}

	@Override
	public Attribute fillAttribute(PerunSession sess, Member member, Attribute attribute) throws PrivilegeException, InternalErrorException, MemberNotExistsException, AttributeNotExistsException, WrongAttributeAssignmentException {
		Utils.checkPerunSession(sess);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		getAttributesManagerBl().checkAttributeExists(sess, attribute);
		//Choose to which attributes has the principal access
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attribute), member )) throw new PrivilegeException("Principal has no access to fill attribute = " + new AttributeDefinition(attribute));

		Attribute attr = getAttributesManagerBl().fillAttribute(sess, member, attribute);
		attr.setWritable(true);
		return attr;
	}

	@Override
	public List<Attribute> fillAttributes(PerunSession sess, Member member, List<Attribute> attributes) throws PrivilegeException, InternalErrorException, MemberNotExistsException, AttributeNotExistsException, WrongAttributeAssignmentException {
		Utils.checkPerunSession(sess);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		getAttributesManagerBl().checkAttributesExists(sess, attributes);
		//Choose to which attributes has the principal access
		List<Attribute> listOfAttributes = getAttributesManagerBl().fillAttributes(sess, member, attributes);
		Iterator<Attribute> attrIter = listOfAttributes.iterator();
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attrNext),member )) attrIter.remove();
			else attrNext.setWritable(true);
		}

		return listOfAttributes;
	}

	@Override
	public Attribute fillAttribute(PerunSession sess, Facility facility, User user, Attribute attribute) throws PrivilegeException, InternalErrorException, FacilityNotExistsException, UserNotExistsException, AttributeNotExistsException, WrongAttributeAssignmentException {
		Utils.checkPerunSession(sess);
		getPerunBl().getFacilitiesManagerBl().checkFacilityExists(sess, facility);
		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);
		getAttributesManagerBl().checkAttributeExists(sess, attribute);
		//Choose to which attributes has the principal access
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attribute), user , facility)) throw new PrivilegeException("Principal has no access to fill attribute = " + new AttributeDefinition(attribute));

		Attribute attr = getAttributesManagerBl().fillAttribute(sess, facility, user, attribute);
		attr.setWritable(true);
		return attr;
	}

	@Override
	public List<Attribute> fillAttributes(PerunSession sess, Facility facility, User user, List<Attribute> attributes) throws PrivilegeException, InternalErrorException, FacilityNotExistsException, UserNotExistsException, AttributeNotExistsException, WrongAttributeAssignmentException {
		Utils.checkPerunSession(sess);
		getPerunBl().getFacilitiesManagerBl().checkFacilityExists(sess, facility);
		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);
		getAttributesManagerBl().checkAttributesExists(sess, attributes);
		//Choose to which attributes has the principal access
		List<Attribute> listOfAttributes = getAttributesManagerBl().fillAttributes(sess, facility, user, attributes);
		Iterator<Attribute> attrIter = listOfAttributes.iterator();
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attrNext), user, facility)) attrIter.remove();
			else attrNext.setWritable(true);
		}

		return listOfAttributes;
	}

	@Override
	public Attribute fillAttribute(PerunSession sess, User user, Attribute attribute) throws PrivilegeException, InternalErrorException, UserNotExistsException, AttributeNotExistsException, WrongAttributeAssignmentException {
		Utils.checkPerunSession(sess);
		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);
		getAttributesManagerBl().checkAttributeExists(sess, attribute);
		//Choose to which attributes has the principal access
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attribute), user )) throw new PrivilegeException("Principal has no access to fill attribute = " + new AttributeDefinition(attribute));

		Attribute attr = getAttributesManagerBl().fillAttribute(sess, user, attribute);
		attr.setWritable(true);
		return attr;
	}

	@Override
	public List<Attribute> fillAttributes(PerunSession sess, User user, List<Attribute> attributes) throws PrivilegeException, InternalErrorException, UserNotExistsException, AttributeNotExistsException, WrongAttributeAssignmentException {
		Utils.checkPerunSession(sess);
		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);
		getAttributesManagerBl().checkAttributesExists(sess, attributes);
		//Choose to which attributes has the principal access
		List<Attribute> listOfAttributes = getAttributesManagerBl().fillAttributes(sess, user, attributes);
		Iterator<Attribute> attrIter = listOfAttributes.iterator();
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attrNext),user )) attrIter.remove();
			else attrNext.setWritable(true);
		}

		return listOfAttributes;
	}

	@Override
	public Attribute fillAttribute(PerunSession sess, Resource resource, Group group, Attribute attribute) throws PrivilegeException, InternalErrorException, ResourceNotExistsException, GroupNotExistsException, AttributeNotExistsException, WrongAttributeAssignmentException, GroupResourceMismatchException {
		Utils.checkPerunSession(sess);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);
		getAttributesManagerBl().checkAttributeExists(sess, attribute);
		if(!getPerunBl().getGroupsManagerBl().getVo(sess, group).equals(getPerunBl().getResourcesManagerBl().getVo(sess, resource))) {
			throw new GroupResourceMismatchException("group and resource are not in the same VO");
		}
		//Choose to which attributes has the principal access
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attribute), group , resource)) throw new PrivilegeException("Principal has no access to fill attribute = " + new AttributeDefinition(attribute));

		Attribute attr = getAttributesManagerBl().fillAttribute(sess, resource, group, attribute);
		attr.setWritable(true);
		return attr;
	}

	@Override
	public List<Attribute> fillAttributes(PerunSession sess, Resource resource, Group group, List<Attribute> attributes) throws PrivilegeException, InternalErrorException, ResourceNotExistsException, GroupNotExistsException, AttributeNotExistsException, WrongAttributeAssignmentException, GroupResourceMismatchException {
		Utils.checkPerunSession(sess);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);
		getAttributesManagerBl().checkAttributesExists(sess, attributes);
		if(!getPerunBl().getGroupsManagerBl().getVo(sess, group).equals(getPerunBl().getResourcesManagerBl().getVo(sess, resource))) {
			throw new GroupResourceMismatchException("group and resource are not in the same VO");
		}
		//Choose to which attributes has the principal access
		List<Attribute> listOfAttributes = getAttributesManagerBl().fillAttributes(sess, resource, group, attributes);
		Iterator<Attribute> attrIter = listOfAttributes.iterator();
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attrNext),group ,resource)) attrIter.remove();
			else attrNext.setWritable(true);
		}

		return listOfAttributes;
	}

	public List<Attribute> fillAttributes(PerunSession sess, Resource resource, Group group, List<Attribute> attributes, boolean workWithGroupAttributes) throws PrivilegeException, InternalErrorException, ResourceNotExistsException, GroupNotExistsException, AttributeNotExistsException, WrongAttributeAssignmentException, GroupResourceMismatchException {
		Utils.checkPerunSession(sess);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);
		getAttributesManagerBl().checkAttributesExists(sess, attributes);
		if(!getPerunBl().getGroupsManagerBl().getVo(sess, group).equals(getPerunBl().getResourcesManagerBl().getVo(sess, resource))) {
			throw new GroupResourceMismatchException("group and resource are not in the same VO");
		}
		//Choose to which attributes has the principal access

		List<Attribute> listOfAttributes = getAttributesManagerBl().fillAttributes(sess, resource, group, attributes,workWithGroupAttributes);
		Iterator<Attribute> attrIter = listOfAttributes.iterator();

		while (attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if (getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_GROUP_ATTR)) {
				if (!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attrNext), group)) {
					attrIter.remove();
				} else {
					attrNext.setWritable(true);
				}
			} else if (getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_GROUP_RESOURCE_ATTR)) {
				if (!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attrNext), group, resource)) {
					attrIter.remove();
				} else {
					attrNext.setWritable(true);
				}
			} else {
				throw new ConsistencyErrorException("There is some attribute which is not type of any possible choice.");
			}
		}

		return listOfAttributes;
	}

	@Override
	public Attribute fillAttribute(PerunSession sess, Host host, Attribute attribute) throws PrivilegeException, InternalErrorException, HostNotExistsException, AttributeNotExistsException, WrongAttributeAssignmentException {
		Utils.checkPerunSession(sess);
		getPerunBl().getFacilitiesManagerBl().checkHostExists(sess, host);
		getAttributesManagerBl().checkAttributeExists(sess, attribute);
		//Choose to which attributes has the principal access
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attribute), host )) throw new PrivilegeException("Principal has no access to fill attribute = " + new AttributeDefinition(attribute));

		Attribute attr = getAttributesManagerBl().fillAttribute(sess, host, attribute);
		attr.setWritable(true);
		return attr;
	}

	@Override
	public List<Attribute> fillAttributes(PerunSession sess, Host host, List<Attribute> attributes) throws PrivilegeException, InternalErrorException, HostNotExistsException, AttributeNotExistsException, WrongAttributeAssignmentException {
		Utils.checkPerunSession(sess);
		getPerunBl().getFacilitiesManagerBl().checkHostExists(sess, host);
		getAttributesManagerBl().checkAttributesExists(sess, attributes);
		//Choose to which attributes has the principal access
		List<Attribute> listOfAttributes = getAttributesManagerBl().fillAttributes(sess, host, attributes);
		Iterator<Attribute> attrIter = listOfAttributes.iterator();
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attrNext), host)) attrIter.remove();
			else attrNext.setWritable(true);
		}

		return listOfAttributes;
	}

	@Override
	public Attribute fillAttribute(PerunSession sess, Group group, Attribute attribute) throws PrivilegeException, InternalErrorException, GroupNotExistsException, AttributeNotExistsException, WrongAttributeAssignmentException {
		Utils.checkPerunSession(sess);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);
		getAttributesManagerBl().checkAttributeExists(sess, attribute);
		//Choose to which attributes has the principal access
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attribute), group )) throw new PrivilegeException("Principal has no access to fill attribute = " + new AttributeDefinition(attribute));

		Attribute attr = getAttributesManagerBl().fillAttribute(sess, group, attribute);
		attr.setWritable(true);
		return attr;
	}

	@Override
	public List<Attribute> fillAttributes(PerunSession sess, Group group, List<Attribute> attributes) throws PrivilegeException, InternalErrorException, GroupNotExistsException, AttributeNotExistsException, WrongAttributeAssignmentException {
		Utils.checkPerunSession(sess);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);
		getAttributesManagerBl().checkAttributesExists(sess, attributes);
		//Choose to which attributes has the principal access
		List<Attribute> listOfAttributes = getAttributesManagerBl().fillAttributes(sess, group, attributes);
		Iterator<Attribute> attrIter = listOfAttributes.iterator();
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attrNext),group)) attrIter.remove();
			else attrNext.setWritable(true);
		}

		return listOfAttributes;
	}

	@Override
	public Attribute fillAttribute(PerunSession sess, UserExtSource ues, Attribute attribute) throws PrivilegeException, InternalErrorException, UserExtSourceNotExistsException, AttributeNotExistsException, WrongAttributeAssignmentException {
		Utils.checkPerunSession(sess);
		getPerunBl().getUsersManagerBl().checkUserExtSourceExists(sess, ues);
		getAttributesManagerBl().checkAttributeExists(sess, attribute);
		//Choose to which attributes has the principal access
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attribute), ues)) throw new PrivilegeException("Principal has no access to fill attribute = " + new AttributeDefinition(attribute));

		Attribute attr = getAttributesManagerBl().fillAttribute(sess, ues, attribute);
		attr.setWritable(true);
		return attr;
	}

	@Override
	public List<Attribute> fillAttributes(PerunSession sess, UserExtSource ues, List<Attribute> attributes) throws PrivilegeException, InternalErrorException, UserExtSourceNotExistsException, AttributeNotExistsException, WrongAttributeAssignmentException {
		Utils.checkPerunSession(sess);
		getPerunBl().getUsersManagerBl().checkUserExtSourceExists(sess, ues);
		getAttributesManagerBl().checkAttributesExists(sess, attributes);
		//Choose to which attributes has the principal access
		List<Attribute> listOfAttributes = getAttributesManagerBl().fillAttributes(sess, ues, attributes);
		Iterator<Attribute> attrIter = listOfAttributes.iterator();
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attrNext), ues)) attrIter.remove();
			else attrNext.setWritable(true);
		}

		return listOfAttributes;
	}

	@Override
	public void checkAttributeValue(PerunSession sess, Facility facility, Attribute attribute) throws PrivilegeException, InternalErrorException, FacilityNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException, WrongReferenceAttributeValueException, AttributeNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getFacilitiesManagerBl().checkFacilityExists(sess, facility);
		getAttributesManagerBl().checkAttributeExists(sess, attribute);
		//Choose to which attributes has the principal access
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attribute), facility )) throw new PrivilegeException("Principal has no access to check attribute = " + new AttributeDefinition(attribute));

		getAttributesManagerBl().checkAttributeValue(sess, facility, attribute);
	}

	@Override
	public void checkAttributesValue(PerunSession sess, Facility facility, List<Attribute> attributes) throws PrivilegeException, InternalErrorException, FacilityNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException, AttributeNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getFacilitiesManagerBl().checkFacilityExists(sess, facility);
		getAttributesManagerBl().checkAttributesExists(sess, attributes);
		//Choose to which attributes has the principal access
		for(Attribute attr: attributes) {
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attr), facility)) throw new PrivilegeException("Principal has no access to check attribute = " + new AttributeDefinition(attr));
		}
		getAttributesManagerBl().checkAttributesValue(sess, facility, attributes);
	}

	@Override
	public void checkAttributeValue(PerunSession sess, Vo vo, Attribute attribute) throws PrivilegeException, InternalErrorException, VoNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException, AttributeNotExistsException {
		Utils.checkPerunSession(sess);
		getAttributesManagerBl().checkAttributeExists(sess, attribute);
		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);
		//Choose to which attributes has the principal access
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attribute), vo)) throw new PrivilegeException("Principal has no access to check attribute = " + new AttributeDefinition(attribute));

		getAttributesManagerBl().checkAttributeValue(sess, vo, attribute);
	}

	@Override
	public void checkAttributesValue(PerunSession sess, Vo vo, List<Attribute> attributes) throws PrivilegeException, InternalErrorException, VoNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException, AttributeNotExistsException {
		Utils.checkPerunSession(sess);
		getAttributesManagerBl().checkAttributesExists(sess, attributes);
		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);
		//Choose to which attributes has the principal access
		for(Attribute attr: attributes) {
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attr), vo)) throw new PrivilegeException("Principal has no access to check attribute = " + new AttributeDefinition(attr));
		}
		getAttributesManagerBl().checkAttributesValue(sess, vo, attributes);
	}

	@Override
	public void checkAttributeValue(PerunSession sess, Resource resource, Attribute attribute) throws PrivilegeException, InternalErrorException, ResourceNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException, AttributeNotExistsException {
		Utils.checkPerunSession(sess);
		getAttributesManagerBl().checkAttributeExists(sess, attribute);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		//Choose to which attributes has the principal access
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attribute), resource)) throw new PrivilegeException("Principal has no access to check attribute = " + new AttributeDefinition(attribute));

		getAttributesManagerBl().checkAttributeValue(sess, resource, attribute);
	}

	@Override
	public void checkAttributesValue(PerunSession sess, Resource resource, List<Attribute> attributes) throws PrivilegeException, InternalErrorException, ResourceNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException, AttributeNotExistsException {
		Utils.checkPerunSession(sess);
		getAttributesManagerBl().checkAttributesExists(sess, attributes);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		//Choose to which attributes has the principal access
		for(Attribute attr: attributes) {
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attr), resource )) throw new PrivilegeException("Principal has no access to check attribute = " + new AttributeDefinition(attr));
		}
		getAttributesManagerBl().checkAttributesValue(sess, resource, attributes);
	}

	@Override
	public void checkAttributeValue(PerunSession sess, Member member, Resource resource, Attribute attribute) throws PrivilegeException, InternalErrorException, ResourceNotExistsException, MemberNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException, AttributeNotExistsException, MemberResourceMismatchException {
		Utils.checkPerunSession(sess);
		getAttributesManagerBl().checkAttributeExists(sess, attribute);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		//Choose to which attributes has the principal access
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attribute), member , resource)) throw new PrivilegeException("Principal has no access to check attribute = " + new AttributeDefinition(attribute));

		getAttributesManagerBl().checkAttributeValue(sess, member, resource, attribute);
	}

	@Override
	public void checkAttributesValue(PerunSession sess, Member member, Resource resource, List<Attribute> attributes) throws PrivilegeException, InternalErrorException, ResourceNotExistsException, MemberNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException, AttributeNotExistsException, MemberResourceMismatchException {
		Utils.checkPerunSession(sess);
		getAttributesManagerBl().checkAttributesExists(sess, attributes);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		//Choose to which attributes has the principal access
		for(Attribute attr: attributes) {
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attr), member , resource)) throw new PrivilegeException("Principal has no access to check attribute = " + new AttributeDefinition(attr));
		}
		getAttributesManagerBl().checkAttributesValue(sess, member, resource, attributes);
	}

	@Override
	public void checkAttributeValue(PerunSession sess, Member member, Group group, Attribute attribute) throws PrivilegeException, InternalErrorException, GroupNotExistsException, MemberNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException, AttributeNotExistsException {
		Utils.checkPerunSession(sess);
		getAttributesManagerBl().checkAttributeExists(sess, attribute);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);
		//Choose to which attributes has the principal access
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attribute), member, group)) throw new PrivilegeException("Principal has no access to check attribute = " + new AttributeDefinition(attribute));

		getAttributesManagerBl().checkAttributeValue(sess, member, group, attribute);
	}

	@Override
	public void checkAttributesValue(PerunSession sess, Member member, Group group, List<Attribute> attributes) throws PrivilegeException, InternalErrorException, MemberNotExistsException, GroupNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException, AttributeNotExistsException {
		Utils.checkPerunSession(sess);
		getAttributesManagerBl().checkAttributesExists(sess, attributes);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);
		//Choose to which attributes has the principal access
		for(Attribute attribute: attributes) {
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attribute), member, group)) throw new PrivilegeException("Principal has no access to check attribute = " + new AttributeDefinition(attribute));
		}
		getAttributesManagerBl().checkAttributesValue(sess, member, group, attributes);
	}

	@Override
	public void checkAttributesValue(PerunSession sess, Member member, Group group, List<Attribute> attributes, boolean workWithUserAttributes) throws PrivilegeException, InternalErrorException, ResourceNotExistsException, MemberNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException, AttributeNotExistsException, GroupNotExistsException {
		Utils.checkPerunSession(sess);
		getAttributesManagerBl().checkAttributesExists(sess, attributes);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);
		//Choose to which attributes has the principal access
		for(Attribute attr: attributes) {
			if(getAttributesManagerBl().isFromNamespace(sess, attr, NS_MEMBER_GROUP_ATTR)) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attr), member , group)) throw new PrivilegeException("Principal has no access to check attribute = " + new AttributeDefinition(attr));
			} else if(getAttributesManagerBl().isFromNamespace(sess, attr, NS_USER_ATTR)) {
				User user = getPerunBl().getUsersManagerBl().getUserByMember(sess, member);
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attr), user)) throw new PrivilegeException("Principal has no access to check attribute = " + new AttributeDefinition(attr));
			} else if(getAttributesManagerBl().isFromNamespace(sess, attr, NS_MEMBER_ATTR)) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attr), member)) throw new PrivilegeException("Principal has no access to check attribute = " + new AttributeDefinition(attr));
			} else {
				throw new WrongAttributeAssignmentException("There is some attribute which is not type of any possible choice.");
			}
		}
		getAttributesManagerBl().checkAttributesValue(sess, member, group, attributes, workWithUserAttributes);
	}

	@Override
	public void checkAttributesValue(PerunSession sess, Member member, Resource resource, List<Attribute> attributes, boolean workWithUserAttributes) throws PrivilegeException, InternalErrorException, ResourceNotExistsException, MemberNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException, AttributeNotExistsException, MemberResourceMismatchException {
		Utils.checkPerunSession(sess);
		getAttributesManagerBl().checkAttributesExists(sess, attributes);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		//Choose to which attributes has the principal access
		for(Attribute attr: attributes) {
			if(getAttributesManagerBl().isFromNamespace(sess, attr, NS_MEMBER_ATTR)) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attr), member )) throw new PrivilegeException("Principal has no access to check attribute = " + new AttributeDefinition(attr));
			} else if(getAttributesManagerBl().isFromNamespace(sess, attr, NS_MEMBER_RESOURCE_ATTR)) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attr), member , resource)) throw new PrivilegeException("Principal has no access to check attribute = " + new AttributeDefinition(attr));
			} else if(getAttributesManagerBl().isFromNamespace(sess, attr, NS_USER_ATTR)) {
				User u = getPerunBl().getUsersManagerBl().getUserByMember(sess, member);
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attr), u )) throw new PrivilegeException("Principal has no access to check attribute = " + new AttributeDefinition(attr));
			} else if(getAttributesManagerBl().isFromNamespace(sess, attr, NS_USER_FACILITY_ATTR)) {
				User u = getPerunBl().getUsersManagerBl().getUserByMember(sess, member);
				Facility f = getPerunBl().getResourcesManagerBl().getFacility(sess, resource);
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attr), u , f)) throw new PrivilegeException("Principal has no access to check attribute = " + new AttributeDefinition(attr));
			} else {
				throw new WrongAttributeAssignmentException("There is some attribute which is not type of any possible choice.");
			}
		}
		getAttributesManagerBl().checkAttributesValue(sess, member, resource, attributes, workWithUserAttributes);
	}

	@Override
	public void checkAttributesValue(PerunSession sess, Facility facility, Resource resource, User user, Member member, List<Attribute> attributes) throws PrivilegeException, InternalErrorException, ResourceNotExistsException, MemberNotExistsException, FacilityNotExistsException, UserNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException, AttributeNotExistsException, MemberResourceMismatchException {
		Utils.checkPerunSession(sess);
		getAttributesManagerBl().checkAttributesExists(sess, attributes);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		getPerunBl().getFacilitiesManagerBl().checkFacilityExists(sess, facility);
		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);
		//Choose to which attributes has the principal access
		for(Attribute attr: attributes) {
			if(getAttributesManagerBl().isFromNamespace(sess, attr, NS_MEMBER_ATTR)) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attr), member )) throw new PrivilegeException("Principal has no access to check attribute = " + new AttributeDefinition(attr));
			} else if(getAttributesManagerBl().isFromNamespace(sess, attr, NS_MEMBER_RESOURCE_ATTR)) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attr), member , resource)) throw new PrivilegeException("Principal has no access to check attribute = " + new AttributeDefinition(attr));
			} else if(getAttributesManagerBl().isFromNamespace(sess, attr, NS_USER_ATTR)) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attr), user )) throw new PrivilegeException("Principal has no access to check attribute = " + new AttributeDefinition(attr));
			} else if(getAttributesManagerBl().isFromNamespace(sess, attr, NS_USER_FACILITY_ATTR)) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attr), user , facility)) throw new PrivilegeException("Principal has no access to check attribute = " + new AttributeDefinition(attr));
			} else {
				throw new WrongAttributeAssignmentException("There is some attribute which is not type of any possible choice.");
			}
		}
		getAttributesManagerBl().checkAttributesValue(sess, facility, resource, user, member, attributes);
	}

	@Override
	public void checkAttributeValue(PerunSession sess, Member member, Attribute attribute) throws PrivilegeException, InternalErrorException, MemberNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException, AttributeNotExistsException {
		Utils.checkPerunSession(sess);
		getAttributesManagerBl().checkAttributeExists(sess, attribute);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		//Choose to which attributes has the principal access
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attribute), member )) throw new PrivilegeException("Principal has no access to check attribute = " + new AttributeDefinition(attribute));

		getAttributesManagerBl().checkAttributeValue(sess, member, attribute);
	}

	@Override
	public void checkAttributesValue(PerunSession sess, Member member, List<Attribute> attributes) throws PrivilegeException, InternalErrorException, MemberNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException, AttributeNotExistsException {
		Utils.checkPerunSession(sess);
		getAttributesManagerBl().checkAttributesExists(sess, attributes);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		//Choose to which attributes has the principal access
		for(Attribute attr: attributes) {
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attr), member )) throw new PrivilegeException("Principal has no access to check attribute = " + new AttributeDefinition(attr));
		}
		getAttributesManagerBl().checkAttributesValue(sess, member, attributes);
	}

	@Override
	public void checkAttributeValue(PerunSession sess, Facility facility, User user, Attribute attribute) throws PrivilegeException, InternalErrorException, FacilityNotExistsException, UserNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException, AttributeNotExistsException {
		Utils.checkPerunSession(sess);
		getAttributesManagerBl().checkAttributeExists(sess, attribute);
		getPerunBl().getFacilitiesManagerBl().checkFacilityExists(sess, facility);
		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);
		//Choose to which attributes has the principal access
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attribute), user , facility)) throw new PrivilegeException("Principal has no access to check attribute = " + new AttributeDefinition(attribute));

		getAttributesManagerBl().checkAttributeValue(sess, facility, user, attribute);
	}

	@Override
	public void checkAttributesValue(PerunSession sess, Facility facility, User user, List<Attribute> attributes) throws PrivilegeException, InternalErrorException, FacilityNotExistsException, UserNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException, AttributeNotExistsException {
		Utils.checkPerunSession(sess);
		getAttributesManagerBl().checkAttributesExists(sess, attributes);
		getPerunBl().getFacilitiesManagerBl().checkFacilityExists(sess, facility);
		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);
		//Choose to which attributes has the principal access
		for(Attribute attr: attributes) {
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attr), user , facility)) throw new PrivilegeException("Principal has no access to check attribute = " + new AttributeDefinition(attr));
		}
		getAttributesManagerBl().checkAttributesValue(sess, facility, user, attributes);
	}

	@Override
	public void checkAttributeValue(PerunSession sess, User user, Attribute attribute) throws PrivilegeException, InternalErrorException, UserNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException, AttributeNotExistsException {
		Utils.checkPerunSession(sess);
		getAttributesManagerBl().checkAttributeExists(sess, attribute);
		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);
		//Choose to which attributes has the principal access
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attribute), user )) throw new PrivilegeException("Principal has no access to check attribute = " + new AttributeDefinition(attribute));

		getAttributesManagerBl().checkAttributeValue(sess, user, attribute);
	}

	@Override
	public void checkAttributeValue(PerunSession sess, Host host, Attribute attribute) throws PrivilegeException, InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, AttributeNotExistsException, HostNotExistsException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getAttributesManagerBl().checkAttributeExists(sess, attribute);
		getPerunBl().getFacilitiesManagerBl().checkHostExists(sess, host);
		//Choose to which attributes has the principal access
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attribute), host )) throw new PrivilegeException("Principal has no access to check attribute = " + new AttributeDefinition(attribute));

		getAttributesManagerBl().checkAttributeValue(sess, host, attribute);
	}

	@Override
	public void checkAttributesValue(PerunSession sess, Host host, List<Attribute> attributes) throws InternalErrorException, PrivilegeException, AttributeNotExistsException, HostNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getAttributesManagerBl().checkAttributesExists(sess, attributes);
		getPerunBl().getFacilitiesManagerBl().checkHostExists(sess, host);
		//Choose to which attributes has the principal access
		for(Attribute attr: attributes) {
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attr), host )) throw new PrivilegeException("Principal has no access to check attribute = " + new AttributeDefinition(attr));
		}
		getAttributesManagerBl().checkAttributesValue(sess, host, attributes);
	}

	@Override
	public void checkAttributeValue(PerunSession sess, Group group, Attribute attribute) throws PrivilegeException, InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException, AttributeNotExistsException, GroupNotExistsException {
		Utils.checkPerunSession(sess);
		getAttributesManagerBl().checkAttributeExists(sess, attribute);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);
		//Choose to which attributes has the principal access
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attribute), group )) throw new PrivilegeException("Principal has no access to check attribute = " + new AttributeDefinition(attribute));

		getAttributesManagerBl().checkAttributeValue(sess, group, attribute);
	}

	@Override
	public void checkAttributesValue(PerunSession sess, User user, List<Attribute> attributes) throws PrivilegeException, InternalErrorException, UserNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException, AttributeNotExistsException {
		Utils.checkPerunSession(sess);
		getAttributesManagerBl().checkAttributesExists(sess, attributes);
		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);
		//Choose to which attributes has the principal access
		for(Attribute attr: attributes) {
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attr), user )) throw new PrivilegeException("Principal has no access to check attribute = " + new AttributeDefinition(attr));
		}
		getAttributesManagerBl().checkAttributesValue(sess, user, attributes);
	}

	@Override
	public void checkAttributeValue(PerunSession sess, Resource resource, Group group, Attribute attribute) throws PrivilegeException, InternalErrorException, AttributeNotExistsException, WrongAttributeValueException, GroupNotExistsException, ResourceNotExistsException, GroupResourceMismatchException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getAttributesManagerBl().checkAttributeExists(sess, attribute);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);

		if(!getPerunBl().getGroupsManagerBl().getVo(sess, group).equals(getPerunBl().getResourcesManagerBl().getVo(sess, resource))) {
			throw new GroupResourceMismatchException("group and resource are not in the same VO");
		}
		//Choose to which attributes has the principal access
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attribute), group , resource)) throw new PrivilegeException("Principal has no access to check attribute = " + new AttributeDefinition(attribute));

		getAttributesManagerBl().checkAttributeValue(sess, resource, group, attribute);
	}

	@Override
	public void checkAttributesValue(PerunSession sess, Resource resource, Group group, List<Attribute> attributes) throws PrivilegeException, InternalErrorException, AttributeNotExistsException, ResourceNotExistsException, GroupNotExistsException, WrongAttributeAssignmentException, WrongAttributeValueException, GroupResourceMismatchException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getAttributesManagerBl().checkAttributesExists(sess, attributes);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);

		if(!getPerunBl().getGroupsManagerBl().getVo(sess, group).equals(getPerunBl().getResourcesManagerBl().getVo(sess, resource))) {
			throw new GroupResourceMismatchException("group and resource are not in the same VO");
		}
		//Choose to which attributes has the principal access
		for(Attribute attr: attributes) {
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attr), group , resource)) throw new PrivilegeException("Principal has no access to check attribute = " + new AttributeDefinition(attr));
		}
		getAttributesManagerBl().checkAttributesValue(sess, resource, group, attributes);
	}

	public void checkAttributesValue(PerunSession sess, Resource resource, Group group, List<Attribute> attributes, boolean workWithGroupAttribute) throws PrivilegeException, InternalErrorException, AttributeNotExistsException, ResourceNotExistsException, GroupNotExistsException, WrongAttributeAssignmentException, WrongAttributeValueException, GroupResourceMismatchException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getAttributesManagerBl().checkAttributesExists(sess, attributes);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);

		if(!getPerunBl().getGroupsManagerBl().getVo(sess, group).equals(getPerunBl().getResourcesManagerBl().getVo(sess, resource))) {
			throw new GroupResourceMismatchException("group and resource are not in the same VO");
		}
		//Choose to which attributes has the principal access
		for(Attribute attr: attributes) {
			if(getAttributesManagerBl().isFromNamespace(sess, attr, NS_GROUP_RESOURCE_ATTR)) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attr), group , resource)) throw new PrivilegeException("Principal has no access to check attribute = " + new AttributeDefinition(attr));
			} else if(getAttributesManagerBl().isFromNamespace(sess, attr, NS_GROUP_ATTR)) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attr), group )) throw new PrivilegeException("Principal has no access to check attribute = " + new AttributeDefinition(attr));
			}
		}
		getAttributesManagerBl().checkAttributesValue(sess, resource, group, attributes,workWithGroupAttribute);

	}

	@Override
	public void checkAttributeValue(PerunSession sess, UserExtSource ues, Attribute attribute) throws PrivilegeException, InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException,AttributeNotExistsException, UserExtSourceNotExistsException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getAttributesManagerBl().checkAttributeExists(sess, attribute);
		getPerunBl().getUsersManagerBl().checkUserExtSourceExists(sess, ues);
		//Choose to which attributes has the principal access
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attribute), ues )) throw new PrivilegeException("Principal has no access to check attribute = " + new AttributeDefinition(attribute));

		getAttributesManagerBl().checkAttributeValue(sess, ues, attribute);
	}

	@Override
	public void checkAttributesValue(PerunSession sess, UserExtSource ues, List<Attribute> attributes) throws InternalErrorException, PrivilegeException, AttributeNotExistsException, UserExtSourceNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException,  WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getAttributesManagerBl().checkAttributesExists(sess, attributes);
		getPerunBl().getUsersManagerBl().checkUserExtSourceExists(sess, ues);
		//Choose to which attributes has the principal access
		for(Attribute attr: attributes) {
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attr), ues )) throw new PrivilegeException("Principal has no access to check attribute = " + new AttributeDefinition(attr));
		}
		getAttributesManagerBl().checkAttributesValue(sess, ues, attributes);
	}

	@Override
	public void removeAttribute(PerunSession sess, Facility facility, AttributeDefinition attribute) throws InternalErrorException, PrivilegeException, AttributeNotExistsException, FacilityNotExistsException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getPerunBl().getFacilitiesManagerBl().checkFacilityExists(sess, facility);
		getAttributesManagerBl().checkAttributeExists(sess, attribute);
		//Choose to which attributes has the principal access
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attribute, facility )) throw new PrivilegeException("Principal has no access to remove attribute = " + new AttributeDefinition(attribute));

		getAttributesManagerBl().removeAttribute(sess, facility, attribute);
	}

	@Override
	public void removeAttribute(PerunSession sess, String key, AttributeDefinition attribute) throws InternalErrorException, WrongAttributeAssignmentException, PrivilegeException, AttributeNotExistsException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getAttributesManagerBl().checkAttributeExists(sess, attribute);
		//Choose to which attributes has the principal access
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attribute, key)) throw new PrivilegeException("Principal has no access to remove attribute = " + new AttributeDefinition(attribute));

		getAttributesManagerBl().removeAttribute(sess, key, attribute);
	}

	@Override
	public void removeAttributes(PerunSession sess, Resource resource, Group group, List<? extends AttributeDefinition> attributes, boolean workWithGroupAttributes) throws InternalErrorException, PrivilegeException, AttributeNotExistsException, GroupNotExistsException, ResourceNotExistsException, GroupResourceMismatchException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getAttributesManagerBl().checkAttributesExists(sess, attributes);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		if(!getPerunBl().getGroupsManagerBl().getVo(sess, group).equals(getPerunBl().getResourcesManagerBl().getVo(sess, resource))) {
			throw new GroupResourceMismatchException("group and resource are not in the same VO");
		}
		//Choose to which attributes has the principal access
		for(AttributeDefinition attrDef: attributes) {
			if(getAttributesManagerBl().isFromNamespace(sess, attrDef, NS_GROUP_ATTR)) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrDef, group )) throw new PrivilegeException("Principal has no access to remove attribute = " + attrDef);
			} else if(getAttributesManagerBl().isFromNamespace(sess, attrDef, NS_GROUP_RESOURCE_ATTR)) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrDef, group , resource)) throw new PrivilegeException("Principal has no access to remove attribute = " + attrDef);
			} else {
				throw new WrongAttributeAssignmentException("There is some attribute which is not type of any possible choice.");
			}
		}
		getAttributesManagerBl().removeAttributes(sess, resource, group, attributes, workWithGroupAttributes);
	}

	@Override
	public void removeAttributes(PerunSession sess, Facility facility, List<? extends AttributeDefinition> attributes) throws InternalErrorException, PrivilegeException, AttributeNotExistsException, FacilityNotExistsException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getPerunBl().getFacilitiesManagerBl().checkFacilityExists(sess, facility);
		getAttributesManagerBl().checkAttributesExists(sess, attributes);
		//Choose to which attributes has the principal access
		for(AttributeDefinition attrDef: attributes) {
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrDef, facility )) throw new PrivilegeException("Principal has no access to remove attribute = " + attrDef);
		}
		getAttributesManagerBl().removeAttributes(sess, facility, attributes);
	}

	@Override
	public void removeAttributes(PerunSession sess, Facility facility, Resource resource, User user, Member member, List<? extends AttributeDefinition> attributes) throws PrivilegeException, ResourceNotExistsException, InternalErrorException, MemberNotExistsException, FacilityNotExistsException, UserNotExistsException, AttributeNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException, MemberResourceMismatchException {
		Utils.checkPerunSession(sess);
		getPerunBl().getFacilitiesManagerBl().checkFacilityExists(sess, facility);
		getAttributesManagerBl().checkAttributesExists(sess, attributes);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		//Choose to which attributes has the principal access
		for(AttributeDefinition attrDef: attributes) {
			if(getAttributesManagerBl().isFromNamespace(sess, attrDef, NS_MEMBER_ATTR)) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrDef, member )) throw new PrivilegeException("Principal has no access to remove attribute = " + attrDef);
			} else if(getAttributesManagerBl().isFromNamespace(sess, attrDef, NS_MEMBER_RESOURCE_ATTR)) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrDef, member , resource)) throw new PrivilegeException("Principal has no access to remove attribute = " + attrDef);
			} else if(getAttributesManagerBl().isFromNamespace(sess, attrDef, NS_USER_ATTR)) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrDef, user )) throw new PrivilegeException("Principal has no access to remove attribute = " + attrDef);
			} else if(getAttributesManagerBl().isFromNamespace(sess, attrDef, NS_USER_FACILITY_ATTR)) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrDef, user , facility)) throw new PrivilegeException("Principal has no access to remove attribute = " + attrDef);
			} else {
				throw new WrongAttributeAssignmentException("There is some attribute which is not type of any possible choice.");
			}
		}
		getAttributesManagerBl().removeAttributes(sess, facility, resource, user, member, attributes);
	}

	@Override
	public void removeAttributes(PerunSession sess, Facility facility, Resource resource, Group group, User user, Member member, List<? extends AttributeDefinition> attributes) throws PrivilegeException, ResourceNotExistsException, InternalErrorException, MemberNotExistsException, FacilityNotExistsException, UserNotExistsException, AttributeNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException, GroupNotExistsException, GroupResourceMismatchException, MemberResourceMismatchException {
		Utils.checkPerunSession(sess);
		getPerunBl().getFacilitiesManagerBl().checkFacilityExists(sess, facility);
		getAttributesManagerBl().checkAttributesExists(sess, attributes);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		//Choose to which attributes has the principal access
		for(AttributeDefinition attrDef: attributes) {
			if(getAttributesManagerBl().isFromNamespace(sess, attrDef, NS_MEMBER_ATTR)) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrDef, member )) throw new PrivilegeException("Principal has no access to remove attribute = " + attrDef);
			} else if(getAttributesManagerBl().isFromNamespace(sess, attrDef, NS_MEMBER_RESOURCE_ATTR)) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrDef, member , resource)) throw new PrivilegeException("Principal has no access to remove attribute = " + attrDef);
			} else if(getAttributesManagerBl().isFromNamespace(sess, attrDef, NS_USER_ATTR)) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrDef, user )) throw new PrivilegeException("Principal has no access to remove attribute = " + attrDef);
			} else if(getAttributesManagerBl().isFromNamespace(sess, attrDef, NS_USER_FACILITY_ATTR)) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrDef, user , facility)) throw new PrivilegeException("Principal has no access to remove attribute = " + attrDef);
			} else if(getAttributesManagerBl().isFromNamespace(sess, attrDef, NS_MEMBER_GROUP_ATTR)) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrDef, member, group)) throw new PrivilegeException("Principal has no access to remove attribute = " + attrDef);
			} else {
				throw new WrongAttributeAssignmentException("There is some attribute which is not type of any possible choice.");
			}
		}
		getAttributesManagerBl().removeAttributes(sess, facility, resource, group, user, member, attributes);
	}

	@Override
	public void removeAttributes(PerunSession sess, Member member, boolean workWithUserAttributes, List<? extends AttributeDefinition> attributes) throws InternalErrorException, PrivilegeException, AttributeNotExistsException, MemberNotExistsException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		getAttributesManagerBl().checkAttributesExists(sess, attributes);
		//Choose to which attributes has the principal access
		for(AttributeDefinition attrDef: attributes) {
			if(getAttributesManagerBl().isFromNamespace(sess, attrDef, NS_MEMBER_ATTR)) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrDef, member )) throw new PrivilegeException("Principal has no access to remove attribute = " + attrDef);
			} else if(getAttributesManagerBl().isFromNamespace(sess, attrDef, NS_USER_ATTR)) {
				User u = getPerunBl().getUsersManagerBl().getUserByMember(sess, member);
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrDef, u )) throw new PrivilegeException("Principal has no access to remove attribute = " + attrDef);
			} else {
				throw new WrongAttributeAssignmentException("There is some attribute which is not type of any possible choice.");
			}
		}
		getAttributesManagerBl().removeAttributes(sess, member, workWithUserAttributes, attributes);
	}

	@Override
	public void removeAllAttributes(PerunSession sess, Facility facility) throws InternalErrorException, PrivilegeException, FacilityNotExistsException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getPerunBl().getFacilitiesManagerBl().checkFacilityExists(sess, facility);
		//Choose if principal has access to remove all attributes
		List<Attribute> allAttributes = getPerunBl().getAttributesManagerBl().getAttributes(sess, facility);
		for(Attribute attr: allAttributes) {
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attr, facility )) throw new PrivilegeException("Principal has no access to remove attribute = " + new AttributeDefinition(attr));
		}
		getAttributesManagerBl().removeAllAttributes(sess, facility);
	}

	@Override
	public void removeAllAttributes(PerunSession sess, Resource resource, Group group, boolean workWithGroupAttributes) throws InternalErrorException, PrivilegeException, GroupNotExistsException, ResourceNotExistsException, GroupResourceMismatchException, WrongAttributeValueException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException {
		Utils.checkPerunSession(sess);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		if(!getPerunBl().getGroupsManagerBl().getVo(sess, group).equals(getPerunBl().getResourcesManagerBl().getVo(sess, resource))) {
			throw new GroupResourceMismatchException("group and resource are not in the same VO");
		}
		List<Attribute> allAttributes = getPerunBl().getAttributesManagerBl().getAttributes(sess, resource, group, workWithGroupAttributes);
		//Choose to which attributes has the principal access
		for(AttributeDefinition attrDef: allAttributes) {
			if(getAttributesManagerBl().isFromNamespace(sess, attrDef, NS_GROUP_ATTR)) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrDef, group )) throw new PrivilegeException("Principal has no access to remove attribute = " + attrDef);
			} else if(getAttributesManagerBl().isFromNamespace(sess, attrDef, NS_GROUP_RESOURCE_ATTR)) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrDef, group , resource)) throw new PrivilegeException("Principal has no access to remove attribute = " + attrDef);
			} else {
				throw new ConsistencyErrorException("There is some attribute which is not type of any possible choice.");
			}
		}
		getAttributesManagerBl().removeAllAttributes(sess, resource, group, workWithGroupAttributes);
	}

	@Override
	public void removeAllAttributes(PerunSession sess, Facility facility, boolean removeAlsoUserFacilityAttributes) throws InternalErrorException, PrivilegeException, FacilityNotExistsException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getPerunBl().getFacilitiesManagerBl().checkFacilityExists(sess, facility);
		//Choose if principal has access to remove all attributes
		List<Attribute> allAttributes = getPerunBl().getAttributesManagerBl().getUserFacilityAttributesForAnyUser(sess, facility);
		for(Attribute attr: allAttributes) {
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attr, facility )) throw new PrivilegeException("Principal has no access to remove attribute = " + new AttributeDefinition(attr));
		}
		getAttributesManagerBl().removeAllAttributes(sess, facility, removeAlsoUserFacilityAttributes);
	}

	@Override
	public void removeAttribute(PerunSession sess, Vo vo, AttributeDefinition attribute) throws InternalErrorException, PrivilegeException, AttributeNotExistsException, VoNotExistsException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);
		getAttributesManagerBl().checkAttributeExists(sess, attribute);
		//Choose to which attributes has the principal access
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attribute, vo )) throw new PrivilegeException("Principal has no access to remove attribute = " + new AttributeDefinition(attribute));

		getAttributesManagerBl().removeAttribute(sess, vo, attribute);
	}

	@Override
	public void removeAttributes(PerunSession sess, Vo vo, List<? extends AttributeDefinition> attributes) throws InternalErrorException, PrivilegeException, AttributeNotExistsException, VoNotExistsException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);
		getAttributesManagerBl().checkAttributesExists(sess, attributes);
		//Choose to which attributes has the principal access
		for(AttributeDefinition attrDef: attributes) {
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrDef, vo )) throw new PrivilegeException("Principal has no access to remove attribute = " + attrDef);
		}
		getAttributesManagerBl().removeAttributes(sess, vo, attributes);
	}

	@Override
	public void removeAllAttributes(PerunSession sess, Vo vo) throws InternalErrorException, PrivilegeException, VoNotExistsException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);
		//Choose if principal has access to remove all attributes
		List<Attribute> allAttributes = getPerunBl().getAttributesManagerBl().getAttributes(sess, vo);
		for(Attribute attr: allAttributes) {
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attr, vo )) throw new PrivilegeException("Principal has no access to remove attribute = " + new AttributeDefinition(attr));
		}
		getAttributesManagerBl().removeAllAttributes(sess, vo);
	}

	@Override
	public void removeAttribute(PerunSession sess, Group group, AttributeDefinition attribute) throws InternalErrorException, PrivilegeException, AttributeNotExistsException, GroupNotExistsException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);
		getAttributesManagerBl().checkAttributeExists(sess, attribute);
		//Choose to which attributes has the principal access
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attribute, group )) throw new PrivilegeException("Principal has no access to remove attribute = " + new AttributeDefinition(attribute));

		getAttributesManagerBl().removeAttribute(sess, group, attribute);
	}

	@Override
	public void removeAttributes(PerunSession sess, Group group, List<? extends AttributeDefinition> attributes) throws InternalErrorException, PrivilegeException, AttributeNotExistsException, GroupNotExistsException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);
		getAttributesManagerBl().checkAttributesExists(sess, attributes);
		//Choose to which attributes has the principal access
		for(AttributeDefinition attrDef: attributes) {
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrDef, group )) throw new PrivilegeException("Principal has no access to remove attribute = " + attrDef);
		}
		getAttributesManagerBl().removeAttributes(sess, group, attributes);
	}

	@Override
	public void removeAllAttributes(PerunSession sess, Group group) throws InternalErrorException, PrivilegeException, GroupNotExistsException, WrongAttributeValueException, WrongReferenceAttributeValueException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);
		//Choose if principal has access to remove all attributes
		List<Attribute> allAttributes = getPerunBl().getAttributesManagerBl().getAttributes(sess, group);
		for(Attribute attr: allAttributes) {
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attr, group )) throw new PrivilegeException("Principal has no access to remove attribute = " + new AttributeDefinition(attr));
		}
		getAttributesManagerBl().removeAllAttributes(sess, group);
	}

	@Override
	public void removeAttribute(PerunSession sess, Resource resource, AttributeDefinition attribute) throws InternalErrorException, PrivilegeException, AttributeNotExistsException, ResourceNotExistsException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		getAttributesManagerBl().checkAttributeExists(sess, attribute);
		//Choose to which attributes has the principal access
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attribute, resource )) throw new PrivilegeException("Principal has no access to remove attribute = " + new AttributeDefinition(attribute));

		getAttributesManagerBl().removeAttribute(sess, resource, attribute);
	}

	@Override
	public void removeAttributes(PerunSession sess, Resource resource, List<? extends AttributeDefinition> attributes) throws InternalErrorException, PrivilegeException, AttributeNotExistsException, ResourceNotExistsException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		getAttributesManagerBl().checkAttributesExists(sess, attributes);
		//Choose to which attributes has the principal access
		for(AttributeDefinition attrDef: attributes) {
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrDef, resource )) throw new PrivilegeException("Principal has no access to remove attribute = " + attrDef);
		}
		getAttributesManagerBl().removeAttributes(sess, resource, attributes);
	}

	@Override
	public void removeAllAttributes(PerunSession sess, Resource resource) throws InternalErrorException, PrivilegeException, ResourceNotExistsException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		//Choose if principal has access to remove all attributes
		List<Attribute> allAttributes = getPerunBl().getAttributesManagerBl().getAttributes(sess, resource);
		for(Attribute attr: allAttributes) {
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attr, resource )) throw new PrivilegeException("Principal has no access to remove attribute = " + new AttributeDefinition(attr));
		}
		getAttributesManagerBl().removeAllAttributes(sess, resource);
	}

	@Override
	public void removeAttribute(PerunSession sess, Member member, Resource resource, AttributeDefinition attribute) throws InternalErrorException, PrivilegeException, AttributeNotExistsException, MemberNotExistsException, ResourceNotExistsException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException, MemberResourceMismatchException {
		Utils.checkPerunSession(sess);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		getAttributesManagerBl().checkAttributeExists(sess, attribute);
		//Choose to which attributes has the principal access
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attribute, member , resource)) throw new PrivilegeException("Principal has no access to remove attribute = " + new AttributeDefinition(attribute));

		getAttributesManagerBl().removeAttribute(sess, member, resource, attribute);
	}

	@Override
	public void removeAttributes(PerunSession sess, Member member, Resource resource, List<? extends AttributeDefinition> attributes) throws InternalErrorException, PrivilegeException, AttributeNotExistsException, MemberNotExistsException, ResourceNotExistsException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException, MemberResourceMismatchException {
		Utils.checkPerunSession(sess);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		getAttributesManagerBl().checkAttributesExists(sess, attributes);
		//Choose to which attributes has the principal access
		for(AttributeDefinition attrDef: attributes) {
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrDef, member , resource)) throw new PrivilegeException("Principal has no access to remove attribute = " + attrDef);
		}
		getAttributesManagerBl().removeAttributes(sess, member, resource, attributes);
	}

	@Override
	public void removeAllAttributes(PerunSession sess, Member member, Resource resource) throws InternalErrorException, PrivilegeException, MemberNotExistsException, ResourceNotExistsException, WrongAttributeValueException, WrongReferenceAttributeValueException, MemberResourceMismatchException {
		Utils.checkPerunSession(sess);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		//Choose if principal has access to remove all attributes
		List<Attribute> allAttributes = getPerunBl().getAttributesManagerBl().getAttributes(sess, member, resource);
		for(Attribute attr: allAttributes) {
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attr, member , resource)) throw new PrivilegeException("Principal has no access to remove attribute = " + new AttributeDefinition(attr));
		}
		getAttributesManagerBl().removeAllAttributes(sess, member, resource);
	}

	@Override
	public void removeAttribute(PerunSession sess, Member member, Group group, AttributeDefinition attribute) throws InternalErrorException, PrivilegeException, AttributeNotExistsException, MemberNotExistsException, GroupNotExistsException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);
		getAttributesManagerBl().checkAttributeExists(sess, attribute);
		//Choose to which attributes has the principal access
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attribute, member, group)) throw new PrivilegeException("Principal has no access to remove attribute = " + new AttributeDefinition(attribute));

		getAttributesManagerBl().removeAttribute(sess, member, group, attribute);
	}

	@Override
	public void removeAttributes(PerunSession sess, Member member, Group group, List<? extends AttributeDefinition> attributes) throws InternalErrorException, PrivilegeException, AttributeNotExistsException, MemberNotExistsException, GroupNotExistsException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		removeAttributes(sess, member, group, attributes, false);
	}

	@Override
	public void removeAttributes(PerunSession sess, Member member, Group group, List<? extends AttributeDefinition> attributes, boolean workWithUserAttributes) throws InternalErrorException, PrivilegeException, AttributeNotExistsException, MemberNotExistsException, GroupNotExistsException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);
		getAttributesManagerBl().checkAttributesExists(sess, attributes);
		//Choose to which attributes has the principal access
		for(AttributeDefinition attrDef: attributes) {
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrDef, member, group)) throw new PrivilegeException("Principal has no access to remove attribute = " + attrDef);
		}
		getAttributesManagerBl().removeAttributes(sess, member, group, attributes, workWithUserAttributes);
	}

	@Override
	public void removeAllAttributes(PerunSession sess, Member member, Group group) throws InternalErrorException, WrongAttributeAssignmentException, PrivilegeException, MemberNotExistsException, GroupNotExistsException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);
		//Choose to which attributes has the principal access
		List<Attribute> allAttributes = getPerunBl().getAttributesManagerBl().getAttributes(sess, member, group);
		for(Attribute attr: allAttributes) {
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attr, member, group)) throw new PrivilegeException("Principal has no access to remove attribute = " + new AttributeDefinition(attr));
		}
		getAttributesManagerBl().removeAllAttributes(sess, member, group);
	}

	@Override
	public void removeAttribute(PerunSession sess, Member member, AttributeDefinition attribute) throws InternalErrorException, PrivilegeException, AttributeNotExistsException, MemberNotExistsException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		getAttributesManagerBl().checkAttributeExists(sess, attribute);
		//Choose to which attributes has the principal access
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attribute, member )) throw new PrivilegeException("Principal has no access to remove attribute = " + new AttributeDefinition(attribute));

		getAttributesManagerBl().removeAttribute(sess, member, attribute);
	}

	@Override
	public void removeAttributes(PerunSession sess, Member member, List<? extends AttributeDefinition> attributes) throws InternalErrorException, PrivilegeException, AttributeNotExistsException, MemberNotExistsException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		getAttributesManagerBl().checkAttributesExists(sess, attributes);
		//Choose to which attributes has the principal access
		for(AttributeDefinition attrDef: attributes) {
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrDef, member )) throw new PrivilegeException("Principal has no access to remove attribute = " + attrDef);
		}
		getAttributesManagerBl().removeAttributes(sess, member, attributes);
	}

	@Override
	public void removeAllAttributes(PerunSession sess, Member member) throws InternalErrorException, PrivilegeException, MemberNotExistsException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		//Choose if principal has access to remove all attributes
		List<Attribute> allAttributes = getPerunBl().getAttributesManagerBl().getAttributes(sess, member);
		for(Attribute attr: allAttributes) {
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attr, member )) throw new PrivilegeException("Principal has no access to remove attribute = " + new AttributeDefinition(attr));
		}
		getAttributesManagerBl().removeAllAttributes(sess, member);
	}

	@Override
	public void removeAttribute(PerunSession sess, Facility facility, User user, AttributeDefinition attribute) throws InternalErrorException, PrivilegeException, AttributeNotExistsException, UserNotExistsException, FacilityNotExistsException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getPerunBl().getFacilitiesManagerBl().checkFacilityExists(sess, facility);
		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);
		getAttributesManagerBl().checkAttributeExists(sess, attribute);
		//Choose to which attributes has the principal access
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attribute, user , facility)) throw new PrivilegeException("Principal has no access to remove attribute = " + new AttributeDefinition(attribute));

		getAttributesManagerBl().removeAttribute(sess, facility, user, attribute);
	}

	@Override
	public void removeAttributes(PerunSession sess, Facility facility, User user, List<? extends AttributeDefinition> attributes) throws InternalErrorException, PrivilegeException, AttributeNotExistsException, UserNotExistsException, FacilityNotExistsException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getPerunBl().getFacilitiesManagerBl().checkFacilityExists(sess, facility);
		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);
		getAttributesManagerBl().checkAttributesExists(sess, attributes);
		//Choose to which attributes has the principal access
		for(AttributeDefinition attrDef: attributes) {
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrDef, user , facility)) throw new PrivilegeException("Principal has no access to remove attribute = " + attrDef);
		}
		getAttributesManagerBl().removeAttributes(sess, facility, user, attributes);
	}

	@Override
	public void removeAllAttributes(PerunSession sess, Facility facility, User user) throws InternalErrorException, PrivilegeException, UserNotExistsException, FacilityNotExistsException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getPerunBl().getFacilitiesManagerBl().checkFacilityExists(sess, facility);
		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);
		//Choose if principal has access to remove all attributes
		List<Attribute> allAttributes = getPerunBl().getAttributesManagerBl().getAttributes(sess, facility, user);
		for(Attribute attr: allAttributes) {
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attr, user , facility)) throw new PrivilegeException("Principal has no access to remove attribute = " + new AttributeDefinition(attr));
		}
		getAttributesManagerBl().removeAllAttributes(sess, facility, user);
	}

	@Override
	public void removeAttribute(PerunSession sess, User user, AttributeDefinition attribute) throws InternalErrorException, PrivilegeException, AttributeNotExistsException, UserNotExistsException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);
		getAttributesManagerBl().checkAttributeExists(sess, attribute);
		//Choose to which attributes has the principal access
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attribute, user )) throw new PrivilegeException("Principal has no access to remove attribute = " + new AttributeDefinition(attribute));

		getAttributesManagerBl().removeAttribute(sess, user, attribute);
	}

	@Override
	public void removeAttributes(PerunSession sess, User user, List<? extends AttributeDefinition> attributes) throws InternalErrorException, PrivilegeException, AttributeNotExistsException, UserNotExistsException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);
		getAttributesManagerBl().checkAttributesExists(sess, attributes);
		//Choose to which attributes has the principal access
		for(AttributeDefinition attrDef: attributes) {
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrDef, user )) throw new PrivilegeException("Principal has no access to remove attribute = " + attrDef);
		}
		getAttributesManagerBl().removeAttributes(sess, user, attributes);
	}

	@Override
	public void removeAllAttributes(PerunSession sess, User user) throws InternalErrorException, PrivilegeException, UserNotExistsException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);
		//Choose if principal has access to remove all attributes
		List<Attribute> allAttributes = getPerunBl().getAttributesManagerBl().getAttributes(sess, user);
		for(Attribute attr: allAttributes) {
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attr, user )) throw new PrivilegeException("Principal has no access to remove attribute = " + new AttributeDefinition(attr));
		}
		getAttributesManagerBl().removeAllAttributes(sess, user);
	}

	@Override
	public void removeAttribute(PerunSession sess, Host host, AttributeDefinition attribute) throws PrivilegeException, InternalErrorException, HostNotExistsException, AttributeNotExistsException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getPerunBl().getFacilitiesManagerBl().checkHostExists(sess, host);
		getAttributesManagerBl().checkAttributeExists(sess, attribute);
		//Choose to which attributes has the principal access
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attribute, host )) throw new PrivilegeException("Principal has no access to remove attribute = " + new AttributeDefinition(attribute));

		getAttributesManagerBl().removeAttribute(sess, host, attribute);

	}

	@Override
	public void removeAttributes(PerunSession sess, Host host, List<? extends AttributeDefinition> attributes) throws PrivilegeException, InternalErrorException, HostNotExistsException, AttributeNotExistsException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getPerunBl().getFacilitiesManagerBl().checkHostExists(sess, host);
		getAttributesManagerBl().checkAttributesExists(sess, attributes);
		//Choose to which attributes has the principal access
		for(AttributeDefinition attrDef: attributes) {
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrDef, host )) throw new PrivilegeException("Principal has no access to remove attribute = " + attrDef);
		}
		getAttributesManagerBl().removeAttributes(sess, host, attributes);
	}

	@Override
	public void removeAllAttributes(PerunSession sess, Host host) throws PrivilegeException, InternalErrorException, HostNotExistsException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getPerunBl().getFacilitiesManagerBl().checkHostExists(sess, host);
		//Choose if principal has access to remove all attributes
		List<Attribute> allAttributes = getPerunBl().getAttributesManagerBl().getAttributes(sess, host);
		for(Attribute attr: allAttributes) {
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attr, host )) throw new PrivilegeException("Principal has no access to remove attribute = " + new AttributeDefinition(attr));
		}
		getAttributesManagerBl().removeAllAttributes(sess, host);
	}

	@Override
	public void removeAttribute(PerunSession sess, Resource resource, Group group, AttributeDefinition attribute) throws PrivilegeException, InternalErrorException, ResourceNotExistsException, GroupNotExistsException, WrongAttributeAssignmentException, GroupResourceMismatchException, AttributeNotExistsException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getAttributesManagerBl().checkAttributeExists(sess, attribute);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);

		if(!getPerunBl().getGroupsManagerBl().getVo(sess, group).equals(getPerunBl().getResourcesManagerBl().getVo(sess, resource))) {
			throw new GroupResourceMismatchException("group and resource are not in the same VO");
		}
		//Choose to which attributes has the principal access
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attribute, group , resource)) throw new PrivilegeException("Principal has no access to remove attribute = " + new AttributeDefinition(attribute));

		getAttributesManagerBl().removeAttribute(sess, resource, group, attribute);
	}

	@Override
	public void removeAttributes(PerunSession sess, Resource resource, Group group, List<? extends AttributeDefinition> attributes) throws PrivilegeException, InternalErrorException, ResourceNotExistsException, GroupNotExistsException, WrongAttributeAssignmentException, AttributeNotExistsException, GroupResourceMismatchException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getAttributesManagerBl().checkAttributesExists(sess, attributes);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);
		//Choose to which attributes has the principal access
		for(AttributeDefinition attrDef: attributes) {
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrDef, group , resource)) throw new PrivilegeException("Principal has no access to remove attribute = " + attrDef);
		}
		if(!getPerunBl().getGroupsManagerBl().getVo(sess, group).equals(getPerunBl().getResourcesManagerBl().getVo(sess, resource))) {
			throw new GroupResourceMismatchException("group and resource are not in the same VO");
		}
		getAttributesManagerBl().removeAttributes(sess, resource, group, attributes);
	}

	@Override
	public void removeAllAttributes(PerunSession sess, Resource resource, Group group) throws PrivilegeException, WrongAttributeAssignmentException, InternalErrorException, ResourceNotExistsException, GroupNotExistsException, GroupResourceMismatchException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);

		if(!getPerunBl().getGroupsManagerBl().getVo(sess, group).equals(getPerunBl().getResourcesManagerBl().getVo(sess, resource))) {
			throw new GroupResourceMismatchException("group and resource are not in the same VO");
		}
		//Choose if principal has access to remove all attributes
		List<Attribute> allAttributes = getPerunBl().getAttributesManagerBl().getAttributes(sess, resource, group);
		for(Attribute attr: allAttributes) {
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attr, group , resource)) throw new PrivilegeException("Principal has no access to remove attribute = " + new AttributeDefinition(attr));
		}
		getAttributesManagerBl().removeAllAttributes(sess, resource, group);
	}

	@Override
	public void removeAttribute(PerunSession sess, UserExtSource ues, AttributeDefinition attribute) throws InternalErrorException, PrivilegeException, AttributeNotExistsException, UserExtSourceNotExistsException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getPerunBl().getUsersManagerBl().checkUserExtSourceExists(sess, ues);
		getAttributesManagerBl().checkAttributeExists(sess, attribute);
		//Choose to which attributes has the principal access
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attribute, ues )) throw new PrivilegeException("Principal has no access to remove attribute = " + new AttributeDefinition(attribute));

		getAttributesManagerBl().removeAttribute(sess, ues, attribute);
	}

	@Override
	public void removeAttributes(PerunSession sess, UserExtSource ues, List<? extends AttributeDefinition> attributes) throws InternalErrorException, PrivilegeException, AttributeNotExistsException, UserExtSourceNotExistsException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getPerunBl().getUsersManagerBl().checkUserExtSourceExists(sess, ues);
		getAttributesManagerBl().checkAttributesExists(sess, attributes);
		//Choose to which attributes has the principal access
		for(AttributeDefinition attrDef: attributes) {
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrDef, ues )) throw new PrivilegeException("Principal has no access to remove attribute = " + attrDef);
		}
		getAttributesManagerBl().removeAttributes(sess, ues, attributes);
	}

	@Override
	public void removeAllAttributes(PerunSession sess, UserExtSource ues) throws InternalErrorException, PrivilegeException, UserExtSourceNotExistsException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getPerunBl().getUsersManagerBl().checkUserExtSourceExists(sess, ues);
		//Choose if principal has access to remove all attributes
		List<Attribute> allAttributes = getPerunBl().getAttributesManagerBl().getAttributes(sess, ues);
		for(Attribute attr: allAttributes) {
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attr, ues )) throw new PrivilegeException("Principal has no access to remove attribute = " + new AttributeDefinition(attr));
		}
		getAttributesManagerBl().removeAllAttributes(sess, ues);
	}

	@Override
	public boolean isOptAttribute(PerunSession sess, AttributeDefinition attribute) {
		return getAttributesManagerBl().isOptAttribute(sess, attribute);
	}

	@Override
	public boolean isCoreAttribute(PerunSession sess, AttributeDefinition attribute) {
		return getAttributesManagerBl().isCoreAttribute(sess, attribute);
	}

	@Override
	public boolean isCoreManagedAttribute(PerunSession sess, AttributeDefinition attribute) {
		return getAttributesManagerBl().isCoreManagedAttribute(sess, attribute);
	}

	@Override
	public boolean isFromNamespace(PerunSession sess, AttributeDefinition attribute, String namespace) {
		return getAttributesManagerBl().isFromNamespace(sess, attribute, namespace);
	}

	@Override
	public void checkNamespace(PerunSession sess, AttributeDefinition attribute, String namespace) throws WrongAttributeAssignmentException {
		getAttributesManagerBl().checkNamespace(sess, attribute, namespace);
	}

	@Override
	public void checkNamespace(PerunSession sess, List<? extends AttributeDefinition> attributes, String namespace) throws WrongAttributeAssignmentException {
		getAttributesManagerBl().checkNamespace(sess, attributes, namespace);
	}

	@Override
	public String getNamespaceFromAttributeName(String attributeName) throws InternalErrorException {
		Utils.notNull(attributeName, "attributeName");

		return getAttributesManagerBl().getNamespaceFromAttributeName(attributeName);
	}

	@Override
	public String getFriendlyNameFromAttributeName(String attributeName) throws InternalErrorException {
		Utils.notNull(attributeName, "attributeName");

		return getAttributesManagerBl().getFriendlyNameFromAttributeName(attributeName);
	}

	@Override
	public List<Attribute> getLogins(PerunSession sess, User user) throws InternalErrorException, PrivilegeException, UserNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);

		//Choose if principal has access to remove all attributes
		List<Attribute> logins = getAttributesManagerBl().getLogins(sess, user);

		Iterator<Attribute> loginIter = logins.iterator();
		while(loginIter.hasNext()) {
			Attribute attrNext = loginIter.next();
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, user )) loginIter.remove();
			else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, user));
		}

		return getAttributesManagerBl().getLogins(sess, user);
	}

	@Override
	public AttributeDefinition updateAttributeDefinition(PerunSession perunSession, AttributeDefinition attributeDefinition) throws AttributeNotExistsException, InternalErrorException, PrivilegeException {
		Utils.checkPerunSession(perunSession);
		Utils.notNull(attributeDefinition, "attributeDefinition");
		getAttributesManagerBl().checkAttributeExists(perunSession, attributeDefinition);

		//Choose if principal has access to update attribute
		if(!AuthzResolver.isAuthorized(perunSession, Role.PERUNADMIN)) throw new PrivilegeException("Only PerunAdmin can update AttributeDefinition");

		return getAttributesManagerBl().updateAttributeDefinition(perunSession, attributeDefinition);
	}

	@Override
	public void doTheMagic(PerunSession sess, Member member) throws InternalErrorException, WrongAttributeAssignmentException, PrivilegeException, WrongAttributeValueException, WrongReferenceAttributeValueException, MemberNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		if(!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN)) throw new PrivilegeException("This operation can do only PerunAdmin.");
		getAttributesManagerBl().doTheMagic(sess, member);
	}

	@Override
	public void doTheMagic(PerunSession sess, Member member, boolean trueMagic) throws InternalErrorException, PrivilegeException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException, MemberNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		if(!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN)) throw new PrivilegeException("This operation can do only PerunAdmin.");
		getAttributesManagerBl().doTheMagic(sess, member, trueMagic);
	}

	@Override
	public List<AttributeRights> getAttributeRights(PerunSession sess, int attributeId) throws InternalErrorException, PrivilegeException, AttributeNotExistsException {
		Utils.checkPerunSession(sess);
		// so as we can check, if the attribute exists
		getAttributeDefinitionById(sess, attributeId);

		if(!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN)) throw new PrivilegeException("This operation can do only PerunAdmin.");
		return getAttributesManagerBl().getAttributeRights(sess, attributeId);
	}

	@Override
	public void setAttributeRights(PerunSession sess, List<AttributeRights> rights) throws InternalErrorException, PrivilegeException, AttributeNotExistsException {
		Utils.checkPerunSession(sess);
		// so as we can check, if the attributes exist
		for (AttributeRights attributeright : rights) {
			getAttributeDefinitionById(sess, attributeright.getAttributeId());
		}
		if(!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN)) throw new PrivilegeException("This operation can do only PerunAdmin.");
		getAttributesManagerBl().setAttributeRights(sess, rights);
	}

	@Override
	public void convertAttributeToUnique(PerunSession session, int attrId) throws InternalErrorException, PrivilegeException, AttributeNotExistsException, AttributeAlreadyMarkedUniqueException {
		Utils.checkPerunSession(session);
		if(!AuthzResolver.isAuthorized(session, Role.PERUNADMIN)) throw new PrivilegeException("This operation can do only PerunAdmin.");
		getAttributesManagerBl().convertAttributeToUnique(session, attrId);
	}

	@Override
	public String getModulesDependenciesGraphText(PerunSession session, GraphTextFormat format) throws InternalErrorException, PrivilegeException {
		if (!AuthzResolver.isAuthorized(session, Role.PERUNADMIN)) {
			throw new PrivilegeException("This operation can be done only by PerunAdmin.");
		}

		return attributesManagerBl.getAttributeModulesDependenciesGraphAsString(session, format);
	}

	@Override
	public String getModulesDependenciesGraphText(PerunSession session, GraphTextFormat format, String attributeName) throws InternalErrorException, PrivilegeException, AttributeNotExistsException {
		if (!AuthzResolver.isAuthorized(session, Role.PERUNADMIN)) {
			throw new PrivilegeException("This operation can be done only by PerunAdmin.");
		}

		AttributeDefinition definition = attributesManagerBl.getAttributeDefinition(session, attributeName);

		return attributesManagerBl.getAttributeModulesDependenciesGraphAsString(session, format, definition);
	}

	@Override
	public Graphviz getModulesDependenciesGraphImage(PerunSession session) throws InternalErrorException, PrivilegeException {
		if (!AuthzResolver.isAuthorized(session, Role.PERUNADMIN)) {
			throw new PrivilegeException("This operation can be done only by PerunAdmin.");
		}

		return attributesManagerBl.getAttributeModulesDependenciesGraphAsImage(session);
	}

	@Override
	public Graphviz getModulesDependenciesGraphImage(PerunSession session, String attributeName) throws InternalErrorException, PrivilegeException, AttributeNotExistsException {
		if (!AuthzResolver.isAuthorized(session, Role.PERUNADMIN)) {
			throw new PrivilegeException("This operation can be done only by PerunAdmin.");
		}

		AttributeDefinition definition = attributesManagerBl.getAttributeDefinition(session, attributeName);

		return attributesManagerBl.getAttributeModulesDependenciesGraphAsImage(session, definition);
	}

	public PerunBl getPerunBl() {
		return this.perunBl;
	}

	/**
	 * Sets the perunBl for this instance.
	 *
	 * @param perunBl The perunBl.
	 */
	public void setPerunBl(PerunBl perunBl)
	{
		this.perunBl = perunBl;
	}

	/**
	 * Sets the attributesManagerBl for this instance.
	 *
	 * @param attributesManagerBl The attributesManagerBl.
	 */
	public void setAttributesManagerBl(AttributesManagerBl attributesManagerBl)
	{
		this.attributesManagerBl = attributesManagerBl;
	}

	/**
	 * Gets the attributesManagerBl for this instance.
	 *
	 * @return The getAttributesManagerBl().
	 */
	public AttributesManagerBl  getAttributesManagerBl() {
		return this.attributesManagerBl;
	}

}
